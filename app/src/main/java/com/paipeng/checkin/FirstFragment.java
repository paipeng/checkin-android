package com.paipeng.checkin;

import android.app.Activity;
import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.paipeng.checkin.databinding.FragmentFirstBinding;
import com.paipeng.checkin.restclient.CheckInRestClient;
import com.paipeng.checkin.restclient.base.HttpClientCallback;
import com.paipeng.checkin.restclient.module.Code;
import com.paipeng.checkin.restclient.module.Task;
import com.paipeng.checkin.ui.TaskArrayAdapter;
import com.paipeng.checkin.utils.CommonUtil;
import com.paipeng.checkin.utils.NdefUtil;
import com.paipeng.checkin.utils.StringUtil;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class FirstFragment extends Fragment {
    private static final String TAG = FirstFragment.class.getSimpleName();

    private FragmentFirstBinding binding;
    private TextView textView;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void showNdefMessage(byte[] tagId, NdefMessage[] ndefMessages) {
        Log.d(TAG, "showNdefMessage size: " + ndefMessages.length + " tagId: " + StringUtil.bytesToHexString(tagId));
        for (int i = 0; i < ndefMessages.length; i++) {
            NdefRecord ndefRecord = Arrays.stream(((NdefMessage) ndefMessages[i]).getRecords()).findFirst().orElse(null);
            Log.d(TAG, "NdefMessage: " +  ndefRecord);
            if (ndefRecord != null) {
                binding.textviewFirst.setText(NdefUtil.parseTextRecord(ndefRecord));
                getCode(StringUtil.bytesToHexString(tagId));
            }
        }
    }

    public void updateTaskListView(List<Task> tasks) {
        TaskArrayAdapter taskArrayAdapter = new TaskArrayAdapter(this.getActivity(), R.layout.task_array_adapter, tasks);

        binding.taskListView.setAdapter(taskArrayAdapter);
        binding.taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Task task = (Task) parent.getItemAtPosition(position);
                Log.d(TAG, "onItemClick: " + task.getName());
            }
        });
    }

    private void getCode(String serialNumber) {
        final Activity activity = this.getActivity();
        String token = CommonUtil.getUserToken(activity);
        Log.d(TAG, "getTasks: " + token);
        if (token != null) {
            CheckInRestClient checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL, token, new HttpClientCallback<Code>() {
                @Override
                public void onSuccess(Code code) {
                    Log.d(TAG, "onSuccess: " + code.getName());
                    activity.runOnUiThread(new Runnable() {
                        public void run() {

                        }
                    });
                }

                @Override
                public void onFailure(int code, String message) {
                    Log.e(TAG, "getTicketData error: " + code + " msg: " + message);
                }
            });
            checkInRestClient.queryCodeBySerialNumber(serialNumber);
        } else {
            Log.e(TAG, "token invalid");
        }
    }
}