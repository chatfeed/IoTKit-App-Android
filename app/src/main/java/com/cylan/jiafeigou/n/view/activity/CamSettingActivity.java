package com.cylan.jiafeigou.n.view.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.ApFilter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.UdpDevice;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.misc.pty.IProperty;
import com.cylan.jiafeigou.misc.pty.PropertiesLoader;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamSettingPresenterImpl;
import com.cylan.jiafeigou.n.view.cam.DeviceInfoDetailFragment;
import com.cylan.jiafeigou.n.view.cam.DoorPassWordSettingFragment;
import com.cylan.jiafeigou.n.view.cam.SafeProtectionFragment;
import com.cylan.jiafeigou.n.view.cam.SdcardDetailActivity;
import com.cylan.jiafeigou.n.view.cam.VideoAutoRecordFragment;
import com.cylan.jiafeigou.n.view.record.DelayRecordActivity;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.SettingItemView1;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_209_LED_INDICATOR;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_501_CAMERA_ALARM_FLAG;
import static com.cylan.jiafeigou.misc.JConstant.JUST_SEND_INFO;
import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;
import static com.cylan.jiafeigou.utils.ActivityUtils.loadFragment;

@Badge(parentTag = "CameraLiveActivity")
public class CamSettingActivity extends BaseFullScreenFragmentActivity<CamSettingContract.Presenter>
        implements CamSettingContract.View/*, DataObserver<List<PropertyItem>>*/ {

    private static final int REQ_DELAY_RECORD = 122;
    @BindView(R.id.sv_setting_device_detail)
    SettingItemView0 svSettingDeviceDetail;
    @BindView(R.id.sv_setting_device_wifi)
    SettingItemView0 svSettingDeviceWifi;
    @BindView(R.id.sv_setting_device_mobile_network)
    SettingItemView1 svSettingDeviceMobileNetwork;
    @BindView(R.id.sv_setting_safe_protection)
    SettingItemView0 svSettingSafeProtection;
    @BindView(R.id.sv_setting_device_auto_record)
    SettingItemView0 svSettingDeviceAutoRecord;
    @BindView(R.id.sv_setting_device_delay_capture)
    SettingItemView0 svSettingDeviceDelayCapture;
    @BindView(R.id.sv_setting_device_standby_mode)
    SettingItemView1 svSettingDeviceStandbyMode;
    @BindView(R.id.sv_setting_device_indicator)
    SettingItemView1 svSettingDeviceLedIndicator;
    @BindView(R.id.sv_setting_device_rotatable)
    SettingItemView1 svSettingDeviceRotate;
    @BindView(R.id.tv_setting_unbind)
    TextView tvSettingUnbind;
    @BindView(R.id.lLayout_setting_item_container)
    LinearLayout lLayoutSettingItemContainer;
    @BindView(R.id.sbtn_setting_110v)
    SettingItemView0 sbtnSetting110v;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.sbtn_setting_sight)
    SettingItemView0 sbtnSettingSight;
    @BindView(R.id.sv_setting_device_wired_mode)
    SettingItemView0 svSettingDeviceWiredMode;
    @BindView(R.id.sv_setting_device_soft_ap)
    SettingItemView0 svSettingDeviceSoftAp;
    @BindView(R.id.sv_setting_device_sd_card)
    SettingItemView0 svSettingDeviceSDCard;
    @BindView(R.id.sv_setting_device_clear_record)
    SettingItemView0 svSettingDeviceClearRecord;
    @BindView(R.id.sbtn_setting_pir)
    SettingItemView0 svSettingDevicePIR;
    @BindView(R.id.sv_setting_device_home_mode)
    SettingItemView0 svSettingDeviceHomeMode;
    @BindView(R.id.sv_setting_direct_mode)
    SettingItemView0 svSettingDeviceDirectMode;
    @BindView(R.id.network_setting_title)
    TextView tvNetWorkSettingTitle;
    @BindView(R.id.sv_setting_device_logo)
    SettingItemView0 svSettingDeviceLogo;
    @BindView(R.id.siv_target_leveledBFS)
    SettingItemView0 svTargetLevelBFS;
    @BindView(R.id.sv_setting_device_ap)
    SettingItemView0 svSettingDeviceAp;
    @BindView(R.id.siv_setting_device_door_look)
    SettingItemView0 sivDeviceDoorLock;
    @BindView(R.id.sv_setting_device_mobile_mode)
    SettingItemView0 sivDeviceMobileNetMode;
    private SimpleDialogFragment mClearRecordFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_setting);
        ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        if (TextUtils.isEmpty(uuid)) {
            AppLogger.e("uuid is null");
            finishExt();
            return;
        }
        presenter = new CamSettingPresenterImpl(this, uuid);
        if (getIntent().getBooleanExtra(JConstant.KEY_JUMP_TO_CAM_DETAIL, false)) {
            jumpDetail(false);
        }
        initProductLayout(DataSourceManager.getInstance().getDevice(this.uuid));
        deviceUpdate(DataSourceManager.getInstance().getDevice(this.uuid));
        AppLogger.d("检查升级包");

        Device device = DataSourceManager.getInstance().getDevice(uuid);
        boolean http = JFGRules.isPan720(device.pid);
        if (http) {
            BaseDeviceInformationFetcher.getInstance().init(uuid);
        }
    }

    private void initProductLayout(Device device) {
        IProperty productProperty = PropertiesLoader.getInstance();
        svSettingDeviceClearRecord.setVisibility(productProperty.isSerial("BELL", device.pid) ? View.VISIBLE : View.INVISIBLE);
        svSettingDeviceWifi.setVisibility(productProperty.hasProperty(device.pid, "WIFI") ? View.VISIBLE : View.GONE);
        svSettingSafeProtection.setVisibility(productProperty.hasProperty(device.pid, "PROTECTION") ? View.VISIBLE : View.GONE);
        svSettingDeviceAutoRecord.setVisibility(productProperty.hasProperty(device.pid, "AUTORECORD") ? View.VISIBLE : View.GONE);
        svSettingDevicePIR.setVisibility(productProperty.hasProperty(device.pid, "INFRAREDVISION") ? View.VISIBLE : View.GONE);
        svSettingDeviceSDCard.setVisibility(productProperty.hasProperty(device.pid, "SD") ? View.VISIBLE : View.GONE);
        sbtnSetting110v.setVisibility(productProperty.hasProperty(device.pid, "NTSC") ? View.VISIBLE : View.GONE);
        svSettingDeviceRotate.setVisibility(productProperty.hasProperty(device.pid, "HANGUP") ? View.VISIBLE : View.GONE);
        svSettingDeviceStandbyMode.setVisibility(productProperty.hasProperty(device.pid, "STANDBY") ? View.VISIBLE : View.GONE);
        svSettingDeviceLedIndicator.setVisibility(productProperty.hasProperty(device.pid, "LED") ? View.VISIBLE : View.GONE);
//        svSettingDeviceMobileNetwork.setVisibility(productProperty.hasProperty(device.pid, "") ? View.VISIBLE : View.GONE);
        svSettingDeviceSoftAp.setVisibility(productProperty.hasProperty(device.pid, "AP") ? View.VISIBLE : View.GONE);
        svSettingDeviceWiredMode.setVisibility(productProperty.hasProperty(device.pid, "WIREDMODE") ? View.VISIBLE : View.GONE);
        sbtnSettingSight.setVisibility(productProperty.hasProperty(device.pid, "VIEWANGLE") ? View.VISIBLE : View.GONE);
        sivDeviceDoorLock.setVisibility(productProperty.hasProperty(device.pid, "DOOR_LOCK") ? View.VISIBLE : View.GONE);
        // TODO: 2018/3/25
        sivDeviceMobileNetMode.setVisibility(device.pid == 16 ? View.VISIBLE : View.GONE);
        //康凯斯门铃测试项
        svTargetLevelBFS.setVisibility(device.getPid() == 1343 || device.getPid() == 42 ? View.VISIBLE : View.GONE);

        svSettingDeviceAp.setVisibility(JFGRules.isPan720(device.getPid()) ? View.VISIBLE : View.GONE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        initBackListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHotSpotConnection();
    }

    private void updateHotSpotConnection() {
        //720设备
        final Device device = presenter.getDevice();
        if (!JFGRules.isPan720(device.pid)) {
            return;
        }
        //1.系统热点开启，2.读取硬件设备描述文件，过滤。
        boolean isWifiApEnabled = NetUtils.isWifiApEnabled();
        if (!isWifiApEnabled) {
            return;
        }
        Subscription ssu = Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    ArrayList<NetUtils.ClientScanResult> list = NetUtils.getClientList(true, 1000);
                    if (ListUtils.isEmpty(list)) {
                        return null;
                    }
                    //设备的mac
                    final String mac = device.$(202, "");
                    for (NetUtils.ClientScanResult ret : list) {
                        if (TextUtils.equals(mac.toLowerCase(), ret.getHWAddr().toLowerCase())) {
                            return ret;
                        }
                    }
                    return null;
                })
                .filter(ret -> ret != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    WifiConfiguration netConfig = NetUtils.getWifiApConfiguration();
                    if (netConfig == null) {
                        return;
                    }
                    AppLogger.d("netConfig:" + new Gson().toJson(netConfig));
                    svSettingDeviceAp.setSubTitle(netConfig.SSID);
                }, AppLogger::e);
        presenter.addSub(ssu, "ssu");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initBackListener() {
        customToolbar.post(() -> customToolbar.setBackAction((View v) -> v.postDelayed(this::finishExt, 200)));
    }

    private void jumpDetail(boolean animation) {

        DeviceInfoDetailFragment fragment = DeviceInfoDetailFragment.newInstance(null);
        fragment.setCallBack((Object t) -> {
            deviceUpdate(DataSourceManager.getInstance().getDevice(uuid));
        });
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(bundle);
        if (animation) {
            loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment, fragment.getClass().getSimpleName())
                    .commit();
        }
    }

    @OnClick({R.id.sv_setting_device_detail,
            R.id.sv_setting_device_auto_record,
            R.id.sv_setting_safe_protection,
            R.id.tv_setting_unbind,
            R.id.sv_setting_device_delay_capture,
            R.id.sv_setting_device_wifi,
            R.id.sbtn_setting_sight,
            R.id.sv_setting_device_sd_card,
            R.id.sv_setting_device_clear_record,
            R.id.sv_setting_device_home_mode,
            R.id.sv_setting_direct_mode,
            R.id.siv_target_leveledBFS,
            R.id.siv_setting_device_door_look
    })
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        switch (view.getId()) {
            case R.id.sv_setting_device_detail: {
                jumpDetail(true);
            }
            break;
            case R.id.tv_setting_unbind: {
                if (NetUtils.getJfgNetType(getContext()) == 0) {
                    ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                Device device = DataSourceManager.getInstance().getDevice(uuid);
                AlertDialogManager.getInstance().showDialog(this, getString(R.string.SURE_DELETE_1, JFGRules.getDeviceAlias(device)),
                        getString(R.string.SURE_DELETE_1, JFGRules.getDeviceAlias(device)),
                        getString(R.string.OK), (DialogInterface dialogInterface, int i) -> {
                            hasPendingUnbindAction = true;
                            presenter.unbindDevice();
                            LoadingDialog.showLoading(this, getString(R.string.DELETEING), true);
                        }, getString(R.string.CANCEL), null);
            }
            break;
            case R.id.sv_setting_device_auto_record: {
                Device device = DataSourceManager.getInstance().getDevice(uuid);

//                initVideoAutoRecordFragment();
                Bundle bundle = new Bundle();
                bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
                VideoAutoRecordFragment fragment = VideoAutoRecordFragment.newInstance(bundle);
                fragment.setArguments(bundle);
                loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
                fragment.setCallBack((Object t) -> {
                    deviceUpdate(DataSourceManager.getInstance().getDevice(uuid));
                });

            }
            break;
            case R.id.sv_setting_safe_protection: {
                Bundle bundle = new Bundle();
                bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
                SafeProtectionFragment fragment = SafeProtectionFragment.newInstance(bundle);
                fragment.setArguments(bundle);
                loadFragment(android.R.id.content, getSupportFragmentManager(), fragment);
                fragment.setCallBack((Object t) -> {
                    deviceUpdate(DataSourceManager.getInstance().getDevice(uuid));
                });
            }
            break;
            case R.id.sv_setting_device_delay_capture: {
                Intent intent = new Intent(this, DelayRecordActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                intent.putExtra(JConstant.VIEW_CALL_WAY, DelayRecordContract.View.VIEW_LAUNCH_WAY_SETTING);
                startActivity(intent,
                        ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),
                                R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
            }
            break;
            case R.id.sv_setting_device_wifi:
                handleJumpToConfig(true);
                break;
            case R.id.sbtn_setting_sight:
                Intent intent = new Intent(this, SightSettingActivity.class);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                startActivity(intent);
                break;

            //bell
            case R.id.sv_setting_device_sd_card:
                Device device = DataSourceManager.getInstance().getDevice(uuid);
                DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());
                String statusContent = getSdcardState(status.hasSdcard, status.err);
                if (!TextUtils.isEmpty(statusContent) && statusContent.contains("(")) {
                    showClearSDDialog();
                    return;
                }

                if (status.hasSdcard)//没有sd卡,不能点击
                {
                    jump2SdcardDetailFragment();
                }
                break;
            case R.id.sv_setting_device_clear_record:
                ViewUtils.deBounceClick(view);
                int cnet = NetUtils.getJfgNetType(this);
                if (cnet == 0) {
                    ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                if (mClearRecordFragment == null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.Tap1_Tipsforclearrecents));
                    bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.CANCEL));
                    bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.DELETE));
                    mClearRecordFragment = SimpleDialogFragment.newInstance(bundle);
                }
                mClearRecordFragment.setAction((id, value) -> {
                    switch (id) {
                        case R.id.tv_dialog_btn_right:
                            presenter.clearBellRecord(uuid);
                            LoadingDialog.showLoading(this, getString(R.string.DELETEING), true);
                    }
                });
                mClearRecordFragment.show(getSupportFragmentManager(), "ClearBellRecordFragment");
                break;

            case R.id.sv_setting_device_home_mode:
                handleJumpToConfig(false);
                break;
            case R.id.sv_setting_direct_mode:
                intent = new Intent();
                intent.putExtra(JConstant.KEY_ANIM_TITLE, getString(R.string.Tap1_AddDevice_CameraTipShort));
                intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, getString(R.string.Tap1_AddDevice_CameraTips));
                intent.putExtra(JConstant.KEY_NEXT_STEP, getString(R.string.BLINKING));
                intent.putExtra(JConstant.KEY_SSID_PREFIX, BindUtils.DOG_AP);
                intent.putExtra("PanoramaConfigure", "OutDoor");
                intent.putExtra(JConstant.JUST_SEND_INFO, uuid);
                intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                intent.setClass(this, BindPanoramaCamActivity.class);
                startActivity(intent);
                break;
            case R.id.siv_target_leveledBFS:
                setBFS();
                break;
            case R.id.siv_setting_device_door_look: {
                DoorPassWordSettingFragment fragment = DoorPassWordSettingFragment.Companion.newInstance(uuid);
                ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(), fragment, android.R.id.content);
            }
            break;
        }
    }

    private void setBFS() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText etMic = new EditText(this);
        etMic.setInputType(InputType.TYPE_CLASS_NUMBER);
        etMic.setId("mic".hashCode());
        etMic.setHint("mic(0,31)");
        etMic.setHintTextColor(getResources().getColor(R.color.color_9e9e9e));

        EditText etSpeaker = new EditText(this);
        etSpeaker.setId("speaker".hashCode());
        etSpeaker.setInputType(InputType.TYPE_CLASS_NUMBER);
        etSpeaker.setHint("speaker(0,10)");
        etSpeaker.setHintTextColor(getResources().getColor(R.color.color_9e9e9e));
        layout.addView(etMic);
        layout.addView(etSpeaker);
