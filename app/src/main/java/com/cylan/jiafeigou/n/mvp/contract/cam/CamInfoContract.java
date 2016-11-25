package com.cylan.jiafeigou.n.mvp.contract.cam;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class CamInfoContract {

    public interface View extends BaseView<Presenter> {


    }

    public interface Presenter extends BasePresenter {
        /**
         * 刷新BeanCamInfo
         *
         * @param info
         */
        void updateCamInfoBean(BeanCamInfo info);

        BeanCamInfo getBeanCamInfo();
    }
}
