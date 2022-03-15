package com.example.fxc;

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

import com.example.fxc.bt.BtMusicManager;
import com.example.fxc.bt.ConnectBlueCallBack;
import com.example.fxc.mediaplayer.CSDMediaPlayer;
import com.example.fxc.mediaplayer.DeviceItem;
import com.example.fxc.mediaplayer.DeviceItemUtil;
import com.example.fxc.mediaplayer.MediaController;
import com.example.fxc.mediaplayer.MediaInfo;
import com.example.fxc.mediaplayer.MediaItem;
import com.example.fxc.mediaplayer.MediaListAdapter;
import com.example.fxc.mediaplayer.R;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static android.security.KeyStore.getApplicationContext;
import static com.example.fxc.mediaplayer.Constants.BLUETOOTH_DEVICE;
import static com.example.fxc.mediaplayer.Constants.USB_DEVICE;
import static com.example.fxc.mediaplayer.MediaItemUtil.TYPE_MUSIC;
import static com.example.fxc.mediaplayer.MediaItemUtil.TYPE_VIDEO;

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
    // private boolean ifVideo = false;
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
        if (deviceItem.getType() == BLUETOOTH_DEVICE) {
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
            updateMediaList(mediaType, deviceItem);
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

    @Override
    public void onResume() {
        super.onResume();
        //音視頻列表
        mediaFile_list = (ListView) view.findViewById(R.id.list);
        MediaInfo mMediaInfo = CSDMediaPlayer.getInstance(mContext).getMediaInfo();
        Log.i(TAG, "onResume: CSDMediaPlayer.mInstance.getMediaInfo();");
        if (mMediaInfo == null) {

        } else {
            if (mMediaInfo.getMediaItems() != null) {
                if (mMediaInfo.getMediaItems().size() > 0) {
                    if (mMediaInfo.getMediaItems().get(0).isIfVideo()) {
                        updateMediaList(TYPE_VIDEO, mMediaInfo.getDeviceItem());
                        TabLayout.Tab tab = ((MainActivity) getActivity()).getmTabLayout().getTabAt(TYPE_VIDEO);
                        tab.select();
                    } else {
                        updateMediaList(TYPE_MUSIC, mMediaInfo.getDeviceItem());
                        TabLayout.Tab tab = ((MainActivity) getActivity()).getmTabLayout().getTabAt(TYPE_MUSIC);
                        tab.select();
                    }
                }


            } else {
                Log.i(TAG, "onResume: ");
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

            MediaController.getInstance(mContext).setCurrentSourceType(mDeviceItem.getType());
            if (MediaController.getInstance(mContext).currentSourceType == USB_DEVICE) {
                ((MainActivity) getActivity()).playMusic(position);
                DeviceItem deviceItem = DeviceItemUtil.getInstance(mContext).getDeviceByStoragePath(mediaItems.get(position).getStoragePath());
                CSDMediaPlayer.getInstance(mContext).setMediaInfo(new MediaInfo(mediaItems, deviceItem));
                //  ifVideo=CSDMediaPlayer.getInstance(mContext).getMediaInfo().getMediaItems().get(position).isIfVideo();
                Log.i(TAG, "onItemClick: mediaItems" + mediaItems.size());
                //<--Sandra@20220311 add
            } else {//设置蓝牙选中歌曲播放，还有控制Activity的UI设置

                //   MediaController.getInstance(mContext).setPlayerState();
            }
            ((MainActivity) getActivity()).setPlayerLayer(mDeviceItem.getType());
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (CSDMediaPlayer.getInstance(mContext).getMediaInfo().getMediaItems().get(0).isIfVideo()) {
            CSDMediaPlayer.getInstance(mContext).onVideoPause();
        }
    }


}