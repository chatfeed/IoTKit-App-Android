package com.cylan.jiafeigou.base.injector.module;

import com.cylan.jiafeigou.base.injector.lifecycle.PerFragment;
import com.cylan.jiafeigou.base.module.BasePresenterInjector;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellDetailContract;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamDelayRecordContract;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareContentContract;
import com.cylan.jiafeigou.n.mvp.contract.setting.AIRecognitionContact;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellDetailSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeWonderfulPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareContentPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.setting.AIRecognitionPresenter;
import com.cylan.jiafeigou.n.view.panorama.Pan720FullContract;
import com.cylan.jiafeigou.n.view.panorama.Pan720FullPresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigureContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigurePresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaPresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaShareContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaSharePresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yanzhendong on 2017/4/12.
 */
@Module
public class FragmentModule {
    @Provides
    @PerFragment
    public static BellDetailContract.Presenter provideBellDetailPresenter(BasePresenterInjector injector) {
        return injector.inject(new BellDetailSettingPresenterImpl());
    }

    @Provides
    @PerFragment
    public static BellSettingContract.Presenter provideBellSettingPresenter(BasePresenterInjector injector) {
        return injector.inject(new BellSettingPresenterImpl());
    }

    @Provides
    @PerFragment
    public static HomeWonderfulContract.Presenter provideHomeWonderfulPresenter(BasePresenterInjector injector) {
        return injector.inject(new HomeWonderfulPresenterImpl());
    }

    @Provides
    @PerFragment
    public static Pan720FullContract.Presenter providePan720FullPresenter(BasePresenterInjector injector) {
        return injector.inject(new Pan720FullPresenter());
    }

    @Provides
    @PerFragment
    public static PanoramaLogoConfigureContact.Presenter providePanoramaLogoConfigurePresenter(BasePresenterInjector injector) {
        return injector.inject(new PanoramaLogoConfigurePresenter());
    }

    @Provides
    @PerFragment
    public static CamDelayRecordContract.Presenter provideCamDelayRecordPresenter(BasePresenterInjector injector) {
        return injector.inject(new CamDelayRecordContract.Presenter());
    }

    @Provides
    @PerFragment
    public static MineShareContentContract.Presenter provideShareContentPresenter(BasePresenterInjector injector) {
        return injector.inject(new MineShareContentPresenterImpl());
    }





    @Provides
    @PerFragment
    public static AIRecognitionContact.Presenter provideAIRecognitionPresenter(BasePresenterInjector injector) {
        AIRecognitionPresenter presenter = injector.inject(new AIRecognitionPresenter());
        return presenter;
    }

}
