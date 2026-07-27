package com.example.araccagir;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class PersonalInfoActivity extends AppCompatActivity {

    private String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        if (getIntent() != null) {
            userRole = getIntent().getStringExtra("USER_ROLE");
        }

        EditText etFullName = findViewById(R.id.etFullName);
        RadioGroup rgGender = findViewById(R.id.rgGender);
        Button btnFinish = findViewById(R.id.btnFinish);
        android.widget.ImageView ivProfilePic = findViewById(R.id.ivProfilePic);

        ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PersonalInfoActivity.this, "Galeri / Kamera açılacak...", Toast.LENGTH_SHORT).show();
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = etFullName.getText().toString().trim();
                int checkedGenderId = rgGender.getCheckedRadioButtonId();

                if (fullName.isEmpty() || checkedGenderId == -1) {
                    Toast.makeText(PersonalInfoActivity.this, "Lütfen adınızı girin ve cinsiyet seçin.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Normalde veritabanına kayıt işlemi burada yapılır.
                Toast.makeText(PersonalInfoActivity.this, "Bilgiler kaydedildi, ilerleniyor.", Toast.LENGTH_SHORT).show();

                Intent intent;
                if ("Sürücü".equals(userRole)) {
                    intent = new Intent(PersonalInfoActivity.this, VehicleInfoActivity.class);
                } else {
                    intent = new Intent(PersonalInfoActivity.this, RegisterActivity.class);
                }
                
                intent.putExtra("USER_ROLE", userRole);
                intent.putExtra("PHONE", getIntent().getStringExtra("PHONE"));
                intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
                intent.putExtra("FULL_NAME", fullName);
                intent.putExtra("GENDER", checkedGenderId == R.id.rbMale ? "Erkek" : "Kadın");
                startActivity(intent);
                finish();
            }
        });
    }
}
