package com.paipeng.checkin.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.paipeng.checkin.data.model.LoggedInUser;
import com.paipeng.checkin.model.FaceOCRIdCard;
import com.paipeng.checkin.model.IdCard;
import com.paipeng.checkin.restclient.module.Role;
import com.paipeng.checkin.restclient.module.User;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class CommonUtil {
    private static final String TAG = CommonUtil.class.getSimpleName();
    private static final String SHARED_PREFS = "sharedPrefs";

    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String LOGIN_USER = "LOGIN_USER";
    public static final String USER_TOKEN = "USER_TOKEN";

    public static final String SERVER_URL = "http://114.115.137.22";
    private static User user;
    private static CommonUtil INSTANCE = null;
    private FaceOCRIdCard faceOCRIdCard;

    public static CommonUtil getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CommonUtil();
        return INSTANCE;
    }


    public static boolean isUserExists(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LOGIN_USER, null) != null;
    }

    public static LoggedInUser getLoggedInUser(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String data = sharedPreferences.getString(LOGIN_USER, null);
        Log.d(TAG, "getLoggedInUser: " + data);
        if (data != null) {
            Gson gson = new Gson();
            return gson.fromJson(data, LoggedInUser.class);
        } else {
            return null;
        }
    }

    public static void saveUser(Activity activity, LoggedInUser user) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(user);
        Log.d(TAG, "saveUser: " + json);
        editor.putString(LOGIN_USER, json);
        editor.commit();
        editor.apply();
    }

    public static LoggedInUser verifyLoggedInUser(Activity activity, LoggedInUser user) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String data = sharedPreferences.getString(LOGIN_USER, null);
        if (data != null) {
            Gson gson = new Gson();
            LoggedInUser loggedInUser =  gson.fromJson(data, LoggedInUser.class);
            if (user.getUsername().equals(loggedInUser.getUsername()) && user.getPassword().equals(loggedInUser.getPassword())) {
                return loggedInUser;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String getUserToken(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_TOKEN, null);
    }

    public static void setUserToken(Activity activity, String token) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Log.d(TAG, "setUserToken: " + token);
        editor.putString(USER_TOKEN, token);
        editor.commit();
        editor.apply();

    }

    public static User getUser() {
        return user;
    }

    public static boolean isAdmin() {
        if (user != null && user.getRoles() != null) {
            boolean roleAdmin = false;
            for (Role role : user.getRoles()) {
                if ("ADMIN".equals(role.getRole())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void setUser(User user) {
        CommonUtil.user = user;
    }

    public static IdCard convertToIdCard(String text) {
        // 姓名：黛丝斯 部门：业务部 单位：NFC证卡测试 签发日期：2021年10月22日 有效期：2022年12月31日
        IdCard idCard = new IdCard();
        String[] w = text.split("\n");
        if (w.length > 1) {
            idCard.setType(1);
            // 姓名: 诸葛亮
            //    单位: 三国蜀
            //    证卡编号: S1234567
            //    过期日期: 2022-12-01
            //    芯片序号: 0443671E196180
            for (String tt : w) {
                String[] ttt = tt.split(": ");

                if (ttt.length == 2) {
                    Log.d(TAG, "t2: " + ttt[0]);
                    if ("姓名".equals(ttt[0])) {
                        idCard.setName(ttt[1]);
                    } else if ("部门".equals(ttt[0])) {
                        idCard.setDepartment(ttt[1]);
                    } else if ("单位".equals(ttt[0])) {
                        idCard.setCompany(ttt[1]);
                    } else if ("签发日期".equals(ttt[0])) {
                        idCard.setIssuedDate(ttt[1]);
                    } else if ("过期日期".equals(ttt[0])) {
                        idCard.setExpireDate(ttt[1]);
                    } else if ("证卡编号".equals(ttt[0])) {
                        idCard.setSerialNumber(ttt[1]);
                    } else if ("芯片序号".equals(ttt[0])) {
                        idCard.setChipUID(ttt[1]);
                    }
                }
            }
        } else {
            idCard.setType(0);
            String[] data = text.split(" ");
            if (data != null && data.length > 0) {
                for (String tt : data) {
                    String[] ttt = tt.split("：");

                    if (ttt.length == 2) {
                        Log.d(TAG, "t2: " + ttt[0]);
                        if ("姓名".equals(ttt[0])) {
                            idCard.setName(ttt[1]);
                        } else if ("部门".equals(ttt[0])) {
                            idCard.setDepartment(ttt[1]);
                        } else if ("单位".equals(ttt[0])) {
                            idCard.setCompany(ttt[1]);
                        } else if ("签发日期".equals(ttt[0])) {
                            idCard.setIssuedDate(ttt[1]);
                        } else if ("有效期".equals(ttt[0])) {
                            idCard.setExpireDate(ttt[1]);
                        }
                    }
                }
            }
        }
        return idCard;
    }

    public static String convertHexToString(byte[] data) {
        return null;
    }



    public static void copyFileFromAssets(Context appCtx, String srcPath, String dstPath) {
        if (srcPath.isEmpty() || dstPath.isEmpty()) {
            return;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new BufferedInputStream(appCtx.getAssets().open(srcPath));
            os = new BufferedOutputStream(new FileOutputStream(new File(dstPath)));
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void copyDirectoryFromAssets(Context appCtx, String srcDir, String dstDir) {
        if (srcDir.isEmpty() || dstDir.isEmpty()) {
            return;
        }
        try {
            if (!new File(dstDir).exists()) {
                new File(dstDir).mkdirs();
            }
            for (String fileName : appCtx.getAssets().list(srcDir)) {
                String srcSubPath = srcDir + File.separator + fileName;
                String dstSubPath = dstDir + File.separator + fileName;
                if (new File(srcSubPath).isDirectory()) {
                    copyDirectoryFromAssets(appCtx, srcSubPath, dstSubPath);
                } else {
                    copyFileFromAssets(appCtx, srcSubPath, dstSubPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FaceOCRIdCard getFaceOCRIdCard() {
        return faceOCRIdCard;
    }

    public void setFaceOCRIdCard(FaceOCRIdCard faceOCRIdCard) {
        this.faceOCRIdCard = faceOCRIdCard;
    }

    public String generateRandomSerialNumber() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

}
