package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.login.ForgetPwdContract;
import com.cylan.jiafeigou.n.mvp.impl.PresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

/**
 * 忘记密码
 * Created by lxh on 16-6-14.
 */

public class ForgetPwdFragment extends android.support.v4.app.Fragment implements ForgetPwdContract.View {


    @BindView(R.id.et_forget_username)
    EditText etForgetUsername;
    @BindView(R.id.iv_forget_clear_username)
    ImageView ivForgetClearUsername;
    @BindView(R.id.tv_forget_pwd_submit)
    TextView tvForgetPwdSubmit;
    @BindView(R.id.lLayout_register_input)
    LinearLayout lLayoutRegisterInput;
    @BindView(R.id.et_verification_input)
    EditText etVerificationInput;
    @BindView(R.id.tv_meter_get_code)
    TextView tvMeterGetCode;
    @BindView(R.id.fLayout_verification_code_input_box)
    FrameLayout fLayoutVerificationCodeInputBox;
    @BindView(R.id.et_rst_pwd_input)
    EditText etRstPwdInput;
    @BindView(R.id.cb_show_pwd)
    CheckBox cbShowPwd;
    @BindView(R.id.iv_rst_clear_pwd)
    ImageView ivRstClearPwd;
    @BindView(R.id.rLayout_rst_pwd_input_box)
    FrameLayout rLayoutRstPwdInputBox;
    @BindView(R.id.tv_rst_pwd_submit)
    TextView tvRstPwdSubmit;
    @BindView(R.id.vs_set_account_pwd)
    ViewSwitcher vsSetAccountPwd;

    @BindView(R.id.tv_login_top_center)
    TextView tvLoginTopCenter;
    /**
     * {0}请输入手机号/邮箱 {1}请输入邮箱
     */
    private int acceptType = 0;
    private ForgetPwdContract.Presenter presenter;


