package com.fxc.ev.mediacenter.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fxc.mediaplayer.R;
import com.fxc.ev.mediacenter.datastruct.DeviceItem;
import com.fxc.ev.mediacenter.util.Constants;

import java.util.List;

/**
 * Created by Sandra on 2022/2/11.
 */

public class DeviceListAdapter extends BaseAdapter {
    private Context context;
    private List<DeviceItem> externalDeviceItems;
    private DeviceItem externalDeviceItem ;

    public DeviceListAdapter() {
    }

    public DeviceListAdapter(Context context, List<DeviceItem> externalDeviceItems/*, DeviceItem currentDevice*/) {
        this.context = context;
        this.externalDeviceItems = externalDeviceItems;
      //  this.mCurrentDevice = currentDevice;
    }

    @Override
    public int getCount() {
        return externalDeviceItems.size();
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
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.device_list, null);
            viewHolder.description = (TextView) convertView.findViewById(R.id.description);
            viewHolder.deviceImage = (ImageView) convertView.findViewById(R.id.device_icon);
            convertView.setTag(viewHolder);            //表示給View新增一個格外的資料，
        } else {
            viewHolder = (DeviceListAdapter.ViewHolder) convertView.getTag();//通過getTag的方法將資料取出來
        }
        externalDeviceItem = externalDeviceItems.get(position);
        viewHolder.description.setText(externalDeviceItem.getDescription());//設備名稱
        if (externalDeviceItem.getType() == Constants.BLUETOOTH_DEVICE) {
            viewHolder.deviceImage.setImageResource(R.drawable.icon_bt);
        } else {
            viewHolder.deviceImage.setImageResource(R.drawable.icon_usb);        //設備圖標
        }

        // viewHolder.deviceImage.setImageBitmap(externalDeviceItem.getThumbBitmap());
        return convertView;
    }

    /**
     * 定義一個內部類
     * 宣告相應的控制元件引用
     */
    public class ViewHolder {
        //所有控制元件物件引用
        public ImageView deviceImage;    //設備圖片
        public TextView description;    //設備名字

       /* public TextView musicDuration;	//音樂時長
        public TextView musicArtist;	//音樂藝術家*/
    }
}
