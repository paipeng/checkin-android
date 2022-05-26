package com.paipeng.checkin;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.baidu.paddle.lite.demo.ocr.Predictor;
import com.paipeng.checkin.databinding.FragmentOrcBinding;

public class OcrFragment extends Fragment {
    private static final String TAG = TaskFragment.class.getSimpleName();

    private FragmentOrcBinding binding;

    protected Predictor predictor = new Predictor();

    public static final int REQUEST_LOAD_MODEL = 0;
    public static final int REQUEST_RUN_MODEL = 1;
    public static final int RESPONSE_LOAD_MODEL_SUCCESSED = 0;
    public static final int RESPONSE_LOAD_MODEL_FAILED = 1;
    public static final int RESPONSE_RUN_MODEL_SUCCESSED = 2;
    public static final int RESPONSE_RUN_MODEL_FAILED = 3;

    protected String modelPath = "";
    protected String labelPath = "";
    protected String imagePath = "";

    protected int cpuThreadNum = 1;
    protected String cpuPowerMode = "";
    protected int detLongSize = 960;
    protected float scoreThreshold = 0.1f;

    protected Handler receiver = null; // Receive messages from worker thread
    protected Handler sender = null; // Send command to worker thread
    protected HandlerThread worker = null; // Worker thread to load&run model


    protected ProgressDialog pbLoadModel = null;
    protected ProgressDialog pbRunModel = null;

    protected Spinner spRunMode;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentOrcBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean settingsChanged = false;
        boolean model_settingsChanged = false;
        String model_path = getString(R.string.MODEL_PATH_DEFAULT);
        String label_path = getString(R.string.LABEL_PATH_DEFAULT);
        //String image_path = sharedPreferences.getString(getString(R.string.IMAGE_PATH_KEY),
        //        getString(R.string.IMAGE_PATH_DEFAULT));
        model_settingsChanged |= !model_path.equalsIgnoreCase(modelPath);
        settingsChanged |= !label_path.equalsIgnoreCase(labelPath);
        //settingsChanged |= !image_path.equalsIgnoreCase(imagePath);
        int cpu_thread_num = Integer.parseInt(sharedPreferences.getString(getString(R.string.CPU_THREAD_NUM_KEY),
                getString(R.string.CPU_THREAD_NUM_DEFAULT)));
        model_settingsChanged |= cpu_thread_num != cpuThreadNum;
        String cpu_power_mode =
                sharedPreferences.getString(getString(R.string.CPU_POWER_MODE_KEY),
                        getString(R.string.CPU_POWER_MODE_DEFAULT));
        model_settingsChanged |= !cpu_power_mode.equalsIgnoreCase(cpuPowerMode);

