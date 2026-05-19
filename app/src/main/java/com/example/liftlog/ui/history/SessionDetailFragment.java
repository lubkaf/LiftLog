package com.example.liftlog.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.liftlog.R;
import com.example.liftlog.data.model.WeightDateTuple;
import com.example.liftlog.ui.workout.ActiveSetAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class SessionDetailFragment extends Fragment {

    private SessionDetailViewModel viewModel;
    private LineChart chart;
    private TextView textNoData;
    private Spinner spinner;
    private ArrayAdapter<String> spinnerAdapter;
    private List<SessionDetailViewModel.ExerciseRef> exerciseRefs = new ArrayList<>();
    private LiveData<List<WeightDateTuple>> currentProgress;
    private Observer<List<WeightDateTuple>> currentProgressObserver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SessionDetailViewModel.class);
        int sessionId = getArguments() != null ? getArguments().getInt("sessionId", -1) : -1;

        chart = view.findViewById(R.id.line_chart);
        textNoData = view.findViewById(R.id.text_no_chart_data);
        spinner = view.findViewById(R.id.spinner_exercise);
        RecyclerView recycler = view.findViewById(R.id.recycler_sets);

        spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (position < 0 || position >= exerciseRefs.size()) return;
                viewModel.selectExercise(exerciseRefs.get(position).id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ActiveSetAdapter setsAdapter = new ActiveSetAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(setsAdapter);

        configureChart();

        viewModel.getSets().observe(getViewLifecycleOwner(), setsAdapter::submitList);
        viewModel.getExercisesInSession().observe(getViewLifecycleOwner(), refs -> {
            exerciseRefs = refs == null ? new ArrayList<>() : refs;
            List<String> labels = new ArrayList<>(exerciseRefs.size());
            for (SessionDetailViewModel.ExerciseRef r : exerciseRefs) labels.add(r.name);
            spinnerAdapter.clear();
            spinnerAdapter.addAll(labels);
            spinnerAdapter.notifyDataSetChanged();
        });
        viewModel.getSelectedExerciseId().observe(getViewLifecycleOwner(), this::subscribeToProgress);

        viewModel.load(sessionId);
    }

    private void configureChart() {
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setNoDataText("");
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
    }

    private void subscribeToProgress(@Nullable Integer exerciseId) {
        if (currentProgress != null && currentProgressObserver != null) {
            currentProgress.removeObserver(currentProgressObserver);
            currentProgress = null;
            currentProgressObserver = null;
        }
        if (exerciseId == null) {
            showChart(new ArrayList<>());
            return;
        }
        currentProgress = viewModel.getProgressFor(exerciseId);
        currentProgressObserver = this::showChart;
        currentProgress.observe(getViewLifecycleOwner(), currentProgressObserver);
    }

    private void showChart(@Nullable List<WeightDateTuple> data) {
        if (data == null || data.isEmpty()) {
            chart.setVisibility(View.GONE);
            textNoData.setVisibility(View.VISIBLE);
            return;
        }
        chart.setVisibility(View.VISIBLE);
        textNoData.setVisibility(View.GONE);

        List<Entry> entries = new ArrayList<>(data.size());
        List<String> dateLabels = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            WeightDateTuple t = data.get(i);
            entries.add(new Entry(i, t.weightKg));
            dateLabels.add(formatShortDate(t.date));
        }

        LineDataSet set = new LineDataSet(entries, "kg");
        set.setColor(getResources().getColor(R.color.chart_line, null));
        set.setCircleColor(getResources().getColor(R.color.chart_line, null));
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setValueTextSize(10f);

        chart.setData(new LineData(set));
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        chart.invalidate();
    }

    private static String formatShortDate(String iso) {
        if (iso == null) return "";
        try {
            LocalDateTime dt = LocalDateTime.parse(iso);
            return dt.format(DateTimeFormatter.ofPattern("dd.MM"));
        } catch (DateTimeParseException e) {
            return iso;
        }
    }
}
