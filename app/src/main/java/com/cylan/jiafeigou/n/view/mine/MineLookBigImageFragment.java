package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineLookBigImageContract;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineLookBigImageFragment extends Fragment implements MineLookBigImageContract.View {

    @BindView(R.id.iv_look_big_image)
    ImageView ivLookBigImage;
    @BindView(R.id.progress_loading)
    ProgressBar progressLoading;

    private boolean loadResult = false;
    private int imageUrl;

    public static MineLookBigImageFragment newInstance(Bundle bundle) {
        MineLookBigImageFragment fragment =  new MineLookBigImageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        imageUrl = arguments.getInt("imageUrl");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_long_big_image, container, false);
        ButterKnife.bind(this, view);
        initImage();
        initLongClickListener();
        return view;
    }

    private void initLongClickListener() {
        ivLookBigImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showSaveImageDialog();
                return false;
            }
        });
    }

    /**
     * desc:保存图片
     */
    private void showSaveImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton("保存图片", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ToastUtil.showToast(getContext(),"图片保存成功");
                //TODO
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void initImage() {
        if(loadResult){

        }else {
            ViewGroup.LayoutParams layoutParams = ivLookBigImage.getLayoutParams();
            layoutParams.width = 200;
            layoutParams.height = 200;
            ivLookBigImage.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        loadImage();
    }

    /**
     * desc:加载图片
     */
    private void loadImage() {
        Glide.with(getContext())
                .load("")
                .asBitmap()
                .error(imageUrl)
                .centerCrop()
                .into(new BitmapImageViewTarget(ivLookBigImage) {

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        showLoadImageProgress();
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        super.onResourceReady(resource, glideAnimation);
                        hideLoadImageProgress();
                        loadResult = true;
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        hideLoadImageProgress();
                        loadResult = false;
                        ToastUtil.showFailToast(getContext(), "加载失败，点击重试");
                    }

                });
    }

    @Override
    public void setPresenter(MineLookBigImageContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_look_big_image})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_look_big_image:                //点击大图退出全屏
                if(loadResult){
                    getFragmentManager().popBackStack();
                }else {
                    loadImage();
                }
                break;
        }
    }

    @Override
    public void showLoadImageProgress() {
        progressLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadImageProgress() {
        progressLoading.setVisibility(View.INVISIBLE);
    }
}
