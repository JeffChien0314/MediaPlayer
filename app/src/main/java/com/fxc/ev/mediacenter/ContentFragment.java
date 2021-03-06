package com.fxc.ev.mediacenter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.adapter.MediaListAdapter;
import com.fxc.ev.mediacenter.bluetooth.BtMusicManager;
import com.fxc.ev.mediacenter.bluetooth.ConnectBlueCallBack;
import com.fxc.ev.mediacenter.datastruct.DeviceItem;
import com.fxc.ev.mediacenter.datastruct.MediaInfo;
import com.fxc.ev.mediacenter.datastruct.MediaItem;
import com.fxc.ev.mediacenter.localplayer.CSDMediaPlayer;
import com.fxc.ev.mediacenter.util.Constants;
import com.fxc.ev.mediacenter.util.DeviceItemUtil;
import com.fxc.ev.mediacenter.util.MediaController;
import com.fxc.ev.mediacenter.util.MediaItemUtil;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.security.KeyStore.getApplicationContext;
import static com.fxc.ev.mediacenter.MainActivity.currentTab;
import static com.fxc.ev.mediacenter.util.Constants.BLUETOOTH_DEVICE;
import static com.fxc.ev.mediacenter.util.Constants.cutDownBrowseFunction;
import static com.fxc.ev.mediacenter.util.MediaItemUtil.MUSIC;
import static com.fxc.ev.mediacenter.util.MediaItemUtil.TYPE_MUSIC;
import static com.fxc.ev.mediacenter.util.MediaItemUtil.TYPE_VIDEO;
import static com.fxc.ev.mediacenter.util.MediaItemUtil.VIDEO;
import static com.fxc.ev.mediacenter.util.MediaItemUtil.getMusicInfos;

/**
 * Created by Jennifer on 2022/2/08.
 */
