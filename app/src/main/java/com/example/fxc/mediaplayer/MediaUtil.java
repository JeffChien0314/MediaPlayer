package com.example.fxc.mediaplayer;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
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

/**
 * Created by Sandra on 2022/2/10.
 */

public class MediaUtil {
    public static final int TYPE_MUSIC = 0;
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

    public static List<MediaInfo> getMediaInfos(int mediaType, Context context, DeviceInfo deviceInfo) {
        if (mediaType == TYPE_MUSIC) {//TYPE_MUSIC = 0
            return getMusicInfos(context, deviceInfo.getStoragePath());
        } else {
            return getVideoInfos(context, deviceInfo.getStoragePath());
        }
    }

    public static List<MediaInfo> getMusicInfos(Context context, String path) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri=  MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        }
        String selection = MediaStore.Audio.Media.DATA + " like ? ";
        String[] selectionArgs = {path + "%"};
        ContentResolver mResolver = context.getContentResolver();
        Cursor cursor = mResolver.query(uri, null, selection, selectionArgs, null);
        List<MediaInfo> musicInfos = new ArrayList<MediaInfo>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            MediaInfo musicInfo = new MediaInfo();
            Long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));    //音樂id
            String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))); // 音樂標題
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 藝術家
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));    //專輯
            //String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            Long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)); // 時長
            //long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)); // 檔案大小
            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)); // 檔案路徑
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)); // 是否為音樂/*1*/
            String isMusicType = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));/*audio/mpeg*///是否為音樂
            Log.i(TAG, "getMusicInfos:isMusicType " + isMusicType);
            GSYVideoModel gsyVideoModel = new GSYVideoModel(String.valueOf(Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id)), title+"\n"+artist);
            Bitmap thumbBitmap = null;
            if (url != null) {
            //    thumbBitmap = getArtwork(context, id, albumId, true); //根据专辑路径获取到专辑封面图

            }
            if (isMusic != 0) { // 只把音樂新增到集合當中
                musicInfo.setIfVideo(false);
                musicInfo.setId(id);
                musicInfo.setTitle(title);
                musicInfo.setArtist(artist);
              //  musicInfo.setAlbum(album);
             //   musicInfo.setDisplayName(displayName);
            //    musicInfo.setAlbumId(albumId);
                musicInfo.setDuration(duration);
               // musicInfo.setSize(size);
               // musicInfo.setUrl(url);
                musicInfo.setThumbBitmap(thumbBitmap);
                musicInfo.setGsyVideoModel(gsyVideoModel);
                musicInfos.add(musicInfo);
            }
        }
        return musicInfos;
    }

    public static List<MediaInfo> getVideoInfos(Context context, String path) {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Log.i(TAG, "getVideoInfos: " + uri);
        String selection = MediaStore.Video.Media.DATA + " like ? ";
        String[] selectionArgs = {path + "%"};
        ContentResolver mResolver = context.getContentResolver();
        Cursor cursor = mResolver.query(uri, null, selection, selectionArgs, null);
        List<MediaInfo> VideoInfos = new ArrayList<MediaInfo>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            MediaInfo videoinfo = new MediaInfo();
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Video.Media._ID));    //視頻id
            String title = cursor.getString((cursor
                    .getColumnIndex(MediaStore.Video.Media.TITLE))); // 視頻標題
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
            String url = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Video.Media.DATA)); // 檔案路徑
            String mime_type =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));/*video/mp4*/
            GSYVideoModel gsyVideoModel = new GSYVideoModel(String.valueOf(Uri.parse(MediaStore.Video.Media.EXTERNAL_CONTENT_URI + "/" + id)), title+"\n"+artist);
            Bitmap thumbBitmap = null;
            if (url != null) {
              /*  try {
                    thumbBitmap = getBitmapFormUrl(url); //根据专辑路径获取到专辑封面图
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }*/
            }
            if (mime_type != null) {
                videoinfo.setIfVideo(true);
                videoinfo.setId(id);
                videoinfo.setTitle(title);
                videoinfo.setArtist(artist);
               // videoinfo.setAlbum(album);
               // videoinfo.setDisplayName(displayName);
                //   musicInfo.setAlbumId(albumId);
                videoinfo.setDuration(duration);
               // videoinfo.setSize(size);
               // videoinfo.setUrl(url);
                videoinfo.setThumbBitmap(thumbBitmap);
                videoinfo.setGsyVideoModel(gsyVideoModel);
                VideoInfos.add(videoinfo);
            }
        }
        return VideoInfos;
    }

    public static Bitmap getBitmapFormUrl(String url) throws FileNotFoundException {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        FileInputStream inputStream = new FileInputStream(new File(url).getAbsolutePath());
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(inputStream.getFD());
            } else {
                retriever.setDataSource(url);

            }
       /*getFrameAtTime()--->在setDataSource()之后调用此方法。
       如果可能，该方法在任何时间位置找到代表性的帧，
               并将其作为位图返回。这对于生成输入数据源的缩略图很有用。**/

            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
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
