package com.cylan.jiafeigou;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.base.module.BellDevice;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <activity_cloud_live_mesg_video_talk_item href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</activity_cloud_live_mesg_video_talk_item>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }


    public void testMy() {
        JFGDevice jfgDevice = new JFGDevice();
        jfgDevice.pid = 5;
        jfgDevice.alias = "摄像头88";
        jfgDevice.shareAccount = "";
        jfgDevice.uuid = "2000000888";
        BellDevice device = new BellDevice();
        device.setDevice(jfgDevice);
        Random random = new Random();


        DpMsgDefine.DPBellCallRecord record;
        JFGDPMsg msg;
        List<JFGDPMsg> list = new ArrayList<>(100000);
        for (int i = 0; i < 100000; i++) {
            msg = new JFGDPMsg();
            record = new DpMsgDefine.DPBellCallRecord();
            record.duration = random.nextInt(1000);
            record.isOK = random.nextInt(2);
            record.time = random.nextInt(1000000);
            record.type = random.nextInt(5);
            byte[] pack = DpUtils.pack(record);
            msg.id = 401;
            msg.version = random.nextLong();
            msg.packValue = pack;
            list.add(msg);
        }
        Log.e("AAAAA", "开始解析了.......");
        long start = System.currentTimeMillis();
        for (JFGDPMsg jfgdpMsg : list) {
            device.setValue(jfgdpMsg);
        }
        long end = System.currentTimeMillis();
        Log.e("AAAAA", "解析100000条数据共耗时" + (end - start) + "毫秒");
    }
}