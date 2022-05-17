package com.fxc.ev.mediacenter;
// Declare any non-default types here with import statements
import com.fxc.ev.mediacenter.datastruct.MediaItem;
import com.fxc.ev.mediacenter.IDataChangeListener;

interface IMyAidlInterface {
    MediaItem getMediaItem();
               long getCurrentProgress();
   void registerSetupNotification(IDataChangeListener listener);
   void unRegisterSetupNotification(IDataChangeListener listener);
}
