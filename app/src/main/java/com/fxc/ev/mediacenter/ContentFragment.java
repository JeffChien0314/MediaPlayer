package com.fxc.ev.mediacenter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fxc.ev.mediacenter.bt.BtMusicManager;
import com.fxc.ev.mediacenter.bt.ConnectBlueCallBack;
import com.fxc.ev.mediacenter.mediaplayer.CSDMediaPlayer;
import com.fxc.ev.mediacenter.mediaplayer.DeviceItem;
import com.fxc.ev.mediacenter.mediaplayer.DeviceItemUtil;
import com.fxc.ev.mediacenter.mediaplayer.MediaController;
import com.fxc.ev.mediacenter.mediaplayer.MediaInfo;
import com.fxc.ev.mediacenter.mediaplayer.MediaItem;
import com.fxc.ev.mediacenter.mediaplayer.MediaItemUtil;
import com.fxc.ev.mediacenter.mediaplayer.MediaListAdapter;
import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.mediaplayer.Constants;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.security.KeyStore.getApplicationContext;

/**
 * Created by Jennifer on 2022/2/08.
 */
public class ContentFragment extends Fragment {
    String TAG = ContentFragment.class.getSimpleName();
    public ArrayList<MediaItem> mediaItems = new ArrayList<>();
    public MediaListAdapter listAdapter;
    private View view;
    private Context mContext;
    public ListView mediaFile_list;
    private List<GSYVideoModel> urls = new ArrayList<>();
    private AnimationDrawable ani_gif_playing;
    private DeviceItem mDeviceItem;
    private MyTask myTask=null;
    private final ConnectBlueCallBack mConnectBlueCallBack = new ConnectBlueCallBack() {
        @Override
        public void onStartConnect() {
            Log.i(TAG, "onStartConnect: ");
            Toast.makeText(getApplicationContext(), "start to connect the buletooth device", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnectSuccess(BluetoothDevice device) {
            Log.i(TAG, "onConnectSuccess: ");
            Toast.makeText(mContext, "Bluetooth device connect successfully", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnectFail(BluetoothDevice device, String string) {
            Log.i(TAG, "onConnectFail: ");
            Toast.makeText(getApplicationContext(), "The bluetooth device  is unable to connect", Toast.LENGTH_SHORT).show();
        }
    };


    public ContentFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public ContentFragment(Context context) {
        super();
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contentfragment, container, false);
        return view;
    }

    public List<GSYVideoModel> getUrls() {
        for (int i = 0; i < mediaItems.size(); i++) {
            urls.add(mediaItems.get(i).getGsyVideoModel());
        }
        return urls;
    }

    public void setUrls(List<GSYVideoModel> urls) {
        this.urls = urls;
    }

    public void smoothScrollToPosition(int position) {

        mediaFile_list.smoothScrollToPosition(position);
        mediaFile_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.i(TAG, "onScrollStateChanged: ");
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.i(TAG, "onScroll: ");
            }
        });

    }

    public void playingAnimation(int position) {
        if (position >= mediaFile_list.getFirstVisiblePosition() && position <= mediaFile_list.getLastVisiblePosition()) {//范围内可见
            ImageView playing_icon = mediaFile_list.getChildAt(position - mediaFile_list.getFirstVisiblePosition()).findViewById(R.id.playing_icon);
            playing_icon.setVisibility(View.VISIBLE);
            playing_icon.setBackgroundResource(R.drawable.ani_gif_playing);
            ani_gif_playing = (AnimationDrawable) playing_icon.getBackground();
            ani_gif_playing.start();
            TextView totaltime = mediaFile_list.getChildAt(position - mediaFile_list.getFirstVisiblePosition()).findViewById(R.id.totalTime);
            totaltime.setVisibility(View.GONE);
        }
    }

