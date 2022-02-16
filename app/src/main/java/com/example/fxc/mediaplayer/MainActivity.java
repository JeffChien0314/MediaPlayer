package com.example.fxc.mediaplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fxc.mediaplayer.R;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.ListGSYVideoPlayer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import permissions.dispatcher.NeedsPermission;

import android.support.v4.app.Fragment;

public class MainActivity extends AppCompatActivity {
    private List<HashMap<String, String>> list;
    private List<HashMap<String, String>> listRandom = new ArrayList<HashMap<String, String>>();
    private String TAG = "MainActivity";
    //private TextView nameView;// 页面中用来显示当前选中音乐名的TextViewl
    protected ListGSYVideoPlayer csdMediaPlayer;
    private String nameChecked;//当前选中的音乐名
    private Uri uriChecked;//当前选中的音乐对应的Uri
    private int currPosition = 0;//list的当前选中项的索引值（第一项对应0）
    private int currState = -1;//当前播放器的状态

    //正常
    public static final int CURRENT_STATE_NORMAL = 0;
    //准备中
    public static final int CURRENT_STATE_PREPAREING = 1;
    //播放中
    public static final int CURRENT_STATE_PLAYING = 2;
    //开始缓冲
    public static final int CURRENT_STATE_PLAYING_BUFFERING_START = 3;
    //暂停
    public static final int CURRENT_STATE_PAUSE = 5;
    //自动播放结束
    public static final int CURRENT_STATE_AUTO_COMPLETE = 6;
    //错误状态
    public static final int CURRENT_STATE_ERROR = 7;
    private static int playMode = 0;// 顺序播放，1单曲循环，2循环播放
    public static boolean randomOpen = false;
    List<GSYVideoModel> urls = new ArrayList<>();
    protected ImageView mPreviousButton;
    protected ImageView mNextButton;
    OrientationUtils orientationUtils;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private LayoutInflater mInflater;
    private List<String> mTitleList = new ArrayList<>();//页卡标题集合
    private View view1, view2, view3, view4, view5;//页卡视图
    private List<View> mViewList = new ArrayList<>();//页卡视图集合
    private List<String> listTitles;
    private List<Fragment> fragments;
    private List<TextView> listTextViews;
    private int currentTab = -1;
    //Sandra@20220215
    private ListView devicelistview;
    private List<ExternalDeviceInfo> externalDeviceInfos =new ArrayList<ExternalDeviceInfo>();
    private DeviceListAdapter deviceListAdapter;

    private String currentStoragePath ="";

