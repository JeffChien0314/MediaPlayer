package com.fxc.ev.mediacenter.service;

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
import android.os.AsyncTask;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.fxc.ev.mediacenter.ContentFragment;
import com.fxc.ev.mediacenter.IDataChangeListener;
import com.fxc.ev.mediacenter.IMyAidlInterface;
import com.fxc.ev.mediacenter.bluetooth.BtMusicManager;
import com.fxc.ev.mediacenter.bluetooth.client.MediaBrowserConnecter;
import com.fxc.ev.mediacenter.localplayer.CSDMediaPlayer;
import com.fxc.ev.mediacenter.datastruct.DeviceItem;
import com.fxc.ev.mediacenter.util.DeviceItemUtil;
import com.fxc.ev.mediacenter.util.MediaController;
import com.fxc.ev.mediacenter.datastruct.MediaInfo;
import com.fxc.ev.mediacenter.datastruct.MediaItem;
import com.fxc.ev.mediacenter.util.MediaItemUtil;
import com.fxc.ev.mediacenter.util.applicationUtils;
import com.fxc.ev.mediacenter.util.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.android.internal.logging.nano.MetricsProto.MetricsEvent.NOTIFICATION_ID;
import static com.fxc.ev.mediacenter.util.Constants.ACTION_SERVICE_START_BROADCAST;
import static com.fxc.ev.mediacenter.util.DeviceItemUtil.ACTION_DEVICE_CHANGED;
import static com.fxc.ev.mediacenter.util.DeviceItemUtil.ACTION_DEVICE_OF_LIST_LOST;

