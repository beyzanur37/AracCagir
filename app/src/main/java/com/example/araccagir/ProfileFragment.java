package com.example.araccagir;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvTotalEarnings;
    private LinearLayout llDriverSection;
    private TextInputEditText etVehiclePlate, etVehicleBrand;
    private Button btnSaveVehicleInfo;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvTotalEarnings = view.findViewById(R.id.tvTotalEarnings);
        llDriverSection = view.findViewById(R.id.llDriverSection);
        etVehiclePlate = view.findViewById(R.id.etVehiclePlate);
        etVehicleBrand = view.findViewById(R.id.etVehicleBrand);
        btnSaveVehicleInfo = view.findViewById(R.id.btnSaveVehicleInfo);
        
        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        loadUserData();

        btnSaveVehicleInfo.setOnClickListener(v -> saveVehicleInfo());

        return view;
    }

    private void loadUserData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String userType = snapshot.child("userType").getValue(String.class);
                    
                    if (name != null) tvProfileName.setText(name);
                    
                    if ("Sürücü".equals(userType)) {
                        llDriverSection.setVisibility(View.VISIBLE);
                        
                        String plate = snapshot.child("vehiclePlate").getValue(String.class);
                        String brand = snapshot.child("vehicleBrand").getValue(String.class);
                        if (plate != null) etVehiclePlate.setText(plate);
                        if (brand != null) etVehicleBrand.setText(brand);
                        
                        calculateDriverEarnings(uid);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    
    private void saveVehicleInfo() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        String plate = etVehiclePlate.getText().toString();
        String brand = etVehicleBrand.getText().toString();
        
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("users").child(uid);
        userRef.child("vehiclePlate").setValue(plate);
        userRef.child("vehicleBrand").setValue(brand).addOnCompleteListener(task -> {
            if (task.isSuccessful() && isAdded()) {
                Toast.makeText(getContext(), "Araç bilgileri güncellendi", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void calculateDriverEarnings(String driverId) {
        DatabaseReference ridesRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("ride_requests");
        ridesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalEarnings = 0;
                for (DataSnapshot req : snapshot.getChildren()) {
                    String status = req.child("status").getValue(String.class);
                    String dId = req.child("driverId").getValue(String.class);
                    
                    if ("paid".equals(status) && driverId.equals(dId)) {
                        String priceStr = req.child("price").getValue(String.class);
                        if (priceStr != null) {
                            try {
                                String numericPart = priceStr.replaceAll("[^0-9]", "");
                                if (!numericPart.isEmpty()) {
                                    totalEarnings += Integer.parseInt(numericPart);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (isAdded()) {
                    tvTotalEarnings.setText(totalEarnings + " TL");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
