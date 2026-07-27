package com.example.araccagir;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import android.os.Looper;
import android.location.Location;

import java.util.ArrayList;

public class DriverMapFragment extends Fragment implements OnMapReadyCallback {

    private String currentRequestId = null;
    private GoogleMap mMap;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DatabaseReference driversAvailableRef;
    private GeoFire geoFire;
    private String currentDriverId;
    private Button btnAccept, btnReject, btnFinishRide;
    private android.app.AlertDialog paymentDialog;
    @Nullable String driverVehicleType = "Standart";
    private String googleApiKey;
    private com.android.volley.RequestQueue requestQueue;
    private com.google.android.gms.maps.model.Polyline currentPolyline;
    private android.location.Location lastDriverLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(requireContext());

            try {
                android.content.pm.ApplicationInfo ai = requireContext().getPackageManager().getApplicationInfo(requireContext().getPackageName(), android.content.pm.PackageManager.GET_META_DATA);
                if (ai.metaData != null) {
                    googleApiKey = ai.metaData.getString("com.google.android.geo.API_KEY");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult == null) return;
                    for (Location location : locationResult.getLocations()) {
                        lastDriverLocation = location;
                        if (currentDriverId != null) {
                            geoFire.setLocation(currentDriverId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                        }
                    }
                }
            };

