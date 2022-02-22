package com.example.fxc.mediaplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.model.GSYVideoModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jennifer on 2022/2/08.
 */
public class ContentFragment extends Fragment {
    private View view;
    //private ListView musicListView;
    private Context mContext;
    //   private SimpleAdapter listAdapter;
    //   private TextView totaltimeView;
    //Sandra@20220215 add
    private ListView mediaFile_list;
    public List<MediaInfo> mediaInfos = null;
    private List<GSYVideoModel> urls = new ArrayList<>();
    //private ImageView playing_icon;
    //private TextView totaltime;
    private AnimationDrawable ani_gif_playing;
    public MediaListAdapter listAdapter;
    int lastPosition=-1;
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
        //音視頻列表
        mediaFile_list = (ListView) view.findViewById(R.id.list);
        String pathDefault = ((MainActivity) getActivity()).getCurrentStoragePath();//默認顯示當前設備的多媒體文件
        mediaInfos = MediaUtil.getMediaInfos(0, mContext, pathDefault);
        listAdapter = new MediaListAdapter(mContext, mediaInfos);
        mediaFile_list.setAdapter(listAdapter);
        mediaFile_list.setOnItemClickListener(onItemClickListener);
        return view;
    }

    public List<GSYVideoModel> getUrls() {
        for (int i = 0; i < mediaInfos.size(); i++) {
            urls.add(mediaInfos.get(i).getGsyVideoModel());
        }
        return urls;
    }

    public void setUrls(List<GSYVideoModel> urls) {
        this.urls = urls;
    }

    ListView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(((MainActivity)getActivity()).csdMediaPlayer.getCurrentState()==-1) {
                ImageView playing_icon = mediaFile_list.getChildAt(position).findViewById(R.id.playing_icon);
                playing_icon.setVisibility(View.VISIBLE);
                playing_icon.setBackgroundResource(R.drawable.ani_gif_playing);
                ani_gif_playing = (AnimationDrawable) playing_icon.getBackground();
                ani_gif_playing.start();
                TextView totaltime = mediaFile_list.getChildAt(position).findViewById(R.id.totalTime);
                totaltime.setVisibility(View.GONE);
                lastPosition=position;
            }else{
                resetAnimation(lastPosition);
                playingAnimation(position);
            }
            ((MainActivity)getActivity()).playMusic(position);
        }
    };

    public void smoothScrollToPosition(int position) {
        mediaFile_list.smoothScrollToPosition(position);
      /*  mediaFile_list.performItemClick(
                mediaFile_list.getAdapter().getView(position, null, null),
                position,
                mediaFile_list.getItemIdAtPosition(position));*/
    }

    public void playingAnimation(int position) {
        try {
            ImageView playing_icon = mediaFile_list.getChildAt(position).findViewById(R.id.playing_icon);
            playing_icon.setVisibility(View.VISIBLE);
            playing_icon.setBackgroundResource(R.drawable.ani_gif_playing);
            ani_gif_playing = (AnimationDrawable) playing_icon.getBackground();
            ani_gif_playing.start();
            TextView totaltime = mediaFile_list.getChildAt(position).findViewById(R.id.totalTime);
            totaltime.setVisibility(View.GONE);
            lastPosition = position;
        }catch (Exception e) {
            Log.d("jason", " e:" + e);
        }
       }

    public void resetAnimation(int lastPosition) {
            ImageView playing_icon = mediaFile_list.getChildAt(lastPosition).findViewById(R.id.playing_icon);
            playing_icon.setVisibility(View.GONE);
            playing_icon.setAnimation(null);
            TextView totaltime = mediaFile_list.getChildAt(lastPosition).findViewById(R.id.totalTime);
            totaltime.setVisibility(View.VISIBLE);
        }

    public void updateMediaList(int mediaType, String path) {
        if (mediaType == 0) {//音樂
            mediaInfos = MediaUtil.getMusicInfos(mContext, path);
        } else {//視頻
            mediaInfos = MediaUtil.getVideoInfos(mContext, path);
        }
        listAdapter = new MediaListAdapter(mContext, mediaInfos);
        if (mediaFile_list == null) return;
        mediaFile_list.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
        if (urls != null && urls.size() != 0) {
            urls.clear();
            for (int i = 0; i < mediaInfos.size(); i++) {
                urls.add(mediaInfos.get(i).getGsyVideoModel());
            }
        }
    }

}