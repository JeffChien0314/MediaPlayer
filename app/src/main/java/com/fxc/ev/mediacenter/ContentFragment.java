package com.fxc.ev.mediacenter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
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

import com.fxc.ev.mediacenter.bluetooth.BtMusicManager;
import com.fxc.ev.mediacenter.bluetooth.ConnectBlueCallBack;
import com.fxc.ev.mediacenter.localplayer.CSDMediaPlayer;
import com.fxc.ev.mediacenter.datastruct.DeviceItem;
import com.fxc.ev.mediacenter.util.DeviceItemUtil;
import com.fxc.ev.mediacenter.util.MediaController;
import com.fxc.ev.mediacenter.datastruct.MediaInfo;
import com.fxc.ev.mediacenter.datastruct.MediaItem;
import com.fxc.ev.mediacenter.util.MediaItemUtil;
import com.fxc.ev.mediacenter.adapter.MediaListAdapter;
import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.util.Constants;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.security.KeyStore.getApplicationContext;
import static com.fxc.ev.mediacenter.util.MediaItemUtil.TYPE_MUSIC;
import static com.fxc.ev.mediacenter.util.MediaItemUtil.TYPE_VIDEO;

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
            if (MediaItemUtil.getAllDevicesMediaItems().size() != 0) {//搜索全部执行完毕，可以去筛选
                mediaItems = getSpecifiedMediaItems(mediaType, deviceItem);
                updateMediaList(mediaItems);
            } else {
                updateMediaList(MediaItemUtil.getAllDevicesMediaItems());
                ((MainActivity) getActivity()).getALLMediaItems(true, deviceItem, mediaType);//抓取单个设备的文件，并更新文件列表，过程有Loading图画
            }
        }
    }

    public void updateMediaList(ArrayList<MediaItem> mediaItemList) {
        mediaItems = mediaItemList;
        listAdapter = new MediaListAdapter(mContext, mediaItemList);
        if (mediaFile_list == null) return;
        mediaFile_list.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        if (urls != null && urls.size() != 0) {
            urls.clear();
            for (int i = 0; i < mediaItemList.size(); i++) {
                urls.add(mediaItemList.get(i).getGsyVideoModel());
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
                    mediaFile_list.post(new Runnable() {
                        @Override
                        public void run() {
                            mediaFile_list.setSelectionFromTop(CSDMediaPlayer.getInstance(mContext).getGSYVideoManager().getPlayPosition(), 0);//显示第几个item
                        }
                    });
                    updateMediaList(mediaItems);
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
            if (mDeviceItem != null) {
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
            if (mDeviceItem != null)
                ((MainActivity) getActivity()).setPlayerLayer(mDeviceItem.getType());
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (CSDMediaPlayer.getInstance(mContext).getMediaInfo() != null
                && CSDMediaPlayer.getInstance(mContext).getMediaInfo().getMediaItems() != null
                && CSDMediaPlayer.getInstance(mContext).getMediaInfo().getMediaItems().size() != 0) {//Sandra@20220315 add
            if (CSDMediaPlayer.getInstance(mContext).getMediaInfo().getMediaItems().get(0).isIfVideo()) {
                CSDMediaPlayer.getInstance(mContext).onVideoPause();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public ArrayList<MediaItem> getSpecifiedMediaItems(int media_Type, DeviceItem deviceInfo) {
        ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();
        ArrayList<MediaItem> totalMediaItems = new ArrayList<>();
        totalMediaItems =/*((MainActivity)getActivity()).getAllDevicesMediaItems()*/MediaItemUtil.getAllDevicesMediaItems();
        String path = deviceInfo.getStoragePath();
        if (media_Type == TYPE_MUSIC) {
            for (int m = 0; m < totalMediaItems.size(); m++) {
                if (!totalMediaItems.get(m).isIfVideo() && totalMediaItems.get(m).getStoragePath().equals(path)) {
                    mediaItems.add(totalMediaItems.get(m));
                }
            }
        } else {
            for (int n = 0; n < totalMediaItems.size(); n++) {
                if (totalMediaItems.get(n).isIfVideo() && totalMediaItems.get(n).getStoragePath().equals(path)) {
                    mediaItems.add(totalMediaItems.get(n));
                }
            }
        }
        return mediaItems;
    }

    public static String printTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss:SSS");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        return str;
    }
}