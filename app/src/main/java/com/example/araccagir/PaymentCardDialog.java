package com.example.araccagir;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;

public class PaymentCardDialog extends Dialog {

    private boolean isBackVisible = false;
    private View cardFront, cardBack;
    private AnimatorSet frontAnim, backAnim, frontAnimBack, backAnimBack;
    
    private PaymentListener listener;
    private String priceText;
    private boolean isForTopUp;

    public interface PaymentListener {
        void onPayFromApp();
        void onPayCash();
    }

    public PaymentCardDialog(@NonNull Context context, String price, boolean isForTopUp, PaymentListener listener) {
        super(context);
        this.priceText = price;
        this.listener = listener;
        this.isForTopUp = isForTopUp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_payment_card);
        
        // Setup distance for 3D effect
        float scale = getContext().getResources().getDisplayMetrics().density;
        cardFront = findViewById(R.id.cardFront);
        cardBack = findViewById(R.id.cardBack);
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);
        
        loadAnimations();
        
        TextView tvTitle = findViewById(R.id.tvPaymentTitle);
        Button btnPayNow = findViewById(R.id.btnPayNow);
        Button btnPayCash = findViewById(R.id.btnPayCash);
        
        if (isForTopUp) {
            tvTitle.setText("Bakiye Yükle (" + priceText + " TL)");
            btnPayNow.setText("Yükle");
            btnPayCash.setVisibility(View.GONE);
        } else {
            tvTitle.setText("Ödeme Yap (" + priceText + ")");
        }
        
        setupInputs();
        
        btnPayNow.setOnClickListener(v -> {
            TextInputEditText etCardNumber = findViewById(R.id.etCardNumber);
            if (etCardNumber.getText().toString().length() < 16) {
                Toast.makeText(getContext(), "Geçerli bir kart numarası giriniz.", Toast.LENGTH_SHORT).show();
                return;
            }
            listener.onPayFromApp();
            dismiss();
        });
        
        btnPayCash.setOnClickListener(v -> {
            listener.onPayCash();
            dismiss();
        });
    }

    private void loadAnimations() {
        frontAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_right_out);
        backAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_right_in);
        
        frontAnimBack = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_left_in);
        backAnimBack = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_left_out);
    }

    private void flipCardToBack() {
        if (!isBackVisible) {
            frontAnim.setTarget(cardFront);
            backAnim.setTarget(cardBack);
            frontAnim.start();
            backAnim.start();
            isBackVisible = true;
        }
    }

    private void flipCardToFront() {
        if (isBackVisible) {
            frontAnimBack.setTarget(cardFront);
            backAnimBack.setTarget(cardBack);
            frontAnimBack.start();
            backAnimBack.start();
            isBackVisible = false;
        }
    }
    
    private void setupInputs() {
        TextInputEditText etCardNumber = findViewById(R.id.etCardNumber);
        TextInputEditText etCardName = findViewById(R.id.etCardName);
        TextInputEditText etCardExpiry = findViewById(R.id.etCardExpiry);
        TextInputEditText etCardCvv = findViewById(R.id.etCardCvv);
        
        TextView tvCardNumberDisplay = findViewById(R.id.tvCardNumberDisplay);
        TextView tvCardNameDisplay = findViewById(R.id.tvCardNameDisplay);
        TextView tvCardExpiryDisplay = findViewById(R.id.tvCardExpiryDisplay);
        TextView tvCardCvvDisplay = findViewById(R.id.tvCardCvvDisplay);
        
        etCardNumber.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String formatted = s.toString().replace(" ", "").replaceAll("(.{4})", "$1 ").trim();
                tvCardNumberDisplay.setText(formatted.isEmpty() ? "**** **** **** ****" : formatted);
            }
        });
        
        etCardName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCardNameDisplay.setText(s.toString().isEmpty() ? "KART SAHİBİ" : s.toString().toUpperCase());
            }
        });
        
        etCardExpiry.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2 && before == 0) {
                    etCardExpiry.setText(s.toString() + "/");
                    etCardExpiry.setSelection(etCardExpiry.getText().length());
                }
                tvCardExpiryDisplay.setText(etCardExpiry.getText().toString().isEmpty() ? "AA/YY" : etCardExpiry.getText().toString());
            }
        });
        
        etCardCvv.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCardCvvDisplay.setText(s.toString().isEmpty() ? "***" : s.toString());
            }
        });
        
        etCardCvv.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                flipCardToBack();
            } else {
                flipCardToFront();
            }
        });
        
        etCardNumber.setOnFocusChangeListener((v, hasFocus) -> { if(hasFocus) flipCardToFront(); });
        etCardName.setOnFocusChangeListener((v, hasFocus) -> { if(hasFocus) flipCardToFront(); });
        etCardExpiry.setOnFocusChangeListener((v, hasFocus) -> { if(hasFocus) flipCardToFront(); });
    }

    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
