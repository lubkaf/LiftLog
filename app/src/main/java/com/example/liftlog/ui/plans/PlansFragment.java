package com.example.liftlog.ui.plans;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.liftlog.R;
import com.example.liftlog.data.model.TrainingPlan;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class PlansFragment extends Fragment implements PlansAdapter.Callbacks {

    private PlansViewModel viewModel;
    private PlansAdapter adapter;
    private TextView textEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plans, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PlansViewModel.class);
        adapter = new PlansAdapter(this);
        textEmpty = view.findViewById(R.id.text_empty);

        RecyclerView recycler = view.findViewById(R.id.recycler_plans);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        ExtendedFloatingActionButton fab = view.findViewById(R.id.fab_add_plan);
        fab.setOnClickListener(v -> showCreateDialog());

        viewModel.getPlans().observe(getViewLifecycleOwner(), plans -> {
            adapter.submitList(plans);
            textEmpty.setVisibility(plans == null || plans.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void showCreateDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_create_plan, null, false);
        TextInputEditText input = dialogView.findViewById(R.id.edit_plan_name);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.plans_new_plan_title)
                .setView(dialogView)
                .setPositiveButton(R.string.common_save, (d, w) -> {
                    String name = input.getText() == null ? "" : input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    viewModel.createPlan(name);
                })
                .setNegativeButton(R.string.common_cancel, null)
                .show();
    }

    @Override
    public void onEditPlan(TrainingPlan plan) {
        Bundle args = new Bundle();
        args.putInt("planId", plan.id);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_plans_to_planEdit, args);
    }

    @Override
    public void onStartPlan(TrainingPlan plan) {
        Bundle args = new Bundle();
        args.putInt("planId", plan.id);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_plans_to_activeWorkout, args);
    }

    @Override
    public void onDeletePlan(TrainingPlan plan) {
        new AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.plans_delete_confirm, plan.name))
                .setPositiveButton(R.string.common_delete, (d, w) -> viewModel.deletePlan(plan))
                .setNegativeButton(R.string.common_cancel, null)
                .show();
    }
}
