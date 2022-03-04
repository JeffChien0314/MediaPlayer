package com.example.fxc.mediaplayer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Sandra on 2022/3/3.
 */

public class SaveData {
    public SaveData() {
    }

    public void saveToFile(Context context, int currentTab, String storagePath, String description) {
        SharedPreferences sharedPreferences =context.getSharedPreferences("currentStatus", Context.MODE_PRIVATE); //私有数据
        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
        editor.putInt("currentTab", currentTab);
        editor.putString("storagePath", storagePath);
        editor.putString("description", description);
        editor.commit();//提交修改
    }
    public int getCurrentTab(Context context){
        SharedPreferences share=context.getSharedPreferences("currentStatus",Context .MODE_PRIVATE);
        return  share.getInt("currentTab",0);
    }
    public String getCurrentDevicestoragePath(Context context){
        SharedPreferences share=context.getSharedPreferences("currentStatus",Context .MODE_PRIVATE);
        return share.getString("storagePath","");
    }
}
