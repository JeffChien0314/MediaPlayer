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
    private ListView musicListView;
    private Context mContext;
    private List<HashMap<String, String>> mList;
    private SimpleAdapter listAdapter;
    private TextView totaltimeView;

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
        musicListView = (ListView) view.findViewById(R.id.list);
   
        initListView();
        musicListView.setOnItemClickListener(onItemClickListener);
        return view;
    }
    ListView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ((MainActivity)getActivity()).playMusic(position);
        }
    };
   public void initListView() {

        listAdapter = new SimpleAdapter(mContext, mList, R.layout.music_list, new String[]{"name", "artist","duration"}, new int[]{R.id.songName, R.id.artistName,R.id.totalTime});
        musicListView.setAdapter(listAdapter);
    }
    public void smoothScrollToPosition(int position) {
        Log.i("main", "Jennifertest21=: " + position);
        musicListView.smoothScrollToPosition( position);
        musicListView.performItemClick(
                musicListView.getAdapter().getView(position, null, null),
                position,
                musicListView.getItemIdAtPosition(position));
    }

}