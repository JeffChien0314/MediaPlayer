package com.fxc.ev.mediacenter.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.datastruct.DeviceItem;
import com.fxc.ev.mediacenter.datastruct.MediaInfo;
import com.fxc.ev.mediacenter.datastruct.MediaItem;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static com.fxc.ev.mediacenter.ContentFragment.printTime;

/**
 * Created by Sandra on 2022/2/10.
 */

public class MediaItemUtil {
    public static final int TYPE_MUSIC = 0;
    public static final int TYPE_VIDEO = 1;
    public static final String MUSIC="Music";
    public static final String VIDEO="Video";
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    public  static ArrayList<MediaItem> allDevicesMediaItems = new ArrayList<>();
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
        }
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
                Long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));    //??????id
                String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))); // ????????????
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)); // ?????????
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));    //??????
                //String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                Long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)); // ??????
                //long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)); // ????????????
                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)); // ????????????
                int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)); // ???????????????/*1*/
                //String isMusicType = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));/*audio/mpeg*///???????????????

                GSYVideoModel gsyVideoModel = new GSYVideoModel(String.valueOf(Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id)), title + "\n" + artist + "-" + album);
                Bitmap thumbBitmap = null;
                if (url != null) {
                    thumbBitmap = getArtwork(context, id, albumId, true); //??????????????????????????????????????????

                }
                if (isMusic != 0) { // ?????????????????????????????????
                    mediaItem.setIfVideo(false);
                    mediaItem.setId(id);
                    mediaItem.setTitle(title);
                    mediaItem.setArtist(artist);
                      mediaItem.setAlbum(album);
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
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        }
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
                        .getColumnIndex(MediaStore.Video.Media._ID));    //??????id
                String title = cursor.getString((cursor
                        .getColumnIndex(MediaStore.Video.Media.TITLE))); // ????????????
                String artist = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Video.Media.ARTIST)); // ?????????
                String album = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Video.Media.ALBUM));    //??????
            /*String displayName = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));*/
                //  long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM_ID));
                long duration = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Video.Media.DURATION)); // ??????
            /*long size = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Video.Media.SIZE)); // ????????????*/
                String path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Video.Media.DATA)); // ????????????
                String mime_type =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));/*video/mp4*/
                GSYVideoModel gsyVideoModel = new GSYVideoModel(String.valueOf(Uri.parse(MediaStore.Video.Media.EXTERNAL_CONTENT_URI + "/" + id)), title + "\n" + artist);
                Bitmap thumbBitmap = null;
                if (path != null) {
                    thumbBitmap=ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MICRO_KIND);//??????????????????????????????????????????
                }
                if (mime_type != null) {
                    mediaItem.setIfVideo(true);
                    mediaItem.setId(id);
                    mediaItem.setTitle(title);
                    mediaItem.setArtist(artist);
                     mediaItem.setAlbum(album);

                    // mediaItem.setDisplayName(displayName);
                    //   musicInfo.setAlbumId(albumId);
                    mediaItem.setDuration(duration);
                    // mediaItem.setSize(size);
                    // mediaItem.setUrl(path);
                    mediaItem.setThumbBitmap(thumbBitmap);
                    mediaItem.setGsyVideoModel(gsyVideoModel);
                    mediaItem.setStoragePath(devicepath);
                    mediaItems.add(mediaItem);
                }
            }
        }
        Log.i(TAG, "getVideoInfos: mediaItems.size(): "+mediaItems.size());
        Log.i(TAG, "getVideoInfos end: " + printTime());
        return mediaItems;
    }

    /**
     * ??????????????????
     * ??????ThumbnailUtils????????????????????????????????????
     * ?????????????????????????????????????????????MICRO_KIND?????????????????????MICRO_KIND??????kind?????????????????????????????????
     * @param width     ????????????????????????????????????
     * @param height    ???????????????????????????????????????
     * @param kind      ??????MediaStore.Images.Thumbnails???????????????MINI_KIND???MICRO_KIND???
     *                  ?????????MINI_KIND: 512 x 384???MICRO_KIND: 96 x 96
     * @return ??????????????????????????????
     */
    public static Bitmap cutDownBitmap(Bitmap bitmap) throws FileNotFoundException {
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 90, 90/*,ThumbnailUtils.OPTIONS_RECYCLE_INPUT*/);
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
           /* if (albumid < 0) {
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
                }*/
            Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songid);
            uri= Uri.parse(uri+"/albumart");
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (FileNotFoundException ex) {

        }
       /* if (bm != null) {
            mCachedBit = bm;
        }*/
        return bm;
    }

    @SuppressLint("ResourceType")
    public static Bitmap getDefaultArtwork(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(
                context.getResources().openRawResource(R.drawable.img_album), null, opts);
    }
    // private static Bitmap mCachedBit = null;

    /**
     * ???????????????????????????????????????:?????????
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
    public static int getBitmapSize(Bitmap bitmap){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){    //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){//API 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
    }
}