//        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle("康凯斯门铃 BFS 设置")
                .setView(layout)
                .setPositiveButton("确认", (dialog, which) -> Schedulers.io().createWorker().schedule(() -> {
                    EditText etM = (EditText) ((AlertDialog) dialog).findViewById("mic".hashCode());
                    int mic = 8;
                    if (etM != null) {
                        final String micStr = etM.getText().toString();
                        if (!TextUtils.isEmpty(micStr) && TextUtils.isDigitsOnly(micStr)) {
                            try {
                                mic = Integer.parseInt(micStr);
                            } catch (Exception e) {
                                runOnUiThread(() -> ToastUtil.showToast("设置失败"));
                            }
                        }
                    }
                    mic = Math.max(mic, 0);
                    mic = Math.min(mic, 31);

                    EditText etS = (EditText) ((AlertDialog) dialog).findViewById("speaker".hashCode());
                    int speaker = 8;
                    if (etS != null) {
                        final String sStr = etS.getText().toString();
                        if (!TextUtils.isEmpty(sStr) && TextUtils.isDigitsOnly(sStr)) {
                            try {
                                speaker = Integer.parseInt(sStr);
                            } catch (Exception e) {
                                runOnUiThread(() -> ToastUtil.showToast("设置失败"));
                            }
                        }
                    }
                    speaker = Math.max(speaker, 0);
                    speaker = Math.min(speaker, 10);
                    try {
                        int i = Command.getInstance().setTargetLeveledBFS(mic, speaker);
                        AppLogger.d("正在设置 TargetLevel:" + mic + ",result:" + i);
                        if (i == 0) {
                            runOnUiThread(() -> ToastUtil.showToast("设置成功"));
                        } else {
                            runOnUiThread(() -> ToastUtil.showToast("设置失败"));
                        }
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }))
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void attributeUpdate() {
        Device mDevice = DataSourceManager.getInstance().getDevice(uuid);
        boolean isAp = JFGRules.isAPDirect(mDevice.uuid, mDevice.$(202, ""));
        if (isAp) {
            svSettingDeviceHomeMode.setSubTitle(getString(R.string.Tap1_Setting_Unopened));
            svSettingDeviceDirectMode.setSubTitle(getString(R.string.Tap1_OutdoorMode_Opened));
        } else {
            svSettingDeviceDirectMode.setSubTitle(getString(R.string.Tap1_Setting_Unopened));
            DpMsgDefine.DPNet net = mDevice.$(201, new DpMsgDefine.DPNet());
            if (JFGRules.isDeviceOnline(net)) {
                svSettingDeviceHomeMode.setSubTitle(net.ssid);
            } else {
                svSettingDeviceHomeMode.setSubTitle(getString(R.string.Tap1_Setting_Unopened));
            }
        }
    }

    /**
     * 显示Sd卡的详情
     */
    private void jump2SdcardDetailFragment() {
        Intent intent = new Intent(this, SdcardDetailActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        startActivity(intent);
    }

    /**
     * 格式化SD卡
     */
    private void showClearSDDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.SD_INIT));
        bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
        bundle.putString(SimpleDialogFragment.KEY_CONTENT_CONTENT, getString(R.string.VIDEO_SD_DESC));
        SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.newInstance(bundle);
        simpleDialogFragment.setAction((int id, Object value) -> {
            //开始格式化
            if (id == R.id.tv_dialog_btn_left) {
                presenter.clearSdcard();
                showLoading();
            }
        });
        simpleDialogFragment.show(getSupportFragmentManager(), "simpleDialogFragment");
    }

    @Override
    public void showLoading() {
        LoadingDialog.showLoading(this, getString(R.string.SD_INFO_2), true);
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading();
    }

    private String getSdcardState(boolean hasSdcard, int err) {
        //sd卡状态
        if (hasSdcard && err != 0) {
            //sd初始化失败时候显示
            return getString(R.string.SD_INIT_ERR, err);
        }
        if (!hasSdcard) {
            return getString(R.string.SD_NO);
        }
        return getString(R.string.SD_NORMAL);
    }

    private void handleJumpToConfig(boolean justSend) {
        handleJumpToConfig(justSend, false);
    }

    /**
     * @param justSend
     * @param justConnectAp 720设备，配置手机热点时，需要停止下一步
     */
    private void handleJumpToConfig(boolean justSend, boolean justConnectAp) {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        if (device == null) {
            finishExt();
            return;
        }
        AppLogger.d("deviceNet:" + device.$(201, new DpMsgDefine.DPNet()));
        AppLogger.d("clientNet:" + NetUtils.getNetName(ContextUtils.getContext()));
        if (JFGRules.isFreeCam(device.pid)) {
            //freeCam直接进入
            Intent intent = new Intent(this, BindCamActivity.class);
            intent.putExtra(JConstant.JUST_SEND_INFO, uuid);
            intent.putExtra(JConstant.KEY_SSID_PREFIX, BindUtils.DOG_AP);
            intent.putExtra(JConstant.KEY_ANIM_TITLE, getString(R.string.Tap1_AddDevice_CameraTipsTitle));
            intent.putExtra(JConstant.KEY_ANIM_SUB_TITLE, getString(R.string.Tap1_AddDevice_CameraTips));
            intent.putExtra(JConstant.KEY_NEXT_STEP, getString(R.string.BLINKING));
            intent.putExtra("just_config", true);
            intent.putExtra(JConstant.KEY_BIND_BACK_ACTIVITY, getClass().getName());
            startActivity(intent);
        } else if (JFGRules.isCatEeyBell(device.pid)) {//猫眼门铃特殊处理
            Intent intent = BindUtils.getIntentByPid(device.pid, getContext());
            intent.setClass(getContext(), BindAnimationActivity.class);
            if (justSend) {
                intent.putExtra(JUST_SEND_INFO, uuid);
            }
//            intent.putExtra("just_config", true);
            intent.putExtra(JConstant.KEY_BIND_BACK_ACTIVITY, getClass().getName());
            startActivity(intent);

        } else if (JFGRules.isBell(device.pid)) {
            Intent intent = new Intent(this, BindBellActivity.class);
            intent.putExtra(JConstant.KEY_SSID_PREFIX, BindUtils.BELL_AP);
            if (justSend) {
                intent.putExtra(JUST_SEND_INFO, uuid);
            }
//            intent.putExtra("just_config", true);
            intent.putExtra(JConstant.KEY_BIND_BACK_ACTIVITY, getClass().getName());
            startActivity(intent);
        } else {
            DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
            if (!JFGRules.isDeviceOnline(net) /*||/*WiFi 关闭时也要从新进入流程页* NetUtils.isWiFiConnected(this)*/) {
                //设备离线
                Intent intent = BindUtils.getIntentByPid(device.pid, getContext());
                intent.putExtra("PanoramaConfigure", "Family");
                if (justSend) {
                    intent.putExtra(JConstant.JUST_SEND_INFO, uuid);
                }
                if (JFGRules.isPan720(device.pid)) {
                    intent.putExtra("just_config", true);
                    intent.putExtra(JConstant.KEY_720_CONFIG_HOT_SPOT, justConnectAp);
                }
                intent.putExtra(JConstant.KEY_BIND_BACK_ACTIVITY, getClass().getName());
                startActivity(intent);
            } else {
                //设备在线
                String localSSid = NetUtils.getNetName(ContextUtils.getContext());
                String remoteSSid = net.ssid;
                boolean cylanDevice = JFGRules.isCylanDevice(localSSid);
                String shortCid = BindUtils.filterCylanDeviceShortCid(localSSid);
                if (!TextUtils.equals(localSSid, remoteSSid) && !cylanDevice && !device.uuid.endsWith(shortCid) && net.net == 1) {
                    AlertDialogManager.getInstance().showDialog(this, getString(R.string.setwifi_check, remoteSSid),
                            getString(R.string.setwifi_check, remoteSSid),
                            getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            }, getString(R.string.CANCEL), null);
                } else if (JFGRules.isPan720(device.pid)) {
                    //相同ssid,判断为同一个网络环境.太水了.
                    Intent intent = BindUtils.getIntentByPid(device.pid, getContext());
                    intent.setClass(this, ConfigWifiActivity.class);
                    intent.putExtra("just_config", true);
                    intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                    intent.putExtra("PanoramaConfigure", "Family");
                    intent.putExtra(JConstant.KEY_BIND_BACK_ACTIVITY, getClass().getName());
                    startActivity(intent);
                } else {
                    //相同ssid,判断为同一个网络环境.太水了.
                    Intent intent = new Intent(this, ConfigWifiActivity_2.class);
                    intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                    intent.putExtra("just_config", true);
                    intent.putExtra(JConstant.KEY_BIND_BACK_ACTIVITY, getClass().getName());
                    startActivity(intent);
                }
            }
        }
    }

    /**
     * 开启待机模式的时候,其余所有选项都不能点击.
     * 递归调用
     *
     * @param viewGroup
     * @param enable
     */
    private void switchBtn(ViewGroup viewGroup, boolean enable) {
        final int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view.getId() == R.id.tv_setting_unbind) {
                continue;//解绑按钮
            }
            if (view.getId() == R.id.sv_setting_device_standby_mode) {
                continue;//解绑按钮
            }
            view.setAlpha(enable ? 1.f : 0.6f);
            view.setEnabled(enable);
        }
    }

    private void initFirmwareHint(Device device) {
        try {
            if (JFGRules.isPanoramaCamera(device.pid)) {
                return;
            }
            if (JFGRules.isShareDevice(device)) {
                return;
            }
            String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + uuid());
            svSettingDeviceDetail.showRedHint(!TextUtils.isEmpty(content));
        } catch (Exception e) {
        }
    }

    @Override
    public void deviceUpdate(Device device) {
        initFirmwareHint(device);
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        //////////////////////////分享账号////////////////////////////////////////////
        if (!TextUtils.isEmpty(device.shareAccount) && !JFGRules.isPan720(device.pid)) {
            //分享账号 隐藏
            final int count = lLayoutSettingItemContainer.getChildCount();
            for (int i = 2; i < count - 1; i++) {
                View v = lLayoutSettingItemContainer.getChildAt(i);
                if (v != null) {
                    v.setVisibility(View.GONE);
                }
            }
            return;
        }
        boolean apNet = ApFilter.isApNet();
        boolean is720 = JFGRules.isPan720(device.pid);
        ////////////////////////////////////////////////////////////////////////
        DpMsgDefine.DPSdStatus sdStatus = device.$(DpMsgMap.ID_204_SDCARD_STORAGE, new DpMsgDefine.DPSdStatus());
        if (sdStatus == null) {
            sdStatus = new DpMsgDefine.DPSdStatus();
        }
        String detailInfo = presenter.getDetailsSubTitle(getContext(), sdStatus.hasSdcard, sdStatus.err);
//        if (!TextUtils.isEmpty(detailInfo) && detailInfo.contains("(")) {
//            svSettingDeviceDetail.setSubTitle(detailInfo, android.R.color.holo_red_dark);
//        } else
        svSettingDeviceDetail.setSubTitle(detailInfo, R.color.color_8C8C8C);
        ////////////////////////standby////////////////////////////////////////////
        DpMsgDefine.DPStandby dpStandby = device.$(DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, new DpMsgDefine.DPStandby());
        if (!JFGRules.showStandbyItem(device.pid, false)) {
            svSettingDeviceStandbyMode.setVisibility(View.GONE);
        } else {
            svSettingDeviceStandbyMode.setChecked(dpStandby.standby);
            if (!JFGRules.isDeviceOnline(net)) {
                enableStandby(false);//离线
            } else {
                enableStandby(true);
            }
            if (JFGRules.isRS(device.pid)) {
                svSettingDeviceStandbyMode.setVisibility(View.GONE);
            }
            svSettingDeviceStandbyMode.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                Device device1 = DataSourceManager.getInstance().getDevice(uuid);
                DpMsgDefine.DPNet dpNet = device1.$(201, new DpMsgDefine.DPNet());
                if (dpNet.net == -1 && isChecked) {
                    buttonView.setChecked(false);
                    return;
                }
                switchBtn(lLayoutSettingItemContainer, !isChecked);
                DpMsgDefine.DPStandby standby = DataSourceManager.getInstance().getDevice(uuid).$(508, new DpMsgDefine.DPStandby());
                standby.standby = isChecked;
                standby.version = System.currentTimeMillis();
                triggerStandby(standby);
                standby.led = isChecked ? ledPreState() : standby.led;//开启待机,保存之前状态到standby.关闭待机,从standby中恢复.
                standby.autoRecord = isChecked ? autoRecordPreState() : standby.autoRecord;
                standby.alarmEnable = isChecked ? alarmPreState() : standby.alarmEnable;
                standby.msgId = 508;
                List<DataPoint> list = new ArrayList<>();
                list.add(standby);
                list.add(new DpMsgDefine.DPPrimary<>(!isChecked && standby.led, ID_209_LED_INDICATOR));
                list.add(new DpMsgDefine.DPPrimary<>(isChecked ? 0 : standby.autoRecord, ID_303_DEVICE_AUTO_VIDEO_RECORD));
                list.add(new DpMsgDefine.DPPrimary<>(!isChecked && standby.alarmEnable, ID_501_CAMERA_ALARM_FLAG));
                presenter.updateInfoReq(list);
                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
            });
        }
        /////////////////////////////led/////////////////////////////////////
        if (JFGRules.showLedIndicator(device.pid, false)) {
            if (!dpStandby.standby) {
                boolean ledTrigger = device.$(209, false);
                svSettingDeviceLedIndicator.setChecked(ledTrigger);
            } else {
                svSettingDeviceLedIndicator.setChecked(false);
            }
            svSettingDeviceLedIndicator.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPStandby standby = DataSourceManager.getInstance().getDevice(uuid).$(508, new DpMsgDefine.DPStandby());
                if (standby != null && standby.standby) {
                    return;//开启待机模式引起的
                }
                DpMsgDefine.DPPrimary<Boolean> check = new DpMsgDefine.DPPrimary<>();
                check.value = isChecked;
                presenter.updateInfoReq(check, ID_209_LED_INDICATOR);
                Log.d("check", "led changed: " + isChecked);
                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
            });
        } else {
            svSettingDeviceLedIndicator.setVisibility(View.GONE);
        }


        ////////////////////////////net////////////////////////////////////////

        boolean isMobileNet = net.net > 1;
        svSettingDeviceWifi.setSubTitle(!TextUtils.isEmpty(net.ssid) ? (isMobileNet ? getString(R.string.OFF) : net.ssid) : getString(R.string.OFF_LINE));


        tvNetWorkSettingTitle.setVisibility(View.VISIBLE);
        //是否有sim卡
        int simCard = device.$(DpMsgMap.ID_223_MOBILE_NET, 0);
        svSettingDeviceMobileNetwork.setVisibility(JFGRules.isDeviceOnline(net) && JFGRules.showMobileNet(device.pid, false) && simCard > 1 ? View.VISIBLE : View.GONE);
        svSettingDeviceMobileNetwork.setEnabled(!dpStandby.standby);
        //是否显示移动网络
        boolean hasSimCard = device.$(DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY, false);
        sivDeviceMobileNetMode.setSubTitle(getMobileNet(hasSimCard, net));
        svSettingDeviceWifi.showDivider(simCard > 1);
        if (JFGRules.is3GCam(device.pid)) {
            boolean s = device.$(DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY, false);
            svSettingDeviceMobileNetwork.setChecked(s);
            svSettingDeviceMobileNetwork.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPPrimary<Boolean> check = new DpMsgDefine.DPPrimary<>();
                check.value = isChecked;
                presenter.updateInfoReq(check, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY);
                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
            });
        }
        /////////////////////////////////110v//////////////////////////////////
        if (JFGRules.showNTSCVLayout(device.pid, false)) {
            boolean state = device.$(216, false);
            sbtnSetting110v.setChecked(state);
            sbtnSetting110v.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPPrimary<Boolean> check = new DpMsgDefine.DPPrimary<>();
                check.value = isChecked;
                presenter.updateInfoReq(check, DpMsgMap.ID_216_DEVICE_VOLTAGE);
                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
            });
        } else {
            sbtnSetting110v.setVisibility(View.GONE);
        }

        /////////////////////////旋转/////////////////////////////////////////
        if (!JFGRules.showRotate(device.pid, false)) {
            svSettingDeviceRotate.setVisibility(View.GONE);
        } else {
            int state = device.$(DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE, 0);
            svSettingDeviceRotate.setChecked(state == 1);
            svSettingDeviceRotate.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                DpMsgDefine.DPPrimary<Integer> check = new DpMsgDefine.DPPrimary<>();
                check.value = isChecked ? 1 : 0;
                presenter.updateInfoReq(check, DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE);
                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
            });
        }
        /////////////////////////////////////////////////////////////////////
        if (device != null && JFGRules.showDelayRecordBtn(device.pid)) {
            svSettingDeviceDelayCapture.setEnabled(true);
        } else {
            svSettingDeviceDelayCapture.setVisibility(View.GONE);
        }


        TreeNode node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName(DelayRecordActivity.class.getSimpleName());
        svSettingDeviceDelayCapture.showRedHint(node != null && node.getNodeCount() > 0);

        sbtnSettingSight.setVisibility(JFGRules.showSight(device.pid, false) ? View.VISIBLE : View.GONE);
        try {
            String dpPrimary = device.$(509, "1");
            sbtnSettingSight.setSubTitle(getString(TextUtils.equals(dpPrimary, "1") ? R.string.Tap1_Camera_Front : R.string.Tap1_Camera_Overlook));
        } catch (Exception e) {
        }
        if (JFGRules.showSight(device.pid, false)) {
            sbtnSettingSight.setVisibility(View.VISIBLE);
            try {
                String dpPrimary = device.$(509, "1");
                sbtnSettingSight.setSubTitle(getString(TextUtils.equals(dpPrimary, "1") ? R.string.Tap1_Camera_Front : R.string.Tap1_Camera_Overlook));
            } catch (Exception e) {
            }
        } else {
            sbtnSettingSight.setVisibility(View.GONE);
        }
        AppLogger.d(String.format(Locale.getDefault(), "3g?%s,net?%s,", isMobileNet, net));
        switchBtn(lLayoutSettingItemContainer, !dpStandby.standby);
        AppLogger.d(String.format(Locale.getDefault(), "3g?%s,net?%s,", isMobileNet, net));
        switchBtn(lLayoutSettingItemContainer, !dpStandby.standby);

        /*
        * DPIDBaseIsExistNetWired = 225
         设备端是否存在可用的有线网络。
         设备端插入和拔出网线均会上报该消息。
         id long 功能消息唯一标识
         timeMsec int64 DP时间点, 毫秒
         value int  0 拔出网线， 1 插入网线
         DPIDBaseIsNetWired = 226
         客户端配置/设备端查询 设备是否使用有线网络
         id long 功能消息唯一标识
         timeMsec int64 DP时间点, 毫秒
         isWiredNet bool   True 使用有线网络， False 不使用有线网络。
        * */
        //有线模式
        svSettingDeviceWiredMode.setVisibility(JFGRules.showWiredMode(device.pid, false) ? View.VISIBLE : View.GONE);
        boolean wiredModeEnable = device.$(225, 0) == 1;
