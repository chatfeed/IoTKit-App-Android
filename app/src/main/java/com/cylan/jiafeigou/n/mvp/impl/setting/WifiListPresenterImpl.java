package com.cylan.jiafeigou.n.mvp.impl.setting;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.ScanResultListFilter;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.setting.WifiListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.n.mvp.contract.setting.WifiListContract.ERR_NO_RAW_LIST;

/**
 * Created by cylan-hunt on 17-2-12.
 */

public class WifiListPresenterImpl extends AbstractPresenter<WifiListContract.View>
        implements WifiListContract.Presenter {
    private WifiManager wifiManager;


    public WifiListPresenterImpl(WifiListContract.View view, String uuid) {
        super(view);
        wifiManager = (WifiManager) ContextUtils.getContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void startScan() {
        Subscription subscription = Observable.just("scan")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    if (wifiManager != null) {
                        wifiManager.startScan();
                    }
                    return null;
                })
                .timeout(1, TimeUnit.SECONDS)
                .filter(o -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                }, throwable -> {
                    if (throwable instanceof TimeoutException) {
                        mView.onErr(ERR_NO_RAW_LIST);
                    }
                    AppLogger.e(MiscUtils.getErr(throwable));
                });
        addSubscription(subscription, "startScan");

    }

    @Override
    public void sendWifiInfo(String ssid, String pwd, int security) {
        String mac = getLatestMac();
        if (TextUtils.isEmpty(mac)) {
            Subscription subscription = RxBus.getCacheInstance().toObservable(JfgUdpMsg.FPingAck.class)
                    .subscribeOn(Schedulers.io())
                    .throttleFirst(500, TimeUnit.MILLISECONDS)
                    .subscribe((JfgUdpMsg.FPingAck fPingAck) -> {
                        if (TextUtils.equals(uuid, fPingAck.cid)) {
                            sendInfo(ssid, pwd, security, fPingAck.mac);
                            AppLogger.i(String.format(Locale.getDefault(), "send info: %s_%s_%s_%s", ssid, pwd, mac, security));
                        }
                    }, (Throwable throwable) -> AppLogger.e("err:" + throwable.getLocalizedMessage()));
            addSubscription(subscription, "sendBindConfig");
            return;
        }
        sendInfo(ssid, pwd, security, mac);
    }

    private void sendInfo(String ssid, String pwd, int security, String mac) {
        JfgUdpMsg.DoSetWifi setWifi = new JfgUdpMsg.DoSetWifi(uuid,
                mac,
                ssid, pwd);
        setWifi.security = security;
        //发送wifi配置
        try {
            Command.getInstance().sendLocalMessage(UdpConstant.IP,
                    UdpConstant.PORT,
                    setWifi.toBytes());
            Command.getInstance().sendLocalMessage(UdpConstant.IP,
                    UdpConstant.PORT,
                    setWifi.toBytes());
            Command.getInstance().sendLocalMessage(UdpConstant.PIP,
                    UdpConstant.PORT,
                    setWifi.toBytes());
            Command.getInstance().sendLocalMessage(UdpConstant.PIP,
                    UdpConstant.PORT,
                    setWifi.toBytes());
            AppLogger.i(TAG + new Gson().toJson(setWifi));
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    /**
     * 每次fping都是为了得到最新的mac
     */
    private String getLatestMac() {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        String m = device.$(202, "");
        if (JConstant.MAC_REG.matcher(m).find()) {
            AppLogger.i("get mac from local: " + m);
            return m;
        }
        return "";
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{
                WifiManager.RSSI_CHANGED_ACTION,
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION,
                ConnectivityManager.CONNECTIVITY_ACTION
        };
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        final String action = intent.getAction();
        if (TextUtils.equals(action, WifiManager.RSSI_CHANGED_ACTION)
                || TextUtils.equals(action, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            updateWifiResults(wifiManager.getScanResults());
        }
    }

    /**
     * wifi列表
     */
    private void updateWifiResults(List<ScanResult> scanResults) {
        Subscription subscription = Observable.just(scanResults)
                //别那么频繁
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .filter((List<ScanResult> s) -> {
                    //非空返回,如果空,下面的map是不会有结果.
                    return getView() != null;
                })
                .map((List<ScanResult> results) -> {
                    ArrayList<ScanResult> list = ScanResultListFilter.extractPretty(results, false);
                    Collections.sort(list, (ScanResult lhs, ScanResult rhs) -> {
                        int left = WifiManager.calculateSignalLevel(lhs.level, 5);
                        int right = WifiManager.calculateSignalLevel(rhs.level, 5);
                        return right - left;
                    });
                    return list;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((List<ScanResult> s) -> {
                    getView().onResults((ArrayList<ScanResult>) s);
                    AppLogger.i("wifiList: " + s.size());
                }, new RxHelper.EmptyException("resultList call"));
        addSubscription(subscription, "updateWifiResults");
    }

}
