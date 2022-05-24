package com.paipeng.checkin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.paipeng.checkin.databinding.FragmentRecordBinding;
import com.paipeng.checkin.restclient.CheckInRestClient;
import com.paipeng.checkin.restclient.base.HttpClientCallback;
import com.paipeng.checkin.restclient.module.Code;
import com.paipeng.checkin.restclient.module.Record;
import com.paipeng.checkin.restclient.module.Task;
import com.paipeng.checkin.ui.RecordArrayAdapter;
import com.paipeng.checkin.ui.TaskArrayAdapter;
import com.paipeng.checkin.utils.CommonUtil;

import java.util.List;

public class RecordFragment extends Fragment {
    private static final String TAG = RecordFragment.class.getSimpleName();

    private FragmentRecordBinding binding;
    private List<Record> records;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentRecordBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
/*
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(RecordFragment.this)
                        .navigate(R.id.action_TaskFragment_to_RecordFragment);
            }
        });

 */
        Task task = ((MainActivity)getActivity()).getSelectedTask();
        if (task != null) {
            getRecords(task);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getRecords(Task task) {
        String token = CommonUtil.getUserToken(getActivity());
        Log.d(TAG, "getTasks: " + token);
        CheckInRestClient checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL, token, new HttpClientCallback<List<Record>>() {
            @Override
            public void onSuccess(List<Record> records) {
                Log.d(TAG, "onSuccess: " + records.size());
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        updateRecordListView(records);
                    }
                });
            }

            @Override
            public void onFailure(int code, String message) {
                Log.e(TAG, "getTicketData error: " + code + " msg: " + message);
            }
        });

        checkInRestClient.queryRecordsByTaskId(task.getId());
    }

    private void updateRecordListView(List<Record> records) {
        this.records = records;
        RecordArrayAdapter recordArrayAdapter = new RecordArrayAdapter(this.getActivity(), R.layout.task_array_adapter, records);

        binding.recordListView.setAdapter(recordArrayAdapter);
        binding.recordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Record record = (Record) parent.getItemAtPosition(position);
                Log.d(TAG, "onItemClick: " + record.getId());
                //switchTaskDetail(task);
            }
        });
    }
}