package com.example.araccagir;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
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
import java.util.List;

public class ReviewsFragment extends Fragment {

    private RecyclerView rvReviews;
    private ReviewAdapter adapter;
    private List<Review> reviewList;
    private TextView tvAverageRating, tvTotalReviews;
    private RatingBar avgRatingBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);

        tvAverageRating = view.findViewById(R.id.tvAverageRating);
        tvTotalReviews = view.findViewById(R.id.tvTotalReviews);
        avgRatingBar = view.findViewById(R.id.avgRatingBar);
        rvReviews = view.findViewById(R.id.rvReviews);

        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewList = new ArrayList<>();
        adapter = new ReviewAdapter(reviewList);
        rvReviews.setAdapter(adapter);

        loadReviews();

        return view;
    }

    private void loadReviews() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance("https://arac-cagir-default-rtdb.firebaseio.com/")
                .getReference("ratings").child(driverId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                float totalRating = 0;
                int count = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    Review review = data.getValue(Review.class);
                    if (review != null) {
                        reviewList.add(0, review); // En yeni en üstte
                        totalRating += review.getRating();
                        count++;
                    }
                }

                if (count > 0) {
                    float avg = totalRating / count;
                    tvAverageRating.setText(String.format("%.1f", avg));
                    avgRatingBar.setRating(avg);
                    tvTotalReviews.setText(count + " Değerlendirme");
                } else {
                    tvAverageRating.setText("0.0");
                    avgRatingBar.setRating(0);
                    tvTotalReviews.setText("Henüz değerlendirme yok");
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