        int det_long_size = Integer.parseInt(sharedPreferences.getString(getString(R.string.DET_LONG_SIZE_KEY),
                getString(R.string.DET_LONG_SIZE_DEFAULT)));
        settingsChanged |= det_long_size != detLongSize;
        float score_threshold =
                Float.parseFloat(sharedPreferences.getString(getString(R.string.SCORE_THRESHOLD_KEY),
                        getString(R.string.SCORE_THRESHOLD_DEFAULT)));
        settingsChanged |= scoreThreshold != score_threshold;
        if (settingsChanged) {
            labelPath = label_path;
            //imagePath = image_path;
            detLongSize = det_long_size;
            scoreThreshold = score_threshold;
            //set_img();
        }
        if (true) {
            modelPath = model_path;
            cpuThreadNum = cpu_thread_num;
            cpuPowerMode = cpu_power_mode;
            // Update UI
            //tvInputSetting.setText("Model: " + modelPath.substring(modelPath.lastIndexOf("/") + 1) + "\nOPENCL: " + cbOpencl.isChecked() + "\nCPU Thread Num: " + cpuThreadNum + "\nCPU Power Mode: " + cpuPowerMode);
            //tvInputSetting.scrollTo(0, 0);
            // Reload model if configure has been changed
            loadModel();
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(OcrFragment.this)
                        .navigate(R.id.action_Second3Fragment_to_First3Fragment);
            }
        });

        // Prepare the worker thread for mode loading and inference
        receiver = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RESPONSE_LOAD_MODEL_SUCCESSED:
                        if (pbLoadModel != null && pbLoadModel.isShowing()) {
                            pbLoadModel.dismiss();
                        }
                        onLoadModelSuccessed();
                        break;
                    case RESPONSE_LOAD_MODEL_FAILED:
                        if (pbLoadModel != null && pbLoadModel.isShowing()) {
                            pbLoadModel.dismiss();
                        }
                        Toast.makeText(getActivity(), "Load model failed!", Toast.LENGTH_SHORT).show();
                        onLoadModelFailed();
                        break;
                    case RESPONSE_RUN_MODEL_SUCCESSED:
                        if (pbRunModel != null && pbRunModel.isShowing()) {
                            pbRunModel.dismiss();
                        }
                        onRunModelSuccessed();
                        break;
                    case RESPONSE_RUN_MODEL_FAILED:
                        if (pbRunModel != null && pbRunModel.isShowing()) {
                            pbRunModel.dismiss();
                        }
                        Toast.makeText(getActivity(), "Run model failed!", Toast.LENGTH_SHORT).show();
                        onRunModelFailed();
                        break;
                    default:
                        break;
                }
            }
        };

        worker = new HandlerThread("Predictor Worker");
        worker.start();
        sender = new Handler(worker.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REQUEST_LOAD_MODEL:
                        // Load model and reload test image
                        if (onLoadModel()) {
                            receiver.sendEmptyMessage(RESPONSE_LOAD_MODEL_SUCCESSED);
                        } else {
                            receiver.sendEmptyMessage(RESPONSE_LOAD_MODEL_FAILED);
                        }
                        break;
                    case REQUEST_RUN_MODEL:
                        // Run model if model is loaded
                        if (onRunModel()) {
                            receiver.sendEmptyMessage(RESPONSE_RUN_MODEL_SUCCESSED);
                        } else {
                            receiver.sendEmptyMessage(RESPONSE_RUN_MODEL_FAILED);
                        }
                        break;
                    default:
                        break;
                }
            }
        };


        Bundle bundle = this.getArguments();

        if (bundle != null) {
            // handle your code here.
            String value = bundle.getString("key");
            Log.d(TAG, "value: " + value);

            Bitmap bitmap = (Bitmap) bundle.getParcelable("IMAGE");

            binding.ocrImageView.setImageBitmap(bitmap);

        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    public void loadModel() {
        pbLoadModel = ProgressDialog.show(getActivity(), "", "loading model...", false, false);
        sender.sendEmptyMessage(REQUEST_LOAD_MODEL);
    }

    public void runModel() {
        Log.d(TAG, "runModel");
        pbRunModel = ProgressDialog.show(getActivity(), "", "running model...", false, false);
        sender.sendEmptyMessage(REQUEST_RUN_MODEL);
    }

    public boolean onLoadModel() {
        Log.d(TAG, "onLoadModel");
        if (predictor.isLoaded()) {
            predictor.releaseModel();
        }
        Log.d(TAG, "modelPath: " + modelPath);
        Log.d(TAG, "labelPath: " + labelPath);
        return predictor.init(getActivity(), modelPath, labelPath, 0, cpuThreadNum,
                cpuPowerMode,
                detLongSize, scoreThreshold);
    }

    public boolean onRunModel() {
        /*
        String run_mode = spRunMode.getSelectedItem().toString();
        int run_det = run_mode.contains("检测") ? 1 : 0;
        int run_cls = run_mode.contains("分类") ? 1 : 0;
        int run_rec = run_mode.contains("识别") ? 1 : 0;

         */
        return predictor.isLoaded() && predictor.runModel(1, 1, 1);
    }

    public void onLoadModelSuccessed() {
        Log.d(TAG, "onLoadModelSuccessed");
        // Load test image from path and run model
        /*
        tvInputSetting.setText("Model: " + modelPath.substring(modelPath.lastIndexOf("/") + 1) + "\nOPENCL: " + cbOpencl.isChecked() + "\nCPU Thread Num: " + cpuThreadNum + "\nCPU Power Mode: " + cpuPowerMode);
        tvInputSetting.scrollTo(0, 0);
        tvStatus.setText("STATUS: load model successed");

         */
        binding.statusTextView.setText("STATUS: load model successed");

        BitmapDrawable drawable = (BitmapDrawable) binding.ocrImageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        predictor.setInputImage(bitmap);
        runModel();

    }

    public void onLoadModelFailed() {
        binding.statusTextView.setText("STATUS: load model failed");
    }

    public void onRunModelSuccessed() {
        binding.statusTextView.setText("STATUS: run model successed");
        // Obtain results and update UI
        /*
        tvInferenceTime.setText("Inference time: " + predictor.inferenceTime() + " ms");
        Bitmap outputImage = predictor.outputImage();
        if (outputImage != null) {
            ivInputImage.setImageBitmap(outputImage);
        }
        tvOutputResult.setText(predictor.outputResult());
        tvOutputResult.scrollTo(0, 0);

         */
    }

    public void onRunModelFailed() {
        binding.statusTextView.setText("STATUS: run model failed");
    }


}