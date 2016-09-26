package com.cylan.jiafeigou.n.base;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.util.Log;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.engine.DaemonService;
import com.cylan.jiafeigou.support.DebugOptionsImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.SuperSpUtils;
import com.cylan.utils.HandlerThreadUtils;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by hunt on 16-5-14.
 */
public class BaseApplication extends Application {

    private static final String TAG = "BaseApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        enableDebugOptions();
        startService(new Intent(this, DaemonService.class));
        initLeakCanary();
    }

    private void initLeakCanary() {
        HandlerThreadUtils.post(new Runnable() {
            @Override
            public void run() {
                LeakCanary.install(BaseApplication.this);
            }
        });
    }

    private void enableDebugOptions() {
        DebugOptionsImpl options = new DebugOptionsImpl("test");
        options.enableCrashHandler(this, PathGetter.createPath(JConstant.CRASH_PATH));
        //options.enableStrictMode();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                //should release some resource
                Log.d(TAG, "onTrimMemory: " + level);
                shouldKillBellCallProcess();
                break;
        }
    }

    /**
     * 进入后台，应该杀掉呼叫页面的进程
     */
    private void shouldKillBellCallProcess() {
        final int processId = SuperSpUtils.getInstance(getApplicationContext())
                .getAppPreferences().getInt(JConstant.KEY_BELL_CALL_PROCESS_ID, JConstant.INVALID_PROCESS);
        final int isForeground = SuperSpUtils.getInstance(getApplicationContext())
                .getAppPreferences().getInt(JConstant.KEY_BELL_CALL_PROCESS_IS_FOREGROUND, 0);
        if (processId != JConstant.INVALID_PROCESS && isForeground == 0) {
            AppLogger.d("kill processId: " + processId);
            android.os.Process.killProcess(processId);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory: ");
    }

}