//        svSettingDeviceWiredMode.setEnabled(wiredModeEnable);
        boolean wiredModeOnline = device.$(226, 0) == 1;
        boolean useWiredNetwork = device.$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet()).net == 10;
        if (useWiredNetwork) {
            svSettingDeviceWifi.setEnabled(false);
            svSettingDeviceWifi.setSubTitle("");
        }
//        svSettingDeviceWifi.setEnabled(!wiredModeOnline);
        svSettingDeviceWiredMode.setSubTitle(useWiredNetwork ? getString(R.string.WIRED_MODE_CONNECTED) : getString(R.string.DOOR_NOT_CONNECT));
//        svSettingDeviceWiredMode.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
//            if (NetUtils.getJfgNetType() == 0) {
//                ToastUtil.showToast(getString(R.string.NoNetworkTips));
//                return;
//            }
//            if (!isChecked) {
//                AlertDialogManager.getInstance().showDialog(this, "关闭有线模式", getString(R.string.Cable_Mode_Switch_Cancel),
//                        getString(R.string.OK), (dialog, which) -> {
//                            svSettingDeviceWiredMode.setChecked(false);
//                            svSettingDeviceWifi.setEnabled(true);
//                            presenter.updateInfoReq(new DpMsgDefine.DPPrimary<>(0), 226);
//                        }, getString(R.string.CANCEL), (dialog, which) -> {
//                            svSettingDeviceWiredMode.setChecked(false);
//                        }, false);
//                return;
//            }
//            presenter.updateInfoReq(new DpMsgDefine.DPPrimary<>(1), 226);
//            //wifi配置开启,,关闭
//            svSettingDeviceWifi.setEnabled(false);
//        });
        svSettingDeviceSoftAp.setVisibility(JFGRules.showSoftAp(device.pid, false) ? View.VISIBLE : View.GONE);
        //总的条件:相同的ssid名字
        if (JFGRules.isDeviceOnline(device.$(201, new DpMsgDefine.DPNet()))) {
            //待机不可用
            if (dpStandby.standby) {
                svSettingDeviceSoftAp.setEnabled(false);
            }
            //在线,判断客户端和设备端的ssid
            //没有连接公网.//必须是连接状态
//            WifiInfo info = NetUtils.getWifiManager(ContextUtils.getContext()).getConnectionInfo();
//            svSettingDeviceSoftAp.setEnabled(info != null && TextUtils.equals(info.getSSID().replace("\"", ""), net.ssid));
        } else {
            svSettingDeviceSoftAp.setEnabled(false);
        }
        svSettingDeviceSoftAp.setOnClickListener(v -> {
            if (NetUtils.getJfgNetType() == 0) {
                ToastUtil.showToast(getString(R.string.NoNetworkTips));
                return;
            }
            getAlertDialogManager()
                    .showDialog(this, getString(R.string.Start_Hotspot), getString(R.string.Start_Hotspot_Prompt, net.ssid), getString(R.string.OK), (dialog, which) -> {
                        ToastUtil.showToast(getString(R.string.Instructions_Sent));
                        LoadingDialog.showLoading(this, getString(R.string.SETTING), true);
                        ToastUtil.showToast(getString(R.string.Instructions_Sent));
                        Subscription subscription = presenter.switchApModel(1)
                                .subscribeOn(Schedulers.io())
                                .delay(1, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(ret -> {
                                    LoadingDialog.dismissLoading();
                                    ToastUtil.showToast(getString(R.string.Start_Success));
                                }, throwable -> {
                                    LoadingDialog.dismissLoading();
                                    ToastUtil.showToast(getString(R.string.Start_Failed));
                                });
                        presenter.addSub(subscription, "enableAp");
                    }, getString(R.string.CANCEL), null, false);
        });
        IProperty productProperty = PropertiesLoader.getInstance();
        //SD 卡的显示与隐藏
        if (productProperty.hasProperty(device.pid, "SD")) {
            svSettingDeviceSDCard.setVisibility(View.VISIBLE);
            DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());
            String statusContent = getSdcardState(status.hasSdcard, status.err);
            if (!TextUtils.isEmpty(statusContent) && statusContent.contains("(")) {
                svSettingDeviceSDCard.setSubTitle(statusContent, android.R.color.holo_red_dark);
            } else {
                svSettingDeviceSDCard.setSubTitle(statusContent, R.color.color_8c8c8c);
            }
        } else {
            svSettingDeviceSDCard.setVisibility(View.GONE);
        }


        //PIR 设置
        if (productProperty.hasProperty(device.pid, "INFRAREDVISION")) {
            svSettingDevicePIR.setVisibility(View.VISIBLE);
        } else {
            svSettingDevicePIR.setVisibility(View.GONE);
        }

        //ap直连
        if (productProperty.hasProperty(device.pid, "APCONNECTTING")) {
            svSettingDeviceDirectMode.setVisibility(View.VISIBLE);
            svSettingDeviceHomeMode.setVisibility(View.VISIBLE);
            //家居模式和 WiFi 配置重复了
            svSettingDeviceWifi.setVisibility(View.GONE);
            tvNetWorkSettingTitle.setVisibility(View.GONE);
            attributeUpdate();
        } else {
            svSettingDeviceDirectMode.setVisibility(View.GONE);
            svSettingDeviceHomeMode.setVisibility(View.GONE);
            //家居模式和 WiFi 配置重复了
            svSettingDeviceWifi.setVisibility(View.VISIBLE);
            tvNetWorkSettingTitle.setVisibility(View.VISIBLE);
        }

        if (productProperty.hasProperty(device.pid, "LOGO 设置")) {
            //暂定为 LoGo 设置
            svSettingDeviceLogo.setVisibility(View.VISIBLE);
        } else {
            svSettingDeviceLogo.setVisibility(View.GONE);
        }


        //////////////////////////////////////////////////////安全防护////////////////////////////////////////////////////
        node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName(SafeProtectionFragment.class.getSimpleName());
        svSettingSafeProtection.setVisibility(productProperty.hasProperty(device.pid, "PROTECTION") ? View.VISIBLE : View.GONE);
        svSettingSafeProtection.showRedHint(node != null && node.getNodeCount() > 0);
        svSettingSafeProtection.setEnabled(!dpStandby.standby && !apNet);//ap 模式下有网操作选项需要置灰
