package com.example.araccagir;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.common.api.Status;
import android.util.Log;
import android.content.pm.ApplicationInfo;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.os.Looper;
import android.location.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import android.graphics.Color;

import com.google.firebase.database.ServerValue;

public class PassengerMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private int radius = 3; // 3 km radius
    private GeoQuery geoQuery;
    private DatabaseReference driversAvailableRef;
    private HashMap<String, Marker> driverMarkers;
    private boolean isQueryStarted = false;
    private boolean isMapCentered = false;
    private String selectedDestination = "";
    private Marker destinationMarker;
    private LinearLayout llVehicleStandard, llVehicleXL, llVehicleVIP;
    private String selectedVehicleType = "Standart";
    private TextView tvStandardPrice, tvXLPrice, tvVIPPrice;
    private LatLng selectedDestinationLatLng;
    private Polyline currentPolyline;
    private String googleApiKey;
    private RequestQueue requestQueue;
    private PaymentCardDialog passengerPaymentDialog;
    private boolean isRidePaid = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult == null) return;
                    for (Location location : locationResult.getLocations()) {
                        lastLocation = location;
                        if (!isMapCentered && mMap != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(), location.getLongitude()), 15));
                            isMapCentered = true;
                        }
                        if (selectedDestinationLatLng != null) {
                            calculateAndDisplayPrices();
                        }
                        getClosestDrivers();
                    }
                }
            };

            View view = inflater.inflate(R.layout.fragment_passenger_map, container, false);

            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            requestPermissionsIfNecessary(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });

            driversAvailableRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("drivers_available");
            driverMarkers = new HashMap<>();

            try {
                ApplicationInfo ai = requireContext().getPackageManager().getApplicationInfo(requireContext().getPackageName(), PackageManager.GET_META_DATA);
                if (ai.metaData != null) {
                    googleApiKey = ai.metaData.getString("com.google.android.geo.API_KEY");
                    if (googleApiKey != null && !googleApiKey.isEmpty()) {
                        if (!Places.isInitialized()) {
                            Places.initialize(requireContext().getApplicationContext(), googleApiKey);
                        }
                    } else {
                        Toast.makeText(getContext(), "Uyarı: API Anahtarı bulunamadı!", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            requestQueue = Volley.newRequestQueue(requireContext());

            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                    getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
            if (autocompleteFragment != null) {
                autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
                autocompleteFragment.setCountries("TR");
                autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(@NonNull Place place) {
                        String address = place.getAddress();
                        if (address == null || !address.toLowerCase().contains("kastamonu")) {
                            Toast.makeText(getContext(), "Seçtiğiniz bölgede hizmet veremiyoruz. Lütfen Kastamonu içinde bir adres seçin.", Toast.LENGTH_LONG).show();
                            selectedDestination = "";
                            selectedDestinationLatLng = null;
                            if (destinationMarker != null) destinationMarker.remove();
                            if (tvStandardPrice != null) tvStandardPrice.setText("-- TL");
                            if (tvXLPrice != null) tvXLPrice.setText("-- TL");
                            if (tvVIPPrice != null) tvVIPPrice.setText("-- TL");
                            return;
                        }
                        
                        selectedDestination = place.getName();
                        if (mMap != null && place.getLatLng() != null) {
                            selectedDestinationLatLng = place.getLatLng();
                            if (destinationMarker != null) destinationMarker.remove();
                            destinationMarker = mMap.addMarker(new MarkerOptions()
                                    .position(place.getLatLng())
                                    .title(place.getName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                            calculateAndDisplayPrices();
                            if (lastLocation != null) {
                                drawRoute(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), selectedDestinationLatLng);
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Status status) {
                        Log.e("Places", "An error occurred: " + status);
                        Toast.makeText(getContext(), "Arama Hatası: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            Button btnCallVehicle = view.findViewById(R.id.btnCallVehicle);
            TextView tvStatus = view.findViewById(R.id.tvStatus);

            llVehicleStandard = view.findViewById(R.id.llVehicleStandard);
            llVehicleXL = view.findViewById(R.id.llVehicleXL);
            llVehicleVIP = view.findViewById(R.id.llVehicleVIP);

            tvStandardPrice = view.findViewById(R.id.tvStandardPrice);
            tvXLPrice = view.findViewById(R.id.tvXLPrice);
            tvVIPPrice = view.findViewById(R.id.tvVIPPrice);

            llVehicleStandard.setBackgroundResource(R.drawable.bg_vehicle_card_selected);

            View.OnClickListener vehicleClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    llVehicleStandard.setBackgroundResource(R.drawable.bg_vehicle_card);
                    llVehicleXL.setBackgroundResource(R.drawable.bg_vehicle_card);
                    llVehicleVIP.setBackgroundResource(R.drawable.bg_vehicle_card);

                    if (v.getId() == R.id.llVehicleStandard) {
                        v.setBackgroundResource(R.drawable.bg_vehicle_card_selected);
                        selectedVehicleType = "Standart";
                    } else if (v.getId() == R.id.llVehicleXL) {
                        v.setBackgroundResource(R.drawable.bg_vehicle_card_selected);
                        selectedVehicleType = "Geniş (XL)";
                    } else if (v.getId() == R.id.llVehicleVIP) {
                        v.setBackgroundResource(R.drawable.bg_vehicle_card_selected);
                        selectedVehicleType = "Lüks VIP";
                    }
                }
            };

            llVehicleStandard.setOnClickListener(vehicleClickListener);
            llVehicleXL.setOnClickListener(vehicleClickListener);
            llVehicleVIP.setOnClickListener(vehicleClickListener);

            btnCallVehicle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedDestination == null || selectedDestination.isEmpty() || selectedDestinationLatLng == null) {
                        Toast.makeText(getContext(), "Lütfen bir hedef seçin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String calculatedPrice = "0 TL";
                    if ("Standart".equals(selectedVehicleType) && tvStandardPrice != null) calculatedPrice = tvStandardPrice.getText().toString();
                    else if ("Geniş (XL)".equals(selectedVehicleType) && tvXLPrice != null) calculatedPrice = tvXLPrice.getText().toString();
                    else if ("Lüks VIP".equals(selectedVehicleType) && tvVIPPrice != null) calculatedPrice = tvVIPPrice.getText().toString();

                    final String finalCalculatedPrice = calculatedPrice;
                    final String finalDest = selectedDestination;

                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Yolculuk Onayı")
                            .setMessage(finalDest + " konumuna " + selectedVehicleType + " araç ile gitmek istiyor musunuz?\n\nTahmini Ücret: " + finalCalculatedPrice)
                            .setPositiveButton("Araç Çağır", (dialog, which) -> {
                                executeRideRequest(finalDest, finalCalculatedPrice, btnCallVehicle, tvStatus);
                            })
                            .setNegativeButton("İptal", null)
                            .show();
                }
            });

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String passengerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference rideRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("ride_requests").child(passengerId);
                rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        if (snapshot.exists()) {
                            String status = snapshot.child("status").getValue(String.class);
                            if ("pending".equals(status) || "accepted".equals(status) || "payment_pending".equals(status)) {
                                String dest = snapshot.child("destination").getValue(String.class);
                                if (dest != null) selectedDestination = dest;
                                
                                btnCallVehicle.setEnabled(false);
                                tvStatus.setVisibility(View.VISIBLE);
                                listenForRideStatus(rideRef, tvStatus, btnCallVehicle);
                                
                                if ("pending".equals(status)) {
                                    btnCallVehicle.setText("Araç Aranıyor...");
                                    tvStatus.setText("Durum: Yakındaki araçlar taranıyor...");
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            return view;
        } catch (Exception e) {
            android.util.Log.e("CrashDebug", "PassengerMap onCreateView error", e);
            Toast.makeText(getContext(), "Harita Ekranı Hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return inflater.inflate(R.layout.fragment_passenger_map, container, false);
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (fusedLocationClient == null || locationCallback == null) return;
        
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void executeRideRequest(String dest, String calculatedPrice, Button btnCallVehicle, TextView tvStatus) {
        isRidePaid = false;
        btnCallVehicle.setEnabled(false);
        btnCallVehicle.setText("Araç Aranıyor...");

        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("Durum: Yakındaki araçlar taranıyor...");

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Oturum hatası.", Toast.LENGTH_SHORT).show();
            btnCallVehicle.setEnabled(true);
            btnCallVehicle.setText("Araç Çağır");
            return;
        }

        String passengerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rideRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("ride_requests").child(passengerId);

        HashMap<String, Object> req = new HashMap<>();
        req.put("destination", dest);
        req.put("destLat", selectedDestinationLatLng.latitude);
        req.put("destLng", selectedDestinationLatLng.longitude);
        req.put("status", "pending");
        req.put("vehicleType", selectedVehicleType);
        req.put("price", calculatedPrice);

        rideRef.setValue(req).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listenForRideStatus(rideRef, tvStatus, btnCallVehicle);

                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    checkRideStatusTimeout(rideRef, btnCallVehicle, tvStatus);
                }, 60000); // 60 seconds timeout for all requests
            } else {
                btnCallVehicle.setEnabled(true);
                btnCallVehicle.setText("Araç Çağır");
                tvStatus.setText("Durum: Hata oluştu");
            }
        });
    }



    private void listenForRideStatus(DatabaseReference rideRef, TextView tvStatus, Button btnCallVehicle) {
        rideRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if (snapshot.exists()) {
                     String status = snapshot.child("status").getValue(String.class);
                     if ("accepted".equals(status)) {
                         tvStatus.setText("Durum: Sürücü bulundu ve yola çıktı!");
                         btnCallVehicle.setText("Sürücü Yolda");
                         playNotificationSound();
                         
                         // Test amaçlı: Sürücü yolda yazdıktan 15 saniye sonra "Araç Konumda" yazsın
                         new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                             if (isAdded() && "Sürücü Yolda".equals(btnCallVehicle.getText().toString())) {
                                 tvStatus.setText("Durum: Araç konumunuza ulaştı!");
                                 btnCallVehicle.setText("Araç Konumda");
                                 playNotificationSound();
                             }
                         }, 15000);
                     } else if ("payment_pending".equals(status)) {
                         tvStatus.setText("Durum: Sürüş tamamlandı, ödeme bekleniyor.");
                         handlePaymentProcess(rideRef, snapshot.child("price").getValue(String.class), btnCallVehicle, tvStatus);
                     } else if ("paid".equals(status)) {
                         isRidePaid = true;
                         if (passengerPaymentDialog != null && passengerPaymentDialog.isShowing()) {
                             passengerPaymentDialog.dismiss();
                         }
                         tvStatus.setText("Durum: Ödeme alındı. Teşekkürler!");
                         btnCallVehicle.setEnabled(true);
                         btnCallVehicle.setText("Araç Çağır");
                         
                         String method = snapshot.child("paymentMethod").getValue(String.class);
                         if ("cash".equals(method) || "pos".equals(method)) {
                             String priceStr = snapshot.child("price").getValue(String.class);
                             saveRideHistory(selectedDestination, priceStr != null ? priceStr : "0 TL", method);
                         }
                         
                         String driverId = snapshot.child("driverId").getValue(String.class);
                         if (driverId != null) {
                             showRatingDialog(driverId);
                         }
                         
                         rideRef.removeEventListener(this);
                     }
                 }
             }
             @Override
             public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
    
    private void handlePaymentProcess(DatabaseReference rideRef, String priceStr, Button btnCallVehicle, TextView tvStatus) {
        if (!isAdded()) return;
        
        String pStr = priceStr != null ? priceStr.replace(" TL", "").trim() : "0";
        int price = 0;
        try {
            price = Integer.parseInt(pStr);
        } catch (Exception e) {}
        
        int finalPrice = price;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("users").child(uid);
        
        userRef.child("walletBalance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int balance = 0;
                if (snapshot.exists() && snapshot.getValue() != null) {
                    try {
                        balance = snapshot.getValue(Integer.class);
                    } catch (Exception e) {}
                }
                
                if (balance >= finalPrice && finalPrice > 0) {
                    int newBalance = balance - finalPrice;
                    userRef.child("walletBalance").setValue(newBalance);
                    rideRef.child("paymentMethod").setValue("wallet");
                    rideRef.child("status").setValue("paid");
                    saveRideHistory(selectedDestination, finalPrice + " TL", "wallet");
                    Toast.makeText(getContext(), "Ödeme (" + finalPrice + " TL) cüzdanınızdan otomatik çekildi. Kalan Bakiye: " + newBalance + " TL", Toast.LENGTH_LONG).show();
                    playNotificationSound();
                    
                    btnCallVehicle.setEnabled(true);
                    btnCallVehicle.setText("Araç Çağır");
                    tvStatus.setText("Durum: Sürüş tamamlandı.");
                    if (destinationMarker != null) destinationMarker.remove();
                } else {
                    showPassengerPaymentDialog(rideRef, priceStr, btnCallVehicle, tvStatus);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showPassengerPaymentDialog(rideRef, priceStr, btnCallVehicle, tvStatus);
            }
        });
    }

    private void showPassengerPaymentDialog(DatabaseReference rideRef, String price, Button btnCallVehicle, TextView tvStatus) {
        if (!isAdded() || isRidePaid) return;
        final String finalPrice = (price == null) ? "Bilinmiyor" : price;
        
        if (passengerPaymentDialog != null && passengerPaymentDialog.isShowing()) {
            passengerPaymentDialog.dismiss();
        }
        
        passengerPaymentDialog = new PaymentCardDialog(requireContext(), finalPrice, false, new PaymentCardDialog.PaymentListener() {
            @Override
            public void onPayFromApp() {
                rideRef.child("paymentMethod").setValue("app");
                rideRef.child("status").setValue("paid");
                saveRideHistory(selectedDestination, finalPrice, "app");
                Toast.makeText(getContext(), "Ödemeniz kartınızdan başarıyla alındı!", Toast.LENGTH_LONG).show();
                playNotificationSound();
                btnCallVehicle.setEnabled(true);
                btnCallVehicle.setText("Araç Çağır");
                tvStatus.setText("Durum: Sürüş tamamlandı.");
                if (destinationMarker != null) destinationMarker.remove();
            }

            @Override
            public void onPayCash() {
                Toast.makeText(getContext(), "Lütfen ödemeyi sürücüye nakit/POS ile yapın.", Toast.LENGTH_SHORT).show();
            }
        });
        passengerPaymentDialog.show();
    }

    private void saveRideHistory(String destination, String price, String paymentMethod) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null || destination == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        String date = new java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm", new java.util.Locale("tr")).format(new java.util.Date());
        
        DatabaseReference historyRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/")
                .getReference("ride_history").child(uid).push();
                
        RideHistory history = new RideHistory(destination, price, date, paymentMethod, "Yolcu");
        historyRef.setValue(history);
    }

    private void playNotificationSound() {
        try {
            android.net.Uri notification = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);
            android.media.Ringtone r = android.media.RingtoneManager.getRingtone(requireContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkRideStatusTimeout(DatabaseReference rideRef, Button btnCallVehicle, TextView tvStatus) {
        if (!isAdded()) return;
        rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("pending".equals(status)) {
                        // Butonu hemen eski haline getir
                        btnCallVehicle.setEnabled(true);
                        btnCallVehicle.setText("Araç Çağır");
                        
                        if (!"Standart".equals(selectedVehicleType)) {
                            new android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Araç Bulunamadı")
                                .setMessage("Yakınınızda " + selectedVehicleType + " araç bulunamadı. Aramaya Standart araçlarla devam etmek ister misiniz?")
                                .setPositiveButton("Evet", (dialog, which) -> {
                                    rideRef.child("vehicleType").setValue("Standart");
                                    tvStatus.setText("Durum: Standart araçlar taranıyor...");
                                    
                                    // Aramaya devam ettiği için butonu tekrar Aranıyor yap
                                    btnCallVehicle.setEnabled(false);
                                    btnCallVehicle.setText("Araç Aranıyor...");
                                    
                                    // Standart arama için yeni bir zamanlayıcı (60 sn)
                                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                        checkStandartTimeout(rideRef, btnCallVehicle, tvStatus);
                                    }, 60000);
                                })
                                .setNegativeButton("İptal Et", (dialog, which) -> {
                                    rideRef.removeValue();
                                    tvStatus.setText("Durum: İptal edildi");
                                })
                                .setCancelable(false)
                                .show();
                        } else {
                            checkStandartTimeout(rideRef, btnCallVehicle, tvStatus);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void checkStandartTimeout(DatabaseReference rideRef, Button btnCallVehicle, TextView tvStatus) {
        if (!isAdded()) return;
        rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("pending".equals(status)) {
                        btnCallVehicle.setEnabled(true);
                        btnCallVehicle.setText("Araç Çağır");
                        
                        new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Araç Bulunamadı")
                            .setMessage("Şu anda yakınınızda uygun araç bulunmamaktadır. Lütfen daha sonra tekrar deneyin.")
                            .setPositiveButton("Tamam", (dialog, which) -> {
                                rideRef.removeValue();
                                tvStatus.setText("Durum: Araç bulunamadı");
                            })
                            .setCancelable(false)
                            .show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void calculateAndDisplayPrices() {
        if (lastLocation == null || selectedDestinationLatLng == null) return;
        
        float[] results = new float[1];
        Location.distanceBetween(
                lastLocation.getLatitude(), lastLocation.getLongitude(),
                selectedDestinationLatLng.latitude, selectedDestinationLatLng.longitude,
                results);
                
        float distanceInMeters = results[0];
        float distanceInKm = distanceInMeters / 1000f;
        
        double basePrice = 65.0 + (distanceInKm * 45.0);
        
        int priceStandard = (int) Math.round(basePrice * 1.0);
        int priceXL = (int) Math.round(basePrice * 1.5);
        int priceVIP = (int) Math.round(basePrice * 2.0);
        
        if (tvStandardPrice != null) tvStandardPrice.setText(priceStandard + " TL");
        if (tvXLPrice != null) tvXLPrice.setText(priceXL + " TL");
        if (tvVIPPrice != null) tvVIPPrice.setText(priceVIP + " TL");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableUserLocation();
    }

    @SuppressLint("MissingPermission")
    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
            startLocationUpdates();
        }
    }


    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLocationUpdates();
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
        }
    }

    private void getClosestDrivers() {
        if (lastLocation == null) return;
        
        if (!isQueryStarted) {
            GeoFire geoFire = new GeoFire(driversAvailableRef);
            geoQuery = geoFire.queryAtLocation(new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), radius);

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    if (mMap != null) {
                        LatLng driverLatLng = new LatLng(location.latitude, location.longitude);
                        Marker mMarker = mMap.addMarker(new MarkerOptions()
                                .position(driverLatLng)
                                .title("Sürücü")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        driverMarkers.put(key, mMarker);
                    }
                }

                @Override
                public void onKeyExited(String key) {
                    if (driverMarkers.containsKey(key)) {
                        Marker marker = driverMarkers.get(key);
                        if (marker != null) marker.remove();
                        driverMarkers.remove(key);
                    }
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    if (driverMarkers.containsKey(key)) {
                        Marker marker = driverMarkers.get(key);
                        if (marker != null) {
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                }

                @Override
                public void onGeoQueryReady() {
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                }
            });
            isQueryStarted = true;
        } else {
            geoQuery.setCenter(new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            requestPermissions(permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            enableUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            enableUserLocation();
        }
    }

    private void drawRoute(LatLng origin, LatLng dest) {
        if (origin == null || dest == null || googleApiKey == null) return;
        
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + dest.latitude + "," + dest.longitude +
                "&key=" + googleApiKey;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    try {
                        JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                            String encodedString = overviewPolyline.getString("points");
                            List<LatLng> points = decodePoly(encodedString);

                            if (currentPolyline != null) {
                                currentPolyline.remove();
                            }
                            
                            PolylineOptions options = new PolylineOptions()
                                    .addAll(points)
                                    .width(12f)
                                    .color(Color.parseColor("#007BFF")) // Mavi rota
                                    .geodesic(true);

                            currentPolyline = mMap.addPolyline(options);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("DirectionsAPI", "Hata: " + e.getMessage());
                    }
                }, error -> {
                    Log.e("DirectionsAPI", "Ağ Hatası: " + error.getMessage());
                });

        requestQueue.add(jsonObjectRequest);
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void showRatingDialog(String driverId) {
        if (!isAdded()) return;
        
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_rate_driver, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
                
        android.widget.RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        android.widget.EditText etComment = dialogView.findViewById(R.id.etComment);
        android.widget.Button btnSubmit = dialogView.findViewById(R.id.btnSubmitRating);
        
        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString();
            
            if (rating == 0) {
                android.widget.Toast.makeText(getContext(), "Lütfen bir puan verin", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            DatabaseReference ratingRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/")
                    .getReference("ratings").child(driverId).push();
            
            java.util.HashMap<String, Object> ratingData = new java.util.HashMap<>();
            ratingData.put("passengerId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            ratingData.put("rating", rating);
            ratingData.put("comment", comment);
            ratingData.put("timestamp", ServerValue.TIMESTAMP);
            
            ratingRef.setValue(ratingData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    android.widget.Toast.makeText(getContext(), "Değerlendirmeniz için teşekkürler!", android.widget.Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        });
        
        dialog.show();
    }
}
