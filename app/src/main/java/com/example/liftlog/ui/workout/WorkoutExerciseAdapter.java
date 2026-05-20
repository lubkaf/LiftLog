package com.example.liftlog.ui.workout;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.liftlog.R;
import com.example.liftlog.data.model.SessionSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkoutExerciseAdapter extends RecyclerView.Adapter<WorkoutExerciseAdapter.ExerciseVH> {

    public interface WorkoutCallback {
        void onLogSet(int exerciseId, float weightKg, int reps);
        void onStartTimer(int seconds);
        void onDeleteSet(SessionSet set);
    }

    private final WorkoutCallback callback;
    private final List<ActiveWorkoutViewModel.ExerciseEntry> items = new ArrayList<>();
    private final Map<Integer, String> weightInput = new HashMap<>();
    private final Map<Integer, String> repsInput = new HashMap<>();
    private final Map<Integer, List<SessionSet>> setsMap = new HashMap<>();

    public WorkoutExerciseAdapter(WorkoutCallback callback) {
        this.callback = callback;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).exerciseId;
    }

    public void setExercises(List<ActiveWorkoutViewModel.ExerciseEntry> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void moveItem(int from, int to) {
        Collections.swap(items, from, to);
        notifyItemMoved(from, to);
    }

    public List<ActiveWorkoutViewModel.ExerciseEntry> getItems() {
        return items;
    }

    public void updateSets(List<SessionSet> allSets) {
        setsMap.clear();
        if (allSets != null) {
            for (SessionSet s : allSets) {
                setsMap.computeIfAbsent(s.exerciseId, k -> new ArrayList<>()).add(s);
            }
        }
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public ExerciseVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_exercise, parent, false);
        return new ExerciseVH(v, callback);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseVH holder, int position) {
        ActiveWorkoutViewModel.ExerciseEntry entry = items.get(position);
        List<SessionSet> sets = setsMap.getOrDefault(entry.exerciseId, new ArrayList<>());
        holder.bind(entry, sets, weightInput, repsInput, callback);
    }

    // ── Inner adapter for logged sets ─────────────────────────────────────────

    static class LoggedSetAdapter extends ListAdapter<SessionSet, LoggedSetAdapter.SetVH> {

        interface DeleteCallback {
            void onDelete(SessionSet set);
        }

        private final DeleteCallback deleteCallback;

        LoggedSetAdapter(DeleteCallback deleteCallback) {
            super(SET_DIFF);
            this.deleteCallback = deleteCallback;
        }

        void onSwipeDelete(int position) {
            if (position >= 0 && position < getCurrentList().size()) {
                deleteCallback.onDelete(getItem(position));
            }
        }

        @NonNull
        @Override
        public SetVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_logged_set, parent, false);
            return new SetVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SetVH holder, int position) {
            SessionSet s = getItem(position);
            holder.summary.setText(
                    String.format(Locale.getDefault(), "%.1f kg × %d", s.weightKg, s.repsDone));
        }

        static class SetVH extends RecyclerView.ViewHolder {
            final TextView summary;
            SetVH(@NonNull View v) {
                super(v);
                summary = v.findViewById(R.id.text_set_summary);
            }
        }

        private static final DiffUtil.ItemCallback<SessionSet> SET_DIFF =
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

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class ExerciseVH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView setsCount;
        final RecyclerView recyclerSets;
        final TextInputEditText editWeight;
        final TextInputEditText editReps;
        final MaterialButton btnLog;
        final MaterialButton btn60;
        final MaterialButton btn90;
        final MaterialButton btn120;

        final LoggedSetAdapter setAdapter;

        TextWatcher weightWatcher;
        TextWatcher repsWatcher;

        ExerciseVH(@NonNull View v, WorkoutCallback workoutCallback) {
            super(v);
            name = v.findViewById(R.id.text_exercise_name);
            setsCount = v.findViewById(R.id.text_sets_count);
            recyclerSets = v.findViewById(R.id.recycler_logged_sets);
            editWeight = v.findViewById(R.id.edit_weight);
            editReps = v.findViewById(R.id.edit_reps);
            btnLog = v.findViewById(R.id.btn_log_set);
            btn60 = v.findViewById(R.id.btn_rest_60);
            btn90 = v.findViewById(R.id.btn_rest_90);
            btn120 = v.findViewById(R.id.btn_rest_120);

            setAdapter = new LoggedSetAdapter(workoutCallback::onDeleteSet);
            recyclerSets.setLayoutManager(new LinearLayoutManager(v.getContext()));
            recyclerSets.setAdapter(setAdapter);

            new ItemTouchHelper(new SwipeToDeleteCallback(setAdapter))
                    .attachToRecyclerView(recyclerSets);
        }

        void bind(
                ActiveWorkoutViewModel.ExerciseEntry entry,
                List<SessionSet> sets,
                Map<Integer, String> weightInput,
                Map<Integer, String> repsInput,
                WorkoutCallback callback
        ) {
            int exId = entry.exerciseId;

            name.setText(entry.name);
            setsCount.setText(itemView.getContext()
                    .getString(R.string.workout_sets_count, sets.size()));

            setAdapter.submitList(new ArrayList<>(sets));

            if (weightWatcher != null) editWeight.removeTextChangedListener(weightWatcher);
            if (repsWatcher != null) editReps.removeTextChangedListener(repsWatcher);

            editWeight.setText(weightInput.getOrDefault(exId, ""));
            editReps.setText(repsInput.getOrDefault(exId, ""));

            weightWatcher = simpleWatcher(s -> weightInput.put(exId, s));
            repsWatcher = simpleWatcher(s -> repsInput.put(exId, s));
            editWeight.addTextChangedListener(weightWatcher);
            editReps.addTextChangedListener(repsWatcher);

            btnLog.setOnClickListener(v -> {
                Float weight = parseFloat(editWeight);
                Integer reps = parseInt(editReps);
                if (weight == null || reps == null || weight < 0 || reps <= 0) {
                    Toast.makeText(itemView.getContext(),
                            R.string.workout_validation_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                callback.onLogSet(exId, weight, reps);
                editWeight.setText("");
                editReps.setText("");
                weightInput.remove(exId);
                repsInput.remove(exId);
            });

            btn60.setOnClickListener(v -> callback.onStartTimer(60));
            btn90.setOnClickListener(v -> callback.onStartTimer(90));
            btn120.setOnClickListener(v -> callback.onStartTimer(120));
        }

        @Nullable
        private static Float parseFloat(TextInputEditText edit) {
            String s = edit.getText() == null ? "" : edit.getText().toString().replace(',', '.');
            if (s.isEmpty()) return null;
            try { return Float.parseFloat(s); } catch (NumberFormatException e) { return null; }
        }

        @Nullable
        private static Integer parseInt(TextInputEditText edit) {
            String s = edit.getText() == null ? "" : edit.getText().toString();
            if (s.isEmpty()) return null;
            try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
        }

        private static TextWatcher simpleWatcher(java.util.function.Consumer<String> cb) {
            return new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void afterTextChanged(Editable s) { cb.accept(s.toString()); }
            };
        }
    }

    // ── Swipe callback for nested set RecyclerView ────────────────────────────

    private static class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private final LoggedSetAdapter adapter;
        private final Paint paint = new Paint();

        SwipeToDeleteCallback(LoggedSetAdapter adapter) {
            super(0, ItemTouchHelper.LEFT);
            this.adapter = adapter;
            paint.setColor(Color.parseColor("#B00020"));
        }

        @Override
        public boolean onMove(@NonNull RecyclerView rv,
                              @NonNull RecyclerView.ViewHolder a,
                              @NonNull RecyclerView.ViewHolder b) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
            adapter.onSwipeDelete(vh.getAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                @NonNull RecyclerView.ViewHolder vh,
                                float dX, float dY, int actionState, boolean isActive) {
            View item = vh.itemView;
            c.drawRect(item.getRight() + dX, item.getTop(), item.getRight(), item.getBottom(), paint);
            super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive);
        }
    }
}
