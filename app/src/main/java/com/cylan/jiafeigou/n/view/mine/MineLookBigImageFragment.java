package com.cylan.jiafeigou.n.view.mine;

import android.content.Context;
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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineLookBigImageContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineLookBigImagePresenterImp;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

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

    private MineLookBigImageContract.Presenter presenter;
    private boolean loadResult = false;
    private String imageUrl;
    private Bitmap bitmapSource;

    public static MineLookBigImageFragment newInstance(Bundle bundle) {
        MineLookBigImageFragment fragment = new MineLookBigImageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        imageUrl = arguments.getString("imageUrl");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_look_big_image, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initImageViewSize();
        initLongClickListener();
        return view;
    }

    private void initPresenter() {
        presenter = new MineLookBigImagePresenterImp(this);
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
     * 初始化大图大小
     */
    private void initImageViewSize() {
        WindowManager wm = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ivLookBigImage.getLayoutParams());
        lp.height = (int) (height * 0.47);
        lp.setMargins(0, (int) (height * 0.23), 0, 0);
        ivLookBigImage.setLayoutParams(lp);
    }


    /**
     * desc:保存图片
     */
    private void showSaveImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(getString(R.string.Tap3_SavePic), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ToastUtil.showToast(getString(R.string.SAVED_PHOTOS));
                if (presenter != null) {
                    presenter.saveImage(bitmapSource);
                }
            }
        }).setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
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
        showLoadImageProgress();
        Glide.with(getContext())
                .load(imageUrl)
                .asBitmap()
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .centerCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new BitmapImageViewTarget(ivLookBigImage) {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        super.onResourceReady(resource, glideAnimation);
                        bitmapSource = resource;
                        hideLoadImageProgress();
                        loadResult = true;
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        hideLoadImageProgress();
                        loadResult = false;
                        ToastUtil.showNegativeToast(getString(R.string.Item_LoadFail));
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
                if (loadResult) {
                    getFragmentManager().popBackStack();
                } else {
                    loadImage();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ivLookBigImage.setImageBitmap(null);
        if (bitmapSource != null && bitmapSource.isRecycled()) {
            bitmapSource.recycle();
            bitmapSource = null;
        }
    }

    @Override
    public void showLoadImageProgress() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
    }

    @Override
    public void hideLoadImageProgress() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }
}
