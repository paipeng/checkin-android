package com.paipeng.checkin;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.nfc.NdefMessage;
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
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
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
import com.paipeng.checkin.restclient.module.Task;
import com.paipeng.checkin.restclient.module.User;
import com.paipeng.checkin.utils.CommonUtil;
import com.paipeng.checkin.utils.ImageUtil;
import com.paipeng.checkin.utils.M1CardUtil;
import com.paipeng.checkin.utils.NfcCpuUtil;
import com.paipeng.checkin.utils.SM4Util;
import com.paipeng.checkin.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    private String currentPhotoPath;
    public static final int OPEN_GALLERY_REQUEST_CODE = 0;
    public static final int TAKE_PHOTO_REQUEST_CODE = 1;



    public static final int PERMISSION_LOCATION = 1;
    public static final int PERMISSION_CAMERA = 2;

    private Bitmap cur_predict_image = null;

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

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);

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
                stopNFCListener();
            }
        });

        startNFCListener();
        waitScanDialog.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    googleLocationService = new GoogleLocationService(this, null, 5000, GoogleLocationService.Language.LANGUAGE_ZH);
                    googleLocationService.start();
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                return;
            }
            case PERMISSION_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
            }
            // other 'case' lines to check for other permissions this app might request
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        NavHostFragment navHostFragment = (NavHostFragment) (getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main));
        if (navHostFragment.getChildFragmentManager().getFragments().size() > 0) {
            if (navHostFragment.getChildFragmentManager().getFragments().get(0) != null && navHostFragment.getChildFragmentManager().getFragments().get(0) instanceof FirstFragment) {
                firstFragment = (FirstFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startNFCListener();
    }

    private void startNFCListener() {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches. You should specify only the ones that you need. */
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        String[][] mTechLists = new String[][]{
                new String[]{
                        NfcF.class.getName()
                },
                new String[]{MifareClassic.class.getName()},
                new String[]{MifareUltralight.class.getName()},
                new String[]{NfcA.class.getName()},
                new String[]{NfcB.class.getName()},
                new String[]{NfcV.class.getName()},
                //new String [] { IsoDep.class.getName () },
                new String[]{NfcBarcode.class.getName()},
                new String[]{NdefFormatable.class.getName()}
        };

        nfcAdapter.enableForegroundDispatch(this, pendingIntent,
                new IntentFilter[]{
                        ndef,
                },
                mTechLists);
    }

    private void stopNFCListener() {
        runOnUiThread(new Runnable() {
            public void run() {
                nfcAdapter.disableReaderMode(MainActivity.this);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (CommonUtil.isAdmin()) {
            menu.findItem(R.id.action_create_task).setVisible(true);
            NavHostFragment navHostFragment = (NavHostFragment) (getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main));
            if (navHostFragment.getChildFragmentManager().getFragments().size() > 0) {
                Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
                if (fragment != null && fragment instanceof TaskFragment) {
                    menu.findItem(R.id.action_create_code).setVisible(true);
                    menu.findItem(R.id.action_create_task).setVisible(false);
                }
            }
        } else {
            User user = CommonUtil.getUser();
            if (user != null) {
                menu.findItem(R.id.action_create_task).setVisible(false);
                menu.findItem(R.id.action_create_code).setVisible(false);
            } else {
                menu.findItem(R.id.action_logout).setVisible(false);
            }
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
        } else if (id == R.id.action_create_code) {
            NavHostFragment navHostFragment = (NavHostFragment) (getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main));
            if (navHostFragment.getChildFragmentManager().getFragments().size() > 0) {
                if (navHostFragment.getChildFragmentManager().getFragments().get(0) != null && navHostFragment.getChildFragmentManager().getFragments().get(0) instanceof TaskFragment) {
                    TaskFragment taskFragment = (TaskFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                    taskFragment.switchCodeFragment(null);
                }
            }
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

        if (waitScanDialog != null && waitScanDialog.isShowing()) {
            waitScanDialog.dismiss();//dismiss dialog
            stopNFCListener();
        }
    }

    public void readCPUCardData(byte[] id, IsoDep isoDep) throws Exception {
        Log.d(TAG, "readCPUCardData");

        NfcCpuUtil nfcCpuUtil = new NfcCpuUtil(isoDep);
        //String data = M1CardUtil.readIsoCard(tag);
        //Log.d(TAG, "cpu data: " + data);

        byte[] text_data = nfcCpuUtil.readFileData((short)1);
        int dataLen = 0;
        for (int i = 0; i < text_data.length; i++) {
            System.out.print(text_data[i] + " ");
            if (text_data[i] == 0) {
                dataLen = i+1;
                break;
            }
        }
        String text = new String(text_data);
        //String text = new String(data, 0, dataLen, "GB18030");
        Log.d(TAG, "cpu data: " + text);
        //return text;


        byte[] data = nfcCpuUtil.readFileData((short)2);

        // SM4 decode
        byte[] decoded_data = SM4Util.decrypt_Ecb(SM4Util.DEFAULT_KEY, data);
        Log.d(TAG, "decoded_data: " + decoded_data.length);

        for (int i = decoded_data.length-16; i < decoded_data.length; i++) {
            Log.d(TAG, "decoded_data: " + decoded_data[i]);
        }
        showNdefMessage(id, null, text_data, decoded_data);

        /*


         */
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
            NdefMessage[] ndefMessages;
            if (rawMsgs != null) {
                ndefMessages = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    ndefMessages[i] = (NdefMessage) rawMsgs[i];
                }
                Log.d(TAG, "NDEF: " + ndefMessages.length);
                byte[] tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);

                showNdefMessage(tagId, ndefMessages, null, null);
            } else {
                // Unknown tag type
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                IsoDep isoDep = IsoDep.get(tag);
                try {
                    readCPUCardData(id, isoDep);
                } catch (Exception e) {
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

    private void showNdefMessage(byte[] tagId, NdefMessage[] ndefMessages, byte[] textData, byte[] data) {
        Log.d(TAG, "showNdefMessage  tagId: " + StringUtil.bytesToHexString(tagId));
        NavHostFragment navHostFragment = (NavHostFragment) (getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main));
        if (navHostFragment.getChildFragmentManager().getFragments().size() > 0) {
            Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
            if (fragment != null && fragment instanceof FirstFragment) {
                ((FirstFragment) fragment).showNdefMessage(tagId, ndefMessages, textData, data);
            } else if (fragment != null && fragment instanceof IdCardFragment) {
                ((IdCardFragment) fragment).showNdefMessage(tagId, ndefMessages, textData, data);
            }
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
                                firstFragment.initTaskListView(tasks);
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

    public void tryTakePhoto() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
    }

    public void takePhoto() {
        Log.d(TAG, "takePhoto");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("MainActitity", ex.getMessage(), ex);
                Toast.makeText(this,
                        "Create Camera temp file failed: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.i(TAG, "FILEPATH " + getExternalFilesDir("Pictures").getAbsolutePath());
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.paipeng.checkin.provider",
                        photoFile);
                currentPhotoPath = photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST_CODE);
                Log.i(TAG, "startActivityForResult finished");
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".bmp",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case OPEN_GALLERY_REQUEST_CODE:
                    if (data == null) {
                        break;
                    }
                    try {
                        ContentResolver resolver = getContentResolver();
                        Uri uri = data.getData();
                        Bitmap image = MediaStore.Images.Media.getBitmap(resolver, uri);
                        String[] proj = {MediaStore.Images.Media.DATA};
                        Cursor cursor = managedQuery(uri, proj, null, null, null);
                        cursor.moveToFirst();
                        if (image != null) {
                            cur_predict_image = image;
                            //ivInputImage.setImageBitmap(image);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                    break;
                case TAKE_PHOTO_REQUEST_CODE:
                    if (currentPhotoPath != null) {
                        ExifInterface exif = null;
                        try {
                            exif = new ExifInterface(currentPhotoPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED);
                        Log.i(TAG, "rotation " + orientation);
                        Bitmap image = BitmapFactory.decodeFile(currentPhotoPath);
                        image = ImageUtil.rotateBitmap(image, orientation);
                        if (image != null) {
                            cur_predict_image = image;
                            //ivInputImage.setImageBitmap(image);
                            idCardOcr(image);
                        }
                    } else {
                        Log.e(TAG, "currentPhotoPath is null");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void idCardOcr(Bitmap image) {
        NavHostFragment navHostFragment = (NavHostFragment) (getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main));
        if (navHostFragment.getChildFragmentManager().getFragments().size() > 0) {
            Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
            if (fragment != null && fragment instanceof IdCardFragment) {
                ((IdCardFragment) fragment).ocrImage(image);
            }
        }
    }
}