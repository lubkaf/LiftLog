package com.example.liftlog.ui.summary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.liftlog.R;
import com.google.android.material.button.MaterialButton;

public class SummaryFragment extends Fragment {

    private SummaryViewModel viewModel;
    private TextView textVolume;
    private TextView textDuration;
    private TextView textSetsCount;
    private LinearLayout listPr;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SummaryViewModel.class);

        textVolume = view.findViewById(R.id.text_volume);
        textDuration = view.findViewById(R.id.text_duration);
        textSetsCount = view.findViewById(R.id.text_sets_count);
        listPr = view.findViewById(R.id.list_pr);
        MaterialButton btnBack = view.findViewById(R.id.btn_back_dashboard);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .popBackStack(R.id.dashboardFragment, false));

        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        viewModel.loadLatest();
    }

    private void render(@Nullable SummaryViewModel.SummaryState state) {
        if (state == null) return;
        if (!state.hasSession) {
            textVolume.setText(R.string.summary_no_session);
            return;
        }
        textVolume.setText(getString(R.string.summary_volume_value, state.volume));
        textDuration.setText(getString(R.string.summary_duration_value, state.durationMinutes));
        textSetsCount.setText(String.valueOf(state.setsCount));

        listPr.removeAllViews();
        if (state.personalRecords.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.summary_no_records);
            empty.setTextColor(getResources().getColor(R.color.text_secondary, null));
            listPr.addView(empty);
            return;
        }
        for (SummaryViewModel.PrItem pr : state.personalRecords) {
            TextView item = new TextView(requireContext());
            item.setText(getString(R.string.summary_pr_item, pr.exerciseName, pr.weight));
            item.setPadding(0, 8, 0, 8);
            listPr.addView(item);
        }
    }
}
