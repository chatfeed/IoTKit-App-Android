package com.cylan.jiafeigou.support.badge;

import android.text.TextUtils;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.RawTree;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

/**
 * Created by hds on 17-6-13.
 */
@Singleton
public class TreeHelper {

    private TreeNode root;


    public void markNodeRead(String key) {
        if (!TextUtils.isEmpty(key) && RawTree.asRefreshTreeSet.contains(key)) {
            TreeNode node = findTreeNodeByName(key);
            if (node != null) {
                node.setCacheData(new CacheObject().setCount(0).setObject(null));
            }
            PreferencesUtils.putString(key, key);
            return;
        }
        TreeNode node = findTreeNodeByName(key);
        if (node != null) {
            node.setCacheData(new CacheObject().setObject(null).setCount(0));
        }
    }

    public void markNodeReadOnce(String key) {
        if (!TextUtils.isEmpty(key) && RawTree.asRefreshTreeSet.contains(key)) {
            TreeNode node = findTreeNodeByName(key);
            if (node != null) {
                node.setCacheData(new CacheObject().setCount(0).setObject(null));
            }
            return;
        }
        TreeNode node = findTreeNodeByName(key);
        if (node != null) {
            node.setCacheData(new CacheObject().setObject(null).setCount(0));
        }
    }

    public void addTreeNode(String newKey) {
        Iterator<String> iterator = RawTree.treeMap.keySet().iterator();
        while (iterator.hasNext()) {
            final String key = iterator.next();
            final String value = RawTree.treeMap.get(key);
            TreeNode node = new TreeNode();
            node.setNodeName(key);
            if (TextUtils.equals(key, newKey)) {
                boolean needShow = !TextUtils.equals(PreferencesUtils.getString(newKey, null), newKey);
                if (TextUtils.equals(key, "WechatGuideFragment")) {
                    needShow = ContextUtils.getContext().getResources().getBoolean(R.bool.show_wechat_entrance);
                }
                if (needShow) {
                    final String result = PreferencesUtils.getString(key);
                    if (TextUtils.isEmpty(result)) {
                        //新的页面,需要标记红点.
                        node.setCacheData(new CacheObject().setCount(1).setObject(1));
                    }
                }
            }
            node.setParentName(value);
            treeNodeList.add(node);
        }
        initTree(treeNodeList);
        RxBus.getCacheInstance().postSticky(new RxEvent.InfoUpdate());
    }

    public TreeHelper() {
        Iterator<String> iterator = RawTree.treeMap.keySet().iterator();
        while (iterator.hasNext()) {
            final String key = iterator.next();
            final String value = RawTree.treeMap.get(key);
            TreeNode node = new TreeNode();
            node.setNodeName(key);
            if (RawTree.asRefreshTreeSet.contains(key)) {
                boolean needShow = true;
                if (TextUtils.equals(key, "WechatGuideFragment")) {
                    needShow = ContextUtils.getContext().getResources().getBoolean(R.bool.show_wechat_entrance);
                }
                if (needShow) {
                    final String result = PreferencesUtils.getString(key);
                    if (TextUtils.isEmpty(result)) {
                        //新的页面,需要标记红点.
                        node.setCacheData(new CacheObject().setCount(1).setObject(1));
                    }
                }
            }
            node.setParentName(value);
            treeNodeList.add(node);
        }
        initTree(treeNodeList);
        RxBus.getCacheInstance().postSticky(new RxEvent.InfoUpdate());
    }

    private Map<String, TreeNode> treeNodeMap = new HashMap<>();

    private List<TreeNode> treeNodeList = new LinkedList<>();

    /**
     * 这棵树不允许 多个root
     *
     * @param list
     */
    public void initTree(List<TreeNode> list) {
        this.treeNodeList = list;
        putListIntoMap(list);
        putMapIntoTree();
    }

    private void putMapIntoTree() {
        Iterator<String> iterator = treeNodeMap.keySet().iterator();
        while (iterator.hasNext()) {
            final String key = iterator.next();
            TreeNode self = treeNodeMap.get(key);
            TreeNode nodeParent = findParentNodeByName(key);
            if (nodeParent != null && !self.equals(nodeParent)) {
                nodeParent.addNodeToChild(self);
            } else if (self.equals(nodeParent)) {
                //自己啊
            } else {
                System.out.println("node is null? " + key);
            }
        }
    }

    /**
     * 一个Node只有一个parent.
     *
     * @param nodeName
     * @return
     */
    public TreeNode findParentNodeByName(String nodeName) {
        Map<String, TreeNode> nodeMap = new HashMap<>(treeNodeMap);
        final TreeNode node = treeNodeMap.get(nodeName);
        if (node == null) {
            return null;
        }
        nodeMap.remove(nodeName);
        return treeNodeMap.get(node.getParentName());
    }

    /**
     * 通过nodeName来查找自己
     *
     * @param nodeName
     * @return
     */
    public TreeNode findTreeNodeByName(String nodeName) {
        return treeNodeMap.get(nodeName);
    }

    /**
     * 整合到map中.大量数据的时候,map效率高很多.
     *
     * @param list
     */
    private void putListIntoMap(final List<TreeNode> list) {
        final int count = list.size();
        int rootCount = 0;
        for (int i = 0; i < count; i++) {
            final String key = list.get(i).getNodeName();
            final String parentName = list.get(i).getParentName();
            if (key.equals(parentName)) {
                rootCount++;
                setRoot(list.get(i));
            }
            treeNodeMap.put(key, list.get(i));
        }
        if (rootCount > 1) {
            throw new IllegalArgumentException("多个 root");
        }
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

}
