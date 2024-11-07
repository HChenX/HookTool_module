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

 * Copyright (C) 2023-2024 HookTool Contributions
 */
package com.hchen.hooktool.tool.additional;

import static com.hchen.hooktool.log.AndroidLog.logW;
import static com.hchen.hooktool.log.LogExpand.getStackTrace;
import static com.hchen.hooktool.log.LogExpand.getTag;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Parcelable;
import android.os.UserHandle;

import com.hchen.hooktool.data.AppData;
import com.hchen.hooktool.log.AndroidLog;
import com.hchen.hooktool.tool.itool.IPackageInfoGetter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 软件包实用程序
 *
 * @author 焕晨HChen
 */
public class PackagesTool {

    public static boolean isUninstall(String pkg) {
        return isUninstall(getContext(), pkg);
    }

    /**
     * 判断目标包名应用是否已经被卸载。
     */
    public static boolean isUninstall(Context context, String pkg) {
        if (context == null) {
            logW(getTag(), "Context is null, can't check if the app is uninstalled!" + getStackTrace());
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(pkg, PackageManager.MATCH_ALL);
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            AndroidLog.logE(getTag(), e);
            return true;
        }
    }

    public static boolean isDisable(String pkg) {
        return isDisable(getContext(), pkg);
    }

    /**
     * 获取包名应用是否被禁用。
     */
    public static boolean isDisable(Context context, String pkg) {
        if (context == null) {
            logW(getTag(), "Context is null, can't check if an app is disabled!" + getStackTrace());
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo result = packageManager.getApplicationInfo(pkg, 0);
            if (!result.enabled) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return false;
    }

    public static boolean isHidden(String pkg) {
        return isHidden(getContext(), pkg);
    }

    /**
     * 获取包名应用是否被 Hidden，一般来说被隐藏视为未安装，可以使用 isUninstall() 来判断。
     */
    public static boolean isHidden(Context context, String pkg) {
        try {
            if (context == null) {
                logW(getTag(), "Context is null, can't check if an app is hidden!" + getStackTrace());
                return false;
            }
            PackageManager packageManager = context.getPackageManager();
            packageManager.getApplicationInfo(pkg, 0);
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    /**
     * 根据 uid 获取 user id。
     */
    public static int getUserId(int uid) {
        return (int) Optional.ofNullable(
                        InvokeTool.callStaticMethod(UserHandle.class, "getUserId", new Class[]{int.class}, uid))
                .orElse(-1);
    }

    /**
     * 可用于判断是否是系统应用。
     * 如果 app 为 null 则固定返回 false，请注意检查 app 是否为 null。
     */
    public static boolean isSystem(ApplicationInfo app) {
        if (Objects.isNull(app)) {
            AndroidLog.logE(getTag(), "ApplicationInfo is null, can't check if it's a system app!" + getStackTrace());
            return false;
        }
        if (app.uid < 10000) {
            return true;
        }
        return (app.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
    }

    /**
     * 通过自定义代码获取 Package 信息，
     * 支持: PackageInfo, ResolveInfo, ActivityInfo, ApplicationInfo, ProviderInfo. 类型的返回值.
     * 返回使用 return new ArrayList<>(XX); 包裹。
     *
     * @param iCode 需要执行的代码
     * @return ListAppData 包含各种应用详细信息
     * @see #addAppData(Parcelable, PackageManager)
     */
    public static List<AppData> getPackagesByCode(Context context, IPackageInfoGetter iCode) {
        List<AppData> appDataList = new ArrayList<>();
        if (context == null) {
            logW(getTag(), "Context is null, can't get packages by code!" + getStackTrace());
            return appDataList;
        }
        PackageManager packageManager = context.getPackageManager();
        List<Parcelable> parcelables = iCode.packageInfoGetter(packageManager);
        if (parcelables != null) {
            for (Parcelable parcelable : parcelables) {
                try {
                    appDataList.add(addAppData(parcelable, packageManager));
                } catch (Throwable e) {
                    AndroidLog.logE(getTag(), e);
                }
            }
        }
        return appDataList;
    }

    public static List<AppData> getPackagesByCode(IPackageInfoGetter iCode) {
        return getPackagesByCode(getContext(), iCode);
    }

    private static AppData addAppData(Parcelable parcelable, PackageManager pm) {
        AppData appData = new AppData();
        if (parcelable instanceof PackageInfo packageInfo) {
            appData.icon = BitmapTool.drawableToBitmap(packageInfo.applicationInfo.loadIcon(pm));
            appData.label = packageInfo.applicationInfo.loadLabel(pm).toString();
            appData.packageName = packageInfo.applicationInfo.packageName;
            appData.versionName = packageInfo.versionName;
            appData.versionCode = Long.toString(packageInfo.getLongVersionCode());
            appData.isSystemApp = isSystem(packageInfo.applicationInfo);
            appData.enabled = packageInfo.applicationInfo.enabled;
            appData.user = getUserId(packageInfo.applicationInfo.uid);
            appData.uid = packageInfo.applicationInfo.uid;
        } else if (parcelable instanceof ResolveInfo resolveInfo) {
            appData.icon = BitmapTool.drawableToBitmap(aboutResolveInfo(resolveInfo).applicationInfo.loadIcon(pm));
            appData.label = aboutResolveInfo(resolveInfo).applicationInfo.loadLabel(pm).toString();
            appData.packageName = aboutResolveInfo(resolveInfo).applicationInfo.packageName;
            appData.isSystemApp = isSystem(aboutResolveInfo(resolveInfo).applicationInfo);
            appData.enabled = aboutResolveInfo(resolveInfo).applicationInfo.enabled;
            appData.user = getUserId(aboutResolveInfo(resolveInfo).applicationInfo.uid);
            appData.uid = aboutResolveInfo(resolveInfo).applicationInfo.uid;
        } else if (parcelable instanceof ActivityInfo activityInfo) {
            appData.icon = BitmapTool.drawableToBitmap(activityInfo.applicationInfo.loadIcon(pm));
            appData.label = activityInfo.applicationInfo.loadLabel(pm).toString();
            appData.packageName = activityInfo.applicationInfo.packageName;
            appData.isSystemApp = isSystem(activityInfo.applicationInfo);
            appData.enabled = activityInfo.applicationInfo.enabled;
            appData.user = getUserId(activityInfo.applicationInfo.uid);
            appData.uid = activityInfo.applicationInfo.uid;
        } else if (parcelable instanceof ApplicationInfo applicationInfo) {
            appData.icon = BitmapTool.drawableToBitmap(applicationInfo.loadIcon(pm));
            appData.label = applicationInfo.loadLabel(pm).toString();
            appData.packageName = applicationInfo.packageName;
            appData.isSystemApp = isSystem(applicationInfo);
            appData.enabled = applicationInfo.enabled;
            appData.user = getUserId(applicationInfo.uid);
            appData.uid = applicationInfo.uid;
        } else if (parcelable instanceof ProviderInfo providerInfo) {
            appData.icon = BitmapTool.drawableToBitmap(providerInfo.applicationInfo.loadIcon(pm));
            appData.label = providerInfo.applicationInfo.loadLabel(pm).toString();
            appData.packageName = providerInfo.applicationInfo.packageName;
            appData.isSystemApp = isSystem(providerInfo.applicationInfo);
            appData.enabled = providerInfo.applicationInfo.enabled;
            appData.user = getUserId(providerInfo.applicationInfo.uid);
            appData.uid = providerInfo.applicationInfo.uid;
        }
        return appData;
    }

    private static ComponentInfo aboutResolveInfo(ResolveInfo resolveInfo) {
        if (resolveInfo.activityInfo != null) return resolveInfo.activityInfo;
        if (resolveInfo.serviceInfo != null) return resolveInfo.serviceInfo;
        if (resolveInfo.providerInfo != null) return resolveInfo.providerInfo;
        return null;
    }

    private static Context getContext() {
        return ContextTool.getContextNoLog(ContextTool.FLAG_ALL);
    }

    public static class BitmapTool {
        public static Bitmap drawableToBitmap(Drawable drawable) {
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            Bitmap bitmap = Bitmap.createBitmap(w, h, config);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
            return bitmap;
        }

        Bitmap drawable2Bitmap(Drawable drawable) {
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            } else if (drawable instanceof NinePatchDrawable) {
                Bitmap bitmap = Bitmap
                        .createBitmap(
                                drawable.getIntrinsicWidth(),
                                drawable.getIntrinsicHeight(),
                                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                        : Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight());
                drawable.draw(canvas);
                return bitmap;
            } else {
                return null;
            }
        }

        public static byte[] Bitmap2Bytes(Bitmap bm) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        }

        public static Bitmap Bytes2Bimap(byte[] b) {
            if (b.length != 0) {
                return BitmapFactory.decodeByteArray(b, 0, b.length);
            } else {
                return null;
            }
        }
    }
}