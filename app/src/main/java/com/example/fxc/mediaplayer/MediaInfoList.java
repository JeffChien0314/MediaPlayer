package com.example.fxc.mediaplayer;

import java.util.ArrayList;

public class MediaInfoList {
    private ArrayList<MediaInfo> mediaInfos;
    private DeviceInfo deviceInfo;

    public MediaInfoList(ArrayList<MediaInfo> mediaInfos, DeviceInfo deviceInfo) {
        this.mediaInfos = mediaInfos;
        this.deviceInfo = deviceInfo;
    }

    public ArrayList<MediaInfo> getMediaInfos() {
        return mediaInfos;
    }

    public void setMediaInfos(ArrayList<MediaInfo> mediaInfos) {
        this.mediaInfos = mediaInfos;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
