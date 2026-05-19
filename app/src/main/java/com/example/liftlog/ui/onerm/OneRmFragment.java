package com.example.liftlog.ui.onerm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.liftlog.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;

public class OneRmFragment extends Fragment {

    private OneRmViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_one_rm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(OneRmViewModel.class);

        TextInputEditText editWeight = view.findViewById(R.id.edit_weight);
        TextInputEditText editReps = view.findViewById(R.id.edit_reps);
        Spinner spinner = view.findViewById(R.id.spinner_formula);
        MaterialButton btnCalc = view.findViewById(R.id.btn_calculate);
        TextView textResult = view.findViewById(R.id.text_result);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList(
                        getString(R.string.onerm_brzycki),
                        getString(R.string.onerm_epley),
                        getString(R.string.onerm_lander)));
        spinner.setAdapter(spinnerAdapter);

        btnCalc.setOnClickListener(v -> {
            Float weight = parseFloatSafe(editWeight);
            Integer reps = parseIntSafe(editReps);
            OneRmCalculator.Formula formula = formulaFor(spinner.getSelectedItemPosition());
            if (!viewModel.compute(formula, weight, reps)) {
                Toast.makeText(requireContext(), R.string.onerm_validation_error,
                        Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) {
                textResult.setText(R.string.onerm_result_placeholder);
            } else {
                textResult.setText(getString(R.string.onerm_result_format, result));
            }
        });
    }

    private OneRmCalculator.Formula formulaFor(int position) {
        switch (position) {
            case 1: return OneRmCalculator.Formula.EPLEY;
            case 2: return OneRmCalculator.Formula.LANDER;
            default: return OneRmCalculator.Formula.BRZYCKI;
        }
    }

    @Nullable
    private static Float parseFloatSafe(TextInputEditText edit) {
        String s = edit.getText() == null ? "" : edit.getText().toString();
        if (s.isEmpty()) return null;
        try {
            return Float.parseFloat(s.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    private static Integer parseIntSafe(TextInputEditText edit) {
        String s = edit.getText() == null ? "" : edit.getText().toString();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
