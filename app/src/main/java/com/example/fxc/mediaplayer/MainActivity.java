package com.example.fxc.mediaplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import permissions.dispatcher.NeedsPermission;

import android.support.v4.app.Fragment;

public class MainActivity extends AppCompatActivity {
    private SimpleAdapter listAdapter;
    private ListView musicListView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestAllPower();
        Log.i("main", "uriChecked2=: ");
        setContentView(R.layout.activity_main);
        list = new ArrayList<HashMap<String, String>>();
        //listAdapter = new SimpleAdapter(MainActivity.this, list, R.layout.music_list, new String[]{"name", "artist"}, new int[]{R.id.songName, R.id.artistName});

        mViewPager = (ViewPager) findViewById(R.id.vp_view);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mPreviousButton = (ImageView) findViewById(R.id.previous);
        mNextButton = (ImageView) findViewById(R.id.next);
        csdMediaPlayer = (CSDMediaPlayer) findViewById(R.id.mediaPlayer_csd);

        csdMediaPlayer.getBackButton().setVisibility(View.INVISIBLE);
        searchMusicFile();//搜索音频文件
        initTabData();
        Log.i("main", "Jennifertest30=: " + csdMediaPlayer.getCurrentState());
    }
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

    //Sandra@20210113 add 搜索视频文件-->
    private void getVideo() {
        ContentResolver contentResolver = getContentResolver();
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DATA,
        };
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, projection, null, null,
                MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        while (cursor.moveToNext()) {
            String id =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
            String title =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
            String duration =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
            String mime_type =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
            String filePath =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            Log.i(TAG, "searchMusicFile: filePath" + cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            HashMap<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("name", title);
            map.put("duration", duration);
            map.put("mime_type", mime_type);
            map.put("data", filePath);
            list.add(map);

            String path = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media.DATA));
            mVideoPaths.add(path);
            Log.i(TAG, "getVideo: path" + path);
        }
        cursor.close();
        System.out.println("Video = " + mVideoPaths.toString());
        cursor.close();
        //搜索完毕之后，发一个message给Handler，对ListView的显示内容进行更新
        Log.i(TAG, "找到" + mVideoPaths.size() + "份視頻频文件");
        Toast.makeText(MainActivity.this, "找到" + mVideoPaths.size() + "份視頻频文件", Toast.LENGTH_LONG).show();

    }

    private List<String> mVideoPaths = new ArrayList<String>();
    //<--Sandra@20210113 add 搜索视频文件

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    private void searchMusicFile() {
//        如果list不是空的，就先清空
        if (!list.isEmpty()) {
            list.clear();
        }
        ContentResolver contentResolver = this.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;  //搜索SD卡里的music文件
        Log.i("main", "searchMusicFile: " + uri);

        String[] projection = {
                MediaStore.Audio.Media._ID,      //根据_ID可以定位歌曲
                MediaStore.Audio.Media.TITLE,   //这个是歌曲名
                MediaStore.Audio.Media.DISPLAY_NAME, //这个是文件名
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.IS_MUSIC,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        String where = MediaStore.Audio.Media.IS_MUSIC + ">0";
        Cursor cursor = contentResolver.query(uri, projection, where, null, MediaStore.Audio.Media.DATA);
        while (cursor.moveToNext()) {
            //将歌曲的信息保存到list中
            String songName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)); //其中，TITLE和ARTIST是用来显示到ListView中的
            String artistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String id = Integer.toString(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));   // _ID和DATA都可以用来播放音乐，其实保存任一个就可以
            String data = Integer.toString(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            String duration = stringForTime(Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))));
                //totaltimeView.setText(toTime(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

            //根据专辑ID获取到专辑封面图
            //     Bitmap thumbBitmap = getAlbumArt(albumId);
            HashMap<String, String> map = new HashMap<>();
            //     map.put("thumbBitmap", thumbBitmap.toString());
            map.put("name", songName);
            map.put("artist", artistName);
            map.put("id", id);
            map.put("data", data);
            map.put("duration", duration);
            list.add(map);
            nameChecked = map.get("name");
            Long idChecked = Long.parseLong(map.get("id"));
            urls.add(new GSYVideoModel((String.valueOf(Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + idChecked))), nameChecked));
            Log.i("main", "Jennifertest3=: " + (String.valueOf(Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + idChecked))));
        }
        //  listRandom=getListRandom(list);
        cursor.close();
        Toast.makeText(getApplicationContext(), "找到" + list.size() + "份文件", Toast.LENGTH_LONG);
        //搜索完毕之后，发一个message给Handler，对ListView的显示内容进行更新
        //   handler.sendEmptyMessage(SEARCH_MUSIC_SUCCESS);
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
        mTabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                GSYVideoManager.releaseAllVideos();
                if (orientationUtils != null)
                    orientationUtils.releaseListener();
                currPosition=-1;
                super.onTabSelected(tab);
                currentTab = tab.getPosition();
                Log.i("main", "Jennifertest20=: " + currentTab);
            }
        });

        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。
        mTabLayout.setTabsFromPagerAdapter(mAdapter);//给Tabs设置适配器

    }
   private String stringForTime(int timeMs){
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
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
