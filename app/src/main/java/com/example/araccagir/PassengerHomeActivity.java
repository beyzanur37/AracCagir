package com.example.araccagir;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PassengerHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_passenger_home);

            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setOnItemSelectedListener(item -> {
                try {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_map) {
                        selectedFragment = new PassengerMapFragment();
                    } else if (itemId == R.id.nav_history) {
                        selectedFragment = new HistoryFragment();
                    } else if (itemId == R.id.nav_wallet) {
                        selectedFragment = new WalletFragment();
                    } else if (itemId == R.id.nav_profile) {
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
                        .replace(R.id.fragment_container, new PassengerMapFragment())
                        .commit();
            }
        } catch (Exception e) {
            android.util.Log.e("CrashDebug", "PassengerHome error", e);
            android.widget.Toast.makeText(this, "Ana Ekran Hatası: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }
}
