package com.example.araccagir;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WalletFragment extends Fragment {

    private TextView tvBalance;
    private EditText etTopUpAmount;
    private DatabaseReference userRef;
    private int currentBalance = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        tvBalance = view.findViewById(R.id.tvBalance);
        etTopUpAmount = view.findViewById(R.id.etTopUpAmount);
        Button btnAddBalance = view.findViewById(R.id.btnAddBalance);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("users").child(uid);

            userRef.child("walletBalance").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        try {
                            currentBalance = snapshot.getValue(Integer.class);
                        } catch (Exception e) {}
                    } else {
                        currentBalance = 0;
                    }
                    tvBalance.setText(currentBalance + " TL");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        btnAddBalance.setOnClickListener(v -> {
            String amountStr = etTopUpAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Lütfen bir tutar girin.", Toast.LENGTH_SHORT).show();
                return;
            }

            int topUpAmount = Integer.parseInt(amountStr);
            if (topUpAmount <= 0) {
                Toast.makeText(getContext(), "Geçerli bir tutar girin.", Toast.LENGTH_SHORT).show();
                return;
            }

            PaymentCardDialog dialog = new PaymentCardDialog(requireContext(), String.valueOf(topUpAmount), true, new PaymentCardDialog.PaymentListener() {
                @Override
                public void onPayFromApp() {
                    if (userRef != null) {
                        userRef.child("walletBalance").setValue(currentBalance + topUpAmount);
                        Toast.makeText(getContext(), topUpAmount + " TL başarıyla yüklendi!", Toast.LENGTH_SHORT).show();
                        etTopUpAmount.setText("");
                    }
                }

                @Override
                public void onPayCash() {
                    // Yüklemede elden ödeme yok, buton zaten gizli
                }
            });
            dialog.show();
        });

        return view;
    }
}
