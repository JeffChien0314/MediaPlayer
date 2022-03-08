package com.example.fxc.util;

import android.content.Context;
import android.content.Intent;

import com.example.fxc.service.MediaPlayerService;

public class applicationUtils {
    public static void startService(Context context) {
        context.startForegroundService(new Intent(context, MediaPlayerService.class));
    }
}