//        svSettingSafeProtection.setAlpha(!dpStandby.standby && !apNet ? 1.0f : 0.6f);
        svSettingSafeProtection.setSubTitle(dpStandby.standby ? getString(R.string.MAGNETISM_OFF) : presenter.getAlarmSubTitle(getContext()));
//        svSettingSafeProtection.setAlpha(apNet ? 0.5f : 1f);

        /////////////////////////////////////////////////////录像设置//////////////////////////////////////////////////////
        if (productProperty.hasProperty(device.pid, "AUTORECORD")) {
            svSettingDeviceAutoRecord.setVisibility(View.VISIBLE);
//             TODO: 2017/7/7 获取自动录像是否开启 ,现在默认关闭
//            svSettingDeviceAutoRecord.setSubTitle(getString(R.string.Tap1_Setting_Unopened), R.color.color_8c8c8c);
            ////////////////////////显示红点//////////////////////////////////////////////
            node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName(VideoAutoRecordFragment.class.getSimpleName());
            ////////////////////////////autoRecord////////////////////////////////////////
            svSettingDeviceAutoRecord.setEnabled(!dpStandby.standby && (!is720 || !apNet));//ap 模式下有网操作选项需要置灰
            svSettingDeviceAutoRecord.setAlpha(!dpStandby.standby && (!is720 || !apNet) ? 1.0f : 0.6f);
            if (!productProperty.hasProperty(device.pid, "VIDEO") && !is720) {
                svSettingDeviceAutoRecord.setSubTitle(dpStandby.standby ? "" : presenter.getAutoRecordTitle(getContext()));
            }
            svSettingDeviceAutoRecord.showRedHint(node != null && node.getNodeCount() > 0);
//            svSettingDeviceAutoRecord.setAlpha(apNet ? 0.5f : 1f);
        } else {
            svSettingDeviceAutoRecord.setVisibility(View.GONE);
        }


        //清空呼叫记录设置 ,只有门铃才有清空呼叫记录
        svSettingDeviceClearRecord.setVisibility(productProperty.hasProperty(device.pid, "EMPTIED") ? View.VISIBLE : View.GONE);

