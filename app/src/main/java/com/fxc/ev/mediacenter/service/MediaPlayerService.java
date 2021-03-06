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
import android.os.Parcel;
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
                    //??????????????????????????????
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
    Sandra add for ???????????????????????????APP service?????????*/
    public  void sendBroadcastToOtherApp(){
        Intent intent = new Intent();
        intent.setAction(ACTION_SERVICE_START_BROADCAST);//??????????????????????????????
        intent.putExtra("msg", "MediaPlayerService started");
        sendBroadcast(intent);
    };
    /**
     * 1.???????????????????????????
     * 2.??????????????????????????????????????????????????????????????????
     * 3.???????????????item????????????????????????item?????????position
     * service ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     **/
    private void resetPlayerCondition(Context context) {
        int position = -1;
        ArrayList<MediaItem> mediaItems = null;
        DeviceItem deviceItem = null;
        SharedPreferences share = context.getSharedPreferences("SavePlayingStatus", Context.MODE_PRIVATE);
        String storagePath = share.getString("storagePath", "");
        if (storagePath.equals("")) {//???????????? && ??????????????????
            //????????????????????????
        } else {//????????????
            List<DeviceItem> externalDeviceItems;
            externalDeviceItems = DeviceItemUtil.getInstance(context).getExternalDeviceInfoList(context, false);
            if (externalDeviceItems == null || externalDeviceItems.size() == 0) {//????????????
                Log.i(TAG, "recoverLastPlayingStatus:???????????? ");
                Toast.makeText(context, "??????????????????", Toast.LENGTH_LONG);
            } else {//?????????
                deviceItem = DeviceItemUtil.getInstance(context).getDeviceByStoragePath(share.getString("storagePath", ""));
                if (deviceItem != null) {//???????????????????????????
                    mediaItems = MediaItemUtil.getMusicInfos(context, storagePath);//?????????????????????????????????????????????????????????????????????
                    if (mediaItems != null && mediaItems.size() != 0) {//???????????????
                        if (share.getInt("currentTab", 0) == MediaItemUtil.TYPE_MUSIC) {//???????????????????????????????????????????????????????????????????????????
                            position = MediaItemUtil.IfIDExist(share.getLong("id", 0), MediaItemUtil.TYPE_MUSIC, context, mediaItems);
                        } else {//?????????????????????????????????????????????
                            position = 0;
                        }
                    } else {//???????????????????????????????????????????????????

                    }

                } else {//??????????????????????????????????????????????????????

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
        SharedPreferences sharedPreferences = getSharedPreferences("currentStatus", Context.MODE_PRIVATE); //????????????
        SharedPreferences.Editor editor = sharedPreferences.edit();//???????????????
        editor.putString("url", mediaPlayer.getCurrentUri());
        editor.apply();
    }

    public void registerReceiver() {
        //USB Device??????
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);// sd??????????????????????????????
        intentFilter.setPriority(1000);// ?????????????????????
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// sd??????????????????????????????
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);// sd????????????
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);// sd????????? USB??????????????????????????????????????????
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);// sd????????????sd?????????????????????????????????????????????
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);// ????????????
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);// ????????????
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_NOFS);
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentFilter.addDataScheme("file");
        registerReceiver(USBDeviceReceiver, intentFilter);

        //????????????action
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        filter.addAction(BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED);//A2DP??????????????????
        filter.addAction(BluetoothA2dpSink.ACTION_PLAYING_STATE_CHANGED);//A2DP??????????????????
        filter.addAction(BluetoothAvrcpController.ACTION_CONNECTION_STATE_CHANGED);//????????????
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
                case Intent.ACTION_MEDIA_MOUNTED:// sd??????????????????????????????
                case Intent.ACTION_MEDIA_SCANNER_STARTED:
                    break;
                case Intent.ACTION_MEDIA_SCANNER_FINISHED:
                    handler.sendEmptyMessageDelayed(DEVICE_ADD, 500);
                    //????????????????????????????????????U??????????????????
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
                    Log.e(TAG, "mBtReceiver???BluetoothA2dpSink.ACTION_PLAYING_STATE_CHANGED");
                    //???????????????????????????,???????????????????????????????????????????????????
                    //       playState(intent);
                    break;

                case BluetoothAvrcpController.ACTION_CONNECTION_STATE_CHANGED:
                    //TODO:1.disconnect ???????????????????????????????????????????????????disable
                    Log.e(TAG, "mBtReceiver???BluetoothAvrcpController.ACTION_CONNECTION_STATE_CHANGED");
                    break;
               /* case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED: //A2DP??????????????????
                    Toast.makeText(context, "A2DP??????????????????", Toast.LENGTH_SHORT);
                    break;
                case BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED: //A2DP??????????????????
                    Toast.makeText(context, "A2DP??????????????????", Toast.LENGTH_SHORT);
                    break;*/
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    handler.sendEmptyMessage(UPDATE_BT_STATE);
                    Log.e(TAG, "mBtReceiver???BluetoothAdapter.ACTION_STATE_CHANGED");
                    break;
             /*   case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    handler.sendEmptyMessageDelayed(UPDATE_DEVICE_LIST, 100);
                    Log.e(TAG, "mBtReceiver???BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED");
                    //???????????????????????????????????????
                    break;
                case BluetoothDevice.ACTION_NAME_CHANGED:
                    handler.sendEmptyMessage(UPDATE_DEVICE_LIST);
                    Log.e(TAG, "mBtReceiver??? BluetoothDevice.ACTION_NAME_CHANGED");
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
                    } else {//??????????????????
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
                .setContentIntent(pendingIntent) // ??????PendingIntent
                //.setSmallIcon(R.drawable.ic_launcher) // ??????????????????????????????
                .setContentTitle(MediaPlayerService.class.getSimpleName())
                .setContentText(MediaPlayerService.class.getSimpleName()); // ?????????????????????
        // .setWhen(System.currentTimeMillis()); // ??????????????????????????????
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //????????????8.1??????????????????
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(false);//????????????????????????????????????????????????????????????????????????????????????
            notificationChannel.setShowBadge(false);//??????????????????
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }

        Notification notification = builder.build(); // ??????????????????Notification
        notification.defaults = Notification.DEFAULT_SOUND; //????????????????????????
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
     * ??????????????????????????????
     */

    class MyTask extends AsyncTask<String, Void, ArrayList<MediaItem>> implements com.fxc.ev.mediacenter.MyTask {

        public MyTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "onPreExecute: ??????????????????????????? start" + ContentFragment.printTime());
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
        protected void onCancelled() {// ????????????????????????????????????????????????
            super.onCancelled();
        }

        // ????????????????????????????????????????????????????????????????????? ???????????????????????????// ????????????????????????????????????????????????
        @Override
        protected ArrayList<MediaItem> doInBackground(String... strings) {
            ArrayList<MediaItem> musicItems = new ArrayList<MediaItem>();
            ArrayList<MediaItem> videoItems = new ArrayList<MediaItem>();
            ArrayList<MediaItem> TotalmediaItems = new ArrayList<MediaItem>();
            //???????????????????????????
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
            Log.i(TAG, "doInBackground:??????????????????????????? " + TotalmediaItems.size() + "Time???" + ContentFragment.printTime());
            return TotalmediaItems;
        }

        // ??????????????????????????????????????????????????????????????????UI??????// ????????????????????????????????????UI??????
        @Override
        protected void onPostExecute(ArrayList<MediaItem> result) {
            super.onPostExecute(result);
            MediaItemUtil.allDevicesMediaItems = result;
        }
    }

    private class MyBinder extends IMyAidlInterface.Stub {

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags)   {
            try {
                return super.onTransact(code, data, reply, flags);

            } catch (RuntimeException e) {
                throw e;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return false;

        }

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
            //????????????
            mRemoteCallbackList.beginBroadcast();
            int N = mRemoteCallbackList.getRegisteredCallbackCount();
            Log.i("", "callback2Client N ==" + N);
            for (int i = 0; i < N; i++) {
                try {
                    if (mRemoteCallbackList.getBroadcastItem(i) == null) {
                        continue;
                    }
                    Log.i("", "callback2Client nodeData ==" + playState);
                    //get??????????????????dataCalback???????????????
                    mRemoteCallbackList.getBroadcastItem(i).onStateChange(playState);
                } catch (DeadObjectException e) {
                    if (mRemoteCallbackList.getBroadcastItem(i) != null)
                        mRemoteCallbackList.unregister(mRemoteCallbackList.getBroadcastItem(i));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            //????????????
            mRemoteCallbackList.finishBroadcast();
        }
    }

    public static void callback2ClientContentChange(MediaItem mediaItem) {
        if (mRemoteCallbackList == null || mRemoteCallbackList.getRegisteredCallbackCount() <= 0) {
            return;
        }
        synchronized (mRemoteCallbackList) {
            //????????????
            mRemoteCallbackList.beginBroadcast();
            int N = mRemoteCallbackList.getRegisteredCallbackCount();
            Log.i("", "callback2Client N ==" + N);
            for (int i = 0; i < N; i++) {
                try {
                    if (mRemoteCallbackList.getBroadcastItem(i) == null) {
                        continue;
                    }
                    Log.i("", "callback2Client nodeData ==" + mediaItem.getTitle());
                    //get??????????????????dataCalback???????????????
                    mRemoteCallbackList.getBroadcastItem(i).onContentChange(mediaItem);
                } catch (DeadObjectException e) {
                    if (mRemoteCallbackList.getBroadcastItem(i) != null)
                        mRemoteCallbackList.unregister(mRemoteCallbackList.getBroadcastItem(i));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            //????????????
            mRemoteCallbackList.finishBroadcast();
        }
    }

    public static void callback2ClientCurrentDurationChange(long currentDuration) {
        if (mRemoteCallbackList == null || mRemoteCallbackList.getRegisteredCallbackCount() <= 0) {
            return;
        }
        synchronized (mRemoteCallbackList) {
            //????????????
            mRemoteCallbackList.beginBroadcast();
            int N = mRemoteCallbackList.getRegisteredCallbackCount();
            Log.i("", "callback2Client N ==" + N);
            for (int i = 0; i < N; i++) {
                try {
                    if (mRemoteCallbackList.getBroadcastItem(i) == null) {
                        continue;
                    }
                    Log.i("", "callback2Client nodeData ==" + currentDuration);
                    //get??????????????????dataCalback???????????????
                    mRemoteCallbackList.getBroadcastItem(i).onDurationChange(currentDuration);
                } catch (DeadObjectException e) {
                    if (mRemoteCallbackList.getBroadcastItem(i) != null)
                        mRemoteCallbackList.unregister(mRemoteCallbackList.getBroadcastItem(i));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            //????????????
            mRemoteCallbackList.finishBroadcast();
        }
    }
}
