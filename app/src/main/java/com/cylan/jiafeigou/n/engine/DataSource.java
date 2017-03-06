package com.cylan.jiafeigou.n.engine;

import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

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
import com.cylan.ext.opt.DebugOptionsImpl;
import com.cylan.jfgapp.interfases.AppCallBack;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.misc.efamily.MsgpackMsg;
import com.cylan.jiafeigou.n.view.cloud.CloudLiveCallActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.stat.MtaManager;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;


public class DataSource implements AppCallBack {

    static {
        System.loadLibrary("jfgsdk");
        System.loadLibrary("sqlcipher");
    }

    private static DataSource instance;

    private DataSource() {
    }

    public static DataSource getInstance() {
        if (instance == null) {
            synchronized (DataSource.class) {
                if (instance == null)
                    instance = new DataSource();
            }
        }
        return instance;
    }

    public void onCreate() {
        initNative();
        GlobalUdpDataSource.getInstance().register();
        GlobalBellCallSource.getInstance().register();
    }


    public void onDestroy() {
        GlobalUdpDataSource.getInstance().unregister();
        GlobalBellCallSource.getInstance().unRegister();
    }


    public void initNative() {
        Context context = ContextUtils.getContext();
        Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
        try {
            JfgAppCmd.initJfgAppCmd(context, DataSource.this);
            JfgAppCmd.getInstance().enableLog(true, JConstant.LOG_PATH);
        } catch (Exception e) {
            AppLogger.d("let's go err:" + e.getLocalizedMessage());
        }
        try2autoLogin();
        AppLogger.d("let's go initNative:");
        MtaManager.customEvent(context, "DataSource", "NativeInit");
    }

    private void try2autoLogin() {
        AppLogger.d("let's go 还没实现:");
    }

    @Override
    public void OnLocalMessage(String s, int i, byte[] bytes) {
        AppLogger.d("OnLocalMessage :" + s + ",i:" + i);
        if (RxBus.getCacheInstance().hasObservers()) {
            RxEvent.LocalUdpMsg msg = new RxEvent.LocalUdpMsg();
            msg.time = System.currentTimeMillis();
            msg.ip = s;
            msg.port = (short) i;
            msg.data = bytes;
            RxBus.getCacheInstance().post(msg);
        }
    }

    @Override
    public void OnReportJfgDevices(JFGDevice[] jfgDevices) {
        AppLogger.i("OnReportJfgDevices:" + (jfgDevices == null ? 0 : jfgDevices.length));
        DataSourceManager.getInstance().cacheJFGDevices(jfgDevices);//缓存设备
    }

    @Override
    public void OnUpdateAccount(JFGAccount jfgAccount) {
        DataSourceManager.getInstance().cacheJFGAccount(jfgAccount);//缓存账号信息
        AppLogger.d("OnUpdateAccount :" + jfgAccount.getPhotoUrl());
    }

    @Override
    public void OnUpdateHistoryVideoList(JFGHistoryVideo jfgHistoryVideo) {
        AppLogger.d("OnUpdateHistoryVideoList :" + jfgHistoryVideo.list.size());
        DataSourceManager.getInstance().cacheHistoryDataList(jfgHistoryVideo);
    }

    @Override
    public void OnUpdateHistoryErrorCode(JFGHistoryVideoErrorInfo jfgHistoryVideoErrorInfo) {
        AppLogger.d("OnUpdateHistoryErrorCode :");
    }

    @Override
    public void OnServerConfig(JFGServerCfg jfgServerCfg) {
        AppLogger.d("OnServerConfig :");
    }

    @Override
    public void OnLogoutByServer(int i) {
        AppLogger.d("OnLocalMessage :" + i);
        RxBus.getCacheInstance().post(i);
        DataSourceManager.getInstance().setLoginState(new LogState(LogState.STATE_ACCOUNT_OFF));
    }

    @Override
    public void OnVideoDisconnect(JFGMsgVideoDisconn jfgMsgVideoDisconn) {
        RxBus.getCacheInstance().post(jfgMsgVideoDisconn);
        AppLogger.d("OnVideoDisconnect :" + new Gson().toJson(jfgMsgVideoDisconn));
    }

    @Override
    public void OnVideoNotifyResolution(JFGMsgVideoResolution jfgMsgVideoResolution) {
        AppLogger.d("OnVideoNotifyResolution" + jfgMsgVideoResolution.peer);
        RxBus.getCacheInstance().post(jfgMsgVideoResolution);
    }

    @Override
    public void OnVideoNotifyRTCP(JFGMsgVideoRtcp jfgMsgVideoRtcp) {
        RxBus.getCacheInstance().post(jfgMsgVideoRtcp);
    }

    @Override
    public void OnHttpDone(JFGMsgHttpResult jfgMsgHttpResult) {
        AppLogger.d("OnLocalMessage :" + new Gson().toJson(jfgMsgHttpResult));
        RxBus.getCacheInstance().post(jfgMsgHttpResult);
    }

