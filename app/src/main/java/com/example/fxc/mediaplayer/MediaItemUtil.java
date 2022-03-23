package com.example.fxc.mediaplayer;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.shuyu.gsyvideoplayer.model.GSYVideoModel;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.example.fxc.ContentFragment.printTime;

/**
 * Created by Sandra on 2022/2/10.
 */

public class MediaItemUtil {
    public static final int TYPE_MUSIC = 0;
    public static final int TYPE_VIDEO = 1;
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

    public static MediaInfo getMediaInfos(int mediaType, Context context, DeviceItem deviceItem) {

        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.setDeviceItem(deviceItem);
        if (mediaType == TYPE_MUSIC) {//TYPE_MUSIC = 0
            mediaInfo.setMediaItems(getMusicInfos(context, deviceItem.getStoragePath()));
        } else {
            mediaInfo.setMediaItems(getVideoInfos(context, deviceItem.getStoragePath()));
        }
        return mediaInfo;
    }

    public static int IfIDExist(Long id, int mediaType, Context context, ArrayList<MediaItem> mediaItems) {
        for (int i = 0; i < mediaItems.size(); i++) {
            if (mediaItems.get(i).getId() == id) {
                Log.i(TAG, "IDExist!!! ");
                return i;
            }
        }
        return 0;
    }

    public static ArrayList<MediaItem> getMusicInfos(Context context, String path) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            Log.i(TAG, "getMusicInfos: if " + uri);
        }
        Log.i(TAG, "getMusicInfos: uri:" + uri);
        String selection = MediaStore.Audio.Media.DATA + " like ? ";
        String[] selectionArgs = {path + "%"};
        ContentResolver mResolver = null;
        ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();
        Cursor cursor = null;
        if (context != null) {
            mResolver = context.getContentResolver();
            cursor = mResolver.query(uri, null, selection, selectionArgs, null);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                MediaItem mediaItem = new MediaItem();
                Long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));    //音樂id
                String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))); // 音樂標題
                Log.i(TAG, "getMusicInfos: title " + title);
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 藝術家
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));    //專輯
                //String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                Long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)); // 時長
                //long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)); // 檔案大小
                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)); // 檔案路徑
                int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)); // 是否為音樂/*1*/
                String isMusicType = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));/*audio/mpeg*///是否為音樂

                GSYVideoModel gsyVideoModel = new GSYVideoModel(String.valueOf(Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id)), title + "\n" + artist + "-" + album);
                Bitmap thumbBitmap = null;
                if (url != null) {
                    thumbBitmap = getArtwork(context, id, albumId, true); //根据专辑路径获取到专辑封面图

                }
                if (isMusic != 0) { // 只把音樂新增到集合當中
                    mediaItem.setIfVideo(false);
                    mediaItem.setId(id);
                    mediaItem.setTitle(title);
                    mediaItem.setArtist(artist);
                    //  mediaItem.setAlbum(album);
                    //   mediaItem.setDisplayName(displayName);
                    //    mediaItem.setAlbumId(albumId);
                    mediaItem.setDuration(duration);
                    // mediaItem.setSize(size);
                    // mediaItem.setUrl(url);
                    mediaItem.setThumbBitmap(thumbBitmap);
                    mediaItem.setGsyVideoModel(gsyVideoModel);
                    mediaItem.setStoragePath(path);
                    mediaItems.add(mediaItem);
                }
            }
        }
        Log.i(TAG, "getMusicInfos:mediaItems.size(): "+mediaItems.size());
        return mediaItems;
    }

    public static ArrayList<MediaItem> getVideoInfos(Context context, String devicepath) {
        Log.i(TAG, "getVideoInfo satart: " + printTime());
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            Log.i(TAG, "getVideoInfos: if " + uri);
        }
        Log.i(TAG, "getVideoInfos: " + uri);
        String selection = MediaStore.Video.Media.DATA + " like ? ";
        String[] selectionArgs = {devicepath + "%"};
        ContentResolver mResolver = null;
        Cursor cursor = null;
        ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();
        if (context != null) {
            mResolver = context.getContentResolver();
            cursor = mResolver.query(uri, null, selection, selectionArgs, null);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                MediaItem mediaItem = new MediaItem();
                Long id = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Video.Media._ID));    //視頻id
                String title = cursor.getString((cursor
                        .getColumnIndex(MediaStore.Video.Media.TITLE))); // 視頻標題
                Log.i(TAG, "getVideoInfos: title: "+title);
                String artist = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Video.Media.ARTIST)); // 藝術家
                String album = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Video.Media.ALBUM));    //專輯
            /*String displayName = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));*/
                //  long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM_ID));
                long duration = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Video.Media.DURATION)); // 時長
            /*long size = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Video.Media.SIZE)); // 檔案大小*/
                String path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Video.Media.DATA)); // 檔案路徑
                String mime_type =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));/*video/mp4*/
                GSYVideoModel gsyVideoModel = new GSYVideoModel(String.valueOf(Uri.parse(MediaStore.Video.Media.EXTERNAL_CONTENT_URI + "/" + id)), title + "\n" + artist);
                Bitmap thumbBitmap = null;
                if (path != null) {
                    try {
                        thumbBitmap = getBitmapFormUrl(path, 90, 90, ThumbnailUtils.OPTIONS_RECYCLE_INPUT); //根据专辑路径获取到专辑封面图

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                if (mime_type != null) {
                    mediaItem.setIfVideo(true);
                    mediaItem.setId(id);
                    mediaItem.setTitle(title);
                    mediaItem.setArtist(artist);
                    // mediaItem.setAlbum(album);
                    // mediaItem.setDisplayName(displayName);
                    //   musicInfo.setAlbumId(albumId);
                    mediaItem.setDuration(duration);
                    // mediaItem.setSize(size);
                    // mediaItem.setUrl(path);
                    mediaItem.setThumbBitmap(thumbBitmap);
                    mediaItem.setGsyVideoModel(gsyVideoModel);
                    mediaItem.setStoragePath(path);
                    mediaItems.add(mediaItem);
                }
            }
        }
        Log.i(TAG, "getVideoInfos: mediaItems.size(): "+mediaItems.size());
        Log.i(TAG, "getVideoInfos end: " + printTime());
        return mediaItems;
    }

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getBitmapFormUrl(String videoPath, int width, int height, int kind) throws FileNotFoundException {
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MICRO_KIND);       // 获取视频的缩略图
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, kind);
        return bitmap;
    }

    public static Bitmap getArtwork(Context context, long song_id, long album_id,
                                    boolean allowdefault) {
        if (album_id < 0) {
            // This is something that is not in the database, so get the album art directly
            // from the file.
            if (song_id >= 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefault) {
                return getDefaultArtwork(context);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, sBitmapOptions);
            } catch (FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefault) {
                            return getDefaultArtwork(context);
                        }
                    }
                } else if (allowdefault) {
                    bm = getDefaultArtwork(context);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }

        return null;
    }

    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        /*byte [] art = null;
        String path = null;*/
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (FileNotFoundException ex) {

        }
       /* if (bm != null) {
            mCachedBit = bm;
        }*/
        return bm;
    }

    @SuppressLint("ResourceType")
    private static Bitmap getDefaultArtwork(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(
                context.getResources().openRawResource(R.drawable.img_album), null, opts);
    }
    // private static Bitmap mCachedBit = null;

    /**
     * 格式化時間，將毫秒轉換為分:秒格式
     *
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

}
