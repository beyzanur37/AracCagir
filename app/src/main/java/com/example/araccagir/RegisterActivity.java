package com.example.araccagir;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        RadioGroup rgUserType = findViewById(R.id.rgUserType);

        String role = getIntent().getStringExtra("USER_ROLE");
        String email = getIntent().getStringExtra("EMAIL");
        String name = getIntent().getStringExtra("FULL_NAME");
        String phone = getIntent().getStringExtra("PHONE");
        String gender = getIntent().getStringExtra("GENDER");
        
        String plate = getIntent().getStringExtra("PLATE");
        String model = getIntent().getStringExtra("MODEL");
        String color = getIntent().getStringExtra("COLOR");
        String vehicleType = getIntent().getStringExtra("VEHICLE_TYPE");

        EditText etRegEmail = findViewById(R.id.etRegEmail);
        EditText etRegName = findViewById(R.id.etRegName);
        EditText etRegPassword = findViewById(R.id.etRegPassword);

        if (email != null) {
            etRegEmail.setText(email);
            etRegEmail.setEnabled(false);
        }
        if (name != null) {
            etRegName.setText(name);
            etRegName.setEnabled(false);
        }
        
        rgUserType.setVisibility(View.GONE);
        tvGoToLogin.setVisibility(View.GONE);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String finalEmail = etRegEmail.getText().toString().trim();
                String password = etRegPassword.getText().toString().trim();
                String finalName = etRegName.getText().toString().trim();

                if (finalEmail.isEmpty() || password.isEmpty() || finalName.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Tüm alanları doldurun.", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                btnRegister.setEnabled(false);
                btnRegister.setText("Kaydediliyor...");

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(finalEmail, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Kullanıcı auth başarılı, DB'ye yazılıyor...", Toast.LENGTH_SHORT).show();
                                String uid = task.getResult().getUser().getUid();
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("name", finalName);
                                map.put("email", finalEmail);
                                map.put("phone", phone != null ? phone : "");
                                map.put("gender", gender != null ? gender : "");
                                map.put("role", role != null ? role : "Yolcu");
                                
                                if ("Sürücü".equals(role)) {
                                    map.put("plate", plate != null ? plate : "");
                                    map.put("model", model != null ? model : "");
                                    map.put("color", color != null ? color : "");
                                    map.put("vehicleType", vehicleType != null ? vehicleType : "Standart");
                                }

                                FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("users").child(uid).setValue(map)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show();
                                                Intent intent;
                                                if ("Sürücü".equals(role)) {
                                                    intent = new Intent(RegisterActivity.this, DriverHomeActivity.class);
                                                } else {
                                                    intent = new Intent(RegisterActivity.this, PassengerHomeActivity.class);
                                                }
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                btnRegister.setEnabled(true);
                                                btnRegister.setText("Kayıt Ol");
                                                String errMsg = dbTask.getException() != null ? dbTask.getException().getMessage() : "Bilinmeyen hata";
                                                Toast.makeText(RegisterActivity.this, "Veritabanına kaydedilirken hata: " + errMsg, Toast.LENGTH_LONG).show();
                                            }
                                        });

                            } else {
                                btnRegister.setEnabled(true);
                                btnRegister.setText("Kayıt Ol");
                                Toast.makeText(RegisterActivity.this, "Kayıt Hatası: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}
