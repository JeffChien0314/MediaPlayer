package com.example.fxc.mediaplayer;

/**
 * Created by Sandra on 2022/2/10.
 */

public class ExternalDeviceInfo {
    public ExternalDeviceInfo() {
    }

    private String storagePath;//设备路径
    private String description;//设备名字
    private int resImage;//设备图片
    private int type;//设备类型
    private  boolean isRemovableResult;


    public int getResImage() {
        return resImage;
    }

    public void setResImage(int resImage) {
        this.resImage = resImage;
    }
    //設備圖標

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRemovableResult() {
        return isRemovableResult;
    }

    public void setRemovableResult(boolean removableResult) {
        isRemovableResult = removableResult;
    }
}
