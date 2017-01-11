package com.cylan.jiafeigou.dp;

import android.os.Parcel;
import android.os.Parcelable;

import com.cylan.jiafeigou.support.log.AppLogger;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Ignore;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.TreeSet;

/**
 * Created by cylan-hunt on 16-12-2.
 */

public abstract class DataPoint<T> implements Parcelable, Comparable<DataPoint> {
    @Ignore
    private boolean isNull = false;

    @Ignore
    public boolean isNull() {
        return isNull;
    }


    @Ignore
    private Object instance;

    @Ignore
    public long id;
    @Ignore
    public long version;
    @Ignore
    public long seq;

    @Ignore
    public byte[] toBytes() {
        try {
            MessagePack msgpack = new MessagePack();
            return msgpack.write(this);
        } catch (IOException ex) {
            AppLogger.e("msgpack read byte ex: " + ex.getLocalizedMessage());
            return null;
        }
    }


    public DataPoint() {
    }


    @Ignore
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataPoint value = (DataPoint) o;

        if (id != value.id) return false;
        return version == value.version && seq == value.seq;

    }

    @Ignore
    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }

    @Ignore
    @Override
    public int compareTo(DataPoint another) {
        return version > another.version ? -1 : 1;//降序
    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.version);
        dest.writeLong(this.seq);
    }


    protected DataPoint(Parcel in) {
        this.id = in.readLong();
        this.version = in.readLong();
        this.seq = in.readLong();
    }


    @Ignore
    protected Object getInstance() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    try {
                        instance = this.getClass().newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return instance;
    }


    /**
     * 避免检查空指针,只针对DataPoint,只针对获取值的情,
     * 因为返回的是原对象的一份拷贝,因此对返回的对象进行写入操作不会影响真正的值
     */
    @Ignore
    public T $() {
        Object value;
        for (Field field : getClass().getFields()) {
            try {
                value = field.get(this);
                if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) continue;
                if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) continue;
                if (value == null) {
                    value = field.getType().newInstance();
                    ((DataPoint) value).isNull = true;
                }
                field.set(getInstance(), value);
                if (DpMsgDefine.DPPrimary.class.isAssignableFrom(field.getType())) {
                    DpMsgDefine.DPPrimary primary = (DpMsgDefine.DPPrimary) value;
                    if (primary.value == null) {
                        Class<?> paramType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        primary.value = getPrimaryValue(paramType);
                    }

                } else if (DpMsgDefine.DPSet.class.isAssignableFrom(field.getType())) {
                    DpMsgDefine.DPSet set = (DpMsgDefine.DPSet) value;
                    if (set.value == null) ((DpMsgDefine.DPSet) value).value = new TreeSet();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (getInstance() instanceof DpMsgDefine.DPPrimary) {
            return (T) ((DpMsgDefine.DPPrimary) getInstance()).value;
        } else if (getInstance() instanceof DpMsgDefine.DPSet) {
            return (T) ((DpMsgDefine.DPSet) getInstance()).value;
        }
        return (T) getInstance();
    }

    @Ignore
    private Object getPrimaryValue(Class clz) {
        if (Integer.class.equals(clz)) {
            return 0;
        } else if (String.class.equals(clz)) {
            return "";
        } else if (Byte.class.equals(clz)) {
            return 0;
        } else if (Long.class.equals(clz)) {
            return 0;
        } else if (Double.class.equals(clz)) {
            return 0;
        } else if (Float.class.equals(clz)) {
            return 0;
        } else if (Character.class.equals(clz)) {
            return ' ';
        } else if (Boolean.class.equals(clz)) {
            return false;
        }
        return null;
    }
}
