package com.example.liftlog.ui.planedit;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.liftlog.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlanExerciseAdapter extends RecyclerView.Adapter<PlanExerciseAdapter.RowVH> {

    public interface OnRowDeleted {
        void onDelete(int position);
    }

    private final List<PlanExerciseRow> items = new ArrayList<>();
    private final OnRowDeleted onDeleted;

    public PlanExerciseAdapter(OnRowDeleted onDeleted) {
        this.onDeleted = onDeleted;
    }

    public void setItems(List<PlanExerciseRow> rows) {
        items.clear();
        if (rows != null) items.addAll(rows);
        notifyDataSetChanged();
    }

    public void addItem(PlanExerciseRow row) {
        items.add(row);
        notifyItemInserted(items.size() - 1);
    }

    public void removeAt(int position) {
        if (position < 0 || position >= items.size()) return;
        items.remove(position);
        notifyItemRemoved(position);
    }

    public List<PlanExerciseRow> snapshot() {
        return new ArrayList<>(items);
    }

    public void moveItem(int from, int to) {
        if (from < 0 || to < 0 || from >= items.size() || to >= items.size()) return;
        Collections.swap(items, from, to);
        notifyItemMoved(from, to);
    }

    @NonNull
    @Override
    public RowVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_exercise, parent, false);
        return new RowVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RowVH holder, int position) {
        holder.bind(items, position, onDeleted);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RowVH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextInputEditText sets;
        final TextInputEditText reps;
        final ImageButton delete;
        TextWatcher setsWatcher;
        TextWatcher repsWatcher;

        RowVH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.text_exercise_name);
            sets = v.findViewById(R.id.edit_sets);
            reps = v.findViewById(R.id.edit_reps);
            delete = v.findViewById(R.id.btn_delete);
        }

        void bind(List<PlanExerciseRow> items, int position, OnRowDeleted onDeleted) {
            PlanExerciseRow row = items.get(position);
            if (setsWatcher != null) sets.removeTextChangedListener(setsWatcher);
            if (repsWatcher != null) reps.removeTextChangedListener(repsWatcher);

            name.setText(row.exerciseName);
            sets.setText(String.valueOf(row.sets));
            reps.setText(String.valueOf(row.reps));

            setsWatcher = onChange(s -> {
                int pos = getAbsoluteAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || pos >= items.size()) return;
                items.get(pos).sets = parseSafe(s);
            });
            repsWatcher = onChange(s -> {
                int pos = getAbsoluteAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || pos >= items.size()) return;
                items.get(pos).reps = parseSafe(s);
            });
            sets.addTextChangedListener(setsWatcher);
            reps.addTextChangedListener(repsWatcher);

            delete.setOnClickListener(v -> {
                int pos = getAbsoluteAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) onDeleted.onDelete(pos);
            });
        }

        private int parseSafe(String s) {
            try {
                return Math.max(0, Integer.parseInt(s));
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private TextWatcher onChange(java.util.function.Consumer<String> cb) {
            return new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    cb.accept(s.toString());
                }
            };
        }
    }
}
