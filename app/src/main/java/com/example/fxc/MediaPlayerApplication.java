package com.example.fxc;

import android.app.Application;
import android.os.Build;

import com.example.fxc.bt.BtMusicManager;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

public class MediaPlayerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BtMusicManager.getInstance().initBtData(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("L");
        }
    }

}