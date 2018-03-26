package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.DeviceInfoDetailPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.n.view.firmware.FirmwareUpdateActivity;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.dialog.EditFragmentDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_202_MAC;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_208_DEVICE_SYS_VERSION;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_210_UP_TIME;
import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_DEFAULT_EDIT_TEXT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_INPUT_HINT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_LEFT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_RIGHT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TITLE;
import static com.cylan.jiafeigou.widget.dialog.EditFragmentDialog.KEY_TOUCH_OUT_SIDE_DISMISS;


/**
 * 创建者     谢坤
 * 创建时间   2016/7/12 17:53
 * 用来控制摄像头模块下的设备信息，点击设备名称和设备时区时进行切换
 */
@Badge(parentTag = "CamSettingActivity")
public class DeviceInfoDetailFragment extends IBaseFragment<CamInfoContract.Presenter>
        implements CamInfoContract.View {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.tv_device_alias)
    SettingItemView0 tvDeviceAlias;
    @BindView(R.id.tv_device_time_zone)
    SettingItemView0 tvDeviceTimeZone;
    @BindView(R.id.tv_device_sdcard_state)
    SettingItemView0 tvDeviceSdcardState;
    @BindView(R.id.tv_device_mobile_net)
    SettingItemView0 tvDeviceMobileNet;
    @BindView(R.id.tv_device_wifi_state)
    SettingItemView0 tvDeviceWifiState;
    @BindView(R.id.tv_device_mac)
    SettingItemView0 tvDeviceMac;
    @BindView(R.id.tv_device_system_version)
    SettingItemView0 tvDeviceSystemVersion;
    @BindView(R.id.tv_device_battery_level)
    SettingItemView0 tvDeviceBatteryLevel;
    @BindView(R.id.tv_device_uptime)
    SettingItemView0 tvDeviceUptime;

    @BindView(R.id.rl_hardware_update)
    SettingItemView0 rlHardwareUpdate;

    @BindView(R.id.tv_device_cid)
    SettingItemView0 tvDeviceCid;
    @BindView(R.id.tv_device_software_version)
    SettingItemView0 tvDeviceSoftwareVersion;
    @BindView(R.id.tv_device_ip)
    SettingItemView0 tvDeviceIp;

    private EditFragmentDialog editDialogFragment;
    private Device device;

    public static DeviceInfoDetailFragment newInstance(Bundle bundle) {
        DeviceInfoDetailFragment fragment = new DeviceInfoDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
        device = DataSourceManager.getInstance().getDevice(uuid);
        presenter = new DeviceInfoDetailPresenterImpl(this, uuid);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facility_information, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean showBattery = JFGRules.showBattery(device != null ? device.pid : 0, false);
        //仅3G摄像头、FreeCam显示此栏
        tvDeviceBatteryLevel.setVisibility(showBattery ? View.VISIBLE : View.GONE);
        //全景不显示固件升级 显示软件版本 
        // TODO: 2017/8/16 #113788 全景需要显示固件升级 和 SD 卡选项
        tvDeviceSoftwareVersion.setVisibility(JFGRules.showSoftWare(device.pid, false) ? View.VISIBLE : View.GONE);
        boolean showFU = JFGRules.showFirmware(device != null ? device.pid : 0, false);
        //固件升级,分享设备不显示
        rlHardwareUpdate.setVisibility(showFU ? View.VISIBLE : View.GONE);
        tvDeviceIp.setVisibility(JFGRules.showIp(device.pid, false) ? View.VISIBLE : View.GONE);


        //控制时区显示与隐藏
        tvDeviceTimeZone.setVisibility(JFGRules.showTimeZone(device != null ? device.pid : 0, false) ? View.VISIBLE : View.GONE);

    }

    @Override
    public void onStart() {
        super.onStart();
        updateDetails();
        checkDevResult();
    }

    private void updateDetails() {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        if (device == null) {
            return;
        }
        //是否分享设备
        if (!TextUtils.isEmpty(device.shareAccount) && !JFGRules.isPan720(device.pid)) {//todo 720 分享者享有所有权限
            tvDeviceAlias.showDivider(false);
            tvDeviceTimeZone.setVisibility(View.GONE);
            tvDeviceSdcardState.setVisibility(View.GONE);
            rlHardwareUpdate.setVisibility(View.GONE);
//            getView().findViewById(R.id.tv_storage).setVisibility(View.GONE);
        }
        //是否显示固件升级


        //是否显示移动网络
        boolean hasWiFi = JFGRules.hasWiFi(device.pid);
        boolean hasSimCard = device.$(DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY, false) || !hasWiFi;
        tvDeviceMobileNet.setVisibility(JFGRules.showMobileNet(device.pid, false) ? View.VISIBLE : View.GONE);
        DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
        tvDeviceMobileNet.setSubTitle(getMobileNet(hasSimCard, net));

        //是否显示 WiFi 信息,没有 WiFi 功能的不显示
        tvDeviceWifiState.setVisibility(hasWiFi ? View.VISIBLE : View.GONE);

        //控制时区显示与隐藏
        boolean showTimezone = JFGRules.showTimeZone(device.pid, false);
        if (showTimezone) {
            DpMsgDefine.DPTimeZone zone = device.$(214, new DpMsgDefine.DPTimeZone());
            if (zone != null) {
                MiscUtils.loadTimeZoneList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((List<TimeZoneBean> list) -> {
                            TimeZoneBean bean = new TimeZoneBean();
                            bean.setId(zone.timezone);
                            if (list != null) {
                                int index = list.indexOf(bean);
                                if (index >= 0 && index < list.size()) {
                                    tvDeviceTimeZone.setSubTitle(list.get(index).getName());
                                }
                            }
                        }, AppLogger::e);
            }
        } else {
            tvDeviceTimeZone.setVisibility(View.GONE);
        }
        DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());
        String statusContent = getSdcardState(status.hasSdcard, status.err);
        if (!TextUtils.isEmpty(statusContent) && statusContent.contains("(")) {
            tvDeviceSdcardState.setSubTitle(statusContent, android.R.color.holo_red_dark);
        } else {
            tvDeviceSdcardState.setSubTitle(statusContent, R.color.color_8c8c8c);
        }
        tvDeviceAlias.setSubTitle(device == null ? "" : TextUtils.isEmpty(device.alias) ? uuid : device.alias);
        tvDeviceCid.setSubTitle(uuid);
        String m = device.$(ID_202_MAC, "");
        if (TextUtils.isEmpty(m)) {
            m = PreferencesUtils.getString(JConstant.KEY_DEVICE_MAC + uuid);
        }
        tvDeviceMac.setSubTitle(m);
        boolean charging = device.$(DpMsgMap.ID_205_CHARGING, false);
        int b = device.$(DpMsgMap.ID_206_BATTERY, 0);
        boolean apMode = JFGRules.isAPDirect(uuid, m);
        tvDeviceBatteryLevel.setSubTitle((JFGRules.isDeviceOnline(net) || apMode) ? (charging ? getString(R.string.CHARGING) + (b + "%") : (b + "%")) : "");
        String v = device.$(ID_208_DEVICE_SYS_VERSION, "");
        tvDeviceSystemVersion.setSubTitle(v);
        int u = device.$(ID_210_UP_TIME, 0);
        tvDeviceUptime.setSubTitle(TimeUtils.getUptime(JFGRules.isDeviceOnline(net) ? u : 0));
        boolean isMobileNet = net.net > 1;
        if (apMode || (JFGRules.isPan720(device.pid) && net.net <= 0)) {//针对离线的720设备以及直连AP模式下，wifi显示：未启用
            tvDeviceWifiState.setSubTitle(getString(R.string.OFF));
        } else {
            tvDeviceWifiState.setSubTitle(!TextUtils.isEmpty(net.ssid) ? (isMobileNet ? getString(R.string.OFF) : net.ssid) : getString(R.string.OFF_LINE));
        }
        String softWare = device.$(DpMsgMap.ID_207_DEVICE_VERSION, "");
        tvDeviceSoftwareVersion.setSubTitle(softWare);
        //ip地址
        tvDeviceIp.setSubTitle(JFGRules.isDeviceOnline(device.$(201, new DpMsgDefine.DPNet())) ? device.$(227, "") : "");


    }

    private String getMobileNet(boolean hasSimcard, DpMsgDefine.DPNet net) {
        if (!hasSimcard) {
            return getString(R.string.OFF);
        }
        return net.ssid;
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

    @OnClick({R.id.tv_toolbar_icon,
            R.id.tv_device_sdcard_state,
            R.id.tv_device_alias,
            R.id.tv_device_time_zone,
            R.id.rl_hardware_update})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().onBackPressed();
                break;
            case R.id.tv_device_alias:
                toEditAlias();
                break;
            case R.id.tv_device_time_zone:
                toEditTimezone();
                break;
            case R.id.tv_device_sdcard_state:
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
            case R.id.rl_hardware_update:
                jump2HardwareUpdateFragment();
                break;
        }
    }

    /**
     * 固件升级
     */
    private void jump2HardwareUpdateFragment() {
        Intent intent = new Intent(getActivity(), FirmwareUpdateActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid());
        startActivity(intent);
    }

    /**
     * 显示Sd卡的详情
     */
    private void jump2SdcardDetailFragment() {
//        Bundle bundle = new Bundle();
//        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
//        SDcardDetailFragment sdcardDetailFragment = SDcardDetailFragment.newInstance(bundle);
//        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
//                sdcardDetailFragment, android.R.id.content);
        Intent intent = new Intent(getActivity(), SdcardDetailActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        getActivity().startActivity(intent);
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
        simpleDialogFragment.show(getFragmentManager(), "simpleDialogFragment");
    }

    /**
     * 编辑时区
     */
    private void toEditTimezone() {
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        DeviceTimeZoneFragment timeZoneFragment = DeviceTimeZoneFragment.newInstance(bundle);
        timeZoneFragment.setCallBack((Object o) -> {
            if (!(o instanceof DpMsgDefine.DPTimeZone)) {
                return;
            }
            Device device = DataSourceManager.getInstance().getDevice(uuid);
            //更新ui
            DpMsgDefine.DPTimeZone zone = device.$(214, new DpMsgDefine.DPTimeZone());
            if (zone != null) {
                MiscUtils.loadTimeZoneList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((List<TimeZoneBean> list) -> {
                            TimeZoneBean bean = new TimeZoneBean();
                            bean.setId(zone.timezone);
                            if (list != null) {
                                int index = list.indexOf(bean);
                                if (index >= 0 && index < list.size()) {
                                    tvDeviceTimeZone.setSubTitle(list.get(index).getName());
                                    ToastUtil.showToast(getString(R.string.SCENE_SAVED));
                                }
                            }
                        }, AppLogger::e);
            }
        });
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                timeZoneFragment, android.R.id.content);
    }

    /**
     * 编辑昵称
     */
    private void toEditAlias() {
//        if (editDialogFragment == null) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, getString(R.string.EQUIPMENT_NAME));
        bundle.putString(KEY_LEFT_CONTENT, getString(R.string.OK));
        bundle.putString(KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
        bundle.putString(KEY_INPUT_HINT, getString(R.string.EQUIPMENT_NAME));
        bundle.putString(KEY_DEFAULT_EDIT_TEXT, TextUtils.isEmpty(tvDeviceAlias.getSubTitle().toString()) ? getString(R.string.EQUIPMENT_NAME) : tvDeviceAlias.getSubTitle().toString());
        bundle.putBoolean(KEY_TOUCH_OUT_SIDE_DISMISS, false);
        editDialogFragment = EditFragmentDialog.newInstance(bundle);
//        }
//        if (editDialogFragment.isVisible())
//            return;
        editDialogFragment.show(getChildFragmentManager(), "editDialogFragment");
        editDialogFragment.setDefaultFilter(new InputFilter[]{
                new InputFilter.LengthFilter(24)
        });
        editDialogFragment.setAction((int id, Object value) -> {
            if (value != null && value instanceof String) {
                String content = (String) value;
                Device device = DataSourceManager.getInstance().getDevice(uuid);
                if (!TextUtils.isEmpty(content) && device != null && !TextUtils.equals(content, device.alias)) {
                    device.alias = content;
                    tvDeviceAlias.setSubTitle((CharSequence) value);
                    if (presenter != null) {
                        presenter.updateAlias(device);
                    }
                }
            }
        });
    }


    public void checkDevResult() {
        String s = device.$(207, "");
        rlHardwareUpdate.setSubTitle(s);
        try {
            String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + uuid());
            rlHardwareUpdate.setSubTitle(!TextUtils.isEmpty(content) ? getString(R.string.Tap1_NewFirmware) : s);

            rlHardwareUpdate.showRedHint(!TextUtils.isEmpty(content));
        } catch (Exception e) {
        }
    }

    @Override
    public void showLoading() {
        LoadingDialog.showLoading(getActivity(), getString(R.string.SD_INFO_2), true);
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading();
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
    }

    @Override
    public void setAliasRsp(int code) {
        if (code == JError.ErrorOK) {
            ToastUtil.showPositiveToast(getString(R.string.SCENE_SAVED));
        } else {
            ToastUtil.showNegativeToast(getString(R.string.set_failed));
        }
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
                    String statusContent = getSdcardState(summary.hasSdcard, summary.errCode);
                    if (!TextUtils.isEmpty(statusContent) && statusContent.contains("(")) {
                        tvDeviceSdcardState.setSubTitle(statusContent, android.R.color.holo_red_dark);
                    } else {
                        tvDeviceSdcardState.setSubTitle(statusContent, R.color.color_8c8c8c);
                    }
                }
                break;
            case 214:
                //zone
                MiscUtils.loadTimeZoneList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((List<TimeZoneBean> list) -> {
                            Device device = DataSourceManager.getInstance().getDevice(uuid);
                            //更新ui
                            DpMsgDefine.DPTimeZone zone = device.$(214, new DpMsgDefine.DPTimeZone());
                            if (zone == null) {
                                return;
                            }
                            TimeZoneBean bean = new TimeZoneBean();
                            bean.setId(zone.timezone);
                            if (list != null) {
                                int index = list.indexOf(bean);
                                if (index >= 0 && index < list.size()) {
                                    tvDeviceTimeZone.setSubTitle(list.get(index).getName());
                                }
                            }
                        }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
                break;
            case 201:
                //wifi
                Device device = DataSourceManager.getInstance().getDevice(uuid);
                DpMsgDefine.DPNet net = device.$(201, new DpMsgDefine.DPNet());
                tvDeviceWifiState.setSubTitle(net != null && !TextUtils.isEmpty(net.ssid) ? net.ssid : getString(R.string.OFF_LINE));
                break;
            case 206:
//                updateDetails();
                break;
        }
        updateDetails();
    }

    @Override
    public void onCheckDeviceVersionFinished(String tagVersion) {
        AppLogger.d("onCheckDeviceVersionFinished:" + tagVersion);
        String s = device.$(207, "");
        rlHardwareUpdate.setSubTitle(s);
        try {
            String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + uuid());
            rlHardwareUpdate.setSubTitle(!TextUtils.isEmpty(content) ? getString(R.string.Tap1_NewFirmware) : s);

            rlHardwareUpdate.showRedHint(!TextUtils.isEmpty(content));
        } catch (Exception e) {
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
