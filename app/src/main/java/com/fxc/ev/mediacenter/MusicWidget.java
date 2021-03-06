package com.fxc.ev.mediacenter;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.fxc.ev.mediacenter.util.MediaController;
import com.fxc.ev.mediacenter.datastruct.MediaItem;
import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.service.MediaPlayerService;
import com.fxc.ev.mediacenter.util.applicationUtils;
import com.fxc.ev.mediacenter.util.Constants;;
import static com.fxc.ev.mediacenter.util.DeviceItemUtil.ACTION_DEVICE_OF_LIST_LOST;
import static com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PAUSE;
import static com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PLAYING;

public class MusicWidget extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i("MusicWidget", "onReceive: action=" + intent.getAction());
        String action = intent.getAction();
        if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) { //操作widget
            Uri data = intent.getData();
            int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
            switch (buttonId) {
                case R.id.player_pause:
                    MediaController.getInstance(context).setPlayerState(Constants.STATE_PLAY, -1);
                    break;
                case R.id.player_play:
                    MediaController.getInstance(context).setPlayerState(Constants.STATE_PAUSE, -1);
                    break;
                case R.id.skip_fwd:
                    MediaController.getInstance(context).setPlayerState(Constants.STATE_NEXT, -1);
                    break;
                case R.id.skip_back:
                    MediaController.getInstance(context).setPlayerState(Constants.STATE_PREVIOUS, -1);
                    break;
                case R.id.widget_open:
                    /*Intent intentShow = new Intent("android.intent.action.MAIN");
                    intentShow.setPackage("com.example.fxc.mediaplayer");
                    intentShow.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentShow);*/
                    Intent intentShow = new Intent();
                    intentShow.setClass(context, MainActivity.class);
                    intentShow.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentShow);
                    break;
            }
        } else if (action != null && action.equals(Constants.ACTION_STATE_CHANGED_BROADCAST)) { //刷新widget显示
            int currentState = intent.getIntExtra(Constants.PLAYSTATE_CHANGED + "", -1);
            //  MediaItem mediaItem = intent.getParcelableExtra(MEDIAITEM_CHANGED + "");
            if (currentState == CURRENT_STATE_PLAYING) {
                pushUpdate(context, AppWidgetManager.getInstance(context), null, true);
            } /*else if (currentState == CURRENT_STATE_ERROR) {
                pushUpdate(context, AppWidgetManager.getInstance(context), "", false);
            }*/ else if (currentState == CURRENT_STATE_PAUSE) {
                pushUpdate(context, AppWidgetManager.getInstance(context), null, false);
            }
/*

            if (mediaItem != null) {
                pushUpdate(context, AppWidgetManager.getInstance(context), mediaItem, null);
            }
*/

        } else if (action != null && action.equals(Constants.ACTION_MEDIAITEM_CHANGED_BROADCAST)) { //刷新widget显示
            //   int currentState = intent.getIntExtra(PLAYSTATE_CHANGED + "", -1);
            //  MediaItem mediaItem = intent.getParcelableExtra(Constants.MEDIAITEM_CHANGED + "");
            //  if (mediaItem != null) {
            pushUpdate(context, AppWidgetManager.getInstance(context), null /*mediaItem*/, true);
            //   }

        } else if (action != null && action.equals(ACTION_DEVICE_OF_LIST_LOST)) { //第一次使用或当前设备失联


        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (!MediaPlayerService.isAlive) {
            applicationUtils.startService(context);
        }
        pushUpdate(context, appWidgetManager, null /*MediaController.getInstance(context).getMediaItem(context)*/, false);
    }

    private void pushUpdate(Context context, AppWidgetManager appWidgetManager, MediaItem mediaItem, Boolean play_pause) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_music_widget);
        remoteViews.setOnClickPendingIntent(R.id.player_play, getPendingIntent(context, R.id.player_play));
        remoteViews.setOnClickPendingIntent(R.id.player_pause, getPendingIntent(context, R.id.player_pause));
        remoteViews.setOnClickPendingIntent(R.id.skip_fwd, getPendingIntent(context, R.id.skip_fwd));
        remoteViews.setOnClickPendingIntent(R.id.skip_back, getPendingIntent(context, R.id.skip_back));
        remoteViews.setOnClickPendingIntent(R.id.widget_open, getPendingIntent(context, R.id.widget_open));
        //设置内容
        mediaItem = MediaController.getInstance(context).getMediaItem(context);
        if (mediaItem.getTitle() != null) {
            remoteViews.setTextViewText(R.id.song_name, mediaItem.getTitle());
            String artistAndAlbum = "null-null";
            artistAndAlbum = mediaItem.getArtist() + "-" + mediaItem.getAlbum();
            remoteViews.setTextViewText(R.id.artist_album, artistAndAlbum);
            //Sandra@20220507 add for 专辑图片显示-->
            Bitmap bitmap = mediaItem.getThumbBitmap();
            remoteViews.setImageViewBitmap(R.id.alum_photo, bitmap);
            //remoteViews.setImageViewBitmap(R.id.bg_photo,bitmap);
            //<--Sandra@20220507 add for 专辑图片显示

            remoteViews.setViewVisibility(R.id.alum_photo, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.skip_fwd, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.skip_back, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.artist_album, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.song_name, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.player_pause, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.player_play, View.INVISIBLE);
            //设定按钮图片
            if (play_pause) { //播放
                remoteViews.setViewVisibility(R.id.player_pause, View.INVISIBLE);
                remoteViews.setViewVisibility(R.id.player_play, View.VISIBLE);
            } else { //暂停
                remoteViews.setViewVisibility(R.id.player_pause, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.player_play, View.INVISIBLE);
            }
        } else {
            //Sandra@20220507 add for 无播放内容的情形
            remoteViews.setViewVisibility(R.id.player_pause, View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.player_play, View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.skip_fwd, View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.skip_back, View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.artist_album, View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.song_name, View.INVISIBLE);
            remoteViews.setImageViewResource(R.id.alum_photo, R.drawable.img_album);
            //   remoteViews.setImageViewResource(R.id.bg_photo,R.drawable.img_album);
        }
        ComponentName componentName = new ComponentName(context, MusicWidget.class);
        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }


    private PendingIntent getPendingIntent(Context context, int buttonId) {
        Intent intent = new Intent();
        intent.setClass(context, MusicWidget.class);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setData(Uri.parse("" + buttonId));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pi;
    }


}