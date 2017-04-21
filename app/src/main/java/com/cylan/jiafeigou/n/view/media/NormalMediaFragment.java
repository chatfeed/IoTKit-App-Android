package com.cylan.jiafeigou.n.view.media;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.photoview.PhotoView;
import com.cylan.jiafeigou.utils.CamWarnGlideURL;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.cylan.jiafeigou.misc.JConstant.KEY_SHARED_ELEMENT_LIST;
import static com.cylan.jiafeigou.misc.JConstant.KEY_SHARE_ELEMENT_BYTE;
import static com.cylan.jiafeigou.misc.JConstant.MISC_PATH;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NormalMediaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NormalMediaFragment extends IBaseFragment {

    public static final String KEY_INDEX = "key_index";
    @BindView(R.id.imgV_show_pic)
    PhotoView imgVShowPic;
    private Device device;


    public NormalMediaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 1.
     * @return A new instance of fragment NormalMediaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NormalMediaFragment newInstance(Bundle bundle) {
        NormalMediaFragment fragment = new NormalMediaFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //http://www.androiddesignpatterns.com/2015/03/activity-postponed-shared-element-transitions-part3b.html
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_normal_big_pic, container, false);
        ButterKnife.bind(this, view);
        // Postpone the shared element enter transition in onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getActivity().postponeEnterTransition();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        String uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        int index = getArguments().getInt(KEY_INDEX);
        DpMsgDefine.DPAlarm dpAlarm = getArguments().getParcelable(KEY_SHARED_ELEMENT_LIST);
        if (dpAlarm != null) {
            loadBitmap(dpAlarm, index, uuid);
        } else {
            Bitmap bitmap = getArguments().getParcelable(KEY_SHARE_ELEMENT_BYTE);
            loadBitmap(bitmap);
        }
        imgVShowPic.setOnViewTapListener((View v, float x, float y) -> {
            if (callBack != null) callBack.callBack(null);
        });
    }

    private void loadBitmap(DpMsgDefine.DPAlarm dpAlarm, int index, String uuid) {
        Glide.with(ContextUtils.getContext())
                .load(new CamWarnGlideURL(uuid, dpAlarm.time + "_" + (index + 1) + ".jpg"))
                .asBitmap()
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgVShowPic);
    }

    private void loadBitmap(Bitmap bitmap) {
        if (getView() != null) {
            getView().post(() -> imgVShowPic.setImageDrawable(new BitmapDrawable(getResources(), bitmap)));
        }
//        Glide.with(this)
//                .load(bitmap)
//                .asBitmap()
//                .placeholder(R.drawable.wonderful_pic_place_holder)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(imgVShowPic);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            if (imgVShowPic != null) {
                Bitmap bitmap = imgVShowPic.getVisibleRectangleBitmap();
                if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
                bitmap = imgVShowPic.getDrawingCache();
                if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            }
        } catch (Exception e) {
            AppLogger.e("err:" + MiscUtils.getErr(e));
        }
    }
}
