package com.example.fxc.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAvrcpController;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.fxc.mediaplayer.CSDMediaPlayer;
import com.example.fxc.mediaplayer.DeviceManager;

import static com.example.fxc.mediaplayer.CSDMediaPlayer.ACTION_CHANGE_STATE;
import static com.example.fxc.mediaplayer.CSDMediaPlayer.POS_EXTRA;
import static com.example.fxc.mediaplayer.CSDMediaPlayer.STATE_EXTRA;
import static com.example.fxc.mediaplayer.CSDMediaPlayer.STATE_PLAY;
import static com.example.fxc.mediaplayer.Constants.USB_DEVICE;
import static com.example.fxc.service.notifications.MediaNotificationManager.NOTIFICATION_ID;

public class MediaPlayerService extends Service {
    private final String TAG = MediaPlayerService.class.getSimpleName();
    //public static CSDMediaPlayer mediaPlayer= CSDMediaPlayer.getInstance(this.getApplicationContext());;//本地音乐播放器
    public static CSDMediaPlayer mediaPlayer;
    private DeviceManager mDeviceManager;
    private static int currentSourceType = USB_DEVICE;


    private final int UPDATE_DEVICE_LIST = 0;
    public static boolean isAlive = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case UPDATE_DEVICE_LIST:
                    mDeviceManager.getExternalDeviceInfoList(MediaPlayerService.this.getApplicationContext());
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        registerReceiver();
        mDeviceManager = DeviceManager.getInstance(this.getApplicationContext());
        // mediaPlayer=CSDMediaPlayer.getInstance(this);
        //  mediaPlayer = new CSDMediaPlayer(this.getApplicationContext());
        mediaPlayer = CSDMediaPlayer.getInstance(this);
        resetPlayerCondition();

    }


    /**
     * service 启动时需抓取上次记录的播放状态，如果上次记录内容还在，继续播放，否则清空不播
     */
    private void resetPlayerCondition() {

        //1.判断存储设备是否在
        //2.判断是否有列表
        //3.判断记录的item是否在列表
        //满足上面3点，重新setList&position，且播放，否则清空列表不播

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        saveData();
        mediaPlayer.release();
        mediaPlayer = null;
        unregisterReceiver();
        isAlive = false;
        startService(new Intent(this, MediaPlayerService.class));
        super.onDestroy();
    }

    public CSDMediaPlayer getMediaPlayer() {
        if (null == mediaPlayer) {
            mediaPlayer = CSDMediaPlayer.getInstance(this);
        }
        return mediaPlayer;
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("currentStatus", Context.MODE_PRIVATE); //私有数据
        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
        editor.putString("url", mediaPlayer.getCurrentUri());
        editor.apply();
    }

    public void registerReceiver() {
        //USB Device侦测
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);// sd卡被插入，且已经挂载
        intentFilter.setPriority(1000);// 设置最高优先级
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// sd卡存在，但还没有挂载
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);// sd卡被移除
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);// sd卡作为 USB大容量存储被共享，挂载被解除
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);// sd卡已经从sd卡插槽拔出，但是挂载点还没解除
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);// 开始扫描
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);// 扫描完成
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_NOFS);
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentFilter.addDataScheme("file");
        registerReceiver(USBDeviceReceiver, intentFilter);

        //蓝牙相关action
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        filter.addAction(BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED);//A2DP连接状态改变
        filter.addAction(BluetoothA2dpSink.ACTION_PLAYING_STATE_CHANGED);//A2DP播放状态改变
        filter.addAction(BluetoothAvrcpController.ACTION_CONNECTION_STATE_CHANGED);//连接状态
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, filter);

        IntentFilter playerFilter = new IntentFilter(ACTION_CHANGE_STATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(playerControlReceiver, playerFilter);
    }

    public void unregisterReceiver() {
        unregisterReceiver(USBDeviceReceiver);
        unregisterReceiver(bluetoothReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playerControlReceiver);
    }

    private final BroadcastReceiver USBDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive: action=" + action);
            switch (action) {
                case Intent.ACTION_MEDIA_EJECT:
                case Intent.ACTION_MEDIA_UNMOUNTED:
                case Intent.ACTION_MEDIA_BAD_REMOVAL:
                case Intent.ACTION_MEDIA_CHECKING:
                case Intent.ACTION_MEDIA_MOUNTED:// sd卡被插入，且已经挂载
                    handler.sendEmptyMessageDelayed(UPDATE_DEVICE_LIST, 500);
                    break;
                case Intent.ACTION_MEDIA_SCANNER_STARTED:
                case Intent.ACTION_MEDIA_SCANNER_FINISHED:
                    //这个地方可以判断可已抓取U盘内部的文件

                    break;
                default:
                    break;
            }
        }
    };

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive: action=" + action);
            switch (action) {
                case BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED:
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                case BluetoothDevice.ACTION_NAME_CHANGED:
                case BluetoothDevice.ACTION_UUID:
                    handler.sendEmptyMessageDelayed(UPDATE_DEVICE_LIST, 100);
                    //  btA2dpContentStatus(intent);
                    break;
                case BluetoothA2dpSink.ACTION_PLAYING_STATE_CHANGED:
                    Log.e(TAG, "mBtReceiver，BluetoothA2dpSink.ACTION_PLAYING_STATE_CHANGED");
                    //控制蓝牙的播放状态,启动这个作为播放状态更新，时序太慢
                    //       playState(intent);
                    break;

                case BluetoothAvrcpController.ACTION_CONNECTION_STATE_CHANGED:
                    Log.e(TAG, "mBtReceiver，BluetoothAvrcpController.ACTION_CONNECTION_STATE_CHANGED");
                    break;
               /* case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED: //A2DP连接状态改变
                    Toast.makeText(context, "A2DP连接状态改变", Toast.LENGTH_SHORT);
                    break;
                case BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED: //A2DP播放状态改变
                    Toast.makeText(context, "A2DP播放状态改变", Toast.LENGTH_SHORT);
                    break;*/
                /*case BluetoothAdapter.ACTION_STATE_CHANGED:
                    handler.sendEmptyMessage(UPDATE_DEVICE_LIST);
                    Log.e(TAG, "mBtReceiver，BluetoothAdapter.ACTION_STATE_CHANGED");
                    break;
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    handler.sendEmptyMessageDelayed(UPDATE_DEVICE_LIST, 100);
                    Log.e(TAG, "mBtReceiver，BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED");
                    //用这个广播判断蓝牙连接状态
                    break;
                case BluetoothDevice.ACTION_NAME_CHANGED:
                    handler.sendEmptyMessage(UPDATE_DEVICE_LIST);
                    Log.e(TAG, "mBtReceiver， BluetoothDevice.ACTION_NAME_CHANGED");
                    break;*/
                default:
                    break;
            }
        }
    };

    private final BroadcastReceiver playerControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_CHANGE_STATE:
                    int state = intent.getIntExtra(STATE_EXTRA, STATE_PLAY);
                    int pos = intent.getIntExtra(POS_EXTRA, -1);
                    if (USB_DEVICE == currentSourceType) {
                        mediaPlayer.mediaControl(state, pos);
                    }

                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + intent.getAction());
            }
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MediaServiceBinder();
    }

    public class MediaServiceBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    private static void startForeground(Service service) {

        String CHANNEL_ONE_ID = MediaPlayerService.class.getName();
        String CHANNEL_ONE_NAME = "MeidaPlayer";
        Intent notificationIntent = new Intent(service, MediaPlayerService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(service, 0, notificationIntent, 0);

        Notification.Builder builder = new Notification.Builder(service.getApplicationContext())
                .setContentIntent(pendingIntent) // 设置PendingIntent
                //.setSmallIcon(R.drawable.ic_launcher) // 设置状态栏内的小图标
                .setContentTitle(MediaPlayerService.class.getSimpleName())
                .setContentText(MediaPlayerService.class.getSimpleName()); // 设置上下文内容
        // .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false);//是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }

        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        service.startForeground(NOTIFICATION_ID, notification);

    }

}
