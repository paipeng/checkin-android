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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

    public void showNdefMessage(NdefMessage[] ndefMessages) {
        Log.d(TAG, "showNdefMessage size: " + ndefMessages.length);
        for (int i = 0; i < ndefMessages.length; i++) {
            NdefRecord ndefRecord = Arrays.stream(((NdefMessage) ndefMessages[i]).getRecords()).findFirst().orElse(null);
            Log.d(TAG, "NdefMessage: " +  ndefRecord);
            if (ndefRecord != null) {
                if (ndefRecord.getType()[0] == 0x54) {
                    Log.d(TAG, "ndef type text");
                    int offset = 3;
                    // byte 0: the length of language code
                    // byte 1: language code byte 1: 'z' 0x7A
                    // byte 2: language code byte 2: 'h' 0x68
                    String text = new String(ndefRecord.getPayload(), offset, ndefRecord.getPayload().length - offset, StandardCharsets.UTF_8);

                    Log.d(TAG, "text: " + text);

                    binding.textviewFirst.setText(text);
                }
            }
        }
    }

}