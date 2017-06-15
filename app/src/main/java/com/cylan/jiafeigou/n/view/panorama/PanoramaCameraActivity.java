package com.cylan.jiafeigou.n.view.panorama;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.databinding.LayoutPanoramaPopMenuBinding;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.view.firmware.FirmwareUpdateActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.photoselect.CircleImageView;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PanoramaThumbURL;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.ImageViewTip;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.live.ILiveControl;
import com.cylan.jiafeigou.widget.video.PanoramicView720_Ext;
import com.cylan.jiafeigou.widget.video.VideoViewFactory;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static com.cylan.jiafeigou.base.module.PanoramaEvent.ERROR_CODE_HTTP_NOT_AVAILABLE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_205_CHARGING;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_206_BATTERY;
import static com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact.View.PANORAMA_RECORD_MODE.MODE_SHORT;

/**
 * Created by yanzhendong on 2017/3/7.
 */
@RuntimePermissions
public class PanoramaCameraActivity extends BaseActivity<PanoramaCameraContact.Presenter> implements PanoramaCameraContact.View {

    @BindView(R.id.act_panorama_camera_banner)
    ViewSwitcher bannerSwitcher;
    @BindView(R.id.imgv_toolbar_right)
    ImageViewTip setting;
    @BindView(R.id.act_panorama_camera_banner_information_connection_icon)
    ImageView bannerConnectionIcon;
    @BindView(R.id.act_panorama_camera_banner_information_connection_text)
    TextView bannerConnectionText;
    @BindView(R.id.act_panorama_camera_banner_information_charge_icon)
    ImageView bannerChargeIcon;
    @BindView(R.id.act_panorama_camera_banner_information_charge_text)
    TextView bannerChargeText;
    @BindView(R.id.act_panorama_camera_toolbar)
    FrameLayout panoramaToolBar;
    @BindView(R.id.act_panorama_camera_flow_speed)
    TextView liveFlowSpeedText;
    @BindView(R.id.act_panorama_camera_banner_bad_net_work_configure)
    TextView bannerWarmingTitle;
    @BindView(R.id.act_panorama_camera_bottom_panel_picture)
    RadioButton bottomPanelPictureMode;
    @BindView(R.id.act_panorama_camera_bottom_panel_video)
    RadioButton bottomPanelVideoMode;
    @BindView(R.id.act_panorama_camera_bottom_panel_more)
    ImageButton bottomPanelMoreItem;
    @BindView(R.id.act_panorama_bottom_panel_camera_photograph)
    ImageButton bottomPanelPhotoGraphItem;
    @BindView(R.id.act_panorama_camera_bottom_panel_album)
    CircleImageView bottomPanelAlbumItem;
    @BindView(R.id.act_panorama_camera_bottom_panel_switcher_menu_item)
    RadioGroup bottomPanelSwitcherItem1ViewMode;
    @BindView(R.id.act_panorama_camera_bottom_panel_switcher_menu)
    ViewSwitcher bottomPanelSwitcher;
    @BindView(R.id.act_panorama_camera_bottom_panel_switcher_menu_information)
    ConstraintLayout bottomPanelSwitcherItem2Information;
    @BindView(R.id.act_panorama_camera_bottom_panel_switcher_menu_information_record_time)
    TextView bottomPanelSwitcherItem2TimeText;
    @BindView(R.id.act_panorama_camera_bottom_panel_switcher_menu_information_red_dot)
    ImageView bottomPanelSwitcherItem2DotIndicator;
    @BindView(R.id.act_panorama_camera_video_container)
    FrameLayout videoLiveContainer;
    @BindView(R.id.act_panorama_camera_loading)
    ILiveControl loadingBar;
    @BindView(R.id.act_panorama_camera_bottom_count_down_line)
    View bottomCountDownLine;
    @BindView(R.id.tv_top_bar_left)
    Button topLeftMenu;
    @BindView(R.id.act_panorama_camera_bottom_panel)
    ConstraintLayout bottomPanelContainer;
    @BindView(R.id.act_panorama_bottom_panel_loading)
    ProgressBar bottomPanelLoading;

