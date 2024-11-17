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
package com.hchen.hooktool.tool;

import static com.hchen.hooktool.log.LogExpand.getTag;
import static com.hchen.hooktool.log.XposedLog.logE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 储存获取到的成员列表。
 *
 * @author 焕晨HChen
 */
public class MemberListData<T> {
    private final ArrayList<T> mMemberList;

    public MemberListData() {
        mMemberList = new ArrayList<>();
    }

    public MemberListData(ArrayList<T> memberList) {
        mMemberList = memberList;
    }

    @Nullable
    public T get(int index) {
        try {
            return mMemberList.get(index);
        } catch (Throwable e) {
            logE(getTag(), e);
            return null;
        }
    }

    @Nullable
    public T first() {
        return get(0);
    }

    @NonNull
    public T firstOr(@NonNull T or) {
        T result = get(0);
        if (result == null)
            return or;
        return result;
    }

    public int size() {
        return mMemberList.size();
    }

    public void forEach(Consumer<? super T> action) {
        mMemberList.forEach(action);
    }


    public boolean contains(T o) {
        return mMemberList.contains(o);
    }

    public boolean containsAll(Collection<T> c) {
        return mMemberList.containsAll(c);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return mMemberList.equals(obj);
    }

    @Override
    public int hashCode() {
        return mMemberList.hashCode();
    }

    public boolean isEmpty() {
        return mMemberList.isEmpty();
    }

    public int indexOf(T o) {
        return mMemberList.indexOf(o);
    }

    public int lastIndexOf(T o) {
        return mMemberList.lastIndexOf(o);
    }

    protected Stream<T> stream() {
        return mMemberList.stream();
    }

    protected ArrayList<T> getList() {
        return mMemberList;
    }
}
