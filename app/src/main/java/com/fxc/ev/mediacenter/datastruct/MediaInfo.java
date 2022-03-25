package com.fxc.ev.mediacenter.datastruct;

import java.util.ArrayList;

public class MediaInfo {
    private ArrayList<MediaItem> mediaItems;
    private DeviceItem deviceItem;

    public MediaInfo() {
    }

    public MediaInfo(ArrayList<MediaItem> mediaItems, DeviceItem deviceItem) {
        this.mediaItems = mediaItems;
        this.deviceItem = deviceItem;
    }

    public ArrayList<MediaItem> getMediaItems() {
        return mediaItems;
    }

    public void setMediaItems(ArrayList<MediaItem> mediaItems) {
        this.mediaItems = mediaItems;
    }

    public DeviceItem getDeviceItem() {
        return deviceItem;
    }

    public void setDeviceItem(DeviceItem deviceItem) {
        this.deviceItem = deviceItem;
    }
}