    @Override
    public void OnRobotTransmitMsg(RobotMsg robotMsg) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnRobotMsgAck(int i) {
        AppLogger.d("OnLocalMessage :");
    }

    @Override
    public void OnRobotGetDataRsp(RobotoGetDataRsp robotoGetDataRsp) {
        AppLogger.d("OnRobotGetDataRsp :" + new Gson().toJson(robotoGetDataRsp));
        DataSourceManager.getInstance().cacheRobotoGetDataRsp(robotoGetDataRsp);
    }

    @Override
    public void OnRobotGetDataExRsp(long l, String s, ArrayList<JFGDPMsg> arrayList) {

    }

    @Override
    public void OnRobotSetDataRsp(long l, ArrayList<JFGDPMsgRet> arrayList) {
        AppLogger.d("OnRobotSetDataRsp :" + l + new Gson().toJson(arrayList));
        RxBus.getCacheInstance().post(new RxEvent.SdcardClearRsp(l, arrayList));
    }

    @Override
    public void OnRobotGetDataTimeout(long l) {
        AppLogger.d("OnRobotGetDataTimeout :");
    }

    @Override
    public ArrayList<JFGDPMsg> OnQuerySavedDatapoint(String s, ArrayList<JFGDPMsg> arrayList) {
        return null;
    }

    @Override
    public void OnlineStatus(boolean b) {
        AppLogger.d("OnlineStatus :" + b);
        RxBus.getCacheInstance().post(new RxEvent.OnlineStatusRsp(b));
        DataSourceManager.getInstance().setOnline(b);//设置用户在线信息
    }

