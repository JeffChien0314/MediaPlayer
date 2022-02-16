package com.example.fxc.mediaplayer;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Sandra on 2022/2/10.
 */

public class MediaUtil {

    public static List<MediaInfo> getMusicInfos(Context context, String path) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.DATA + " like ? ";
        String[] selectionArgs = {path + "%"};
   /*     Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);*/
        ContentResolver mResolver = context.getContentResolver();
        Cursor cursor = mResolver.query(uri, null, selection, selectionArgs, null);
        List<MediaInfo> musicInfos = new ArrayList<MediaInfo>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            MediaInfo musicInfo = new MediaInfo();
            long id = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID));	//音樂id
            String title = cursor.getString((cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE))); // 音樂標題
            String artist = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 藝術家
            String album = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM));	//專輯
            String displayName = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            long duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION)); // 時長
            long size = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.SIZE)); // 檔案大小
            String url = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA)); // 檔案路徑
            int isMusic = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)); // 是否為音樂

            Bitmap thumbBitmap = null;
            if (url != null) {
                       thumbBitmap = loadingCoverOfMusic(url); //根据专辑路径获取到专辑封面图

            }
            if (isMusic != 0) { // 只把音樂新增到集合當中
                musicInfo.setId(id);
                musicInfo.setTitle(title);
                musicInfo.setArtist(artist);
                musicInfo.setAlbum(album);
                musicInfo.setDisplayName(displayName);
                musicInfo.setAlbumId(albumId);
                musicInfo.setDuration(duration);
                musicInfo.setSize(size);
                musicInfo.setUrl(url);
                musicInfo.setThumbBitmap(thumbBitmap);
                musicInfos.add(musicInfo);
            }
        }
        return musicInfos;
    }

    public static List<HashMap<String, String>> getMusicMaps(
            List<MediaInfo> mp3Infos) {
        List<HashMap<String, String>> musiclist = new ArrayList<HashMap<String, String>>();
        for (Iterator iterator = mp3Infos.iterator(); iterator.hasNext();) {
            MediaInfo musicInfo = (MediaInfo) iterator.next();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("title", musicInfo.getTitle());
            map.put("Artist", musicInfo.getArtist());
            map.put("album", musicInfo.getAlbum());
            map.put("displayName", musicInfo.getDisplayName());
            map.put("albumId", String.valueOf(musicInfo.getAlbumId()));
            map.put("duration", formatTime(musicInfo.getDuration()));
            map.put("size", String.valueOf(musicInfo.getSize()));
            map.put("data", musicInfo.getUrl());
            musiclist.add(map);
        }
        return musiclist;
    }
    public static List<MediaInfo> getVideoInfos(Context context, String path) {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Video.Media.DATA+ " like ? ";
        String[] selectionArgs = {path + "%"};
        ContentResolver mResolver = context.getContentResolver();
        Cursor cursor = mResolver.query(uri, null, selection, selectionArgs, null);
        List<MediaInfo> VideoInfos = new ArrayList<MediaInfo>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            MediaInfo videoinfo = new MediaInfo();
            long id = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Video.Media._ID));	//音樂id
            String title = cursor.getString((cursor
                    .getColumnIndex(MediaStore.Video.Media.TITLE))); // 音樂標題
            String artist = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media.ARTIST)); // 藝術家
            String album = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media.ALBUM));	//專輯
            String displayName = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
          //  long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM_ID));
            long duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Video.Media.DURATION)); // 時長
            long size = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Video.Media.SIZE)); // 檔案大小
            String url = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media.DATA)); // 檔案路徑
            String mime_type =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));

            Bitmap thumbBitmap = null;
            if (url != null) {
                thumbBitmap = loadingCoverOfVideo(url); //根据专辑路径获取到专辑封面图

            }
            if (mime_type != null) {
                videoinfo.setId(id);
                videoinfo.setTitle(title);
                videoinfo.setArtist(artist);
                videoinfo.setAlbum(album);
                videoinfo.setDisplayName(displayName);
             //   musicInfo.setAlbumId(albumId);
                videoinfo.setDuration(duration);
                videoinfo.setSize(size);
                videoinfo.setUrl(url);
                videoinfo.setThumbBitmap(thumbBitmap);
                VideoInfos.add(videoinfo);
            }
        }
        return VideoInfos;
    }
    public static List<HashMap<String, String>> getVideoMaps(
            List<MediaInfo> mp3Infos) {
        List<HashMap<String, String>> videolist = new ArrayList<HashMap<String, String>>();
        for (Iterator iterator = mp3Infos.iterator(); iterator.hasNext();) {
            MediaInfo videoInfo = (MediaInfo) iterator.next();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("title", videoInfo.getTitle());
            map.put("Artist", videoInfo.getArtist());
            map.put("album", videoInfo.getAlbum());
            map.put("displayName", videoInfo.getDisplayName());
          //  map.put("albumId", String.valueOf(videoInfo.getAlbumId()));
            map.put("duration", formatTime(videoInfo.getDuration()));
            map.put("size", String.valueOf(videoInfo.getSize()));
            map.put("data", videoInfo.getUrl());
            videolist.add(map);
        }
        return videolist;
    }
    /**
     * 格式化時間，將毫秒轉換為分:秒格式
     * @param time
     * @return
     */
    public static String formatTime(long time) {
        // TODO Auto-generated method stub
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }

    /**
     * 加载封面
     *
     * @param mediaUri MP3文件路径
     */
    public static Bitmap loadingCoverOfMusic(String mediaUri) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(mediaUri);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
        if (picture != null && picture.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            return bitmap;
        } else {
            return null;
        }
    }
    /**
     * 加载封面
     *
     * @param mediaUri VIDE0文件路径
     */
    private static Bitmap loadingCoverOfVideo(String mediaUri) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(mediaUri);
        Bitmap frameAtTime = mediaMetadataRetriever.getFrameAtTime(/*1 * 1000 * 1000,*//* MediaMetadataRetriever.OPTION_CLOSEST*/);
        if (frameAtTime == null) {
            frameAtTime = mediaMetadataRetriever.getFrameAtTime(3 * 1000 * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
        }
        return frameAtTime;
    }
}
