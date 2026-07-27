package com.example.araccagir;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

public class DriverHomeActivity extends AppCompatActivity {

    private ValueEventListener globalRequestListener;
    private DatabaseReference requestsRef;
    private String driverVehicleType = "Standart";
    private String lastSeenRequestId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_driver_home);

            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setOnItemSelectedListener(item -> {
                try {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_driver_map) {
                        selectedFragment = new DriverMapFragment();
                    } else if (itemId == R.id.nav_driver_history) {
                        selectedFragment = new HistoryFragment();
                    } else if (itemId == R.id.nav_driver_reviews) {
                        selectedFragment = new ReviewsFragment();
                    } else if (itemId == R.id.nav_driver_profile) {
                        selectedFragment = new ProfileFragment();
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                    }
                } catch (Exception e) {
                    android.widget.Toast.makeText(this, "Menü Hatası: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                }
                return true;
            });

            // initial loading
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new DriverMapFragment())
                        .commit();
            }

            setupGlobalNotificationListener();

        } catch (Exception e) {
            android.util.Log.e("CrashDebug", "DriverHome error", e);
            android.widget.Toast.makeText(this, "Ana Ekran Hatası: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }
    private void setupGlobalNotificationListener() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("users")
                    .child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.hasChild("vehicleType")) {
                        driverVehicleType = snapshot.child("vehicleType").getValue(String.class);
                    }
                    startGlobalRequestListener();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void startGlobalRequestListener() {
        requestsRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("ride_requests");
        globalRequestListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot reqSnapshot : snapshot.getChildren()) {
                    String status = reqSnapshot.child("status").getValue(String.class);
                    String reqVehicleType = reqSnapshot.child("vehicleType").getValue(String.class);
                    
                    if ("pending".equals(status)) {
                        if (reqVehicleType == null || reqVehicleType.equals(driverVehicleType)) {
                            String reqId = reqSnapshot.getKey();
                            if (!reqId.equals(lastSeenRequestId)) {
                                lastSeenRequestId = reqId;
                                
                                BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                                if (bottomNav.getSelectedItemId() != R.id.nav_driver_map) {
                                    try {
                                        android.net.Uri notification = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);
                                        android.media.Ringtone r = android.media.RingtoneManager.getRingtone(DriverHomeActivity.this, notification);
                                        r.play();
                                    } catch (Exception e) {}
                                    
                                    if (!isFinishing()) {
                                        new android.app.AlertDialog.Builder(DriverHomeActivity.this)
                                            .setTitle("Yeni Çağrı!")
                                            .setMessage("Yakınınızda yeni bir yolcu aracı çağırıyor. Haritaya dönüp çağrıyı görmek ister misiniz?")
                                            .setPositiveButton("Haritaya Dön", (dialog, which) -> {
                                                bottomNav.setSelectedItemId(R.id.nav_driver_map);
                                            })
                                            .setNegativeButton("Kapat", null)
                                            .show();
                                    }
                                }
                            }
                            return;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        requestsRef.addValueEventListener(globalRequestListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestsRef != null && globalRequestListener != null) {
            requestsRef.removeEventListener(globalRequestListener);
        }
    }
}
