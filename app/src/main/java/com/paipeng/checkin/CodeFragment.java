package com.paipeng.checkin;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.paipeng.checkin.databinding.FragmentCodeBinding;
import com.paipeng.checkin.location.CLocation;
import com.paipeng.checkin.restclient.CheckInRestClient;
import com.paipeng.checkin.restclient.base.HttpClientCallback;
import com.paipeng.checkin.restclient.module.Code;
import com.paipeng.checkin.restclient.module.Task;
import com.paipeng.checkin.utils.BarcodeUtil;
import com.paipeng.checkin.utils.CommonUtil;

import java.math.BigDecimal;

public class CodeFragment extends Fragment {
    private final static String TAG = CodeFragment.class.getSimpleName();

    private FragmentCodeBinding binding;
    private Task task;
    private Code code;

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

        binding.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCode();
            }
        });

        binding.randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serialNumber = CommonUtil.getInstance().generateRandomSerialNumber();
                Log.d(TAG, "generateRandomSerialNumber: " + serialNumber);
                binding.serialNumberEditText.setText(serialNumber);
            }
        });
        binding.currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CLocation location = ((MainActivity) getActivity()).getLocation();
                Log.d(TAG, "currentLocation latlng: " + location.getLatitude() + "-" + location.getLongitude());

                binding.latitudeEditText.setText(String.valueOf(location.getLatitude()));
                binding.longitudeEditText.setText(String.valueOf(location.getLongitude()));
            }
        });
        binding.showBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBarcodeDialog();
            }
        });
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            // handle your code here.
            String value = bundle.getString("key");
            Log.d(TAG, "value: " + value);

            this.task = (Task) bundle.getSerializable("TASK");

            this.code = (Code) bundle.getSerializable("CODE");
            if (this.code != null) {
                initCode();
                binding.deleteButton.setEnabled(true);
            } else {
                binding.deleteButton.setEnabled(false);
            }
        }
    }

    private void showBarcodeDialog() {
        Log.d(TAG, "showBarcodeDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Yes Button
        builder.setPositiveButton(getString(com.arcsoft.arcfacedemo.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getActivity(), "Yes button Clicked", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Yes button Clicked!");
                dialog.dismiss();
            }
        });

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.dialog_showbarcode, null);

        ImageView barcodeImageView = (ImageView) dialoglayout.findViewById(R.id.barcodeImageView);
        // gen barcode (qrcode)
        Bitmap bitmap = BarcodeUtil.generateBarcode(code.getSerialNumber());
        barcodeImageView.setImageBitmap(bitmap);

        builder.setView(dialoglayout);
        builder.show();
    }

    private void initCode() {
        Log.d(TAG, "initCode");

        binding.nameEditText.setText(code.getName());
        binding.serialNumberEditText.setText(code.getSerialNumber());

        if (code.getLatitude() != null) {
            binding.latitudeEditText.setText(code.getLatitude().toString());
        }
        if (code.getLongitude() != null) {
            binding.longitudeEditText.setText(code.getLongitude().toString());
        }
        binding.stateSwitch.setChecked(code.getState()==1);
        //

        if (code.getType() == 1) {
            binding.typeRadioGroup.check(R.id.nfcRadioButton);
        } else {
            binding.typeRadioGroup.check(R.id.barcodeRadioButton);
        }

        binding.distanceEditText.setText(String.valueOf(code.getDistance()));
    }

    private Code validateCode() {
        if (code == null) {
            code = new Code();
        }

        Log.d(TAG, "saveCode name: " + binding.nameEditText.getText().toString());
        if (TextUtils.isEmpty(binding.nameEditText.getText().toString())) {
            binding.nameEditText.setError(getActivity().getResources().getString(R.string.name_should_not_empty));
            return null;
        }
        code.setName(binding.nameEditText.getText().toString());
        code.setSerialNumber(binding.serialNumberEditText.getText().toString());
        if (!TextUtils.isEmpty(binding.latitudeEditText.getText().toString())) {
            code.setLatitude(BigDecimal.valueOf(Double.valueOf(binding.latitudeEditText.getText().toString())));
        }
        if (!TextUtils.isEmpty(binding.longitudeEditText.getText().toString())) {
            code.setLongitude(BigDecimal.valueOf(Double.valueOf(binding.longitudeEditText.getText().toString())));
        }
        code.setState(binding.stateSwitch.isChecked() ? 1 : 0);

        code.setDistance(Integer.valueOf(binding.distanceEditText.getText().toString()).intValue());
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

                            NavHostFragment.findNavController(CodeFragment.this).navigateUp();
//                                    .navigate(R.id.action_CodeFragment_to_TaskFragment);
                        }
                    });
                }

                @Override
                public void onFailure(int code, String message) {
                    Log.e(TAG, "getTicketData error: " + code + " msg: " + message);

                }
            });
            if (code.getId() > 0) {
                checkInRestClient.updateCode(code);
            } else {
                checkInRestClient.saveCode(code);
            }
        } else {
            Log.e(TAG, "token invalid");
        }
    }


    private void deleteCode() {
        if (this.code != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getActivity().getResources().getString(R.string.dialog_title_confirm_delete))
                    .setMessage(getActivity().getResources().getString(R.string.dialog_confirm_delete_code))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            Toast.makeText(getActivity(), "Yes", Toast.LENGTH_SHORT).show();

                            final Activity activity = getActivity();
                            String token = CommonUtil.getUserToken(activity);
                            Log.d(TAG, "deleteCode: " + token);
                            if (token != null) {
                                CheckInRestClient checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL, token, new HttpClientCallback<String>() {
                                    @Override
                                    public void onSuccess(String response) {
                                        Log.d(TAG, "onSuccess: " + response);

                                        activity.runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(activity, "code deleted: " + code.getId(), Toast.LENGTH_LONG).show();
                                                NavHostFragment.findNavController(CodeFragment.this).navigateUp();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(int code, String message) {
                                        Log.e(TAG, "getTicketData error: " + code + " msg: " + message);

                                    }
                                });
                                if (code.getId() > 0) {
                                    checkInRestClient.deleteCode(code);
                                }
                            } else {
                                Log.e(TAG, "token invalid");
                            }
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}