/*
 * This file is part of HookTool.

 * HookTool is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2024 HChenX
 */
package com.hchen.hooktool.tool;

import static com.hchen.hooktool.helper.TryHelper.run;
import static com.hchen.hooktool.log.LogExpand.getTag;
import static com.hchen.hooktool.log.XposedLog.logI;
import static com.hchen.hooktool.tool.CoreBase.baseCallMethod;
import static com.hchen.hooktool.tool.CoreBase.baseCallStaticMethod;
import static com.hchen.hooktool.tool.CoreBase.baseCallSuperPrivateMethod;
import static com.hchen.hooktool.tool.CoreBase.baseCallSuperStaticPrivateMethod;
import static com.hchen.hooktool.tool.CoreBase.baseFilterConstructor;
import static com.hchen.hooktool.tool.CoreBase.baseFilterMethod;
import static com.hchen.hooktool.tool.CoreBase.baseFindAllConstructor;
import static com.hchen.hooktool.tool.CoreBase.baseFindAllMethod;
import static com.hchen.hooktool.tool.CoreBase.baseFindClass;
import static com.hchen.hooktool.tool.CoreBase.baseFindConstructor;
import static com.hchen.hooktool.tool.CoreBase.baseFindField;
import static com.hchen.hooktool.tool.CoreBase.baseFindMethod;
import static com.hchen.hooktool.tool.CoreBase.baseFirstUnhook;
import static com.hchen.hooktool.tool.CoreBase.baseGetAdditionalStaticField;
import static com.hchen.hooktool.tool.CoreBase.baseGetField;
import static com.hchen.hooktool.tool.CoreBase.baseGetStaticField;
import static com.hchen.hooktool.tool.CoreBase.baseHook;
import static com.hchen.hooktool.tool.CoreBase.baseHookAll;
import static com.hchen.hooktool.tool.CoreBase.baseNewInstance;
import static com.hchen.hooktool.tool.CoreBase.baseRemoveAdditionalStaticField;
import static com.hchen.hooktool.tool.CoreBase.baseSetAdditionalStaticField;
import static com.hchen.hooktool.tool.CoreBase.baseSetField;
import static com.hchen.hooktool.tool.CoreBase.baseSetStaticField;

import com.hchen.hooktool.HCData;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.log.LogExpand;
import com.hchen.hooktool.tool.itool.IMemberFilter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * 核心工具
 *
 * @author 焕晨HChen
 * @noinspection unused
 */
public class CoreTool {
    public static ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

    //------------ 检查指定类是否存在 --------------
    public static boolean existsClass(String clazz) {
        return existsClass(clazz, HCData.getClassLoader());
    }

    public static boolean existsClass(String clazz, ClassLoader classLoader) {
        return findClass(clazz, classLoader).isSuccess();
    }

    // --------- 查找类 -----------
    public static SingleMember<Class<?>> findClass(String name) {
        return findClass(name, HCData.getClassLoader());
    }

    public static SingleMember<Class<?>> findClass(String name, ClassLoader classLoader) {
        return baseFindClass(name, classLoader);
    }

    //------------ 检查指定方法是否存在 --------------
    public static boolean existsMethod(String clazz, String name, Object... objs) {
        return existsMethod(findClass(clazz).getNoReport(), name, objs);
    }

    public static boolean existsMethod(String clazz, ClassLoader classLoader, String name, Object... objs) {
        return existsMethod(findClass(clazz, classLoader).getNoReport(), name, objs);
    }

    public static boolean existsMethod(Class<?> clazz, String name, Object... objs) {
        if (clazz == null || name == null || name.isEmpty() || objs == null) return false;
        return findMethod(clazz, name, objs).isSuccess();
    }

    public static boolean existsAnyMethod(String clazz, String name) {
        return existsAnyMethod(findClass(clazz).getNoReport(), name);
    }

    public static boolean existsAnyMethod(String clazz, ClassLoader classLoader, String name) {
        return existsAnyMethod(findClass(clazz, classLoader).getNoReport(), name);
    }

    public static boolean existsAnyMethod(Class<?> clazz, String name) {
        if (clazz == null || name == null || name.isEmpty()) return false;
        return Arrays.stream(clazz.getDeclaredMethods()).anyMatch(method -> name.equals(method.getName()));
    }

    // ------------ 查找方法 --------------
    public static SingleMember<Method> findMethod(String clazz, String name, Object... objs) {
        return baseFindMethod(findClass(clazz), name, objs);
    }

    public static SingleMember<Method> findMethod(String clazz, ClassLoader classLoader, String name, Object... objs) {
        return baseFindMethod(findClass(clazz, classLoader), name, objs);
    }

