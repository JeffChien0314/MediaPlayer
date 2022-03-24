package com.fxc.ev.mediacenter.util;

import android.content.Context;
import android.content.Intent;

import com.fxc.ev.mediacenter.service.MediaPlayerService;

public class applicationUtils {
    public static void startService(Context context) {
        context.startForegroundService(new Intent(context, MediaPlayerService.class));
    }
}
