package com.example.fxc.mediaplayer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.example.fxc.mediaplayer.R;

import java.util.HashMap;
import java.util.List;
/**
 * Created by Jennifer on 2022/2/08.
 */
public class ContentFragment extends Fragment {
    private View view;
    //private ListView musicListView;
    private Context mContext;
    private List<HashMap<String, String>> mList;
    //   private SimpleAdapter listAdapter;
    //   private TextView totaltimeView;
    //Sandra@20220215 add
    private ListView mediaFile_list;
    public List<MediaInfo> musicInfos = null;
    public List<MediaInfo> videoInfos = null;
    public MediaListAdapter Listadapter;
    public ContentFragment() {
        super();
    }
    @SuppressLint("ValidFragment")
    public ContentFragment(Context context, List<HashMap<String, String>> list) {
        super();
        mContext = context;
        mList = list;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contentfragment, container, false);
        //音視頻列表
        mediaFile_list = (ListView)view.findViewById(R.id.list);
        String pathDefault= ((MainActivity)getActivity()).getCurrentStoragePath();//默認顯示當前設備的多媒體文件
        musicInfos = MediaUtil.getMusicInfos(mContext,pathDefault);
        Listadapter = new MediaListAdapter(mContext, musicInfos);
        mediaFile_list.setAdapter(Listadapter);
        mediaFile_list.setOnItemClickListener(onItemClickListener);
        return view;
    }
    ListView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ((MainActivity)getActivity()).playMusic(position);
        }
    };
    public void smoothScrollToPosition(int position) {
        Log.i("main", "Jennifertest21=: " + position);
        mediaFile_list.smoothScrollToPosition( position);
        mediaFile_list.performItemClick(
                mediaFile_list.getAdapter().getView(position, null, null),
                position,
                mediaFile_list.getItemIdAtPosition(position));
    }
    public void updateMusic(String path){
        musicInfos = MediaUtil.getMusicInfos(mContext,path);
        Listadapter = new MediaListAdapter(mContext, musicInfos);
        if (mediaFile_list==null)return;
        mediaFile_list.setAdapter(Listadapter);
        Listadapter.notifyDataSetChanged();
    }
    public void updateVideo(String path){
        videoInfos = MediaUtil.getVideoInfos(mContext,path);
        Listadapter = new MediaListAdapter(mContext, videoInfos);
        if (mediaFile_list==null)return;
        mediaFile_list.setAdapter(Listadapter);
        Listadapter.notifyDataSetChanged();
    }
}