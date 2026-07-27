package com.example.araccagir;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private TextView tvEmptyHistory;
    private HistoryAdapter historyAdapter;
    private List<RideHistory> historyList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        rvHistory = view.findViewById(R.id.rvHistory);
        tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyList);
        rvHistory.setAdapter(historyAdapter);

        loadHistoryData();

        return view;
    }

    private void loadHistoryData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference historyRef = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/")
                .getReference("ride_history").child(uid);

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    RideHistory history = dataSnapshot.getValue(RideHistory.class);
                    if (history != null) {
                        historyList.add(history);
                    }
                }
                
                // En yeniyi en üstte göstermek için ters çevir
                Collections.reverse(historyList);
                
                historyAdapter.notifyDataSetChanged();

                if (historyList.isEmpty()) {
                    rvHistory.setVisibility(View.GONE);
                    tvEmptyHistory.setVisibility(View.VISIBLE);
                } else {
                    rvHistory.setVisibility(View.VISIBLE);
                    tvEmptyHistory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
