package com.example.araccagir;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        LinearLayout btnPassenger = findViewById(R.id.btnPassenger);
        LinearLayout btnDriver = findViewById(R.id.btnDriver);

        btnPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNextStep("Yolcu");
            }
        });

        btnDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNextStep("Sürücü");
            }
        });
    }

    private void goToNextStep(String role) {
        Intent intent = new Intent(RoleSelectionActivity.this, ContactInfoActivity.class);
        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
    }
}
