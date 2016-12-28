package com.cylan.jiafeigou.dp;

import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public class DataPointManager implements IParser, IDataPoint {
    private static boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "DataPointManager";

    private static DataPointManager instance;
    private Map<Long, Long> querySeqMap = new HashMap<>();

    @Override
    public Subscription[] register() {
        return new Subscription[]{fullDataPointAssembler(),
                robotDataSyncSub()};
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private Subscription robotDataSyncSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.JFGRobotSyncData.class)
                .subscribeOn(Schedulers.newThread())
                .map((RxEvent.JFGRobotSyncData jfgRobotSyncData) -> {
                    String uuid = jfgRobotSyncData.identity;
                    Map<String, BaseValue> updatedItems = new HashMap<>();
                    for (JFGDPMsg jfg : jfgRobotSyncData.dataList) {
                        try {
                            BaseValue base = new BaseValue();
                            base.setId(jfg.id);
                            base.setVersion(jfg.version);
                            base.setValue(DpUtils.unpackData(jfg.packValue, DpMsgMap.ID_2_CLASS_MAP.get((int) jfg.id)));
                            boolean result = update(uuid, base);
                            if (result) updatedItems.put(jfgRobotSyncData.identity, base);
                        } catch (IOException e) {
                            AppLogger.e("" + e.getLocalizedMessage());
                        }
                    }
                    AppLogger.i("robotSyc:" + updatedItems.keySet());
                    return updatedItems;
                })
                .filter((Map<String, BaseValue> map) -> (map.size() > 0))
                .map((Map<String, BaseValue> map) -> {
                    for (String uuid : map.keySet()) {
                        RxEvent.DataPoolUpdate data = new RxEvent.DataPoolUpdate();
                        data.id = (int) map.get(uuid).getId();
                        data.uuid = uuid;
                        RxBus.getCacheInstance().post(data);
                    }
                    return null;
                })
                .retry(new RxHelper.RxException<>("robotDataSyncSub:"))
                .subscribe();
    }

    @Override
    public void clear() {
        if (DEBUG) Log.d(TAG, "clear: ");
        bundleMap.clear();
    }

    /**
     * object: 可以是HashSet<BaseValue>,可以是BaseValue
     */
    private HashMap<String, Object> bundleMap = new HashMap<>();
    //硬编码,注册list类型的id
    private static final HashMap<Long, Integer> mapObject = new HashMap<>();


    static {
        mapObject.put(505L, 505);
        mapObject.put(204L, 204);
        mapObject.put(222L, 222);
    }

    public static DataPointManager getInstance() {
        if (instance == null) {
            synchronized (DataPointManager.class) {
                if (instance == null)
                    instance = new DataPointManager();
            }
        }
        return instance;
    }

    private DataPointManager() {
    }

    private boolean putHashSetValue(String uuid, BaseValue baseValue) {
        boolean isSet = mapObject.containsKey(baseValue.getId());
        if (!isSet) {
            AppLogger.e("you go the wrong way: " + baseValue.getId());
            return false;
        }
        Object o = bundleMap.get(uuid);
        HashSet<BaseValue> set;
        if (o != null && o instanceof HashSet) {
            set = cast(o);
            set.add(baseValue);
        } else {
            set = new HashSet<>();
            set.add(baseValue);
            o = set;
        }
        bundleMap.put(uuid, o);
        if (DEBUG) Log.d(TAG, "putHashSetValue: " + uuid + " " + o);
        return true;
    }

    private boolean putValue(String uuid, BaseValue baseValue) {
        boolean update = false;
        synchronized (DataPointManager.class) {
            boolean isSetType = mapObject.containsKey(baseValue.getId());
            if (isSetType) {
                return putHashSetValue(uuid, baseValue);
            } else {
                Object o = bundleMap.get(uuid);
                if (o != null && o instanceof BaseValue) {
                    if (((BaseValue) o).getVersion() < baseValue.getVersion()) {
                        bundleMap.remove(uuid);
                        update = true;
                    }
                }
                bundleMap.put(uuid, baseValue);
            }
        }
        return update;
    }

    private Object removeId(String uuid, long id) {
        if (DEBUG) Log.d(TAG, "removeId: " + uuid + " " + id);
        return bundleMap.remove(uuid + id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Set<?>> T cast(Object obj) {
        return (T) obj;
    }

    private Subscription fullDataPointAssembler() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .subscribeOn(Schedulers.computation())
                .map(new Func1<RobotoGetDataRsp, Integer>() {
                    @Override
                    public Integer call(RobotoGetDataRsp dpDataRsp) {
                        final String identity = dpDataRsp.identity;
                        Log.d(TAG, "fullDataPointAssembler: " + identity);
                        boolean needNotify = false;
                        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : dpDataRsp.map.entrySet()) {
                            if (entry.getValue() == null) continue;
                            for (JFGDPMsg dp : entry.getValue()) {
                                try {
                                    if (dp == null) continue;
                                    Object o = DpUtils.unpackData(dp.packValue, DpMsgMap.ID_2_CLASS_MAP.get((int) dp.id));
                                    BaseValue value = new BaseValue();
                                    value.setId(dp.id);
                                    value.setVersion(dp.version);
                                    value.setValue(o);
                                    boolean changed = putValue(identity + dp.id, value);
                                    needNotify |= changed;
                                    Log.d(TAG, "put: " + dp.id + " " + changed + " " + needNotify);
                                } catch (Exception e) {
                                    AppLogger.i("dp is null: " + dp.id + ".." + e.getLocalizedMessage());
                                }
                            }
                        }
                        if (needNotify && querySeqMap.containsKey(dpDataRsp.seq)) {
                            Log.e(TAG, "file update: " + dpDataRsp.seq);
                            querySeqMap.remove(dpDataRsp.seq);
                            RxBus.getCacheInstance().post(dpDataRsp.seq);
                        }
                        return null;
                    }
                })
                //此retry能跳过当前一次的exception
                .retry(new RxHelper.RxException<>(TAG + "deviceDpSub"))
                .subscribe();
    }

    @Override
    public boolean insert(String uuid, BaseValue baseValue) {
        return putValue(uuid, baseValue);
    }

    @Override
    public boolean update(String uuid, BaseValue baseValue) {
        return putValue(uuid, baseValue);
    }

    @Override
    public Object delete(String uuid, long id) {
        return removeId(uuid, id);
    }

    @Override
    public Object delete(String uuid, long id, long version) {
        boolean isSet = mapObject.containsKey(id);
        if (isSet) {
            Object o = bundleMap.get(uuid + id);
            if (o != null && o instanceof HashSet) {
                HashSet<BaseValue> set = cast(o);
                BaseValue value = new BaseValue();
                value.setId(id);
                value.setVersion(version);
                return set.remove(value);
            }
        } else {
            return bundleMap.remove(uuid + id);
        }
        if (DEBUG) Log.d(TAG, "delete: " + uuid + " " + id);
        return null;
    }

    @Override
    public BaseValue fetchLocal(String uuid, long id) {
        try {
            return (BaseValue) bundleMap.get(uuid + id);
        } catch (ClassCastException c) {
            AppLogger.e(String.format("id:%s is not registered in DataPointManager#mapObject,%s", id, c.getLocalizedMessage()));
            return null;
        }
    }

    @Override
    public boolean deleteAll(String uuid, long id, ArrayList<Long> versions) {
        Object result = bundleMap.remove(uuid + id);
        if (DEBUG) Log.d(TAG, "deleteAll: " + uuid + " " + id + " " + (result == null));
        deleteRobot(uuid, id, versions);
        return result != null;
    }

    private void deleteRobot(String uuid, long id, ArrayList<Long> versions) {
        if (versions == null || versions.size() == 0)
            return;
        ArrayList<JFGDPMsg> msgs = new ArrayList<>();
        for (long version : versions) {
            JFGDPMsg msg = new JFGDPMsg();
            msg.id = id;
            msg.version = version;
            msgs.add(msg);
        }
        long req = JfgCmdInsurance.getCmd().robotDelData(uuid, msgs, 0);
        if (DEBUG) Log.d(TAG, "deleteRobot: " + req);
    }

    @Override
    public ArrayList<BaseValue> fetchLocalList(String uuid, long id) {
        try {
            Object o = bundleMap.get(uuid + id);
            if (o != null && o instanceof HashSet) {
                HashSet<BaseValue> set = cast(o);//bundleMap中存的是HashSet,set的key不会重复.
                return new ArrayList<>(set);
            }
        } catch (ClassCastException c) {
            AppLogger.e(String.format("id:%s is not registered in DataPointManager#mapObject,%s", id, c.getLocalizedMessage()));
            return null;
        }
        return null;
    }

    @Override
    public boolean isArrayType(int id) {
        return mapObject.containsKey(id);
    }

    @Override
    public long robotGetData(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) throws JfgException {
        long seq = JfgCmdInsurance.getCmd().robotGetData(peer, queryDps, limit, asc, timeoutMs);
        querySeqMap.put(seq, seq);
        return seq;
    }

}
