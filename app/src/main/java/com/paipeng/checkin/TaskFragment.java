package com.paipeng.checkin;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.fragment.NavHostFragment;

import com.paipeng.checkin.databinding.FragmentTaskBinding;
import com.paipeng.checkin.restclient.CheckInRestClient;
import com.paipeng.checkin.restclient.base.HttpClientCallback;
import com.paipeng.checkin.restclient.module.Code;
import com.paipeng.checkin.restclient.module.Task;
import com.paipeng.checkin.restclient.module.User;
import com.paipeng.checkin.ui.CodeAdapter;
import com.paipeng.checkin.utils.CommonUtil;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class TaskFragment extends BaseFragment {
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

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (task == null || task.getId() == 0) {
                    saveTask();
                } else {
                    updateTask();
                }
            }
        });

        binding.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTask();
            }
        });
        binding.recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(TaskFragment.this)
                        .navigate(R.id.action_TaskFragment_to_RecordFragment);
            }
        });


        binding.startTimeEditText.setInputType(InputType.TYPE_NULL);
        binding.endTimeEditText.setInputType(InputType.TYPE_NULL);

        binding.startTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);

                String dateText = binding.startTimeEditText.getText().toString();
                String[] date = dateText.split("-");
                if (date != null && date.length == 3) {
                    year = Integer.valueOf(date[0]);
                    month = Integer.valueOf(date[1]) - 1;
                    day = Integer.valueOf(date[2]);
                }
                // date picker dialog
                DatePickerDialog picker = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                binding.startTimeEditText.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        binding.endTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);

                String dateText = binding.endTimeEditText.getText().toString();
                String[] date = dateText.split("-");
                if (date != null && date.length == 3) {
                    year = Integer.valueOf(date[0]);
                    month = Integer.valueOf(date[1]) - 1;
                    day = Integer.valueOf(date[2]);
                }
                // date picker dialog
                DatePickerDialog picker = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                binding.endTimeEditText.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        task = ((MainActivity) getActivity()).getSelectedTask();
        if (task != null) {
            binding.nameEditText.setText(task.getName());
            binding.descriptionEditText.setText(task.getDescription());
            binding.companyNameEditText.setText(task.getCompany().getName());
            binding.stateSwitch.setChecked(task.getState() == 1);

            //Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            Calendar cal = new GregorianCalendar(TimeZone.getDefault());

            if (task.getStartTime() != null) {
                cal.setTimeInMillis(task.getStartTime().getTime());
                String date = DateFormat.format("yyyy-MM-dd", cal).toString();
                binding.startTimeEditText.setText(date);
            }
            if (task.getEndTime() != null) {
                cal.setTimeInMillis(task.getEndTime().getTime());
                String date = DateFormat.format("yyyy-MM-dd", cal).toString();
                binding.endTimeEditText.setText(date);
            }
            getCodesByTask(task);

            binding.deleteButton.setEnabled(true);
        } else {
            binding.deleteButton.setEnabled(false);
        }
    }

    private void deleteTask() {
        Log.d(TAG, "deleteTask");
        new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getResources().getString(R.string.dialog_title_confirm_delete))
                .setMessage(getActivity().getResources().getString(R.string.dialog_confirm_delete_task))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getActivity(), "Yes", Toast.LENGTH_SHORT).show();

                        final Activity activity = getActivity();
                        String token = CommonUtil.getUserToken(activity);
                        Log.d(TAG, "deleteCode: " + token);
                        if (token == null) {
                            Log.e(TAG, "token invalid");
                            return;
                        }
                        CheckInRestClient checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL, token, new HttpClientCallback<String>() {
                            @Override
                            public void onSuccess(String response) {
                                Log.d(TAG, "onSuccess: " + response);

                                activity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(activity, "task deleted: " + task.getId(), Toast.LENGTH_LONG).show();
                                        taskResult(2, task);
                                        NavHostFragment.findNavController(TaskFragment.this).navigateUp();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(int code, String message) {
                                Log.e(TAG, "deleteTask error: " + code + " msg: " + message);
                            }
                        });
                        if (task.getId() > 0) {
                            checkInRestClient.deleteTask(task);
                        }
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private Task validateTask() {

        if (task == null) {
            task = new Task();
            //set company via user
            User user = CommonUtil.getUser();
            if (user != null && user.getCompany() != null) {
                task.setCompany(user.getCompany());
            } else {
                return null;
            }
        }

        Log.d(TAG, "validateTask name: " + binding.nameEditText.getText().toString());
        if (TextUtils.isEmpty(binding.nameEditText.getText().toString())) {
            binding.nameEditText.setError(getActivity().getResources().getString(R.string.name_should_not_empty));
            return null;
        }
        task.setName(binding.nameEditText.getText().toString());
        task.setDescription(binding.descriptionEditText.getText().toString());
        task.setState(binding.stateSwitch.isChecked()?1:0);


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = (Date)formatter.parse(binding.startTimeEditText.getText().toString());
            Timestamp timestamp = new java.sql.Timestamp(date.getTime());
            task.setStartTime(timestamp);

            date = (Date)formatter.parse(binding.endTimeEditText.getText().toString());
            timestamp = new java.sql.Timestamp(date.getTime());
            task.setEndTime(timestamp);

        } catch (ParseException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        return task;
    }



    private void saveTask() {
        Log.d(TAG, "saveTask");
        Task task = validateTask();
        if (task == null) {
            return;
        }

        final Activity activity = this.getActivity();
        String token = CommonUtil.getUserToken(activity);
        Log.d(TAG, "saveTask: " + token);
        if (token == null) {
            Log.e(TAG, "token invalid");
            return;
        }
        CheckInRestClient checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL, token, new HttpClientCallback<Task>() {
            @Override
            public void onSuccess(Task task) {
                Log.d(TAG, "onSuccess: " + task.getId());

                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activity, "new task saved: " + task.getId(), Toast.LENGTH_LONG).show();
                        taskResult(0, task);
                        NavHostFragment.findNavController(TaskFragment.this).navigateUp();
//                                    .navigate(R.id.action_CodeFragment_to_TaskFragment);
                    }
                });
            }

            @Override
            public void onFailure(int code, String message) {
                Log.e(TAG, "getTicketData error: " + code + " msg: " + message);

            }
        });
        checkInRestClient.saveTask(task);
    }


    private void updateTask() {
        Log.d(TAG, "updateTask");
        Task task = validateTask();
        if (task == null) {
            return;
        }

        final Activity activity = this.getActivity();
        String token = CommonUtil.getUserToken(activity);
        Log.d(TAG, "updateTask: " + token);
        if (token == null) {
            Log.e(TAG, "token invalid");
            return;
        }
        CheckInRestClient checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL, token, new HttpClientCallback<Task>() {
            @Override
            public void onSuccess(Task task) {
                Log.d(TAG, "onSuccess: " + task.getId());

                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activity, "task updated: " + task.getId(), Toast.LENGTH_LONG).show();
                        taskResult(1, task);
                        NavHostFragment.findNavController(TaskFragment.this).navigateUp();
//                                    .navigate(R.id.action_CodeFragment_to_TaskFragment);
                    }
                });
            }

            @Override
            public void onFailure(int code, String message) {
                Log.e(TAG, "getTicketData error: " + code + " msg: " + message);

            }
        });
        checkInRestClient.updateTask(task);
    }

    /**
     *
     * @param state: 0 save; 1 update; 2 delete
     * @task Task
     */
    private void taskResult(int state, Task task) {
        Log.d(TAG, "taskResult: " + state);
        Bundle result = new Bundle();
        result.putInt("state", state);
        result.putSerializable("task", task);
        getParentFragmentManager().setFragmentResult("TASK_RESULT", result);
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
                final Code code = (Code) parent.getItemAtPosition(position);
                Log.d(TAG, "onItemClick: " + code);
                switchCodeFragment(code);
            }
        });
    }


    @Override
    protected void returnActiveResult(ActivityResult result) {

    }

    public void switchCodeFragment(Code code) {
        Bundle bundle = new Bundle();
        bundle.putString("key", "abc"); // Put anything what you want
        bundle.putSerializable("TASK", task);
        bundle.putSerializable("CODE", code);

        ((MainActivity) getActivity()).setSelectedTask(task);
        NavHostFragment.findNavController(TaskFragment.this)
                .navigate(R.id.action_TaskFragment_to_CodeFragment, bundle);
    }
}