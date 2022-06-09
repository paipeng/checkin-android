package com.paipeng.checkin;

import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.paipeng.checkin.data.model.LoggedInUser;
import com.paipeng.checkin.restclient.CheckInRestClient;
import com.paipeng.checkin.restclient.base.HttpClientCallback;
import com.paipeng.checkin.restclient.module.User;
import com.paipeng.checkin.utils.CommonUtil;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    protected void checkLogin() {
        LoggedInUser loggedInUser = CommonUtil.getLoggedInUser(BaseActivity.this);
        if (loggedInUser == null) {
            gotoLogin();
        } else {
            login(loggedInUser);
            //intent.setClass(SplashActivity.this, MainActivity.class);
        }
    }
    protected void login(LoggedInUser loggedInUser) {
        CheckInRestClient checkInRestClient = new CheckInRestClient(CommonUtil.SERVER_URL);
        checkInRestClient.setHttpClientCallback(new HttpClientCallback<User>() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "onSuccess: " + user.getToken());
                CommonUtil.setUserToken(BaseActivity.this, user.getToken());
                CommonUtil.setUser(user);
                loginSuccess();
            }

            @Override
            public void onFailure(int code, String message) {
                Log.e(TAG, "getTicketData error: " + code + " msg: " + message);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(BaseActivity.this, ("getTicketData error: " + code + " msg: " + message), Toast.LENGTH_LONG);
                    }
                });
                loginFailed();
            }
        });

        User user = new User();
        user.setUsername(loggedInUser.getUsername());
        user.setPassword(loggedInUser.getPassword());
        user.setTenant("tenant_1");

        checkInRestClient.login(user);
    }

    protected abstract void loginSuccess();
    protected abstract void loginFailed();

    protected void gotoMain() {

    }

    protected void gotoLogin() {

    }
}
