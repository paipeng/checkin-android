package com.paipeng.checkin;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcBarcode;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.paipeng.checkin.databinding.ActivityMainBinding;
import com.paipeng.checkin.location.CLocation;
import com.paipeng.checkin.location.GoogleLocationService;
import com.paipeng.checkin.restclient.CheckInRestClient;
import com.paipeng.checkin.restclient.base.HttpClientCallback;
import com.paipeng.checkin.restclient.module.Role;
import com.paipeng.checkin.restclient.module.Task;
import com.paipeng.checkin.restclient.module.User;
import com.paipeng.checkin.ui.TaskArrayAdapter;
import com.paipeng.checkin.utils.CommonUtil;
import com.paipeng.checkin.utils.M1CardUtil;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private NfcAdapter nfcAdapter;
    private Tag tag;
    private FirstFragment firstFragment;

    private CheckInRestClient checkInRestClient;

    private GoogleLocationService googleLocationService;

    private Task selectedTask;
    private List<Task> tasks;

    private ProgressDialog waitScanDialog;

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
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                 */
                showWaitingDialog();
            }
        });
        nfcAdapter = M1CardUtil.isNfcAble(this);
        M1CardUtil.setPendingIntent(PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0));

        getTasks();

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

    }

    private void showWaitingDialog() {
        if (waitScanDialog == null) {
            waitScanDialog = new ProgressDialog(MainActivity.this);
        }
        waitScanDialog.setTitle(getResources().getString(R.string.wait_scan_dialog_title));
        waitScanDialog.setMessage(getResources().getString(R.string.scan_chip));
        waitScanDialog.setIndeterminate(true);
        waitScanDialog.setCancelable(true);
        waitScanDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                waitScanDialog.dismiss();//dismiss dialog
            }
        });

        waitScanDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    googleLocationService = new GoogleLocationService(this, null, 5000, GoogleLocationService.Language.LANGUAGE_ZH);
                    googleLocationService.start();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        NavHostFragment navHostFragment = (NavHostFragment)(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main));
        if (navHostFragment.getChildFragmentManager().getFragments().size() > 0) {
            if (navHostFragment.getChildFragmentManager().getFragments().get(0) != null && navHostFragment.getChildFragmentManager().getFragments().get(0) instanceof FirstFragment) {
                firstFragment = (FirstFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
            }
        }
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


        String[][] mTechLists = new String[][] {
                new String[] {
                    NfcF.class.getName()
                },
                new String [] { MifareClassic.class.getName () },
                new String [] { MifareUltralight.class.getName () },
                new String [] { NfcA.class.getName () },
                new String [] { NfcB.class.getName () },
                new String [] { NfcV.class.getName () },
                //new String [] { IsoDep.class.getName () },
                new String [] { NfcBarcode.class.getName () },
                new String [] { NdefFormatable.class.getName () }
        };

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
        User user = CommonUtil.getUser();
        if (user != null) {
            if (user.getRoles() != null) {
                for (Role role : user.getRoles()) {
                    if ("ADMIN".equals(role.getRole())) {
                        menu.findItem(R.id.action_create_task).setVisible(true);
                        break;
                    }
                }
            }
        } else {
            menu.findItem(R.id.action_logout).setVisible(false);
        }
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
            gotoSettings();
            return true;
        } else if (id == R.id.action_create_task) {
            firstFragment.switchTaskDetail(null);
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
                }
                Log.d(TAG, "NDEF: " + msgs.length);
                byte[] tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                if (firstFragment != null) {
                    firstFragment.showNdefMessage(tagId, msgs);
                } else {
                    Log.d(TAG, "firstFragment is null");
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);

                IsoDep isoDep = IsoDep.get(tag);

                try {
                    String data = M1CardUtil.readIsoCard(tag);
                    Log.d(TAG, "cpu data: " + data);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //byte[] payload = dumpTagData(tag).getBytes();
                //NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);

                //mTags.add(tag);
            }
            // Setup the views
            //buildTagViews(msgs);
        }
    }

    public void getTasks() {
        String token = CommonUtil.getUserToken(this);
        Log.d(TAG, "getTasks: " + token);
        if (token != null) {
            checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL, token, new HttpClientCallback<List<Task>>() {
                @Override
                public void onSuccess(List<Task> tasks) {
                    Log.d(TAG, "onSuccess: " + tasks.size());
                    MainActivity.this.tasks = tasks;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (firstFragment != null) {
                                firstFragment.updateTaskListView(tasks);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(int code, String message) {
                    Log.e(TAG, "getTicketData error: " + code + " msg: " + message);
                }
            });
            checkInRestClient.queryTasks();
        } else {
            Log.e(TAG, "token invalid");
        }
    }

    public CLocation getLocation() {
        return googleLocationService.getLocation();
    }

    private void gotoSettings() {
        Intent intent = new Intent();
        intent.setClass(this, SettingsActivity.class);
        startActivity(intent);
        //finish();
    }

    public Task getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(Task task) {
        this.selectedTask = task;
    }

    public List<Task> getTaskList() {
        return this.tasks;
    }
}