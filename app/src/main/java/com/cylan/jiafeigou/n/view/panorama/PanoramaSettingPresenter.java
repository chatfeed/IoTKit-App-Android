package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.ver.AbstractVersion;
import com.cylan.jiafeigou.misc.ver.PanDeviceVersionChecker;
import com.cylan.jiafeigou.module.SubscriptionSupervisor;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/3/11.
 */
public class PanoramaSettingPresenter extends BasePresenter<PanoramaSettingContact.View> implements PanoramaSettingContact.Presenter {

    private PanDeviceVersionChecker version;

    @Inject
    public PanoramaSettingPresenter(PanoramaSettingContact.View view) {
        super(view);
    }

    @Override
    public void start() {
        super.start();
        subscribeNewVersion();
    }

    private void subscribeNewVersion() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(AbstractVersion.BinVersion.class)
                .subscribeOn(Schedulers.io())
                .subscribe(version -> {
                    version.setLastShowTime(System.currentTimeMillis());
                    PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(version));
                    //必须手动断开,因为rxBus订阅不会断开
                    throw new RxEvent.HelperBreaker(version);
                }, AppLogger::e);
        addStopSubscription(subscribe);
        if (version!=null){
            version.clean();
        }
        version = new PanDeviceVersionChecker();
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        version.setPortrait(new AbstractVersion.Portrait().setCid(uuid).setPid(device.pid));
        version.startCheck();
    }

    @Override
    public void stop() {
        super.stop();
        if (version!=null){
            version.clean();
        }
    }

    @Override
    public void unBindDevice() {
        Subscription subscribe = Observable.just(new DPEntity()
                .setUuid(uuid)
                .setAction(DBAction.UNBIND))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(this::perform)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> mView.unbindDeviceRsp(rsp.getResultCode()), e -> {
                    if (e instanceof TimeoutException) {
                        mView.unbindDeviceRsp(-1);
                    }
                    AppLogger.d(e.getMessage());
                    e.printStackTrace();
                }, () -> {
                });
        addStopSubscription(subscribe);
    }
}
