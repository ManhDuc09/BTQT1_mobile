package com.example.btqt1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.text.format.DateFormat;

import com.example.btqt1.Model.ConversionRecord;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<ConversionRecord> records;

    public HistoryAdapter(List<ConversionRecord> records) {
        this.records = records;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ConversionRecord record = records.get(position);
        String time = DateFormat.format("yyyy-MM-dd HH:mm", record.timestamp).toString();
        holder.tvLine1.setText(time + " — " + record.amount + " " + record.type);
        holder.tvLine2.setText(String.format("%,d VND", (long) record.result));
    }


    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLine1, tvLine2;
        public ViewHolder(View itemView) {
            super(itemView);
            tvLine1 = itemView.findViewById(R.id.tvLine1);
            tvLine2 = itemView.findViewById(R.id.tvLine2);
        }
    }
}