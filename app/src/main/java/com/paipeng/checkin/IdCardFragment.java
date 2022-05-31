package com.paipeng.checkin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.arcsoft.arcfacedemo.activity.RegisterAndRecognizeActivity;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.paipeng.checkin.databinding.FragmentIdcardBinding;
import com.paipeng.checkin.model.IdCard;
import com.paipeng.checkin.ui.IdCardAdapter;
import com.paipeng.checkin.utils.CommonUtil;
import com.paipeng.checkin.utils.ImageUtil;
import com.paipeng.checkin.utils.NdefUtil;
import com.paipeng.checkin.utils.StringUtil;

import java.util.Arrays;

public class IdCardFragment extends Fragment {
    private static final String TAG = IdCardFragment.class.getSimpleName();

    private FragmentIdcardBinding binding;
    private IdCard idCard;

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult");
                        Intent intent = result.getData();
                        if (intent.getBooleanExtra("FACE_COMPARE", false)) {
                            float score = intent.getFloatExtra("FACE_COMPARE_SCORE", -1f);
                            Log.d(TAG, "face compare success: " + score);

                            Bitmap photo = ((BitmapDrawable)binding.photoImageView.getDrawable()).getBitmap();
                            Bitmap frame = BitmapFactory.decodeResource(getResources(), R.drawable.face_compare_success);
                            Bitmap bitmap = ImageUtil.createSingleImageFromMultipleImages(photo, ImageUtil.resize(frame, photo.getWidth(), photo.getHeight()));


                            binding.photoImageView.setImageBitmap(bitmap);
                        } else {
                            Log.e(TAG, "face compare error");
                        }
                        // Handle the Intent
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        Log.d(TAG, "onActivityResult");
                        //Intent intent = result.getIntent();
                        // Handle the Intent
                    }
                }
            });

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentIdcardBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*
        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(IdCardFragment.this)
                        .navigate(R.id.action_Second2Fragment_to_First2Fragment);
            }
        });

         */

        binding.ocrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ocrIdCard();
            }
        });
        binding.faceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                faceValidate();
            }
        });

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            // handle your code here.
            String value = bundle.getString("key");
            Log.d(TAG, "value: " + value);

            byte[] tagId = bundle.getByteArray("ID");

            Log.d(TAG, "tagId: " + StringUtil.bytesToHexString(tagId));
            NdefMessage[] ndefMessages = (NdefMessage[]) bundle.getParcelableArray("NDEF");
            if (ndefMessages != null) {
                Log.d(TAG, "ndef size: " + ndefMessages.length);
            }
            byte[] data = bundle.getByteArray("DATA");

            showNdefMessage(tagId, ndefMessages, data);

            /*
            for (int i = 0; i < ndefMessages.length; i++) {
                NdefRecord ndefRecord = Arrays.stream(((NdefMessage) ndefMessages[i]).getRecords()).findFirst().orElse(null);
                Log.d(TAG, "NdefMessage: " +  ndefRecord);
                if (ndefRecord != null) {
                    Log.d(TAG, "Msg: " + NdefUtil.parseTextRecord(ndefRecord));
                }
                break;
            }
             */
        } else {
            Log.e(TAG, "bundle invalid");
        }
    }

    private void faceValidate() {
        /*
        Intent intent = new Intent();
        intent.setClass(getActivity(), RegisterAndRecognizeActivity.class);
        startActivity(intent);

         */

        mStartForResult.launch(new Intent(this.getActivity(), RegisterAndRecognizeActivity.class));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void showNdefMessage(byte[] tagId, NdefMessage[] ndefMessages, byte[] data) {
        if (ndefMessages != null) {
            for (int i = 0; i < ndefMessages.length; i++) {
                NdefRecord ndefRecord = Arrays.stream(((NdefMessage) ndefMessages[i]).getRecords()).findFirst().orElse(null);
                Log.d(TAG, "NdefMessage: " + ndefRecord);
                if (ndefRecord != null) {
                    Log.d(TAG, "Msg: " + NdefUtil.parseTextRecord(ndefRecord));
                }
                // 姓名：黛丝斯 部门：业务部 单位：NFC证卡测试 签发日期：2021年10月22日 有效期：2022年12月31日
                IdCard idCard = CommonUtil.convertToIdCard(NdefUtil.parseTextRecord(ndefRecord));
                binding.nameTextView.setText(idCard.getName());
                binding.serialNumberTextView.setText(StringUtil.bytesToHexString(tagId));
                updateIdCardListView(idCard);
                break;
            }
        } else if (data != null) {
            int dataLen = 0;
            for (int i = 0; i < data.length; i++) {
                System.out.print(data[i] + " ");
                if (data[i] == 0) {
                    dataLen = i + 1;
                    break;
                }
            }

            // convert byte[] to bitmap
            Bitmap bitmap = ImageUtil.convertByteToBitmap(data);
            if (bitmap != null) {
                Log.d(TAG, "bitmap size: " + bitmap.getWidth() + "-" + bitmap.getHeight());

                binding.photoImageView.setImageBitmap(bitmap);

                FaceServer.getInstance().init(getActivity());
                FaceServer.getInstance().clearAllFaces(getActivity());
                // register face image from
                Bitmap resizeBitmap = ImageUtil.resize(bitmap, bitmap.getWidth()/4*4, bitmap.getHeight()/4*4);
                Log.d(TAG, "resizeBitmap: " + resizeBitmap.getWidth() + "-" + resizeBitmap.getHeight());

                //binding.photoImageView.setImageBitmap(resizeBitmap);
                FaceServer.getInstance().registerBgr24(getActivity(), ImageUtil.bitmapToRGBByteArray(resizeBitmap), resizeBitmap.getWidth(), resizeBitmap.getHeight(), "ABCD1234");

            } else {
                Log.e(TAG, "bitmap invalid");
            }

            /*
            String text = new String(data, 0, dataLen);
            //String text = new String(data, 0, dataLen, "GB18030");
            Log.d(TAG, "cpu data: " + text);

            IdCard idCard = CommonUtil.convertToIdCard(text);
            binding.nameTextView.setText(idCard.getName());
            binding.serialNumberTextView.setText(StringUtil.bytesToHexString(tagId));
            updateIdCardListView(idCard);

             */
        }
    }

    private void updateIdCardListView(IdCard idCard) {
        this.idCard = idCard;
        // IdCardAdapter
        IdCardAdapter taskArrayAdapter = new IdCardAdapter(this.getActivity(), R.layout.idcard_adapter);

        taskArrayAdapter.setIdCard(idCard);
        binding.idCardListView.setAdapter(taskArrayAdapter);
        binding.idCardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String data = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "onItemClick: " + data);
            }
        });
    }

    private void ocrIdCard() {
        Log.d(TAG, "ocrIdCard");
        ((MainActivity) getActivity()).tryTakePhoto();
    }


    public void ocrImage(Bitmap image) {
        Log.d(TAG, "ocrImage");
        Bundle bundle = new Bundle();
        bundle.putString("key", "abc"); // Put anything what you want

        bundle.putParcelable("IMAGE", image);

        //((MainActivity)getActivity()).setSelectedTask(task);
        NavHostFragment.findNavController(IdCardFragment.this)
                .navigate(R.id.action_IdCardFragment_to_OcrFragment, bundle);

    }

}