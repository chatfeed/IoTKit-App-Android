package com.cylan.jiafeigou.widget.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.support.log.AppLogger;

/**
 * Created by cylan-hunt on 17-3-13.
 */

public class LiveViewWithThumbnail extends FrameLayout implements VideoViewFactory.ILiveView {
    private static final String TAG = "LiveViewWithThumbnail";
    private VideoViewFactory.IVideoView videoView;//视屏view
    private FrameLayout standByLayout;//待机
    private ImageView imgThumbnail;//缩略图
    private TextView tvLiveFlow;//流量
    private ImageView imgLiveMotionArea;
    private DpMsgDefine.Rect4F motionAreaSetting;
    private boolean motionAreaEnabled;

    public LiveViewWithThumbnail(Context context) {
        this(context, null);
    }

    public LiveViewWithThumbnail(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveViewWithThumbnail(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.layout_live_view_with_thumbnail, this, true);
        imgThumbnail = (ImageView) viewGroup.findViewById(R.id.imgv_live_thumbnail);
        imgLiveMotionArea = viewGroup.findViewById(R.id.imgv_live_motion_area);
        imgThumbnail.setOnClickListener(v -> {//do nothing
            Log.d("wat", "wat");
            if (listener != null) {
                listener.onSingleTap(0, 0);
            }
        });
        standByLayout = (FrameLayout) viewGroup.findViewById(R.id.fLayout_standby_mode);
        tvLiveFlow = (TextView) viewGroup.findViewById(R.id.tv_live_flow);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(ev);
    }

    private VideoViewFactory.InterActListener listener;

    public void setInterActListener(VideoViewFactory.InterActListener listener) {
        this.listener = listener;
        if (videoView != null) {
            videoView.setInterActListener(listener);
        }
    }

    public TextView getTvLiveFlow() {
        return tvLiveFlow;
    }

    /**
     * 待机模式的view:"已进入待机模式,前往打开"
     */
    public void enableStandbyMode(boolean enable, OnClickListener onClickListener, boolean isShareDevice) {
        //进入待机模式
        if (enable) {
            standByLayout.setVisibility(VISIBLE);
            standByLayout.bringToFront();
        } else {
            standByLayout.setVisibility(GONE);
            return;
        }
        TextView tv = (TextView) standByLayout.findViewById(R.id.lLayout_standby_jump_setting);
        //分享设备显示：已进入待机状态
        if (isShareDevice) {
            tv.setText(getContext().getString(R.string.Tap1_Camera_Video_Standby));
            return;
        }
        //非分享设备显示：已进入待机状态，前往开启，和设置点击事件。跳转到设置页面
        if (onClickListener != null) {
            tv.setOnClickListener(onClickListener);
        }
    }

    @Override
    public void setLiveView(VideoViewFactory.IVideoView iVideoView, String uuid) {
        this.videoView = iVideoView;
        if (videoView == null) {
            return;
        }
        videoView.setInterActListener(listener);
        View view = (View) this.videoView;
        view.setId("videoView".hashCode());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        view.setVisibility(INVISIBLE);
        addView((View) this.videoView, 0, lp);
    }

    @Override
    public void updateLayoutParameters(int height, int width) {
        if (videoView == null) {
            return;
        }
        AppLogger.d("更新view高度: " + height);
        RelativeLayout.LayoutParams parentLp = (RelativeLayout.LayoutParams) getLayoutParams();
        parentLp.height = height;
        setLayoutParams(parentLp);
        ViewGroup.LayoutParams lp = ((View) videoView).getLayoutParams();
        lp.height = height;
        lp.width = width;
        ((View) videoView).setLayoutParams(lp);
        post(this::updateMotionAreaParameterInternal);
    }

    @Override
    public void updateMotionAreaParameters(DpMsgDefine.Rect4F rect4F, boolean enable) {
        AppLogger.d("updateMotionAreaParameters:rect:" + rect4F + ",enable:" + enable);
        this.motionAreaEnabled = enable;
        this.motionAreaSetting = rect4F;
        post(this::updateMotionAreaParameterInternal);
    }