            View view = inflater.inflate(R.layout.fragment_driver_map, container, false);

            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            requestPermissionsIfNecessary(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });

            LinearLayout llRideRequest = view.findViewById(R.id.llRideRequest);
            btnAccept = view.findViewById(R.id.btnAccept);
            btnReject = view.findViewById(R.id.btnReject);
            btnFinishRide = view.findViewById(R.id.btnFinishRide);
            TextView tvPassengerInfo = view.findViewById(R.id.tvPassengerInfo);

            driversAvailableRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("drivers_available");
            geoFire = new GeoFire(driversAvailableRef);
            
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                currentDriverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            com.google.android.material.floatingactionbutton.FloatingActionButton fabNav = view.findViewById(R.id.fabNavigation);
            fabNav.setOnClickListener(v -> {
                if (currentRequestId != null) {
                    DatabaseReference ref = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("ride_requests").child(currentRequestId);
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Double lat = snapshot.child("destLat").getValue(Double.class);
                                Double lng = snapshot.child("destLng").getValue(Double.class);
                                if (lat != null && lng != null) {
                                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse("google.navigation:q=" + lat + "," + lng));
                                    intent.setPackage("com.google.android.apps.maps");
                                    if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                                        startActivity(intent);
                                    } else {
                                        // Google Maps yoksa tarayıcıdan aç
                                        startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng)));
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            });
            if (currentDriverId != null) {
                FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("users")
                        .child(currentDriverId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;
                        if (snapshot.exists() && snapshot.hasChild("vehicleType")) {
                            driverVehicleType = snapshot.child("vehicleType").getValue(String.class);
                        }
                        
                        DatabaseReference allRequestsRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("ride_requests");
                        allRequestsRef.orderByChild("driverId").equalTo(currentDriverId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                if (!isAdded()) return;
                                for (DataSnapshot reqSnapshot : snapshot2.getChildren()) {
                                    String status = reqSnapshot.child("status").getValue(String.class);
                                    if ("accepted".equals(status) || "payment_pending".equals(status)) {
                                        currentRequestId = reqSnapshot.getKey();
                                        llRideRequest.setVisibility(View.GONE);
                                        btnFinishRide.setVisibility(View.VISIBLE);
                                        fabNav.setVisibility(View.VISIBLE);
                                        tvPassengerInfo.setText("Aktif sürüş devam ediyor...");
                                        
                                        if ("payment_pending".equals(status)) {
                                            showDriverPaymentDialog(reqSnapshot.getRef());
                                        }
                                        break;
                                    }
                                }
                                listenForPendingRequests(llRideRequest, tvPassengerInfo);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                listenForPendingRequests(llRideRequest, tvPassengerInfo);
                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listenForPendingRequests(llRideRequest, tvPassengerInfo);
                    }
                });
            } else {
                listenForPendingRequests(llRideRequest, tvPassengerInfo);
            }

            btnAccept.setOnClickListener(v -> {
                if (currentRequestId != null) {
                    DatabaseReference ref = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("ride_requests")
                            .child(currentRequestId);
                    ref.child("status").setValue("accepted");
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        ref.child("driverId").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    }
                    tvPassengerInfo.setText("Yolcuya gidiliyor...");
                    
                    llRideRequest.setVisibility(View.GONE);
                    btnFinishRide.setVisibility(View.VISIBLE);
                    fabNav.setVisibility(View.VISIBLE); // Navigasyon butonunu göster
                    
                    Toast.makeText(getContext(), "Çağrı kabul edildi!", Toast.LENGTH_SHORT).show();
                    
                    // Rotayı çiz
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Double lat = snapshot.child("destLat").getValue(Double.class);
                                Double lng = snapshot.child("destLng").getValue(Double.class);
                                if (lat != null && lng != null && lastDriverLocation != null) {
                                    drawRoute(new com.google.android.gms.maps.model.LatLng(lastDriverLocation.getLatitude(), lastDriverLocation.getLongitude()), 
                                              new com.google.android.gms.maps.model.LatLng(lat, lng));
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            });

            btnReject.setOnClickListener(v -> {
                llRideRequest.setVisibility(View.GONE);
                currentRequestId = null;
                fabNav.setVisibility(View.GONE);
            });

            btnFinishRide.setOnClickListener(v -> {
                if (currentRequestId != null) {
                    DatabaseReference rideRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("ride_requests")
                            .child(currentRequestId);
                    rideRef.child("status").setValue("payment_pending");
                    showDriverPaymentDialog(rideRef);
                    fabNav.setVisibility(View.GONE);
                }
            });

            return view;
        } catch (Exception e) {
            android.util.Log.e("CrashDebug", "DriverMap onCreateView error", e);
            Toast.makeText(getContext(), "Harita Ekranı Hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return inflater.inflate(R.layout.fragment_driver_map, container, false);
        }
    }

    private void listenForPendingRequests(LinearLayout llRideRequest, TextView tvInfo) {
        DatabaseReference requestsRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/").getReference("ride_requests");
        requestsRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if (currentRequestId != null) return; // Zaten bir sürüşteyiz, bekleyenleri tarama
                 
                 for (DataSnapshot reqSnapshot : snapshot.getChildren()) {
                     String status = reqSnapshot.child("status").getValue(String.class);
                     String reqVehicleType = reqSnapshot.child("vehicleType").getValue(String.class);
                     
                     if ("pending".equals(status)) {
                         if (reqVehicleType == null || reqVehicleType.equals(driverVehicleType)) {
                             String dest = reqSnapshot.child("destination").getValue(String.class);
                             currentRequestId = reqSnapshot.getKey();
                             
                             String displayType = reqVehicleType != null ? reqVehicleType : "Bilinmiyor";
                             tvInfo.setText("Yeni yolcu isteği! Tür: " + displayType + " - Hedef: " + dest);
                             llRideRequest.setVisibility(View.VISIBLE);
                             playNotificationSound();
                             return; // Shows the first pending matching request
                         }
                     }
                 }
                 // Hide if no requests
                 llRideRequest.setVisibility(View.GONE);
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void showDriverPaymentDialog(DatabaseReference rideRef) {
        if (!isAdded()) return;
        
        rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String price = snapshot.child("price").getValue(String.class);
                final String finalPrice = (price == null) ? "Bilinmiyor" : price;
                
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
                builder.setTitle("Ödeme Bekleniyor");
                builder.setMessage("Tutar: " + finalPrice + "\n\nLütfen ödeme yöntemini seçin veya yolcunun uygulamadan ödemesini bekleyin.");
                
                builder.setPositiveButton("Nakit Alındı", (dialog, which) -> {
                    rideRef.child("paymentMethod").setValue("cash");
                    rideRef.child("status").setValue("paid");
                    saveRideHistory(snapshot.child("destination").getValue(String.class), finalPrice, "cash");
                    playNotificationSound();
                    resetDriverUI();
                });
                builder.setNegativeButton("POS ile Alındı", (dialog, which) -> {
                    rideRef.child("paymentMethod").setValue("pos");
                    rideRef.child("status").setValue("paid");
                    saveRideHistory(snapshot.child("destination").getValue(String.class), finalPrice, "pos");
                    playNotificationSound();
                    resetDriverUI();
                });
                builder.setCancelable(false);
                paymentDialog = builder.create();
                paymentDialog.show();
                
                rideRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        String status = snap.child("status").getValue(String.class);
                        String method = snap.child("paymentMethod").getValue(String.class);
                        if ("paid".equals(status) && ("app".equals(method) || "app_card".equals(method) || "wallet".equals(method))) {
                            if (paymentDialog != null && paymentDialog.isShowing()) {
                                paymentDialog.dismiss();
                            }
                            saveRideHistory(snapshot.child("destination").getValue(String.class), finalPrice, method);
                            Toast.makeText(getContext(), "Yolcu ödemeyi uygulama/cüzdan üzerinden yaptı!", Toast.LENGTH_LONG).show();
                            playNotificationSound();
                            resetDriverUI();
                            rideRef.removeEventListener(this);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void resetDriverUI() {
        if (!isAdded() || getView() == null) return;
        LinearLayout llRideRequest = getView().findViewById(R.id.llRideRequest);
        if (llRideRequest != null) llRideRequest.setVisibility(View.GONE);
        if (btnAccept != null) btnAccept.setVisibility(View.VISIBLE);
        if (btnReject != null) btnReject.setVisibility(View.VISIBLE);
        if (btnFinishRide != null) btnFinishRide.setVisibility(View.GONE);
        currentRequestId = null;
        Toast.makeText(getContext(), "Sürüş tamamlandı, yeni çağrılar bekleniyor.", Toast.LENGTH_SHORT).show();
    }

    private void saveRideHistory(String destination, String price, String paymentMethod) {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null || destination == null) return;
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        String date = new java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm", new java.util.Locale("tr")).format(new java.util.Date());
        
        DatabaseReference historyRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/")
                .getReference("ride_history").child(uid).push();
                
        RideHistory history = new RideHistory(destination, price, date, paymentMethod, "Sürücü");
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

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (fusedLocationClient == null || locationCallback == null) return;
        
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
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
        if (currentDriverId != null && geoFire != null) {
            geoFire.removeLocation(currentDriverId);
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

    private void drawRoute(com.google.android.gms.maps.model.LatLng origin, com.google.android.gms.maps.model.LatLng dest) {
        if (origin == null || dest == null || googleApiKey == null) return;
        
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + dest.latitude + "," + dest.longitude +
                "&key=" + googleApiKey;

        com.android.volley.toolbox.JsonObjectRequest jsonObjectRequest = new com.android.volley.toolbox.JsonObjectRequest
                (com.android.volley.Request.Method.GET, url, null, response -> {
                    try {
                        org.json.JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() > 0) {
                            org.json.JSONObject route = routes.getJSONObject(0);
                            org.json.JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                            String encodedString = overviewPolyline.getString("points");
                            java.util.List<com.google.android.gms.maps.model.LatLng> points = decodePoly(encodedString);

                            if (currentPolyline != null) {
                                currentPolyline.remove();
                            }
                            
                            com.google.android.gms.maps.model.PolylineOptions options = new com.google.android.gms.maps.model.PolylineOptions()
                                    .addAll(points)
                                    .width(12f)
                                    .color(android.graphics.Color.parseColor("#007BFF"))
                                    .geodesic(true);

                            currentPolyline = mMap.addPolyline(options);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> { });

        requestQueue.add(jsonObjectRequest);
    }

    private java.util.List<com.google.android.gms.maps.model.LatLng> decodePoly(String encoded) {
        java.util.ArrayList<com.google.android.gms.maps.model.LatLng> poly = new java.util.ArrayList<>();
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
            poly.add(new com.google.android.gms.maps.model.LatLng((((double) lat / 1E5)), (((double) lng / 1E5))));
        }
        return poly;
    }
}
