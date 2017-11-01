package com.cylan.jiafeigou.n.view.cam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.n.view.cam.item.FaceListHeaderItem
import com.cylan.jiafeigou.n.view.cam.item.FaceListItem
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ToastUtil
import com.github.promeg.pinyinhelper.Pinyin
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.HeaderAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.fragment_facelist.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceListFragment : BaseFragment<FaceListContact.Presenter>(), FaceListContact.View {
    override fun onAuthorizationError() {
        ToastUtil.showToast("语言包:授权失败!")
    }

    private var faceId: String? = null

    override fun onVisitorInformationReady(visitors: List<DpMsgDefine.Visitor>?) {
        visitors?.map {
            FaceListItem().withVisitorInformation(it)
        }?.apply {
            itemAdapter.setNewList(this)
            when {
                itemAdapter.adapterItemCount == 0 -> {
                    empty_view.visibility = View.VISIBLE
                    headerAdapter.clear()
                }
                itemAdapter.adapterItemCount > 0 -> {
                    empty_view.visibility = View.GONE
                    if (headerAdapter.adapterItemCount == 0) {
                        headerAdapter.add(FaceListHeaderItem())
                    }
                }
            }
        }
    }

    override fun onMoveFaceToPersonSuccess(personId: String) {
        AppLogger.w("移动面孔成功了")
        ToastUtil.showToast("语言包:移动面孔成功了!")
        fragmentManager.popBackStack()

        if (targetFragment != null) {
            val intent = Intent()
            intent.putExtra("person_id", personId)
            targetFragment.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        }

        if (resultCallback != null) {
            resultCallback!!.invoke(personId, "todo:还不知道要传多少个参数", "todo:还不知道要传多少个参数")
        }
    }

    override fun onFaceNotExistError() {
        ToastUtil.showToast("语言包:面孔不存在,移动失败")
    }

    override fun onFaceInformationReady(data: List<DpMsgDefine.FaceInformation>) {
        AppLogger.w("onFaceInformationReady")
    }

    lateinit var adapter: FastAdapter<*>
    private lateinit var itemAdapter: ItemAdapter<FaceListItem>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_facelist, container, false)
        return view
    }

    lateinit var layoutManager: LinearLayoutManager

    private lateinit var headerAdapter: HeaderAdapter<FaceListHeaderItem>

    override fun initViewAndListener() {
        super.initViewAndListener()
        account = arguments?.getString("account")
        faceId = arguments?.getString("face_id")
        when (arguments?.getInt("type", TYPE_ADD_TO)) {
            TYPE_ADD_TO -> {
                custom_toolbar.setToolbarLeftTitle(R.string.MESSAGES_IDENTIFY_ADD_BTN)
            }
            TYPE_MOVE_TO -> {
                custom_toolbar.setToolbarLeftTitle(R.string.MESSAGES_FACE_MOVE)
            }
            else -> {
                custom_toolbar.setToolbarLeftTitle(R.string.MESSAGES_IDENTIFY_ADD_BTN)
            }
        }

        adapter = FastAdapter<IItem<*, *>>()

        headerAdapter = HeaderAdapter()
        headerAdapter.withUseIdDistributor(true)

        itemAdapter = ItemAdapter()

        itemAdapter.wrap(headerAdapter.wrap(adapter))

        itemAdapter.withUseIdDistributor(true)

        adapter.withMultiSelect(false)
        adapter.withAllowDeselection(false)
        (adapter as FastAdapter<IItem<*, *>>).withSelectionListener { _, _ ->
            custom_toolbar.setRightEnable(adapter.selections.size > 0)
        }

        BaseApplication.getAppComponent().getCmd().sessionId
        custom_toolbar.setRightEnable(false)
        itemAdapter.withComparator { item1, item2 ->
            val char1 = item1.visitor?.personName?.get(0) ?: '#'
            val char2 = item2.visitor?.personName?.get(0) ?: '#'
            val pinyin1 = Pinyin.toPinyin(if (Pinyin.isChinese(char1)) char1 else '#')
            val pinyin2 = Pinyin.toPinyin(if (Pinyin.isChinese(char2)) char2 else '#')
            val i = pinyin1.compareTo(pinyin2, true)
            return@withComparator when {
                i == 0 -> i
                pinyin1 == "#" -> 1
                pinyin2 == "#" -> -1
                else -> i
            }
        }

        itemAdapter.fastAdapter.withEventHook(object : ClickEventHook<FaceListItem>() {

            override fun onBindMany(viewHolder: RecyclerView.ViewHolder): MutableList<View>? {
                if (viewHolder is FaceListItem.FaceListViewHolder) {
                    return mutableListOf(viewHolder.itemView, viewHolder.radio)
                }
                return null
            }

            override fun onClick(v: View?, position: Int, fastAdapter: FastAdapter<FaceListItem>, item: FaceListItem) {
                if (!item.isSelected) {
                    val selections = fastAdapter.selections
                    if (!selections.isEmpty()) {
                        val selectedPosition = selections.iterator().next()
                        fastAdapter.deselect()
                        fastAdapter.notifyItemChanged(selectedPosition)
                    }
                    fastAdapter.select(position)
                }
            }

        })



        layoutManager = LinearLayoutManager(context)
        face_list_items.adapter = adapter
        face_list_items.layoutManager = layoutManager

        custom_toolbar.setBackAction { fragmentManager.popBackStack() }
        custom_toolbar.setRightAction { moveFaceTo() }

    }

    private fun moveFaceTo() {
        val selections = itemAdapter.fastAdapter.selectedItems
        if (selections != null && selections.size > 0) {
            val item = selections.elementAt(0)
            when {
                item.visitor?.personId == null -> {
                    AppLogger.w("PersonId is null")
                }
                faceId == null -> {
                    AppLogger.w("faceId is null")
                }
                else -> {
                    AppLogger.w("moveFaceToPerson with person id:${item.visitor?.personId},face id:$faceId")
                    presenter.moveFaceToPerson(item.visitor!!.personId!!, faceId!!)
                }
            }
        } else {
            AppLogger.w("Empty To Do")
        }
    }

    val words = arrayOf("普鹤骞", "田惠君", "貊怀玉", "潘鸿信", "士春柔", "阙子璇", "皇甫笑", "妍李颖", "初殷浩旷",
            "!普鹤骞", "@田惠君", "%貊怀玉", "22潘鸿信", "&士春柔", "7979阙子璇", "3皇甫笑", "//妍李颖", "3896初殷浩旷"
    )


    override fun onStart() {
        super.onStart()
//        presenter.loadPersonItems(account!!, uuid)
        presenter.loadPersonItem2()
    }

    var resultCallback: ((a: Any, b: Any, c: Any) -> Unit)? = null

    private var account: String? = null

    companion object {
        const val TYPE_ADD_TO = 1
        const val TYPE_MOVE_TO = 2
        fun newInstance(account: String, uuid: String, faceId: String, type: Int = TYPE_ADD_TO): FaceListFragment {
            val fragment = FaceListFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putInt("type", type)
            argument.putString("account", account)
            argument.putString("face_id", faceId)
            fragment.arguments = argument
            return fragment
        }
    }


}