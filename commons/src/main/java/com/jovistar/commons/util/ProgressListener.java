package com.jovistar.commons.util;

public interface ProgressListener {

    void update(Object who, Object what);
    void completed(Object who);
    //public void populate(IDTObject obj, int index);
}