    public String getCurrentStoragePath() {
        return currentStoragePath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestAllPower();
        Log.i("main", "uriChecked2=: ");
        setContentView(R.layout.activity_main);
        list = new ArrayList<HashMap<String, String>>();
        mViewPager = (ViewPager) findViewById(R.id.vp_view);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mPreviousButton = (ImageView) findViewById(R.id.previous);
        mNextButton = (ImageView) findViewById(R.id.next);
        csdMediaPlayer = (CSDMediaPlayer) findViewById(R.id.mediaPlayer_csd);

        csdMediaPlayer.getBackButton().setVisibility(View.INVISIBLE);
        // searchMusicFile();//搜索音频文件

        initTabData();
        Log.i("main", "Jennifertest30=: " + csdMediaPlayer.getCurrentState());
        //Sandra@20220215 add-->
        //創建設備列表獲取顯示存儲設備信息
        devicelistview=(ListView) findViewById(R.id.input_source_list);
        externalDeviceInfos= getExternalDeviceInfoList();
        if (externalDeviceInfos!=null && externalDeviceInfos.size()>0){
            currentStoragePath =externalDeviceInfos.get(0).getStoragePath();
        }
        deviceListAdapter=new DeviceListAdapter(this,externalDeviceInfos);
        devicelistview.setAdapter(deviceListAdapter);
        //根據選擇的設備刷新音視頻列表
        devicelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                currentStoragePath =externalDeviceInfos.get(position).getStoragePath();
                currentTab = mTabLayout.getSelectedTabPosition();
                if (currentTab==0){
                    ((ContentFragment)fragments.get(currentTab)).updateMusic(currentStoragePath);
                }else if (currentTab==1){
                    ((ContentFragment)fragments.get(currentTab)).updateVideo(currentStoragePath);
                }
            }
        });

        //Sandra@20220215 add<--
    }

    //Sandra@20220215 add-->
    private StorageManager mStorageManager;
    List<StorageVolume> volumes;
    /**
     * 获取所有外置存储器的目录
     *
     * @return
     */
    List<ExternalDeviceInfo> getExternalDeviceInfoList() {
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        //获取所有挂载的设备（内部sd卡、外部sd卡、挂载的U盘）
        volumes = mStorageManager.getStorageVolumes();
        try {
            Class<?> storageVolumeClazz = Class
                    .forName("android.os.storage.StorageVolume");
            //通过反射调用系统hide的方法
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            for (int i = 0; i < volumes.size(); i++) {
                StorageVolume storageVolume = volumes.get(i);//获取每个挂
                // 载的StorageVolume
                //通过反射调用getPath、isRemovable

                String storagePath = (String) getPath.invoke(storageVolume); //获取路径
                boolean isRemovableResult = (boolean) isRemovable.invoke(storageVolume);//是否可移除
                String description = storageVolume.getDescription(this);

                Log.d("jason", " i=" + i + " ,storagePath=" + storagePath
                        + " ,isRemovableResult=" + isRemovableResult + " ,description=" + description);
                ExternalDeviceInfo externalDeviceInfo= new  ExternalDeviceInfo();
                //   if (isRemovableResult){//Sandra@20220210 剔除内部存储
                externalDeviceInfo.setStoragePath(storagePath);
                externalDeviceInfo.setRemovableResult(isRemovableResult);
                externalDeviceInfo.setDescription(description);
                externalDeviceInfo.
                        setResImage(R.drawable.icon_usb);//此處設置設備圖標icon_usb/icon_bt
                externalDeviceInfos.add(externalDeviceInfo);
                //  }

            }
        } catch (Exception e) {
            Log.d("jason", " e:" + e);
        }
        return externalDeviceInfos;
    }

    //Sandra@20220215 add
    public void playMusic(int position){
        HashMap<String, String> map = list.get(position);
        Long idChecked = Long.parseLong(map.get("id"));
        //uriChecked:选中的歌曲相对应的Uri
        uriChecked = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + nameChecked);
        Log.i("main", "uriChecked=: " + uriChecked);
        Log.i("main", "uriChecked=: ");
        //randomOpen=true;

        //      nameView.setText(nameChecked);
        currPosition = position; //这个是歌曲在列表中的位置，“上一曲”“下一曲”功能将会用到
        Log.i("main", "Jennifertest4=: " + currPosition);
        csdMediaPlayer.setUp(urls, true, currPosition);
        if (playMode == 1) {
            csdMediaPlayer.setLooping(true);
        }

        csdMediaPlayer.startPlayLogic();

        //   csdMediaPlayer. prepareVideo();
    }

    private void initTabData() {
        listTitles = new ArrayList<>();
        fragments = new ArrayList<>();
        listTextViews = new ArrayList<>();


        listTitles.add("Music");
        listTitles.add("Video");

        for (int i = 0; i < listTitles.size(); i++) {
            ContentFragment fragment = new ContentFragment(MainActivity.this, list);
            fragments.add(fragment);
        }
        //mTabLayout.setTabMode(TabLayout.SCROLL_AXIS_HORIZONTAL);//设置tab模式，当前为系统默认模式
        for (int i=0;i<listTitles.size();i++){
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
                GSYVideoManager.releaseAllVideos();
                if (orientationUtils != null)
                    orientationUtils.releaseListener();
                currPosition=-1;
                super.onTabSelected(tab);
                currentTab = tab.getPosition();
                if (currentTab ==0){//音樂
                    ((ContentFragment)fragments.get(currentTab)).updateMusic(currentStoragePath);
                }else if (currentTab ==1){//視頻
                    ((ContentFragment)fragments.get(currentTab)).updateVideo(currentStoragePath);
                }else {

                }
                Log.i("main", "Jennifertest20=: " + currentTab);
            }
        });

    }


    public void onPreviousClick(View v) {
        previous();
    }

    private void previous() {
        currState = csdMediaPlayer.getCurrentState();
        Log.i("main", "Jennifertest1=: " + currPosition);
        if (list.size() > 0) {
            if (currPosition > 0) {
                currPosition--;
                switch (currState) {
                    case CURRENT_STATE_NORMAL:
                        ((ContentFragment)fragments.get(currentTab)).smoothScrollToPosition(currPosition);
                        break;
                    case CURRENT_STATE_AUTO_COMPLETE:
                    case CURRENT_STATE_PLAYING:
                    case CURRENT_STATE_PAUSE:
                        ((ContentFragment)fragments.get(currentTab)).smoothScrollToPosition(currPosition);
                        csdMediaPlayer.setUp(urls, true, currPosition);
                        csdMediaPlayer.startPlayLogic();
                        break;
                }
            } else {
                currPosition = list.size() - 1;
                switch (currState) {
                    case CURRENT_STATE_NORMAL:
                        ((ContentFragment)fragments.get(currentTab)).smoothScrollToPosition(currPosition);
                        break;
                    case CURRENT_STATE_AUTO_COMPLETE:
                    case CURRENT_STATE_PLAYING:
                    case CURRENT_STATE_PAUSE:
                        ((ContentFragment)fragments.get(currentTab)).smoothScrollToPosition(currPosition);
                        csdMediaPlayer.setUp(urls, true, currPosition);
                        csdMediaPlayer.startPlayLogic();
                        break;
                }
            }
        }
    }

    public void onNextClick(View v) {
        next();
    }

    private void next() {
        currState = csdMediaPlayer.getCurrentState();
        Log.i("main", "Jennifertest2=: " + currPosition);
        if (list.size() > 0) {
            if (currPosition < list.size() - 1) {
                currPosition++;
                switch (currState) {
                    case CURRENT_STATE_NORMAL:
                        ((ContentFragment)fragments.get(currentTab)).smoothScrollToPosition(currPosition);
                        break;
                    case CURRENT_STATE_AUTO_COMPLETE:
                    case CURRENT_STATE_PLAYING:
                    case CURRENT_STATE_PAUSE:
                        ((ContentFragment)fragments.get(0)).smoothScrollToPosition(currPosition);
                        csdMediaPlayer.setUp(urls, true, currPosition);
                        csdMediaPlayer.startPlayLogic();
                }
            } else {
                currPosition = 0;
                switch (currState) {
                    case CURRENT_STATE_NORMAL:
                        ((ContentFragment)fragments.get(currentTab)).smoothScrollToPosition(currPosition);
                        break;
                    case CURRENT_STATE_AUTO_COMPLETE:
                    case CURRENT_STATE_PLAYING:
                    case CURRENT_STATE_PAUSE:
                        ((ContentFragment)fragments.get(currentTab)).smoothScrollToPosition(currPosition);
                        csdMediaPlayer.setUp(urls, true, currPosition);
                        csdMediaPlayer.startPlayLogic();
                }
            }
        }
    }

    public void onPlayModeClick(View v) {
        if (randomOpen == false) {
            switch (playMode) {
                case -1:
                    playMode = 0;
                    csdMediaPlayer.setLooping(false);
                    Log.i("main", "Jennifertest7=: " + playMode);
                    break;
                case 0:
                    playMode = 1;
                    csdMediaPlayer.setLooping(true);
                    Log.i("main", "Jennifertest7=: " + playMode);
                    break;
                case 1:
                    playMode = 2;
                    csdMediaPlayer.setLooping(false);
                    Log.i("main", "Jennifertest8=: " + playMode);
                    break;
                case 2:
                    playMode = -1;
                    csdMediaPlayer.setLooping(false);
                    break;
            }
        }
    }

    public void onRandomOpenClick(View v) {
        if (playMode == -1) {
            if (randomOpen == false) {
                csdMediaPlayer.setLooping(false);
                randomOpen = true;
            } else if (randomOpen == false) {
                randomOpen = false;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        csdMediaPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        csdMediaPlayer.onVideoResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    List mylist = new ArrayList();

    public void getRandom(List<HashMap<String, String>> list) {
        int random;
        if (listRandom.size() != 0) {
            listRandom.clear();
        }
        if (mylist.size() > 0) {
            mylist.clear();
        }
        while (mylist.size() < list.size()) {
            random = (int) (Math.random() * list.size());//生成1到list.size()-1之间的随机数
            if (!mylist.contains(random + "")) {
                mylist.add(random + "");  //往集合里面添加数据。
                listRandom.add(list.get(random));
                Log.i(TAG, "getRandom: name: " + listRandom.size() + ":" + list.get(random).get("name") + "\nID:" + list.get(random).get("id") + "\nartist:" + list.get(random).get("artist"));
            }
        }
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
}
