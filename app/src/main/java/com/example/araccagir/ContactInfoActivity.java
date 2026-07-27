package com.example.araccagir;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ContactInfoActivity extends AppCompatActivity {

    private String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        if (getIntent() != null) {
            userRole = getIntent().getStringExtra("USER_ROLE");
        }

        EditText etPhone = findViewById(R.id.etPhone);
        EditText etEmail = findViewById(R.id.etEmail);
        Button btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = "+90" + etPhone.getText().toString().trim().replaceAll("^0+", "");
                String email = etEmail.getText().toString().trim();

                if (etPhone.getText().toString().trim().isEmpty() || email.isEmpty()) {
                    Toast.makeText(ContactInfoActivity.this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(ContactInfoActivity.this, PersonalInfoActivity.class);
                intent.putExtra("USER_ROLE", userRole);
                intent.putExtra("PHONE", phone);
                intent.putExtra("EMAIL", email);
                startActivity(intent);
            }
        });
    }
}
