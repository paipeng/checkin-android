package com.paipeng.checkin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.paipeng.checkin.databinding.FragmentSecondBinding;
import com.paipeng.checkin.restclient.module.Task;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private Task task;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        task = ((MainActivity)getActivity()).getSelectedTask();
        if (task != null) {
            binding.taskNameEditText.setText(task.getName());
            binding.taskDescriptionEditText.setText(task.getDescription());
            binding.taskCompanyNameEditText.setText(task.getCompany().getName());
            binding.taskStateSwitch.setChecked(task.getState()==1);
            //binding.taskStartDatePicker.set
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}