    @SPEED_MODE
    private int speedMode = SPEED_MODE.AUTO;
    @PANORAMA_VIEW_MODE
    private int panoramaViewMode = PANORAMA_VIEW_MODE.MODE_PICTURE;
    @PANORAMA_RECORD_MODE
    private int panoramaRecordMode = PANORAMA_RECORD_MODE.MODE_NONE;
    private PopupWindow videoPopHint;
    private PanoramicView720_Ext surfaceView;
    private ConnectionDialog connectionDialog;
    private AlertDialog mobileAlert;
    private boolean hasResolution = false;
    private ConnectionDescriptionFragment fragment;
    private boolean justForTest = false;
    private boolean hasNetSetting = false;
    private PopupWindow popOption;
    private LayoutPanoramaPopMenuBinding menuBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        JConstant.KEY_CURRENT_PLAY_VIEW = this.getClass().getName();
    }

    @Override
    public void onShowProperty(Device device) {
        boolean apDirect = JFGRules.isAPDirect(uuid, device.$(202, ""));
        if (!apDirect) {
            int battery = device.$(ID_206_BATTERY, 0);
            boolean charging = device.$(ID_205_CHARGING, false);
            onDeviceBatteryChanged(charging ? -1 : battery);
        }
        onRefreshConnectionMode(-1);
    }

    @Override
    public void onViewer() {
        liveFlowSpeedText.setText("0K/s");
        loadingBar.setState(JConstant.PLAY_STATE_PREPARE, null);
        onHideBadNetWorkBanner();
    }

    @Override
    public void onViewAction(int action, String handler, Object extra) {
        super.onViewAction(action, handler, extra);
        if (action == VIEW_ACTION_OK) {
            hasNetSetting = false;
            int netType = NetUtils.getNetType(this);
            boolean alertMobile = netType == ConnectivityManager.TYPE_MOBILE && PreferencesUtils.getBoolean(JConstant.ALERT_MOBILE);
            if (!hasNetSetting) {
                presenter.startViewer();
            }
            onRefreshConnectionMode(alertMobile ? 1 : -1);
            onRefreshViewModeUI(panoramaViewMode, false);
        }
    }

    @Override
    public void onDismiss() {
        if (surfaceView != null) {
            surfaceView.onDestroy();
        }
    }

    public void showLoading(String msg) {
        if (LoadingDialog.isShowing(getSupportFragmentManager())) {
            LoadingDialog.dismissLoading(getSupportFragmentManager());
        }
        LoadingDialog.showLoading(getSupportFragmentManager(), msg);
    }

    public void dismissLoading() {
        if (LoadingDialog.isShowing(getSupportFragmentManager())) {
            LoadingDialog.dismissLoading(getSupportFragmentManager());
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        menuBinding = LayoutPanoramaPopMenuBinding.inflate(getLayoutInflater());//先初始化,以免为空
        View contentView = menuBinding.getRoot();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popOption = new PopupWindow(contentView, contentView.getMeasuredWidth(), contentView.getMeasuredHeight());
        popOption.setFocusable(true);
        menuBinding.actPanoramaCameraQuickMenuItem2Voice.setEnabled(false);//默认不可点击,只有收到分辨率后才能点击
        menuBinding.actPanoramaCameraQuickMenuItem1Mic.setEnabled(false);
        menuBinding.actPanoramaCameraQuickMenuItem3Left.setEnabled(false);
        menuBinding.actPanoramaCameraQuickMenuItem3Right.setEnabled(false);
        menuBinding.actPanoramaCameraQuickMenuItem2Voice.setOnClickListener(this::clickedQuickMenuItem2SwitchVoice);
        menuBinding.actPanoramaCameraQuickMenuItem1Mic.setOnClickListener(this::clickedQuickMenuItem1SwitchMic);
        menuBinding.actPanoramaCameraQuickMenuItem3Left.setOnClickListener(this::clickedQuickMenuItem3Left);
        menuBinding.actPanoramaCameraQuickMenuItem3Right.setOnClickListener(this::clickedQuickMenuItem3Right);
        bottomPanelLoading.setEnabled(false);
        String picture = PreferencesUtils.getString(JConstant.PANORAMA_THUMB_PICTURE + ":" + uuid);
        if (!TextUtils.isEmpty(picture)) {
            onShowPreviewPicture(picture);
        }
        Device device = sourceManager.getDevice(uuid);
        String alias = device.getAlias();
        topLeftMenu.setText(TextUtils.isEmpty(alias) ? getString(R.string._720PanoramicCamera) : alias);
        loadingBar.setAction(loadController);
        bottomPanelPhotoGraphItem.setOnTouchListener(photoGraphTouchListener);
        panoramaToolBar.setBackgroundResource(JFGRules.getTimeRule() == 0 ? R.color.color_0ba8cf : R.color.color_23344e);
    }

    private View.OnTouchListener photoGraphTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP: {
                    if (panoramaRecordMode == MODE_SHORT) {
                        AppLogger.d("录制短视频结束了");
                        onRefreshControllerView(false);
                        presenter.stopVideoRecord(PANORAMA_RECORD_MODE.MODE_SHORT);
                        bottomPanelLoading.setVisibility(View.VISIBLE);
                        bottomPanelPhotoGraphItem.setEnabled(false);
                    }
                    break;
                }
            }
            return false;
        }
    };

    private ILiveControl.Action loadController = new ILiveControl.Action() {
        @Override
        public void clickImage(View view, int state) {
            onHideBadNetWorkBanner();
            presenter.startViewer();
        }

        @Override
        public void clickText(View view) {

        }

        @Override
        public void clickHelp(View view) {

        }
    };

    @Override
    public void onSpeaker(boolean on) {
        menuBinding.actPanoramaCameraQuickMenuItem2Voice.setImageResource(on ? R.drawable.camera720_icon_voice_selector : R.drawable.camera720_icon_no_voice_selector);
    }

    @Override
    public void onMicrophone(boolean on) {
        menuBinding.actPanoramaCameraQuickMenuItem1Mic.setImageResource(on ? R.drawable.camera720_icon_talk_selector : R.drawable.camera720_icon_no_talk_selector);
    }

    @Override
    public void onResolution(JFGMsgVideoResolution resolution) throws JfgException {
        hasResolution = true;
        if (surfaceView == null) {
            surfaceView = (PanoramicView720_Ext) VideoViewFactory.CreateRendererExt(VideoViewFactory.RENDERER_VIEW_TYPE.TYPE_PANORAMA_720, this, true);
            surfaceView.configV720();
            surfaceView.setDisplayMode(PanoramicView720_Ext.DM_Fisheye);
            surfaceView.setId("IVideoView".hashCode());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            surfaceView.setLayoutParams(params);
            videoLiveContainer.addView(surfaceView);

        }
        menuBinding.actPanoramaCameraQuickMenuItem3Left.setEnabled(true);
        menuBinding.actPanoramaCameraQuickMenuItem3Right.setEnabled(true);
        appCmd.enableRenderSingleRemoteView(true, surfaceView);
        loadingBar.setState(JConstant.PLAY_STATE_IDLE, null);
        onRefreshViewModeUI(panoramaViewMode, true);
        onHideBadNetWorkBanner();
    }

    @Override
    public void onShowPreviewPicture(String picture) {
        AppLogger.e("正在加载相册最新缩略图" + PreferencesUtils.getString(JConstant.PANORAMA_THUMB_PICTURE + ":" + uuid) + "," + picture);
        Glide.with(this)
                .load(new PanoramaThumbURL(uuid, picture))
                .animate(view -> {
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.2f, 1.0f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.2f, 1.0f);
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(scaleX, scaleY);
                    set.setDuration(200);
                    set.start();
                })
                .error(R.drawable.camera720_icon_album_selector)
                .placeholder(R.drawable.camera720_icon_album_selector)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new ImageViewTarget<GlideDrawable>(bottomPanelAlbumItem) {
                    @Override
                    protected void setResource(GlideDrawable resource) {
                        PreferencesUtils.putString(JConstant.PANORAMA_THUMB_PICTURE + ":" + uuid, picture);
                        view.setImageDrawable(resource);

                    }
                });

    }

    @Override
    public void onFlowSpeed(int speed) {
        liveFlowSpeedText.setText(MiscUtils.getByteFromBitRate(speed));
    }

    @Override
    public void onConnectDeviceTimeOut() {
        onRefreshConnectionMode(-1);
    }

    @Override
    public void onVideoDisconnect(int code) {
        loadingBar.setState(JConstant.PLAY_STATE_LOADING_FAILED, null);
        if (code == JError.ErrorVideoPeerInConnect) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.CONNECTING)
                    .setCancelable(false)
                    .setPositiveButton(R.string.OK, (dialog, which) -> {
                        dialog.dismiss();
                        presenter.dismiss();
                        finish();
                    })
                    .show();
        } else {
            onRefreshConnectionMode(-1);
        }
        onRefreshViewModeUI(panoramaViewMode, false);
        hasResolution = false;
    }

    @Override
    public void onDeviceUnBind() {
        AppLogger.d("当前设备已解绑" + uuid);
        presenter.cancelViewer();
        AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(getActivityContext());
        builder.setPositiveButton(getString(R.string.OK), (dialog, which) -> {
            finish();
            Intent intent = new Intent(this, NewHomeActivity.class);
            startActivity(intent);
        })
                .setMessage(getString(R.string.Tap1_device_deleted));
        AlertDialogManager.getInstance().showDialog("onDeviceUnBind", this, builder);
    }

    @Override
    public void onLoading(boolean loading) {
        loadingBar.setState(loading ? JConstant.PLAY_STATE_PREPARE : JConstant.PLAY_STATE_IDLE, null);
//        onRefreshControllerView(!loading);//loading 不再更新ControllerView 因为可能有冲突
        if (!loading) {
            onHideBadNetWorkBanner();
        }
        AppLogger.d("正在加载中.......");
    }

    @Override
    public void onShowVideoPreviewPicture(String picture) {

    }

    @Override
    public void hasNoAudioPermission() {
        AppLogger.d("没有声音权限.......");
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_panorama_camera;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewUtils.setViewPaddingStatusBar(panoramaToolBar);
        hasResolution = false;
        setting.setShowDot(!TextUtils.isEmpty(PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + uuid)));
        int netType = NetUtils.getNetType(this);
        boolean alertMobile = netType == ConnectivityManager.TYPE_MOBILE && PreferencesUtils.getBoolean(JConstant.ALERT_MOBILE);
        if (!hasNetSetting) {//fragment 和 activity 会同时调用生命周期方法我们的播放逻辑必须在当前没有 fragment 的情况下进行
            presenter.startViewer();
        }
        onRefreshConnectionMode(alertMobile ? 1 : -1);
        onRefreshViewModeUI(panoramaViewMode, false);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewUtils.clearViewPaddingStatusBar(panoramaToolBar);
        presenter.dismiss();
    }

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    public boolean muteAudio(boolean bMute) {
        boolean isSuccess;
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (bMute) {
            int result = am.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            isSuccess = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        } else {
            int result = am.abandonAudioFocus(null);
            isSuccess = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        }
        AppLogger.e("pauseMusic bMute=" + bMute + " result=" + isSuccess);
        return isSuccess;
    }


    @OnClick(R.id.act_panorama_camera_bottom_panel_more)
    public void clickedBottomPanelMoreItem() {
        AppLogger.d("clickedBottomPanelMoreItem");
        if (popOption.isShowing()) {
            popOption.dismiss();
        } else {
            PopupWindowCompat.showAsDropDown(popOption, bottomPanelContainer, 0, 0, Gravity.TOP | Gravity.END);
        }
    }

    @OnLongClick(R.id.act_panorama_bottom_panel_camera_photograph)
    public boolean longClickedBottomPanelPhotoGraphItem() {
        AppLogger.d("longClickedBottomPanelPhotoGraphItem");
        if (panoramaViewMode == PANORAMA_VIEW_MODE.MODE_VIDEO && panoramaRecordMode == PANORAMA_RECORD_MODE.MODE_NONE) {
            presenter.startVideoRecord(PANORAMA_RECORD_MODE.MODE_SHORT);
        }
        hideVideoModePop();
        return false;
    }

    @OnClick(R.id.act_panorama_bottom_panel_camera_photograph)
    public void clickedBottomPanelPhotoGraphItem() {
        if (panoramaViewMode == PANORAMA_VIEW_MODE.MODE_PICTURE) {
            AppLogger.d("将进行拍照");
            presenter.makePhotograph();
        } else if (panoramaRecordMode == PANORAMA_RECORD_MODE.MODE_NONE) {
            presenter.startVideoRecord(panoramaRecordMode = PANORAMA_RECORD_MODE.MODE_LONG);
        } else if (panoramaRecordMode == PANORAMA_RECORD_MODE.MODE_LONG) {
            presenter.stopVideoRecord(panoramaRecordMode);
        }
        hideVideoModePop();
        onRefreshControllerView(false);
        bottomPanelLoading.setVisibility(View.VISIBLE);
        bottomPanelPhotoGraphItem.setEnabled(false);
    }

    @OnClick(R.id.act_panorama_camera_bottom_panel_album)
    public void clickedBottomPanelAlbumItem() {
        AppLogger.d("clickedBottomPanelAlbumItem");
        presenter.dismiss();
        Intent intent = new Intent(this, PanoramaAlbumActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        startActivity(intent);
    }

    @OnClick(R.id.act_panorama_camera_bottom_panel_picture)
    public void switchViewerModeToPicture() {
        AppLogger.d("switchViewerModeToPicture");
        hideVideoModePop();
        onRefreshViewModeUI(PANORAMA_VIEW_MODE.MODE_PICTURE, true);
    }

    @OnClick(R.id.act_panorama_camera_bottom_panel_video)
    public void switchViewerModeToVideo() {
        AppLogger.d("switchViewerModeToVideo");
        if (PreferencesUtils.getBoolean(JConstant.KEY_PANORAMA_POP_HINT, true)) {//只提示一次
            PreferencesUtils.putBoolean(JConstant.KEY_PANORAMA_POP_HINT, false);
            showVideoModePop();
        }
        onRefreshViewModeUI(PANORAMA_VIEW_MODE.MODE_VIDEO, true);
    }

    public void showVideoModePop() {
        if (videoPopHint == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_panorama_pop_hint, null);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            videoPopHint = new PopupWindow(view, view.getMeasuredWidth(), view.getMeasuredHeight());
            videoPopHint.setFocusable(false);
            videoPopHint.setOutsideTouchable(false);
            videoPopHint.setTouchable(false);
            videoPopHint.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        int xPos = (int) ((videoPopHint.getWidth() - bottomPanelVideoMode.getWidth()) / 2 + getResources().getDimension(R.dimen.y2));
        int yPos = (int) (videoPopHint.getHeight() + getResources().getDimension(R.dimen.y2) + bottomPanelVideoMode.getHeight());
        videoPopHint.showAsDropDown(bottomPanelVideoMode, -xPos, -yPos);
    }

    public boolean hideVideoModePop() {
        if (videoPopHint != null && videoPopHint.isShowing()) {
            videoPopHint.dismiss();
            return true;
        }
        return false;
    }

    @OnClick(R.id.imgv_toolbar_right)
    public void clickedToolBarSettingMenu() {
        AppLogger.d("clickedSettingMenu");
        hideVideoModePop();
        presenter.dismiss();
        startActivity(new Intent(this, PanoramaSettingActivity.class));
    }

    @Override
    @OnClick(R.id.tv_top_bar_left)
    public void onBackPressed() {
        if (!hideVideoModePop()) {
            presenter.dismiss();
            super.onBackPressed();
        }
        AppLogger.d("clickedToolBarBackMenu");
    }

    @Override
    protected void onPrepareToExit(Action action) {
        presenter.dismiss();
        if (surfaceView != null) {
            surfaceView.onPause();
            videoLiveContainer.removeAllViews();
            surfaceView = null;
            muteAudio(false);
        }
        action.actionDone();
    }

    @OnClick(R.id.act_panorama_camera_banner_bad_net_work_close)
    public void clickedCloseBadNetWorkBanner() {
        AppLogger.d("clickedCloseBadNetWorkBanner");
        onHideBadNetWorkBanner();
    }

    @OnClick(R.id.act_panorama_camera_banner_bad_net_work_configure)
    public void clickedConfigureNetWorkBanner() {
        AppLogger.d("clickedConfigureNetWorkBanner");
        CharSequence text = bannerWarmingTitle.getText();
        if (TextUtils.equals(text, getString(R.string.Tap1_DisconnectedPleaseCheck))) {
            showConfigNetWorkFragment();
        } else if (TextUtils.equals(text, getString(R.string.Tap1_Offline))) {
            showConfigConnectionDialog();
        } else if (TextUtils.equals(text, getString(R.string.Tips_Device_TimeoutRetry))) {

        }
    }

    private void showConfigNetWorkFragment() {
        if (fragment == null) {
            fragment = ConnectionDescriptionFragment.newInstance();
        }
        hasNetSetting = true;
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(), fragment, android.R.id.content);
    }


    public void clickedQuickMenuItem1SwitchMic(View view) {
        AppLogger.d("clickedQuickMenuItem1SwitchMic");
        PanoramaCameraActivityPermissionsDispatcher.switchMicroPhoneWithPermissionWithCheck(this);
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    void switchSpeakerWithPermission() {
        presenter.switchSpeaker();
    }

    @OnShowRationale(Manifest.permission.RECORD_AUDIO)
    public void showEnablePermissionAlert(PermissionRequest request) {
        AppLogger.d("需要提醒用户给 APP 授权");
    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    public void onNoAudioPermission() {
        AppLogger.d("没有控制扬声器和麦克风的权限");
    }


    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    void switchMicroPhoneWithPermission() {
        presenter.switchMicrophone();
    }

    public void clickedQuickMenuItem2SwitchVoice(View view) {
        AppLogger.d("clickedQuickMenuItem2SwitchVoice");
        PanoramaCameraActivityPermissionsDispatcher.switchSpeakerWithPermissionWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PanoramaCameraActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public void clickedQuickMenuItem3Left(View view) {
        AppLogger.d("clickedQuickMenuItem3Left");
        presenter.switchVideoResolution(move(speedMode, false));
    }

    private
    @SPEED_MODE
    int move(@SPEED_MODE int mode, boolean next) {
        switch (mode) {
            case SPEED_MODE.NORMAL:
                return next ? SPEED_MODE.HD : SPEED_MODE.HD;
            case SPEED_MODE.HD:
                return next ? SPEED_MODE.NORMAL : SPEED_MODE.NORMAL;
            case SPEED_MODE.AUTO:
                return next ? SPEED_MODE.NORMAL : SPEED_MODE.HD;
            case SPEED_MODE.FLUENCY:
                return next ? SPEED_MODE.NORMAL : SPEED_MODE.HD;
            default:
                return SPEED_MODE.HD;
        }
    }

    public void clickedQuickMenuItem3Right(View view) {
        AppLogger.d("clickedQuickMenuItem3Right");
        presenter.switchVideoResolution(move(speedMode, true));
    }

    public void onHideBadNetWorkBanner() {
        AppLogger.d("onHideBadNetWorkBanner");
        int childIndex = bannerSwitcher.getDisplayedChild();
        if (childIndex == 1) {
            bannerSwitcher.showPrevious();
        }
    }

    @Override
    public void onBellBatteryDrainOut() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.Tap1_LowPower)
                .setCancelable(false)
                .setPositiveButton(R.string.I_KNOW, null)
                .show();
    }

    @Override
    public void onDeviceBatteryChanged(Integer battery) {
        bannerChargeText.setVisibility(View.VISIBLE);
        bannerChargeIcon.setVisibility(View.VISIBLE);
        bannerChargeText.setText(battery == -1 ? getString(R.string.CHARGING) : battery + "%");
        if (battery == -1) {
            bannerChargeIcon.setImageResource(R.drawable.camera720_icon_electricity_charge);
        } else if (battery <= 20) {
            bannerChargeIcon.setImageResource(R.drawable.camera720_icon_electricity_low_power);
        } else if (battery > 20 && battery < 80) {
            bannerChargeIcon.setImageResource(R.drawable.camera720_icon_electricity_charge_half);
        } else if (battery >= 80) {
            bannerChargeIcon.setImageResource(R.drawable.camera720_icon_electricity_charge_full);
        }
    }

    public void showConfigConnectionDialog() {
        if (connectionDialog == null) {
            connectionDialog = ConnectionDialog.newInstance(uuid);
        }
        if (!connectionDialog.isVisible()) {
            connectionDialog.show(getSupportFragmentManager(), ConnectionDialog.class.getSimpleName());
        }
    }

    public void onSwitchSpeedMode(@SPEED_MODE int mode) {
        switch (this.speedMode = mode) {
            case SPEED_MODE.AUTO:
                menuBinding.actPanoramaCameraQuickMenuItem3Content.setText(R.string.Tap1_Camera_Video_Auto);
                break;
            case SPEED_MODE.FLUENCY:
            case SPEED_MODE.NORMAL:
                menuBinding.actPanoramaCameraQuickMenuItem3Content.setText(R.string.Tap1_Camera_Video_SD);
                menuBinding.actPanoramaCameraQuickMenuItem3Left.setVisibility(View.GONE);
                menuBinding.actPanoramaCameraQuickMenuItem3Right.setVisibility(View.VISIBLE);
                break;
            case SPEED_MODE.HD:
                menuBinding.actPanoramaCameraQuickMenuItem3Content.setText(R.string.Tap1_Camera_Video_HD);
                menuBinding.actPanoramaCameraQuickMenuItem3Left.setVisibility(View.VISIBLE);
                menuBinding.actPanoramaCameraQuickMenuItem3Right.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onRefreshViewModeUI(int viewMode, boolean enable) {
        AppLogger.d("onRefreshViewModeUI");
        panoramaViewMode = viewMode;
        panoramaRecordMode = PANORAMA_RECORD_MODE.MODE_NONE;
        bottomPanelAlbumItem.setVisibility(View.VISIBLE);
        bottomPanelMoreItem.setVisibility(View.VISIBLE);
        bottomCountDownLine.setVisibility(View.GONE);
        bottomPanelLoading.setVisibility(View.GONE);
        bottomPanelPhotoGraphItem.setImageResource(viewMode == PANORAMA_VIEW_MODE.MODE_PICTURE ? R.drawable.camera720_icon_photograph_selector : R.drawable.camera720_icon_short_video_selector);
        bottomPanelSwitcherItem1ViewMode.check(viewMode == PANORAMA_VIEW_MODE.MODE_PICTURE ? R.id.act_panorama_camera_bottom_panel_picture : R.id.act_panorama_camera_bottom_panel_video);
        bottomCountDownLine.setVisibility(View.GONE);
        bottomCountDownLine.setScaleX(1);
        if (bottomPanelSwitcher.getDisplayedChild() == 1) {
            bottomPanelSwitcher.showPrevious();
        }
        if (enable) dismissLoading();
        onRefreshControllerView(enable);
        popOption.dismiss();
    }

    public void onRefreshControllerView(boolean enable) {
        bottomPanelSwitcher.setEnabled((hasResolution && enable) || justForTest);
        bottomPanelPictureMode.setEnabled((hasResolution && enable) || justForTest);
        bottomPanelVideoMode.setEnabled((hasResolution && enable) || justForTest);
        if (!bottomPanelPhotoGraphItem.isPressed()) {//这里是为了让长按事件能收到 actionUp事件
            bottomPanelPhotoGraphItem.setEnabled((hasResolution && enable) || justForTest);
        }
        if (!bottomPanelMoreItem.isPressed()) {
            bottomPanelMoreItem.setEnabled((hasResolution && enable) || justForTest);
        }
        menuBinding.actPanoramaCameraQuickMenuItem2Voice.setEnabled((hasResolution && enable) || justForTest);
        menuBinding.actPanoramaCameraQuickMenuItem1Mic.setEnabled((hasResolution && enable) || justForTest);
        setting.setEnabled(!hasResolution || enable);
    }

    @Override
    public void onRefreshConnectionMode(int connectionType) {//-1:根据设备网络状态显示失败 banner:-2:强制显示 banner,根据设备和当前网络状态显示对应的文本
        onRefreshControllerView(false);
        Device device = sourceManager.getDevice(uuid);
        String mac = device.$(DpMsgMap.ID_202_MAC, "");
        DpMsgDefine.DPNet net = device.$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
        boolean apMode = JFGRules.isAPDirect(uuid, mac);
        boolean isOnline = net.net > 0;
        boolean online = sourceManager.isOnline();
        bannerConnectionIcon.setImageResource(apMode ? R.drawable.camera720_icon_ap : R.drawable.camera720_icon_wifi);
        bannerConnectionIcon.setVisibility((apMode || isOnline) ? View.VISIBLE : View.GONE);
        bannerConnectionText.setText(apMode ? R.string.Tap1_OutdoorMode : isOnline ? R.string.DEVICE_WIFI_ONLINE : R.string.NOT_ONLINE);
        bannerChargeText.setVisibility((apMode || isOnline) ? View.VISIBLE : View.INVISIBLE);
        bannerChargeIcon.setVisibility((apMode || isOnline) ? View.VISIBLE : View.INVISIBLE);
        if (!apMode && !isOnline) {
            loadingBar.setState(JConstant.PLAY_STATE_IDLE, null);
        }
        if (NetUtils.getNetType(this) == -1) {//真没网了
            if (bannerSwitcher.getDisplayedChild() == 0) {
                bannerSwitcher.showNext();
            }
            bannerWarmingTitle.setText(R.string.Tap1_DisconnectedPleaseCheck);
        } else if (!apMode && !isOnline) {//不是 ap 直连且当前设备是离线状态
            if (bannerSwitcher.getDisplayedChild() == 0) {
                bannerSwitcher.showNext();
            }
            bannerWarmingTitle.setText(R.string.Tap1_Offline);
        } else if (connectionType < 0) {
            if (bannerSwitcher.getDisplayedChild() == 0) {
                bannerSwitcher.showNext();
            }
            bannerWarmingTitle.setText(R.string.Tips_Device_TimeoutRetry);
        } else if (connectionType >= 0) {
            if (bannerSwitcher.getDisplayedChild() == 1) {
                bannerSwitcher.showPrevious();
            }
        }
        menuBinding.actPanoramaCameraQuickMenuItem1Mic.setEnabled(!apMode);
        menuBinding.actPanoramaCameraQuickMenuItem2Voice.setEnabled(!apMode);
        if (connectionType < 0) return;
        if (connectionType == 0) {//wifi
            AppLogger.d("正在使用 WiFi 网络,可以放心观看");
            if (mobileAlert != null && mobileAlert.isShowing()) {
                mobileAlert.dismiss();
            }
            if (!hasResolution) {
                presenter.startViewer();
            }
            ToastUtil.showPositiveToast(getString(R.string.Tap1_SwitchedWiFi));
        } else if (connectionType == 1) {//mobile
            AppLogger.d("正在使用移动网络,请注意流量");
            boolean alert = PreferencesUtils.getBoolean(JConstant.ALERT_MOBILE, true);
            if (alert) {
                presenter.cancelViewer();
                onRefreshControllerView(false);
                if (mobileAlert == null) {
                    mobileAlert = new AlertDialog.Builder(this)
                            .setMessage(R.string.Tap1_Firmware_DataTips)
                            .setPositiveButton(R.string.OK, (dialog, which) -> {
                                PreferencesUtils.putBoolean(JConstant.ALERT_MOBILE, true);
                                loadingBar.setState(JConstant.PLAY_STATE_PREPARE, null);
                                presenter.startViewer();
                            })
                            .setNegativeButton(R.string.Dont_Show_Again, (dialog, which) -> {
                                loadingBar.setState(JConstant.PLAY_STATE_LOADING_FAILED, null);
                                PreferencesUtils.putBoolean(JConstant.ALERT_MOBILE, false);
                            })
                            .create();
                }
                mobileAlert.show();
            } else {
                loadingBar.setState(JConstant.PLAY_STATE_PREPARE, null);
                presenter.startViewer();
                ToastUtil.showPositiveToast(getString(R.string.Tap1_SwitchedNetwork));
            }
        }
    }

    @Override
    public void onRefreshVideoRecordUI(int second, @PANORAMA_RECORD_MODE int type) {
        panoramaRecordMode = type;
        if (!hasResolution) return;
        if (bottomPanelSwitcher.getDisplayedChild() == 0) {
            bottomPanelSwitcher.showNext();
        }
        if (bottomPanelAlbumItem.getVisibility() == View.VISIBLE) {
            bottomPanelAlbumItem.setVisibility(View.GONE);
        }
        if (bottomPanelMoreItem.getVisibility() == View.VISIBLE) {
            bottomPanelMoreItem.setVisibility(View.GONE);
        }
        if (panoramaRecordMode != PANORAMA_RECORD_MODE.MODE_SHORT && !bottomPanelPhotoGraphItem.isEnabled()) {
            bottomPanelPhotoGraphItem.setEnabled(true);
        }
        if (bottomPanelLoading.getVisibility() != View.GONE) {
            bottomPanelLoading.setVisibility(View.GONE);
        }
        switch (type) {
            case PANORAMA_RECORD_MODE.MODE_LONG:
                bottomPanelSwitcherItem2TimeText.setText(TimeUtils.getHHMMSS(second * 1000L));
                int visibility = bottomPanelSwitcherItem2DotIndicator.getVisibility();
                bottomPanelSwitcherItem2DotIndicator.setVisibility(visibility == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                break;
            case PANORAMA_RECORD_MODE.MODE_SHORT:
                if (bottomCountDownLine.getVisibility() != View.VISIBLE) {
                    bottomCountDownLine.setVisibility(View.VISIBLE);
                }
                if (bottomPanelSwitcherItem2DotIndicator.getVisibility() == View.VISIBLE) {
                    bottomPanelSwitcherItem2DotIndicator.setVisibility(View.INVISIBLE);
                }
                int countDown = 8 - second;
                bottomPanelSwitcherItem2TimeText.setText(countDown + "S");
                bottomCountDownLine.animate().scaleX(countDown * 1.0F / 8).start();
                break;
            default:
                break;
        }
    }

    @Override
    public void onReportDeviceError(int err, boolean useAlert) {
        onRefreshViewModeUI(panoramaViewMode, true);

        switch (err) {
            case 150://低电量
                AppLogger.d("设备电量过低");
                ToastUtil.showNegativeToast(getString(R.string.DOOR_LOWBETTERY));
                break;
            case 2003://sd 卡没有容量
                AppLogger.d("SD 卡内存已满");
                if (useAlert) {
                    new AlertDialog.Builder(this).setCancelable(false).
                            setMessage(R.string.Tap1_SDCardFullyTips)
                            .setCancelable(false)
                            .setPositiveButton(R.string.OK, null)
                            .show();
                } else {
                    ToastUtil.showNegativeToast(getString(R.string.Tap1_SDCardFullyTips));
                }
                break;
            case 2004://没有 sd 卡
                AppLogger.d("未检测到 SD 卡");
                if (useAlert) {
                    new AlertDialog.Builder(this).setCancelable(false).
                            setMessage(R.string.MSG_SD_OFF)
                            .setCancelable(false)
                            .setPositiveButton(R.string.OK, null)
                            .show();
                } else {
                    ToastUtil.showNegativeToast(getString(R.string.NO_SDCARD));
                }
                break;
            case 2007://正在录像
                break;
            case 2008://sd 卡正在格式化
                break;
            case 2022://sd卡识别失败，需要格式化
                break;
            //小于
            case -1:
                break;
            case -2:
                ToastUtil.showNegativeToast(getString(R.string.Tap1_LessThan3sTips));
                break;
            case ERROR_CODE_HTTP_NOT_AVAILABLE: {
                boolean pop = PreferencesUtils.getBoolean(JConstant.SWITCH_MODE_POP, true);
                if (!pop) return;
                //松开弹
                if (useAlert) {
                    new AlertDialog.Builder(PanoramaCameraActivity.this)
                            .setMessage(R.string.Switch_Mode_Pop)
                            .setCancelable(false)
                            .setNegativeButton(R.string.WELL_OK, null)
                            .setPositiveButton(R.string.Dont_Show_Again, (dialog, which) -> {
                                PreferencesUtils.putBoolean(JConstant.SWITCH_MODE_POP, false);
                            })
                            .show();
                } else {
                    ToastUtil.showNegativeToast(getString(R.string.Switch_Mode_Pop));
                }
            }
        }

    }

    @Override
    public void onNewFirmwareRsp() {
        if (isFinishing()) return;
        AlertDialogManager.getInstance().showDialog(this,
                getString(R.string.Tap1_Device_UpgradeTips), getString(R.string.Tap1_Device_UpgradeTips),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    Intent intent = new Intent(this, FirmwareUpdateActivity.class);
                    intent.putExtra(JConstant.KEY_COMPONENT_NAME, this.getClass().getName());
                    intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                    startActivity(intent);
                }, getString(R.string.CANCEL), null, false);
    }
}
