package com.fxc.ev.mediacenter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.datastruct.MediaItem;
import com.fxc.ev.mediacenter.util.MediaItemUtil;

import java.util.List;

/**
 * Created by Sandra on 2022/2/10.
 */

public class MediaListAdapter extends BaseAdapter {
    private Context context;
    private List<MediaItem> mediaItems;
    private MediaItem mediaItem;

    /**
     * 建構函式
     *
     * @param context  上下文
     * @param mp3Infos 集合物件
     */
    public MediaListAdapter(Context context, List<MediaItem> mp3Infos) {
        this.context = context;
        this.mediaItems = mp3Infos;
    }

    public List<MediaItem> getMediaItems() {
        return mediaItems;
    }

    public void setMediaItems(List<MediaItem> mediaItems) {
        this.mediaItems = mediaItems;
    }

    @Override
    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.music_list, null);
            viewHolder.musicTitle = (TextView) convertView.findViewById(R.id.songName);
            viewHolder.musicDuration = (TextView) convertView.findViewById(R.id.totalTime);
            viewHolder.musicArtist = (TextView) convertView.findViewById(R.id.artistName);
            viewHolder.albumImage = (ImageView) convertView.findViewById(R.id.playlist_icon);
            convertView.setTag(viewHolder);            //表示給View新增一個格外的資料，
        } else {
            viewHolder = (ViewHolder) convertView.getTag();//通過getTag的方法將資料取出來
        }
        mediaItem = mediaItems.get(position);
        viewHolder.musicTitle.setText(mediaItem.getTitle());//顯示標題
        viewHolder.musicArtist.setText(mediaItem.getArtist()+ "-" +mediaItem.getAlbum());//顯示藝術家
        viewHolder.musicDuration.setText(MediaItemUtil.formatTime(mediaItem.getDuration()));//顯示時長
        viewHolder.albumImage.setImageBitmap(mediaItem.getThumbBitmap());

        return convertView;
    }

    /**
     * 定義一個內部類
     * 宣告相應的控制元件引用
     */
    public class ViewHolder {
        //所有控制元件物件引用
        public ImageView albumImage;    //專輯圖片
        public TextView musicTitle;        //音樂標題
        public TextView musicDuration;    //音樂時長
        public TextView musicArtist;    //音樂藝術家
    }
}
