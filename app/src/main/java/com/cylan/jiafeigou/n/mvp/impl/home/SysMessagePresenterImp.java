package com.cylan.jiafeigou.n.mvp.impl.home;

import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.module.SysMsgBean;
import com.cylan.jiafeigou.cache.db.module.SysMsgBeanDao;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.SysMessageContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.rx.RxBus.getCacheInstance;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class SysMessagePresenterImp extends AbstractPresenter<SysMessageContract.View> implements SysMessageContract.Presenter {

    private BaseDBHelper helper;
    private ArrayList<SysMsgBean> results = new ArrayList<SysMsgBean>();

    public SysMessagePresenterImp(SysMessageContract.View view) {
        super(view);
        helper = (BaseDBHelper) BaseApplication.getAppComponent().getDBHelper();
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                getAccount()};
    }

    /**
     * 加载消息数据
     */
    @Override
    public void initMesgData(String account) {
        getMesgDpData(account);
    }

    /**
     * 处理数据的显示
     *
     * @param list
     */
    private void handlerDataResult(ArrayList<SysMsgBean> list) {
        if (getView() != null) {
            if (list.size() != 0) {
                getView().hideNoMesgView();
                getView().initRecycleView(list);
            } else {
                getView().showNoMesgView();
                getView().initRecycleView(new ArrayList<>());
            }
        }
    }

    /**
     * 拿到数据库的操作对象
     *
     * @return
     */
    @Override
    public Subscription getAccount() {
        return getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(account -> {
                    if (account != null) {
                        // 加载数据库数据
                        initMesgData(account.jfgAccount.getAccount());
                        markMesgHasRead();
                    }
                }, AppLogger::e);
    }

    /**
     * 获取到本地数据库中的所有消息记录
     *
     * @return
     */
    @Override
    public ArrayList<SysMsgBean> findAllFromDb() {
        List<SysMsgBean> tempList = helper.getDaoSession().getSysMsgBeanDao()
                .loadAll();
        return sortAddReqList((ArrayList<SysMsgBean>) tempList);
    }

    /**
     * 清空本地消息记录
     */
    @Override
    public void deleteAllRecords() {
        helper.getDaoSession().getSysMsgBeanDao().deleteAll();
    }

    /**
     * 消息保存到数据库
     *
     * @param bean
     */
    @Override
    public void saveIntoDb(SysMsgBean bean) {
//        try {
//            dbManager.save(bean);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
    }


    /**
     * Dp获取到消息记录
     */
    @Override
    public void getMesgDpData(String account) {
        Observable.just(null)
                .observeOn(Schedulers.io())
                .map(o -> {
                    long seq = -1;
                    try {
                        JFGDPMsg msg1 = new JFGDPMsg(601, 0);
                        JFGDPMsg msg4 = new JFGDPMsg(701, 0);
                        ArrayList<JFGDPMsg> params = new ArrayList<>();
                        params.add(msg1);
                        params.add(msg4);
                        seq = BaseApplication.getAppComponent().getCmd().robotGetData("", params, 100, false, 0);
                        Log.d(TAG, "getMesgDpData:" + seq);
                    } catch (Exception e) {
                        AppLogger.e("getMesgDpData:" + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                    return seq;
                })
                .flatMap(seq -> {
                    Log.d(TAG, System.currentTimeMillis() + "");
                    Observable<RobotoGetDataRsp> filter = RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class).filter(rsp -> {
                        Log.d(TAG, "seq:" + rsp.seq + ",before seq:" + seq);
                        return rsp.seq == seq;

                    });
                    Log.d(TAG, "" + System.currentTimeMillis());
                    return filter;
                })
                .first()
                .map(robotoGetDataRsp -> {
                    Log.e(TAG, "getMesgDpData: robotoGetDataRsp");
                    if (results.size() != 0)
                        results.clear();
                    results.addAll(convertData(robotoGetDataRsp));
                    return results;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.size() != 0) {
                        handlerDataResult(list);
                    } else {
                        getView().showNoMesgView();
                    }
                }, AppLogger::e);
    }

    /**
     * Dp获取消息记录的回调
     *
     * @return
     */
    @Override
    public Subscription getMesgDpDataCallBack() {
        return getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .subscribeOn(Schedulers.io())
                .first()
                .map(robotoGetDataRsp -> {
                    if (results.size() != 0)
                        results.clear();
                    results.addAll(convertData(robotoGetDataRsp));
//                            markMesgHasRead();
                    return results;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.size() != 0) {
                        handlerDataResult(list);
                    } else {
                        getView().showNoMesgView();
                    }
                }, AppLogger::e);
    }


    /**
     * 解析转换数据
     *
     * @param robotoGetDataRsp
     */
    private ArrayList<SysMsgBean> convertData(RobotoGetDataRsp robotoGetDataRsp) {
        SysMsgBean bean;
        deleteAllRecords();
        ArrayList<SysMsgBean> results = new ArrayList<SysMsgBean>();
        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : robotoGetDataRsp.map.entrySet()) {
            if (entry.getValue() == null) continue;
            for (JFGDPMsg dp : entry.getValue()) {
                bean = new SysMsgBean();
                bean.type = entry.getKey();
                try {
                    if (bean.type == 701) {
                        DpMsgDefine.DPSystemMesg sysMesg = DpUtils.unpackData(dp.packValue, DpMsgDefine.DPSystemMesg.class);
                        if (sysMesg == null) continue;
                        bean.name = sysMesg.content.trim();
                        bean.content = sysMesg.title.trim();
                        bean.time = dp.version + "";
                        bean.isDone = 0;
                    } else {
                        DpMsgDefine.DPMineMesg mesg = DpUtils.unpackData(dp.packValue, DpMsgDefine.DPMineMesg.class);
                        if (mesg == null) continue;
                        bean.name = mesg.account.trim();
                        bean.isDone = mesg.isDone ? 1 : 0;
                        bean.content = mesg.cid.trim();
                        bean.time = dp.version + "";
                        bean.sn = mesg.sn;
                    }
                    results.add(bean);
                    saveIntoDb(bean);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sortAddReqList(results);
    }

    public ArrayList<SysMsgBean> sortAddReqList(ArrayList<SysMsgBean> list) {
        Comparator<SysMsgBean> comparator = (lhs, rhs) -> {
            long oldTime = Long.parseLong(rhs.time);
            long newTime = Long.parseLong(lhs.time);
            if (oldTime == newTime)
                return 0;
            if (oldTime < newTime)
                return -1;
            return 1;
        };
        Collections.sort(list, comparator);
        return list;
    }

    @Override
    public void deleteServiceMsg(long type, long version) {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    try {
                        ArrayList<JFGDPMsg> list = new ArrayList<JFGDPMsg>();
                        JFGDPMsg msg = new JFGDPMsg(type, version);
                        list.add(msg);
                        long req = BaseApplication.getAppComponent().getCmd().robotDelData("", list, 0);
                        AppLogger.d("deleteServiceMsg:" + req);
                    } catch (JfgException e) {
                        AppLogger.e("deleteServiceMsg:" + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    @Override
    public Subscription deleteMsgBack() {
        return getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteDataRspClass -> {
                    if (getView() != null) getView().deleteMesgReuslt(deleteDataRspClass);
                }, AppLogger::e);
    }

    @Override
    public void deleteOneItem(SysMsgBean bean) {
        Observable.just(bean)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {
                    QueryBuilder<SysMsgBean> cacheBeanBuilder = helper.getDaoSession().getSysMsgBeanDao().queryBuilder();
                    List<SysMsgBean> beanList = cacheBeanBuilder.where(SysMsgBeanDao.Properties.Name.eq(o.name))
                            .list();
                    if (beanList != null)
                        helper.getDaoSession().getSysMsgBeanDao().deleteInTx(beanList);
                }, AppLogger::e);
    }

    @Override
    public void markMesgHasRead() {
        Observable.just(null)
                .observeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        ArrayList<JFGDPMsg> list = new ArrayList<JFGDPMsg>();
                        JFGDPMsg msg1 = new JFGDPMsg(1101L, 0);
                        JFGDPMsg msg2 = new JFGDPMsg(1103L, 0);
                        JFGDPMsg msg3 = new JFGDPMsg(1104L, 0);
                        msg1.packValue = DpUtils.pack(0);
                        msg2.packValue = DpUtils.pack(0);
                        msg3.packValue = DpUtils.pack(0);
                        list.add(msg1);
                        list.add(msg2);
                        list.add(msg3);
                        long req = BaseApplication.getAppComponent().getCmd().robotSetData("", list);
                        AppLogger.d("mine_markHasRead:" + req);
                    } catch (JfgException e) {
                        AppLogger.e("mine_markHasRead:" + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }


}
