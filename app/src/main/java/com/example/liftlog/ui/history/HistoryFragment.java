package com.example.liftlog.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.liftlog.R;

public class HistoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HistoryViewModel viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        TextView empty = view.findViewById(R.id.text_empty);
        RecyclerView recycler = view.findViewById(R.id.recycler_history);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        HistoryAdapter adapter = new HistoryAdapter(item -> {
            Bundle args = new Bundle();
            args.putInt("sessionId", item.sessionId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_history_to_sessionDetail, args);
        });
        recycler.setAdapter(adapter);

        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            empty.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
