package com.example.fxc;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.fxc.mediaplayer.CSDMediaPlayer;
import com.example.fxc.mediaplayer.DeviceInfo;
import com.example.fxc.mediaplayer.DeviceListAdapter;
import com.example.fxc.mediaplayer.MediaDeviceManager;
import com.example.fxc.mediaplayer.R;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static int playMode = 0;// 循环播放,1单曲循环
    protected CSDMediaPlayer csdMediaPlayer;
    protected ImageView mPreviousButton;
    protected ImageView mNextButton;
    protected ImageView mPlayModeButton;
    protected ImageView mRandomButton;

    private List<HashMap<String, String>> listRandom = new ArrayList<HashMap<String, String>>();
    private String TAG = "MainActivity";
    private static int currPosition = 0;//list的当前选中项的索引值（第一项对应0）
    private android.os.Bundle outState;
    private  boolean ifVideo=false;
    public int getCurrPosition() {
        return currPosition;
    }
    private boolean randomOpen = false;
    private GSYVideoModel url = new GSYVideoModel("", "");
    private OrientationUtils orientationUtils;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private List<String> listTitles;
    private List<Fragment> fragments;
    private List<TextView> listTextViews;
    public static int currentTab = 0;
    private LinkedList<Integer> randomIndexList=new LinkedList<>();
    private ListView devicelistview;
    private List<DeviceInfo> externalDeviceInfos = new ArrayList<DeviceInfo>();
    private DeviceListAdapter deviceListAdapter;
    private int lastPosition = -1;
    private int random;
    private MediaDeviceManager mMediaDeviceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        if(savedInstanceState != null){
           currentTab = savedInstanceState.getInt("currentTab");
            //可以用Log/Toast输出
            Log.i(TAG, "onCreate: currentTab:"+currentTab);
            MediaDeviceManager.getInstance().setCurrentDevice(savedInstanceState.getParcelable("currentDevice"));
            String description=MediaDeviceManager.getInstance().getCurrentDevice().getDescription();
            Log.i(TAG, "onCreate: description"+description);
        }
        initCondition();
        setContentView(R.layout.activity_main);
        mMediaDeviceManager = MediaDeviceManager.getInstance();
        initView();
        startListen();
    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.vp_view);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mPreviousButton = (ImageView) findViewById(R.id.previous);
        mNextButton = (ImageView) findViewById(R.id.next);
        csdMediaPlayer = (CSDMediaPlayer) findViewById(R.id.mediaPlayer_csd);
        mPlayModeButton = (ImageView) findViewById(R.id.play_mode);
        mRandomButton = (ImageView) findViewById(R.id.random);
        csdMediaPlayer.getBackButton().setVisibility(View.INVISIBLE);
        csdMediaPlayer.setOnAutoCompletionListener((new CSDMediaPlayer.onAutoCompletionListener() {
            @Override
            public void completion() {
                    if (playMode == 0) {
                        next();
                    } else if (playMode == 1) {
                        playMusic(currPosition);
                    }
            }
        }));
        initTabData();
        //Sandra@20220215 add-->
        //創建設備列表獲取顯示存儲設備信息
        devicelistview = (ListView) findViewById(R.id.input_source_list);
        updateDeviceListView();
        //根據選擇的設備刷新音視頻列表
        devicelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                currentTab = mTabLayout.getSelectedTabPosition();
                Log.i(TAG, "onItemClick: currentTab"+currentTab);
                ((ContentFragment) fragments.get(currentTab)).deviceItemOnClick(currentTab, externalDeviceInfos.get(position));
                MediaDeviceManager.getInstance().setCurrentDevice(externalDeviceInfos.get(position));
                Log.i(TAG, "defaultDeviceindex"+position);
            }
        });
        //Sandra@20220215 add<--
    }
    public LinkedList<Integer> getRandom() {
        int random;
        while (randomIndexList.size() < ((ContentFragment) fragments.get(currentTab)).mediaInfos.size()) {
            random = (int) (Math.random() * ((ContentFragment) fragments.get(currentTab)).mediaInfos.size());//生成1到list.size()-1之间的随机数
            if (!randomIndexList.contains(random)) {
                randomIndexList.add(random);
            }
        }
        Log.i("main", "Jennifertest90=: " +randomIndexList);
        return randomIndexList;
    }

    private void initCondition() {
        requestAllPower();
    }

    //Sandra@20220215 add-->

    //Sandra@20220215 add
    public void playMusic(int position) {
        currPosition = position; //这个是歌曲在列表中的位置，“上一曲”“下一曲”功能将会用到
        csdMediaPlayer.setUp(((ContentFragment) fragments.get(currentTab)).getUrls(), true, currPosition);
        csdMediaPlayer.startPlayLogic();
        if(((ContentFragment) fragments.get(currentTab)).mediaInfos.get(currPosition).isIfVideo()){
            ifVideo=true;
        }else {
            ifVideo=false;
        }
    }

    private void initTabData() {
        listTitles = new ArrayList<>();
        fragments = new ArrayList<>();
        listTextViews = new ArrayList<>();

        listTitles.add("Music");
        listTitles.add("Video");

        for (int i = 0; i < listTitles.size(); i++) {
            ContentFragment fragment = new ContentFragment(MainActivity.this);
            fragments.add(fragment);
        }
        //mTabLayout.setTabMode(TabLayout.SCROLL_AXIS_HORIZONTAL);//设置tab模式，当前为系统默认模式
        for (int i = 0; i < listTitles.size(); i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(listTitles.get(i)));//添加tab选项
        }
        FragmentPagerAdapter mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            //ViewPager与TabLayout绑定后，这里获取到PageTitle就是Tab的Text
            @Override
            public CharSequence getPageTitle(int position) {
                return listTitles.get(position);
            }
        };
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。
        mTabLayout.setTabsFromPagerAdapter(mAdapter);//给Tabs设置适配器
        mTabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                currentTab = tab.getPosition();
                ((ContentFragment) fragments.get(currentTab)).updateMediaList(currentTab, mMediaDeviceManager.getCurrentDevice());
                Log.i("main", "Jennifertest20=: " + currentTab);
            }
        });

    }


    public void onPreviousClick(View v) {
        previous();
    }

    private void previous() {
        if(randomOpen==false){
            ((ContentFragment) fragments.get(currentTab)).resetAnimation(currPosition);
            if (((ContentFragment) fragments.get(currentTab)).mediaInfos.size() > 0) {
                if (currPosition > 0) {
                    currPosition--;
                } else {
                    currPosition = ((ContentFragment) fragments.get(currentTab)).mediaInfos.size() - 1;
                }
            }
        }else{
            int i;
            for (i=randomIndexList.size()-1; i>0; i--) {
                if (currPosition == randomIndexList.get(i)) {
                    currPosition = randomIndexList.get(i-1);
                    break;
                } else if (currPosition == randomIndexList.get(0)) {
                    currPosition = randomIndexList.get(randomIndexList.size()-1);
                    i =randomIndexList.size()-1;
                    break;
                }
            }
        }
        ((ContentFragment) fragments.get(currentTab)).smoothScrollToPosition(currPosition);
        playMusic(currPosition);
        ((ContentFragment) fragments.get(currentTab)).playingAnimation(currPosition);
    }


    public void onNextClick(View v) {
        next();
    }

    public void next() {
        if (randomOpen == false) {
            ((ContentFragment) fragments.get(currentTab)).resetAnimation(currPosition);
            if (((ContentFragment) fragments.get(currentTab)).mediaInfos.size() > 0) {
                if (currPosition < ((ContentFragment) fragments.get(currentTab)).mediaInfos.size() - 1) {
                    currPosition++;
                } else {
                    currPosition = 0;
                }
            }
        } else {
            int i;
            ((ContentFragment) fragments.get(currentTab)).resetAnimation(currPosition);
            for (i = 0; i < randomIndexList.size() - 1; i++) {
                if (currPosition == randomIndexList.get(i)) {
                    currPosition = randomIndexList.get(i + 1);
                    break;
                } else if (currPosition == randomIndexList.get(randomIndexList.size() - 1)) {
                    currPosition = randomIndexList.get(0);
                    i = 0;
                    break;
                }
            }
        }
        ((ContentFragment) fragments.get(currentTab)).smoothScrollToPosition(currPosition);
        playMusic(currPosition);
        ((ContentFragment) fragments.get(currentTab)).playingAnimation(currPosition);
    }
    public void onPlayModeClick(View v) {
        switch (playMode) {
            case 0:
                playMode = 1;
                mPlayModeButton.setBackgroundResource(R.drawable.icon_repeat_single_active);
                Log.i("main", "Jennifertest7=: " + playMode);
                break;
            case 1:
                playMode = 0;
                mPlayModeButton.setBackgroundResource(R.drawable.icon_repeat_normal);
                Log.i("main", "Jennifertest8=: " + playMode);
                break;

        }
    }


    public void onRandomOpenClick(View v) {
        if (randomOpen == false) {
            randomOpen = true;
            getRandom();
            mRandomButton.setBackgroundResource(R.drawable.icon_shuffle_active);
        } else{
            randomOpen = false;
            if (randomIndexList.size() > 0) {
                randomIndexList.clear();
            }
            mRandomButton.setBackgroundResource(R.drawable.icon_shuffle_normal);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
            if(ifVideo){//這個判斷條件需要優化，以防越界--儅播放音樂又點擊了另一個Tab
                csdMediaPlayer.onVideoPause();
            }
        Log.i(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
        outState=new Bundle();
        outState.putInt("currentTab",currentTab);
        outState.putParcelable("currentDevice",MediaDeviceManager.getInstance().getCurrentDevice());
        onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        unregisterReceiver();
    }
    //调用（a）
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //定义变量的tempData可以设置为文本框

        Log.i(TAG, "onSaveInstanceState: ");
    }
    @Override
    public  void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        currentTab = savedInstanceState.getInt("currentTab");
        MediaDeviceManager.getInstance().setCurrentDevice(savedInstanceState.getParcelable("currentDevice"));
        Log.i(TAG, "onRestoreInstanceState: ");
    }
    public void requestAllPower() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }
    public void startListen() {
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
        registerReceiver(broadcastRec, intentFilter);
    }
    public void unregisterReceiver() {
        unregisterReceiver(broadcastRec);
    }