public class ContentFragment extends Fragment {
    String TAG = ContentFragment.class.getSimpleName();
    public ArrayList<MediaItem> mediaItems = new ArrayList<>();
    public MediaListAdapter listAdapter;
    private View view;
    private Context mContext;
    private String FragmentName ="";
    private TextView initial_tips;
    public ListView mediaFile_list;
    private List<GSYVideoModel> urls = new ArrayList<>();
    private AnimationDrawable ani_gif_playing;
    private DeviceItem mDeviceItem;
    @SuppressLint("ValidFragment")
    public ContentFragment(Context context,String title) {
        super();
        mContext = context;
        FragmentName =title;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contentfragment, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //???????????????
        mediaFile_list = (ListView) view.findViewById(R.id.list);
        initial_tips = (TextView) view.findViewById(R.id.initial_tips);
        updateMediaList(mediaItems);
        mediaFile_list.setOnItemClickListener(onItemClickListener);
        mediaFile_list.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.i(TAG, "onScrollChange: playingAnimation/resetAnimation");
                for (int i = 0; i < mediaItems.size(); i++) {
                    if (currentTab==TYPE_MUSIC){//Sandra@20220423 modify-->
                        if (isVisiable(i) && DeviceItemUtil.getInstance(mContext).getCurrentDevice().getLastMusicIndex()== i) {
                            playingAnimation(DeviceItemUtil.getInstance(mContext).getCurrentDevice().getLastMusicIndex());
                    } else {
                        resetAnimation(i);
                    }
                    }else {
                        if (isVisiable(i) && DeviceItemUtil.getInstance(mContext).getCurrentDevice().getLastVideoIndex() == i) {
                            playingAnimation(DeviceItemUtil.getInstance(mContext).getCurrentDevice().getLastVideoIndex());
                        } else {
                            resetAnimation(i);
                        }
                    }//<--Sandra@20220423 modify
                }
            }
        });
        if (BLUETOOTH_DEVICE == MediaController.getInstance(mContext).currentSourceType) {
            mediaItems = MediaController.getInstance(getApplicationContext()).getMeidaInfosByDevice(mDeviceItem, 0, true).getMediaItems();
        } else {
            MediaInfo mediaInfo = CSDMediaPlayer.getInstance(mContext).getMediaInfo();
            if (mediaInfo != null) {
                if (mediaInfo.getMediaItems() != null) {
                    if (mediaInfo.getMediaItems().size() > 0) {
                        if (!DeviceItemUtil.getInstance(getContext()).isDeviceExist(mediaInfo.getDeviceItem().getStoragePath())) {
                            return;
                        }
                        mDeviceItem = mediaInfo.getDeviceItem();
                        DeviceItemUtil.getInstance(getApplicationContext()).setCurrentDevice(mDeviceItem);//Sandra@20220324 add
                        mediaItems = mediaInfo.getMediaItems();
                        if (mediaItems != null && mediaItems.size() != 0) {
                            //Fix ??????????????????????????????????????????????????????
                        } else {
                            ((MainActivity) getActivity()).playMusic(CSDMediaPlayer.getInstance(mContext).getPlayPosition());//position??????????????????????????????????????????
                        }
                        //jennifer add for ?????????????????????List?????????-->
                        mediaFile_list.post(new Runnable() {
                            @Override
                            public void run() {
                                mediaFile_list.setSelectionFromTop(CSDMediaPlayer.getInstance(mContext).getGSYVideoManager().getPlayPosition(), 0);//???????????????item
                            }
                        });
                        //jennifer add for ?????????????????????List?????????<--
                        TabLayout.Tab tab = ((MainActivity) getActivity()).getmTabLayout().getTabAt(0);
                        if (mediaInfo.getMediaItems().get(0).isIfVideo()) {
                            tab = ((MainActivity) getActivity()).getmTabLayout().getTabAt(1);
                            ((MainActivity) getActivity()).mAlbum_photo.setBackgroundResource(R.drawable.background_portriat);
                            ((MainActivity) getActivity()).mAlbum_photo_mask.setVisibility(View.INVISIBLE);
                        }else {
                            Drawable drawable = new BitmapDrawable(getApplicationContext().getResources(), mediaItems.get(  ((MainActivity) getActivity()).getCurrPosition()).getThumbBitmap());
                            ((MainActivity) getActivity()).mAlbum_photo.setBackground(drawable);
                            ((MainActivity) getActivity()).mAlbum_photo_mask.setVisibility(View.VISIBLE);
                        }
                        tab.select();
                    } else {
                        Log.i(TAG, "onResume: ");
                    }
                }

            } else {//player ?????????????????????????????????
                if (DeviceItemUtil.getInstance(getApplicationContext()).getExternalDeviceInfoList() == null
                        || DeviceItemUtil.getInstance(getApplicationContext()).getExternalDeviceInfoList().size() == 0) {
                    return;//?????????
                } else {
                    mDeviceItem = DeviceItemUtil.getInstance(getApplicationContext()).getExternalDeviceInfoList().get(0);
                    MediaController.getInstance(getContext()).getDevices();
                    DeviceItem itemDefault = MediaController.getInstance(getContext()).getDevices().get(0);
                    DeviceItemUtil.getInstance(getApplicationContext()).setCurrentDevice(itemDefault);
                    CSDMediaPlayer.getInstance(getApplicationContext()).setMediaInfo(new MediaInfo(MediaController.getInstance(getApplicationContext()).getMeidaInfosByDevice(itemDefault, 0, true).getMediaItems(), itemDefault));
                }
                DeviceItemUtil.getInstance(getApplicationContext()).setCurrentDevice(mDeviceItem);//Sandra@20220324 add
                mediaItems = getMusicInfos(getApplicationContext(), mDeviceItem.getStoragePath());
                if (mediaItems == null || mediaItems.size() == 0) return;
                CSDMediaPlayer.getInstance(mContext).setMediaInfo(new MediaInfo(mediaItems, mDeviceItem));
                ((MainActivity) getActivity()).playMusic(0);
            }

        }

        if (mDeviceItem == null) {
            return;
        }
        ((MainActivity) getActivity()).device_tips.setText(mDeviceItem.getDescription());
        ((MainActivity) getActivity()).changeVisibleOfDeviceView(false);
        ((MainActivity) getActivity()).setPlayerLayer(mDeviceItem.getType());//???????????????????????????????????????????????????????????????UI??????
        ((MainActivity) getActivity()).updateDeviceListData(/*false*/);//????????????????????????
        listAdapter = new MediaListAdapter(mContext, mediaItems);
        mediaFile_list.setAdapter(listAdapter);
        //jennifer add for ?????????????????????list?????????????????????-->
     /*   ViewGroup.LayoutParams params = mediaFile_list.getLayoutParams();
        if (isDeviceMenuOpen()) {
            if (MediaController.getInstance(mContext).getDevices().size() * 90 < 990) {
                params.height = 1000 - (MediaController.getInstance(mContext).getDevices().size() * 90);
            } else {
                params.height = 0;
            }
        } else {
            params.height = 1000;
        }
        mediaFile_list.setLayoutParams(params);*/
        //jennifer add for ?????????????????????list?????????????????????<--

    }

    ListView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
           // playingAnimation(position);//Sandra@20220423 delete ??????mediaFile_list???onScrollChange??????????????????????????????????????????
            if (mDeviceItem != null) {
                MediaController.getInstance(mContext).setCurrentSourceType(mDeviceItem.getType());
            }
            if (MediaController.getInstance(mContext).currentSourceType == Constants.USB_DEVICE) {
                ((MainActivity) getActivity()).playMusic(position);
                CSDMediaPlayer.getInstance(mContext).setMediaInfo(new MediaInfo(mediaItems, mDeviceItem));
                Log.i(TAG, "onItemClick: mediaItems" + mediaItems.size());
                //<--Sandra@20220311 add
            } else {//?????????????????????????????????????????????Activity???UI??????

                //   MediaController.getInstance(mContext).setPlayerState();
            }
          /*  if (mDeviceItem != null) {
                //  ((MainActivity) getActivity()).setPlayerLayer(mDeviceItem.getType());
                ((MainActivity) getActivity()).device_tips.setText(mDeviceItem.getDescription());
                ((MainActivity) getActivity()).updateDeviceListData(*//*false*//*);
                ((MainActivity) getActivity()).changeVisibleOfDeviceView(false);
            }*/
        }
    };
    @Override
    public void onPause() {
        super.onPause();
        if ( ((MainActivity) getActivity()).popWindow!=null){
            ((MainActivity) getActivity()).popWindow.dismiss();
            ((MainActivity) getActivity()).changeVisibleOfDeviceView(false);
        }
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


    public List<GSYVideoModel> getUrls() {
        for (int i = 0; i < mediaItems.size(); i++) {
            urls.add(mediaItems.get(i).getGsyVideoModel());
        }
        return urls;
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
        if (position >= mediaFile_list.getFirstVisiblePosition() && position <= mediaFile_list.getLastVisiblePosition()) {//???????????????
            ImageView playing_icon = mediaFile_list.getChildAt(position - mediaFile_list.getFirstVisiblePosition()).findViewById(R.id.playing_icon);
            playing_icon.setVisibility(View.VISIBLE);
            playing_icon.setBackgroundResource(R.drawable.ani_gif_playing);
            ani_gif_playing = (AnimationDrawable) playing_icon.getBackground();
            ani_gif_playing.start();
            TextView totaltime = mediaFile_list.getChildAt(position - mediaFile_list.getFirstVisiblePosition()).findViewById(R.id.totalTime);
            totaltime.setVisibility(View.GONE);
            RelativeLayout playlist_item = mediaFile_list.getChildAt(position - mediaFile_list.getFirstVisiblePosition()).findViewById(R.id.playlist_item);
            playlist_item.setActivated(true);
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
                RelativeLayout playlist_item = mediaFile_list.getChildAt(lastPosition - mediaFile_list.getFirstVisiblePosition()).findViewById(R.id.playlist_item);
                playlist_item.setActivated(false);
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

    public void deviceItemOnClick(int mediaType, DeviceItem deviceItem, ConnectBlueCallBack connectBlueCallBack) {
        if (BLUETOOTH_DEVICE == MediaController.getInstance(mContext).currentSourceType) {
            BtMusicManager.getInstance().setA2dpSinkConnect(null, false, connectBlueCallBack);
        }
        MediaController.getInstance(mContext).setCurrentSourceType(deviceItem.getType());
        if (deviceItem.getType() == Constants.USB_DEVICE) {
            mDeviceItem = deviceItem;
            if (MediaItemUtil.allDevicesMediaItems.size() != 0) {//??????????????????????????????????????????
                mediaItems = filterAllMediaItemsOfSpecificDevice(mediaType, deviceItem);
                updateMediaList(mediaItems);
                modifyRelativeUI(mediaItems);
                if (((MainActivity) getActivity()).device_tips!=null){
                ((MainActivity) getActivity()).device_tips.setText(deviceItem.getDescription());
                }
            } else {//??????????????????????????????????????????????????????????????????Loading????????????????????????????????????
              //  ((MainActivity) getActivity()).updateDeviceListData(/*true*/);
                if (deviceItem != null) {
                    ((MainActivity) getActivity()).getALLMediaItemsOfSpecificDevice(true, deviceItem, mediaType);
                    updateMediaList(mediaItems);
                    modifyRelativeUI(mediaItems);
                }
            }
            if (cutDownBrowseFunction) {
                CSDMediaPlayer.getInstance(mContext).setMediaInfo(new MediaInfo(mediaItems, mDeviceItem));
            }
        } else {
            //  MediaController.getInstance(mContext).setCurrentSourceType(BLUETOOTH_DEVICE);
            CSDMediaPlayer.getInstance(getApplicationContext()).release();
           // ((MainActivity) getActivity()).changeVisibleOfDeviceView(false);//Sandra@20220427 delete ??????????????????
            MediaController.getInstance(getApplicationContext()).setPlayerState(Constants.STATE_PLAY, -1);

            if (deviceItem.getBluetoothDevice().isConnected() && BtMusicManager.getInstance().isA2dpActiveDevice(deviceItem.getBluetoothDevice())) {
            } else {
                try {
                    if (deviceItem.getBluetoothDevice().getBondState() == BluetoothDevice.BOND_NONE) {
                        Method m = BluetoothDevice.class.getMethod("createBond");
                        m.invoke(deviceItem.getBluetoothDevice());
                    } else if (deviceItem.getBluetoothDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
                        BtMusicManager.getInstance().setA2dpSinkConnect(deviceItem.getBluetoothDevice(), true, connectBlueCallBack);
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
        }
    }

    public void updateMediaList(ArrayList<MediaItem> mediaItemList) {
        if (mediaItemList == null) return;
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

    public void modifyRelativeUI(ArrayList<MediaItem> mediaItemList) {
        try{
        if (mediaItemList.size() == 0) {
            ((MainActivity) getActivity()).mRandomButton.setEnabled(false);
            ((MainActivity) getActivity()). mPlayModeButton.setBackgroundResource(R.drawable.playmode_bg);
            ((MainActivity) getActivity()). mPlayModeButton.setEnabled(false);
            initial_tips.setVisibility(View.VISIBLE);
        } else {
                if ( ((MainActivity) getActivity()). mRandomButton!=null)
            ((MainActivity) getActivity()). mRandomButton.setEnabled(true);
                if ( ((MainActivity) getActivity()). mPlayModeButton!=null)
            ((MainActivity) getActivity()). mPlayModeButton.setEnabled(true);
                if (initial_tips!=null)
            initial_tips.setVisibility(View.GONE);
        }
        }catch (Exception e){
            Log.i(TAG, "modifyRelativeUI: e"+e);
        }
    }
    public ArrayList<MediaItem> filterAllMediaItemsOfSpecificDevice(int media_Type, DeviceItem deviceInfo) {
        if (deviceInfo == null || deviceInfo.getStoragePath() == null) {
            return null;
        }
        ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();
        ArrayList<MediaItem> totalMediaItems = new ArrayList<>();
        totalMediaItems = MediaItemUtil.allDevicesMediaItems;
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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy???MM???dd???   HH:mm:ss:SSS");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        return str;
    }


}