//        if (productProperty.hasProperty(device.pid, "VIDEO")) {
////            svSettingDeviceAutoRecord.setVisibility(View.VISIBLE);
////             TODO: 2017/7/7 获取自动录像是否开启 ,现在默认关闭
//            svSettingDeviceAutoRecord.setVisibility(View.VISIBLE);
//            Boolean aBoolean = device.$(305, false);
////            svSettingDeviceAutoRecord.setSubTitle(aBoolean ? getString(R.string.ON) : getString(R.string.OFF));
//
////            svSettingDeviceAutoRecord.setSubTitle(getString(R.string.Tap1_Setting_Unopened), R.color.color_8c8c8c);
////            ////////////////////////显示红点//////////////////////////////////////////////
////            node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName(VideoAutoRecordFragment.class.getSimpleName());
////            ////////////////////////////autoRecord////////////////////////////////////////
////            svSettingDeviceAutoRecord.setEnabled(!dpStandby.standby);
////            svSettingDeviceAutoRecord.setAlpha(!dpStandby.standby ? 1.0f : 0.6f);
////            svSettingDeviceAutoRecord.setSubTitle(dpStandby.standby ? "" : presenter.getAutoRecordTitle(getContext()));
////            svSettingDeviceAutoRecord.showRedHint(node != null && node.getNodeCount() > 0);
//        }
//        else {
//            svSettingDeviceAutoRecord.setVisibility(View.GONE);
//        }

