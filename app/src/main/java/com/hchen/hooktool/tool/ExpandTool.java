package com.hchen.hooktool.tool;

import static com.hchen.hooktool.log.XposedLog.logE;
import static com.hchen.hooktool.log.XposedLog.logW;

import androidx.annotation.Nullable;

import com.hchen.hooktool.utils.DataUtils;

import java.lang.reflect.Field;

import de.robv.android.xposed.XposedHelpers;

public class ExpandTool {
    private final DataUtils utils;

    public ExpandTool(DataUtils dataUtils) {
        utils = dataUtils;
    }

    @Nullable
    public Class<?> findClass(String name) {
        return findClass(name, utils.getClassLoader());
    }

    @Nullable
    public Class<?> findClass(String name, ClassLoader classLoader) {
        try {
            if (classLoader == null) {
                logE(utils.getTAG(), "classLoader is null!");
                return null;
            }
            return XposedHelpers.findClass(name, classLoader);
        } catch (XposedHelpers.ClassNotFoundError e) {
            logE(utils.getTAG(), e);
        }
        return null;
    }

    // ---------- 非静态 -----------

    /**
     * 请使用 new Object[]{} 传入参数。<br/>
     * 如果仅传入一个参数可以不使用 new Object[]{}<br/>
     * 这是为了规避泛型与可变参数的冲突。
     */
    @Nullable
    public <T, R> R callMethod(Object instance, String name, T ts) {
        try {
            return (R) XposedHelpers.callMethod(instance, name, tToObject(ts));
        } catch (Throwable e) {
            logE(utils.getTAG(), "call method: " + e);
        }
        return null;
    }

    @Nullable
    public <R> R callMethod(Object instance, String name) {
        return callMethod(instance, name, new Object[]{});
    }

    @Nullable
    public <T> T getField(Object instance, String name) {
        try {
            return (T) XposedHelpers.getObjectField(instance, name);
        } catch (Throwable e) {
            logE(utils.getTAG(), "get field: " + name);
        }
        return null;
    }

    @Nullable
    public <T> T getField(Object instance, Field field) {
        try {
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (Throwable e) {
            logE(utils.getTAG(), "get field: " + e);
        }
        return null;
    }

    public boolean setField(Object instance, String name, Object key) {
        try {
            XposedHelpers.setObjectField(instance, name, key);
            return true;
        } catch (Throwable e) {
            logE(utils.getTAG(), "set field: " + e);
        }
        return false;
    }

    public boolean setField(Object instance, Field field, Object key) {
        try {
            field.setAccessible(true);
            field.set(instance, key);
            return true;
        } catch (Throwable e) {
            logE(utils.getTAG(), "set field: " + e);
        }
        return false;
    }

    public boolean setAdditionalInstanceField(Object instance, String name, Object key) {
        try {
            XposedHelpers.setAdditionalInstanceField(instance, name, key);
            return true;
        } catch (Throwable e) {
            logE(utils.getTAG(), "set additional: " + name + " e: " + e);
        }
        return false;
    }

    @Nullable
    public <T> T setAdditionalInstanceField(Object instance, String name) {
        try {
            return (T) XposedHelpers.getAdditionalInstanceField(instance, name);
        } catch (Throwable e) {
            logE(utils.getTAG(), "get additional: " + name + " e: " + e);
        }
        return null;
    }

    public boolean removeAdditionalInstanceField(Object instance, String name) {
        try {
            XposedHelpers.removeAdditionalInstanceField(instance, name);
            return true;
        } catch (Throwable e) {
            logE(utils.getTAG(), "remove additional: " + name + " e: " + e);
        }
        return false;
    }


    // ---------- 静态 ------------

    /**
     * 请使用 new Object[]{} 传入参数。<br/>
     * 如果仅传入一个参数可以不使用 new Object[]{}<br/>
     * 这是为了规避泛型与可变参数的冲突。
     */
    @Nullable
    public <T, R> R newInstance(Class<?> clz, T objects) {
        if (clz != null) {
            try {
                return (R) XposedHelpers.newInstance(clz, tToObject(objects));
            } catch (Throwable e) {
                logE(utils.getTAG(), "new instance: " + e);
            }
        } else logW(utils.getTAG(), "class is null, cant new instance.");
        return null;
    }

    @Nullable
    public <R> Object newInstance(Class<?> clz) {
        return newInstance(clz, new Object[]{});
    }

    /**
     * 请使用 new Object[]{} 传入参数。<br/>
     * 如果仅传入一个参数可以不使用 new Object[]{}<br/>
     * 这是为了规避泛型与可变参数的冲突。
     */
    @Nullable
    public <T, R> R callStaticMethod(Class<?> clz, String name, T objs) {
        if (clz != null) {
            try {
                return (R) XposedHelpers.callStaticMethod(clz, name, tToObject(objs));
            } catch (Throwable e) {
                logE(utils.getTAG(), "call static method: " + e);
            }
        } else {
            logW(utils.getTAG(), "class is null, cant call: " + name);
        }
        return null;
    }

    @Nullable
    public <R> R callStaticMethod(Class<?> clz, String name) {
        return callStaticMethod(clz, name, new Object[]{});
    }

    @Nullable
    public <T> T getStaticField(Class<?> clz, String name) {
        if (clz != null) {
            try {
                return (T) XposedHelpers.getStaticObjectField(clz, name);
            } catch (Throwable e) {
                logE(utils.getTAG(), "get static field: " + e);
            }
        } else logW(utils.getTAG(), "class is null, cant get field: " + name);
        return null;
    }

    @Nullable
    public <T> T getStaticField(Field field) {
        try {
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (Throwable e) {
            logE(utils.getTAG(), "get static field: " + e);
        }
        return null;
    }

    public boolean setStaticField(Class<?> clz, String name, Object value) {
        if (clz != null) {
            try {
                XposedHelpers.setStaticObjectField(clz, name, value);
                return true;
            } catch (Throwable e) {
                logE(utils.getTAG(), "set static field: " + e);
            }
        } else logW(utils.getTAG(), "class is null, cant set field: " + name);
        return false;
    }

    public boolean setStaticField(Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(null, value);
            return true;
        } catch (Throwable e) {
            logE(utils.getTAG(), "set static field: " + e);
        }
        return false;
    }

    public boolean setAdditionalStaticField(Class<?> clz, String key, Object value) {
        if (clz != null) {
            try {
                XposedHelpers.setAdditionalStaticField(clz, key, value);
                return true;
            } catch (Throwable e) {
                logE(utils.getTAG(), "set additional static field: " + e);
            }
        } else logW(utils.getTAG(), "class is null, cant additional: " + key);
        return false;
    }

    @Nullable
    public <T> T getAdditionalStaticField(Class<?> clz, String key) {
        if (clz != null) {
            try {
                return (T) XposedHelpers.getAdditionalStaticField(clz, key);
            } catch (Throwable e) {
                logE(utils.getTAG(), "get additional static field: " + e);
            }
        } else logW(utils.getTAG(), "class is null, cant get additional: " + key);
        return null;
    }

    public boolean removeAdditionalStaticField(Class<?> clz, String key) {
        if (clz != null) {
            try {
                XposedHelpers.removeAdditionalStaticField(clz, key);
                return true;
            } catch (Throwable e) {
                logE(utils.getTAG(), "remove additional static field: " + e);
            }
        } else
            logW(utils.getTAG(), "class is null, cant remove additional: " + key);
        return false;
    }

    private <T> Object[] tToObject(T ts) {
        if (ts instanceof Object[] objects) {
            return objects;
        }
        return new Object[]{ts};
    }
}