    public static SingleMember<Method> findMethod(Class<?> clazz, String name, Object... objs) {
        return baseFindMethod(new SingleMember<>(clazz, null), name, objs);
    }

    public static Method[] findAllMethod(String clazz, String name) {
        return baseFindAllMethod(findClass(clazz), name);
    }

    public static Method[] findAllMethod(String clazz, ClassLoader classLoader, String name) {
        return baseFindAllMethod(findClass(clazz, classLoader), name);
    }

    public static Method[] findAllMethod(Class<?> clazz, String name) {
        return baseFindAllMethod(new SingleMember<>(clazz, null), name);
    }

    //------------ 检查指定构造函数是否存在 --------------
    public static boolean existsConstructor(String clazz, Object... objs) {
        return existsConstructor(findClass(clazz).getNoReport(), objs);
    }

    public static boolean existsConstructor(String clazz, ClassLoader classLoader, Object... objs) {
        return existsConstructor(findClass(clazz, classLoader).getNoReport(), objs);
    }

    public static boolean existsConstructor(Class<?> clazz, Object... objs) {
        if (clazz == null || objs == null) return false;
        return findConstructor(clazz, objs).isSuccess();
    }

    // --------- 查找构造函数 -----------
    public static SingleMember<Constructor<?>> findConstructor(String clazz, Object... objs) {
        return baseFindConstructor(findClass(clazz), objs);
    }

    public static SingleMember<Constructor<?>> findConstructor(String clazz, ClassLoader classLoader, Object... objs) {
        return baseFindConstructor(findClass(clazz, classLoader), objs);
    }

    public static SingleMember<Constructor<?>> findConstructor(Class<?> clazz, Object... objs) {
        return baseFindConstructor(new SingleMember<>(clazz, null), objs);
    }

    public static Constructor<?>[] findAllConstructor(String clazz) {
        return baseFindAllConstructor(findClass(clazz));
    }

    public static Constructor<?>[] findAllConstructor(String clazz, ClassLoader classLoader) {
        return baseFindAllConstructor(findClass(clazz, classLoader));
    }

    public static Constructor<?>[] findAllConstructor(Class<?> clazz) {
        return baseFindAllConstructor(new SingleMember<>(clazz, null));
    }

    //------------ 检查指定字段是否存在 --------------
    public static boolean existsField(String clazz, String name) {
        return existsField(findClass(clazz).getNoReport(), name);
    }

    public static boolean existsField(String clazz, ClassLoader classLoader, String name) {
        return existsField(findClass(clazz, classLoader).getNoReport(), name);
    }

    public static boolean existsField(Class<?> clazz, String name) {
        if (clazz == null || name == null || name.isEmpty()) return false;
        return findField(clazz, name).isSuccess();
    }

    // --------- 查找字段 -----------
    public static SingleMember<Field> findField(String clazz, String name) {
        return baseFindField(findClass(clazz), name);
    }

    public static SingleMember<Field> findField(String clazz, ClassLoader classLoader, String name) {
        return baseFindField(findClass(clazz, classLoader), name);
    }

    public static SingleMember<Field> findField(Class<?> clazz, String name) {
        return baseFindField(new SingleMember<>(clazz, null), name);
    }

    // --------- 执行 hook -----------
    // --------- 普通方法 -------------
    public static XC_MethodHook.Unhook hookMethod(String clazz, String method, Object... params) {
        return baseHook(findClass(clazz), method, params);
    }

    public static XC_MethodHook.Unhook hookMethod(String clazz, ClassLoader classLoader, String method, Object... params) {
        return baseHook(findClass(clazz, classLoader), method, params);
    }

    public static XC_MethodHook.Unhook hookMethod(Class<?> clazz, String method, Object... params) {
        return baseHook(new SingleMember<>(clazz, null), method, params);
    }

    public static XC_MethodHook.Unhook[] hookAllMethod(String clazz, String method, IHook iHook) {
        return baseHookAll(findAllMethod(clazz, method), iHook);
    }

    public static XC_MethodHook.Unhook[] hookAllMethod(String clazz, ClassLoader classLoader, String method, IHook iHook) {
        return baseHookAll(findAllMethod(clazz, classLoader, method), iHook);
    }

    public static XC_MethodHook.Unhook[] hookAllMethod(Class<?> clazz, String method, IHook iHook) {
        return baseHookAll(findAllMethod(clazz, method), iHook);
    }

    // --------- 构造函数 ------------
    public static XC_MethodHook.Unhook hookConstructor(String clazz, Object... params) {
        return baseHook(findClass(clazz), null, params);
    }

    public static XC_MethodHook.Unhook hookConstructor(String clazz, ClassLoader classLoader, Object... params) {
        return baseHook(findClass(clazz, classLoader), null, params);
    }

