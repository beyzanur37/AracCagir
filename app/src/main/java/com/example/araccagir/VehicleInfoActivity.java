package com.example.araccagir;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class VehicleInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_info);

        EditText etPlate = findViewById(R.id.etPlate);
        EditText etModel = findViewById(R.id.etModel);
        EditText etColor = findViewById(R.id.etColor);
        Spinner spVehicleType = findViewById(R.id.spVehicleType);
        Button btnContinueVehicle = findViewById(R.id.btnContinueVehicle);

        String[] types = new String[]{"Standart", "Geniş (XL)", "Lüks VIP"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spVehicleType.setAdapter(adapter);

        btnContinueVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plate = etPlate.getText().toString().trim();
                String model = etModel.getText().toString().trim();
                String color = etColor.getText().toString().trim();
                String vehicleType = spVehicleType.getSelectedItem().toString();

                if (plate.isEmpty() || model.isEmpty() || color.isEmpty()) {
                    Toast.makeText(VehicleInfoActivity.this, "Lütfen tüm bilgileri girin.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(VehicleInfoActivity.this, RegisterActivity.class);
                if (getIntent() != null && getIntent().getExtras() != null) {
                    intent.putExtras(getIntent().getExtras());
                }
                
                intent.putExtra("PLATE", plate);
                intent.putExtra("MODEL", model);
                intent.putExtra("COLOR", color);
                intent.putExtra("VEHICLE_TYPE", vehicleType);

                startActivity(intent);
                finish();
            }
        });
    }
}
