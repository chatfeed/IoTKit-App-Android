package com.cylan.jiafeigou.n.view.panorama;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.google.gson.Gson;
import com.lzy.okserver.download.DownloadInfo;
import com.lzy.okserver.download.DownloadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpUtils.unpackData;


/**
 * Created by yanzhendong on 2017/5/10.
 */

public class PanoramaAlbumPresenter extends BasePresenter<PanoramaAlbumContact.View> implements PanoramaAlbumContact.Presenter {
    private Subscription fetchSubscription;
    private Subscription deleteSubscription;
    private Subscription monitorDeleteSubscription;

    private boolean hasSDCard;

    @Inject
    public PanoramaAlbumPresenter(PanoramaAlbumContact.View view) {
        super(view);
        DownloadManager.getInstance().setTargetFolder(JConstant.MEDIA_PATH + File.separator + uuid);
        BaseDeviceInformationFetcher.getInstance().init(uuid);
        if (monitorDeleteSubscription != null && !monitorDeleteSubscription.isUnsubscribed()) {
            monitorDeleteSubscription.unsubscribe();
        }
        monitorDeleteSubscription = monitorDeleteUpdateSub();
        Device device = DataSourceManager.getInstance().getDevice(uuid);

        DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());

        hasSDCard = status.hasSdcard;
    }

    @Override
    public void unsubscribe() {
        super.unsubscribe();
        if (monitorDeleteSubscription != null && !monitorDeleteSubscription.isUnsubscribed()) {
            monitorDeleteSubscription.unsubscribe();
        }
    }

    @Override
    public void stop() {
        super.stop();
//        DownloadManager.getInstance().stopAllTask();//#113300

        //1.1.0 新需求,需要在退出页面是停止下载
        DownloadManager.getInstance().stopAllTask();
    }

    @Override
    public void subscribe() {
        super.subscribe();
        subscribeSDCardUnMount();
        subscribeNetwork();
    }

    private void subscribeNetwork() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    AppLogger.e("监听到网络状态发生变化");
                    BaseDeviceInformationFetcher.getInstance().init(uuid);
                }, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
                });
        addStopSubscription(subscribe);
    }

    private Subscription monitorDeleteUpdateSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeletePanoramaItem.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> mView.onDelete(ret.position), e -> {
                    AppLogger.e(e.getMessage());
                });
    }

    private void subscribeSDCardUnMount() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter(msg -> TextUtils.equals(msg.uuid, uuid))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    AppLogger.e("收到设备同步消息:" + new Gson().toJson(result));
                    try {
                        for (JFGDPMsg msg : result.dpList) {
                            if (msg.id == 222) {
                                DpMsgDefine.DPSdcardSummary sdcardSummary = null;
                                try {
                                    sdcardSummary = unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
                                } catch (Exception e) {
                                    AppLogger.e(e.getMessage());
                                }

                                if (sdcardSummary != null && !sdcardSummary.hasSdcard && hasSDCard) {//SDCard 不存在
                                    mView.onSDCardCheckResult(0);
                                } else if (sdcardSummary != null && sdcardSummary.errCode != 0) {//SDCard 需要格式化
//                                    mView.onSDCardCheckResult(0);
                                }
                                hasSDCard = sdcardSummary != null && sdcardSummary.hasSdcard;
                            } else if (msg.id == 204) {
                                // TODO: 2017/8/17 AP 模式下发的是204 消息,需要特殊处理
//                                Device device = DataSourceManager.getInstance().getDevice(uuid);
//                                if (JFGRules.isAPDirect(uuid, device.$(202, ""))) {
                                DpMsgDefine.DPSdStatus status = unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);
                                if (status != null && !status.hasSdcard && hasSDCard) {//SDCard 不存在
                                    mView.onSDCardCheckResult(0);
                                } else if (status != null && status.err != 0) {//SDCard 需要格式化
//                                    mView.onSDCardCheckResult(0);
                                }
                                hasSDCard = status != null && status.hasSdcard;
//                                }

                            }
                        }
                    } catch (Exception e) {
                        AppLogger.e(e.getMessage());
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        addStopSubscription(subscribe);
    }


    public void checkSDCardAndInit() {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().getSdInfo(uuid)
                .timeout(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (ret != null && !ret.sdIsExist) {//sd 卡不存在
                        mView.onSDCardCheckResult(0);
                    } else {
                        mView.onSDCardCheckResult(1);
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
    }

    @Override
    public void fetch(int time, int fetchLocation) {//0:本地;1:设备;2:本地+设备
        if (fetchSubscription != null && !fetchSubscription.isUnsubscribed()) {
            fetchSubscription.unsubscribe();
        }
        Subscription subscribe = Observable.just("")
                .flatMap(ret -> {
                    Observable<List<PanoramaAlbumContact.PanoramaItem>> observableResult = null;
                    if (fetchLocation == 0) {
                        observableResult = loadFromLocal(time)
                                .delay(1, TimeUnit.SECONDS);
                    } else if (fetchLocation == 1) {
                        observableResult = loadFromServer(time);
                    } else if (fetchLocation == 2) {
                        observableResult = loadFromLocal(time)
                                .observeOn(Schedulers.io())
                                .flatMap(items -> JFGRules.isDeviceOnline(uuid) ?//设备当前真的不在线,就不需要去 Ping 浪费时间等待了
                                        loadFromServer(time).map(items1 -> {
                                            if (items1 != null) {
                                                Map<String, PanoramaAlbumContact.PanoramaItem> sort = new HashMap<>();
                                                items1.addAll(items);
                                                for (PanoramaAlbumContact.PanoramaItem panoramaItem : items1) {
                                                    PanoramaAlbumContact.PanoramaItem panoramaItem1 = sort.get(panoramaItem.fileName);
                                                    if (panoramaItem1 == null) {
                                                        sort.put(panoramaItem.fileName, panoramaItem);
                                                    } else {
                                                        panoramaItem1.location = 2;
                                                    }
                                                }
                                                List<PanoramaAlbumContact.PanoramaItem> result = new ArrayList<>(sort.values());
                                                Collections.sort(result, (o1, o2) -> o2.time == o1.time ? o2.location - o1.location : o2.time - o1.time);
                                                return result.subList(0, result.size() > 20 ? 20 : result.size());
                                            } else {
                                                return items;
                                            }
                                        }) : Observable.just(items)
                                );
                    }
                    if (observableResult == null) {
                        observableResult = Observable.empty();
                    }
                    return observableResult;
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    mView.onAppend(items, time == 0, true, fetchLocation);
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
        addStopSubscription(subscribe);
    }

    private Observable<List<PanoramaAlbumContact.PanoramaItem>> loadFromServer(int time) {
        return BasePanoramaApiHelper.getInstance().getFileList(uuid, 0, time == 0 ? (int) (System.currentTimeMillis() / 1000) : time, 20)
                .timeout(10, TimeUnit.SECONDS, Observable.just(null))//访问网络设置超时时间,访问本地不用设置超时时间
                .onErrorResumeNext(Observable.just(null))
                .map(files -> {
                    List<PanoramaAlbumContact.PanoramaItem> result = new ArrayList<>();
                    if (files != null && files.files != null) {
                        String deviceIp = BasePanoramaApiHelper.getInstance().getDeviceIp();
                        PanoramaAlbumContact.PanoramaItem item;
                        for (String file : files.files) {
                            if (TextUtils.isEmpty(file)) {
                                continue;
                            }
                            try {
                                item = new PanoramaAlbumContact.PanoramaItem(file);
                                item.location = 1;
                                result.add(item);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return result;
                });
    }

    private Observable<List<PanoramaAlbumContact.PanoramaItem>> loadFromLocal(int time) {
        return Observable.just(DownloadManager.getInstance().getAllTask())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(items -> {
                    List<PanoramaAlbumContact.PanoramaItem> result = new ArrayList<>();
                    if (items != null && items.size() > 0) {
                        Collections.sort(items, (item1, item2) -> {
                            int item1Time = parseTime(item1.getFileName());
                            int item2Time = parseTime(item2.getFileName());
                            return item2Time - item1Time;
                        });
                        int finalTime = time == 0 ? Integer.MAX_VALUE : time;
                        PanoramaAlbumContact.PanoramaItem panoramaItem;
                        for (DownloadInfo item : items) {
                            int itemTime = parseTime(item.getFileName());
                            if (itemTime >= finalTime) {
                                continue;
                            }
                            boolean endsWith = item.getTargetPath() != null && item.getTaskKey().startsWith(uuid + "/images");
                            ;
                            if (item.getState() == 4 && FileUtils.isFileExist(item.getTargetPath()) && result.size() < 20 && endsWith) {
                                try {
                                    panoramaItem = new PanoramaAlbumContact.PanoramaItem(item.getFileName());
                                    panoramaItem.location = 0;
                                    panoramaItem.downloadInfo = item;
                                    result.add(panoramaItem);
                                } catch (Exception e) {
                                    AppLogger.e(e.getMessage());
                                }
                            }
                        }
                    }
                    return result;
                });
    }

    private int parseTime(String fileName) {
        if (fileName == null) {
            return 0;
        }
        try {
            return Integer.parseInt(fileName.split("\\.")[0].split("_")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void deletePanoramaItem(List<PanoramaAlbumContact.PanoramaItem> items, int mode) {
        if (deleteSubscription != null && !deleteSubscription.isUnsubscribed()) {
            deleteSubscription.unsubscribe();
        }
        if (mode == 0) {//本地
            deleteSubscription = Observable.create((Observable.OnSubscribe<List<PanoramaAlbumContact.PanoramaItem>>) subscriber -> {
                for (PanoramaAlbumContact.PanoramaItem item : items) {
                    DownloadManager.getInstance().removeTask(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, item.fileName), true);
                }
                subscriber.onNext(items);
                subscriber.onCompleted();
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> mView.onDelete(result), e -> {
                        AppLogger.e(e.getMessage());
                    });
        } else if (mode == 1) {//设备
            deleteSubscription = BasePanoramaApiHelper.getInstance().delete(uuid, 1, 0, convert(items))
                    .map(ret -> {
                        List<PanoramaAlbumContact.PanoramaItem> failed = new ArrayList<>();
                        for (PanoramaAlbumContact.PanoramaItem item : items) {
                            for (String file : ret.files) {
                                if (TextUtils.equals(file, item.fileName)) {
                                    failed.add(item);
                                }
                            }
                        }
                        items.removeAll(failed);
                        return items;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> mView.onDelete(result), e -> {
                        AppLogger.e(e);
                    });
        } else if (mode == 2) {//本地+设备
            deleteSubscription = Observable.create((Observable.OnSubscribe<List<PanoramaAlbumContact.PanoramaItem>>) subscriber -> {
                for (PanoramaAlbumContact.PanoramaItem item : items) {
                    DownloadManager.getInstance().removeTask(PanoramaAlbumContact.PanoramaItem.getTaskKey(uuid, item.fileName), true);
                }
                subscriber.onNext(items);
                subscriber.onCompleted();
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap(ret -> {
                        if (JFGRules.isDeviceOnline(uuid)) {
                            return BasePanoramaApiHelper.getInstance().delete(uuid, 1, 0, convert(items));
                        } else {
                            return Observable.just(null);
                        }

                    })
                    .map(ret -> {
                        if (ret != null && ret.files != null) {
                            List<PanoramaAlbumContact.PanoramaItem> failed = new ArrayList<>();
                            for (PanoramaAlbumContact.PanoramaItem item : items) {
                                for (String file : ret.files) {
                                    if (TextUtils.equals(file, item.fileName)) {
                                        failed.add(item);
                                    }
                                }
                            }
                            items.removeAll(failed);
                        }
                        return items;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> mView.onDelete(result), e -> {
                        AppLogger.e(e);
                    });
        }
    }

    private List<String> convert(List<PanoramaAlbumContact.PanoramaItem> items) {
        List<String> result = new ArrayList<>(items.size());
        for (PanoramaAlbumContact.PanoramaItem item : items) {
            result.add(item.fileName);
        }
        return result;
    }
}
