package com.fxc.ev.mediacenter.mediaplayer;

public class Constants {
    public static final String ACTION_STATE_CHANGED_BROADCAST = "MediaPlayer.stateChanged";
    public static final String ACTION_BT_STATE_INIT_BROADCAST = "BT_MediaPlayer.state.init";
    public static final String ACTION_MEDIAITEM_CHANGED_BROADCAST = "MediaPlayer.ITEMChanged";
    public static final String ACTION_CHANGE_STATE_RECEIVER = "CSDMediaPlayer.changestate";
    public static final String ACTION_MEDIALIST_NEED_CHANGE = "MediaItemUtil.MediaItem.Changed";
    public static final int USB_DEVICE = 0;
    public static final int BLUETOOTH_DEVICE = 1;
    public static final int PLAYSTATE_CHANGED = 1;
    public static final int MEDIAITEM_CHANGED = 2;
    public static final int REPEATMODE_CHANGED = 3;
    public static final int SHUFFLEMODE_CHANGED = 4;
    public static final int PLAYSTATE_INIT = 5;

    public static final int STATE_PLAY = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_NEXT = 2;
    public static final int STATE_PREVIOUS = 3;
    public static final int STATE_SEEKTO = 4;
    public static final int STATE_RANDOM_OPEN = 5;
    public static final int STATE_RANDOM_CLOSE = 6;
    public static final int STATE_SINGLE_REPEAT = 7;
    public static final int STATE_ALL_REPEAT = 8;
}
