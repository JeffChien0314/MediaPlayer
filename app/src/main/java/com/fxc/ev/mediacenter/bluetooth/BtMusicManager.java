package com.fxc.ev.mediacenter.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAvrcpController;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;

//import android.bluetooth.BluetoothA2dpSink;
//import android.bluetooth.BluetoothAvrcpMediaItemData;
//import android.bluetooth.BluetoothAvrcpMediaItemData;


/**
 * created by
 */
public class BtMusicManager implements IBtMusicManager {
    private static final String TAG = "BtMusicManager";
    private static volatile BtMusicManager sInstance;
    public static final int A2DP_ACTIVE = 2;
    private final int AVRCP_CONTROLLER = 12;
    private final int A2DP_SINK = 11;
    //蓝牙开关状态，搜索蓝牙，配对蓝牙等
    private BluetoothAdapter mBluetoothAdapter;
    //这个类里主要是确定蓝牙音乐是否连接上
    private BluetoothA2dpSink mBluetoothA2dpSink;
    //这个类里主要是维护蓝牙音乐的相关信息更新（ID3）,操作控制蓝牙音乐（播放暂停上一曲下一曲等）
    private BluetoothAvrcpController mAvrcpController;
    private BluetoothDevice mConnectedDevice = null;//蓝牙连接的设备
    private BluetoothA2dp mA2dp;
    private boolean mIsAudioStreamModeOn = false; //系统没有记录蓝牙音乐是否出声状态，需要自己记录，false不可以出声
    private boolean mBtPlayState = false;//蓝牙播放状态是播放还是暂停 true 播放 false暂停,图标控制显示
    private boolean mHaveMediaMetadatacache;//是否有缓存媒体,场景一，当车载播放的不是蓝牙音乐，这个时候蓝牙手机端一直在播放，这个时候切换到bt，MediaMetadata不在更新，只会有进度条的回调
    private boolean isUsedBt = false; //bt是否可用（这里是 a2dp是否连接），bt媒体连上代表可用，bt断开代表不可用
    private Set<BluetoothDevice> mBondedDevices;
    private Context mContext;
    //############################################蓝牙广播回调   start############################################
    private BroadcastReceiver mBtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                Log.i(TAG, "mBtReceiver: action==null");
                return;
            }

            switch (action) {
                case BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED:
                    Log.e(TAG, "mBtReceiver，BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED");
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
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED: //A2DP连接状态改变
                    Toast.makeText(mContext, "A2DP连接状态改变", Toast.LENGTH_SHORT);
                    break;
                case BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED: //A2DP播放状态改变
                    Toast.makeText(mContext, "A2DP播放状态改变", Toast.LENGTH_SHORT);
                    break;

/*
                case BluetoothAvrcpController.ACTION_TRACK_EVENT:
                    Log.i(TAG, "mBtReceiver，BluetoothAvrcpController.ACTION_TRACK_EVENT");
                    mediaInfo(intent);
                    break;
                case BluetoothAvrcpController.ACTION_BROWSE_CONNECTION_STATE_CHANGED:
                    Log.e(TAG, "mBtReceiver，BluetoothAvrcpController.ACTION_BROWSE_CONNECTION_STATE_CHANGED");
                    int curState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
                    if (curState == BluetoothProfile.STATE_CONNECTED) {
                        Log.e(TAG, "acrcpController connect");
                    } else {
//                        手机端断开的时候
                        Log.e(TAG, "acrcpController disconnect");
                    }
                    break;
                case BluetoothAvrcpController.ACTION_BROWSING_EVENT:
                    Log.e(TAG, "mBtReceiver，BluetoothAvrcpController.ACTION_BROWSING_EVENT");
//                    btList(intent.getIntExtra(BluetoothAvrcpController.EXTRA_EVENT_ID, -1));//蓝牙音乐列表
                    break;
                case BluetoothAvrcpController.ACTION_CURRENT_MEDIA_ITEM_CHANGED:
                    Log.e(TAG, "mBtReceiver，BluetoothAvrcpController.ACTION_CURRENT_MEDIA_ITEM_CHANGED");
                    //广播得到媒体信息
                    BluetoothAvrcpMediaItemData mMeidaItemData = intent.getParcelableExtra(BluetoothAvrcpController.EXTRA_MEDIA_ITEM_DATA);
                    Log.i(TAG, "mMeidaItemData: " + mMeidaItemData);
                    break;*/
             /*   case BluetoothAvrcpController.ACTION_PLAYER_SETTING:
                    Log.e(TAG, "mBtReceiver，BluetoothAvrcpController.ACTION_PLAYER_SETTING");
                    break;*/
               /* case BluetoothAvrcpController.ACTION_PLAY_FAILURE:
                    Log.e(TAG, "mBtReceiver，BluetoothAvrcpController.ACTION_PLAY_FAILURE");
                    Toast.makeText(context, "未找到可播放内容,请在手机中选择音乐播放源后重试", Toast.LENGTH_SHORT).show();
                    break;*/
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    Log.e(TAG, "mBtReceiver，BluetoothAdapter.ACTION_STATE_CHANGED");
                    stateChanged(intent);
                    break;
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    Log.e(TAG, "mBtReceiver，BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED");
                    //用这个广播判断蓝牙连接状态
                    btContentStatus(intent);
                    break;
                case BluetoothDevice.ACTION_NAME_CHANGED:
                    Log.e(TAG, "mBtReceiver， BluetoothDevice.ACTION_NAME_CHANGED");
                    checkBtName(intent);
                    break;
            }
        }
    };
    private BluetoothProfile.ServiceListener profileServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            //   Toast.makeText(mContext, "onServiceConnected: profile=" + profile + ",BluetoothProfile=" + proxy, Toast.LENGTH_LONG).show();
            Log.i(TAG, "onServiceConnected: profile=" + profile + ",BluetoothProfile=" + proxy);
            switch (profile) {
                case /*BluetoothProfile.A2DP_SINK*/ A2DP_SINK:
                    mBluetoothA2dpSink = (BluetoothA2dpSink) proxy;
                    Log.e(TAG, "onServiceConnected: mBluetoothA2dpSink=" + mBluetoothA2dpSink);
/*


                    if (mConnectedDevice == null || mBluetoothA2dpSink == null) {
                        Log.e(TAG, "onServiceConnected: mConnectedDevice=" + mConnectedDevice +
                                " , mBluetoothA2dpSink=" + mBluetoothA2dpSink);
                        return;
                    }
*/


                    break;

               /* case BluetoothProfile.A2DP:
                    Toast.makeText(mContext, "a2dp profle connect success", Toast.LENGTH_SHORT).show();
                    if (profile == BluetoothProfile.A2DP) {
                        mA2dp = (BluetoothA2dp) proxy; //转换
                        connectA2DPdevice();
                    }

                    break;*/
                case AVRCP_CONTROLLER:
                    mAvrcpController = (BluetoothAvrcpController) proxy;
                   /* Log.e(TAG, "onServiceConnected: mAvrcpController=" + mAvrcpController);

                    if (mConnectedDevice == null || mAvrcpController == null) {
                        Log.e(TAG, "onServiceConnected: mConnectedDevice=" + mConnectedDevice +
                                " , mAvrcpController=" + mAvrcpController);
                        return;
                    }*/

                    //第一次注册，这种情况播放状态也需要改变
                    //  updataPlayState(true);
                    //  updateMediaMetadata(mAvrcpController.getMetadata(mConnectedDevice));
                    //  updatePlaybackState(mAvrcpController.getPlaybackState(mConnectedDevice));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + profile);
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.i(TAG, "onServiceDisconnected: profile=" + profile);
            switch (profile) {
                case A2DP_SINK://LocalBluetoothProfile.A2DP_SINK:
                    mBluetoothA2dpSink = null;
                    Log.e(TAG, "onServiceDisconnected: mBluetoothA2dpSink为null");
                    break;

                case AVRCP_CONTROLLER://LocalBluetoothProfile.AVRCP_CONTROLLER:
                    mAvrcpController = null;
                    Log.e(TAG, "onServiceDisconnected: mAvrcpController为null");
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + profile);
            }
        }
    };


    private BtMusicManager() {
    }

    public static BtMusicManager getInstance() {
        if (sInstance == null) {
            synchronized (BtMusicManager.class) {
                if (sInstance == null) {
                    sInstance = new BtMusicManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化的时候调用这里
     */
    @Override
    public void initBtData(Context context) {
        if (mContext == null)
            mContext = context;
        initConnectDevice();
        //  registerBtReceiver(context);
        try{
        if (!isEnabled()) return;
        }catch (Exception e){
            Log.i(TAG, "isEnabled: e"+e);
        }
        registerProfile(context);
    }

    /**
     * 获取连接设备的 BluetoothDevice
     *
     * @return
     */
    @Override
    public void initConnectDevice() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBondedDevices = null;
      /*  if (!isEnabled()) return;
        mBondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : mBondedDevices) {
            if (device.isConnected()) {
                mConnectedDevice = device;
                Log.e(TAG, "蓝牙连接上的设备：mConnectedDevice=" + mConnectedDevice);
            }
        } */
    }
//############################################regist 蓝牙相关广播   start############################################

    public Set<BluetoothDevice> getBondedDevices() {
        /*if (isEnabled() && mBondedDevices == null) {
            mBondedDevices = mBluetoothAdapter.getBondedDevices();
        }*/
        mBondedDevices = mBluetoothAdapter.getBondedDevices();
        return mBondedDevices;
    }

    public boolean isEnabled() {
        try{
        if (null == mBluetoothAdapter) return false;
        return mBluetoothAdapter.isEnabled();
        }catch (Exception e){
        }
       return false;
    }

    //############################################regist 蓝牙相关广播   end############################################

    private void registerBtReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED);//A2DP连接状态改变
        intentFilter.addAction(BluetoothA2dpSink.ACTION_PLAYING_STATE_CHANGED);//A2DP播放状态改变
        //  intentFilter.addAction(BluetoothAvrcpController.ACTION_TRACK_EVENT);//监听蓝牙音乐暂停、播放等 系统修改的
        intentFilter.addAction(BluetoothAvrcpController.ACTION_CONNECTION_STATE_CHANGED);//连接状态
        //  intentFilter.addAction(BluetoothAvrcpController.ACTION_BROWSE_CONNECTION_STATE_CHANGED);//浏览  系统修改的
        //   intentFilter.addAction(BluetoothAvrcpController.ACTION_BROWSING_EVENT);// 正在浏览的事件 系统修改的
        //  intentFilter.addAction(BluetoothAvrcpController.ACTION_CURRENT_MEDIA_ITEM_CHANGED);//当前 媒体 项目 改变  系统修改的
//        intentFilter.addAction(BluetoothAvrcpController.ACTION_PLAYER_SETTING);
        //  intentFilter.addAction(BluetoothAvrcpController.ACTION_PLAY_FAILURE);//没有媒体信息
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);

        context.registerReceiver(mBtReceiver, intentFilter);
    }

    public void registerProfile(Context context) {
        if (BluetoothAdapter.getDefaultAdapter().getProfileProxy(context, profileServiceListener, A2DP_SINK/*BluetoothProfile.A2DP_SINK*/)) {
            Log.i(TAG, "registerProfile: A2DP_SINK success");
        } else {
            Log.e(TAG, "registerProfile: A2DP_SINK failed");
        }
        if (BluetoothAdapter.getDefaultAdapter().getProfileProxy(context, profileServiceListener, AVRCP_CONTROLLER/*BluetoothProfile.AVRCP_CONTROLLER*/)) {
            Log.i(TAG, "registerProfile: A2DP_SINK success");
        } else {
            Log.e(TAG, "registerProfile: A2DP_SINK failed");
        }

    }
