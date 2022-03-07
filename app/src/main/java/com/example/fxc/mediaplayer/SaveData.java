package com.example.fxc.mediaplayer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Sandra on 2022/3/3.
 */

public class SaveData {
    public SaveData() {
    }

    public void saveToFile(Context context,int currPosition, int currentTab, String storagePath, String description) {
        SharedPreferences sharedPreferences =context.getSharedPreferences("currentStatus", Context.MODE_PRIVATE); //私有数据
        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
        editor.putString("description", description);
        editor.putString("storagePath", storagePath);
        editor.putInt("currentTab", currentTab);
        editor.putInt("currPosition", currPosition);
        editor.commit();//提交修改
    }
    public int getCurrentTab(Context context){
        SharedPreferences share=context.getSharedPreferences("currentStatus",Context .MODE_PRIVATE);
        return  share.getInt("currentTab",0);
    }
    public int getCurrentPosition(Context context){
        SharedPreferences share=context.getSharedPreferences("currentStatus",Context .MODE_PRIVATE);
        return  share.getInt("currPosition",0);
    }
    public String getCurrentDevicestoragePath(Context context){
        SharedPreferences share=context.getSharedPreferences("currentStatus",Context .MODE_PRIVATE);
        return share.getString("storagePath","");
    }
}