public class MediaPlayerService extends Service {
    private final String TAG = MediaPlayerService.class.getSimpleName();
    private CSDMediaPlayer mediaPlayer;
    // private DeviceItemUtil mDeviceItemUtil;
    private final int DEVICE_ADD = 0;
    private final int UPDATE_BT_STATE = 1;
    private final int DEVICE_LOST = 2;
    public static boolean isAlive = false;
    private MyTask myTask;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case DEVICE_ADD:
                    //同时需要更新文件列表
                    Intent intent1 = new Intent(ACTION_DEVICE_CHANGED);
                    intent1.setPackage(getApplicationContext().getPackageName());
                    sendBroadcast(intent1);
                    Log.i(TAG, "handleMessage: ACTION_DEVICE_CHANGED11");
                    break;
                case UPDATE_BT_STATE:
                    BtMusicManager.getInstance().initBtData(MediaPlayerService.this);
                    break;
                case DEVICE_LOST:
                    if (DeviceoOfCurrentListIsExist()) {
                        Intent intent2 = new Intent(ACTION_DEVICE_CHANGED);
                    intent2.setPackage(getApplicationContext().getPackageName());
                    sendBroadcast(intent2);
                        Log.i(TAG, "handleMessage: ACTION_DEVICE_CHANGED22");

                    } else {
                        Intent intent3 = new Intent(ACTION_DEVICE_OF_LIST_LOST);
                        intent3.setPackage(getApplicationContext().getPackageName());
                        sendBroadcast(intent3);
                        Log.i(TAG, "handleMessage: not exist");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private boolean DeviceoOfCurrentListIsExist() {
        List<DeviceItem> externalDeviceItems = DeviceItemUtil.getInstance(getApplicationContext()).getExternalDeviceInfoList(MediaPlayerService.this.getApplicationContext(), true);
        DeviceItem currentDevice = DeviceItemUtil.getInstance(getApplicationContext()).getCurrentDevice();
        if (currentDevice != null) {
            if (externalDeviceItems != null && externalDeviceItems.size() != 0) {
                for (int i = 0; i < externalDeviceItems.size(); i++) {
                    if (externalDeviceItems.get(i) != null && externalDeviceItems.get(i).getStoragePath() != null) {
                        if (externalDeviceItems.get(i).getStoragePath().equals(currentDevice.getStoragePath())) {
                            DeviceItem currentDeviceTemp = new DeviceItem();
                            DeviceItemUtil.getInstance(getApplicationContext()).setCurrentDevice(currentDeviceTemp);
                            Log.i(TAG, "DeviceoOfCurrentListIsExist: true");
                            return true;
                        }
                    }

                }
            }
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        isAlive = true;
        sendBroadcastToOtherApp();
        registerReceiver();
        MediaBrowserConnecter.getInstance(this.getApplicationContext()).initBroswer();
        // mDeviceItemUtil = DeviceItemUtil.getInstance(this.getApplicationContext());
        mediaPlayer = CSDMediaPlayer.getInstance(this);
        resetPlayerCondition(this.getApplicationContext());
        getALLMediaItems();
    }
    /* *
    Sandra add for 添加广播通知第三方APP service已开启*/
    public  void sendBroadcastToOtherApp(){
        Intent intent = new Intent();
        intent.setAction(ACTION_SERVICE_START_BROADCAST);//用隐式意图来启动广播
        intent.putExtra("msg", "MediaPlayerService started");
        sendBroadcast(intent);
    };
    /**
     * 1.判断是否使用过设备
     * 2.判断是否有设备列表，及上次使用的设备是否还在
     * 3.判断记录的item是否在列表，计算item当前的position
     * service 启动时需抓取上次记录的播放状态，如果上次记录内容还在，继续显示内容，否则清空
     **/
    private void resetPlayerCondition(Context context) {
        int position = -1;
        ArrayList<MediaItem> mediaItems = null;
        DeviceItem deviceItem = null;
        SharedPreferences share = context.getSharedPreferences("SavePlayingStatus", Context.MODE_PRIVATE);
        String storagePath = share.getString("storagePath", "");
        if (storagePath.equals("")) {//初次使用 && 尚未点击播放
            //此时展示空的内容
        } else {//再次叫起
            List<DeviceItem> externalDeviceItems;
            externalDeviceItems = DeviceItemUtil.getInstance(context).getExternalDeviceInfoList(context, false);
            if (externalDeviceItems == null || externalDeviceItems.size() == 0) {//没有设备
                Log.i(TAG, "recoverLastPlayingStatus:没有设备 ");
                Toast.makeText(context, "没有设备加载", Toast.LENGTH_LONG);
            } else {//有设备
                deviceItem = DeviceItemUtil.getInstance(context).getDeviceByStoragePath(share.getString("storagePath", ""));
                if (deviceItem != null) {//上次使用的设备还在
                    mediaItems = MediaItemUtil.getMusicInfos(context, storagePath);//无论上次播放的是视频还是音乐，返回后都展示音乐
                    if (mediaItems != null && mediaItems.size() != 0) {//设备有音乐
                        if (share.getInt("currentTab", 0) == MediaItemUtil.TYPE_MUSIC) {//判断上次播放的是音乐的话，就展示最后一次播放的歌曲
                            position = MediaItemUtil.IfIDExist(share.getLong("id", 0), MediaItemUtil.TYPE_MUSIC, context, mediaItems);
                        } else {//是视频的话，展示音乐，第一首歌
                            position = 0;
                        }
                    } else {//上次使用的设备没有音乐，待确认行为

                    }

                } else {//上次设备失联，无内容自动展开设备清单

                }
            }
        }
        mediaPlayer.setMediaInfo(new MediaInfo(mediaItems, deviceItem));
        Log.i(TAG, " mInstance.setMediaInfo(mediaInfo);: ");
        mediaPlayer.setPlayPosition(position);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(this);
       // return START_NOT_STICKY;
         return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        saveData();
        if (myTask != null) myTask.cancel(true);
        mediaPlayer.release();
        mediaPlayer = null;
        unregisterReceiver();
        isAlive = false;
        applicationUtils.startService(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public MyBinder onBind(Intent intent) {
        mRemoteCallbackList = new RemoteCallbackList<IDataChangeListener>();
        MyBinder myBinder = new MyBinder();
        return myBinder;
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

        IntentFilter playerFilter = new IntentFilter(Constants.ACTION_CHANGE_STATE_RECEIVER);
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
                    break;
                case Intent.ACTION_MEDIA_BAD_REMOVAL:
                    handler.sendEmptyMessage(DEVICE_LOST);
                    break;
                case Intent.ACTION_MEDIA_CHECKING:
                case Intent.ACTION_MEDIA_MOUNTED:// sd卡被插入，且已经挂载
                case Intent.ACTION_MEDIA_SCANNER_STARTED:
                    break;
                case Intent.ACTION_MEDIA_SCANNER_FINISHED:
                    handler.sendEmptyMessageDelayed(DEVICE_ADD, 500);
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
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                case BluetoothDevice.ACTION_NAME_CHANGED:
                case BluetoothDevice.ACTION_UUID:
                    handler.sendEmptyMessageDelayed(DEVICE_ADD, 100);
                    //  btA2dpContentStatus(intent);
                    break;
                case BluetoothA2dpSink.ACTION_PLAYING_STATE_CHANGED:
                    Log.e(TAG, "mBtReceiver，BluetoothA2dpSink.ACTION_PLAYING_STATE_CHANGED");
                    //控制蓝牙的播放状态,启动这个作为播放状态更新，时序太慢
                    //       playState(intent);
                    break;

                case BluetoothAvrcpController.ACTION_CONNECTION_STATE_CHANGED:
                    //TODO:1.disconnect 情况下，蓝牙页面控制播放的按钮需要disable
                    Log.e(TAG, "mBtReceiver，BluetoothAvrcpController.ACTION_CONNECTION_STATE_CHANGED");
                    break;
               /* case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED: //A2DP连接状态改变
                    Toast.makeText(context, "A2DP连接状态改变", Toast.LENGTH_SHORT);
                    break;
                case BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED: //A2DP播放状态改变
                    Toast.makeText(context, "A2DP播放状态改变", Toast.LENGTH_SHORT);
                    break;*/
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    handler.sendEmptyMessage(UPDATE_BT_STATE);
                    Log.e(TAG, "mBtReceiver，BluetoothAdapter.ACTION_STATE_CHANGED");
                    break;
             /*   case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
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
                case Constants.ACTION_CHANGE_STATE_RECEIVER:
                    int state = intent.getIntExtra(CSDMediaPlayer.STATE_EXTRA, Constants.STATE_PLAY);
                    int pos = intent.getIntExtra(CSDMediaPlayer.POS_EXTRA, -1);
                    if (Constants.USB_DEVICE == MediaController.getInstance(context).currentSourceType) {
                        mediaPlayer.mediaControl(state, pos);
                    } else {//蓝牙设备控制
                        MediaBrowserConnecter.getInstance(MediaPlayerService.this).setBTDeviceState(state, pos);
                    }

                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + intent.getAction());
            }
        }
    };


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

    public void getALLMediaItems() {
        // allDevicesMediaItems.clear();
        if (myTask != null && !myTask.isCancelled()) {
            myTask.cancel(true);
            myTask = null;
        }

        new MyTask().execute();
    }

    /**
     * 异步获取文件列表内容
     */

    class MyTask extends AsyncTask<String, Void, ArrayList<MediaItem>> implements com.fxc.ev.mediacenter.MyTask {

        public MyTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "onPreExecute: 抓取所有设备的文件 start" + ContentFragment.printTime());
            ContentFragment.printTime();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(ArrayList<MediaItem> s) {
            super.onCancelled(s);
        }

        @Override
        protected void onCancelled() {// 作用：将异步任务设置为：取消状态
            super.onCancelled();
        }

        // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果// 注：必须复写，从而自定义线程任务
        @Override
        protected ArrayList<MediaItem> doInBackground(String... strings) {
            ArrayList<MediaItem> musicItems = new ArrayList<MediaItem>();
            ArrayList<MediaItem> videoItems = new ArrayList<MediaItem>();
            ArrayList<MediaItem> TotalmediaItems = new ArrayList<MediaItem>();
            //抓取所有设备的文件
            List<DeviceItem> externalDeviceItems = DeviceItemUtil.getInstance(getApplicationContext()).getExternalDeviceInfoList();
            for (int i = 0; i < externalDeviceItems.size(); i++) {
                musicItems = MediaItemUtil.getMusicInfos(getApplicationContext(), externalDeviceItems.get(i).getStoragePath());
                for (int j = 0; j < musicItems.size(); j++) {
                    TotalmediaItems.add(musicItems.get(j));
                }
                videoItems = MediaItemUtil.getVideoInfos(getApplicationContext(), externalDeviceItems.get(i).getStoragePath());
                for (int k = 0; k < videoItems.size(); k++) {
                    TotalmediaItems.add(videoItems.get(k));
                }
            }
            Log.i(TAG, "doInBackground:抓取所有设备的文件 " + TotalmediaItems.size() + "Time：" + ContentFragment.printTime());
            return TotalmediaItems;
        }

        // 作用：接收线程任务执行结果、将执行结果显示到UI组件// 注：必须复写，从而自定义UI操作
        @Override
        protected void onPostExecute(ArrayList<MediaItem> result) {
            super.onPostExecute(result);
            MediaItemUtil.allDevicesMediaItems =result;
        }
    }

    private class MyBinder extends IMyAidlInterface.Stub {


        @Override
        public long getCurrentProgress() throws RemoteException {
          //  return mediaPlayer.getGSYVideoManager().getCurrentPosition();
            return MediaController.getInstance(getApplicationContext()).getCurrentProgress(getApplicationContext());
        }

        @Override
        public MediaItem getMediaItem() throws RemoteException {
            if (!MediaPlayerService.isAlive) {
                applicationUtils.startService(getApplicationContext());
            }
            Log.i(TAG, "getMediaItem:mediaPlayer.getPlayPosition() " + mediaPlayer.getPlayPosition());
            return   MediaController.getInstance(getApplicationContext()).getMediaItem(getApplicationContext());
        }

        @Override
        public void registerSetupNotification(IDataChangeListener listener) throws RemoteException {
            if (listener != null) {
                mRemoteCallbackList.register(listener);
            }
        }

        @Override
        public void unRegisterSetupNotification(IDataChangeListener listener) throws RemoteException {
            if (listener != null) {
                mRemoteCallbackList.unregister(listener);
            }
        }
    }

    private static RemoteCallbackList<IDataChangeListener> mRemoteCallbackList = null;
    public static void callback2ClientPlayStateChange(int playState) {
        if (mRemoteCallbackList == null || mRemoteCallbackList.getRegisteredCallbackCount() <= 0) {
            return;
        }
        synchronized (mRemoteCallbackList) {
            //开始回调
            mRemoteCallbackList.beginBroadcast();
            int N = mRemoteCallbackList.getRegisteredCallbackCount();
            Log.i("", "callback2Client N ==" + N);
            for (int i = 0; i < N; i++) {
                try {
                    if (mRemoteCallbackList.getBroadcastItem(i) == null) {
                        continue;
                    }
                    Log.i("", "callback2Client nodeData ==" + playState);
                    //get出来以后调用dataCalback到客户端去
                    mRemoteCallbackList.getBroadcastItem(i).onStateChange(playState);
                } catch (DeadObjectException e) {
                    if (mRemoteCallbackList.getBroadcastItem(i) != null)
                        mRemoteCallbackList.unregister(mRemoteCallbackList.getBroadcastItem(i));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            //结束回调
            mRemoteCallbackList.finishBroadcast();
        }
    }
    public static void callback2ClientContentChange(MediaItem mediaItem) {
        if (mRemoteCallbackList == null || mRemoteCallbackList.getRegisteredCallbackCount() <= 0) {
            return;
        }
        synchronized (mRemoteCallbackList) {
            //开始回调
            mRemoteCallbackList.beginBroadcast();
            int N = mRemoteCallbackList.getRegisteredCallbackCount();
            Log.i("", "callback2Client N ==" + N);
            for (int i = 0; i < N; i++) {
                try {
                    if (mRemoteCallbackList.getBroadcastItem(i) == null) {
                        continue;
                    }
                    Log.i("", "callback2Client nodeData ==" + mediaItem.getTitle());
                    //get出来以后调用dataCalback到客户端去
                    mRemoteCallbackList.getBroadcastItem(i).onContentChange(mediaItem);
                } catch (DeadObjectException e) {
                    if (mRemoteCallbackList.getBroadcastItem(i) != null)
                        mRemoteCallbackList.unregister(mRemoteCallbackList.getBroadcastItem(i));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            //结束回调
            mRemoteCallbackList.finishBroadcast();
        }
    }
    public static void callback2ClientCurrentDurationChange(long currentDuration) {
        if (mRemoteCallbackList == null || mRemoteCallbackList.getRegisteredCallbackCount() <= 0) {
            return;
        }
        synchronized (mRemoteCallbackList) {
            //开始回调
            mRemoteCallbackList.beginBroadcast();
            int N = mRemoteCallbackList.getRegisteredCallbackCount();
            Log.i("", "callback2Client N ==" + N);
            for (int i = 0; i < N; i++) {
                try {
                    if (mRemoteCallbackList.getBroadcastItem(i) == null) {
                        continue;
                    }
                    Log.i("", "callback2Client nodeData ==" + currentDuration);
                    //get出来以后调用dataCalback到客户端去
                    mRemoteCallbackList.getBroadcastItem(i).onDurationChange(currentDuration);
                } catch (DeadObjectException e) {
                    if (mRemoteCallbackList.getBroadcastItem(i) != null)
                        mRemoteCallbackList.unregister(mRemoteCallbackList.getBroadcastItem(i));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            //结束回调
            mRemoteCallbackList.finishBroadcast();
        }
    }
}