    public static XC_MethodHook.Unhook hookConstructor(Class<?> clazz, Object... params) {
        return baseHook(new SingleMember<>(clazz, null), null, params);
    }

    public static XC_MethodHook.Unhook[] hookAllConstructor(String clazz, IHook iHook) {
        return baseHookAll(findAllConstructor(clazz), iHook);
    }

    public static XC_MethodHook.Unhook[] hookAllConstructor(String clazz, ClassLoader classLoader, IHook iHook) {
        return baseHookAll(findAllConstructor(clazz, classLoader), iHook);
    }

    public static XC_MethodHook.Unhook[] hookAllConstructor(Class<?> clazz, IHook iHook) {
        return baseHookAll(findAllConstructor(clazz), iHook);
    }

    // --------------- Member -----------
    public static XC_MethodHook.Unhook hook(Member member, IHook iHook) {
        return baseFirstUnhook(baseHookAll(new Member[]{member}, iHook));
    }

    public static <T extends Member> XC_MethodHook.Unhook[] hookAll(T[] members, IHook iHook) {
        return baseHookAll(members, iHook);
    }

    // --------- 快捷方法 -----------
    public static IHook returnResult(final Object result) {
        return new IHook() {
            @Override
            public void before() {
                setResult(result);
            }
        };
    }

    public static IHook doNothing() {
        return new IHook() {
            @Override
            public void before() {
                setResult(null);
            }
        };
    }

    // --------- 解除 hook ---------
    public static void unHook(Member member, XC_MethodHook xcMethodHook) {
        XposedBridge.unhookMethod(member, xcMethodHook);
    }

    // --------- 过滤方法 -----------
    public static Method[] filterMethod(String clazz, IMemberFilter<Method> iMemberFilter) {
        return baseFilterMethod(findClass(clazz), iMemberFilter);
    }

    public static Method[] filterMethod(String clazz, ClassLoader classLoader, IMemberFilter<Method> iMemberFilter) {
        return baseFilterMethod(findClass(clazz, classLoader), iMemberFilter);
    }

    public static Method[] filterMethod(Class<?> clazz, IMemberFilter<Method> iMemberFilter) {
        return baseFilterMethod(new SingleMember<>(clazz, null), iMemberFilter);
    }

    public static Constructor<?>[] filterConstructor(String clazz, IMemberFilter<Constructor<?>> iMemberFilter) {
        return baseFilterConstructor(findClass(clazz), iMemberFilter);
    }

    public static Constructor<?>[] filterConstructor(String clazz, ClassLoader classLoader, IMemberFilter<Constructor<?>> iMemberFilter) {
        return baseFilterConstructor(findClass(clazz, classLoader), iMemberFilter);
    }

    public static Constructor<?>[] filterConstructor(Class<?> clazz, IMemberFilter<Constructor<?>> iMemberFilter) {
        return baseFilterConstructor(new SingleMember<>(clazz, null), iMemberFilter);
    }

    // --------- 打印堆栈 ----------
    public static String getStackTrace(boolean autoLog) {
        String task = getStackTrace();
        if (autoLog) logI(getTag(), task);
        return task;
    }

    public static String getStackTrace() {
        return LogExpand.getStackTrace();
    }

    // --------- 耗时检查 -----------
    public static long timeConsumption(Runnable runnable) {
        return run(() -> {
            Instant start = Instant.now();
            runnable.run();
            Instant end = Instant.now();
            return Duration.between(start, end).toMillis();
        }).orErrMag(-1L, "Failed to check code time consumption!");
    }

    // ---------- 非静态 -----------
    public static Object callMethod(Object instance, String name, Object... objs) {
        return baseCallMethod(instance, name, objs);
    }

    public static Object callMethod(Object instance, Method method, Object... objs) {
        return baseCallMethod(instance, method, objs);
    }

    public static Object callSuperPrivateMethod(Object instance, String name, Object... objs) {
        return baseCallSuperPrivateMethod(instance, name, objs);
    }

    public static Object getField(Object instance, String name) {
        return baseGetField(instance, name);
    }

    public static Object getField(Object instance, Field field) {
        return baseGetField(instance, field);
    }

    public static boolean setField(Object instance, String name, Object value) {
        return baseSetField(instance, name, value);
    }

    public static boolean setField(Object instance, Field field, Object value) {
        return baseSetField(instance, field, value);
    }

    public static Object setAdditionalInstanceField(Object instance, String key, Object value) {
        return run(() -> XposedHelpers.setAdditionalInstanceField(instance, key, value))
                .orErrMag(null, "Failed to set additional instance!");
    }