    public void resetAnimation(int lastPosition) {
        try {
            if (lastPosition >= mediaFile_list.getFirstVisiblePosition() && lastPosition <= mediaFile_list.getLastVisiblePosition()) {
                ImageView playing_icon = mediaFile_list.getChildAt(lastPosition - mediaFile_list.getFirstVisiblePosition()).findViewById(R.id.playing_icon);
                playing_icon.setVisibility(View.GONE);
                playing_icon.setAnimation(null);
                TextView totaltime = mediaFile_list.getChildAt(lastPosition - mediaFile_list.getFirstVisiblePosition()).findViewById(R.id.totalTime);
                totaltime.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isVisiable(int position) {
        if (position >= mediaFile_list.getFirstVisiblePosition() && position <= mediaFile_list.getLastVisiblePosition()) {
            return true;
        }
        return false;
    }

    public void deviceItemOnClick(int mediaType, DeviceItem deviceItem) {
        if (deviceItem.getType() == Constants.BLUETOOTH_DEVICE) {
            if (deviceItem.getBluetoothDevice().isConnected()) {
                //展示音乐列表，获取播放状态
            } else {
                try {
                    if (deviceItem.getBluetoothDevice().getBondState() == BluetoothDevice.BOND_NONE) {
                        Method m = BluetoothDevice.class.getMethod("createBond");
                        m.invoke(deviceItem.getBluetoothDevice());
                    } else if (deviceItem.getBluetoothDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
                        BtMusicManager.getInstance().a2dpSinkConnect(deviceItem.getBluetoothDevice(), mConnectBlueCallBack);
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "The bluetooth device  is unable to connect...", Toast.LENGTH_SHORT).show();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "The bluetooth device  is unable to connect...", Toast.LENGTH_SHORT).show();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "The bluetooth device  is unable to connect...", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            mDeviceItem = deviceItem;
            if (myTask != null && !myTask.isCancelled()) {
                myTask.cancel(true);
                myTask = null;
            }
            myTask=new MyTask();
            Log.i(TAG, "deviceItemOnClick: "+    printTime());
            myTask.execute(deviceItem.getStoragePath());

            // mediaItems = MediaController.getInstance(mContext).getMeidaInfosByDevice(deviceItem, mediaType, false).getMediaItems();
            // updateMediaList(mediaType, deviceItem);
        }
    }

    public void updateMediaList(int mediaType, DeviceItem deviceItem) {
        if (null != deviceItem) {
            mDeviceItem = deviceItem;
        }
        mediaItems = MediaController.getInstance(mContext).getMeidaInfosByDevice(mDeviceItem, mediaType, false).getMediaItems();
        listAdapter = new MediaListAdapter(mContext, mediaItems);
        if (mediaFile_list == null) return;
        mediaFile_list.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        if (urls != null && urls.size() != 0) {
            urls.clear();
            for (int i = 0; i < mediaItems.size(); i++) {
                urls.add(mediaItems.get(i).getGsyVideoModel());
            }
        }
    }
    public void updateMediaList2(ArrayList<MediaItem>mediaItems) {
        listAdapter = new MediaListAdapter(mContext, mediaItems);
        if (mediaFile_list == null) return;
        mediaFile_list.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        Log.i(TAG, "updateMediaList2: end"+printTime());
        if (urls != null && urls.size() != 0) {
            urls.clear();
            for (int i = 0; i < mediaItems.size(); i++) {
                urls.add(mediaItems.get(i).getGsyVideoModel());
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        //音視頻列表
        mediaFile_list = (ListView) view.findViewById(R.id.list);
        MediaInfo mMediaInfo = CSDMediaPlayer.getInstance(mContext).getMediaInfo();
        Log.i(TAG, "onResume: CSDMediaPlayer.mInstance.getMediaInfo();");
        if (mMediaInfo != null) {
            if (mMediaInfo.getMediaItems() != null) {
                if (mMediaInfo.getMediaItems().size() > 0) {
                    mDeviceItem = mMediaInfo.getDeviceItem();
                    DeviceItemUtil.getInstance(getApplicationContext()).setCurrentDevice(mDeviceItem);//Sandra@20220324 add
                    mediaItems = mMediaInfo.getMediaItems();
                    updateMediaList2(mediaItems);
                    TabLayout.Tab tab = ((MainActivity) getActivity()).getmTabLayout().getTabAt(0);
                    if (mMediaInfo.getMediaItems().get(0).isIfVideo()) {
                        tab = ((MainActivity) getActivity()).getmTabLayout().getTabAt(1);
                    }
                        tab.select();
                    } else {
                    Log.i(TAG, "onResume: ");
                    }
                }

        }
        listAdapter = new MediaListAdapter(mContext, mediaItems);
        mediaFile_list.setAdapter(listAdapter);
        mediaFile_list.setOnItemClickListener(onItemClickListener);
        mediaFile_list.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.i(TAG, "onScrollChange: currentposition=" + (((MainActivity) getActivity()).getCurrPosition()));
                for (int i = 0; i < mediaItems.size(); i++) {
                    if (isVisiable(i) && (((MainActivity) getActivity()).getCurrPosition()) == i) {
                        playingAnimation((((MainActivity) getActivity()).getCurrPosition()));
                    } else {
                        resetAnimation(i);
                    }
                }

            }
        });
    }

    ListView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            playingAnimation(position);
            if (mDeviceItem!=null){
            MediaController.getInstance(mContext).setCurrentSourceType(mDeviceItem.getType());
            }
            if (MediaController.getInstance(mContext).currentSourceType == Constants.USB_DEVICE) {
                ((MainActivity) getActivity()).playMusic(position);
                DeviceItem deviceItem = DeviceItemUtil.getInstance(mContext).getDeviceByStoragePath(mediaItems.get(position).getStoragePath());
                CSDMediaPlayer.getInstance(mContext).setMediaInfo(new MediaInfo(mediaItems, deviceItem));
                //  ifVideo=CSDMediaPlayer.getInstance(mContext).getMediaInfo().getMediaItems().get(position).isIfVideo();
                Log.i(TAG, "onItemClick: mediaItems" + mediaItems.size());
                //<--Sandra@20220311 add
            } else {//设置蓝牙选中歌曲播放，还有控制Activity的UI设置

                //   MediaController.getInstance(mContext).setPlayerState();
            }
            if (mDeviceItem!=null)
            ((MainActivity) getActivity()).setPlayerLayer(mDeviceItem.getType());
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (CSDMediaPlayer.getInstance(mContext).getMediaInfo()!=null
                && CSDMediaPlayer.getInstance(mContext).getMediaInfo().getMediaItems()!=null
                && CSDMediaPlayer.getInstance(mContext).getMediaInfo().getMediaItems().size()!=0){//Sandra@20220315 add
        if (CSDMediaPlayer.getInstance(mContext).getMediaInfo().getMediaItems().get(0).isIfVideo()) {
            CSDMediaPlayer.getInstance(mContext).onVideoPause();
        }
    }
    }

    @Override
    public void onDestroy() {
        if (myTask!=null) myTask.cancel(true);
        super.onDestroy();
    }
    public void whenTabSelected(String storePath){
        if (myTask != null && !myTask.isCancelled()) {
            myTask.cancel(true);
            myTask=null;
        }
        myTask=new MyTask();
        ArrayList<String> storePaths=new  ArrayList<String>();
        storePaths.add(storePath);
        myTask.execute(storePath);
    }

    /**
     * 异步获取文件列表内容
     */
     class MyTask extends AsyncTask<String, Integer, ArrayList<MediaItem>> {
        @Override
        protected void onPreExecute() {  // 作用：执行 线程任务前的操作
            super.onPreExecute();
            Log.i(TAG, "onPreExecute: "+printTime());
        }
        // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果// 注：必须复写，从而自定义线程任务
        @Override
        protected ArrayList<MediaItem> doInBackground(String... storagePath) {
          //  MediaInfo mediaInfo=MediaController.getInstance(getApplicationContext()).getMeidaInfosByDevice(DeviceItemUtil.getInstance(getApplicationContext()).getCurrentDevice(),currentTab, false);
                     Log.i(TAG, "doInBackground start:  "+ printTime());

            if (MainActivity.currentTab== MediaItemUtil.TYPE_MUSIC){
                mediaItems= MediaItemUtil.getMusicInfos(getContext(),storagePath[0]);
            }else {
                mediaItems= MediaItemUtil.getVideoInfos(getContext(),storagePath[0]);
            }

            if (mediaItems!=null){
                return mediaItems;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progresses) {  // 作用：在主线程 显示线程任务执行的进度

        }

        // 作用：接收线程任务执行结果、将执行结果显示到UI组件// 注：必须复写，从而自定义UI操作
        @Override
        protected void onPostExecute(ArrayList<MediaItem> result) {
     //       Log.i(TAG, "onPostExecute: "+printTime());
            updateMediaList2(result);
          mediaItems=result;
        }


        @Override
        protected void onCancelled() {// 作用：将异步任务设置为：取消状态

        }
    }
    public static String printTime(){
        SimpleDateFormat formatter   =   new   SimpleDateFormat   ("yyyy年MM月dd日   HH:mm:ss:SSS");
        Date curDate =  new Date(System.currentTimeMillis());
        String   str   =   formatter.format(curDate);
     return str;
    }


}