    private CountDownTimer countDownTimer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        acceptType = bundle.getInt(JConstant.KEY_LOCALE);
        List<android.support.v4.app.Fragment> fragmentList = getActivity().getSupportFragmentManager().getFragments();
        Log.d("", "" + fragmentList);
        initCountDownTimer();
    }

    private void initCountDownTimer() {
        countDownTimer = new CountDownTimer(10 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final String content = millisUntilFinished / 1000 + "s";
                tvMeterGetCode.setText(content);
            }

            @Override
            public void onFinish() {
                tvMeterGetCode.setText("重新发送");
                tvMeterGetCode.setEnabled(true);
            }
        };

    }

    @Nullable
    @Override
    public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.view.View view = inflater.inflate(R.layout.fragment_forget_pwd, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(android.view.View view, @Nullable Bundle savedInstanceState) {
        initTitleBar();
        initView(view);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (countDownTimer != null)
            countDownTimer.onFinish();
        if (presenter != null)
            presenter.stop();
    }

    private void initTitleBar() {
        FrameLayout layout = (FrameLayout) getView().findViewById(R.id.rLayout_login_top);
        layout.findViewById(R.id.tv_login_top_right).setVisibility(android.view.View.GONE);
        TextView tvTitle = (TextView) layout.findViewById(R.id.tv_login_top_center);
        tvTitle.setText("忘记密码");
        ImageView imgBackHandle = (ImageView) layout.findViewById(R.id.iv_login_top_left);
        imgBackHandle.setImageResource(R.drawable.btn_nav_back);
        imgBackHandle.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                ActivityUtils.justPop(getActivity());
            }
        });
    }

    public static ForgetPwdFragment newInstance(Bundle bundle) {
        ForgetPwdFragment fragment = new ForgetPwdFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    private void initView(android.view.View view) {
        if (acceptType == 1) {
            etForgetUsername.setHint("please input email address");
        }
        Bundle bundle = getArguments();
        if (bundle != null && !TextUtils.isEmpty(bundle.getString(LoginFragment.KEY_TEMP_ACCOUNT))) {
            etForgetUsername.setText(bundle.getString(LoginFragment.KEY_TEMP_ACCOUNT));
        }
    }


    private int checkInputType() {
        final String account = ViewUtils.getTextViewContent(etForgetUsername);
        final boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(account).find();
        if (isEmail) {
            return JConstant.TYPE_EMAIL;
        } else {
            final boolean isPhone = JConstant.PHONE_REG.matcher(account).find();
            if (isPhone) {
                return JConstant.TYPE_PHONE;
            }
            return JConstant.TYPE_INVALID;
        }
    }

    @OnClick(R.id.tv_meter_get_code)
    public void reGetVerificationCode() {
        countDownTimer.start();
        tvMeterGetCode.setEnabled(false);
        if (presenter != null)
            presenter.executeSubmitAccount(ViewUtils.getTextViewContent(etForgetUsername));
    }

    @OnClick(R.id.iv_forget_clear_username)
    public void clearUsername(android.view.View view) {
        etForgetUsername.setText("");
    }

    @OnFocusChange(R.id.et_forget_username)
    public void onEtFocusChange(android.view.View view, boolean focused) {
        ivForgetClearUsername.setVisibility(focused ? android.view.View.VISIBLE : android.view.View.INVISIBLE);
    }

    /**
     * 这里有个动画，就是setVisibility(View.VISIBLE),加上
     * android:animateLayoutChanges="true",就有transition的效果。
     */
    private void start2HandleVerificationCode() {
        fLayoutVerificationCodeInputBox.setVisibility(android.view.View.VISIBLE);
        countDownTimer.start();
    }

    //判读是手机号还是邮箱
    private void next() {
        final int type = checkInputType();
        switch (type) {
            case JConstant.TYPE_INVALID:
                Toast.makeText(getActivity(), "不合法", Toast.LENGTH_SHORT).show();
                enableEditTextCursor(true);
                return;
            case JConstant.TYPE_PHONE:
                if (fLayoutVerificationCodeInputBox.getVisibility() == android.view.View.GONE) {
                    tvLoginTopCenter.setText("忘记密码(手机)");
                    start2HandleVerificationCode();
                    if (presenter != null) {
                        presenter.executeSubmitAccount(ViewUtils.getTextViewContent(etForgetUsername));
                    }
                } else {
                    final String code = ViewUtils.getTextViewContent(etVerificationInput);
                    if (!TextUtils.isEmpty(code) && code.length() != 6) {
                        Toast.makeText(getActivity(), "验证码有错", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(etForgetUsername.getText())) {
                        Toast.makeText(getActivity(), "手机号码为空", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "已发送", Toast.LENGTH_SHORT).show();
                        getArguments().putString(LoginFragment.KEY_TEMP_ACCOUNT, etForgetUsername.getText().toString());
                        countDownTimer.onFinish();
                        presenter.submitPhoneNumAndCode(etForgetUsername.getText().toString(), ViewUtils.getTextViewContent(etVerificationInput));
                    }
                }
                break;
            case JConstant.TYPE_EMAIL:
                Toast.makeText(getActivity(), "已发送", Toast.LENGTH_SHORT).show();
                tvLoginTopCenter.setText("忘记密码(邮箱)");
                if (presenter != null)
                    presenter.executeSubmitAccount(ViewUtils.getTextViewContent(etForgetUsername));
                break;
        }
        IMEUtils.hide(getActivity());
    }

    /**
     * 清除焦点。
     */
    private void enableEditTextCursor(boolean enable) {
        if (isResumed() && getActivity() != null) {
            ViewUtils.enableEditTextCursor(etForgetUsername, enable);
        }
    }

    @OnTextChanged(R.id.et_forget_username)
    public void userNameChange(CharSequence s, int start, int before, int count) {
        final boolean flag = TextUtils.isEmpty(s);
        ivForgetClearUsername.setVisibility(flag ? android.view.View.GONE : android.view.View.VISIBLE);
        boolean codeValid = checkInputType() != JConstant.TYPE_INVALID;
        tvForgetPwdSubmit.setEnabled(!flag && codeValid);
    }

    @OnTextChanged(R.id.et_verification_input)
    public void verificationInputChange(CharSequence s, int start, int before, int count) {
        boolean self = !TextUtils.isEmpty(s) && TextUtils.isDigitsOnly(s);
        boolean user = !TextUtils.isEmpty(etForgetUsername.getText()) && TextUtils.isDigitsOnly(etForgetUsername.getText());
        tvForgetPwdSubmit.setEnabled(self && user);
    }

    @OnClick(R.id.tv_forget_pwd_submit)
    public void forgetPwdCommit(android.view.View v) {
        enableEditTextCursor(false);
        next();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    private void initTitle(final int ret) {
        if (ret == -1)
            return;
        if (ret == JConstant.TYPE_EMAIL)
            tvLoginTopCenter.setText(getString(R.string.EMAIL));
        else if (ret == JConstant.TYPE_PHONE) {
            tvLoginTopCenter.setText("新密码");
        }
    }

    private void prepareMailView() {
        android.view.View view = vsSetAccountPwd.findViewById(R.id.layout_to_be_update);
        if (view != null) {
            vsSetAccountPwd.removeView(view);
        }
        android.view.View mailView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_forget_pwd_by_email, null);
        if (mailView == null) {
            return;
        }
        final String content = String.format(getString(R.string.send_email_tip_content),
                ViewUtils.getTextViewContent(etForgetUsername));
        ((TextView) mailView.findViewById(R.id.tv_send_email_content)).setText(content);
        android.view.View btn = mailView.findViewById(R.id.tv_email_confirm);
        btn.setEnabled(true);
        btn.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Toast.makeText(getActivity(), "yes?", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        vsSetAccountPwd.addView(mailView);
        vsSetAccountPwd.setInAnimation(getContext(), R.anim.slide_in_right_overshoot);
        vsSetAccountPwd.setOutAnimation(getContext(), R.anim.slide_out_left);
        vsSetAccountPwd.showNext();
    }

    @Override
    public void submitResult(RequestResetPwdBean bean) {
        final int ret = bean == null ? -1 : bean.ret;
        switch (ret) {
            case ForgetPwdContract.THIS_ACCOUNT_NOT_REGISTERED:
                Toast.makeText(getContext(), "账号未注册", Toast.LENGTH_SHORT).show();
                break;
            case ForgetPwdContract.AUTHORIZE_MAIL:
                prepareMailView();
//                final String account = etForgetUsername.getText().toString().trim();
//                Bundle bundle = getArguments();
//                bundle.putString(JConstant.KEY_ACCOUNT_TO_SEND, account);
//                getChildFragmentManager().beginTransaction()
//                        .setCustomAnimations(R.anim.slide_right_in, R.anim.slide_out_right
//                                , R.anim.slide_out_right, R.anim.slide_out_right)
//                        .replace(R.id.fLayout_forget_container,
//                                ForgetPwdByEmailFragment.newInstance(getArguments()),
//                                "mailFragment")
//                        .commit();
                break;
            case ForgetPwdContract.AUTHORIZE_PHONE:
                //show timer
                ResetPwdFragment fragment = ResetPwdFragment.newInstance(getArguments());
                getChildFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_right_in, R.anim.slide_out_right
                                , R.anim.slide_out_right, R.anim.slide_out_right)
                        .replace(R.id.fLayout_forget_container,
                                fragment,
                                "rstPwdFragment")
                        .commit();
                new PresenterImpl(fragment);
                break;
        }
    }

    @Override
    public void setPresenter(ForgetPwdContract.Presenter presenter) {
        this.presenter = presenter;
    }
}