    public static Object getAdditionalInstanceField(Object instance, String key) {
        return run(() -> XposedHelpers.getAdditionalInstanceField(instance, key))
                .orErrMag(null, "Failed to get additional instance!");
    }

    public static Object removeAdditionalInstanceField(Object instance, String key) {
        return run(() -> XposedHelpers.removeAdditionalInstanceField(instance, key))
                .orErrMag(null, "Failed to remove additional instance!");
    }

    // ---------- 静态 ------------
    public static Object newInstance(Class<?> clz, Object... objs) {
        return baseNewInstance(new SingleMember<>(clz, null), objs);
    }

    public static Object newInstance(String clz, Object... objs) {
        return baseNewInstance(findClass(clz), objs);
    }

    public static Object newInstance(String clz, ClassLoader classLoader, Object... objs) {
        return baseNewInstance(findClass(clz, classLoader), objs);
    }

    public static Object callStaticMethod(Class<?> clz, String name, Object... objs) {
        return baseCallStaticMethod(new SingleMember<>(clz, null), null, name, objs);
    }

    public static Object callStaticMethod(String clz, String name, Object... objs) {
        return baseCallStaticMethod(findClass(clz), null, name, objs);
    }

    public static Object callStaticMethod(String clz, ClassLoader classLoader, String name, Object... objs) {
        return baseCallStaticMethod(findClass(clz, classLoader), null, name, objs);
    }

    public static Object callStaticMethod(Method method, Object... objs) {
        return baseCallStaticMethod(null, method, null, objs);
    }

    public static Object callSuperStaticPrivateMethod(String clz, String name, Object... objs) {
        return baseCallSuperStaticPrivateMethod(findClass(clz), name, objs);
    }

    public static Object callSuperStaticPrivateMethod(String clz, ClassLoader classLoader, String name, Object... objs) {
        return baseCallSuperStaticPrivateMethod(findClass(clz, classLoader), name, objs);
    }

    public static Object callSuperStaticPrivateMethod(Class<?> clz, String name, Object... objs) {
        return baseCallSuperStaticPrivateMethod(new SingleMember<>(clz, null), name, objs);
    }

    public static Object getStaticField(Class<?> clz, String name) {
        return baseGetStaticField(new SingleMember<>(clz, null), null, name);
    }

    public static Object getStaticField(String clz, String name) {
        return baseGetStaticField(findClass(clz), null, name);
    }

    public static Object getStaticField(String clz, ClassLoader classLoader, String name) {
        return baseGetStaticField(findClass(clz, classLoader), null, name);
    }

    public static Object getStaticField(Field field) {
        return baseGetStaticField(null, field, null);
    }

    public static boolean setStaticField(Class<?> clz, String name, Object value) {
        return baseSetStaticField(new SingleMember<>(clz, null), null, name, value);
    }

    public static boolean setStaticField(String clz, String name, Object value) {
        return baseSetStaticField(findClass(clz), null, name, value);
    }

    public static boolean setStaticField(String clz, ClassLoader classLoader, String name, Object value) {
        return baseSetStaticField(findClass(clz, classLoader), null, name, value);
    }

    public static boolean setStaticField(Field field, Object value) {
        return baseSetStaticField(null, field, null, value);
    }

    public static Object setAdditionalStaticField(Class<?> clz, String key, Object value) {
        return baseSetAdditionalStaticField(new SingleMember<>(clz, null), key, value);
    }

    public static Object setAdditionalStaticField(String clz, String key, Object value) {
        return baseSetAdditionalStaticField(findClass(clz), key, value);
    }

    public static Object setAdditionalStaticField(String clz, ClassLoader classLoader, String key, Object value) {
        return baseSetAdditionalStaticField(findClass(clz, classLoader), key, value);
    }

    public static Object getAdditionalStaticField(Class<?> clz, String key) {
        return baseGetAdditionalStaticField(new SingleMember<>(clz, null), key);
    }

    public static Object getAdditionalStaticField(String clz, String key) {
        return baseGetAdditionalStaticField(findClass(clz), key);
    }

    public static Object getAdditionalStaticField(String clz, ClassLoader classLoader, String key) {
        return baseGetAdditionalStaticField(findClass(key, classLoader), key);
    }

    public static Object removeAdditionalStaticField(Class<?> clz, String key) {
        return baseRemoveAdditionalStaticField(new SingleMember<>(clz, null), key);
    }

    public static Object removeAdditionalStaticField(String clz, String key) {
        return baseRemoveAdditionalStaticField(findClass(clz), key);
    }

    public static Object removeAdditionalStaticField(String clz, ClassLoader classLoader, String key) {
        return baseRemoveAdditionalStaticField(findClass(clz, classLoader), key);
    }
}
