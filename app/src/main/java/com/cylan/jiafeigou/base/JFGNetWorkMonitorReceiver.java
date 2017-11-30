package com.cylan.jiafeigou.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.view.JFGSourceManager;

public class JFGNetWorkMonitorReceiver extends BroadcastReceiver {

    public JFGNetWorkMonitorReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        JFGSourceManager sourceManager = DataSourceManager.getInstance();
//        if (sourceManager.monitorPersonInformation() == null) {//只在有账号信息的情况下发送网络状态变化通知
//            return;
//        }
//        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//        //改变背景或者 处理网络的全局变量
//        AppLogger.d("当前网络可用");
//        RxEvent.NetConnectionEvent connectionEvent = new RxEvent.NetConnectionEvent(true);
//        connectionEvent.mobile = mobNetInfo;
//        connectionEvent.wifi = wifiNetInfo;
//        connectionEvent.isOnLine = sourceManager.isOnline();
//        RxBus.getCacheInstance().post(connectionEvent);
    }
}