    private void updateMotionAreaParameterInternal() {
        final boolean enable = motionAreaEnabled;
        final DpMsgDefine.Rect4F rect4F = motionAreaSetting;
        if (!enable) {
            imgLiveMotionArea.setVisibility(GONE);
            return;
        }
        FrameLayout.LayoutParams layoutParams = (LayoutParams) imgLiveMotionArea.getLayoutParams();
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        float widthRadio = rect4F == null ? 1 : rect4F.right - rect4F.left;
        float heightRadio = rect4F == null ? 1 : rect4F.bottom - rect4F.top;
        layoutParams.width = (int) (measuredWidth * widthRadio);
        layoutParams.height = (int) (measuredHeight * heightRadio);
        int marginLeft = (int) (measuredWidth * (rect4F == null ? 0 : rect4F.left));
        int marginTop = (int) (measuredHeight * (rect4F == null ? 0 : rect4F.top));
        int marginRight = 0;
        int marginBottom = 0;
        layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
        imgLiveMotionArea.setVisibility(VISIBLE);
        imgLiveMotionArea.setLayoutParams(layoutParams);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        post(this::updateMotionAreaParameterInternal);
    }

    @Override
    public void showFlowView(boolean show, String content) {
        tvLiveFlow.setText(content);
        tvLiveFlow.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    @Override
    public void detectOrientationChanged(boolean port) {
        if (videoView == null) {
            AppLogger.e("这是个bug");
            return;
        }
        FrameLayout.LayoutParams lp = (LayoutParams) tvLiveFlow.getLayoutParams();
        if (port) {
            lp.rightMargin = (int) getResources().getDimension(R.dimen.x14);
            lp.topMargin = (int) getResources().getDimension(R.dimen.x14);
        } else {
            lp.rightMargin = (int) getResources().getDimension(R.dimen.x14);
            lp.topMargin = (int) getResources().getDimension(R.dimen.x54);
        }
        tvLiveFlow.setLayoutParams(lp);
        videoView.detectOrientationChanged();

    }

    @Override
    public void showMobileDataInterface(OnClickListener clickListener) {
        final View v = findViewById(R.id.v_mobile_data_cover);
        if (v != null && v.isShown()) {
            return;
        } else if (v != null && !v.isShown()) {
            v.setVisibility(VISIBLE);
            findViewById(R.id.btn_go_ahead)
                    .setOnClickListener(v1 -> {
                        v.setVisibility(GONE);//
                        if (clickListener != null) {
                            clickListener.onClick(v1);
                        }
                    });
            AppLogger.d("显示手机数据,层");
        }
    }

    @Override
    public void showVideoView(boolean show) {
        imgThumbnail.setVisibility(show ? INVISIBLE : VISIBLE);
        if (videoView != null) {
            ((View) videoView).setVisibility(show ? VISIBLE : INVISIBLE);
        }
    }

    @Override
    public void showLiveThumbPicture(Bitmap bitmap, boolean normalView) {
        if (normalView) {
            ViewGroup.LayoutParams lp = imgThumbnail.getLayoutParams();
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            imgThumbnail.setLayoutParams(lp);
            imgThumbnail.setVisibility(VISIBLE);
            if (bitmap != null && !bitmap.isRecycled()) {
                imgThumbnail.setImageBitmap(bitmap);
            } else {
                imgThumbnail.setImageResource(R.drawable.default_diagram_mask);
            }
        } else if (videoView != null) {
            View videoView = (View) LiveViewWithThumbnail.this.videoView;
            videoView.setVisibility(VISIBLE);
            if (bitmap != null && !bitmap.isRecycled()) {
                LiveViewWithThumbnail.this.videoView.loadBitmap(bitmap);
                imgThumbnail.setVisibility(INVISIBLE);
                imgThumbnail.setImageResource(android.R.color.transparent);
            } else {
                imgThumbnail.setVisibility(VISIBLE);
                imgThumbnail.setImageResource(R.drawable.default_diagram_mask);
            }
        }
    }

    public void hideThumbPicture() {
        imgThumbnail.setVisibility(INVISIBLE);
    }

    public void showBlackBackground() {
        imgThumbnail.setVisibility(VISIBLE);
        imgThumbnail.setImageResource(R.color.color_black);
    }

    public void destroyVideoView() {
        try {
            if (videoView != null) {
                videoView.onPause();
                videoView.onDestroy();
            }
        } catch (Exception e) {
        }
    }
}
