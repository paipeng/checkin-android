package com.paipeng.checkin;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.paipeng.checkin.databinding.FragmentFirstBinding;
import com.paipeng.checkin.restclient.module.Task;
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
            }
        }
    }

    public void showTasks(List<Task> task) {
        Log.d(TAG, "showTasks: " + task.size());
    }
}