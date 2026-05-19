package com.example.liftlog.ui.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.liftlog.R;
import com.example.liftlog.data.model.SessionSet;

public class ActiveSetAdapter extends ListAdapter<SessionSet, ActiveSetAdapter.VH> {

    public ActiveSetAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_set, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SessionSet s = getItem(position);
        holder.number.setText(String.format(java.util.Locale.getDefault(), "%d.", s.setNumber));
        holder.summary.setText(String.format(java.util.Locale.getDefault(),
                "%.1f kg × %d", s.weightKg, s.repsDone));
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView number;
        final TextView summary;

        VH(@NonNull View v) {
            super(v);
            number = v.findViewById(R.id.text_set_number);
            summary = v.findViewById(R.id.text_set_summary);
        }
    }

    private static final DiffUtil.ItemCallback<SessionSet> DIFF =
            new DiffUtil.ItemCallback<SessionSet>() {
                @Override
                public boolean areItemsTheSame(@NonNull SessionSet a, @NonNull SessionSet b) {
                    return a.exerciseId == b.exerciseId && a.setNumber == b.setNumber;
                }

                @Override
                public boolean areContentsTheSame(@NonNull SessionSet a, @NonNull SessionSet b) {
                    return a.weightKg == b.weightKg && a.repsDone == b.repsDone;
                }
            };
}