private final BroadcastReceiver broadcastRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case Intent.ACTION_MEDIA_EJECT:
                case Intent.ACTION_MEDIA_UNMOUNTED:
                case Intent.ACTION_MEDIA_BAD_REMOVAL:
                    handler.sendEmptyMessageDelayed(UPDATE_DEVICE_LIST,500);
                    //如果列表是当前移除的设备的，需要刷新音视频列表
                    break;
                case Intent.ACTION_MEDIA_CHECKING:
                case Intent.ACTION_MEDIA_MOUNTED:// sd卡被插入，且已经挂载
                    handler.sendEmptyMessageDelayed(UPDATE_DEVICE_LIST,500);
                    break;
                case Intent.ACTION_MEDIA_SCANNER_STARTED:
                    break;
                case Intent.ACTION_MEDIA_SCANNER_FINISHED:
                    //这个地方可以判断可已抓取U盘内部的文件
                    break;
               default:
                    break;
            }
        }
    };
    private final int UPDATE_DEVICE_LIST =0 ;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                //Sandra@20220107 add 更新播放列表
                case UPDATE_DEVICE_LIST:
                    updateDeviceListView();
                    break;
                default:
                    break;
            }
        }
    };

    public void updateDeviceListView(){
        if (externalDeviceInfos !=null && externalDeviceInfos.size()>0){
            externalDeviceInfos.clear();
        }
        externalDeviceInfos = mMediaDeviceManager.getExternalDeviceInfoList(this);
        deviceListAdapter = new DeviceListAdapter(this, externalDeviceInfos);
        devicelistview.setAdapter(deviceListAdapter);
        devicelistview.invalidateViews();
    }

}
