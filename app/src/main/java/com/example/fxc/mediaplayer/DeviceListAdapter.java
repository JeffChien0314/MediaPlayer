package com.example.fxc.mediaplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Sandra on 2022/2/11.
 */

public class DeviceListAdapter extends BaseAdapter {
    private Context context;
    private List<ExternalDeviceInfo> externalDeviceInfos;
    private ExternalDeviceInfo externalDeviceInfo;

    public DeviceListAdapter() {
    }
    public DeviceListAdapter(Context context,List<ExternalDeviceInfo> externalDeviceInfos) {
        this.context = context;
        this.externalDeviceInfos = externalDeviceInfos;
    }
    @Override
    public int getCount() {
        return externalDeviceInfos.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeviceListAdapter.ViewHolder viewHolder = null;
        if(convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.device_list, null);
            viewHolder.description = (TextView) convertView.findViewById(R.id.description);
            viewHolder.deviceImage=(ImageView) convertView.findViewById(R.id.device_icon);
            convertView.setTag(viewHolder);			//表示給View新增一個格外的資料，
        } else {
            viewHolder = (DeviceListAdapter.ViewHolder)convertView.getTag();//通過getTag的方法將資料取出來
        }
        externalDeviceInfo = externalDeviceInfos.get(position);
        viewHolder.description.setText(externalDeviceInfo.getDescription());//設備名稱
        viewHolder.deviceImage.setImageResource(externalDeviceInfo.getResImage());		//設備圖標
       // viewHolder.deviceImage.setImageBitmap(externalDeviceInfo.getThumbBitmap());
        return convertView;
    }

    /**
     * 定義一個內部類
     * 宣告相應的控制元件引用
     *
     */
    public class ViewHolder {
        //所有控制元件物件引用
        public ImageView deviceImage;	//設備圖片
        public TextView description;	//設備名字
       /* public TextView musicDuration;	//音樂時長
        public TextView musicArtist;	//音樂藝術家*/
    }
}
