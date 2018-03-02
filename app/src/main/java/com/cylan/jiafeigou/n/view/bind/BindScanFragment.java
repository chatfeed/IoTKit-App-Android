package com.cylan.jiafeigou.n.view.bind;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.bind.ScanContract;
import com.cylan.jiafeigou.n.mvp.impl.bind.ScanPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.BindDeviceActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.zscan.ZXingScannerView;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.google.zxing.Result;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static com.cylan.jiafeigou.misc.JError.ErrorCIDBinded;
import static com.cylan.jiafeigou.misc.JError.ErrorCIDNotBind;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BindScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@RuntimePermissions
public class BindScanFragment extends IBaseFragment<ScanContract.Presenter> implements ZXingScannerView.ResultHandler, ScanContract.View {

    private static final String VID = "vid";
    private static final String SN = "sn";
    private static final String PID = "pid";
    @BindView(R.id.zxV_scan)
    ZXingScannerView zxVScan;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    public BindScanFragment() {
        // Required empty public constructor
    }

    public static BindScanFragment newInstance(Bundle bundle) {
        BindScanFragment fragment = new BindScanFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.presenter = new ScanPresenterImpl(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        BindScanFragmentPermissionsDispatcher.onCameraPermissionWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BindScanFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    public void onCameraPermissionDenied() {
        if (getActivity() != null && getView() != null) {
            getView().post(() -> getActivity().getSupportFragmentManager().popBackStack());
        }
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    public void onNeverAskAgainCameraPermission() {
        AlertDialogManager.getInstance().showDialog(getActivity(),
                getString(R.string.permission_auth, getString(R.string.CAMERA)),
                getString(R.string.permission_auth, getString(R.string.CAMERA)),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    settingIntent.setData(Uri.parse("package:" + ContextUtils.getContext().getPackageName()));
                    startActivity(settingIntent);
                    PackageManager packageManager = ContextUtils.getContext().getPackageManager();
                    List<ResolveInfo> list = packageManager.queryIntentActivities(settingIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (list.size() > 0) {
                        startActivityForResult(settingIntent, 0);
                    } else {
                        if (getActivity() != null && getActivity() instanceof BindDeviceActivity) {
                            ((BindDeviceActivity) getActivity()).finishExt();
                        }
                    }
                },
                getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    if (getActivity() != null && getActivity() instanceof BindDeviceActivity) {
                        ((BindDeviceActivity) getActivity()).finishExt();
                    }
                });
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void onCameraPermission() {
        customToolbar.setVisibility(View.VISIBLE);
        zxVScan.startCamera();
        zxVScan.setResultHandler(BindScanFragment.this);
    }


    @OnShowRationale(Manifest.permission.CAMERA)
    public void showRationaleForCamera(PermissionRequest request) {
        // NOTE: Show activity_cloud_live_mesg_call_out_item rationale to explain why the permission is needed, e.g. with activity_cloud_live_mesg_call_out_item dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showRationaleForCamera");
        onNeverAskAgainCameraPermission();
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d("onResume", "onResume: " + Thread.currentThread().getId());
        zxVScan.stopCamera();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bind_scan, null, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ViewUtils.addViewMargins(customToolbar, 0, (int) getResources().getDimension(R.dimen.y9), 0, 0);
        customToolbar.setBackAction(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }


    @Override
    public void handleResult(Result rawResult) {// Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the startTime to figure out.
        //转化成小写
        final String result = rawResult.getText().toLowerCase();
        ProductInfo info = new ProductInfo();
        info.setPid(MiscUtils.getValueFromUri(result, PID)).setSn(MiscUtils.getValueFromUri(result, SN)).setVid(MiscUtils.getValueFromUri(result, VID));
        if (info.isValid()) {
            try {
                handleScanResult(info);
            } catch (Exception e) {
                AppLogger.e("" + e.getLocalizedMessage());
            }
        } else if (info.isNotSupport()) {
            ToastUtil.showNegativeToast(getString(R.string.Tap1_AddDevice_QR_Fail));
            zxVScan.stopCamera();
            if (getActivity() instanceof BindDeviceActivity) {
                ((BindDeviceActivity) getActivity()).finishExt();
            }
        } else {
            ToastUtil.showToast(getString(R.string.EFAMILY_INVALID_DEVICE));
            if (getActivity() instanceof BindDeviceActivity) {
                ((BindDeviceActivity) getActivity()).finishExt();
            }
        }
    }

    public static final Pattern vidReg = Pattern.compile("vid=[0-9a-zA-Z]{0,12}", Pattern.CASE_INSENSITIVE);

    private void handleScanResult(ProductInfo info) {
        if (info.isEmpty()) {
            ToastUtil.showToast(getString(R.string.EFAMILY_INVALID_DEVICE));
            return;
        }
        int net = NetUtils.getJfgNetType(getActivity());
        if (net == 0) {
            ToastUtil.showToast(getString(R.string.NoNetworkTips));
            return;
        }
        zxVScan.stopCamera();
        try {
            Device device = DataSourceManager.getInstance().getDevice(info.sn);
            if (device != null && device.available()) {
                ToastUtil.showNegativeToast(getString(R.string.Tap1_AddedDeviceTips));
                HandlerThreadUtils.postDelay(() -> {
                    if (zxVScan != null) {
                        zxVScan.stop();
                    }
                    if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                    zxVScan.stop();
                }, 2000);
            } else {
                zxVScan.stop();
                int pid = Integer.parseInt(info.pid);
                getActivity().getSupportFragmentManager().beginTransaction().remove(this)
                        .commit();
                if (JFGRules.isConsumerCam(pid)) {
                    getActivity().findViewById(R.id.v_to_bind_consumer_cam)
                            .performClick();
                } else if (JFGRules.isCloudCam(pid)) {
                    getActivity().findViewById(R.id.v_to_bind_camera_cloud)
                            .performClick();
                } else if (JFGRules.isPanoramaCamera(pid)) {
                    getActivity().findViewById(R.id.v_to_bind_panorama_camera)
                            .performClick();
                } else if (JFGRules.isHalfCamera(pid)) {
                    getActivity().findViewById(R.id.v_to_bind_half_camera)
                            .performClick();
                } else if (JFGRules.isOutDoorCamera(pid)) {
                    getActivity().findViewById(R.id.v_to_bind_cam_outdoor).performClick();
                } else if (JFGRules.isCatEeyBell(pid)) {
                    getActivity().findViewById(R.id.v_to_bind_cat_eye_cam)
                            .performClick();
                } else if (JFGRules.isNoPowerBell(pid)) {
                    getActivity().findViewById(R.id.v_to_bind_bell_no_battery)
                            .performClick();
                } else if (JFGRules.isBell(pid)) {
                    getActivity().findViewById(R.id.v_to_bind_bell_battery)
                            .performClick();
                } else if (JFGRules.isCamera(pid)) {
                    getActivity().findViewById(R.id.v_to_bind_camera)
                            .performClick();
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onScanRsp(int state) {
        Log.d(this.getClass().getSimpleName(), "bindResult: " + state);
        if (state == 0) {
            ToastUtil.showPositiveToast(getString(R.string.Added_successfully));
        } else if (state == -1) {
            ToastUtil.showToast(getString(R.string.ADD_FAILED));
        } else if (state == ErrorCIDBinded) {
            ToastUtil.showToast(getString(R.string.RET_EISBIND_BYSELF));
        } else if (state == ErrorCIDNotBind) {
            ToastUtil.showToast(getString(R.string.RET_ECID_INVALID));
        }
        getActivity().getSupportFragmentManager().popBackStack();
        //默认强绑
    }

    @Override
    public void onStartScan() {
        zxVScan.startCamera();
    }

    private static final class ProductInfo {
        private String vid;
        private String sn;
        private String pid;

        public ProductInfo setVid(String vid) {
            this.vid = vid;
            return this;
        }

        public ProductInfo setSn(String sn) {
            this.sn = sn;
            return this;
        }

        public ProductInfo setPid(String pid) {
            this.pid = pid;
            return this;
        }

        public boolean isValid() {
            return !TextUtils.isEmpty(vid) && !TextUtils.isEmpty(pid) && !TextUtils.isEmpty(sn);
        }

        public boolean isNotSupport() {
            return TextUtils.isEmpty(vid) && !TextUtils.isEmpty(sn) && !TextUtils.isEmpty(pid);
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(vid) || TextUtils.isEmpty(pid) || TextUtils.isEmpty(sn);
        }
    }

}
