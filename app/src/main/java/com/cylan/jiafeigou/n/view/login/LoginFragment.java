package com.cylan.jiafeigou.n.view.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.SmartcallActivity;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.n.mvp.impl.ForgetPwdPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.LoginPresenterImpl;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.LocaleUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.LoginButton;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;


/**
 * 登陆主界面
 */
public class LoginFragment extends IBaseFragment<LoginContract.Presenter>
        implements LoginContract.View,
        BaseDialog.BaseDialogAction {
    private static final String TAG = "Fragment";
    public static final String KEY_TEMP_ACCOUNT = "temp_account";
    @BindView(R.id.et_login_username)
    EditText etLoginUsername;
    @BindView(R.id.iv_login_clear_username)
    ImageView ivLoginClearUsername;
    @BindView(R.id.et_login_pwd)
    EditText etLoginPwd;
    @BindView(R.id.iv_login_clear_pwd)
    ImageView ivLoginClearPwd;
    @BindView(R.id.cb_show_pwd)
    CheckBox cbShowPwd;
    @BindView(R.id.vsLayout_login_box)
    ViewSwitcher vsLayoutSwitcher;
    @BindView(R.id.rLayout_login_third_party)
    RelativeLayout rLayoutLoginThirdParty;
    @BindView(R.id.rLayout_login)
    LinearLayout rLayoutLogin;
    @BindView(R.id.tv_qqLogin_commit)
    TextView tvQqLoginCommit;
    @BindView(R.id.tv_xlLogin_commit)
    TextView tvXlLoginCommit;
    @BindView(R.id.tv_login_forget_pwd)
    TextView tvForgetPwd;
    @BindView(R.id.lb_login_commit)
    LoginButton lbLogin;
    @BindView(R.id.rLayout_pwd_input_box)
    FrameLayout rLayoutPwdInputBox;
    @BindView(R.id.view_third_party_center)
    View viewThirdPartyCenter;
    @BindView(R.id.et_register_input_box)
    EditText etRegisterInputBox;
    @BindView(R.id.iv_register_username_clear)
    ImageView ivRegisterUserNameClear;
    @BindView(R.id.et_verification_input)
    EditText etVerificationInput;
    @BindView(R.id.tv_meter_get_code)
    TextView tvMeterGetCode;
    @BindView(R.id.tv_register_submit)
    TextView tvRegisterSubmit;
    @BindView(R.id.tv_register_way_content)
    TextView tvRegisterWayContent;
    @BindView(R.id.fLayout_verification_code_input_box)
    FrameLayout fLayoutVerificationCodeInputBox;

    @BindView(R.id.lLayout_agreement)
    LinearLayout lLayoutAgreement;
    @BindView(R.id.tv_agreement)
    TextView tvAgreement;
    @BindView(R.id.tv_before_agreement)
    TextView before_tvAgreement;
    @BindView(R.id.tv_twitterLogin_commit)
    TextView tvTwitterLoginCommit;
    @BindView(R.id.tv_facebookLogin_commit)
    TextView tvFacebookLoginCommit;
    @BindView(R.id.rLayout_login_third_party_abroad)
    RelativeLayout rLayoutLoginThirdPartyAbroad;
    @BindView(R.id.rLayout_register_box)
    FrameLayout rLayoutRegisterBox;
    @BindView(R.id.rLayout_login_top)
    CustomToolbar rLayoutLoginToolbar;
    @BindView(R.id.fLayout_third)
    FrameLayout fLayoutThird;

    private VerificationCodeLogic verificationCodeLogic;
    private int registerWay = JConstant.REGISTER_BY_PHONE;
    private boolean isRegetCode;
    private String tempPhone;

    public static LoginFragment newInstance(Bundle bundle) {
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = new LoginPresenterImpl(this);
    }

    /**
     * 用来点击空白处隐藏键盘
     *
     * @param view
     */
    public void addOnTouchListener(View view) {
        view.setOnTouchListener((View v, MotionEvent event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                IMEUtils.hide(getActivity());
            }
            return false;
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addOnTouchListener(view);
        showLayout();
        decideRegisterWay();
        initView();
        showRegisterPage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (lbLogin != null) {
            lbLogin.cancelAnim();
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    /**
     * 不同的入口，在beforeLogin页面点击注册，进入主页页面；否则默认。
     */
    private void showRegisterPage() {
        Bundle bundle = getArguments();
        /**
         * 第三方使用亲友功能跳转到绑定手机这
         */
        if (bundle != null && bundle.containsKey("show_login_fragment")) {
            if (bundle.getBoolean(JConstant.OPEN_LOGIN_TO_BIND_PHONE)) {
                switchBoxBindPhone();
            } else {
                switchBox();
            }
        }
    }

    private void switchBoxBindPhone() {
        //register
        int way = LocaleUtils.getLanguageType(getActivity());
        if (way == JConstant.LOCALE_SIMPLE_CN) {
            rLayoutLoginToolbar.setToolbarTitle(R.string.Tap0_BindPhoneNo);
        } else {
            rLayoutLoginToolbar.setToolbarTitle(R.string.Tap0_BindEmail);
        }
        rLayoutLoginToolbar.getTvToolbarRight().setVisibility(View.GONE);
        tvRegisterWayContent.setVisibility(View.GONE);
        tvAgreement.setVisibility(View.GONE);
        before_tvAgreement.setVisibility(View.GONE);
        vsLayoutSwitcher.setInAnimation(getContext(), R.anim.slide_in_right_overshoot);
        vsLayoutSwitcher.setOutAnimation(getContext(), R.anim.slide_out_left);
        vsLayoutSwitcher.showNext();
    }

    /**
     * 是否显示邮箱注册。
     */
    private void decideRegisterWay() {
        int way = LocaleUtils.getLanguageType(getActivity());
        if (way == JConstant.LOCALE_SIMPLE_CN) {
            tvRegisterWayContent.setVisibility(View.VISIBLE);
            registerWay = JConstant.REGISTER_BY_PHONE;
        }
        registerWay = way == JConstant.LOCALE_SIMPLE_CN ? JConstant.REGISTER_BY_PHONE : JConstant.REGISTER_BY_EMAIL;
        if (registerWay == JConstant.REGISTER_BY_PHONE) {
            //中国大陆
            tvRegisterSubmit.setText(ContextUtils.getContext().getString(R.string.GET_CODE));
        } else {
            //只显示邮箱注册
            etRegisterInputBox.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            etRegisterInputBox.setHint(ContextUtils.getContext().getString(R.string.EMAIL));
            ViewUtils.setTextViewMaxFilter(etRegisterInputBox, 65);
        }
    }

    @OnFocusChange(R.id.et_login_username)
    public void onUserNameLoseFocus(View view, boolean focus) {
        Log.d(TAG, "onUserNameLoseFocus: " + focus);
        final boolean visibility = !TextUtils.isEmpty(etLoginUsername.getText()) && focus;
        ivLoginClearUsername.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
    }

    @OnFocusChange(R.id.et_login_pwd)
    public void onPwdLoseFocus(View view, boolean focus) {
        Log.d(TAG, "onPwdLoseFocus: " + focus);
        final boolean visibility = !TextUtils.isEmpty(etLoginPwd.getText()) && focus;
        ivLoginClearPwd.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 显示当前的布局
     */
    private void showLayout() {
        showAllLayout(false);
    }

    /**
     * 动画的表现方式
     *
     * @param orientation true 为垂直方向展现动画，false为水平方向展现动画
     */
    private void showAllLayout(boolean orientation) {
        AnimatorUtils.onSimpleBounceUpIn(vsLayoutSwitcher, 1000, 20);
        AnimatorUtils.onSimpleBounceUpIn(rLayoutLoginThirdParty, 200, 400);
    }

    /**
     * 初始化view
     */
    private void initView() {
        fLayoutThird.setVisibility(getResources().getBoolean(R.bool.show_third_login) ? View.VISIBLE : View.GONE);
        lLayoutAgreement.setVisibility(getResources().getBoolean(R.bool.show_agreement) ? View.VISIBLE : View.GONE);
        ViewUtils.setChineseExclude(etLoginUsername, 65);
        ViewUtils.setChineseExclude(etLoginPwd, getResources().getInteger(R.integer.max_password_length));
        ViewUtils.setChineseExclude(etRegisterInputBox, LocaleUtils.getLanguageType(getActivity()) == JConstant.LOCALE_SIMPLE_CN ? getResources().getInteger(R.integer.max_password_length) : 65);
        ViewUtils.setChineseExclude(etVerificationInput, 6);
        rLayoutLoginToolbar.setBackAction(v -> getActivity().getSupportFragmentManager().popBackStack());
        tvAgreement.setText("《" + getString(R.string.TERM_OF_USE) + "》");
        if (getView() != null) {
            getView().findViewById(R.id.tv_toolbar_right).setVisibility(View.VISIBLE);
        }
//        ViewUtils.setChineseExclude(etLoginPwd, JConstant.PWD_LEN_MAX);
        //大陆用户显示 第三方登陆
        rLayoutLoginThirdParty.setVisibility(LocaleUtils.getLanguageType(getActivity()) == JConstant.LOCALE_SIMPLE_CN ? View.VISIBLE : View.GONE);
        rLayoutLoginThirdPartyAbroad.setVisibility(LocaleUtils.getLanguageType(getActivity()) == JConstant.LOCALE_SIMPLE_CN ? View.GONE : View.VISIBLE);
        etLoginUsername.setHint(LocaleUtils.getLanguageType(getActivity()) == JConstant.LOCALE_SIMPLE_CN
                ? getString(R.string.SHARE_E_MAIL) : getString(R.string.EMAIL));


        if (!TextUtils.isEmpty(etLoginUsername.getText().toString().trim()) && !TextUtils.isEmpty(etLoginPwd.getText().toString().trim())) {
            lbLogin.setEnabled(true);
        }

        if (!TextUtils.isEmpty(etRegisterInputBox.getText().toString().trim())) {
            tvRegisterSubmit.setEnabled(JConstant.PHONE_REG.matcher(etRegisterInputBox.getText().toString().trim()).find());
        }
        presenter.reShowAccount();
    }

    /**
     * 明文/密文 密码
     *
     * @param buttonView
     * @param isChecked
     */
    @OnCheckedChanged(R.id.cb_show_pwd)
    public void onShowPwd(CompoundButton buttonView, boolean isChecked) {
        ViewUtils.showPwd(etLoginPwd, isChecked);
        etLoginPwd.setSelection(etLoginPwd.length());
    }

    /**
     * 密码变化
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @OnTextChanged(R.id.et_login_pwd)
    public void onPwdChange(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        ivLoginClearPwd.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        if (flag || s.length() < 6) {
            lbLogin.setEnabled(false);
        } else if (!TextUtils.isEmpty(ViewUtils.getTextViewContent(etLoginUsername))) {
            lbLogin.setEnabled(true);
        }
    }

    /***
     * 账号变化
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @OnTextChanged(R.id.et_login_username)
    public void onUserNameChange(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        ivLoginClearUsername.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        final String pwd = ViewUtils.getTextViewContent(etLoginPwd);
        if (flag) {
            lbLogin.setEnabled(false);
        } else if (!TextUtils.isEmpty(pwd) && pwd.length() >= 6) {
            lbLogin.setEnabled(true);
        }
    }

    @OnClick({
            R.id.tv_qqLogin_commit,
            R.id.tv_xlLogin_commit,
            R.id.iv_login_clear_pwd,
            R.id.iv_login_clear_username,
            R.id.tv_login_forget_pwd,
            R.id.tv_twitterLogin_commit,
            R.id.tv_facebookLogin_commit,
            R.id.tv_toolbar_icon,
            R.id.tv_toolbar_right,
            R.id.tv_agreement,
            R.id.rLayout_login,
            R.id.rLayout_register_box,
            R.id.layout_login
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_login:
            case R.id.rLayout_register_box:
            case R.id.rLayout_login:
                if (getActivity() != null) {
                    IMEUtils.hide(getActivity());
                }
                break;
            case R.id.iv_login_clear_pwd:
                etLoginPwd.getText().clear();
                break;
            case R.id.iv_login_clear_username:
                etLoginUsername.getText().clear();
                break;
            case R.id.tv_login_forget_pwd:
                forgetPwd();
                break;
            case R.id.tv_qqLogin_commit:
                if (TextUtils.equals(NetUtils.getNetName(getActivity()), "offLine") || NetUtils.getJfgNetType(getActivity()) == -1) {
                    ToastUtil.showToast(ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                presenter.performAuthentication(3);
                break;
            case R.id.tv_xlLogin_commit:
                if (TextUtils.equals(NetUtils.getNetName(getActivity()), "offLine") || NetUtils.getJfgNetType(getActivity()) == -1) {
                    ToastUtil.showToast(ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                //新功能了 http://120.24.247.124/jfgou/1.3.0/#g=1&p=android微博登录授权
                if (!PackageUtils.isAppInstalled(getContext().getApplicationContext().getPackageManager(),
                        "com.sina.weibo")) {
                    ToastUtil.showToast(ContextUtils.getContext().getString(R.string.Tap0_Login_NoInstalled, getString(R.string.Weibo)));
                    return;
                }
                presenter.performAuthentication(4);
                break;
            case R.id.tv_toolbar_icon:
                IMEUtils.hide(getActivity());
                if (getActivity() != null && getActivity() instanceof SmartcallActivity) {
                    getActivity().finish();
                } else if (getActivity() != null && getActivity() instanceof NewHomeActivity) {
                    getActivity().onBackPressed();
                }
                break;
            case R.id.tv_toolbar_right:
                switchBox();
                break;
            case R.id.tv_agreement: {
                IMEUtils.hide(getActivity());
                AgreementFragment fragment = AgreementFragment.getInstance(null);
                ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                        fragment, android.R.id.content);
            }
            break;
            case R.id.tv_twitterLogin_commit:
                if (TextUtils.equals(NetUtils.getNetName(getActivity()), "offLine") || NetUtils.getJfgNetType(getActivity()) == -1) {
                    ToastUtil.showToast(ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                presenter.performAuthentication(6);
                break;

            case R.id.tv_facebookLogin_commit:
                if (TextUtils.equals(NetUtils.getNetName(getActivity()), "offLine") || NetUtils.getJfgNetType(getActivity()) == -1) {
                    ToastUtil.showToast(ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                presenter.performAuthentication(7);
                break;
        }
    }


    /**
     * 页面切换
     */
    private void switchBox() {
        final String content = rLayoutLoginToolbar.getTitle().toString();
        if (TextUtils.equals(content, ContextUtils.getContext().getString(R.string.LOGIN))) {
            //register
            rLayoutLoginToolbar.setToolbarTitle(R.string.Tap0_register);
            rLayoutLoginToolbar.setToolbarRightTitle(R.string.LOGIN);
            vsLayoutSwitcher.setInAnimation(getContext(), R.anim.slide_in_right_overshoot);
            vsLayoutSwitcher.setOutAnimation(getContext(), R.anim.slide_out_left);
            vsLayoutSwitcher.showNext();
            isRegetCode = false;
        } else if (TextUtils.equals(content, ContextUtils.getContext().getString(R.string.Tap0_register))) {
            rLayoutLoginToolbar.setToolbarTitle(R.string.LOGIN);
            rLayoutLoginToolbar.setToolbarRightTitle(R.string.Tap0_register);
            //延时200ms,
            vsLayoutSwitcher.setInAnimation(getContext(), R.anim.slide_in_left_overshoot);
            vsLayoutSwitcher.setOutAnimation(getContext(), R.anim.slide_out_right);
            vsLayoutSwitcher.showPrevious();
            isRegetCode = false;
//            if (!lLayoutAgreement.isShown())
//                lLayoutAgreement.setVisibility(View.VISIBLE);

        }
    }

    /**
     * 清除焦点。
     */
    private void enableEditTextCursor(boolean enable) {
        if (isResumed() && getActivity() != null) {
            ViewUtils.enableEditTextCursor(etLoginPwd, enable);
            ViewUtils.enableEditTextCursor(etLoginUsername, enable);
        }
    }

    @OnClick({R.id.lb_login_commit})
    public void login(View view) {
        final String netName = NetUtils.getNetName(ContextUtils.getContext());
        if (netName != null && TextUtils.equals(netName, "offLine") || NetUtils.getJfgNetType(getActivity()) == -1) {
            ToastUtil.showToast(ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1));
            return;
        }
//        if (netName != null && netName.contains("DOG"))
//            MiscUtils.recoveryWiFi();

        boolean b = JConstant.PHONE_REG.matcher(etLoginUsername.getText()).find()
                || JConstant.EMAIL_REG.matcher(etLoginUsername.getText()).find();
        if (!b) {
            ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.ACCOUNT_ERR));
            return;
        }

        IMEUtils.hide(getActivity());
        AnimatorUtils.viewAlpha(tvForgetPwd, false, 300, 0);
        AnimatorUtils.viewTranslationY(LocaleUtils.getLanguageType(getActivity()) == JConstant.LOCALE_SIMPLE_CN ? rLayoutLoginThirdParty : rLayoutLoginThirdPartyAbroad, false, 100, 0, 800, 500);
        lbLogin.viewZoomSmall(() -> {
            if (presenter != null && NetUtils.getNetType(ContextUtils.getContext()) != -1) {
                String account = ViewUtils.getTextViewContent(etLoginUsername);
                String password = ViewUtils.getTextViewContent(etLoginPwd);
                presenter.performLogin(account, password);
            }
        });

        enableEditTextCursor(false);
        enableOtherBtn(false);
    }

    /**
     * 直到登陆状态返回
     *
     * @param enable
     */
    private void enableOtherBtn(boolean enable) {
        rLayoutLoginToolbar.setEnabled(enable);
        cbShowPwd.setEnabled(enable);
    }

    /**
     * 忘记密码
     */
    private void forgetPwd() {
        //忘记密码
        if (getActivity() != null) {
            Bundle bundle = getArguments();
            final int containerId = bundle.getInt(JConstant.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID);
            final String tempAccount = ViewUtils.getTextViewContent(etLoginUsername);
            bundle.putInt(JConstant.KEY_LOCALE, LocaleUtils.getLanguageType(getActivity()));
            bundle.putString(KEY_TEMP_ACCOUNT, tempAccount);
            ForgetPwdFragment forgetPwdFragment = ForgetPwdFragment.newInstance(bundle);
            new ForgetPwdPresenterImpl(forgetPwdFragment);
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                    forgetPwdFragment, containerId);
        }
    }

    @Override
    public boolean isLoginViewVisible() {
        final long time = System.currentTimeMillis();
        boolean notNull = getActivity() != null && getActivity().getWindow().getDecorView() != null;
        if (notNull) {
            View v = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
            if (v != null && v instanceof ViewGroup) {
                final int count = ((ViewGroup) v).getChildCount();
                if (count > 0) {
                    View thisLayout = ((ViewGroup) v).getChildAt(count - 1);
                    //yes this fragment is in top
                    notNull = (thisLayout != null && thisLayout.getId() == R.id.rLayout_login);
                }
            }
        }
        Log.d("perform", "perform: " + (System.currentTimeMillis() - time));
        return notNull;
    }

    @Override
    public void verifyCodeResult(int code) {
        if (!isVisible()) {
            return;
        }
        if (code == JError.ErrorOK) {
            jump2NextPage();
        } else if (code == JError.ErrorSMSCodeTimeout) {
            ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.RET_ESMS_CODE_TIMEOUT));
            if (verificationCodeLogic != null) {
                verificationCodeLogic.initTimer();
            }
        } else {
            ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.Tap0_wrongcode));
        }
    }

    /**
     * 弹框，{fragment}
     */
    private void showSimpleDialog(String title, String lContent, String rContent, boolean dismiss) {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("dialogFragment");
        if (f == null) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, title);
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, lContent);
            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, rContent);
            bundle.putBoolean(SimpleDialogFragment.KEY_TOUCH_OUT_SIDE_DISMISS, dismiss);
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
            dialogFragment.setAction(this);
            dialogFragment.show(getActivity().getSupportFragmentManager(), "dialogFragment");
        }
    }

    /**
     * 登录超时，或者失败后动画复位
     */
    @Override
    public void resetView() {
        lbLogin.viewZoomBig();
        AnimatorUtils.viewAlpha(tvForgetPwd, true, 300, 0);
        AnimatorUtils.viewTranslationY(LocaleUtils.getLanguageType(getActivity()) == JConstant.LOCALE_SIMPLE_CN ? rLayoutLoginThirdParty : rLayoutLoginThirdPartyAbroad, true, 100, 800, 0, 200);
        enableOtherBtn(true);
        enableEditTextCursor(true);
        hideLoading();
    }

    @Override
    public void registerResult(int result) {
        if (result == JError.ErrorAccountAlreadyExist) {
            showSimpleDialog(ContextUtils.getContext().getString(R.string.RET_EREGISTER_PHONE_EXIST), ContextUtils.getContext().getString(R.string.CANCEL), ContextUtils.getContext().getString(R.string.Tap0_register_GoToLogin), false);
        } else if (result == JError.ErrorOK) {
            if (!(getActivity() instanceof NewHomeActivity)) {
                getActivity().finish();
            }
            Intent intent = new Intent(getContext(), NewHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            getContext().startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void switchBox(String account) {
        switchBox();
        final boolean validPhoneNum = JConstant.PHONE_REG.matcher(etRegisterInputBox.getText()).find();
//        if (validPhoneNum) {
//        AppLogger.i("account:" + etRegisterInputBox.getText());
//        etRegisterInputBox.post(new Runnable() {
//            @Override
//            public void run() {
//                if (registerWay == JConstant.REGISTER_BY_PHONE && !validPhoneNum) {
//                    handleRegisterByMail();
//                } else {
//                    //email
//                }
//            }
//        });
//        }
    }

    @Override
    public void updateAccount(final String account) {
        etLoginUsername.post(new Runnable() {
            @Override
            public void run() {
                etLoginUsername.setText(account);
            }
        });
    }

    @Override
    public void loginTimeout() {
        resetView();
        ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.LOGIN_ERR));
    }

    @OnTextChanged(R.id.et_register_input_box)
    public void onRegisterEtChange(CharSequence s, int start, int before, int count) {
        boolean result;
        if (registerWay == JConstant.REGISTER_BY_PHONE) {
            if (etVerificationInput.isShown()) {
                result = JConstant.PHONE_REG.matcher(s).find() && (etVerificationInput.getText().toString().trim().length() == 6);
            } else {
                result = JConstant.PHONE_REG.matcher(s).find();
            }
        } else {
            result = Patterns.EMAIL_ADDRESS.matcher(s).find();
        }
        ivRegisterUserNameClear.setVisibility(!TextUtils.isEmpty(s) ? View.VISIBLE : View.INVISIBLE);
        tvRegisterSubmit.setEnabled(result);
    }

    @OnTextChanged(R.id.et_verification_input)
    public void onRegisterVerificationCodeEtChange(CharSequence s, int start, int before, int count) {
        boolean isValidCode = TextUtils.isDigitsOnly(s) && s.length() == 6;
        tvRegisterSubmit.setEnabled(isValidCode && JConstant.PHONE_REG.matcher(etRegisterInputBox.getText().toString().trim()).find());
    }

    /**
     * 在跳转之前，做一些清理工作
     */
    private void clearSomeThing() {
        if (verificationCodeLogic != null) {
            verificationCodeLogic.stop();
            verificationCodeLogic = null;
        }
    }

    /**
     * 校验 验证码
     */
    private void verifyCode() {
        if (presenter != null) {
            presenter.verifyCode(ViewUtils.getTextViewContent(etRegisterInputBox),
                    ViewUtils.getTextViewContent(etVerificationInput),
                    PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
        }
    }

    /**
     * 手机号和验证码是否准备,或者注册类型{手机，邮箱}
     *
     * @return
     */
    @Override
    public void jump2NextPage() {
//        clearSomeThing();
        //to set up pwd
        Bundle bundle = getArguments();
        if (getActivity() != null && bundle != null) {
            final int containerId = bundle.getInt(JConstant.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID);
            bundle.putString(JConstant.KEY_ACCOUNT_TO_SEND, ViewUtils.getTextViewContent(etRegisterInputBox));
            bundle.putString(JConstant.KEY_PWD_TO_SEND, ViewUtils.getTextViewContent(etRegisterInputBox));
            bundle.putString(JConstant.KEY_VCODE_TO_SEND, ViewUtils.getTextViewContent(etVerificationInput));
            bundle.putInt(JConstant.KEY_SET_UP_PWD_TYPE, 1);
            RegisterPwdFragment fragment = RegisterPwdFragment.newInstance(bundle);
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                    fragment, containerId);
        }
    }

    /**
     * 是否已注册结果
     *
     * @param callback
     */
    @Override
    public void checkAccountResult(RxEvent.CheckRegisterBack callback) {
        if (callback.jfgResult.code != 0) {
            boolean validPhoneNum = JConstant.PHONE_REG.matcher(ViewUtils.getTextViewContent(etRegisterInputBox)).find();
            registerWay = validPhoneNum ? JConstant.REGISTER_BY_PHONE : JConstant.REGISTER_BY_EMAIL;

            if (registerWay == JConstant.REGISTER_BY_EMAIL) {
                jump2NextPage();
                AppLogger.d("jump_time:" + System.currentTimeMillis());
                return;
            }
            int codeLen = ViewUtils.getTextViewContent(etVerificationInput).length();
            boolean validCode = codeLen == JConstant.VALID_VERIFICATION_CODE_LEN;

            if (isRegetCode) {
                //重新获取验证码也要检测一下账号
                if (presenter.checkOverCount(ViewUtils.getTextViewContent(etRegisterInputBox))) {
                    ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.GetCode_FrequentlyTips));
                    isRegetCode = false;
                    return;
                }

                if (verificationCodeLogic != null) {
                    verificationCodeLogic.start();
                }
                if (presenter != null) {
                    presenter.getCodeByPhone(ViewUtils.getTextViewContent(etRegisterInputBox));
                }
                tempPhone = ViewUtils.getTextViewContent(etRegisterInputBox);
                isRegetCode = false;
            } else if (fLayoutVerificationCodeInputBox.isShown() && validCode) {
                //第二次检测账号是否注册返回执行获取校验验证码
                if (TextUtils.equals(tempPhone, ViewUtils.getTextViewContent(etRegisterInputBox))) {
                    verifyCode();
                } else {
                    ToastUtil.showToast(ContextUtils.getContext().getString(R.string.RET_ESMS_CODE_TIMEOUT));
                }

            } else {
                //第一次检测账号是否注册返回执行获取验证码
                if (presenter.checkOverCount(ViewUtils.getTextViewContent(etRegisterInputBox))) {
                    ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.GetCode_FrequentlyTips));
                    return;
                }
                if (verificationCodeLogic == null) {
                    verificationCodeLogic = new VerificationCodeLogic(tvMeterGetCode);
                }
                verificationCodeLogic.start();
                presenter.getCodeByPhone(ViewUtils.getTextViewContent(etRegisterInputBox));
                tempPhone = ViewUtils.getTextViewContent(etRegisterInputBox);
                //显示验证码输入框
                handleVerificationCodeBox(true);
                tvRegisterSubmit.setText(ContextUtils.getContext().getString(R.string.CARRY_ON));
                tvRegisterSubmit.setEnabled(false);
                lLayoutAgreement.setVisibility(View.GONE);
            }

        } else {
            ToastUtil.showToast(ContextUtils.getContext().getString(R.string.RET_EREGISTER_PHONE_EXIST));
        }

    }

    //回显
    @Override
    public void reShowAccount(String account) {
        if (JConstant.EMAIL_REG.matcher(account).find() || JConstant.PHONE_REG.matcher(account).find()) {
            etLoginUsername.setText(account);
        } else {
            etLoginUsername.setText("");
        }
    }


    /**
     * 验证码输入框
     *
     * @param show
     */
    private void handleVerificationCodeBox(boolean show) {
        etVerificationInput.setText("");
        fLayoutVerificationCodeInputBox.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 注册块，确认按钮逻辑。
     */
    private void handleRegisterConfirm() {
        final boolean validPhoneNum = JConstant.PHONE_REG.matcher(ViewUtils.getTextViewContent(etRegisterInputBox)).find();
        registerWay = validPhoneNum ? JConstant.REGISTER_BY_PHONE : JConstant.REGISTER_BY_EMAIL;
        if (registerWay == JConstant.REGISTER_BY_PHONE) {
            IMEUtils.hide(getActivity());
        } else {
            final boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(ViewUtils.getTextViewContent(etRegisterInputBox)).find();
            if (!isValidEmail) {
                Toast.makeText(getActivity(), ContextUtils.getContext().getString(R.string.EMAIL_2), Toast.LENGTH_SHORT).show();
                return;
            }
            if (fLayoutVerificationCodeInputBox.isShown()) {
                ToastUtil.showToast(ContextUtils.getContext().getString(R.string.PHONE_NUMBER_2));
                return;
            }
        }
        //检测账号是否注册
        if (presenter != null) {
            presenter.checkAccountIsReg(ViewUtils.getTextViewContent(etRegisterInputBox));
        }
    }

    private void handleRegisterByMail() {
        lLayoutAgreement.setVisibility(getResources().getBoolean(R.bool.show_agreement) ? View.VISIBLE : View.GONE);
        if (registerWay == JConstant.REGISTER_BY_PHONE) {
            tvRegisterWayContent.setText(ContextUtils.getContext().getString(R.string.PHONE_SIGNUP));
            etRegisterInputBox.setText("");
            etRegisterInputBox.setHint(ContextUtils.getContext().getString(R.string.EMAIL_1));
            etRegisterInputBox.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            //设置长度
            ViewUtils.setChineseExclude(etRegisterInputBox, 65);
            registerWay = JConstant.REGISTER_BY_EMAIL;
            tvRegisterSubmit.setText(ContextUtils.getContext().getString(R.string.CARRY_ON));
            handleVerificationCodeBox(false);
        } else if (registerWay == JConstant.REGISTER_BY_EMAIL) {
            tvRegisterWayContent.setText(ContextUtils.getContext().getString(R.string.EMAIL_SIGNUP));
            etRegisterInputBox.setText("");
            etRegisterInputBox.setHint(ContextUtils.getContext().getString(R.string.PHONE_NUMBER_1));
            etRegisterInputBox.setInputType(EditorInfo.TYPE_CLASS_PHONE);
            ViewUtils.setTextViewMaxFilter(etRegisterInputBox, 11);
            ViewUtils.setChineseExclude(etRegisterInputBox, 11);
            ViewUtils.setChineseExclude(etVerificationInput, 6);
            registerWay = JConstant.REGISTER_BY_PHONE;
            tvRegisterSubmit.setText(ContextUtils.getContext().getString(R.string.GET_CODE));
        }
    }

    /**
     * 控件抖动
     */
    private void toBeStar() {
        AnimatorUtils.onSimpleTangle(400, 10, tvRegisterSubmit);
        AnimatorUtils.onSimpleTangle(400, 10, etRegisterInputBox);
    }

    @OnClick({R.id.tv_meter_get_code,
            R.id.tv_register_submit,
            R.id.tv_register_way_content,
            R.id.iv_register_username_clear})
    public void onClickRegister(View view) {
        switch (view.getId()) {
            case R.id.tv_meter_get_code:
                final boolean validPhoneNum = JConstant.PHONE_REG.matcher(ViewUtils.getTextViewContent(etRegisterInputBox)).find();
                if (!validPhoneNum) {
                    ToastUtil.showToast(ContextUtils.getContext().getString(R.string.PHONE_NUMBER_2));
                    return;
                }
                isRegetCode = true;
                if (presenter != null) {
                    presenter.checkAccountIsReg(ViewUtils.getTextViewContent(etRegisterInputBox));
                }
                break;
            case R.id.tv_register_submit:
                if (NetUtils.getNetType(getContext()) == -1) {
                    ToastUtil.showToast(ContextUtils.getContext().getString(R.string.OFFLINE_ERR_1));
                    return;
                }
                handleRegisterConfirm();
                break;
            case R.id.tv_register_way_content:
                handleRegisterByMail();
                toBeStar();
                break;
            case R.id.iv_register_username_clear:
                etRegisterInputBox.setText("");
                break;
        }
    }

    @Override
    public void onDialogAction(int id, Object value) {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("dialogFragment");
        if (f != null && f.isVisible()) {
            ((SimpleDialogFragment) f).dismiss();
        }
        if (id == R.id.tv_dialog_btn_right) {
            etLoginPwd.setText("");
            final boolean validPhoneNum = JConstant.PHONE_REG.matcher(etRegisterInputBox.getText()).find();
            switchBox();
            if (!validPhoneNum) {
                //已经有RegisterPwdFragment，先popStack
                Fragment fragment = getActivity()
                        .getSupportFragmentManager()
                        .findFragmentByTag("RegisterPwdFragment");
                if (fragment != null && fragment.isVisible()) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
            etRegisterInputBox.post(new Runnable() {
                @Override
                public void run() {
                    etLoginUsername.setText(etRegisterInputBox.getText());
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearSomeThing();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    /**
     * 验证码
     */
    private static class VerificationCodeLogic {
        WeakReference<TextView> viewWeakReference;
        CountDownTimer timer;

        public VerificationCodeLogic(TextView textView) {
            this.viewWeakReference = new WeakReference<>(textView);
            initTimer();
        }

        private void start() {
            if (timer == null) {
                initTimer();
            }
            if (this.viewWeakReference.get() != null) {
                viewWeakReference.get().setEnabled(false);
                timer.start();
            }
        }

        public void stop() {
            timer.cancel();
            if (this.viewWeakReference.get() != null) {
                this.viewWeakReference.get().setText("");
            }

        }

        public void reset() {
            if (timer != null) {
                timer.cancel();
            }
            if (this.viewWeakReference.get() != null) {
                this.viewWeakReference.get().setText(
                        ContextUtils.getContext().getString(R.string.Button_ReObtain));
            }
        }

        private void initTimer() {
            timer = new CountDownTimer(JConstant.VERIFICATION_CODE_DEADLINE, 1000L) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (viewWeakReference.get() == null) {
                        return;
                    }
                    final String content = millisUntilFinished / 1000 + "s";
                    viewWeakReference.get().setText(content);
                    viewWeakReference.get().setEnabled(false);
                }

                @Override
                public void onFinish() {
                    if (viewWeakReference.get() == null) {
                        return;
                    }
                    TextView tv = viewWeakReference.get();
                    tv.setText(ContextUtils.getContext().getString(R.string.Button_ReObtain));
                    tv.setEnabled(true);
                }
            };
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    public void showLoading() {
        if (isAdded()) {
            LoadingDialog.showLoading(getActivity(), ContextUtils.getContext().getString(R.string.LOADING), true);
        }
    }

    @Override
    public void hideLoading() {
        if (isAdded() && LoadingDialog.isShowLoading()) {
            LoadingDialog.dismissLoading();
        }
    }

    @Override
    public Activity getActivityContext() {
        return getActivity();
    }

    @Override
    public void onAuthenticationResult(int code) {
        if (!isAdded()) {
            return;
        }
        if (code == 0) {

        } else if (code == -1) {
            ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.Tap0_Authorizationfailed));
            hideLoading();
        } else if (code == 1) {
            ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.Tap3_ShareDevice_DeleteSucces));
            hideLoading();
        }
    }

    @Override
    public void onLoginSuccess() {
        AppLogger.d("onLoginSuccess");
        hideLoading();
        if ((getActivity() instanceof NewHomeActivity)) {
            getActivity().getSupportFragmentManager().popBackStack();
            ((NewHomeActivity) getActivity()).showHomeListFragment();
        } else {
            Intent intent = new Intent(getContext(), NewHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }
    }


    @Override
    public void onAccountNotExist() {
        AppLogger.d("onAccountNotExist");
        ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.RET_ELOGIN_ACCOUNT_NOT_EXIST));
    }

    @Override
    public void onInvalidPassword() {
        AppLogger.d("onInvalidPassword");
        ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.RET_ELOGIN_ERROR));
    }

    @Override
    public void onOpenLoginInvalidToken() {
        AppLogger.d("onOpenLoginInvalidToken");
        ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.LOGIN_ERR) + ":162");
    }

    @Override
    public void onConnectError() {
        AppLogger.d("onConnectError");
        ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.NoNetworkTips));
    }

    @Override
    public void onLoginFailed(int errorCode) {
        AppLogger.d("onLoginFailed");
        ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.LOGIN_ERR) + ":" + errorCode);
    }

    @Override
    public void onLoginTimeout() {
        AppLogger.d("onLoginTimeout");
        ToastUtil.showNegativeToast(ContextUtils.getContext().getString(R.string.Tips_Device_TimeoutRetry));
    }
}
