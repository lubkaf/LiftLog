package com.example.liftlog.ui.planedit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.liftlog.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PlanEditFragment extends Fragment {

    public static final String RESULT_KEY = "pick_exercise";
    public static final String RESULT_BUNDLE_KEY = "exerciseId";

    private PlanEditViewModel viewModel;
    private PlanExerciseAdapter adapter;
    private TextInputEditText editName;
    private TextView textEmpty;
    private boolean nameInitialized = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plan_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PlanEditViewModel.class);
        int planId = getArguments() != null ? getArguments().getInt("planId", -1) : -1;

        editName = view.findViewById(R.id.edit_plan_name);
        textEmpty = view.findViewById(R.id.text_empty);
        RecyclerView recycler = view.findViewById(R.id.recycler_exercises);
        MaterialButton btnAdd = view.findViewById(R.id.btn_add_exercise);
        MaterialButton btnSave = view.findViewById(R.id.btn_save_plan);

        adapter = new PlanExerciseAdapter(position -> {
            adapter.removeAt(position);
            updateEmptyState();
        });
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);
        setupDragAndDrop(recycler);

        btnAdd.setOnClickListener(v -> openLibraryPicker());
        btnSave.setOnClickListener(v -> {
            String name = editName.getText() == null ? "" : editName.getText().toString();
            if (!viewModel.save(name, adapter.snapshot())) {
                Toast.makeText(requireContext(), R.string.planedit_save_error,
                        Toast.LENGTH_LONG).show();
            }
        });

        getParentFragmentManager().setFragmentResultListener(RESULT_KEY, getViewLifecycleOwner(),
                (key, bundle) -> {
                    int exId = bundle.getInt(RESULT_BUNDLE_KEY, -1);
                    if (exId > 0) viewModel.requestAddExercise(exId);
                });

        viewModel.getInitial().observe(getViewLifecycleOwner(), data -> {
            if (data == null || nameInitialized) return;
            nameInitialized = true;
            editName.setText(data.name);
            adapter.setItems(data.rows);
            updateEmptyState();
        });
        viewModel.getExerciseAdded().observe(getViewLifecycleOwner(), row -> {
            if (row == null) return;
            adapter.addItem(row);
            updateEmptyState();
            viewModel.consumeExerciseAdded();
        });
        viewModel.getSaved().observe(getViewLifecycleOwner(), saved -> {
            if (Boolean.TRUE.equals(saved)) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });

        viewModel.load(planId);
    }

    private void openLibraryPicker() {
        Bundle args = new Bundle();
        args.putBoolean("pickerMode", true);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_planEdit_to_libraryPicker, args);
    }

    private void updateEmptyState() {
        textEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void setupDragAndDrop(RecyclerView recycler) {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getBindingAdapterPosition();
                int to = target.getBindingAdapterPosition();
                adapter.moveItem(from, to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recycler);
    }
}
