package com.example.liftlog.ui.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.liftlog.R;
import com.example.liftlog.data.model.Exercise;

public class ExerciseAdapter extends ListAdapter<Exercise, ExerciseAdapter.VH> {

    public interface OnClick {
        void onExerciseClick(Exercise exercise);
    }

    private final OnClick listener;

    public ExerciseAdapter(OnClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Exercise e = getItem(position);
        holder.name.setText(e.name);
        holder.group.setText(e.muscleGroup);
        holder.badgeCustom.setVisibility(e.isCustom == 1 ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> listener.onExerciseClick(e));
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView group;
        final TextView badgeCustom;

        VH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.text_exercise_name);
            group = v.findViewById(R.id.text_exercise_group);
            badgeCustom = v.findViewById(R.id.badge_custom);
        }
    }

    private static final DiffUtil.ItemCallback<Exercise> DIFF =
            new DiffUtil.ItemCallback<Exercise>() {
                @Override
                public boolean areItemsTheSame(@NonNull Exercise a, @NonNull Exercise b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Exercise a, @NonNull Exercise b) {
                    return a.id == b.id && a.name.equals(b.name)
                            && (a.muscleGroup == null ? b.muscleGroup == null
                                    : a.muscleGroup.equals(b.muscleGroup))
                            && a.isCustom == b.isCustom;
                }
            };
}
