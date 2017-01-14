package com.cylan.jiafeigou.n.view.bind;


import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.bind.BindDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.BindDevicePresenterImpl;
import com.cylan.jiafeigou.n.view.BaseTitleFragment;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.utils.ListUtils;
import com.nineoldandroids.animation.Animator;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BindCameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BindCameraFragment extends BaseTitleFragment<BindDeviceContract.Presenter> implements BindDeviceContract.View {
    public static final String KEY_SUB_FRAGMENT_ID = "sub_key_id";
    public static final String KEY_DEVICE_LIST = "key_device_list";

    @BindView(R.id.imgV_camera_wifi_light_flash)
    ImageView imgVCameraWifiLightFlash;
    @BindView(R.id.imgV_camera_hand)
    ImageView imgVCameraHand;
    @BindView(R.id.imgV_camera_red_dot)
    ImageView imgVCameraRedDot;
    private Animator animator;

    public BindCameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 2.
     * @return A new instance of fragment BindCameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BindCameraFragment newInstance(Bundle bundle) {
        BindCameraFragment fragment = new BindCameraFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.basePresenter = new BindDevicePresenterImpl(this);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAnimation();
    }


    @Override
    protected int getSubContentViewId() {
        return R.layout.fragment_bind_camera;
    }


    private void initAnimation() {
        animator = AnimatorUtils.onHandMoveAndFlash(imgVCameraHand, imgVCameraRedDot, imgVCameraWifiLightFlash);
        animator.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (animator != null && animator.isRunning())
            animator.cancel();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate activity_cloud_live_mesg_call_out_item fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @OnClick(R.id.tv_bind_camera_tip)
    public void onClick() {
        if (getView() != null)
            ViewUtils.deBounceClick(getView().findViewById(R.id.tv_bind_camera_tip));
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_BIND_DEVICE, getString(R.string.DOG_CAMERA_NAME));
        BindGuideFragment fragment = BindGuideFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                fragment, android.R.id.content);
        cancelAnimation();
    }


    @Override
    public void onDevicesRsp(List<ScanResult> resultList) {
        final int count = ListUtils.getSize(resultList);
        if (count == 0) {
            Toast.makeText(getContext(), "没发现设备", Toast.LENGTH_SHORT).show();
            return;
        }
//        initDeviceListFragment();
//
//        if (listFragmentWeakReference.get().isResumed()) {
//            listFragmentWeakReference.get().updateList((ArrayList<ScanResult>) resultList);
//            Log.d("simple", "what the hell.....");
//            return;
//        }
//        if (getActivity().getSupportFragmentManager().findFragmentByTag("BindDeviceListFragment") != null)
//            return;
//        Bundle bundle = getArguments();
//        if (bundle == null) {
//            bundle = new Bundle();
//        }
//        Log.d("simple", "what the hell");
//        bundle.putInt(KEY_SUB_FRAGMENT_ID, R.id.fLayout_bind_device_list_fragment_container);
//        bundle.putParcelableArrayList(KEY_DEVICE_LIST, (ArrayList<? extends Parcelable>) resultList);
//        getActivity().getSupportFragmentManager()
//                .beginTransaction()
//                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
//                        , R.anim.slide_in_left, R.anim.slide_out_right)
//                .add(android.R.id.content, listFragmentWeakReference.get(), "BindDeviceListFragment")
//                .addToBackStack("BindDeviceListFragment")
//                .commit();
//        cancelAnimation();
    }

    private void cancelAnimation() {
        if (animator != null && animator.isRunning())
            animator.cancel();
    }

    @Override
    public void onNoListError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(getContext(), "请启用定位服务", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "没有wifi列表", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNoJFGDevices() {
        Toast.makeText(getContext(), "找不到设备啊", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(BindDeviceContract.Presenter presenter) {
        this.basePresenter = presenter;
    }
}