//        svSettingDeviceClearRecord.setVisibility(productProperty.isSerial("BELL", device.pid) ? View.VISIBLE : View.INVISIBLE);
//        svSettingDeviceWifi.setVisibility(productProperty.hasProperty(device.pid, "WIFI") ? View.VISIBLE : View.GONE);
//
//        svSettingDevicePIR.setVisibility(productProperty.hasProperty(device.pid, "INFRAREDVISION") ? View.VISIBLE : View.GONE);
//        svSettingDeviceSDCard.setVisibility(productProperty.hasProperty(device.pid, "SD") ? View.VISIBLE : View.GONE);
//        sbtnSetting110v.setVisibility(productProperty.hasProperty(device.pid, "NTSC") ? View.VISIBLE : View.GONE);
//        svSettingDeviceRotate.setVisibility(productProperty.hasProperty(device.pid, "HANGUP") ? View.VISIBLE : View.GONE);
//        svSettingDeviceStandbyMode.setVisibility(productProperty.hasProperty(device.pid, "STANDBY") ? View.VISIBLE : View.GONE);
//        svSettingDeviceLedIndicator.setVisibility(productProperty.hasProperty(device.pid, "LED") ? View.VISIBLE : View.GONE);
////        svSettingDeviceMobileNetwork.setVisibility(productProperty.hasProperty(device.pid, "") ? View.VISIBLE : View.GONE);
//        svSettingDeviceSoftAp.setVisibility(productProperty.hasProperty(device.pid, "AP") ? View.VISIBLE : View.GONE);
//        svSettingDeviceWiredMode.setVisibility(productProperty.hasProperty(device.pid, "WIREDMODE") ? View.VISIBLE : View.GONE);
//        sbtnSettingSight.setVisibility(productProperty.hasProperty(device.pid, "VIEWANGLE") ? View.VISIBLE : View.GONE);
    }

    @Override
    public void deviceUpdate(JFGDPMsg msg) throws IOException {
        switch ((int) msg.id) {
            case 222:
                if (msg.packValue != null) {
                    DpMsgDefine.DPSdcardSummary summary = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
                    if (summary == null) {
                        summary = new DpMsgDefine.DPSdcardSummary();
                    }
                    //sd
                    String statusContent = presenter.getDetailsSubTitle(getContext(), summary.hasSdcard, summary.errCode);
//                    if (!TextUtils.isEmpty(statusContent) && statusContent.contains("(")) {
//                        svSettingDeviceDetail.setSubTitle(statusContent, android.R.color.holo_red_dark);
//                    } else {
                    svSettingDeviceDetail.setSubTitle(statusContent, R.color.color_8c8c8c);
//                    }
                    //自动录像显示.
                    Device device = DataSourceManager.getInstance().getDevice(uuid);
                    if (!PropertiesLoader.getInstance().hasProperty(device.pid, "VIDEO") && !JFGRules.isPan720(device.pid)) {//只有有24小时录像选项才显示子标题
                        DpMsgDefine.DPStandby standby = presenter.getDevice().$(508, new DpMsgDefine.DPStandby());
                        svSettingDeviceAutoRecord.setSubTitle(standby.standby ? "" : presenter.getAutoRecordTitle(getContext()));
                    }
                }
                break;
            case DpMsgMap.ID_223_MOBILE_NET:
            case ID_209_LED_INDICATOR:
            case DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE:
                deviceUpdate(DataSourceManager.getInstance().getDevice(uuid));
                //	0 未知, 1 没卡, 2 user'account PIN, 3 user'account PUK, 4 network PIN, 5 正常
                break;
            case DpMsgMap.ID_204_SDCARD_STORAGE:
                DpMsgDefine.DPSdStatus status = null;
                DpMsgDefine.DPSdStatusInt statusInt = null;
                try {
                    status = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        statusInt = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdStatusInt.class);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                if (status == null && statusInt != null) {
                    status = new DpMsgDefine.DPSdStatus();
                    status.hasSdcard = statusInt.hasSdcard == 1;
                    status.err = statusInt.err;
                    status.used = statusInt.used;
                    status.total = statusInt.total;
                }
                if (status == null) {
                    status = DataSourceManager.getInstance().getDevice(uuid)
                            .$(204, new DpMsgDefine.DPSdStatus());
                }
                if (status == null) {
                    status = new DpMsgDefine.DPSdStatus();
                }
                String detailInfo = presenter.getDetailsSubTitle(getContext(), status.hasSdcard, status.err);
//                if (!TextUtils.isEmpty(detailInfo) && detailInfo.contains("(")) {
//                    svSettingDeviceDetail.setSubTitle(detailInfo, android.R.color.holo_red_dark);
//                } else
                svSettingDeviceDetail.setSubTitle(detailInfo, R.color.color_8C8C8C);
                LoadingDialog.dismissLoading();
                break;
            case 305:

//                try {
//                    Boolean enable = DpUtils.unpackData(msg.packValue, boolean.class);
//                    if (enable == null) enable = false;
//                    svSettingDeviceAutoRecord.setSubTitle(enable ? getString(R.string.ON) : getString(R.string.OFF));
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    try {
//                    } catch (Exception e1) {
//                        e1.printStackTrace();
//                    }
//                }
                //不显示
                break;

        }
    }

    private boolean ledPreState() {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        return device.$(ID_209_LED_INDICATOR, false);
    }

    /**
     * 安全防护
     *
     * @return
     */
    private boolean alarmPreState() {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        return device.$(ID_501_CAMERA_ALARM_FLAG, false);
    }

    private int autoRecordPreState() {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        return device.$(ID_303_DEVICE_AUTO_VIDEO_RECORD, -1);
    }

    private void triggerStandby(DpMsgDefine.DPStandby dpStandby) {
        boolean open = dpStandby.standby;
        svSettingSafeProtection.setEnabled(!open);
        svSettingSafeProtection.setSubTitle(open ? getString(R.string.MAGNETISM_OFF) : presenter.getAlarmSubTitle(getContext()));

        svSettingDeviceAutoRecord.setEnabled(!open);
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        if (!PropertiesLoader.getInstance().hasProperty(device.pid, "VIDEO") && !JFGRules.isPan720(device.pid)) {
            svSettingDeviceAutoRecord.setSubTitle(open ? "" : presenter.getAutoRecordTitle(getContext()));
        }

        boolean led = !open && dpStandby.led;
        svSettingDeviceLedIndicator.setEnabled(!open);
        svSettingDeviceLedIndicator.setChecked(!open && led);
    }

    @Override
    public void unbindDeviceRsp(int state) {
        if (state == JError.ErrorOK) {
            LoadingDialog.dismissLoading();
            Intent intent = new Intent(this, NewHomeActivity.class);
            intent.putExtra("NewHomeActivity_intent", getString(R.string.DELETED_SUC));
            startActivity(intent);
        } else if (state == -1) {
            ToastUtil.showToast(getString(R.string.Tips_DeleteFail));
            LoadingDialog.dismissLoading();
            hasPendingUnbindAction = false;
        }
    }

    @Override
    public void onNetworkChanged(boolean connected) {

        DpMsgDefine.DPStandby dpStandby = presenter.getDevice().$(DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, new DpMsgDefine.DPStandby());
        svSettingDeviceMobileNetwork.setEnabled(!dpStandby.standby && connected);
        svSettingDeviceDelayCapture.setEnabled(connected);
        DpMsgDefine.DPNet net = presenter.getDevice().$(201, new DpMsgDefine.DPNet());
        enableStandby(connected && JFGRules.isDeviceOnline(net));
    }

    @Override
    public void clearSdResult(int code) {
        hideLoading();
        if (code == 0) {
            ToastUtil.showPositiveToast(getString(R.string.SD_INFO_3));
        } else if (code == -1) {
            ToastUtil.showNegativeToast(getString(R.string.NETWORK_TIMEOUT));
        } else {
            ToastUtil.showNegativeToast(getString(R.string.SD_ERR_3));
        }
        deviceUpdate(DataSourceManager.getInstance().getDevice(uuid));
    }

    @Override
    public void onClearBellRecordSuccess() {
        ToastUtil.showPositiveToast(getString(R.string.Clear_Sdcard_tips3));
        LoadingDialog.dismissLoading();
    }

    @Override
    public void onClearBellRecordFailed() {
        ToastUtil.showNegativeToast(getString(R.string.Clear_Sdcard_tips4));
        LoadingDialog.dismissLoading();
    }

    private boolean hasPendingUnbindAction = false;

    @Override
    public void onDeviceUnBind() {
        AppLogger.w("当前设备已解绑");
        if (!hasPendingUnbindAction) {
            AlertDialogManager.getInstance().showDialog(this, getString(R.string.Tap1_device_deleted), getString(R.string.Tap1_device_deleted),
                    getString(R.string.OK), (dialog, which) -> {
                        finish();
                        Intent intent = new Intent(getContext(), NewHomeActivity.class);
                        startActivity(intent);
                    }, false);
        }
    }

    @Override
    public void onNewVersion(RxEvent.VersionRsp versionRsp) {
        AppLogger.w("new Version:" + versionRsp);
        initFirmwareHint(DataSourceManager.getInstance().getDevice(uuid));
    }

    private void enableStandby(boolean enable) {
        svSettingDeviceStandbyMode.setEnabled(enable);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DELAY_RECORD) {

        }
    }

    @OnClick(R.id.sv_setting_device_ap)
    public void onCheckDeviceAp() {
        Log.d("CamSettingActivity", "......");
        UdpDevice.pingDevice(UdpConstant.IP, uuid)
                .subscribe(s -> AppLogger.d("............." + s), AppLogger::e);
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        if (JFGRules.isAPDirect(uuid, device.$(202, ""))) {
            //wifi直连，客户端连接了设备
            startActivity(new Intent(this, ApSettingActivity.class)
                    .putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid));
        } else if (JFGRules.isDeviceOnline(uuid)) {
            //设备与客户端在线。
            String localSSid = NetUtils.getNetName(ContextUtils.getContext());
            String remoteSSid = device.$(201, new DpMsgDefine.DPNet()).ssid;
            if (TextUtils.equals(localSSid, remoteSSid) && !TextUtils.isEmpty(localSSid)) {
                //公网局域网
                startActivity(new Intent(this, ApSettingActivity.class)
                        .putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid));
            }
        } else {
            boolean isWifiApEnabled = NetUtils.isWifiApEnabled();
            if (!isWifiApEnabled) {
                handleJumpToConfig(true, true);
                return;
            }
            if (!NetUtils.isWifiApEnabled()) {
                ToastUtil.showToast(getString(R.string.DEVICE_OFFLINE6));
                return;
            }
            Subscription ssu = Observable.just("")
                    .subscribeOn(Schedulers.io())
                    .map(s -> {
                        ArrayList<NetUtils.ClientScanResult> list = NetUtils.getClientList(true, 1000);
                        if (ListUtils.isEmpty(list)) {
                            return null;
                        }
                        //设备的mac
                        final String mac = device.$(202, "");
                        NetUtils.ClientScanResult result = null;
                        for (NetUtils.ClientScanResult ret : list) {
                            if (TextUtils.equals(mac.toLowerCase(), ret.getHWAddr().toLowerCase())) {
                                result = ret;
                                break;
                            }
                        }
                        return result;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        //
                        if (result == null) {
                            handleJumpToConfig(true, true);
                        } else {
                            AppLogger.d("连上热点");
                            //wifi直连，客户端连接了设备
                            startActivity(new Intent(this, ApSettingActivity.class)
                                    .putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid));
                        }
                    }, AppLogger::e);
            presenter.addSub(ssu, "ssuu");
        }

    }

    private String getMobileNet(boolean hasSimcard, DpMsgDefine.DPNet net) {
        if (!hasSimcard) {
            return getString(R.string.OFF);
        }
        return net.ssid;
    }
}
