package com.cylan.jiafeigou.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

/**
 * Created by cylan-hunt on 17-2-18.
 */
public class TimeUtilsTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getTodayStartTime() throws Exception {

    }

    @Test
    public void getTodayEndTime() throws Exception {

    }

    @Test
    public void getSpecificDayEndTime() throws Exception {

    }

    @Test
    public void getSpecificDayStartTime() throws Exception {

    }

    @Test
    public void getMediaPicTimeInString() throws Exception {

    }

    @Test
    public void getMediaVideoTimeInString() throws Exception {

    }

    @Test
    public void getTodayString() throws Exception {

    }

    @Test
    public void getDayString() throws Exception {

    }

    @Test
    public void testGetUptime() throws Exception {
        long time = System.currentTimeMillis() / 1000 - RandomUtils.getRandom(50) * 1000;

        time = 0;
        int temp = (int) time / 60;
        int minute = temp % 60;
        temp = temp / 60;
        int hour = temp % 24;
        temp = temp / 24;
        int day = temp;
        if (day > 0 && hour > 0) {
            System.out.println(String.format(Locale.CANADA, "%1$d天%2$d小时%3$d分", day, hour, minute));
        } else if (hour > 0) {
            System.out.println(String.format(Locale.CANADA, "%1$d小时%2$d分", hour, minute));
        } else {
            System.out.println(String.format(Locale.CANADA, "%1$d分", minute));
        }
    }

    @Test
    public void getDayInMonth() throws Exception {

    }

    @Test
    public void getMM_DD() throws Exception {

    }

    @Test
    public void getHH_MM() throws Exception {

    }

    @Test
    public void getHH_MM_Remain() throws Exception {

    }

    @Test
    public void getTestTime() throws Exception {

    }

    @Test
    public void getSpecifiedDate() throws Exception {

    }

    @Test
    public void startOfDay() throws Exception {

    }

    @Test
    public void isToday() throws Exception {

    }

    @Test
    public void getSuperString() throws Exception {

    }

    @Test
    public void getHomeItemTime() throws Exception {

    }

    @Test
    public void getMonthInYear() throws Exception {

    }

    @Test
    public void getBellRecordTime() throws Exception {

    }

}