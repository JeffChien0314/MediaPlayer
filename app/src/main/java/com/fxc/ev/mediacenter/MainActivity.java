
package com.fxc.ev.mediacenter;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.adapter.DeviceListAdapter;
import com.fxc.ev.mediacenter.bluetooth.ConnectBlueCallBack;
import com.fxc.ev.mediacenter.bluetooth.ui.BtplayerLayout;
import com.fxc.ev.mediacenter.datastruct.DeviceItem;
import com.fxc.ev.mediacenter.datastruct.MediaInfo;
import com.fxc.ev.mediacenter.datastruct.MediaItem;
import com.fxc.ev.mediacenter.localplayer.CSDMediaPlayer;
import com.fxc.ev.mediacenter.service.MediaPlayerService;
import com.fxc.ev.mediacenter.util.BlurTransformation;
import com.fxc.ev.mediacenter.util.Constants;
import com.fxc.ev.mediacenter.util.DeviceItemUtil;
import com.fxc.ev.mediacenter.util.MediaController;
import com.fxc.ev.mediacenter.util.MediaItemUtil;
import com.fxc.ev.mediacenter.util.applicationUtils;
import java.util.ArrayList;
import java.util.List;

import static com.fxc.ev.mediacenter.util.Constants.BLUETOOTH_DEVICE;
import static com.fxc.ev.mediacenter.util.Constants.USB_DEVICE;
import static com.fxc.ev.mediacenter.util.Constants.cutDownBrowseFunction;
import static com.fxc.ev.mediacenter.util.MediaItemUtil.MUSIC;
import static com.fxc.ev.mediacenter.util.MediaItemUtil.TYPE_MUSIC;
import static com.fxc.ev.mediacenter.util.MediaItemUtil.TYPE_VIDEO;
import static com.fxc.ev.mediacenter.util.MediaItemUtil.VIDEO;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "MainActivity";
    private final int UPDATE_DEVICE_LIST = 1;
    private final int UPDATE_MEDIAITEM = 2;
    private final int UPDATE_BT_STATE = 3;
    private final int CLEAR_MEDIA_LIST_AND_SHOW_OTHER_DEVICE = 4;
    private final int REGULAR_UPDATE =5;

    private int playMode = 0;// 0????????????,1????????????
    protected CSDMediaPlayer mCsdMediaPlayer;
    protected ImageView mPlayModeButton;
    protected ImageView mRandomButton;
    protected ImageView mInputSourceButton;
    protected TextView device_tips;
   // protected RelativeLayout pair_device;
   // private ImageView device_icon;
    private FrameLayout mUsbFrameLayout;
    private FrameLayout mbtFrameLayout;
    private BtplayerLayout mBtPlayerLayer;
    public static int currPosition = 0;//list????????????????????????????????????????????????0???
    private boolean randomOpen = false;
    private TabLayout mTabLayout;

    public TabLayout getmTabLayout() {
        return mTabLayout;
    }
    private CustomViews customViews;
    protected ImageView mAlbum_photo;
    protected ImageView mAlbum_photo_mask;
    private ViewPager mViewPager;
    private List<String> listTitles;
    private List<Fragment> fragments;
    public static int currentTab = 0;
    private MediaInfo mMediaInfo;
    //Sandra@20220428 modify-->
    //protected ListView devicelistview;
    private List<DeviceItem> externalDeviceItems = new ArrayList<DeviceItem>();
   // private DeviceListAdapter deviceListAdapter;
   private DeviceListAdapter deviceListAdapter_popup;
    private ListView devicelistview_popup;
    public  PopupWindow popWindow;
    private int VisiableDeviceCount=8;
    //<--Sandra@20220428 modify
    private DeviceItemUtil mDeviceItemUtil;
    public ArrayList<MediaItem> allDevicesMediaItems = new ArrayList<>();
    private AnimationDrawable ani_gif_Connecting;
    private MyTask myTask = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                //Sandra@20220107 add ??????????????????
                case UPDATE_DEVICE_LIST:
                  //  updateDeviceListData(/*false*/);
                    initPopWindow(mInputSourceButton);
                   // devicelistview.setVisibility(View.VISIBLE);
                    //   getALLMediaItemsOfSpecificDevice();//????????????Loading?????????
                    break;
                case CLEAR_MEDIA_LIST_AND_SHOW_OTHER_DEVICE:
                    //updateDeviceListData(/*false*/);
                    initPopWindow(mInputSourceButton);
                    if (mDeviceItemUtil.getCurrentDevice() != null && mDeviceItemUtil.isDeviceExist(mDeviceItemUtil.getCurrentDevice().getStoragePath())) {//currentDeviceInfo??????????????????????????????????????????????????????????????????????????????
                    } else {//currentDeviceInfo??????????????????????????????????????????????????????????????????????????????
                        // mediaItems.clear();
                        ArrayList<MediaItem> mediaItems2 = new ArrayList<>();
                        ((ContentFragment) fragments.get(currentTab)).updateMediaList(mediaItems2);
                        ((ContentFragment) fragments.get(currentTab)).modifyRelativeUI(mediaItems2);
                        if (externalDeviceItems != null && externalDeviceItems.size() != 0) {
                            Toast.makeText(getApplicationContext(), "?????????????????????????????????????????????????????????", Toast.LENGTH_LONG);
                          //  devicelistview.setVisibility(View.VISIBLE);
                        }
                    }
                    //   getALLMediaItemsOfSpecificDevice();//????????????Loading?????????
                    break;
                case UPDATE_MEDIAITEM:
                    Bundle bundle = message.getData();
                    updateMediaItem(bundle);
                    break;
                case UPDATE_BT_STATE:
                    updateStateButtonImg(message.arg1);
                    break;
                case REGULAR_UPDATE:
                    if (mCsdMediaPlayer.getGSYVideoManager().isPlaying()){
                        if(null==mCsdMediaPlayer) break;
                        try {
                            long progress = mCsdMediaPlayer.getGSYVideoManager()!= null
                                    ? (long) mCsdMediaPlayer.getGSYVideoManager().getCurrentPosition()
                                    : 0;
                            if (MediaPlayerService.isAlive) {
                                MediaPlayerService.callback2ClientCurrentDurationChange(progress);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        handler.sendEmptyMessageDelayed(REGULAR_UPDATE, 1000);
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
        registerReceiver();
        setContentView(R.layout.activity_main);
        mDeviceItemUtil = DeviceItemUtil.getInstance(this);
        initView();
        allDevicesMediaItems.clear();
        // getALLMediaItemsOfSpecificDevice();//????????????Loading?????????
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        if (myTask != null) myTask.cancel(true);
        mBtPlayerLayer.release();
        unregisterReceiver();
    }

    private void initView() {
        customViews = (CustomViews) findViewById(R.id.custom_views);
        mAlbum_photo = (ImageView) findViewById(R.id.main_activity_bg);
        mAlbum_photo_mask = (ImageView) findViewById(R.id.main_activity_bg_mask);
        mViewPager = (ViewPager) findViewById(R.id.vp_view);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mCsdMediaPlayer = CSDMediaPlayer.getInstance(this);
        mCsdMediaPlayer.setContext(this);
        mPlayModeButton = (ImageView) findViewById(R.id.play_mode);
        mRandomButton = (ImageView) findViewById(R.id.random);
        mInputSourceButton = (ImageView) findViewById(R.id.input_source_click_button);
      //  pair_device = (RelativeLayout) findViewById(R.id.pair_device);
       // device_icon = (ImageView) findViewById(R.id.device_icon);
     //   device_icon.setBackgroundResource(R.drawable.icon_pair);
      /*  pair_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            }
        });*/
        device_tips = (TextView) findViewById(R.id.device_tips);
        mCsdMediaPlayer.getBackButton().setVisibility(View.GONE);
        mbtFrameLayout = (FrameLayout) findViewById(R.id.mediaPlayer_bt_view);
        mBtPlayerLayer = new BtplayerLayout(this);
        mbtFrameLayout.addView(mBtPlayerLayer);
        mUsbFrameLayout = (FrameLayout) findViewById(R.id.mediaPlayer_usb_view);
        if (mCsdMediaPlayer.getParent() != null) {//Sandra@20220311 add-->
            ((ViewGroup) mCsdMediaPlayer.getParent()).removeAllViews();
        }//Sandra@20220311 add to fix bug( The specified child already has a parent. You must call removeView() on the child's parent first..)
        mUsbFrameLayout.addView(mCsdMediaPlayer);
       // MediaController.getInstance(this).setCurrentSourceType(Constants.BLUETOOTH_DEVICE);//Sandra ????????????
        if (MediaController.getInstance(this).currentSourceType == BLUETOOTH_DEVICE) {
            mUsbFrameLayout.setVisibility(View.GONE);
            mBtPlayerLayer.showControlBtn();
        } else {
            mBtPlayerLayer.setVisibility(View.GONE);
        }

        initTabData();
        TabLayout.Tab tab = mTabLayout.getTabAt(currentTab);
        tab.select();
        //Sandra@20220215 add-->
        //????????????????????????????????????????????????
       // devicelistview = (ListView) findViewById(R.id.input_source_list);
        MediaInfo mediaInfo = CSDMediaPlayer.getInstance(this).getMediaInfo();
        //Sandra@20220402 Modify for ??????????????????UX-->
        if (mediaInfo != null) {
            mDeviceItemUtil.setCurrentDevice(mediaInfo.getDeviceItem());
            ((ContentFragment) fragments.get(currentTab)).mediaItems = mediaInfo.getMediaItems();
            if (mediaInfo.getMediaItems() != null && mediaInfo.getMediaItems().size() != 0) {
                //Fix ????????????????????????????????????????????????
            } else {
                if (BLUETOOTH_DEVICE == MediaController.getInstance(this).currentSourceType) {

                } else {
                    playMusic(mCsdMediaPlayer.getPlayPosition());
                    device_tips.setText(mediaInfo.getDeviceItem().getDescription());
                }

            }
        } else {
            /*externalDeviceItems = MediaController.getInstance(this).getDevices();
            if (externalDeviceItems.size() != 0) {
                device_tips.setText(R.string.Select);
             //   changeVisibleOfDeviceView(false);
            } else {
                device_tips.setText(R.string.paire);
           //     changeVisibleOfDeviceView(false);
            }*/
        }
        //<--Sandra@20220402 Modify for ??????????????????UX
        updateDeviceListData(/*false*/);
        //??????????????????????????????????????????

        //Sandra@20220215 add<--
    }

    private void initCondition() {
        requestAllPower();
    }

    public void playMusic(int position) {
        mMediaInfo = new MediaInfo(((ContentFragment) fragments.get(currentTab)).mediaItems, mDeviceItemUtil.getCurrentDevice());//Sandra@20220308 add
        currPosition = position; //???????????????????????????????????????????????????????????????????????????????????????
        if (((ContentFragment) fragments.get(currentTab)).getUrls() != null && ((ContentFragment) fragments.get(currentTab)).getUrls().size() > 0) {
            mCsdMediaPlayer.setUp(mMediaInfo, true, currPosition);
            mCsdMediaPlayer.startPlayLogic();
        }
    }

    private void initTabData() {
        listTitles = new ArrayList<>();
        fragments = new ArrayList<>();
        listTitles.add(MUSIC);
        listTitles.add(VIDEO);

        for (int i = 0; i < listTitles.size(); i++) {
            ContentFragment fragment = new ContentFragment(MainActivity.this,listTitles.get(i));
            fragments.add(fragment);
        }
        //mTabLayout.setTabMode(TabLayout.SCROLL_AXIS_HORIZONTAL);//??????tab????????????????????????????????????
        for (int i = 0; i < listTitles.size(); i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(listTitles.get(i)));//??????tab??????
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

            //ViewPager???TabLayout???????????????????????????PageTitle??????Tab???Text
            @Override
            public CharSequence getPageTitle(int position) {
                return listTitles.get(position);
            }
        };
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.setupWithViewPager(mViewPager);//???TabLayout???ViewPager???????????????
        mTabLayout.setTabsFromPagerAdapter(mAdapter);//???Tabs???????????????
        for (int i = 0; i < listTitles.size(); i++) {
            mTabLayout.getTabAt(i).setCustomView(getTabView(i));//??????tab???????????????
        }
        mTabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                currentTab = tab.getPosition();
                if (MediaItemUtil.allDevicesMediaItems.size() != 0) {//??????????????????????????????????????????
                    ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();
                    mediaItems = ((ContentFragment) fragments.get(currentTab)).filterAllMediaItemsOfSpecificDevice(currentTab, mDeviceItemUtil.getCurrentDevice());
                    ((ContentFragment) fragments.get(currentTab)).updateMediaList(mediaItems);
                    ((ContentFragment) fragments.get(currentTab)).modifyRelativeUI(mediaItems);

                } else {//????????????
                    if (mDeviceItemUtil.getCurrentDevice() != null) {
                        getALLMediaItemsOfSpecificDevice(true, mDeviceItemUtil.getCurrentDevice(), currentTab);//???????????????????????????????????????????????????????????????Loading??????
                    }
                }
                changeTabSelect(tab);
                if (mDeviceItemUtil!=null && mDeviceItemUtil.getCurrentDevice()!=null){//Sandra@20220423 add for
                    int lastIndex=-1;
                    if (currentTab==0){
                        lastIndex= mDeviceItemUtil.getCurrentDevice().getLastMusicIndex();
                        if (lastIndex<0){}else {
                            ((ContentFragment) fragments.get(currentTab)).mediaFile_list.setSelectionFromTop(lastIndex, 0);//LastMusicIndex?????????????????????
                        }
                    }else {
                        lastIndex= mDeviceItemUtil.getCurrentDevice().getLastMusicIndex();
                        if (lastIndex<0){}else{
                            ((ContentFragment) fragments.get(currentTab)).mediaFile_list.setSelectionFromTop(lastIndex, 0);//LastMusicIndex?????????????????????
                        }
                    }
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);
                changeTabUnSelect(tab);
            }
        });
    }

    public View getTabView(int position) {
        View tabView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tab_custom_layour, null);
        TextView txt_title = (TextView) tabView.findViewById(R.id.tab_title);
        txt_title.setText(listTitles.get(position));
        return tabView;
    }

    private void changeTabSelect(TabLayout.Tab tab) {
        View view = tab.getCustomView();
        TextView txt_title = (TextView) view.findViewById(R.id.tab_title);
        if (mTabLayout.isEnabled()) {
            txt_title.setBackgroundResource(R.drawable.tab_enable_background);
            int text_color = ContextCompat.getColor(getApplicationContext(), R.color.tab_enable_selected_text_color);
            txt_title.setTextColor(text_color);
        } else {
            txt_title.setBackgroundResource(R.drawable.tab_disable_background);
            int text_color = ContextCompat.getColor(getApplicationContext(), R.color.tab_disable_text_color);
            txt_title.setTextColor(text_color);
        }
    }

    private void changeTabUnSelect(TabLayout.Tab tab) {
        View view = tab.getCustomView();
        TextView txt_title = (TextView) view.findViewById(R.id.tab_title);
        if (mTabLayout.isEnabled()) {
            txt_title.setBackgroundResource(R.drawable.tab_enable_background);
            int text_color = ContextCompat.getColor(getApplicationContext(), R.color.tab_enable_unselect_text_color);
            txt_title.setTextColor(text_color);
        } else {
            txt_title.setBackgroundResource(R.drawable.tab_disable_background);
            int text_color = ContextCompat.getColor(getApplicationContext(), R.color.tab_disable_text_color);
            txt_title.setTextColor(text_color);
        }
    }

    private void setRelativeUIdisable() {
        mTabLayout.setEnabled(false);
        mTabLayout.setClickable(false);
        for (int i = 0; i < listTitles.size(); i++) {
            if (i == currentTab) {
                changeTabSelect(mTabLayout.getTabAt(currentTab));
            } else {
                changeTabUnSelect(mTabLayout.getTabAt(i));
            }
        }
    }

    private void setRelativeUIenable() {
        mTabLayout.setEnabled(true);
        mTabLayout.setClickable(true);
        for (int i = 0; i < listTitles.size(); i++) {
            if (i == currentTab) {
                changeTabSelect(mTabLayout.getTabAt(currentTab));
            } else {
                changeTabUnSelect(mTabLayout.getTabAt(i));
            }
        }
    }

    public void onInputSourceClick(View v) {
        if (device_tips.getText().equals(getString(R.string.paire))) {
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        } else {
            if (device_tips.getVisibility() == View.VISIBLE) {
                mInputSourceButton.setBackgroundResource(R.drawable.icon_collapse_active);
                device_tips.setVisibility(View.INVISIBLE);
                initPopWindow(v);
              //  changeVisibleOfDeviceView(true);
            }
        }
    }

    private void initPopWindow(View v) {//Sandra@20220426 add for ??????????????????UI
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_popup, null, true);
         devicelistview_popup=(ListView) view.findViewById(R.id.input_source_list_popup);
        updateDeviceListData();
        List<DeviceItem> popUPDeviceItems = new ArrayList<DeviceItem>();//Sandra@20220517 add for Fix ????????????????????????????????????UI????????? ListView????????????????????????????????????
        for (int i=0;i<externalDeviceItems.size();i++){
            popUPDeviceItems.add(externalDeviceItems.get(i));
        }
        deviceListAdapter_popup = new DeviceListAdapter(MainActivity.this,  popUPDeviceItems/*, mDeviceItemUtil.getCurrentDevice()*/);
        devicelistview_popup.setAdapter(deviceListAdapter_popup);
        deviceListAdapter_popup.notifyDataSetChanged();
        devicelistview_popup.invalidateViews();
        setListViewHeight(devicelistview_popup);
        //1.????????????PopupWindow???????????????????????????View?????????
         popWindow = new PopupWindow(view,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //popWindow.setAnimationStyle(R.anim.anim_pop);  //??????????????????

        //?????????????????????PopupWindow?????????PopupWindow????????????????????????????????????
        //???????????????????????????????????????PopupWindow????????????????????????????????????????????????
        //PopupWindow????????????????????????????????????????????????????????????????????????????????????
        popWindow.setTouchable(true);
        popWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mInputSourceButton.setBackgroundResource(R.drawable.icon_input_source_bg);
                device_tips.setVisibility(View.VISIBLE);
                return false;
                // ??????????????????true?????????touch??????????????????
                // ????????? PopupWindow???onTouchEvent?????????????????????????????????????????????dismiss
            }
        });
        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));    //android6.0???????????????popWindow???????????????????????????
        popWindow.setFocusable(true);
        popWindow.update();

        //??????popupWindow???????????????????????????????????????View???x??????????????????y???????????????
        if (customViews.isDragUp()){//?????????
            popWindow.showAsDropDown(v, 0, 15);
        }else {//??????
            popWindow.showAsDropDown(v, 0, -185);
        }

        //??????popupWindow?????????????????????
        devicelistview_popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position==externalDeviceItems.size()-1){
                    startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                }else {
                    currentTab = mTabLayout.getSelectedTabPosition();
                    mDeviceItemUtil.setCurrentDevice(externalDeviceItems.get(position));
                    setPlayerLayer(externalDeviceItems.get(position).getType());
                    ((ContentFragment) fragments.get(currentTab)).deviceItemOnClick(currentTab, mDeviceItemUtil.getCurrentDevice(), mConnectBlueCallBack);
                    // updateDeviceListData(/*false*/);
                    if (cutDownBrowseFunction && MediaController.getInstance(MainActivity.this).currentSourceType == USB_DEVICE) {
                        playMusic(0);//TODO:??????????????????LastPosition
                        device_tips.setText(externalDeviceItems.get(position).getDescription());
                changeVisibleOfDeviceView(false);
            }
                    popWindow.dismiss();
        }
            }
        });

    }
    private void setListViewHeight(ListView listView){
        ListAdapter listAdapter = listView.getAdapter(); //??????ListView ??????????????????
        if(listAdapter == null){
            return;
        }

        View itemView = listAdapter.getView(0, null, listView); //?????????????????????
        //??????????????????????????????????????????????????????????????????????????? https://www.jianshu.com/p/dbd6afb2c890????????????
        itemView.measure(0,0);
        int itemHeight = itemView.getMeasuredHeight(); //???????????????
        int itemCount = listAdapter.getCount();//??????????????????
        LinearLayout.LayoutParams layoutParams = null; //???????????????????????????
        if(itemCount <= VisiableDeviceCount){
            layoutParams = new LinearLayout.LayoutParams(420,itemHeight*itemCount);
        }else if(itemCount > VisiableDeviceCount){
            layoutParams = new LinearLayout.LayoutParams(420 ,itemHeight*VisiableDeviceCount);
        }
        layoutParams.setMargins(30,30,30,30);
        listView.setLayoutParams(layoutParams);
    }

    public void changeVisibleOfDeviceView(boolean ifopen) {
        if (ifopen) {
            mInputSourceButton.setBackgroundResource(R.drawable.icon_collapse_active);
            device_tips.setVisibility(View.INVISIBLE);
        } else {
            mInputSourceButton.setBackgroundResource(R.drawable.icon_input_source_normal);//TODO:?????????XML?????????
            device_tips.setVisibility(View.VISIBLE);
        }
    }

    public int getCurrPosition() {
        return currPosition;
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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DeviceItemUtil.ACTION_DEVICE_CHANGED);
        intentFilter.addAction(Constants.ACTION_STATE_CHANGED_BROADCAST);
        intentFilter.addAction(Constants.ACTION_MEDIAITEM_CHANGED_BROADCAST);
        intentFilter.addAction(DeviceItemUtil.ACTION_DEVICE_OF_LIST_LOST);
        registerReceiver(DeviceChangedReceiver, intentFilter);

    }

    public void unregisterReceiver() {
        unregisterReceiver(DeviceChangedReceiver);
    }

    /**
     * ??????????????????
     * ??????????????????
     * MediaItem??????
     */
    private final BroadcastReceiver DeviceChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive: action=" + action);
            switch (action) {
                case DeviceItemUtil.ACTION_DEVICE_CHANGED:
                    handler.sendEmptyMessage(UPDATE_DEVICE_LIST);
                    break;
                case DeviceItemUtil.ACTION_DEVICE_OF_LIST_LOST:
                    handler.sendEmptyMessage(CLEAR_MEDIA_LIST_AND_SHOW_OTHER_DEVICE);
                    break;
                case Constants.ACTION_MEDIAITEM_CHANGED_BROADCAST:
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.MEDIAITEM_CHANGED + "", intent.getParcelableExtra(Constants.MEDIAITEM_CHANGED + ""));
                    bundle.putInt(CSDMediaPlayer.POS_EXTRA, intent.getIntExtra(CSDMediaPlayer.POS_EXTRA, -1));
                    message.setData(bundle);
                    message.what = UPDATE_MEDIAITEM;
                    handler.sendMessage(message);
                    //Sandra@20220511 add for ?????????????????????
                    if (MediaPlayerService.isAlive) {
                        handler.sendEmptyMessage(REGULAR_UPDATE);
                        MediaPlayerService.callback2ClientContentChange(intent.getParcelableExtra(Constants.MEDIAITEM_CHANGED + ""));
                    }
                    break;
                case Constants.ACTION_STATE_CHANGED_BROADCAST:
                    int state = intent.getIntExtra(Constants.PLAYSTATE_CHANGED + "", -1);
                    if (DeviceItemUtil.getInstance(context).getCurrentDevice()!=null && DeviceItemUtil.getInstance(context).getCurrentDevice().getType()==BLUETOOTH_DEVICE){//Sandra@20220520 add fix ??????????????????????????????Random?????????Active?????????
                    Message msg = new Message();
                    msg.arg1 = state;
                    msg.what = UPDATE_BT_STATE;
                    handler.sendMessage(msg);
                    }

                    //Sandra@20220511 add for ?????????????????????
                    if (MediaPlayerService.isAlive) {
                        MediaPlayerService.callback2ClientPlayStateChange(state);
                        if (state==Constants.STATE_PLAY){
                            handler.sendEmptyMessage(REGULAR_UPDATE);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * ??????????????????
     */
    public void updateDeviceListData(/*boolean ifShowLoading*/) {
        if (externalDeviceItems != null && externalDeviceItems.size() > 1) {
            externalDeviceItems.clear();
        }
        externalDeviceItems = MediaController.getInstance(this).getDevices();
        if (externalDeviceItems.size() > 1) {//?????????????????????PairDevice
            if (externalDeviceItems.size() > 2) {
                if (mDeviceItemUtil.getCurrentDevice() != null) {//Sandra@20220411 add ????????????????????????????????????
            externalDeviceItems.remove(mDeviceItemUtil.getDeviceIndex(mDeviceItemUtil.getCurrentDevice()));
            externalDeviceItems.add(0, mDeviceItemUtil.getCurrentDevice());
        }
            }
            setRelativeUIenable();
            if (mDeviceItemUtil.getCurrentDevice() != null) {
                device_tips.setText(mDeviceItemUtil.getCurrentDevice().getDescription());
            } else {
                device_tips.setText(R.string.Select);
                mAlbum_photo.setBackgroundResource(R.drawable.background_portriat);
                mAlbum_photo_mask.setVisibility(View.INVISIBLE);
            }
        } else {
            Log.i(TAG, "updateDeviceListData: setRelativeUIdisable");
            setRelativeUIdisable();
            device_tips.setText(R.string.paire);
            mAlbum_photo.setBackgroundResource(R.drawable.background_portriat);
            mAlbum_photo_mask.setVisibility(View.INVISIBLE);
        }
       /* deviceListAdapter = new DeviceListAdapter(getApplicationContext(), externalDeviceItems*//*, mDeviceItemUtil.getCurrentDevice()*//*);
        devicelistview.setAdapter(deviceListAdapter_popup);
        deviceListAdapter.notifyDataSetChanged();*/
        //   devicelistview.invalidateViews();
    }

    /*???????????????/??????????????????????????????????????????*/
    public void connectAnimationStart(/*int position*/) {
        ImageView Connecting_icon = mInputSourceButton;
        Connecting_icon.setVisibility(View.VISIBLE);
        Connecting_icon.setBackgroundResource(R.drawable.ani_gif_loading);
        ani_gif_Connecting = (AnimationDrawable) Connecting_icon.getBackground();
        ani_gif_Connecting.start();
    }

    /*???????????????/????????????????????????????????????*/
    public void connectAnimationStop(/*int position*/) {
        ImageView Connecting_icon = mInputSourceButton;
        Connecting_icon.setVisibility(View.VISIBLE);
        Connecting_icon.setBackgroundResource(R.drawable.ani_gif_loading);
        ani_gif_Connecting = (AnimationDrawable) Connecting_icon.getBackground();
        ani_gif_Connecting.stop();
        mInputSourceButton.setBackgroundResource(R.drawable.icon_input_source_bg);
    }

    /**
     * ????????????????????????????????????UI??????
     *
     * @param device_Type
     */
    public void setPlayerLayer(int device_Type) {
        Log.i(TAG, "setPlayerLayer: device_Type=" + device_Type);
        switch (device_Type) {
            case BLUETOOTH_DEVICE:
                mUsbFrameLayout.setVisibility(View.GONE);
                mbtFrameLayout.setVisibility(View.VISIBLE);
                mBtPlayerLayer.showControlBtn();
                break;
            case Constants.USB_DEVICE:
                mUsbFrameLayout.setVisibility(View.VISIBLE);
                mbtFrameLayout.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * ??????MediaItem??????????????????UI??????
     *
     * @param bundle
     */
    private void updateMediaItem(Bundle bundle) {
        MediaItem item = (MediaItem) bundle.getParcelable(Constants.MEDIAITEM_CHANGED + "");
        int pos = bundle.getInt(CSDMediaPlayer.POS_EXTRA, -1);
        switch (MediaController.getInstance(MainActivity.this).currentSourceType) {
            case BLUETOOTH_DEVICE:
                mBtPlayerLayer.updateMediaDetail(item);
                break;
            case Constants.USB_DEVICE:
                mAlbum_photo_mask.setVisibility(View.VISIBLE);
                if (mMediaInfo !=null && mMediaInfo.getMediaItems().get(pos).isIfVideo()){
                    mAlbum_photo_mask.setBackgroundResource(R.color.bg_mask_video_playing);//???????????????UI?????????????????????
                    mDeviceItemUtil.getCurrentDevice().setLastVideoIndex(pos);
                }else {
                    BlurTransformation blurTransformation= new BlurTransformation(getApplicationContext());
                    blurTransformation. MediaBlurrAPI(mMediaInfo.getMediaItems().get(pos).getId(),mAlbum_photo);
                    mAlbum_photo_mask.setBackgroundResource(R.color.bg_mask);
                    mDeviceItemUtil.getCurrentDevice().setLastMusicIndex(pos);
                }
                ((ContentFragment) fragments.get(currentTab)).resetAnimation(currPosition);
                if (-1 != pos) {
                    currPosition = pos;
                    if ((currentTab==TYPE_MUSIC && !mMediaInfo.getMediaItems().get(pos).isIfVideo()) ||(currentTab==TYPE_VIDEO && mMediaInfo.getMediaItems().get(pos).isIfVideo())  ){
                        //Sandra@20220423 ??????????????????????????????????????????????????????Video???video?????????Active/??????????????????????????????????????????????????????
                    ((ContentFragment) fragments.get(currentTab)).smoothScrollToPosition(currPosition);
                        //((ContentFragment) fragments.get(currentTab)).playingAnimation(activePosition);
                        // Sandra@20220423 delete ??????mediaFile_list???onScrollChange??????????????????????????????????????????
                    ((ContentFragment) fragments.get(currentTab)).mediaFile_list.post(new Runnable() {
                        @Override
                        public void run() {
                            ((ContentFragment) fragments.get(currentTab)).mediaFile_list.setSelectionFromTop(currPosition, 0);//???????????????item
                        }
                    });
                }
                }
                break;
        }
    }

    /**
     * ???????????????????????????????????????UI
     */
    private void updateStateButtonImg(int state) {
        switch (state) {
            case Constants.STATE_PLAY:
            case Constants.STATE_PAUSE:
                mBtPlayerLayer.updateStateButtonImg(state);
                break;
            case Constants.STATE_RANDOM_CLOSE:
           //     mRandomButton.setBackgroundResource(R.drawable.icon_shuffle_normal);
                mRandomButton.setActivated(false);
                mRandomButton.setBackgroundResource(R.drawable.random_bg);
                break;
            case Constants.STATE_RANDOM_OPEN:
             //   mRandomButton.setBackgroundResource(R.drawable.icon_shuffle_active);
                mRandomButton.setActivated(true);
                mRandomButton.setBackgroundResource(R.drawable.random_bg);
                break;
            case Constants.STATE_SINGLE_REPEAT:
             //   mPlayModeButton.setBackgroundResource(R.drawable.icon_repeat_single_active);
                mPlayModeButton.setActivated(true);
                mPlayModeButton.setBackgroundResource(R.drawable.playmode_one_single_repeat_bg);
                break;
            case Constants.STATE_ALL_REPEAT:
             //   mPlayModeButton.setBackgroundResource(R.drawable.icon_repeat_normal);
                mPlayModeButton.setActivated(false);
                mPlayModeButton.setBackgroundResource(R.drawable.playmode_bg);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int state = Constants.STATE_PLAY;
        switch (v.getId()) {
            case R.id.play_mode:
                switch (playMode) {
                    case 0://????????????
                        playMode = 1;
                        state = Constants.STATE_SINGLE_REPEAT;
                        mPlayModeButton.setBackgroundResource(R.drawable.icon_repeat_single_active);
                        mPlayModeButton.setActivated(true);
                        mPlayModeButton.setBackgroundResource(R.drawable.playmode_one_single_repeat_bg);
                        //   MediaController.getInstance(this).setPlayerState(STATE_SINGLE_REPEAT, -1);
                        Log.i("main", "Jennifertest7=: " + playMode);
                        break;
                    case 1://????????????
                        playMode = 0;
                        state = Constants.STATE_ALL_REPEAT;
                        mPlayModeButton.setBackgroundResource(R.drawable.icon_repeat_active);//Sandra modify
                        mPlayModeButton.setActivated(true);
                        mPlayModeButton.setBackgroundResource(R.drawable.playmode_bg);
                        // MediaController.getInstance(this).setPlayerState(STATE_ALL_REPEAT, -1);
                        Log.i("main", "Jennifertest8=: " + playMode);
                        break;
                }
                break;
            case R.id.random:
                if (randomOpen == false) {
                    randomOpen = true;
                    state = Constants.STATE_RANDOM_OPEN;
                  //  mRandomButton.setBackgroundResource(R.drawable.icon_shuffle_active);
                    mRandomButton.setActivated(true);
                  //  mRandomButton.setBackgroundResource(R.drawable.random_bg);
                    //   MediaController.getInstance(this).setPlayerState(STATE_RANDOM_OPEN, -1);
                } else {
                    randomOpen = false;
                    state = Constants.STATE_RANDOM_CLOSE;
                    // MediaController.getInstance(this).setPlayerState(STATE_RANDOM_CLOSE, -1);
                 //   mRandomButton.setBackgroundResource(R.drawable.icon_shuffle_normal);
                    mRandomButton.setActivated(false);
                 //   mRandomButton.setBackgroundResource(R.drawable.random_bg);
                }
                break;
        }
        MediaController.getInstance(this).setPlayerState(state, -1);
    }

    public void getALLMediaItemsOfSpecificDevice(boolean ifShowLoading, DeviceItem deviceItem, int mediaType) {
        connectAnimationStart();
        allDevicesMediaItems.clear();
        if (myTask != null && !myTask.isCancelled()) {
            myTask.cancel(true);
            myTask = null;
        }

        new MyTask(ifShowLoading, deviceItem, mediaType).execute();
    }

    public void deviceTipsClick(View view) {
        if (device_tips.getText().equals(getString(R.string.paire))) {
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        }
    }

    /**
     * ??????????????????????????????
     */

    class MyTask extends AsyncTask<String, Void, ArrayList<MediaItem>> implements com.fxc.ev.mediacenter.MyTask {
        private boolean ifShowLoading;
        private DeviceItem deviceItem = null;
        private int mediaType = -1;

        public MyTask(boolean ifShowLoading, DeviceItem deviceItem, int mediaType) {
            this.ifShowLoading = ifShowLoading;
            this.deviceItem = deviceItem;
            this.mediaType = mediaType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "onPreExecute: ??????????????????????????? start" + ContentFragment.printTime());
            device_tips.setText(R.string.Connecting);
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
            ArrayList<MediaItem> TotalmediaItems = new ArrayList<MediaItem>();
            if (mediaType != -1 && deviceItem != null) {//???????????????????????????
                if (mediaType == 0) {
                    TotalmediaItems = MediaItemUtil.getMusicInfos(getApplicationContext(), deviceItem.getStoragePath());
                } else {
                    TotalmediaItems = MediaItemUtil.getVideoInfos(getApplicationContext(), deviceItem.getStoragePath());
                }
                return TotalmediaItems;
            }
            return null;
        }

        // ??????????????????????????????????????????????????????????????????UI??????// ????????????????????????????????????UI??????
        @Override
        protected void onPostExecute(ArrayList<MediaItem> result) {
            super.onPostExecute(result);
            if (ifShowLoading) {
                connectAnimationStop(/*mDeviceItemUtil.getDeviceIndex(mDeviceItemUtil.getCurrentDevice())*/);
                //  updateDeviceListData(/*false*/);
                ((ContentFragment) fragments.get(currentTab)).updateMediaList(result);
                ((ContentFragment) fragments.get(currentTab)).modifyRelativeUI(result);
                device_tips.setText(deviceItem.getDescription());
                //changeVisibleOfDeviceView(false);
                ((ContentFragment) fragments.get(currentTab)).mediaItems = result;
                Log.i(TAG, "onPostExecute: //??????????????????????????? " + ContentFragment.printTime());
            }
        }
    }

    protected final ConnectBlueCallBack mConnectBlueCallBack = new ConnectBlueCallBack() {
        @Override
        public void onStartConnect() {
            Log.i(TAG, "onStartConnect: ");
            connectAnimationStart();//Sandra@20220408 add
            device_tips.setText(R.string.Connecting);
            Toast.makeText(getApplicationContext(), "start to connect the buletooth device", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnectSuccess(BluetoothDevice device) {
            Log.i(TAG, "onConnectSuccess: ");
            device_tips.setText(device.getName());
           // updateDeviceListData(/*false*/);
            connectAnimationStop();//Sandra@20220408 add
            mBtPlayerLayer.changeBtnEnable();//Sandra@20220419 add
            Toast.makeText(getApplicationContext(), "Bluetooth device connect successfully", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onConnectFail(BluetoothDevice device, String string) {
            Log.i(TAG, "onConnectFail: ");
            device_tips.setText(R.string.fail);
            connectAnimationStop();//Sandra@20220408 add
            Toast.makeText(getApplicationContext(), "The bluetooth device  is unable to connect", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisConnectSuccess(BluetoothDevice device) {
             Toast.makeText(getApplicationContext(), "Bluetooth device disconnect successfully", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisConnectFail(BluetoothDevice device, String string) {
            Toast.makeText(getApplicationContext(), "Bluetooth device disconnect fail", Toast.LENGTH_SHORT).show();

        }
    };

}