//############################################unregist 蓝牙相关广播   end############################################

    //############################################unregist 蓝牙相关广播   start############################################
    @Override
    public void unregisterBtReceiver(Context context) {
       /* if (mBtReceiver != null) {
            context.unregisterReceiver(mBtReceiver);
            mBtReceiver = null;
        }*/
    }

    @Override
    public void unRegisterProfile() {
        mBluetoothAdapter.closeProfileProxy(/*BluetoothProfile.A2DP_SINK*/A2DP_SINK, mBluetoothA2dpSink);
        mBluetoothAdapter.closeProfileProxy(AVRCP_CONTROLLER, mAvrcpController);
    }

//############################################蓝牙广播回调   end############################################


    //############################################处理蓝牙广播方法  start############################################


    /**
     * 蓝牙开关状态
     *
     * @param intent
     */
    private void stateChanged(Intent intent) {
        int adapterState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        Log.e(TAG, "stateChanged: ACTION_STATE_CHANGED state=" + adapterState);
        switch (adapterState) {
            case BluetoothAdapter.STATE_ON:
                Log.e(TAG, "手机蓝牙打开");
                // setConnectionState(BT_ENABLE);
//                updataName();//应用运行情况下，点击打开蓝牙开关这里不走，走BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                Log.e(TAG, "手机蓝牙正在打开");
                //setConnectionState(BT_ENABLE);
                break;
            case BluetoothAdapter.STATE_OFF:
                Log.e(TAG, "手机蓝牙关闭");
                //setConnectionState(BT_DISABLED);
                //蓝牙开关断开的时候，不走这里
//                updataName();
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                Log.e(TAG, "手机蓝牙正在关闭");
                break;
        }
    }

    /**
     * 蓝牙a2dp连接状态
     *
     * @param intent
     */
    private void btA2dpContentStatus(Intent intent) {
        int a2dpSinkConnectionState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
        //int previousA2dpSinkConnectionState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1);
        switch (a2dpSinkConnectionState) {
            case BluetoothProfile.STATE_CONNECTED:
                mConnectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "btA2dpContentStatus,蓝牙连接上的设备:" + mConnectedDevice);
                //蓝牙音乐连接上，设置音源为true，里面会自动播放
                setAudioStreamMode(true);
                Log.e(TAG, "STATE_CONNECTED 设置蓝牙为可用状态");
                //   setUsedBt(true);

                //前提:iphone 会先走 adapter 后走 BluetoothA2dpSink
                //     initConnectDevice();
                //todo 蓝牙设备连接上，这个时候可以获取到 mConnectedDevice 可以看是否更新设备名字
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                Log.e(TAG, "STATE_DISCONNECTED 设置蓝牙为不可用状态");
                //  setUsedBt(false);
                //   clearMediaMetadata();
                break;
        }
    }


    /**
     * 蓝牙开关连接状态
     *
     * @param intent
     */
    private void btContentStatus(Intent intent) {
        //BluetoothAdapter.EXTRA_CONNECTION_STATE 连接状态
        int currentContentStatus = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
        //上一次连接状态
//        int previousAdapterConnectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, -1);
        switch (currentContentStatus) {
            case BluetoothAdapter.STATE_DISCONNECTED:
                Log.e(TAG, "蓝牙已经断开连接");
//                clearData();
                //只有蓝牙断开设置才置为null
                mConnectedDevice = null;
//                Log.e(TAG, "蓝牙已经断开连接 mConnectedDevice=" + mConnectedDevice);
               /* clearMediaMetadata();
                // TODO: 2020/11/25 蓝牙断开，一般需要把设备名字更新
                updataPlayState(false);
                //蓝牙断开，蓝牙也不应该发声了
                setAudioStreamMode(false);*/
                break;
            case BluetoothAdapter.STATE_CONNECTING:
                Log.e(TAG, "蓝牙正在连接");
                break;
            case BluetoothAdapter.STATE_CONNECTED:
                Log.e(TAG, "蓝牙已经连接");
                //前提:应用打开，蓝牙设备未连接的时候，打开蓝牙这里会走，需要更新名字
//                initConnectDevice();
//                updataName();
                break;
            case BluetoothAdapter.STATE_DISCONNECTING:
                Log.e(TAG, "蓝牙正在断开连接");
                break;
        }

    }

    /**
     * 检查蓝牙名字，是否更新
     *
     * @param intent
     */
    private void checkBtName(Intent intent) {
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (bluetoothDevice == null) {
            Log.i(TAG, "checkBtName bluetoothDevice为null");
            return;
        }

        if (bluetoothDevice.equals(mConnectedDevice)) {//地址相等则更新名字
            // TODO: 2020/11/25  更新设备名字
        }
    }

    //############################################处理蓝牙广播方法  end############################################


    /**
     * bt是否可用
     *
     * @return isUsedBt true 可用
     */
    @Override
    public boolean isUsedBt() {
        if (!isUsedBt) {
            Log.i(TAG, "bt 不可用");
        }
        return isUsedBt;
    }

    /**
     * 设置bt是否可用状态
     *
     * @param usedBt
     */
    private void setUsedBt(boolean usedBt) {
        isUsedBt = usedBt;
    }


    /**
     * 获取蓝牙播放状态
     *
     * @return
     */
    @Override
    public boolean isPlay() {
        return mBtPlayState;
    }


    /**
     * //     * 初始化 BluetoothProfile.AVRCP_CONTROLLER 回调的时候需要设置一个播放状态为true，
     * //     * 因为后面会走updateMediaMetadata，updatePlaybackState代表蓝牙音乐开始播放了
     * <p>
     * 更新播放状态
     *
     * @param playState
     */
    private void updataPlayState(boolean playState) {

      /*  if (mBtPlayState == playState) {
            Log.i(TAG, "播放状态跟上次播放状态相同则不处理 mBtPlayState=" + mBtPlayState);
            return;
        }
*/
        mBtPlayState = playState;
        Log.e(TAG, "updataPlayState mBtPlayState=" + mBtPlayState);

        //todo 播放状态有改变的时候，需要更新到界面
    }


    /**
     * 获得本地蓝牙适配器的名称
     */
    @Override
    public String getBTAdapterName() {
//        Log.i(TAG, "getBTAdapterName: mBTState=" + mBTState + " mConnectedDevice=" + mConnectedDevice);
        return BluetoothAdapter.getDefaultAdapter().getName();
    }

    /**
     * 获得远端（手机端）已连接的蓝牙设备的名称
     */
    @Override
    public String getBTDeviceName() {
        if (mConnectedDevice == null) {
            return "蓝牙设备未连接";
        }
        Log.i(TAG, "蓝牙设备名字:" + mConnectedDevice.getName());
        if (mConnectedDevice.getName().equals("")) {
            return "蓝牙已连接";
        }

        if (mConnectedDevice.getName().length() <= 8) {
            return mConnectedDevice.getName();
        }
        //如果名字超过6个汉字，以...结尾
        return mConnectedDevice.getName().substring(0, 8) + "...";
    }

    /**
     * 获得远端（手机端）已连接的蓝牙设备的名称
     * <p>
     * window界面
     */
    @Override
    public String getBTDeviceAddress() {
        if (mConnectedDevice == null) {
            return null;
        }
        Log.i(TAG, "蓝牙设备名字:" + mConnectedDevice.getAddress());
        return mConnectedDevice.getAddress();

    }

    public void connect(BluetoothDevice device, ConnectBlueCallBack callBack) {
        //连接之前把扫描关闭
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        new ConnectBlueTask(callBack).execute(device);
    }

    public boolean isA2dpActiveDevice(BluetoothDevice device) {
        if (null != mBluetoothA2dpSink) {
            Log.i(TAG, "isA2dpActiveDevice: mBluetoothA2dpSink.getConnectionState(device)" + mBluetoothA2dpSink.getConnectionState(device));
            return A2DP_ACTIVE == mBluetoothA2dpSink.getConnectionState(device);
        }
        return false;
    }

    public void setA2dpSinkConnect(BluetoothDevice device, boolean connect, ConnectBlueCallBack callBack) {
        //  setPriority(device, 100); //设置priority
        if (null == mBluetoothA2dpSink) return;
        if (connect) {
            callBack.onStartConnect();
            for (BluetoothDevice device1 : mBluetoothA2dpSink.getConnectedDevices()) {
                mBluetoothA2dpSink.disconnect(device1);
            }
            if (mBluetoothA2dpSink.connect(device)) {
                callBack.onConnectSuccess(device);
            } else {
                callBack.onConnectFail(device, "bluetooth device connected fail");
            }
        } else {
            for (BluetoothDevice device1 : mBluetoothA2dpSink.getConnectedDevices()) {
                mBluetoothA2dpSink.disconnect(device1);
            }
        }

        /*try {
            mBluetoothA2dpSink.connect(device);
            //通过反射获取BluetoothA2dp中connect方法（hide的），进行连接。
            Method connectMethod = BluetoothA2dpSink.class.getMethod("connect",
                    BluetoothDevice.class);
           // for ()
            mBluetoothA2dpSink.getConnectedDevices();
            connectMethod.invoke(mBluetoothA2dpSink, device);
            callBack.onConnectSuccess(device);
        } catch (Exception e) {
            e.printStackTrace();
            callBack.onConnectFail(device, "bluetooth device connected fail");
        }*/
    }

    public void stopSource() {
        Intent intent = new Intent();
        intent.setAction("android.bluetooth.IBluetoothA2dp");
        intent.setPackage("com.android.bluetooth");
        intent.putExtra("action", "com.android.bluetooth.btservice.action.STATE_CHANGED");
        intent.putExtra(BluetoothAdapter.EXTRA_STATE, 10);
        mContext.startService(intent);
    }

    public void startSink() {
        Intent intent = new Intent();
        intent.setAction("android.bluetooth.IBluetoothA2dpSink");
        intent.setPackage("com.android.bluetooth");
        intent.putExtra("action", "com.android.bluetooth.btservice.action.STATE_CHANGED");
        intent.putExtra(BluetoothAdapter.EXTRA_STATE, 12);
        mContext.startService(intent);
    }

    /**
     * 更新歌曲基本信息ui
     *
     * @param mediaMetadata
     */
    private void updateMediaMetadata(MediaMetadata mediaMetadata) {

        Log.i(TAG, "updateMediaMetadata，mediaMetadata=" + mediaMetadata);

        //场景一，当前车载不是蓝牙在播放，这个时候蓝牙手机端一直在播放，这个时候源切换到bt，MediaMetadata不在更新，只会有进度条的回调
        if (mediaMetadata != null) {
            mHaveMediaMetadatacache = true;
        }

        Log.e(TAG, "当前源不在蓝牙，or 亿联 拦截方法 updateMediaMetadata");


        if (mConnectedDevice == null) {
            Log.e(TAG, "mConnectedDevice=" + mConnectedDevice);
            return;
        }

        //场景一，当前源不在蓝牙，这个时候蓝牙手机端一直在播放，这个时候源切换到bt，MediaMetadata不在更新，只会有进度条的回调
        /*if (mHaveMediaMetadatacache) {//有缓存的特殊处理
                mediaMetadata = mAvrcpController.getMetadata(mConnectedDevice);
            setAudioStreamMode(true);//不设置这个  实时更新有可能不走
            updataPlayState(true);

            mHaveMediaMetadatacache = false;
            Log.e(TAG, "缓存导致的更新");
        }
*/
        if (mediaMetadata == null) {
            Log.i(TAG, "updateMediaMetadata， mediaMetadata为null");
            return;
        }

//        mSrcManager.switchSrc(SRCManager.APP_BT_MUSIC);

        String title = mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE);
        String artist = mediaMetadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
        String album = mediaMetadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
        String genre = mediaMetadata.getString(MediaMetadata.METADATA_KEY_GENRE);
        long totalTime = mediaMetadata.getLong(MediaMetadata.METADATA_KEY_DURATION); //总时间更新,ms单位
        long currentTrack = mediaMetadata.getLong(MediaMetadata.METADATA_KEY_NUM_TRACKS);
        long totalTrack = mediaMetadata.getLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER);
        Log.e(TAG, "title=" + title + ","
                + "artist=" + artist + ","
                + "album=" + album + ","
                + "genre=" + genre + ","
                + "totalTime=" + totalTime + ","
                + "currentTrack=" + currentTrack + ","
                + "totalTrack=" + totalTrack + ","
        );
    }

    /**
     * 主要用来实时更新当前播放进度
     *
     * @param playbackState
     */
    private void updatePlaybackState(PlaybackState playbackState) {
        Log.i(TAG, "updatePlaybackState，playbackState=" + playbackState);
        if (playbackState == null) {
            return;
        }


        //更新播放状态
        if ((playbackState.getState() == PlaybackState.STATE_PLAYING)
                || (playbackState.getState() == PlaybackState.STATE_FAST_FORWARDING)//快进
                || (playbackState.getState() == PlaybackState.STATE_REWINDING)) {//快退
            updataPlayState(true);
        } else {
            updataPlayState(false);
        }


        long currentTime = playbackState.getPosition();//当前时间，ms为单位
        Log.i(TAG, "currentTime=" + currentTime);
    }


    /**
     * 设置音频焦点
     *
     * @param on
     */
    @Override
    public void setAudioStreamMode(boolean on) {
       /*try {
            boolean ret = mBluetoothAdapter.setAudioStreamMode(on);
            if (ret) {
                mIsAudioStreamModeOn = on;
//            setSrcByBt();
            } else {
                mIsAudioStreamModeOn = false;
            }


            if (mIsAudioStreamModeOn) {
                updataPlayState(true);
            } else {
                updataPlayState(false);
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/


        //    Log.d(TAG, "setAudioStreamMode: mIsAudioStreamModeOn=" + mIsAudioStreamModeOn + ",mBtPlayState=" + mBtPlayState);
    }


    /**
     * 播放与暂停
     */
    @Override
    public void sendPlayPauseCmd(boolean isAvrcpPlayStatus) {
        Log.i(TAG, "sendPlayPauseCmd: mAvrcpController=" + mAvrcpController + " mConnectedDevice=" + mConnectedDevice + ",isAvrcpPlayStatus=" + isAvrcpPlayStatus);
        if (mAvrcpController == null || mConnectedDevice == null) {
            return;
        }

        mBtPlayState = isAvrcpPlayStatus;//将新的状态复值给 mBtPlayState
        Log.e(TAG, " sendPlayPauseCmd mBtPlayState=" + mBtPlayState);

        //  if (isAvrcpPlayStatus) {
          /*  Class<?> clazz = null;
            try {
                clazz = Class.forName("android.bluetooth.BluetoothAvrcpController");
                Method method=clazz.getMethod("sendPassThroughCmd",BluetoothDevice.class,int.class,int.class);
                if(isAvrcpPlayStatus){
                    method.invoke(mAvrcpController,mConnectedDevice,BluetoothAvrcp.PASSTHROUGH_ID_PLAY, BluetoothAvrcp.PASSTHROUGH_STATE_PRESS);
                    method.invoke(mAvrcpController,mConnectedDevice,BluetoothAvrcp.PASSTHROUGH_ID_PLAY, BluetoothAvrcp.PASSTHROUGH_STATE_RELEASE);
                }else {
                    method.invoke(mAvrcpController,mConnectedDevice,BluetoothAvrcp.PASSTHROUGH_ID_PAUSE, BluetoothAvrcp.PASSTHROUGH_STATE_PRESS);
                    method.invoke(mAvrcpController,mConnectedDevice,BluetoothAvrcp.PASSTHROUGH_ID_PAUSE, BluetoothAvrcp.PASSTHROUGH_STATE_RELEASE);
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }*/
     /* if (isAvrcpPlayStatus) {
            mAvrcpController.sendPassThroughCmd(mConnectedDevice, BluetoothAvrcp.PASSTHROUGH_ID_PLAY, BluetoothAvrcp.PASSTHROUGH_STATE_PRESS);

            mAvrcpController.sendPassThroughCmd(mConnectedDevice, BluetoothAvrcp.PASSTHROUGH_ID_PLAY, BluetoothAvrcp.PASSTHROUGH_STATE_RELEASE);
        } else {

            mAvrcpController.sendPassThroughCmd(mConnectedDevice, BluetoothAvrcp.PASSTHROUGH_ID_PAUSE, BluetoothAvrcp.PASSTHROUGH_STATE_PRESS);

            mAvrcpController.sendPassThroughCmd(mConnectedDevice, BluetoothAvrcp.PASSTHROUGH_ID_PAUSE, BluetoothAvrcp.PASSTHROUGH_STATE_RELEASE);
        }*/
        //todo 这里也有播放状态的更新
    }

    /**
     * 上一曲
     */
    @Override
    public void sendPastCmd() {
       /* Log.i(TAG, "sendCmdBack: mAvrcpController=" + mAvrcpController + " mConnectedDevice=" + mConnectedDevice);
        if (mAvrcpController != null && mConnectedDevice != null) {
            mAvrcpController.sendGroupNavigationCmd(mConnectedDevice, BluetoothAvrcp.PASSTHROUGH_ID_BACKWARD, BluetoothAvrcp.PASSTHROUGH_STATE_PRESS);

            mAvrcpController.sendGroupNavigationCmd(mConnectedDevice, BluetoothAvrcp.PASSTHROUGH_ID_BACKWARD, BluetoothAvrcp.PASSTHROUGH_STATE_RELEASE);
        }*/
    }


    /**
     * 下一曲
     */
    @Override
    public void sendNextCmd() {
       /* Log.i(TAG, "sendCmdNext: mAvrcpController=" + mAvrcpController + " mConnectedDevice=" + mConnectedDevice);
        if (mAvrcpController != null && mConnectedDevice != null) {
            mAvrcpController.sendGroupNavigationCmd(mConnectedDevice, BluetoothAvrcp.PASSTHROUGH_ID_FORWARD, BluetoothAvrcp.PASSTHROUGH_STATE_PRESS);

            mAvrcpController.sendGroupNavigationCmd(mConnectedDevice, BluetoothAvrcp.PASSTHROUGH_ID_FORWARD, BluetoothAvrcp.PASSTHROUGH_STATE_RELEASE);
        }*/
    }


    /**
     * 获取当前媒体信息
     *
     * @return
     */
    @Override
    public MediaMetadata getCurrentMediaInfo() {
       /* if (mConnectedDevice == null || mAvrcpController == null) {
            Log.i(TAG, "mConnectedDevice=" + mConnectedDevice + ",mAvrcpController=" + mAvrcpController);
            return null;
        }*/
        // return mAvrcpController.getMetadata(mConnectedDevice);
        return null;
    }


    /**
     * 获取当前进度
     *
     * @return
     */
    @Override
    public long getCurrentProgress() {
      /*  if (mConnectedDevice == null || mAvrcpController == null) {
            Log.i(TAG, "mConnectedDevice=" + mConnectedDevice + ",mAvrcpController=" + mAvrcpController);
            return 0;
        }*/

        //     return mAvrcpController.getPlaybackState(mConnectedDevice) == null ? 0 : mAvrcpController.getPlaybackState(mConnectedDevice).getPosition();

        return 0;

    }

    /**
     * 获取当前媒体总时长
     *
     * @return
     */
    @Override
    public long getCurrentTotleTime() {
       /* if (mConnectedDevice == null || mAvrcpController == null) {
            Log.i(TAG, "mConnectedDevice=" + mConnectedDevice + ",mAvrcpController=" + mAvrcpController);
            return 0;
        }
*/
       /* try {
            return mAvrcpController.getMetadata(mConnectedDevice) == null ? 0
                    : mAvrcpController.getMetadata(mConnectedDevice).getLong(MediaMetadata.METADATA_KEY_DURATION);
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
        return 0;
    }

    /**
     * 清空蓝牙的媒体信息
     */
    private void clearMediaMetadata() {
        mBtPlayState = false;
        Log.e(TAG, "clearMediaMetadata,mBtPlayState =" + mBtPlayState);
    }


    @Override
    public boolean ismBtPlayState() {
        return mBtPlayState;
    }


}
