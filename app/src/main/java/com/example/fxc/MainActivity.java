package com.example.fxc;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.fxc.mediaplayer.CSDMediaPlayer;
import com.example.fxc.mediaplayer.DeviceItem;
import com.example.fxc.mediaplayer.DeviceItemUtil;
import com.example.fxc.mediaplayer.DeviceListAdapter;
import com.example.fxc.mediaplayer.MediaController;
import com.example.fxc.mediaplayer.MediaInfo;
import com.example.fxc.mediaplayer.R;
import com.example.fxc.util.applicationUtils;

import java.util.ArrayList;
import java.util.List;

import static com.example.fxc.mediaplayer.Constants.BLUETOOTH_DEVICE;
import static com.example.fxc.mediaplayer.Constants.USB_DEVICE;
import static com.example.fxc.mediaplayer.DeviceItemUtil.ACTION_DEVICE_CHANGED;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private int playMode = 0;// 0循环播放,1单曲循环
    protected CSDMediaPlayer csdMediaPlayer;
    protected ImageView mPlayModeButton;
    protected ImageView mRandomButton;
    protected ImageView mInputSourceButton;
    private FrameLayout mFrameLayout;
    private View mBtPlayerLayer;

    private static int currPosition = 0;//list的当前选中项的索引值（第一项对应0）
    private boolean randomOpen = false;
    private TabLayout mTabLayout;

    public TabLayout getmTabLayout() {
        return mTabLayout;
    }

    private ViewPager mViewPager;
    private List<String> listTitles;
    private List<Fragment> fragments;
    private int currentTab = 0;
    private MediaInfo mMediaInfo;
    private ListView devicelistview;
    private List<DeviceItem> externalDeviceItems = new ArrayList<DeviceItem>();
    private DeviceListAdapter deviceListAdapter;
    private DeviceItemUtil mDeviceItemUtil;

    private final int UPDATE_DEVICE_LIST = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                //Sandra@20220107 add 更新播放列表
                case UPDATE_DEVICE_LIST:
                    updateDeviceListView();
                    if (externalDeviceItems.size() == 0) {
                        ((ContentFragment) fragments.get(currentTab)).updateMediaList(currentTab, mDeviceItemUtil.getCurrentDevice());
                        //实际currentDeviceInfo内容为空，所以刷新后文件列表为空
                    } else {
                        if (mDeviceItemUtil.getCurrentDevice() != null && mDeviceItemUtil.isDeviceExist(mDeviceItemUtil.getCurrentDevice().getStoragePath())) {//currentDeviceInfo即当前文件列表对应的设备，设备还在，无需更新文件列表
                        } else {//currentDeviceInfo即当前文件列表对应的设备，设备已移除，需更新文件列表
                            ((ContentFragment) fragments.get(currentTab)).updateMediaList(currentTab, externalDeviceItems.get(0));
                            mDeviceItemUtil.setCurrentDevice(externalDeviceItems.get(0));
                            currentTab = 0;
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        applicationUtils.startService(this);
        initCondition();
        setContentView(R.layout.activity_main);
        mDeviceItemUtil = DeviceItemUtil.getInstance(this);
        initView();
        registerReceiver();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        unregisterReceiver();
    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.vp_view);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        csdMediaPlayer = CSDMediaPlayer.getInstance(this);
        mBtPlayerLayer = findViewById(R.id.bt_player);
        mFrameLayout = (FrameLayout) findViewById(R.id.mediaPlayer_csd_container);
        if (csdMediaPlayer.getParent() != null) {//Sandra@20220311 add-->
            ((ViewGroup) csdMediaPlayer.getParent()).removeAllViews();
        }//Sandra@20220311 add to fix bug( The specified child already has a parent. You must call removeView() on the child's parent first..)
        mFrameLayout.addView(csdMediaPlayer);
        if (MediaController.getInstance(this).currentSourceType == BLUETOOTH_DEVICE) {
            mFrameLayout.setVisibility(View.GONE);
        } else {
            mBtPlayerLayer.setVisibility(View.GONE);
        }

        mPlayModeButton = (ImageView) findViewById(R.id.play_mode);
        mRandomButton = (ImageView) findViewById(R.id.random);
        mInputSourceButton = (ImageView) findViewById(R.id.input_source_click_button);
        csdMediaPlayer.getBackButton().setVisibility(View.GONE);

        initTabData();
        TabLayout.Tab tab = mTabLayout.getTabAt(currentTab);
        tab.select();
        //Sandra@20220215 add-->
        //創建設備列表獲取顯示存儲設備信息
        devicelistview = (ListView) findViewById(R.id.input_source_list);
        updateDeviceListView();
        //根據選擇的設備刷新音視頻列表
        devicelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                currentTab = mTabLayout.getSelectedTabPosition();
                Log.i(TAG, "onItemClick: currentTab" + currentTab);
                ((ContentFragment) fragments.get(currentTab)).deviceItemOnClick(currentTab, externalDeviceItems.get(position));
                mDeviceItemUtil.setCurrentDevice(externalDeviceItems.get(position));
                Log.i(TAG, "defaultDeviceindex" + position);
                devicelistview.setVisibility(View.GONE);
                ViewGroup.LayoutParams params = ((ContentFragment) fragments.get(currentTab)).mediaFile_list.getLayoutParams();
                params.height = 1000;
                ((ContentFragment) fragments.get(currentTab)).mediaFile_list.setLayoutParams(params);
                mInputSourceButton.setBackgroundResource(R.drawable.icon_input_source_normal);
            }
        });
        //Sandra@20220215 add<--
    }

    private void initCondition() {
        requestAllPower();
    }

    public void playMusic(int position) {
        mMediaInfo = new MediaInfo(((ContentFragment) fragments.get(currentTab)).mediaItems, mDeviceItemUtil.getCurrentDevice());//Sandra@20220308 add
        currPosition = position; //这个是歌曲在列表中的位置，“上一曲”“下一曲”功能将会用到
        if (((ContentFragment) fragments.get(currentTab)).getUrls() != null && ((ContentFragment) fragments.get(currentTab)).getUrls().size() > 0) {
            csdMediaPlayer.setUp(mMediaInfo, true, currPosition);
            csdMediaPlayer.startPlayLogic();
        }
    }

    private void initTabData() {
        listTitles = new ArrayList<>();
        fragments = new ArrayList<>();
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
                ((ContentFragment) fragments.get(currentTab)).updateMediaList(currentTab, mDeviceItemUtil.getCurrentDevice());
            }
        });
    }

    public void onPlayModeClick(View v) {
        switch (playMode) {
            case 0://列表循环
                playMode = 1;
                mPlayModeButton.setBackgroundResource(R.drawable.icon_repeat_single_active);
                MediaController.getInstance(this).setPlayerState(CSDMediaPlayer.STATE_SINGLE_REPEAT, -1);
                Log.i("main", "Jennifertest7=: " + playMode);
                break;
            case 1://单曲循环
                playMode = 0;
                mPlayModeButton.setBackgroundResource(R.drawable.icon_repeat_normal);
                MediaController.getInstance(this).setPlayerState(CSDMediaPlayer.STATE_ALL_REPEAT, -1);
                Log.i("main", "Jennifertest8=: " + playMode);
                break;

        }
    }

    public void onRandomOpenClick(View v) {
        if (randomOpen == false) {
            randomOpen = true;
            mRandomButton.setBackgroundResource(R.drawable.icon_shuffle_active);
            MediaController.getInstance(this).setPlayerState(CSDMediaPlayer.STATE_RANDOM_OPEN, -1);
        } else {
            randomOpen = false;
            MediaController.getInstance(this).setPlayerState(CSDMediaPlayer.STATE_RANDOM_CLOSE, -1);
            mRandomButton.setBackgroundResource(R.drawable.icon_shuffle_normal);
        }
    }

    public void onInputSourceClick(View v) {
        if (devicelistview.getVisibility() == View.GONE) {
            ViewGroup.LayoutParams params = ((ContentFragment) fragments.get(currentTab)).mediaFile_list.getLayoutParams();
            if (MediaController.getInstance(this).getDevices().size() * 90 < 990) {
                params.height = 1000 - (MediaController.getInstance(this).getDevices().size() * 90);
                ((ContentFragment) fragments.get(currentTab)).mediaFile_list.setLayoutParams(params);
            } else {
                params.height = 0;
            }
            devicelistview.setVisibility(View.VISIBLE);
            mInputSourceButton.setBackgroundResource(R.drawable.icon_collapse_normal);
        } else if (devicelistview.getVisibility() == View.VISIBLE) {
            devicelistview.setVisibility(View.GONE);
            ViewGroup.LayoutParams params = ((ContentFragment) fragments.get(currentTab)).mediaFile_list.getLayoutParams();
            params.height = 1000;
            ((ContentFragment) fragments.get(currentTab)).mediaFile_list.setLayoutParams(params);
            mInputSourceButton.setBackgroundResource(R.drawable.icon_input_source_normal);
        }
    }


    public int getCurrPosition() {
        return currPosition;
    }

    public int getCurrentTab() {
        return currentTab;
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

    public void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter(ACTION_DEVICE_CHANGED);// sd卡被插入，且已经挂载
        registerReceiver(DeviceChangedReceiver, intentFilter);
    }

    public void unregisterReceiver() {
        unregisterReceiver(DeviceChangedReceiver);
    }

    private final BroadcastReceiver DeviceChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive: action22" + action);
            switch (action) {
                case ACTION_DEVICE_CHANGED:
                    handler.sendEmptyMessage(UPDATE_DEVICE_LIST);
                    break;
                default:
                    break;
            }
        }
    };

    public void updateDeviceListView() {
        if (externalDeviceItems != null && externalDeviceItems.size() > 0) {
            externalDeviceItems.clear();
        }
        externalDeviceItems = MediaController.getInstance(this).getDevices();
        deviceListAdapter = new DeviceListAdapter(this, externalDeviceItems, mDeviceItemUtil.getCurrentDevice());
        devicelistview.setAdapter(deviceListAdapter);
        devicelistview.invalidateViews();
    }

    public void setPlayerLayer(int device_Type) {
        switch (device_Type) {
            case BLUETOOTH_DEVICE:
                mFrameLayout.setVisibility(View.GONE);
                mBtPlayerLayer.setVisibility(View.VISIBLE);
                break;
            case USB_DEVICE:
                mFrameLayout.setVisibility(View.VISIBLE);
                mBtPlayerLayer.setVisibility(View.GONE);
                break;
        }
    }

}
