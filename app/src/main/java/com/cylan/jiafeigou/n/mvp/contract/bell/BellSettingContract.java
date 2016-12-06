package com.cylan.jiafeigou.n.mvp.contract.bell;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.n.mvp.model.BellBean;
import com.cylan.jiafeigou.rx.RxEvent;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public interface BellSettingContract {


    interface View extends BaseView<Presenter> {

        void onSettingInfoRsp(BeanBellInfo bellInfoBean);

        void onLoginState(boolean state);

    }

    interface Presenter extends BasePresenter {
        BeanBellInfo getBellInfo();

        void deleteDevice();
    }
}

