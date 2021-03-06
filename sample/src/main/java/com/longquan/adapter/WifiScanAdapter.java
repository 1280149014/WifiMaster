 
 package com.longquan.adapter;

/**
 * author : charile yuan
 * date   : 21-2-23
 * desc   :
 */
import android.content.Context;


import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.longquan.R;
import com.longquan.bean.WifiInfo;
import com.longquan.utils.LogUtils;
import com.longquan.utils.WifiHelper;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WifiScanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String TAG = WifiScanAdapter.class.getSimpleName();
    private Context mContext;
    private final int ITEM_TYPE_CONTENT = 1;
    private final int ITEM_TYPE_FOOTER = 2;
    private List<WifiInfo> mData = new ArrayList<>();
    private onClickListener mOnClickListener;
    private WifiInfo mConnecting;

    public WifiScanAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<WifiInfo> data) {
        this.mData = data;
    }

    public void setOnClickListener(onClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == ITEM_TYPE_CONTENT) {
            return new APViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_wifi_general, viewGroup, false));
        } else if (viewType == ITEM_TYPE_FOOTER) {
            return new FooterViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_wifi_add_others_general, viewGroup, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder == null) {
            return;
        }
        if (holder instanceof APViewHolder) {
            APViewHolder apViewHolder = (APViewHolder) holder;
            WifiInfo selected = mData.get(position);
            apViewHolder.itemView.setOnClickListener(v -> {
                LogUtils.d(TAG, "item click position:" + position + ", selected = " + selected);
                clearSelectedState();
                selected.isConnecting = true;
                mConnecting = selected;
//                    notifyDataSetChanged();
                if (mOnClickListener != null) {
                    mOnClickListener.onItemClickListener(position, selected);
                }
            });
            apViewHolder.wifiName.setText(selected.getSsid());
            apViewHolder.wifiSecurity.setText(selected.getCapabilities());
            apViewHolder.wifiConnectState.setVisibility(View.VISIBLE);
            if(!getConnectStateText(selected).isEmpty()){
                apViewHolder.wifiConnectState.setVisibility(View.VISIBLE);
                apViewHolder.wifiConnectState.setText(getConnectStateText(selected));
            } else if(selected.getCapabilities().equals("[ESS][WFA-HT]")
                    || selected.getCapabilities().equals("[ESS][WFA-HT][WFA-VHT]")){
                apViewHolder.wifiConnectState.setVisibility(View.VISIBLE);
                apViewHolder.wifiConnectState.setText(R.string.networks_may_need_authentication);
            } else {
                apViewHolder.wifiConnectState.setVisibility(View.GONE);
            }
            apViewHolder.pb.setVisibility(selected.isConnecting ? View.VISIBLE : View.GONE);
            apViewHolder.wifiLock.setVisibility(selected.getSecurity() != WifiInfo.Security.NONE ? View.VISIBLE : View.INVISIBLE);
            apViewHolder.wifiSignal.setImageResource(selected.getWifiLevelImageWithoutCryp());
            apViewHolder.wifiInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.d(TAG, "info click position:" + position);
                    if (mOnClickListener != null) {
                        mOnClickListener.onItemInfoClickListener(position, selected);
                    }
                }
            });
        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            footerViewHolder.addOthers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onAddOthersNetwork();
                    }
                }
            });
        }
    }

    private String getConnectStateText(WifiInfo selected) {
        if(selected.isConnect()){
            return mContext.getResources().getString(R.string.value_wifi_connected);
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= mData.size()) {
            return ITEM_TYPE_FOOTER;
        } else {
            return ITEM_TYPE_CONTENT;
        }
    }

    public static class APViewHolder extends RecyclerView.ViewHolder {
        TextView wifiName;
        TextView wifiSecurity;
        TextView wifiConnectState;
        LottieAnimationView pb;
        ImageView wifiLock;
        ImageView wifiSignal;
        ImageView wifiInfo;

        public APViewHolder(@NonNull View itemView) {
            super(itemView);
            wifiName = itemView.findViewById(R.id.tv_wifi_name);
            wifiSecurity = itemView.findViewById(R.id.tv_wifi_security);
            pb = itemView.findViewById(R.id.pb_icon);
            wifiLock = itemView.findViewById(R.id.img_wifi_lock);
            wifiSignal = itemView.findViewById(R.id.img_signal);
            wifiInfo = itemView.findViewById(R.id.img_info);
            wifiConnectState = itemView.findViewById(R.id.tv_wifi_connected_state);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        Button addOthers;

        public FooterViewHolder(View itemView) {
            super(itemView);
            addOthers = itemView.findViewById(R.id.bt_add_others);
        }
    }

    public void updateData(List<WifiInfo> newData) {
        mData.clear();
        LogUtils.d(TAG, "updateData DataList size:" + newData.size());
        updateConnectingState(newData);
        WifiInfo curConnected = WifiHelper.getConnectedWifi(newData);
        LogUtils.d(TAG, "updateData curConnected:"+curConnected);
        if (curConnected != null) {
            mData.addAll(WifiHelper.removeWifi(newData, curConnected));
        } else {
            mData.addAll(newData);
        }
        Collections.sort(mData);
        if(curConnected != null){
            mData.add(0,curConnected);
        }
        notifyDataSetChanged();
    }

    private void updateConnectingState(List<WifiInfo> newData) {
        if (mConnecting == null) {
            LogUtils.d(TAG, "updateConnectingState null");
            return;
        }
        for (WifiInfo info : newData) {
            if (mConnecting != null && mConnecting.getSsid().equals(info.getSsid())) {
                LogUtils.d(TAG, "updateConnectingState info:" + info.getSsid() + " before refreshConnecting -isConnecting- " + info.isConnecting);
                break;
            }
        }
        WifiInfo found = findBySSID(newData, mConnecting.getSsid());
        if (found != null) {
            found.isConnecting = mConnecting.isConnecting;
            LogUtils.d(TAG, "updateConnectingState info:" + found.getSsid() + " after refreshConnecting -isConnecting- " + found.isConnecting);
        } else {
            mConnecting = null;
        }
    }

    private void clearSelectedState() {
        if (mData == null) {
            return;
        }
        for (WifiInfo info : mData) {
            info.isConnecting = false;
        }
    }

    public void updateWifiListRecConnected(WifiInfo connected) {
        LogUtils.d(TAG, "updateWifiListRecConnected -- WifiInfo:" + connected);
        clearSelectedState();
        if (connected != null) {
            WifiHelper.removeWifi(mData, connected);
            if (mConnecting != null && mConnecting.getSsid().equalsIgnoreCase(connected.getSsid())) {
                mConnecting = null;
            }
            Collections.sort(mData);
            notifyDataSetChanged();
        }
    }

    public void updateWifiListRecDisConnected(){
        clearSelectedState();
    }

    public void updateWifiListOnRssiChange(WifiInfo wifiInfo) {
        LogUtils.d(TAG, "updateWifiListOnRssiChange -- WifiInfo:" + wifiInfo);
        updateWifiListRecConnected(wifiInfo);
    }

    private WifiInfo findBySSID(List<WifiInfo> list, String ssid) {
        if (list == null || TextUtils.isEmpty(ssid)) {
            return null;
        }
        WifiInfo found = null;
        for (WifiInfo info : list) {
            if (ssid.equals(info.getSsid())) {
                found = info;
            }
        }
        return found;
    }

    public void updateWifiListOnCancel(WifiInfo toCancel) {
        LogUtils.d(TAG, "updateWifiListOnCancel -- WifiInfo:" + toCancel);
        toCancel.isConnecting = false;
        clearSelectedState();
        notifyDataSetChanged();
        mConnecting = null;
    }

    private int getIndexOf(WifiInfo info) {
        WifiInfo toFind = null;
        for (WifiInfo it : mData) {
            if (it.getSsid().equals(info.getSsid())) {
                toFind = it;
            }
        }
        return mData.indexOf(toFind);
    }

    public void refreshConnectFail() {
        clearSelectedState();
    }

    public void updateWifiListOnDoJoin(WifiInfo toJoin) {
        LogUtils.d(TAG, "updateWifiListOnDoJoin:"+toJoin);
        if (getIndexOf(toJoin) != -1) {
            clearSelectedState();
            toJoin.isConnecting = true;
            mConnecting = toJoin;
            notifyDataSetChanged();
        }
    }


    public interface onClickListener {
        void onItemClickListener(int position, WifiInfo selected);

        void onItemInfoClickListener(int position, WifiInfo selected);

        void onAddOthersNetwork();
    }
}
