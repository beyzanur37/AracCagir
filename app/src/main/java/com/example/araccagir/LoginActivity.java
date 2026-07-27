package com.example.araccagir;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);

            Button btnLogin = findViewById(R.id.btnLogin);
            TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
            EditText etEmail = findViewById(R.id.etEmail);
            EditText etPassword = findViewById(R.id.etPassword);

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String email = etEmail.getText().toString().trim();
                        String password = etPassword.getText().toString().trim();

                        if (email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(LoginActivity.this, "E-posta ve şifre girin", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        btnLogin.setEnabled(false);
                        btnLogin.setText("Giriş Yapılıyor...");

                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        checkUserRoleAndRedirect(task.getResult().getUser().getUid());
                                    } else {
                                        btnLogin.setEnabled(true);
                                        btnLogin.setText("Giriş Yap");
                                        Toast.makeText(LoginActivity.this, "Giriş başarısız: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Tıklama Hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            tvGoToRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, RoleSelectionActivity.class);
                    startActivity(intent);
                }
            });

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                checkUserRoleAndRedirect(currentUser.getUid());
            }
        } catch (Exception e) {
            Log.e("CrashDebug", "onCreate error", e);
            Toast.makeText(this, "Başlatma Hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkUserRoleAndRedirect(String uid) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("users").child(uid);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (snapshot.exists()) {
                            String role = snapshot.child("role").getValue(String.class);
                            Intent intent;
                            if ("Sürücü".equals(role)) {
                                intent = new Intent(LoginActivity.this, DriverHomeActivity.class);
                            } else {
                                intent = new Intent(LoginActivity.this, PassengerHomeActivity.class);
                            }
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Kullanıcı verisi bulunamadı", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            Button btnLogin = findViewById(R.id.btnLogin);
                            if (btnLogin != null) {
                                btnLogin.setEnabled(true);
                                btnLogin.setText("Giriş Yap");
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Veri İşleme Hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LoginActivity.this, "Veritabanı hatası: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Button btnLogin = findViewById(R.id.btnLogin);
                    if (btnLogin != null) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Giriş Yap");
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Redirect Hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
