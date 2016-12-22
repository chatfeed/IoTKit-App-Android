package com.cylan.jiafeigou.n.view.mine;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoBindMailContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineInfoBineMailPresenterImp;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

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
        builder.setMessage(getString(R.string.RET_EEDITUSERINFO_EMAIL))
                .setPositiveButton(getString(R.string.SURE), new DialogInterface.OnClickListener() {
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
        if (!TextUtils.isEmpty(getEditText())) {
            hideSendReqHint();
            if (getEditText().equals(getUserInfo.jfgAccount.getEmail())) {
                //绑定成功
                ToastUtil.showPositiveToast(getString(R.string.SCENE_SAVED));
                getFragmentManager().popBackStack();
            } else {
                //绑定失败
                ToastUtil.showPositiveToast(getString(R.string.SUBMIT_FAIL));
            }
        }

    }

    /**
     * 拿到账号
     */
    @Override
    public void getUserAccountData(JFGAccount account) {
        userinfo = account;
        if (userinfo != null) {
            if ("".equals(userinfo.getEmail()) || userinfo.getEmail() == null) {
                bindOrChange = true;
            } else {
                bindOrChange = false;
                tvTopTitle.setText(getString(R.string.CHANGE_EMAIL));
            }
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
        if (presenter != null) {
            presenter.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mIvMailBoxBindDisable.setVisibility(View.VISIBLE);
        mIvMailBoxBind.setVisibility(View.GONE);
        mIvMailBoxBindDisable.setClickable(false);
        mIvMailBoxBindDisable.setEnabled(false);
        mIvMailBoxBindDisable.setFocusable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_info_mailbox, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initListener();
        return view;
    }

    private void initPresenter() {
        presenter = new MineInfoBineMailPresenterImp(this);
    }


    @OnTextChanged(R.id.et_mine_personal_information_mailbox)
    public void onEditChange(CharSequence s, int start, int before, int count) {
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
                mailBox = getEditText();
                if (TextUtils.isEmpty(mailBox)) {
                    return;
                } else if (!presenter.checkEmail(mailBox)) {
                    ToastUtil.showToast(getString(R.string.EMAIL_2));
                    return;
                } else {
                    presenter.checkEmailIsBinded(mailBox);
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
            presenter.sendSetAccountReq(getEditText());
        } else {
            // 三方登录 未绑定邮箱和手机号跳转到设置密码界面
            if ("".equals(userinfo.getEmail()) && "".equals(userinfo.getPhone())) {
                jump2SetPasswordFragment(getEditText());
            }
        }
    }

    @Override
    public void showSendReqHint() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.upload));
    }

    @Override
    public void hideSendReqHint() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    /**
     * 获取到输入的内容
     *
     * @return
     */
    @Override
    public String getEditText() {
        return mETMailBox.getText().toString();
    }

    /**
     * 网络状态变化
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1){
            hideSendReqHint();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    /**
     * 跳转到设置密码界面
     */
    private void jump2SetPasswordFragment(String account) {
        //TODO
    }
}
