package com.cylan.jiafeigou.base.module;

import android.util.Log;

import com.cylan.entity.jniCall.DevUpgradleInfo;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgCount;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGDoorBellCaller;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGResult;
import com.cylan.entity.jniCall.JFGServerCfg;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.entity.jniCall.RobotMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jfgapp.interfases.AppCallBack;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.ver.PanDeviceVersionChecker;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by yanzhendong on 2017/4/14.
 */

public class BaseAppCallBackHolder implements AppCallBack {
    /**
     * 不要在这个类里做复杂的逻辑处理,所有的消息都应该以 RxBus 发送出去,在对应的地方再做处理
     */
    private Gson gson = new Gson();

    @Override
    public void OnLocalMessage(String s, int i, byte[] bytes) {
//        AppLogger.d("OnLocalMessage :" + s + ",i:" + i);
        RxBus.getCacheInstance().post(new RxEvent.LocalUdpMsg(System.currentTimeMillis(), s, (short) i, bytes));
    }

    @Override
    public void OnReportJfgDevices(JFGDevice[] jfgDevices) {
        AppLogger.d("OnReportJfgDevices" + gson.toJson(jfgDevices));
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheDeviceEvent(jfgDevices));
    }

    @Override
    public void OnUpdateAccount(JFGAccount jfgAccount) {
        AppLogger.d("OnUpdateAccount :" + gson.toJson(jfgAccount));
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheAccountEvent(jfgAccount));
    }

    @Override
    public void OnUpdateHistoryVideoList(JFGHistoryVideo jfgHistoryVideo) {
        AppLogger.d("OnUpdateHistoryVideoList :" + jfgHistoryVideo.list.size());
        BaseApplication.getAppComponent().getSourceManager().cacheHistoryDataList(jfgHistoryVideo);
    }

    @Override
    public void OnUpdateHistoryErrorCode(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {
        AppLogger.d("OnUpdateHistoryErrorCode :" + gson.toJson(jfgHistoryVideoErrorInfo));
        RxBus.getCacheInstance().post(jfgHistoryVideoErrorInfo);
    }

    @Override
    public void OnServerConfig(JFGServerCfg jfgServerCfg) {
        AppLogger.d("OnServerConfig :" + gson.toJson(jfgServerCfg));
        RxBus.getCacheInstance().post(jfgServerCfg);
    }

    @Override
    public void OnLogoutByServer(int i) {
        AppLogger.d("OnLogoutByServer:" + i);
        RxBus.getCacheInstance().post(new RxEvent.PwdHasResetEvent(i));
        BaseApplication.getAppComponent().getSourceManager().setLoginState(new LogState(LogState.STATE_ACCOUNT_OFF));
    }

    @Override
    public void OnVideoDisconnect(JFGMsgVideoDisconn jfgMsgVideoDisconn) {
        AppLogger.d("OnVideoDisconnect :" + gson.toJson(jfgMsgVideoDisconn));
        RxBus.getCacheInstance().post(jfgMsgVideoDisconn);
    }

    @Override
    public void OnVideoNotifyResolution(JFGMsgVideoResolution jfgMsgVideoResolution) {
        AppLogger.d("OnVideoNotifyResolution" + jfgMsgVideoResolution.peer);
        RxBus.getCacheInstance().post(jfgMsgVideoResolution);
    }

    @Override
    public void OnVideoNotifyRTCP(JFGMsgVideoRtcp jfgMsgVideoRtcp) {
        Log.d("", "OnVideoNotifyRTCP" + gson.toJson(jfgMsgVideoRtcp));
        RxBus.getCacheInstance().post(jfgMsgVideoRtcp);
    }

    @Override
    public void OnHttpDone(JFGMsgHttpResult jfgMsgHttpResult) {
        AppLogger.d("OnHttpDone :" + gson.toJson(jfgMsgHttpResult));
        RxBus.getCacheInstance().post(jfgMsgHttpResult);
    }

    @Override
    public void OnRobotTransmitMsg(RobotMsg robotMsg) {
        AppLogger.d("OnRobotTransmitMsg :" + gson.toJson(robotMsg));
        RxBus.getCacheInstance().post(robotMsg);
    }

    @Override
    public void OnRobotMsgAck(int i) {
        AppLogger.d("OnRobotMsgAck :" + i);
    }

    @Override
    public void OnRobotGetDataRsp(RobotoGetDataRsp robotoGetDataRsp) {
        AppLogger.d("OnRobotGetDataRsp :" + gson.toJson(robotoGetDataRsp));
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheGetDataEvent(robotoGetDataRsp));
    }

    @Override
    public void OnRobotGetDataExRsp(long l, String s, ArrayList<JFGDPMsg> arrayList) {
        RobotoGetDataRsp robotoGetDataRsp = new RobotoGetDataRsp();
        robotoGetDataRsp.identity = s;
        robotoGetDataRsp.seq = l;
        robotoGetDataRsp.put(-1, arrayList);//key在这种情况下无用
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheGetDataEvent(robotoGetDataRsp));
        AppLogger.d("OnRobotGetDataExRsp :" + s + "," + gson.toJson(arrayList));
    }

    @Override
    public void OnRobotSetDataRsp(long l, String uuid, ArrayList<JFGDPMsgRet> arrayList) {
        AppLogger.d("OnRobotSetDataRsp :" + l + gson.toJson(arrayList));
        RxBus.getCacheInstance().post(new RxEvent.SetDataRsp(l, uuid, arrayList));
    }

    @Override
    public void OnRobotGetDataTimeout(long l, String s) {
        AppLogger.d("OnRobotGetDataTimeout :" + l + ":" + s);
    }

    @Override
    public ArrayList<JFGDPMsg> OnQuerySavedDatapoint(String s, ArrayList<JFGDPMsg> arrayList) {
        AppLogger.e("这是一个bug");
        return null;
    }

    @Override
    public void OnlineStatus(boolean b) {
        AppLogger.d("OnlineStatus :" + b);
        RxBus.getCacheInstance().post(new RxEvent.OnlineStatusRsp(b));
        BaseApplication.getAppComponent().getSourceManager().setOnline(b);//设置用户在线信息
    }

    @Override
    public void OnResult(JFGResult jfgResult) {
        RxBus.getCacheInstance().post(jfgResult);
        AppLogger.i("jfgResult:" + jfgResult.code);
    }

    @Override
    public void OnDoorBellCall(JFGDoorBellCaller jfgDoorBellCaller) {
        AppLogger.d("OnDoorBellCall :" + gson.toJson(jfgDoorBellCaller));
        RxBus.getCacheInstance().post(new RxEvent.BellCallEvent(jfgDoorBellCaller, false));
    }

    @Override
    public void OnOtherClientAnswerCall(String s) {
        AppLogger.d("OnOtherClientAnswerCall:" + s);
        RxBus.getCacheInstance().post(new RxEvent.CallResponse(false));
    }

    @Override
    public void OnRobotCountDataRsp(long l, String s, ArrayList<JFGDPMsgCount> arrayList) {
        AppLogger.d("OnRobotCountDataRsp :" + l + ":" + s + "");
    }

    @Override
    public void OnRobotDelDataRsp(long l, String s, int i) {
        AppLogger.d("OnRobotDelDataRsp :" + l + " uuid:" + s + " i:" + i);
        RxBus.getCacheInstance().post(new RxEvent.DeleteDataRsp(l, s, i));
    }

    @Override
    public void OnRobotSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {
        AppLogger.d("OnRobotSyncData :" + b + " " + s + " " + new Gson().toJson(arrayList));
        RxBus.getCacheInstance().post(new RxEvent.SerializeCacheSyncDataEvent(b, s, arrayList));
    }

    @Override
    public void OnSendSMSResult(int i, String s) {
        AppLogger.d("OnSendSMSResult :" + i + "," + s);
        //store the token .
        PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN, s);
        RxBus.getCacheInstance().post(new RxEvent.SmsCodeResult(i, s));
    }

    @Override
    public void OnGetFriendListRsp(int i, ArrayList<JFGFriendAccount> arrayList) {
        AppLogger.d("OnLocalMessage :" + arrayList.size());
        RxBus.getCacheInstance().post(new RxEvent.GetFriendList(i, arrayList));
    }

    @Override
    public void OnGetFriendRequestListRsp(int i, ArrayList<JFGFriendRequest> arrayList) {
        AppLogger.d("OnLocalMessage:" + arrayList.size());
        RxBus.getCacheInstance().post(new RxEvent.GetAddReqList(i, arrayList));
    }

    @Override
    public void OnGetFriendInfoRsp(int i, JFGFriendAccount jfgFriendAccount) {
        AppLogger.d("OnLocalMessage :" + new Gson().toJson(jfgFriendAccount));
        RxBus.getCacheInstance().post(new RxEvent.GetFriendInfoCall(i, jfgFriendAccount));
    }

    @Override
    public void OnCheckFriendAccountRsp(int i, String s, String s1, boolean b) {
        AppLogger.d("OnLocalMessage :");
        RxBus.getCacheInstance().post(new RxEvent.CheckAccountCallback(i, s, s1, b));
    }

    @Override
    public void OnShareDeviceRsp(int i, String s, String s1) {
        AppLogger.d("OnShareDeviceRsp :" + i + ":" + s + ":" + s1);
        RxBus.getCacheInstance().post(new RxEvent.ShareDeviceCallBack(i, s, s1));
    }

    @Override
    public void OnUnShareDeviceRsp(int i, String s, String s1) {
        AppLogger.d("OnUnShareDeviceRsp :" + i + "," + s + "," + s1);
        RxBus.getCacheInstance().post(new RxEvent.UnshareDeviceCallBack(i, s, s1));
    }

    @Override
    public void OnGetShareListRsp(int i, ArrayList<JFGShareListInfo> arrayList) {
        AppLogger.d("OnGetShareListRsp :" + i);
        RxBus.getCacheInstance().post(new RxEvent.GetShareListCallBack(i, arrayList));
        BaseApplication.getAppComponent().getSourceManager().cacheShareList(arrayList);
    }

    @Override
    public void OnGetUnShareListByCidRsp(int i, ArrayList<JFGFriendAccount> arrayList) {
        AppLogger.d("OnGetUnShareListByCidRsp :");
        RxBus.getCacheInstance().post(new RxEvent.GetHasShareFriendCallBack(i, arrayList));
    }

    @Override
    public void OnUpdateNTP(int l) {
        AppLogger.d("OnUpdateNTP :" + l);
        PreferencesUtils.putInt(JConstant.KEY_NTP_INTERVAL, (int) (System.currentTimeMillis() / 1000 - l));
    }

    @Override
    public void OnForgetPassByEmailRsp(int i, String s) {
        AppLogger.d("OnForgetPassByEmailRsp :" + s);
        RxBus.getCacheInstance().post(new RxEvent.ForgetPwdByMail(s));
    }

    @Override
    public void OnGetAliasByCidRsp(int i, String s) {
        AppLogger.d("OnGetAliasByCidRsp :" + i + ":" + s);
    }

    @Override
    public void OnGetFeedbackRsp(int i, ArrayList<JFGFeedbackInfo> arrayList) {
        AppLogger.d("OnGetFeedbackRsp :" + ListUtils.getSize(arrayList));
        Object o = RxBus.getCacheInstance().getStickyEvent(RxEvent.GetFeedBackRsp.class);
        ArrayList<JFGFeedbackInfo> array = new ArrayList<>();
        if (o != null) {
            //先加载之前的.
            RxEvent.GetFeedBackRsp rsp = (RxEvent.GetFeedBackRsp) o;
            if (ListUtils.getSize(rsp.arrayList) > 0) array.addAll(rsp.arrayList);
        }
        if (ListUtils.getSize(arrayList) > 0) array.addAll(arrayList);
        RxBus.getCacheInstance().postSticky(new RxEvent.GetFeedBackRsp(i, array));
        BaseApplication.getAppComponent().getSourceManager().handleSystemNotification(arrayList);
    }


    @Override
    public void OnNotifyStorageType(int i) {
        AppLogger.d("OnNotifyStorageType:" + i);
        //此event是全局使用,不需要删除.因为在DataSourceManager需要用到.
        RxBus.getCacheInstance().postSticky(new RxEvent.StorageTypeUpdate(i));
        BaseApplication.getAppComponent().getSourceManager().setStorageType(i);
        BaseApplication.getAppComponent().getCmd().getAccount();
    }

    @Override
    public void OnBindDevRsp(int i, String s) {
        AppLogger.d("onBindDev: " + i + " uuid:" + s);
        RxBus.getCacheInstance().postSticky(new RxEvent.BindDeviceEvent(i, s));
        PreferencesUtils.putString(JConstant.BINDING_DEVICE, "");
    }

    @Override
    public void OnUnBindDevRsp(int i, String s) {
        AppLogger.d(String.format(Locale.getDefault(), "OnUnBindDevRsp:%d,%s", i, s));
    }

    @Override
    public void OnGetVideoShareUrl(String s) {
        AppLogger.d(String.format(Locale.getDefault(), "OnGetVideoShareUrl:%s", s));
    }

    @Override
    public void OnForwardData(byte[] bytes) {
        try {
            PanoramaEvent.MsgForward rawRspMsg = DpUtils.unpackData(bytes, PanoramaEvent.MsgForward.class);
            RxBus.getCacheInstance().post(rawRspMsg);
        } catch (IOException e) {
            e.printStackTrace();
            AppLogger.e("解析服务器透传消息失败");
        }
    }

    @Override
    public void OnMultiShareDevices(int i, String s, String s1) {
        AppLogger.d(String.format(Locale.getDefault(), "check OnMultiShareDevices:%d,%s,%s", i, s, s1));
    }

    @Override
    public void OnCheckClientVersion(int i, String s, int i1) {
        RxBus.getCacheInstance().post(new RxEvent.ClientCheckVersion(i, s, i1));
    }

    @Override
    public void OnRobotCountMultiDataRsp(long l, Object o) {
        AppLogger.d("OnRobotCountMultiDataRsp:" + o.toString());
    }

    @Override
    public void OnRobotGetMultiDataRsp(long l, Object o) {
        AppLogger.d("OnRobotGetMultiDataRsp:" + l + ":" + o);
    }


    @Override
    public void OnGetAdPolicyRsp(int i, long l, String picUrl, String tagUrl) {
        AppLogger.d("OnGetAdPolicyRsp:" + l + ":" + picUrl);
//        l = System.currentTimeMillis() + 2 * 60 * 1000;
//        tagUrl = "http://www.baidu.com";
//        picUrl = "http://cdn.duitang.com/uploads/item/201208/19/20120819131358_2KR2S.thumb.600_0.png";
        RxBus.getCacheInstance().postSticky(new RxEvent.AdsRsp().setPicUrl(picUrl).setTagUrl(tagUrl)
                .setRet(i).setTime(l));
    }

    @Override
    public void OnCheckDevVersionRsp(boolean b, String s, String s1, String s2, String s3, String s4) {
        AppLogger.d("OnCheckDevVersionRsp :" + b + ":" + s + ":" + s1 + ":" + s2 + ":" + s3);
//        b = true;
//        s = "http://yf.cylan.com.cn:82/Garfield/JFG2W/3.0.0/3.0.0.1000/201704261515/hi.bin";
//        s1 = "3.0.0";
//        s2 = "你好";
//        s3 = "xx";
        RxBus.getCacheInstance().post(new RxEvent.CheckVersionRsp(b, s, s1, s2, s3).setUuid(s4).setSeq(0));
    }


    @Override
    public void OnCheckTagDeviceVersionRsp(int ret, String cid,
                                           String tagVersion,
                                           String content,
                                           ArrayList<DevUpgradleInfo> arrayList) {
        AppLogger.d("OnCheckTagDeviceVersionRsp:" + ret + ":" + cid + ",:" + tagVersion + "," + new Gson().toJson(arrayList));
        arrayList = testList();
        cid = "290000000065";
        tagVersion = "1.0.0.009";
        content = "test";
        PanDeviceVersionChecker.BinVersion version = new PanDeviceVersionChecker.BinVersion();
        version.setCid(cid);
        version.setContent(content);
        version.setList(arrayList);
        version.setTagVersion(tagVersion);
        RxBus.getCacheInstance().post(new RxEvent.VersionRsp().setUuid(cid).setVersion(version));
    }

    private ArrayList<DevUpgradleInfo> testList() {
        ArrayList<DevUpgradleInfo> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            DevUpgradleInfo info = new DevUpgradleInfo();
            info.md5 = "";
            info.tag = i;
            info.url = tmp[i];
            info.version = "1.0.0.009";
            list.add(info);
        }
        return list;
    }

    private static final String[] tmp = new String[]{
            "http://oss-cn-hangzhou.aliyuncs.com/jiafeigou-yf/package/21/JFG5W-1.0.0.009-Kernel.bin?Expires=1527472979&Signature=m2KroyFfNhOVZi1YmzLWh14NUU4%3D&OSSAccessKeyId=xjBdwD1du8lf2wMI",
            "http://yf.cylan.com.cn:82/Garfield/Android-New/cylan/201706021000-3.2.0.286/ChangeLog.txt",
            "http://yf.cylan.com.cn:82/Garfield/Android-New/cylan/201706021000-3.2.0.286/com.cylan.jiafeigou-test1.jfgou.com_443-release-v3.2.0.286-20170602.apk",
            "http://tse4.mm.bing.net/th?id=OIP.QxZxJAfP-lq-OxYjS3bFLAFNC7&pid=15.1",
            "http://a2.att.hudong.com/18/04/14300000931600128341040320614.jpg"
    };
}
