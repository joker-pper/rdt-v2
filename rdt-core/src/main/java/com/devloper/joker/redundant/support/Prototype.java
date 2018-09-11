package com.devloper.joker.redundant.support;


import java.io.*;

public class Prototype<T> implements Cloneable, Serializable {

    private final T model;

    public T getModel() {
        return model;
    }

    public Prototype() {
        this(null);
    }

    public Prototype(T model) {
        this.model = model;
    }

    public static <T> Prototype<T> of(T model) {
        return new Prototype(model);
    }

    @Override
    public Prototype<T> clone() throws CloneNotSupportedException {
        return (Prototype<T>) super.clone();
    }

    public Prototype<T> deepClone() {
        return deepClone(this);
    }

    /* 深复制 */
    private <T> T deepClone(T obj) {
        if (obj == null) return null;
        ByteArrayOutputStream bos;
        ByteArrayInputStream bis;

        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
         /* 写入当前对象的二进制流 */
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
        /* 读出二进制流产生的新对象 */
            bis = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (IOException e) {
            throw new RuntimeException("clone object failed in io.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("class not found.", e);
        } finally {
            try {
                bos = null;
                bis = null;
                if (oos != null) oos.close();
                if (ois != null) ois.close();
            } catch (IOException e) {
            }
        }
    }
}
