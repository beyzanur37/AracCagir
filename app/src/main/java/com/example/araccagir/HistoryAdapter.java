package com.example.araccagir;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<RideHistory> historyList;

    public HistoryAdapter(List<RideHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        RideHistory history = historyList.get(position);
        holder.tvHistoryDestination.setText(history.getDestination());
        
        // Fiyat formatlaması
        String price = history.getPrice();
        if (price != null && !price.contains("TL")) {
            price += " TL";
        }
        holder.tvHistoryPrice.setText(price);
        
        holder.tvHistoryDate.setText(history.getDate());
        
        // Ödeme yöntemi çevirisi
        String method = history.getPaymentMethod();
        if ("app".equals(method) || "app_card".equals(method)) {
            holder.tvHistoryMethod.setText("Kredi Kartı");
        } else if ("wallet".equals(method)) {
            holder.tvHistoryMethod.setText("Cüzdan");
        } else if ("cash".equals(method)) {
            holder.tvHistoryMethod.setText("Nakit");
        } else if ("pos".equals(method)) {
            holder.tvHistoryMethod.setText("Kredi Kartı (POS)");
        } else {
            holder.tvHistoryMethod.setText(method != null ? method : "Bilinmiyor");
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryDestination, tvHistoryPrice, tvHistoryDate, tvHistoryMethod;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryDestination = itemView.findViewById(R.id.tvHistoryDestination);
            tvHistoryPrice = itemView.findViewById(R.id.tvHistoryPrice);
            tvHistoryDate = itemView.findViewById(R.id.tvHistoryDate);
            tvHistoryMethod = itemView.findViewById(R.id.tvHistoryMethod);
        }
    }
}
