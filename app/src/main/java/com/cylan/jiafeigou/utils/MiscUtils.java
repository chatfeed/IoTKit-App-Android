package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.location.LocationManager;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.utils.ContextUtils.getContext;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public class MiscUtils {


    public static boolean isInRange(int start, int end, int dst) {
        return dst >= start && dst <= end;
    }

    public static boolean isBad(List<Integer> list, int level, int count) {
        if (list == null || list.size() < count)
            return false;
        final int size = list.size();
        int result = 0;
        for (int i = 0; i < size; i++) {
            if (list.get(i) < level)
                result++;
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
        if (byteData < 1024)
            return byteData + "K/s";
        if (byteData >= 1024 && byteData < 1024 * 1024) {
            return (byteData >>> 10) + "M/s";
        }
        if (byteData >= 1024 * 1024 && byteData < 1024 * 1024 * 1024) {
            return (byteData >>> 20) + "G/s";
        }
        if (byteData >= 1024 * 1024 * 1024 && byteData < 1024 * 1024 * 1024 * 1024L) {
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
            if ((sum >> i & 0x01) == 1) count++;
        }
        return count == 0 ? 1 : count;
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
                .flatMap(new Func1<Integer, Observable<List<TimeZoneBean>>>() {
                    @Override
                    public Observable<List<TimeZoneBean>> call(Integer integer) {
                        XmlResourceParser xrp = getContext().getResources().getXml(integer);
                        List<TimeZoneBean> list = new ArrayList<>();
                        try {
                            final String tag = "timezone";
                            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                                if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                                    TimeZoneBean bean = new TimeZoneBean();
                                    final String name = xrp.getName();
                                    if (TextUtils.equals(name, tag)) {
                                        final String timeGmtName = xrp.getAttributeValue(0);
                                        bean.setGmt(timeGmtName);
                                        final String timeIdName = xrp.getAttributeValue(1);
                                        bean.setId(timeIdName);
                                        String region = xrp.nextText().replace("\n", "");
                                        bean.setName(region);
                                        int factor = timeGmtName.contains("+") ? 1 : -1;
                                        String digitGmt = BindUtils.getDigitsString(timeGmtName);
                                        int offset = factor * Integer.parseInt(digitGmt.substring(0, 2)) * 3600 +
                                                factor * (timeGmtName.contains(":30") ? 3600 / 2 : 0);
                                        bean.setOffset(offset);
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
                    }
                });
    }

    public static <T> T getValue(Object o, T t) {
        if (o == null) return t;
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
        if (devices == null)
            return new ArrayList<>();
        ArrayList<String> arrayList = new ArrayList<>();
        for (Device device : devices) {
            if (device != null && !TextUtils.isEmpty(device.shareAccount)) {
                arrayList.add(device.uuid);
            }
        }
        return arrayList;
    }

    public static ArrayList<String> getNoneSharedList(List<Device> devices) {
        if (devices == null)
            return new ArrayList<>();
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

    public static boolean checkAudioPermission() {
        String record = android.Manifest.permission.RECORD_AUDIO;
        String recordSetting = android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
        return getContext().checkCallingOrSelfPermission(record) == PackageManager.PERMISSION_GRANTED
                && getContext().checkCallingOrSelfPermission(recordSetting) == PackageManager.PERMISSION_GRANTED;
    }

    public static int setBit(int x, int n, int flag) {
        if (flag == 1) {
            x |= (1 << (n - 1));
        } else if (flag == 0) {
            x &= ~(1 << (n - 1));
        }
        return x;
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

    public static <T extends DataPoint> T safeGet_(DataPoint value, T defaultValue) {
        if (value != null) {
            try {
                return defaultValue;
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
            if (min)
                return set.get(set.size() - 1).version;
            else {
                return set.get(0).version;
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
            if (TextUtils.isEmpty(content)) return tClass.newInstance();
            return new Gson().fromJson(content, tClass);
        } catch (Exception e) {
            return null;
        }
    }

    public static long[] getChaosDpList(boolean isV2) {
        return isV2 ? new long[]{222L, 505L} : new long[]{222L, 512L};
    }

    /**
     * desc:转换文件的大小
     *
     * @param fileS
     * @return
     */
    public static String FormetSDcardSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS == 0) {
            fileSizeString = "0.0MB";
        } else if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    public static String getChaosTime(Context context, DpMsgDefine.DPAlarmInfo info, boolean off) {
        if (!off) {
            return context.getString(R.string.MAGNETISM_OFF);
        }
        //每一天
        if (info.day == 127) {
            if (info.timeStart == 0 && info.timeEnd == 5947)
                return context.getString(R.string.EVERY_DAY) + " " + context.getString(R.string.HOURS);
            return context.getString(R.string.EVERY_DAY) + " " + getTime(context, info.timeStart, info.timeEnd);
        }
        //工作日
        if (info.day == 124) {
            if (info.timeStart == 0 && info.timeEnd == 5947)
                return context.getString(R.string.WEEKDAYS) + " " + context.getString(R.string.HOURS);
            return context.getString(R.string.WEEKDAYS) + " " + getTime(context, info.timeStart, info.timeEnd);
        }
        //周末
        if (info.day == 3) {
            if (info.timeStart == 0 && info.timeEnd == 5947)
                return context.getString(R.string.WEEKEND) + " " + context.getString(R.string.HOURS);
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
        if (builder.length() > 1)
            builder.replace(builder.length() - 1, builder.length(), "");
        if (info.timeStart == 0 && info.timeEnd == 5947) {
            builder.append(" ");
            return builder.append(context.getString(R.string.HOURS)).toString();
        }
        return builder.append(getTime(context, info.timeStart, info.timeEnd)).toString();
    }

    private static String getTime(Context context, int timeStart, int timeEnd) {
        if (timeStart > timeEnd) {
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
        if (TextUtils.isEmpty(content) || content.length() < 10)
            return 0;
        try {
            return Integer.valueOf(content.substring(0, 10));
        } catch (Exception e) {
            return 0;
        }
    }

    public static List<JFGDPMsg> getCamDateVersionList(long startTime) {
        ArrayList<JFGDPMsg> list = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            list.add(makeMsg(startTime - 24 * 3600 * 1000L * i, 222));
            list.add(makeMsg(startTime - 24 * 3600 * 1000L * i, 505));
            list.add(makeMsg(startTime - 24 * 3600 * 1000L * i, 512));
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
}