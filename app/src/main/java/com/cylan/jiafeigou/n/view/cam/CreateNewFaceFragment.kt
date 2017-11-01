package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.text.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.IMEUtils
import com.cylan.jiafeigou.utils.ToastUtil
import kotlinx.android.synthetic.main.fragment_face_create.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class CreateNewFaceFragment : BaseFragment<CreateFaceContact.Presenter>(), CreateFaceContact.View {
    override fun onFaceNotExistError() {
        AppLogger.w("face_id 不存在 ,创建失败了")
        ToastUtil.showToast("语言包: face_id 不存在,创建失败了!")
    }

    override fun onCreateNewFaceTimeout() {
        AppLogger.w("创建面孔超时了")
        ToastUtil.showToast("创建面孔超时了")
    }

    override fun onCreateNewFaceSuccess(personId: String) {
        AppLogger.w("创建面孔返回值为:$personId")
        ToastUtil.showToast(getString(R.string.PWD_OK_2))
        resultCallback?.invoke(personId)
        fragmentManager.popBackStack()

    }

    override fun onCreateNewFaceError(ret: Int) {
        AppLogger.w("创建面孔失败了")
        ToastUtil.showToast("创建失败了")
    }

    private var imageUrl: String? = null
    var faceId: String? = null
    var resultCallback: ((personId: String) -> Unit)? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_face_create, container, false)
        ButterKnife.bind(this, view)
        return view
    }


    override fun initViewAndListener() {
        super.initViewAndListener()
        faceId = arguments.getString("face_id")
        imageUrl = arguments.getString("image")
        custom_toolbar.setRightAction {
            if (faceId == null || picture == null) {
                AppLogger.w("FaceId :$faceId ,picture:$picture")
            } else {
                IMEUtils.hide(activity)
                presenter.createNewFace(faceId!!, name.text.toString().trim())
            }
        }
        //默认不可点击,需要输入名称后才能点击
        custom_toolbar.setRightEnable(false)
        custom_toolbar.setBackAction {
            sendResultIfNeed()
            fragmentManager.popBackStack()
        }
        name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var empty = TextUtils.isEmpty(s) || TextUtils.isEmpty(s?.trim())
                custom_toolbar.setRightEnable(!empty)
                clear_icon.visibility = if (empty) View.INVISIBLE else View.VISIBLE
            }
        })

        name.filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
            val originWidth = BoringLayout.getDesiredWidth("$dest", name.paint)
            val measuredWidth = name.measuredWidth
            var result = "$source"
            var width = BoringLayout.getDesiredWidth(result, name.paint)

            Log.i(JConstant.CYLAN_TAG, "source:$source,dest:$dest,usedWidth:$originWidth inputWidth:$width,acceptWidth:${name.measuredWidth}")

            while (originWidth + width > measuredWidth) {
                result = result.dropLast(1)
                width = BoringLayout.getDesiredWidth(result, name.paint)
            }
            result
        })

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(picture)
    }

    override fun onDetach() {
        super.onDetach()
        IMEUtils.hide(activity)
    }

    private fun sendResultIfNeed() {

    }

    @OnClick(R.id.clear_icon)
    fun clearInputText() {
        name.setText("")
    }

    companion object {

        fun newInstance(uuid: String, faceId: String, imageUrl: String): CreateNewFaceFragment {
            val fragment = CreateNewFaceFragment()
//            HmacSHA1Signature().computeSignature()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putString("face_id", faceId)
            argument.putString("image", imageUrl)
            fragment.arguments = argument
            return fragment
        }

    }
}