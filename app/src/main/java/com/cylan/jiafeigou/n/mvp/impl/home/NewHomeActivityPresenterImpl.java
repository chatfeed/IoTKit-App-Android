package com.cylan.jiafeigou.n.mvp.impl.home;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.LoginHelper;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.NewHomeActivityContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.concurrent.TimeUnit;

import permissions.dispatcher.PermissionUtils;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-23.
 */
public class NewHomeActivityPresenterImpl extends AbstractPresenter<NewHomeActivityContract.View>
        implements NewHomeActivityContract.Presenter {

    public NewHomeActivityPresenterImpl(NewHomeActivityContract.View view) {
        super(view);
        view.initView();
    }

    private void updateRsp() {
        Subscription subscribe = RxBus.getCacheInstance().toObservableSticky(RxEvent.ApkDownload.class)
                .subscribeOn(Schedulers.io())
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    long time = PreferencesUtils.getLong(JConstant.KEY_CLIENT_NEW_VERSION_DIALOG);
                    boolean force = ret.updateType == RxEvent.UpdateType.GOOGLE_PLAY || (ret.forceUpdate == 1);//强制升级
                    if (force || time == 0 || System.currentTimeMillis() - time > 24 * 3600 * 1000) {
                        PreferencesUtils.putLong(JConstant.KEY_CLIENT_NEW_VERSION_DIALOG, System.currentTimeMillis());
                        mView.needUpdate(ret.updateType, "", ret.filePath, ret.forceUpdate);
                    }
                    if (!force) {
                        RxBus.getCacheInstance().removeStickyEvent(RxEvent.ApkDownload.class);
                    }
                }, throwable -> {
                    AppLogger.e("err:" + MiscUtils.getErr(throwable));
                    RxBus.getCacheInstance().removeStickyEvent(RxEvent.ApkDownload.class);
//                    addSubscription(updateRsp());
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void start() {
        super.start();
        updateRsp();
        mineTabNewInfoRsp();
    }

    private void mineTabNewInfoRsp() {
        Subscription subscribe = RxBus.getCacheInstance().toObservableSticky(RxEvent.InfoUpdate.class)
                .subscribeOn(Schedulers.io())
                .throttleFirst(2, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    TreeNode node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName("HomeMineFragment");
                    mView.refreshHint(node != null && node.getTraversalCount() > 0);
                }, throwable -> mineTabNewInfoRsp());
        addStopSubscription(subscribe);
    }

    @Override
    public void performLoginVerify() {
        JFGAccount account = DataSourceManager.getInstance().getJFGAccount();
        if (account == null && PermissionUtils.hasSelfPermissions(ContextUtils.getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //为什么在这个页面操作：被回收，可以在任何一个页面。
            AppLogger.e("被系统回收了，或者Oppo手机，任何一个权限由->禁止，就会kill.导致整个appInit出错");
            Subscription subscribe = LoginHelper.performAutoLogin().subscribe(accountArrived -> {
            }, error -> {
                AppLogger.e(MiscUtils.getErr(error));
            });
            addDestroySubscription(subscribe);
        }
    }
}
