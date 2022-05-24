package com.paipeng.checkin;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.paipeng.checkin.databinding.ActivityMainBinding;
import com.paipeng.checkin.utils.M1CardUtil;

import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NfcAdapter nfcAdapter;
    private NdefMessage mNdefPushMessage;
    private Tag tag;
    private FirstFragment firstFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        nfcAdapter = M1CardUtil.isNfcAble(this);
        M1CardUtil.setPendingIntent(PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0));

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

    }

    @Override
    public void onResume() {
        super.onResume();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }


        String[][] mTechLists = new String[][] { new String[] { NfcF.class.getName() } };

        nfcAdapter.enableForegroundDispatch(this, pendingIntent,
                new IntentFilter[] {
                        ndef,
                },
                mTechLists);
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        setIntent(intent);
        resolveIntent(intent);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                }
                // Process the messages array.
            }
        }
    }


    public String readCPUCardData() throws IOException {
        Log.d(TAG, "readCPUCardData");
        tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            if (M1CardUtil.hasCardType(tag, this, "IsoDep")) {
                return M1CardUtil.readIsoCard(tag);
            } else {
                return "no IsoDep card found!";
            }
        } else {
            return "no IsoDep card found 2!";
        }
    }


    private void resolveIntent(Intent intent) {
        Log.d(TAG, "resolveIntent");
        String action = intent.getAction();
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.d(TAG, "tag: " + tag + " action: " + action);

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    /*
                    NdefRecord ndefRecord = Arrays.stream(((NdefMessage) rawMsgs[i]).getRecords()).findFirst().orElse(null);
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
                        }
                    }
                     */
                }
                Log.d(TAG, "NDEF: " + msgs.length);

                NavHostFragment navHostFragment = (NavHostFragment)(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main));

//                FirstFragment firstFragment = (FirstFragment)navHostFragment.getChildFragmentManager().findFragmentById(R.id.FirstFragment);
                if (navHostFragment.getChildFragmentManager().getFragments().size() > 0) {
                    if (navHostFragment.getChildFragmentManager().getFragments().get(0) != null && navHostFragment.getChildFragmentManager().getFragments().get(0) instanceof FirstFragment) {
                        FirstFragment firstFragment = (FirstFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                        firstFragment.showNdefMessage(msgs);
                    } else {
                        Log.d(TAG, "firstFragment is null");
                    }
                } else {
                    Log.d(TAG, "no fragment in NavHostFragment");
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                if (tag != null) {
                    Log.d(TAG, "tag: " + tag);
                }
                /*
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };

                 */
                //mTags.add(tag);
            }
            // Setup the views
            //buildTagViews(msgs);
        }
    }
}