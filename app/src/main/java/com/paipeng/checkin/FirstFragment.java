package com.paipeng.checkin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.paipeng.checkin.databinding.FragmentFirstBinding;
import com.paipeng.checkin.location.CLocation;
import com.paipeng.checkin.restclient.CheckInRestClient;
import com.paipeng.checkin.restclient.base.HttpClientCallback;
import com.paipeng.checkin.restclient.module.Code;
import com.paipeng.checkin.restclient.module.Record;
import com.paipeng.checkin.restclient.module.Task;
import com.paipeng.checkin.ui.TaskArrayAdapter;
import com.paipeng.checkin.utils.CommonUtil;
import com.paipeng.checkin.utils.StringUtil;

import java.math.BigDecimal;
import java.util.List;

public class FirstFragment extends BaseFragment {
    private static final String TAG = FirstFragment.class.getSimpleName();

    private FragmentFirstBinding binding;
    private TextView textView;
    private boolean loading;
    private ProgressDialog progressDialog;

    private List<Task> tasks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchTaskDetail(null);
            }
        });
        //binding.buttonFirst.setVisibility(View.GONE);

        if (this.tasks == null) {
            this.tasks = ((MainActivity) getActivity()).getTaskList();
        }
        if (this.tasks != null) {
            updateTaskListView(this.tasks);
        } else {
            Log.e(TAG, "task invalid: null");
        }

        //switchIdCard(null, null, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void showNdefMessage(byte[] tagId, NdefMessage[] ndefMessages, byte[] textData, byte[] data) {
        Log.d(TAG, "showNdefMessage  tagId: " + StringUtil.bytesToHexString(tagId));
        /*
        for (int i = 0; i < ndefMessages.length; i++) {
            NdefRecord ndefRecord = Arrays.stream(((NdefMessage) ndefMessages[i]).getRecords()).findFirst().orElse(null);
            Log.d(TAG, "NdefMessage: " +  ndefRecord);
            if (ndefRecord != null) {
                binding.textviewFirst.setText(NdefUtil.parseTextRecord(ndefRecord));
                if (!loading) {
                    getCode(StringUtil.bytesToHexString(tagId));
                }
            }
        }*/

        switchIdCard(tagId, ndefMessages, textData, data);
    }

    public void updateTaskListView(List<Task> tasks) {
        this.tasks = tasks;
        TaskArrayAdapter taskArrayAdapter = new TaskArrayAdapter(this.getActivity(), R.layout.task_array_adapter, tasks);

        binding.taskListView.setAdapter(taskArrayAdapter);
        binding.taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Task task = (Task) parent.getItemAtPosition(position);
                Log.d(TAG, "onItemClick: " + task.getName());
                switchTaskDetail(task);
            }
        });
    }

    private void getCode(String serialNumber) {
        showWaitDialog(true);

        final Activity activity = this.getActivity();
        String token = CommonUtil.getUserToken(activity);
        Log.d(TAG, "getCode: " + serialNumber);
        if (token != null) {
            CheckInRestClient checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL, token, new HttpClientCallback<Code>() {
                @Override
                public void onSuccess(Code code) {
                    Log.d(TAG, "onSuccess: " + code.getName());

                    addRecord(code);
                }

                @Override
                public void onFailure(int code, String message) {
                    Log.e(TAG, "getTicketData error: " + code + " msg: " + message);
                    showWaitDialog(false);

                }
            });
            checkInRestClient.queryCodeBySerialNumber(serialNumber);
        } else {
            Log.e(TAG, "token invalid");
        }
    }

    private void showWaitDialog(boolean loading) {
        this.loading = loading;
        if (loading) {
            progressDialog = ProgressDialog.show(this.getActivity(), "",
                    "Loading. Please wait...", true);
            progressDialog.show();
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.cancel();
            }

        }
    }

    private void addRecord(Code code) {
        final Activity activity = this.getActivity();
        String token = CommonUtil.getUserToken(activity);
        Log.d(TAG, "addRecord: " + token);
        if (token != null) {
            CheckInRestClient checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL, token, new HttpClientCallback<Record>() {
                @Override
                public void onSuccess(Record record) {
                    Log.d(TAG, "onSuccess: " + record.getId());
                    showWaitDialog(false);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, "record logged: " + record.getId(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onFailure(int code, String message) {
                    Log.e(TAG, "getTicketData error: " + code + " msg: " + message);
                    showWaitDialog(false);
                }
            });

            Record record = new Record();
            record.setCode(code);
            CLocation location = ((MainActivity) activity).getLocation();
            Log.d(TAG, "record latlng: " + location.getLatitude() + "-" + location.getLongitude());
            record.setLatitude(BigDecimal.valueOf(location.getLatitude()));
            record.setLongitude(BigDecimal.valueOf(location.getLongitude()));
            Log.d(TAG, "record latlng: " + record.getLatitude() + "-" + record.getLongitude());

            record.setUser(CommonUtil.getUser());
            checkInRestClient.saveRecord(record);
        } else {
            Log.e(TAG, "token invalid");
        }
    }

    public void switchTaskDetail(Task task) {
        /*
        ((MainActivity)getActivity()).setSelectedTask(task);
        NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment);
         */
        //mStartForResult.launch(new Intent(this.getActivity(), BarcodeCameraActivity.class));
        //mStartForResult.launch(new Intent(this.getActivity(), FaceCameraActivity.class));

        //Intent intent = new Intent(this.getActivity(), IdCardCameraActivity.class);
        Intent intent = new Intent(this.getActivity(), FaceCameraActivity.class);

        intent.putExtra("key", "firstFragment"); // Put anything what you want
        mStartForResult.launch(intent);
    }

    public void switchIdCard(byte[] tagId, NdefMessage[] ndefMessages, byte[] textData, byte[] data) {
        Bundle bundle = new Bundle();
        bundle.putString("key", "abc"); // Put anything what you want
        bundle.putByteArray("ID", tagId);
        bundle.putParcelableArray("NDEF", ndefMessages);
        bundle.putByteArray("DATA", data);
        bundle.putByteArray("TEXTDATA", textData);

        NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_IdCardFragment, bundle);
    }

    @Override
    protected void returnActiveResult(ActivityResult result) {
        Log.d(TAG, "returnActiveResult");

        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent intent = result.getData();

            String barcodeText = intent.getStringExtra("BARCODE_TEXT");
            if (barcodeText != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), "Barcode: " + barcodeText, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}