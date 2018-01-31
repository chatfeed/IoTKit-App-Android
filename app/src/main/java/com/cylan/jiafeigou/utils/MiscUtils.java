package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.SchemeResolver;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.n.view.adapter.item.HomeItem;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.bind.UdpConstant.BIND_TAG;
import static com.cylan.jiafeigou.utils.BindUtils.TAG_NET_RECOVERY_FLOW;
import static com.cylan.jiafeigou.utils.BindUtils.TAG_UDP_FLOW;
import static com.cylan.jiafeigou.utils.ContextUtils.getContext;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public class MiscUtils {

    private static final int BYTE = 1024;
    private static final int MEGA_BYTE = 1024 * 1024;
    private static final int GIGA_BYTE = 1024 * 1024 * 1024;

    public static boolean isInRange(int start, int end, int dst) {
        return dst >= start && dst <= end;
    }

    public static boolean isBad(List<Integer> list, int level, int count) {
        if (list == null || list.size() < count) {
            return false;
        }
        final int size = list.size();
        int result = 0;
        for (int i = 0; i < size; i++) {
            if (list.get(i) < level) {
                result++;
            }
        }
        return result >= count;
    }

    public static int parseTime(String time) {
        String[] times = time.split(":");
        final String h = Integer.toBinaryString(times[0].length() > 1 ? Integer.parseInt(times[0].substring(times[0].length() - 2, times[0].length())) : Integer.parseInt(times[0].substring(times[0].length() - 1, times[0].length())));
        final String m = String.format(Locale.getDefault(), "%08d",
                Integer.parseInt(Integer.toBinaryString(Integer.parseInt(times[1]))));
        return Integer.parseInt(h + m, 2);
    }

    public static String parse2Time(int value) {
        return String.format(Locale.getDefault(), "%02d", value >> 8)
                + String.format(Locale.getDefault(), ":%02d", (((byte) value << 8) >> 8));
    }

    public static String getByteFromBitRate(long bitRate) {
        bitRate = bitRate / 8;
        return getFlowResult(bitRate);
    }

    public static String getFlowResult(long byteData) {
        if (byteData < BYTE) {
            return byteData + "K/s";
        }
        if (byteData >= BYTE && byteData < MEGA_BYTE) {
            return (byteData >>> 10) + "M/s";
        }
        if (byteData >= MEGA_BYTE && byteData < GIGA_BYTE) {
            return (byteData >>> 20) + "G/s";
        }
        if (byteData >= GIGA_BYTE && byteData < 1024L * GIGA_BYTE) {
            return (byteData >>> 30) + "T/s";
        }
        return "";
    }

//    public static void main(String[] args) {
//        System.out.println(getFlowResult(1024 * 1024 + 1));
//        System.out.println(getFlowResult(1024 * 1024));
//        System.out.println(getFlowResult(1024 * 1024 - 1));
//        System.out.println(getFlowResult(1025));
//        System.out.println(getFlowResult(1024));
//        System.out.println(getFlowResult(1023));
//        System.out.println(getCount(1));
//        System.out.println(getCount(1));
//        System.out.println(getCount(1));
//        System.out.println(getCount(1));
//    }

    public static int getCount(int sum) {
        int count = 0;
        for (int i = 0; i < 3; i++) {
            if ((sum >> i & 0x01) == 1) {
                count++;
            }
        }
        return count == 0 ? 1 : count;
    }

    public static String getFileName(CamMessageBean bean, int index) {
        String fileName = null;
        if (bean == null) return null;

        switch ((int) bean.message.getMsgId()) {
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) bean.message;
                if (dpAlarm.face_id == null || dpAlarm.face_id.length == 0) {
                    fileName = dpAlarm.version / 1000 + "_" + index + ".jpg";
                } else {
                    fileName = dpAlarm.version / 1000 + ".jpg";
                }
            }
            break;
            case DpMsgMap.ID_401_BELL_CALL_STATE: {
                DpMsgDefine.DPBellCallRecord dpBellCallRecord = (DpMsgDefine.DPBellCallRecord) bean.message;
                if (dpBellCallRecord.fileIndex == 0) {
                    //旧版本门铃呼叫记录,不带index
                    fileName = dpBellCallRecord.time + ".jpg";
                } else {
                    fileName = dpBellCallRecord.time + "_" + index + ".jpg";
                }
            }
            break;
            default: {

            }
        }
        return fileName;
    }

    public static int getFileTime(CamMessageBean bean) {
        if (bean == null) return 0;
        int time;
        switch ((int) bean.message.getMsgId()) {
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) bean.message;
                time = dpAlarm.time;
            }
            break;
            case DpMsgMap.ID_401_BELL_CALL_STATE: {
                DpMsgDefine.DPBellCallRecord dpBellCallRecord = (DpMsgDefine.DPBellCallRecord) bean.message;
                time = dpBellCallRecord.time;
            }
            break;
            case DpMsgMap.ID_526_CAM_AI_WARM_MSG: {
                DpMsgDefine.DPCameraAIWarmMsg dpCameraAIWarmMsg = (DpMsgDefine.DPCameraAIWarmMsg) bean.message;
                time = dpCameraAIWarmMsg.time;
            }
            break;
            default: {
                time = 0;
            }
        }
        return time;
    }

    public static int getFileType(CamMessageBean bean) {
        if (bean == null) return 0;
        int fileType = 0;
        switch ((int) bean.message.getMsgId()) {
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) bean.message;
                fileType = dpAlarm.ossType;
            }
            break;
            case DpMsgMap.ID_401_BELL_CALL_STATE: {
                DpMsgDefine.DPBellCallRecord dpBellCallRecord = (DpMsgDefine.DPBellCallRecord) bean.message;
                fileType = dpBellCallRecord.type;
            }
            break;
            default: {

            }
        }
        return fileType;
    }

    public static CamWarnGlideURL getCamWarnUrl(String cid, CamMessageBean bean, int index) {
        CamWarnGlideURL result;
        if (bean == null) return null;
        switch ((int) bean.message.getMsgId()) {
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) bean.message;
                if (dpAlarm.face_id != null && dpAlarm.face_id.length > 0) {
                    result = new CamWarnGlideURL(cid, dpAlarm.ossType, String.valueOf(dpAlarm.time));
                } else {
                    result = new CamWarnGlideURL(cid, dpAlarm.time + "_" + index + ".jpg", dpAlarm.time, index, dpAlarm.ossType);
                }
            }
            break;
            case DpMsgMap.ID_401_BELL_CALL_STATE: {
                DpMsgDefine.DPBellCallRecord dpBellCallRecord = (DpMsgDefine.DPBellCallRecord) bean.message;
                String name;
                if (dpBellCallRecord.fileIndex == -1) {
                    //旧版本门铃呼叫记录,不带index
                    result = new CamWarnGlideURL(cid, dpBellCallRecord.time + ".jpg", dpBellCallRecord.type);
                } else {
                    result = new CamWarnGlideURL(cid, dpBellCallRecord.time + "_" + index + ".jpg", dpBellCallRecord.time, index, dpBellCallRecord.type);
                }
            }
            break;
            default: {
                result = new CamWarnGlideURL("", "", 0);
            }
        }
        AppLogger.w("getCamWarnUrl:" + result.toStringUrl());
        return result;
    }

    public static String getCamWarnUrlV2(String cid, CamMessageBean bean, int index) {
        if (bean == null) return null;
        SchemeResolver schemeResolver = SchemeResolver.INSTANCE;
        String parse = schemeResolver.parse(cid, bean.message);
        switch ((int) bean.message.getMsgId()) {
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) bean.message;
                if (dpAlarm.face_id == null || dpAlarm.face_id.length == 0) {
                    return schemeResolver.index(parse, index);
                }
            }
            default:
                return parse;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object object, T defaultValue) {
        try {
            return object == null ? defaultValue : (T) object;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static Observable<List<TimeZoneBean>> loadTimeZoneList() {
        return Observable.just(R.xml.timezones)
                .subscribeOn(Schedulers.computation())
                .flatMap(integer -> {
                    String[] timeIds = TimeZone.getAvailableIDs();
                    HashMap<String, String> map = new HashMap<>(timeIds.length);
                    for (String ids : timeIds) {
                        map.put(ids, ids);
                    }
                    XmlResourceParser xrp = getContext().getResources().getXml(integer);
                    List<TimeZoneBean> list = new ArrayList<>();
                    try {
                        final String tag = "timezone";
                        while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                            if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                                TimeZoneBean bean = new TimeZoneBean();
                                final String name = xrp.getName();
                                if (TextUtils.equals(name, tag)) {
                                    final String key = xrp.getAttributeValue(1);
                                    bean.setId(key);
                                    String region = xrp.nextText().replace("\n", "");
                                    bean.setName(region);
                                    TimeZone timeZone = TimeZone.getTimeZone(key);
                                    bean.setOffset(timeZone.getRawOffset());
                                    final String gmt = getTimeZoneText(timeZone, false);
                                    bean.setGmt(gmt);
                                    list.add(bean);
                                }
                            }
                            xrp.next();
                        }
                        Log.d(tag, "timezone: " + list);
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                    }
                    return Observable.just(list);
                });
    }

    private static final String FORMAT_PH_P = "GMT +%02d:%02d";
    private static final String FORMAT_PH_N = "GMT %02d:%02d";

    private static String displayTimeZone(TimeZone tz) {
        long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset())
                - TimeUnit.HOURS.toMinutes(hours);
        // avoid -4:-30 issue
        minutes = Math.abs(minutes);
        if (hours >= 0) {
            return String.format(Locale.getDefault(), FORMAT_PH_P, hours, minutes);
        } else {
            return String.format(Locale.getDefault(), FORMAT_PH_N, hours, minutes);
        }
    }

    public static <T> T getValue(Object o, T t) {
        if (o == null) {
            return t;
        }
        try {
            return (T) o;
        } catch (Exception e) {
            return t;
        }
    }


    public static String getBeautifulString(String temp, int len) {
        if (!TextUtils.isEmpty(temp) && temp.length() > len) {
            temp = temp.substring(0, len) + "...";
        }
        return temp;
    }

    public static ArrayList<String> getSharedList(List<Device> devices) {
        if (devices == null) {
            return new ArrayList<>();
        }
        ArrayList<String> arrayList = new ArrayList<>();
        for (Device device : devices) {
            if (device != null && !TextUtils.isEmpty(device.shareAccount)) {
                arrayList.add(device.uuid);
            }
        }
        return arrayList;
    }

    public static ArrayList<String> getNoneSharedList(List<Device> devices) {
        if (devices == null) {
            return new ArrayList<>();
        }
        ArrayList<String> arrayList = new ArrayList<>();
        for (Device device : devices) {
            if (device != null && TextUtils.isEmpty(device.shareAccount)) {
                arrayList.add(device.uuid);
            }
        }
        return arrayList;
    }

    /**
     * 还需要判断 2.0版本的设备，3.0版本的设备
     *
     * @param device
     * @return
     */
    public static ArrayList<Long> createGetCameraWarnMsgDp(Device device) {
        ArrayList<Long> list = new ArrayList<>();
        boolean isV2 = TextUtils.isEmpty(device.vid);
        list.add(isV2 ? 505L : 512L);
        list.add(222L);
        return list;
    }

    public static boolean checkWriteExternalPermission() {
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


    public static int setBit(int x, int n, int flag) {
        if (flag == 1) {
            x |= (1 << (n));
        } else if (flag == 0) {
            x &= ~(1 << (n));
        }
        return x;
    }

    public static int getBit(int n, int k) {
        return (n >> k) & 1;
    }

    public static <T> T safeGet(DpMsgDefine.DPPrimary<? extends T> value, T defaultValue) {
        if (value != null && value.value != null) {
            try {
                return value.value;
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }


    /**
     * @param set
     * @param min ： max or min
     * @param <T>
     * @return
     */
    public static <T extends DataPoint> long getVersion(List<T> set, boolean min) {
        if (set != null && set.size() > 0) {
            Collections.sort(set);
            if (min) {
                return set.get(set.size() - 1).getVersion();
            } else {
                return set.get(0).getVersion();
            }
        }
        return 0L;
    }

    public static JFGDPMsg getMessageByVersion(long id, long version) {
        JFGDPMsg msg = new JFGDPMsg();
        msg.id = id;
        msg.version = version;
        return msg;
    }


    public static boolean checkGpsAvailable(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static <T> T getObjectFromSP(String key, Class<T> tClass) {
        try {
            String content = PreferencesUtils.getString(key);
            if (TextUtils.isEmpty(content)) {
                return tClass.newInstance();
            }
            return new Gson().fromJson(content, tClass);
        } catch (Exception e) {
            return null;
        }
    }

//    public static long[] getChaosDpList(boolean isNormalVisitor) {
//        return isNormalVisitor ? new long[]{505L,222L,512} : new long[]{222L, 512L};
//    }

    /**
     * desc:转换文件的大小
     *
     * @param fileS
     * @return
     */
    public static String FormatSdCardSize(long fileS) {
        return FormatSdCardSizeSpec(fileS, "B");
    }

    /**
     * desc:转换文件的大小
     *
     * @param fileS
     * @return
     */
    public static String FormatSdCardSizeSpec(long fileS, String unit) {
        switch (unit) {
            case "B":
                if (fileS < 1024) {
                    return new DecimalFormat("#######0").format((int) fileS) + "B";//取整
                }
            case "K":
                if (fileS < 1048576) {
                    return new DecimalFormat("#######0").format((int) fileS / 1024) + "K";//取整
                }
            case "M":
                if (fileS < 1073741824) {
                    return new DecimalFormat("#######0").format((int) fileS / 1048576) + "M";//取整
                }
            case "G":
                return new DecimalFormat("#######0.00").format((double) fileS / 1073741824) + "G";//有两位小数点,所以用 double
            default:
                return "";
        }
    }


    public static String getChaosTime(Context context, DpMsgDefine.DPAlarmInfo info, boolean off) {
        if (!off) {
            return context.getString(R.string.MAGNETISM_OFF);
        }
        //每一天
        if (info.day == 127) {
            if (info.timeStart == 0 && info.timeEnd == 5947) {
                return context.getString(R.string.EVERY_DAY) + " " + context.getString(R.string.HOURS);
            }
            return context.getString(R.string.EVERY_DAY) + " " + getTime(context, info.timeStart, info.timeEnd);
        }
        //工作日
        if (info.day == 124) {
            if (info.timeStart == 0 && info.timeEnd == 5947) {
                return context.getString(R.string.WEEKDAYS) + " " + context.getString(R.string.HOURS);
            }
            return context.getString(R.string.WEEKDAYS) + " " + getTime(context, info.timeStart, info.timeEnd);
        }
        //周末
        if (info.day == 3) {
            if (info.timeStart == 0 && info.timeEnd == 5947) {
                return context.getString(R.string.WEEKEND) + " " + context.getString(R.string.HOURS);
            }
            return context.getString(R.string.WEEKEND) + " " + getTime(context, info.timeStart, info.timeEnd);
        }
        //零散
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (((info.day >> (7 - 1 - i)) & 0x01) == 1) {
                //hit
                builder.append(context.getString(periodResId[i]));
                builder.append(",");
            }
        }
        if (builder.length() > 1) {
            builder.replace(builder.length() - 1, builder.length(), "");
        }
        if (info.timeStart == 0 && info.timeEnd == 5947) {
            builder.append(" ");
            return builder.append(context.getString(R.string.HOURS)).toString();
        }
        return builder.append(getTime(context, info.timeStart, info.timeEnd)).toString();
    }

    private static String getTime(Context context, int timeStart, int timeEnd) {
        if (timeStart >= timeEnd) {
            StringBuilder builder = new StringBuilder(parse2Time(timeStart));
            return builder.append("-")
                    .append(context.getString(R.string.TOW))
                    .append(parse2Time(timeEnd)).toString();
        }
        return parse2Time(timeStart) + "-" + parse2Time(timeEnd);
    }

    private static final int[] periodResId = {R.string.MON_1, R.string.TUE_1,
            R.string.WED_1, R.string.THU_1,
            R.string.FRI_1, R.string.SAT_1, R.string.SUN_1};


    /**
     * 文件名，命名格式[timestamp].jpg 或[timestamp]_[secends].avi，timestamp是文件生成时间的unix时间戳，secends是视频录制的时长,单位秒。根据后缀区分是图片或视频。
     * 提取时间戳
     * 1489756095
     *
     * @param content
     * @return
     */
    public static int getValueFrom(String content) {
        if (TextUtils.isEmpty(content) || content.length() < 10) {
            return 0;
        }
        try {
            return Integer.valueOf(content.substring(0, 10));
        } catch (Exception e) {
            return 0;
        }
    }

    public static ArrayList<JFGDPMsg> getCamDateVersionList(long startTime, int days) {
        ArrayList<JFGDPMsg> list = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            list.add(makeMsg(startTime - 24 * 3600 * 1000L * i, 222L));
            list.add(makeMsg(startTime - 24 * 3600 * 1000L * i, 505L));
            list.add(makeMsg(startTime - 24 * 3600 * 1000L * i, 512L));
            list.add(makeMsg(startTime - 24 * 3600 * 1000L * i, 401L));
        }
        return list;
    }

    private static JFGDPMsg makeMsg(long version, long msgId) {
        return new JFGDPMsg(msgId, version);
    }

    /**
     * 从{@link android.support.v4.app.FragmentPagerAdapter}中抽出来
     *
     * @param viewId
     * @param id
     * @return
     */
    public static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }


    public static DPEntity getMaxVersionEntity(DPEntity... entities) {
        if (entities == null) {
            return null;
        }
        List<DPEntity> list = new ArrayList<>();
        for (DPEntity entity : entities) {
            if (entity != null) {
                list.add(entity);
            }
        }
        if (list.size() == 0) {
            return null;
        }
        Collections.sort(list);
        return list.get(0);//降序的
    }

    public static ArrayList<IDPEntity> msgList(DBAction dbAction, String uuid, String account, String server, List<JFGDPMsg> list) {
        ArrayList<IDPEntity> idpEntities = new ArrayList<>();
        for (JFGDPMsg jfgdpMsg : list) {
            DPEntity dpEntity = new DPEntity();
            dpEntity.setUuid(uuid);
            dpEntity.setVersion(jfgdpMsg.version);
            dpEntity.setBytes(jfgdpMsg.packValue);
            dpEntity.setMsgId((int) jfgdpMsg.id);
            dpEntity.setAction(dbAction);
            dpEntity.setAccount(account);
            dpEntity.setServer(server);
            idpEntities.add(dpEntity);
        }
        return idpEntities;
    }

    public static List<IDPEntity> buildEntity(String uuid, long msgId, long version, boolean asc) {
        List<IDPEntity> list = new ArrayList<>();
        list.add(new DPEntity()
                .setMsgId((int) msgId)
                .setUuid(uuid)
                .setVersion(version)
                .setOption(new DBOption.SimpleMultiDpQueryOption(1, asc))
                .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount()));
        return list;
    }

    public static int getFileIndex(CamMessageBean camMessageBean) {
        int index = 1;
        if (camMessageBean == null) return index;
        switch ((int) camMessageBean.message.getMsgId()) {
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) camMessageBean.message;
                index = dpAlarm.fileIndex;
            }
            break;
            case DpMsgMap.ID_401_BELL_CALL_STATE: {
                DpMsgDefine.DPBellCallRecord dpBellCallRecord = (DpMsgDefine.DPBellCallRecord) camMessageBean.message;
                index = dpBellCallRecord.fileIndex;
            }
            break;
            default: {

            }
        }
        if (index < 0) {
            index = 1;
        }
        return index;
    }

    public static long getVersion(CamMessageBean camMessageBean) {
        if (camMessageBean == null) return 0;
        long version = 0;
        switch ((int) camMessageBean.message.getMsgId()) {
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) camMessageBean.message;
                version = dpAlarm.version;
            }
            break;
            case DpMsgMap.ID_401_BELL_CALL_STATE: {
                DpMsgDefine.DPBellCallRecord dpBellCallRecord = (DpMsgDefine.DPBellCallRecord) camMessageBean.message;
                version = dpBellCallRecord.version;
            }
            break;
            default: {

            }
        }
        return version;
    }

    public static long getFinalVersion(CamMessageBean bean, int index) {
        long finalVersion = 0;
        if (bean == null) return finalVersion;
        switch ((int) bean.message.getMsgId()) {
            case DpMsgMap.ID_505_CAMERA_ALARM_MSG: {
                DpMsgDefine.DPAlarm dpAlarm = (DpMsgDefine.DPAlarm) bean.message;
                finalVersion = (dpAlarm.time + index) * 1000L;
            }
            break;
            case DpMsgMap.ID_401_BELL_CALL_STATE: {
                DpMsgDefine.DPBellCallRecord dpBellCallRecord = (DpMsgDefine.DPBellCallRecord) bean.message;
                if (dpBellCallRecord.fileIndex == 0) {
                    finalVersion = dpBellCallRecord.time * 1000L;
                } else {
                    finalVersion = (dpBellCallRecord.time + index) * 1000L;
                }
            }
            break;
            default: {

            }
        }
        return finalVersion;
    }

    public static void getDelta(CamMessageBean bean, long version) {

    }

    public static class DPEntityBuilder {
        private List<IDPEntity> list = new ArrayList<>();

        public DPEntityBuilder() {
        }

        public DPEntityBuilder add(DBAction action, String uuid, long msgId, long version, boolean asc) {
            list.add(new DPEntity()
                    .setMsgId((int) msgId)
                    .setUuid(uuid)
                    .setAction(action)
                    .setVersion(version)
                    .setOption(new DBOption.SimpleMultiDpQueryOption(1, asc))
                    .setAccount(DataSourceManager.getInstance().getJFGAccount().getAccount()));
            return this;
        }

        public List<IDPEntity> build() {
            return list;
        }
    }

    public static String getErr(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        return "err:" + throwable.getLocalizedMessage();
    }

    public static boolean isLand() {
        return ContextUtils.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static List<String> getUUidListFromItem(List<HomeItem> list) {
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        }
        List<String> list1 = new ArrayList<>(list.size());
        for (HomeItem item : list) {
            list1.add(item.getUUid());
        }
        return list1;
    }

    public static List<HomeItem> getHomeItemListFromDevice(List<Device> list) {
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        }
        ArrayList<Device> devices = new ArrayList<>(list);
        List<HomeItem> list1 = new ArrayList<>(list.size());
        for (Device item : devices) {
            if (item != null) {
                Log.d("FromDevice", "item:" + item.pid);
                list1.add(new HomeItem().withUUID(item.uuid));
            }
        }
        return list1;
    }

    public static boolean insertImage(String filePath, String fileName) {
        // 其次把文件插入到系统图库
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            if (sdcard != null) {
                File mediaDir = new File(sdcard, "DCIM/Camera");
                if (!mediaDir.exists()) {
                    mediaDir.mkdirs();
                }
            }
            MediaStore.Images.Media.insertImage(ContextUtils.getContext().getContentResolver(),
                    filePath + File.separator + fileName, fileName, null);
            // 最后通知图库更新
            ContextUtils.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath + File.separator + fileName)));
            return true;
        } catch (Exception e) {
            AppLogger.e("insertImage err: " + MiscUtils.getErr(e));
            return false;
        }
    }

    public static String getFileNameWithoutExn(String url) {
//        String url = "http://oss-cn-hangzhou.aliyuncs.com/jiafeigou-test/package/camera/JFG1W-2.4.6.28-V1-SENSOR_8330.bin?Expires=1521872297&Signature=dbu%2F0nQ3aNGz0wGIAieB7opsNiI%3D&OSSAccessKeyId=xjBdwD1du8lf2wMI";
        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public static boolean isDeviceInWLAN(String uuid) {
        if (TextUtils.isEmpty(uuid) && BuildConfig.DEBUG) {
            throw new IllegalArgumentException("uuid is  null");
        }
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        boolean isApDirect = JFGRules.isAPDirect(uuid, device.$(202, ""));
        if (isApDirect) {//Ap
            return true;
        } else {//局域网
            //在线
            String appSSID = NetUtils.getNetName(ContextUtils.getContext());
            return TextUtils.equals(appSSID, device.$(201, new DpMsgDefine.DPNet()).ssid);
        }
    }

    /**
     * 属于绑定过程,恢复公网的wifi
     */
    public static boolean recoveryWiFi() {
        PerformanceUtils.stopTrace(TAG_UDP_FLOW);
        PerformanceUtils.startTrace(TAG_NET_RECOVERY_FLOW);
        WifiManager wifiManager = (WifiManager) ContextUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.WifiLock wifiLock = wifiManager.createWifiLock("jfg reconnect processor");
        try {
            wifiLock.acquire();
            List<WifiConfiguration> list = new ArrayList<>(wifiManager.getConfiguredNetworks());
            //小米会一直弹权限框,所以直接注释掉
//            WifiInfo info = wifiManager.getConnectionInfo();
//            if (JFGRules.isCylanDevice(info.getSSID())) {
//                boolean disconnect = wifiManager.disconnect();
//                boolean disableNetwork = wifiManager.disableNetwork(info.getNetworkId());
//                boolean removed = wifiManager.removeNetwork(info.getNetworkId());
//                AppLogger.d("当前连接的网络:" + info.getSSID()
//                        + ",disconnected:" + disconnect
//                        + ",disable Network:" + disableNetwork
//                        + ",removed:" + removed);
//            }
            int highPriority = -1;
            int index = -1;
            for (int i = 0; i < list.size(); i++) {
                String ssid = list.get(i).SSID;
                if (!JFGRules.isCylanDevice(ssid)) {
                    //恢复之前连接过的wifi
                    if (highPriority < list.get(i).priority) {
                        highPriority = list.get(i).priority;
                        index = i;
                    }
                } else {
                    //小米会一直弹 WiFi 权限框,所以直接注释掉
//                        WifiConfiguration configuration = list.get(i);
//                        boolean disconnect = wifiManager.disconnect();
//                        boolean s = wifiManager.disableNetwork(configuration.networkId);
//                        boolean b = wifiManager.removeNetwork(configuration.networkId);
//                        AppLogger.d("断开网络是否成功:" + disconnect + "禁用加菲狗 Dog:" + s + "移除加菲狗 dog:" + b);
                }
            }
            if (index != -1) {
                wifiManager.disconnect();
                boolean enableNetwork = wifiManager.enableNetwork(list.get(index).networkId, true);
                AppLogger.d("re enable ssid: " + list.get(index).SSID + "success:" + enableNetwork);
                boolean reconnect = wifiManager.reconnect();
                AppLogger.d("re connect :" + reconnect);

                if (!reconnect && list.size() > 0) {//重连失败了,则随机连接一个了
                    for (WifiConfiguration wifiConfiguration : list) {
                        if (!JFGRules.isCylanDevice(wifiConfiguration.SSID)) {
                            //恢复之前连接过的wifi
                            wifiManager.disconnect();
                            enableNetwork = wifiManager.enableNetwork(list.get(index).networkId, true);
                            AppLogger.d("re enable ssid: " + list.get(index).SSID + "success:" + enableNetwork);
                            reconnect = wifiManager.reconnect();
                            AppLogger.d("re connect :" + reconnect);
                            if (reconnect) {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            wifiLock.release();
        }
        return true;
    }

    public static Observable<String> getAppVersionFromGooglePlay() {
        return Observable.just("getVersion")
                .subscribeOn(Schedulers.io())
                .filter(ret -> {
                    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
                    int resultCode = apiAvailability.isGooglePlayServicesAvailable(ContextUtils.getContext());
                    return resultCode == ConnectionResult.SUCCESS;//可用的时候,检查
                })
                .flatMap(s -> {
                    try {
                        String newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + ContextUtils.getContext().getPackageName() + "&hl=en")
                                .timeout(10000)
                                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                                .referrer("http://www.google.com")
                                .get()
                                .select("div[itemprop=softwareVersion]")
                                .first()
                                .ownText();
                        return Observable.just(newVersion);
                    } catch (Exception e) {
                        return Observable.just("");
                    }
                });
    }

    public static boolean isGooglePlayServiceAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(ContextUtils.getContext());
        return resultCode == ConnectionResult.SUCCESS;//可用的时候,检查
    }

    public static boolean isAPDirect(String mac) {
        //没有连接公网.//必须是连接状态
        WifiInfo info = NetUtils.getWifiManager(ContextUtils.getContext()).getConnectionInfo();
        boolean state = info != null && info.getSupplicantState() == SupplicantState.COMPLETED;
        return TextUtils.equals(NetUtils.getRouterMacAddress(), mac) && state;
    }

    public static long getFileSizeFromUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            //空的 URL 地址,则直接返回了
            return 0;
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            return response.body().contentLength();
        } catch (IOException e) {
            return 0;
        }
    }

    public static String verticalString(String str) {
        if (TextUtils.isEmpty(str)) {
            return "\n";
        }
        StringBuilder builder = new StringBuilder(str.length() * 2);
        for (char c : str.toCharArray()) {
            builder.append(c).append("\n");
        }
        return builder.toString();
    }

    /**
     * 看看是否安装了 doby cell_c
     */
    public static void checkJFGLikeApp() {
        Observable.just("go")
                .subscribeOn(Schedulers.io())
                .subscribe(ret -> {
                    final PackageManager pm = ContextUtils.getContext().getPackageManager();
                    //get a list of installed apps.
                    List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                    for (ApplicationInfo packageInfo : packages) {
                        final String pname = packageInfo.packageName;
                        if (!TextUtils.isEmpty(pname) && pname.contains("com.cylan")) {
                            AppLogger.e(BIND_TAG + pname);
                        }
                    }
                }, AppLogger::e);
    }

    public static void checkVPNState() {
        Observable.just("check")
                .subscribeOn(Schedulers.io())
                .subscribe(ret -> AppLogger.d(BIND_TAG + "vpn is on..." + NetUtils.isVPNOn()), AppLogger::e);
    }

    public static String getValueFromUri(String url, String key) {
//        String url = "http://127.0.0.1:8080/??sdf=s&&st=b=&&?sw?=%B9%FA+%BC%D2&tb=&st=9";
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter(key);
    }

    public static int getSum(int num, int base) {
        float ret = (float) num / base;
        int count = num / base;
        if (ret > count) {
            return count + 1;
        }
        return 0;
    }

    public static <T> boolean arrayContains(T[] array, T target) {
        return false;
    }

    public static boolean arrayContains(int[] array, int target) {
        if (array == null || array.length == 0) {
            return false;
        }
        for (int t : array) {
            if (t == target) {
                return true;
            }
        }
        return false;
    }

    public static void dumpSystemInfo() {
        AppLogger.d(PackageUtils.getAppVersionName(ContextUtils.getContext()));
        AppLogger.d("" + PackageUtils.getAppVersionCode(ContextUtils.getContext()));
        AppLogger.d(ProcessUtils.myProcessName(ContextUtils.getContext()));
        AppLogger.d(Build.DISPLAY);
        AppLogger.d(Build.MODEL);
        AppLogger.d(Build.MANUFACTURER);
        AppLogger.d(Build.VERSION.SDK_INT + " " + Build.VERSION.RELEASE);
    }

    private static SimpleDateFormat gmtFormatter = new SimpleDateFormat("ZZZZ");

    public static String getTimeZoneText(TimeZone tz, boolean includeName) {
        Date now = new Date();

        // Use SimpleDateFormat to format the GMT+00:00 string.

        gmtFormatter.setTimeZone(tz);
        String gmtString = gmtFormatter.format(now);

        // Ensure that the "GMT+" stays with the "00:00" even if the digits are RTL.
        BidiFormatter bidiFormatter = BidiFormatter.getInstance();
        Locale l = Locale.getDefault();
        boolean isRtl = TextUtils.getLayoutDirectionFromLocale(l) == View.LAYOUT_DIRECTION_RTL;
        gmtString = bidiFormatter.unicodeWrap(gmtString,
                isRtl ? TextDirectionHeuristics.RTL : TextDirectionHeuristics.LTR);

        if (!includeName) {
            return gmtString;
        }

        // Optionally append the time zone name.
        SimpleDateFormat zoneNameFormatter = new SimpleDateFormat("zzzz");
        zoneNameFormatter.setTimeZone(tz);
        String zoneNameString = zoneNameFormatter.format(now);

        // We don't use punctuation here to avoid having to worry about localizing that too!
        return gmtString + " " + zoneNameString;
    }

    private void addTimeZone(String olsonId) {
//        // We always need the "GMT-07:00" string.
//        final TimeZone tz = TimeZone.getTimeZone(olsonId);
//
//        // For the display name, we treat time zones within the country differently
//        // from other countries' time zones. So in en_US you'd get "Pacific Daylight Time"
//        // but in de_DE you'd get "Los Angeles" for the same time zone.
//        String displayName;
//        if (mLocalZones.contains(olsonId)) {
//            // Within a country, we just use the local name for the time zone.
//            mZoneNameFormatter.setTimeZone(tz);
//            displayName = mZoneNameFormatter.format(mNow);
//        } else {
//            // For other countries' time zones, we use the exemplar location.
//            final String localeName = Locale.getDefault().toString();
//            displayName = TimeZoneNames.getExemplarLocation(localeName, olsonId);
//        }
//    }

    }
}