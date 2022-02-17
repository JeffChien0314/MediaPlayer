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
    private List<HashMap<String, String>> mList;
    //   private SimpleAdapter listAdapter;
    //   private TextView totaltimeView;
    //Sandra@20220215 add
    private ListView mediaFile_list;
    public List<MediaInfo> mediaInfos = null;
    private List<GSYVideoModel> urls = new ArrayList<>();

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
        mediaInfos = MediaUtil.getMediaInfos(0,mContext,pathDefault);
        Listadapter = new MediaListAdapter(mContext, mediaInfos);
        mediaFile_list.setAdapter(Listadapter);
        mediaFile_list.setOnItemClickListener(onItemClickListener);
        return view;
    }

    public List<GSYVideoModel> getUrls() {
        for (int i=0;i<mediaInfos.size();i++){
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
    public void updateMediaList(int mediaType,String path){
        if (mediaType==0){//音樂
            mediaInfos  = MediaUtil.getMusicInfos(mContext,path);
        }else {//視頻
            mediaInfos = MediaUtil.getVideoInfos(mContext,path);
        }
        Listadapter = new MediaListAdapter(mContext, mediaInfos);
        if (mediaFile_list==null)return;
        mediaFile_list.setAdapter(Listadapter);
        Listadapter.notifyDataSetChanged();
        if (urls!=null && urls.size()!=0){
            urls.clear();
            for (int i=0;i<mediaInfos.size();i++){
                urls.add(mediaInfos.get(i).getGsyVideoModel());
            }
        }
    }

}