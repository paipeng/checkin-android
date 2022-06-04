package com.paipeng.checkin;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.paipeng.checkin.databinding.FragmentCodeBinding;
import com.paipeng.checkin.location.CLocation;
import com.paipeng.checkin.restclient.CheckInRestClient;
import com.paipeng.checkin.restclient.base.HttpClientCallback;
import com.paipeng.checkin.restclient.module.Code;
import com.paipeng.checkin.restclient.module.Record;
import com.paipeng.checkin.restclient.module.Task;
import com.paipeng.checkin.utils.CommonUtil;

import java.math.BigDecimal;

public class CodeFragment extends Fragment {
    private final static String TAG = CodeFragment.class.getSimpleName();

    private FragmentCodeBinding binding;
    private Task task;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentCodeBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCode();
            }
        });


        Bundle bundle = this.getArguments();

        if (bundle != null) {
            // handle your code here.
            String value = bundle.getString("key");
            Log.d(TAG, "value: " + value);

            this.task = (Task) bundle.getSerializable("TASK");
        }
    }

    private Code validateCode() {
        Code code = new Code();

        Log.d(TAG, "saveCode name: " + binding.nameEditText.getText().toString());
        if(TextUtils.isEmpty(binding.nameEditText.getText().toString())) {
            binding.nameEditText.setError(getActivity().getResources().getString (R.string.name_should_not_empty));
            return null;
        }
        code.setName(binding.nameEditText.getText().toString());
        code.setSerialNumber(binding.serialNumberEditText.getText().toString());
        if(!TextUtils.isEmpty(binding.latitudeEditText.getText().toString())) {
            code.setLatitude(BigDecimal.valueOf(Double.valueOf(binding.latitudeEditText.getText().toString())));
        }
        if(!TextUtils.isEmpty(binding.longitudeEditText.getText().toString())) {
            code.setLongitude(BigDecimal.valueOf(Double.valueOf(binding.longitudeEditText.getText().toString())));
        }
        code.setState(binding.stateSwitch.isChecked()?1:0);

        if (binding.typeRadioGroup.getCheckedRadioButtonId() == R.id.nfcRadioButton) {
            code.setType(1);
        } else {
            code.setType(0);
        }

        code.setTask(task);
        return code;
    }

    private void saveCode() {
        Code code = validateCode();
        if (code == null) {
            return;
        }

        final Activity activity = this.getActivity();
        String token = CommonUtil.getUserToken(activity);
        Log.d(TAG, "saveCode: " + token);
        if (token != null) {
            CheckInRestClient checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL, token, new HttpClientCallback<Code>() {
                @Override
                public void onSuccess(Code code) {
                    Log.d(TAG, "onSuccess: " + code.getId());

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, "new code saved: " + code.getId(), Toast.LENGTH_LONG).show();

                            NavHostFragment.findNavController(CodeFragment.this)
                                    .navigate(R.id.action_CodeFragment_to_TaskFragment);
                        }
                    });
                }

                @Override
                public void onFailure(int code, String message) {
                    Log.e(TAG, "getTicketData error: " + code + " msg: " + message);

                }
            });
            checkInRestClient.saveCode(code);
        } else {
            Log.e(TAG, "token invalid");
        }



    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}