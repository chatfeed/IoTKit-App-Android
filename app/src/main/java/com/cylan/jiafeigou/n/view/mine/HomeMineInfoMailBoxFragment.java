package com.cylan.jiafeigou.n.view.mine;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoBindMailContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineInfoBineMailPresenterImp;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/10 15:37
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMineInfoMailBoxFragment extends Fragment implements MineInfoBindMailContract.View {

    @BindView(R.id.iv_mine_personal_information_mailbox)
    ImageView mIvMailBox;

    @BindView(R.id.view_mine_personal_information_mailbox)
    View mViewMailBox;

    @BindView(R.id.et_mine_personal_information_mailbox)
    EditText mETMailBox;

    @BindView(R.id.iv_mine_personal_mailbox_bind)
    ImageView mIvMailBoxBind;

    @BindView(R.id.iv_mine_personal_mailbox_bind_disable)
    ImageView mIvMailBoxBindDisable;
    @BindView(R.id.tv_top_title)
    TextView tvTopTitle;
    @BindView(R.id.rl_send_pro_hint)
    RelativeLayout rlSendProHint;

    private String mailBox;
    private MineInfoBindMailContract.Presenter presenter;

    private OnBindMailBoxListener onBindMailBoxListener;
    private JFGAccount userinfo;

    private boolean bindOrChange = false;       //绑定或者修改邮箱

    @Override
    public void setPresenter(MineInfoBindMailContract.Presenter presenter) {

    }

    @Override
    public void showMailHasBindDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("该邮箱已经被绑定")
                .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 显示请求发送结果
     */
    @Override
    public void showSendReqResult(RxEvent.GetUserInfo getUserInfo) {
//        if (onBindMailBoxListener != null) {
//            onBindMailBoxListener.mailBoxChange(mailBox);
//            String mailBoxText = PreferencesUtils.getString(userinfo.getAccount() + "邮箱");
//            mETMailBox.setText(mailBoxText);
//            IMEUtils.hide((Activity) getContext());
//            getFragmentManager().popBackStack();
//        }

        hideSendReqHint();
        if (!"".equals(getUserInfo.jfgAccount.getEmail())) {
            //绑定成功
            ToastUtil.showPositiveToast("绑定成功");
            getFragmentManager().popBackStack();
        } else {
            //绑定失败
            ToastUtil.showPositiveToast("绑定失败");
        }

    }


    public void getArgumentData() {
        Bundle arguments = getArguments();
        userinfo = (JFGAccount) arguments.getSerializable("userinfo");

        if ("".equals(userinfo.getEmail()) || userinfo.getEmail() == null) {
            bindOrChange = true;
        } else {
            bindOrChange = false;
            tvTopTitle.setText("修改邮箱");
        }

    }

    public interface OnBindMailBoxListener {
        void mailBoxChange(String content);
    }

    public void setListener(OnBindMailBoxListener mListener) {
        this.onBindMailBoxListener = mListener;
    }

    public static HomeMineInfoMailBoxFragment newInstance(Bundle bundle) {
        HomeMineInfoMailBoxFragment fragment = new HomeMineInfoMailBoxFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        String mailBoxText = PreferencesUtils.getString(userinfo.getAccount() + "邮箱");
        mETMailBox.setText(mailBoxText);
        mIvMailBoxBindDisable.setVisibility(View.VISIBLE);
        mIvMailBoxBind.setVisibility(View.GONE);
        mIvMailBoxBindDisable.setClickable(false);
        mIvMailBoxBindDisable.setEnabled(false);
        mIvMailBoxBindDisable.setFocusable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_personal_information_mailbox, container, false);
        ButterKnife.bind(this, view);
        getArgumentData();
        initPresenter();
        initListener();
        return view;
    }

    private void initPresenter() {
        presenter = new MineInfoBineMailPresenterImp(this);
    }

    /**
     * 监听输入框内容的变化
     */
    private void initListener() {
        //设置输入框，不可输入回车
        mETMailBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });

        mETMailBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isEmpty = TextUtils.isEmpty(s);
                mIvMailBox.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                mIvMailBoxBind.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                mIvMailBoxBindDisable.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                mViewMailBox.setBackgroundColor(isEmpty ? getResources().getColor(R.color.color_f2f2f2) : getResources().getColor(R.color.color_36bdff));
                mIvMailBoxBindDisable.setClickable(false);
                mIvMailBoxBindDisable.setEnabled(false);
                mIvMailBoxBind.setClickable(true);
                mIvMailBoxBind.setEnabled(true);
            }
        });

        /**
         * 当输入有内容的时候，点击右侧xx便吧 editText内容清空
         */
        mIvMailBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mETMailBox.setText("");
            }
        });
    }

    @OnClick({R.id.iv_mine_personal_mailbox_back, R.id.iv_mine_personal_mailbox_bind})
    public void onClick(View v) {
        switch (v.getId()) {
            //返回上一个fragment
            case R.id.iv_mine_personal_mailbox_back:
                IMEUtils.hide((Activity) getContext());
                getFragmentManager().popBackStack();
                break;
            //绑定邮箱
            case R.id.iv_mine_personal_mailbox_bind:
                mailBox = mETMailBox.getText().toString();
                if (TextUtils.isEmpty(mailBox)) {
                    return;
                } else if (!presenter.checkEmail(mailBox)) {
                    ToastUtil.showToast("请输入有效邮箱");
                    return;
                } else {
                    if (bindOrChange) {
                        //绑定邮箱
                        presenter.checkEmailIsBinded(mailBox);
                    } else {
                        //修改邮箱
                        userinfo.setEmail(mETMailBox.getText().toString());
                        userinfo.resetFlag();
                        presenter.sendSetAccountReq(userinfo);
                    }
                    presenter.getChangeAccountCallBack();
                }
                break;
        }
    }

    /**
     * 账号未注册过
     */
    @Override
    public void showAccountUnReg() {
        if (presenter.checkAccoutIsPhone(userinfo.getAccount())) {
            //是手机号登录
            userinfo.setEmail(mETMailBox.getText().toString());
            userinfo.resetFlag();
            presenter.sendSetAccountReq(userinfo);
        } else {
            // 三方登录 未绑定邮箱和手机号跳转到设置密码界面
            if ("".equals(userinfo.getEmail()) && "".equals(userinfo.getPhone())) {
                jump2SetPasswordFragment(mETMailBox.getText().toString());
            }
        }
    }

    @Override
    public void showSendReqHint() {
        rlSendProHint.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSendReqHint() {
        rlSendProHint.setVisibility(View.INVISIBLE);
    }

    /**
     * 跳转到设置密码界面
     */
    private void jump2SetPasswordFragment(String account) {
        //TODO
    }
}