    @Override
    public void OnResult(JFGResult jfgResult) {
        boolean login = false;
        switch (jfgResult.event) {
            case 0:
                RxBus.getCacheInstance().post(new RxEvent.ResultVerifyCode(jfgResult.code));
                break;
            case 1:
                login = jfgResult.code == JError.ErrorOK;//注册成功
                RxBus.getCacheInstance().post(new RxEvent.ResultRegister(jfgResult.code));
                break;
            case 2:
                login = jfgResult.code == JError.ErrorOK;//登陆成功
                RxBus.getCacheInstance().post(new RxEvent.ResultLogin(jfgResult.code));
                break;
            case JResultEvent.JFG_RESULT_BINDDEV:
                //绑定设备
                RxBus.getCacheInstance().post(new RxEvent.BindDeviceEvent(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_UNBINDDEV:
                RxBus.getCacheInstance().post(new RxEvent.UnBindDeviceEvent(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_CHANGE_PASS:
                RxBus.getCacheInstance().post(new RxEvent.ChangePwdBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_RESET_PASS:
                RxBus.getCacheInstance().post(new RxEvent.ResetPwdBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_ADD_FRIEND:
                RxBus.getCacheInstance().post(new RxEvent.AddFriendBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_CONSENT_ADD_FRIEND:
                RxBus.getCacheInstance().post(new RxEvent.ConsentAddFriendBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_DEL_FRIEND:
                RxBus.getCacheInstance().post(new RxEvent.DelFriendBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_SETPWD_WITH_BINDACCOUNT:
                RxBus.getCacheInstance().post(new RxEvent.OpenLogInSetPwdBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_SEND_FEEDBACK:
                RxBus.getCacheInstance().post(new RxEvent.SendFeekBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_DEL_FRIEND_ADD_REQ:
                RxBus.getCacheInstance().post(new RxEvent.DeleteAddReqBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_CHECK_REGISTER:
                RxBus.getCacheInstance().post(new RxEvent.CheckRegsiterBack(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_SET_DEVICE_ALIAS:
                RxBus.getCacheInstance().post(new RxEvent.SetAlias(jfgResult));
                break;
            case JResultEvent.JFG_RESULT_SET_FRIEND_MARKNAME:
                RxBus.getCacheInstance().post(new RxEvent.SetFriendMarkNameBack(jfgResult));
                break;
        }
        if (login) {
            AfterLoginService.startGetAccountAction(ContextUtils.getContext());
            AfterLoginService.startSaveAccountAction(ContextUtils.getContext());
            AfterLoginService.resumeOfflineRequest();
        }
        AppLogger.i("jfgResult:[event:" + jfgResult.event + ",code:" + jfgResult.code + "]");
    }

    @Override
    public void OnDoorBellCall(JFGDoorBellCaller jfgDoorBellCaller) {
        AppLogger.d("OnDoorBellCall :");
        RxBus.getCacheInstance().post(new RxEvent.BellCallEvent(jfgDoorBellCaller));
    }

    @Override
    public void OnOtherClientAnswerCall() {
        AppLogger.d("OnOtherClientAnswerCall");
        RxBus.getCacheInstance().post(new RxEvent.CallAnswered(false));
    }

    @Override
    public void OnRobotCountDataRsp(long l, String s, ArrayList<JFGDPMsgCount> arrayList) {
        RxBus.getCacheInstance().post(new RxEvent.UnreadCount(s, l, arrayList));
        AppLogger.d("OnRobotCountDataRsp :");
    }

    @Override
    public void OnRobotDelDataRsp(long l, String s, int i) {
        AppLogger.d("OnRobotDelDataRsp :" + l + " uuid:" + s + " i:" + i);
        RxBus.getCacheInstance().post(new RxEvent.DeleteDataRsp(l, s, i));
    }

    @Override
    public void OnRobotSyncData(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {
        AppLogger.d("OnRobotSyncData :" + b + " " + s + " " + new Gson().toJson(arrayList));
        DataSourceManager.getInstance().cacheRobotoSyncData(b, s, arrayList);
    }

    @Override
    public void OnSendSMSResult(int i, String s) {
        AppLogger.d("OnSendSMSResult :" + i + "," + s);
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
        AppLogger.d("OnShareDeviceRsp :");
        if (RxBus.getCacheInstance() != null && RxBus.getCacheInstance().hasObservers()) {
            RxBus.getCacheInstance().post(new RxEvent.ShareDeviceCallBack(i, s, s1));
        }
    }

    @Override
    public void OnUnShareDeviceRsp(int i, String s, String s1) {
        AppLogger.d("OnUnShareDeviceRsp :");
        RxBus.getCacheInstance().post(new RxEvent.UnshareDeviceCallBack(i, s, s1));
    }

    @Override
    public void OnGetShareListRsp(int i, ArrayList<JFGShareListInfo> arrayList) {
        AppLogger.d("OnGetShareListRsp :");
        RxBus.getCacheInstance().post(new RxEvent.GetShareListCallBack(i, arrayList));
        DataSourceManager.getInstance().cacheShareList(arrayList);
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
    public void OnEfamilyMsg(byte[] bytes) {
        MsgpackMsg.MsgHeader header = MsgpackMsg.fromBytes(bytes);
        if (header == null) {
            AppLogger.e("err: header is null");
            return;
        }
        RxEvent.EFamilyMsgpack eFamilyMsgpack = new RxEvent.EFamilyMsgpack();
        eFamilyMsgpack.msgId = header.msgId;
        eFamilyMsgpack.data = bytes;
        RxBus.getCacheInstance().post(eFamilyMsgpack);
        AppLogger.d("OnEfamilyMsg :" + header.msgId);

        //暂try try
        if ((!TextUtils.isEmpty(header.caller)) && header.msgId == 2529) {
            Intent intent = new Intent(ContextUtils.getContext(), CloudLiveCallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, header.caller);
            intent.putExtra("call_in_or_out", true);
            ContextUtils.getContext().startActivity(intent);
        }
    }

    @Override
    public void OnForgetPassByEmailRsp(int i, String s) {
        AppLogger.d("OnForgetPassByEmailRsp :" + s);
        if (RxBus.getCacheInstance().hasObservers()) {
            RxBus.getCacheInstance().post(new RxEvent.ForgetPwdByMail(s));
        }
    }

    @Override
    public void OnGetAliasByCidRsp(int i, String s) {
        AppLogger.d("OnGetAliasByCidRsp :");
    }

    @Override
    public void OnGetFeedbackRsp(int i, ArrayList<JFGFeedbackInfo> arrayList) {
        AppLogger.d("OnGetFeedbackRsp :");
        if (RxBus.getCacheInstance().hasObservers()) {
            RxBus.getCacheInstance().post(new RxEvent.GetFeedBackRsp(i, arrayList));
        }
    }

    @Override
    public void OnCheckDevVersionRsp(boolean b, String s, String s1, String s2, String s3) {
        AppLogger.d("OnCheckDevVersionRsp :");
        if (RxBus.getCacheInstance().hasObservers()) {
            RxBus.getCacheInstance().post(new RxEvent.CheckDevVersionRsp(b, s, s1, s2, s3));
        }
    }

    @Override
    public void OnNotifyStorageType(int i) {
        AppLogger.d("I:" + i);
    }

    @Override
    public HashMap<String, String> getAppParameter() {
        String trimPackageName = JFGRules.getTrimPackageName();
        //读取Smarthome/log/config.txt的内容
        String extra = DebugOptionsImpl.getServer();
        //研发平台下才能使用额外配置的服务器地址.不检查服务器地址格式.
        String serverAddress = (TextUtils.equals(trimPackageName, "yf") && !TextUtils.isEmpty(extra))
                ? extra : Security.getServerPrefix(trimPackageName) + ".jfgou.com:443";
        String vid = Security.getVId(trimPackageName);
        String vKey = Security.getVKey(trimPackageName);
        HashMap<String, String> map = new HashMap<>();
        map.put("vid", vid);
        map.put("vkey", vKey);
        map.put("ServerAddress", serverAddress);
        Log.d("getAppParameter", "getAppParameter:" + map);
        return map;
    }

    @Override
    public void OnBindDevRsp(int i, String s) {
        AppLogger.d("onBindDev: " + i + " uuid:" + s);
    }

    @Override
    public void OnGetVideoShareUrl(String s) {

    }


}