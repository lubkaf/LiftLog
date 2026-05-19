package com.example.liftlog.ui.plans;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.liftlog.R;
import com.example.liftlog.data.model.TrainingPlan;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PlansAdapter extends ListAdapter<TrainingPlan, PlansAdapter.PlanVH> {

    public interface Callbacks {
        void onEditPlan(TrainingPlan plan);
        void onStartPlan(TrainingPlan plan);
        void onDeletePlan(TrainingPlan plan);
    }

    private final Callbacks callbacks;

    public PlansAdapter(Callbacks callbacks) {
        super(DIFF);
        this.callbacks = callbacks;
    }

    @NonNull
    @Override
    public PlanVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan, parent, false);
        return new PlanVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanVH holder, int position) {
        holder.bind(getItem(position), callbacks);
    }

    static class PlanVH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView date;
        final ImageButton start;
        final ImageButton delete;

        PlanVH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.text_plan_name);
            date = v.findViewById(R.id.text_plan_date);
            start = v.findViewById(R.id.btn_start);
            delete = v.findViewById(R.id.btn_delete);
        }

        void bind(TrainingPlan plan, Callbacks cb) {
            name.setText(plan.name);
            date.setText(formatDate(plan.createdAt));
            itemView.setOnClickListener(v -> cb.onEditPlan(plan));
            start.setOnClickListener(v -> cb.onStartPlan(plan));
            delete.setOnClickListener(v -> cb.onDeletePlan(plan));
        }

        private static String formatDate(String iso) {
            if (iso == null) return "";
            try {
                LocalDateTime dt = LocalDateTime.parse(iso);
                return "utworzono " + dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } catch (DateTimeParseException e) {
                return iso;
            }
        }
    }

    private static final DiffUtil.ItemCallback<TrainingPlan> DIFF =
            new DiffUtil.ItemCallback<TrainingPlan>() {
                @Override
                public boolean areItemsTheSame(@NonNull TrainingPlan a, @NonNull TrainingPlan b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull TrainingPlan a, @NonNull TrainingPlan b) {
                    return a.id == b.id
                            && a.name.equals(b.name)
                            && (a.createdAt == null ? b.createdAt == null
                                    : a.createdAt.equals(b.createdAt));
                }
            };
}
