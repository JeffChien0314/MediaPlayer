package com.fxc.ev.mediacenter.datastruct;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.shuyu.gsyvideoplayer.model.GSYVideoModel;

/**
 * Created by Sandra on 2022/2/10.
 */

public class MediaItem implements Parcelable {
    private Long id; // 歌曲ID
    private String title; // 歌曲名稱
    private String album; // 專輯
    private String artist; // 歌手名稱
    private long duration; // 歌曲時長
    private Bitmap thumbBitmap;
    private GSYVideoModel gsyVideoModel;
    private boolean ifVideo;
    private String storagePath;//对应设备路径
    /* private long albumId;//專輯ID*/
    //   private String displayName; //顯示名稱
    //  private long size; // 歌曲大小
    //  private String url; // 歌曲路徑
   /* private String lrcTitle; // 歌詞名稱
    private String lrcSize; // 歌詞大小*/


    protected MediaItem(Parcel in) {
        id = in.readLong();
        title = in.readString();
        album = in.readString();
        artist = in.readString();
        duration = in.readLong();
        thumbBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        ifVideo = in.readByte() != 0;
        storagePath=in.readString();
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };


    public boolean isIfVideo() {
        return ifVideo;
    }

    public void setIfVideo(boolean ifVideo) {
        this.ifVideo = ifVideo;
    }

    public MediaItem() {
        super();
    }

    public MediaItem(Long id, String title, String album, String artist, long duration, Bitmap thumbBitmap, GSYVideoModel gsyVideoModel, boolean ifVideo, String storagePath) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.thumbBitmap = thumbBitmap;
        this.gsyVideoModel = gsyVideoModel;
        this.ifVideo = ifVideo;
        this.storagePath = storagePath;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
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

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeLong(duration);
        dest.writeParcelable(thumbBitmap, flags);
        dest.writeByte((byte) (ifVideo ? 1 : 0));
        dest.writeString(storagePath);
    }

}
