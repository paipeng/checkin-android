package com.paipeng.checkin.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.paipeng.checkin.MainActivity;
import com.paipeng.checkin.R;
import com.paipeng.checkin.SplashActivity;
import com.paipeng.checkin.data.model.LoggedInUser;
import com.paipeng.checkin.databinding.ActivityLoginBinding;
import com.paipeng.checkin.restclient.CheckInRestClient;
import com.paipeng.checkin.restclient.base.HttpClientCallback;
import com.paipeng.checkin.restclient.module.User;
import com.paipeng.checkin.utils.CommonUtil;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    LoggedInUser user = new LoggedInUser();
                    user.setUsername(usernameEditText.getText().toString());
                    user.setPassword(passwordEditText.getText().toString());
                    login(user);

                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);

                LoggedInUser user = new LoggedInUser();
                user.setUsername(usernameEditText.getText().toString());
                user.setPassword(passwordEditText.getText().toString());
                login(user);

            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();


        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);

        startActivity(intent);
        finish();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }


    private void login(LoggedInUser loggedInUser) {
        CheckInRestClient checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL);
        checkInRestClient.setHttpClientCallback(new HttpClientCallback<User>() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "onSuccess: " + user.getToken());

                loggedInUser.setDisplayName("");
                CommonUtil.saveUser(LoginActivity.this, loggedInUser);
                CommonUtil.setUserToken(LoginActivity.this, user.getToken());

                runOnUiThread(new Runnable() {
                    public void run() {
                        loginViewModel.login(user.getUsername(),
                                user.getPassword());
                        gotoMain();
                    }
                });
            }

            @Override
            public void onFailure(int code, String message) {
                Log.e(TAG, "getTicketData error: " + code + " msg: " + message);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(LoginActivity.this, ("getTicketData error: " + code + " msg: " + message), Toast.LENGTH_LONG);
                    }
                });
                //gotoLogin();
            }
        });

        User user = new User();
        user.setUsername(loggedInUser.getUsername());
        user.setPassword(loggedInUser.getPassword());
        user.setTenant("tenant_1");

        checkInRestClient.login(user);
    }


    private void gotoMain() {
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}