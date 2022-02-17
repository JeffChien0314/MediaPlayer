package com.example.fxc.mediaplayer;
import android.graphics.Bitmap;

import com.shuyu.gsyvideoplayer.model.GSYVideoModel;

/**
 * Created by Sandra on 2022/2/10.
 */

public class MediaInfo {
    private long id; // 歌曲ID
    private String title; // 歌曲名稱
    private String album; // 專輯
    private long albumId;//專輯ID
    private String displayName; //顯示名稱
    private String artist; // 歌手名稱
    private long duration; // 歌曲時長
    private long size; // 歌曲大小
    private String url; // 歌曲路徑
    private String lrcTitle; // 歌詞名稱
    private String lrcSize; // 歌詞大小
   private Bitmap thumbBitmap;
    private GSYVideoModel gsyVideoModel;

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


    public MediaInfo() {
        super();
    }
    public MediaInfo(long id, String title, String album, long albumId,
                     String displayName, String artist, long duration, long size,
                     String url, String lrcTitle, String lrcSize) {
        super();
        this.id = id;
        this.title = title;
        this.album = album;
        this.albumId = albumId;
        this.displayName = displayName;
        this.artist = artist;
        this.duration = duration;
        this.size = size;
        this.url = url;
        this.lrcTitle = lrcTitle;
        this.lrcSize = lrcSize;
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

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLrcTitle() {
        return lrcTitle;
    }

    public void setLrcTitle(String lrcTitle) {
        this.lrcTitle = lrcTitle;
    }

    public String getLrcSize() {
        return lrcSize;
    }

    public void setLrcSize(String lrcSize) {
        this.lrcSize = lrcSize;
    }
}
