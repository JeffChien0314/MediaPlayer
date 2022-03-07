package com.example.fxc.mediaplayer;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.shuyu.gsyvideoplayer.model.GSYVideoModel;

/**
 * Created by Sandra on 2022/2/10.
 */

public class MediaInfo implements Parcelable {
    private long id; // 歌曲ID
    private String title; // 歌曲名稱
    // private String album; // 專輯
    private String artist; // 歌手名稱
    private long duration; // 歌曲時長
    private Bitmap thumbBitmap;
    private GSYVideoModel gsyVideoModel;
    private boolean ifVideo;
    /* private long albumId;//專輯ID*/
    //   private String displayName; //顯示名稱
    //  private long size; // 歌曲大小
    //  private String url; // 歌曲路徑
   /* private String lrcTitle; // 歌詞名稱
    private String lrcSize; // 歌詞大小*/


    protected MediaInfo(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        duration = in.readLong();
        thumbBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        ifVideo = in.readByte() != 0;
    }

    public static final Creator<MediaInfo> CREATOR = new Creator<MediaInfo>() {
        @Override
        public MediaInfo createFromParcel(Parcel in) {
            return new MediaInfo(in);
        }

        @Override
        public MediaInfo[] newArray(int size) {
            return new MediaInfo[size];
        }
    };

    public boolean isIfVideo() {
        return ifVideo;
    }

    public void setIfVideo(boolean ifVideo) {
        this.ifVideo = ifVideo;
    }

    public MediaInfo() {
        super();
    }


    public GSYVideoModel getGsyVideoModel() {
        return gsyVideoModel;
    }

    public void setGsyVideoModel(GSYVideoModel gsyVideoModel) {
        this.gsyVideoModel = gsyVideoModel;
    }

    public Bitmap getThumbBitmap() {
        return thumbBitmap;
    }

    public void setThumbBitmap(Bitmap thumbBitmap) {
        this.thumbBitmap = thumbBitmap;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeLong(duration);
        dest.writeParcelable(thumbBitmap, flags);
        dest.writeByte((byte) (ifVideo ? 1 : 0));
    }

}
