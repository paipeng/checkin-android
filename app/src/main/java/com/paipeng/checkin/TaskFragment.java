package com.paipeng.checkin;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.paipeng.checkin.databinding.FragmentTaskBinding;
import com.paipeng.checkin.location.CLocation;
import com.paipeng.checkin.restclient.CheckInRestClient;
import com.paipeng.checkin.restclient.base.HttpClientCallback;
import com.paipeng.checkin.restclient.module.Code;
import com.paipeng.checkin.restclient.module.Record;
import com.paipeng.checkin.restclient.module.Task;
import com.paipeng.checkin.ui.CodeAdapter;
import com.paipeng.checkin.ui.IdCardAdapter;
import com.paipeng.checkin.utils.CommonUtil;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class TaskFragment extends Fragment {
    private static final String TAG = TaskFragment.class.getSimpleName();

    private FragmentTaskBinding binding;
    private Task task;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(TaskFragment.this)
                        .navigate(R.id.action_TaskFragment_to_FirstFragment);
            }
        });
        binding.recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(TaskFragment.this)
                        .navigate(R.id.action_TaskFragment_to_RecordFragment);
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

            getCodesByTask(task);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getCodesByTask(Task task) {
        final Activity activity = this.getActivity();
        String token = CommonUtil.getUserToken(activity);
        Log.d(TAG, "getCodesByTask: " + token);
        if (token != null) {
            CheckInRestClient checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL, token, new HttpClientCallback<List<Code>>() {
                @Override
                public void onSuccess(List<Code> codes) {
                    Log.d(TAG, "onSuccess: " + codes.size());
                    //showWaitDialog(false);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            updateCodeListView(codes);
                        }
                    });
                }

                @Override
                public void onFailure(int code, String message) {
                    Log.e(TAG, "getTicketData error: " + code + " msg: " + message);
                    //showWaitDialog(false);
                }
            });
            checkInRestClient.queryCodesByTaskId(task.getId());
        } else {
            Log.e(TAG, "token invalid");
        }
    }

    private void updateCodeListView(List<Code> codes) {
        CodeAdapter codeAdapter = new CodeAdapter(this.getActivity(), R.layout.code_adapter);

        codeAdapter.setCodes(codes);
        binding.codeListView.setAdapter(codeAdapter);
        binding.codeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String data = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "onItemClick: " + data);
            }
        });
    }

}