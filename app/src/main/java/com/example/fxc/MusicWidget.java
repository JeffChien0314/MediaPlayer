package com.example.fxc;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.fxc.service.MediaPlayerService;

public class MusicWidget extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        /* <action android:name="CSDMediaPlayer.stateChanged" />=>播放状态改变，给UI修改播放器当前状态显示
                <action android:name="DeviceManager.deviceChanged" />=>设备改变，给UI重新抓取设备*/
        Log.i("MusicWidget", "onReceive: action=" + intent.getAction());
        if (!MediaPlayerService.isAlive) {
            context.startForegroundService(new Intent(context, MediaPlayerService.class));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //音乐控制部分，直接调用即可
        //  MediaController.getInstance(context).setPlayerState(state,posion);
    }


}
