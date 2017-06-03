package com.cylan.jiafeigou.base.module;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDoorBellCaller;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscription;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpUtils.unpack;
import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/**
 * Created by yanzhendong on 2017/4/14.
 */
@Singleton
public class BaseUdpMsgParser {
    @Inject
    public BaseUdpMsgParser() {
    }

    public Subscription initSubscription() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                .subscribeOn(Schedulers.io())
                .retry((integer, throwable) -> true)
                .subscribe(msg -> {
                    try {
                        parserUdpMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        AppLogger.e(e.getMessage());
                    }
                }, AppLogger::e);
    }

    private void parserUdpMessage(RxEvent.LocalUdpMsg localUdpMsg) throws Exception {
        JfgUdpMsg.UdpRecvHeard header = unpackData(localUdpMsg.data, JfgUdpMsg.UdpRecvHeard.class);
        if (header == null) return;
        AppLogger.e("局域网消息命令:" + header.cmd);

        switch (header.cmd) {
            case UdpConstant.PING_ACK: {
                JfgUdpMsg.PingAck pingAck = unpackData(localUdpMsg.data, JfgUdpMsg.PingAck.class);
                RxBus.getCacheInstance().post(pingAck);
                break;
            }
            case UdpConstant.F_PING_ACK: {
                JfgUdpMsg.FPingAck f_pingAck = unpackData(localUdpMsg.data, JfgUdpMsg.FPingAck.class);
                RxBus.getCacheInstance().post(f_pingAck);
                break;
            }
            case UdpConstant.DOORBELL_RING: {
                Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(header.cid);
                if (device != null && device.available()) {//说明当前账号有这个设备
                    JFGDoorBellCaller caller = new JFGDoorBellCaller();
                    caller.cid = header.cid;
                    RxEvent.BellCallEvent callEvent = new RxEvent.BellCallEvent(caller);
                    callEvent.isFromLocal = true;
                    RxBus.getCacheInstance().post(callEvent);
                }
                break;
            }
            case UdpConstant.REPORT_MSG: {
                try {
                    PanoramaEvent.ReportMsg reportMsg = unpackData(localUdpMsg.data, PanoramaEvent.ReportMsg.class);
                    if (reportMsg == null) return;
                    List<PanoramaEvent.DpMsgForward> forwards = null;
                    PanoramaEvent.MsgForward msgForward = unpackData(reportMsg.bytes, PanoramaEvent.MsgForward.class);
                    if (msgForward != null) {
                        Type type = new TypeToken<List<PanoramaEvent.DpMsgForward>>() {
                        }.getType();
                        forwards = unpack(msgForward.msg, type);
                    }
                    if (forwards != null) {
                        ArrayList<Long> idList = new ArrayList<>();
                        ArrayList<JFGDPMsg> msgList = new ArrayList<>();
                        for (PanoramaEvent.DpMsgForward forward : forwards) {
                            idList.add(forward.id);
                            msgList.add(forward.msg());
                        }
                        RxEvent.DeviceSyncRsp syncData = new RxEvent.DeviceSyncRsp().setUuid(header.cid, idList, msgList);
                        RxBus.getCacheInstance().post(syncData);
                    }
                } catch (IOException e) {
                    AppLogger.e(e.getMessage());
                }
                break;
            }
        }
    }
}
