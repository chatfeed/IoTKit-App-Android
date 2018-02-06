package com.cylan.jiafeigou.module

import android.text.TextUtils
import android.util.Log
import rx.Subscription
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by yanzhendong on 2017/12/5.
 */
object SubscriptionSupervisor {
    private val TAG = SubscriptionSupervisor::class.java.simpleName
    private val subscriptions = ConcurrentHashMap<String, SubscriptionTarget>()
    const val CATEGORY_STOP = "CATEGORY_STOP"
    const val CATEGORY_DESTROY = "CATEGORY_DESTROY"
    const val CATEGORY_DEFAULT = "CATEGORY_DEFAULT"

    @JvmStatic/*存在已知的并发问题*/
    fun subscribe(target: Any, category: String, tag: String, subscription: Subscription) {
        var subscriptionTarget = subscriptions[target.javaClass.name + ":" + target.toString()]
        Log.d(TAG, "subscribe for:target:${target.javaClass.name},category:$category,tag:$tag,subscription:$subscription")
        if (subscriptionTarget == null) {
            subscriptionTarget = SubscriptionTarget()
            subscriptions[target.javaClass.name] = subscriptionTarget!!
        }
        subscriptionTarget.subscribe(category, tag, subscription)
    }

    @JvmStatic
    fun unsubscribe(target: Any, category: String?, tag: String?) {
        unsubscribe(target.javaClass.name + ":" + target.toString(), category, tag)
    }

    @JvmStatic
    fun unsubscribe(target: String, category: String?, tag: String?) {
        subscriptions[target]?.apply {
            Log.d(TAG, "unsubscribe for:target:$target,category:$category,tag:$tag")
            synchronized(SubscriptionSupervisor::class) {
                if (TextUtils.isEmpty(category)) {
                    subscriptions.remove(target)
                }
                unsubscribe(category, tag)
            }
        }
    }

    @JvmStatic
    fun get(target: Any, category: String, tag: String): Subscription? = synchronized(SubscriptionSupervisor::class) {
        subscriptions[target.javaClass.name]?.get(category, tag)
    }

    private class SubscriptionTarget {
        private val subscriptions = ConcurrentHashMap<String, SubscriptionCategory>()

        fun subscribe(category: String, tag: String, subscription: Subscription) {
            var subscriptionCategory = subscriptions[category]
            if (subscriptionCategory == null) {
                subscriptionCategory = SubscriptionCategory()
                subscriptions[category] = subscriptionCategory!!
            }
            subscriptionCategory!!.subscribe(tag, subscription)
        }

        fun unsubscribe(category: String?, tag: String?) {
            subscriptions
                    .filter { TextUtils.isEmpty(category) || TextUtils.equals(it.key, category) }
                    .forEach {
                        if (TextUtils.isEmpty(category)) {
                            subscriptions.remove(it.key)
                        }
                        Log.d(TAG, "unsubscribe for category :$category,tag:$tag,value:${it.value}")
                        it.value.unsubscribe(tag)
                    }
        }

        fun get(category: String, tag: String): Subscription? = subscriptions[category]?.get(tag)
        override fun toString(): String {
            return "SubscriptionTarget(subscriptions=$subscriptions)"
        }
    }

    private class SubscriptionCategory {
        private val subscriptions = ConcurrentHashMap<String, Subscription>()
        fun subscribe(tag: String, subscription: Subscription) {
            val subscription1 = subscriptions[tag]
            if (subscription1 != null) {
                if (!subscription1.isUnsubscribed) {
                    subscription1.unsubscribe()
                }
            }
            subscriptions[tag] = subscription
        }

        fun unsubscribe(tag: String?) {
            subscriptions.filter { TextUtils.isEmpty(tag) || TextUtils.equals(it.key, tag) }
                    .forEach {
                        subscriptions.remove(it.key)
                        val subscription = it.value
                        if (!subscription.isUnsubscribed) {
                            subscription.unsubscribe()
                            Log.d(TAG, "Finally unsubscribe:" + it.key)
                        }
                    }
        }


        fun get(tag: String): Subscription? = subscriptions[tag]
        override fun toString(): String {
            return "SubscriptionCategory(subscriptions=$subscriptions)"
        }
    }

}