package com.hiber.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by qianli.ma on 2017/11/28 0028.
 */

public class DeepCloneBean implements Cloneable, Serializable {

    //深克隆
    public <T> T deepClone() {
        T stu = null;
        try {
            stu = null;
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bo);
            oos.writeObject(this);
            ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
            ObjectInputStream oi = new ObjectInputStream(bi);
            stu = (T) oi.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stu;
    }
}
