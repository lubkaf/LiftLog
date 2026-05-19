package com.example.liftlog.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.liftlog.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class HistoryAdapter extends ListAdapter<HistoryItem, HistoryAdapter.VH> {

    public interface OnSessionClick {
        void onClick(HistoryItem item);
    }

    private final OnSessionClick listener;

    public HistoryAdapter(OnSessionClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        HistoryItem item = getItem(position);
        holder.date.setText(formatDate(item.dateIso));
        holder.summary.setText(String.format(java.util.Locale.getDefault(),
                "%d min · wolumen %.0f kg", item.durationMinutes, item.volumeKg));
        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    private static String formatDate(String iso) {
        if (iso == null) return "";
        try {
            LocalDateTime dt = LocalDateTime.parse(iso);
            return dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        } catch (DateTimeParseException e) {
            return iso;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView date;
        final TextView summary;

        VH(@NonNull View v) {
            super(v);
            date = v.findViewById(R.id.text_session_date);
            summary = v.findViewById(R.id.text_session_summary);
        }
    }

    private static final DiffUtil.ItemCallback<HistoryItem> DIFF =
            new DiffUtil.ItemCallback<HistoryItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull HistoryItem a, @NonNull HistoryItem b) {
                    return a.sessionId == b.sessionId;
                }

                @Override
                public boolean areContentsTheSame(@NonNull HistoryItem a, @NonNull HistoryItem b) {
                    return a.durationMinutes == b.durationMinutes
                            && a.volumeKg == b.volumeKg
                            && (a.dateIso == null ? b.dateIso == null : a.dateIso.equals(b.dateIso));
                }
            };
}
