package com.paipeng.checkin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.paipeng.checkin.databinding.FragmentSecondBinding;
import com.paipeng.checkin.restclient.module.Task;

import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

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

        binding.taskStartTextDate.setInputType(InputType.TYPE_NULL);
        binding.taskEndTextDate.setInputType(InputType.TYPE_NULL);

        binding.taskStartTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);

                String dateText = binding.taskStartTextDate.getText().toString();
                String[] date = dateText.split("-");
                if (date != null && date.length == 3) {
                    year = Integer.valueOf(date[0]);
                    month = Integer.valueOf(date[1]) -1;
                    day = Integer.valueOf(date[2]);
                }
                // date picker dialog
                DatePickerDialog picker = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                binding.taskStartTextDate.setText(year + "-" +(monthOfYear + 1) + "-" + dayOfMonth);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        binding.taskEndTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);

                String dateText = binding.taskEndTextDate.getText().toString();
                String[] date = dateText.split("-");
                if (date != null && date.length == 3) {
                    year = Integer.valueOf(date[0]);
                    month = Integer.valueOf(date[1]) -1;
                    day = Integer.valueOf(date[2]);
                }
                // date picker dialog
                DatePickerDialog picker = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                binding.taskEndTextDate.setText(year + "-" +(monthOfYear + 1) + "-" + dayOfMonth);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        task = ((MainActivity)getActivity()).getSelectedTask();
        if (task != null) {
            binding.taskNameEditText.setText(task.getName());
            binding.taskDescriptionEditText.setText(task.getDescription());
            binding.taskCompanyNameEditText.setText(task.getCompany().getName());
            binding.taskStateSwitch.setChecked(task.getState()==1);

            //Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            Calendar cal = new GregorianCalendar(TimeZone.getDefault());

            cal.setTimeInMillis(task.getStartTime().getTime());
            String date = DateFormat.format("yyyy-MM-dd", cal).toString();
            binding.taskStartTextDate.setText(date);

            cal.setTimeInMillis(task.getEndTime().getTime());
            date = DateFormat.format("yyyy-MM-dd", cal).toString();
            binding.taskEndTextDate.setText(date);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}