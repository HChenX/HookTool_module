package com.hchen.hooktool.tool;

import static com.hchen.hooktool.log.XposedLog.logE;
import static com.hchen.hooktool.log.XposedLog.logW;
import static de.robv.android.xposed.callbacks.XCallback.PRIORITY_HIGHEST;

import com.hchen.hooktool.action.Action;
import com.hchen.hooktool.callback.IAction;
import com.hchen.hooktool.callback.IAllAction;
import com.hchen.hooktool.data.MemberData;
import com.hchen.hooktool.data.StateEnum;
import com.hchen.hooktool.utils.DataUtils;

import java.lang.reflect.Member;
import java.util.ArrayList;

import de.robv.android.xposed.XposedBridge;

public class ActionTool {
    private final DataUtils utils;
    private int count = 0;
    private int lastIndex = -1;
    // protected ArrayList<Member> members = null;

    public ActionTool(DataUtils data) {
        this.utils = data;
    }

    /**
     * {@link ActionTool#allAction(int, IAllAction)}
     */
    public MethodTool allAction(IAllAction iAllAction) {
        return allAction(utils.getCount(), iAllAction);
    }

    /**
     * 使用全部回调接口
     */
    public MethodTool allAction(int index, IAllAction iAllAction) {
        return allAction(index, -1, iAllAction);
    }

    /**
     * {@link ActionTool#allAction(int, IAllAction)}
     */
    public MethodTool allAction(int classIndex, int methodIndex, IAllAction iAllAction) {
        if (actionSafe("iAllAction", iAllAction)) {
            hookTool("allAction", classIndex, methodIndex, new IActionTool() {
                @Override
                public Action action(Member member) {
                    return allActionTool(member, iAllAction);
                }
            });
        }
        return utils.getMethodTool();
    }

    /**
     * {@link ActionTool#after(int, IAction)}
     */
    public MethodTool after(IAction iAction) {
        return after(utils.getCount(), iAction);
    }

    /**
     * 使用 after
     */
    public MethodTool after(int index, IAction iAction) {
        return after(index, -1, iAction);
    }


    /**
     * {@link ActionTool#after(int, IAction)}
     */
    public MethodTool after(int classIndex, int methodIndex, IAction iAction) {
        if (actionSafe("iAction after", iAction)) {
            hookTool("after", classIndex, methodIndex, new IActionTool() {
                @Override
                public Action action(Member member) {
                    return afterTool(member, iAction);
                }
            });
        }
        return utils.getMethodTool();
    }

    /**
     * {@link ActionTool#before(int, IAction)}
     */
    public MethodTool before(IAction iAction) {
        return before(utils.getCount(), iAction);
    }

    /**
     * 使用 before
     */
    public MethodTool before(int index, IAction iAction) {
        return before(index, -1, iAction);
    }

    /**
     * {@link ActionTool#before(int, IAction)}
     */
    public MethodTool before(int classIndex, int methodIndex, IAction iAction) {
        if (actionSafe("iAction before", iAction)) {
            hookTool("before", classIndex, methodIndex, new IActionTool() {
                @Override
                public Action action(Member member) {
                    return beforeTool(member, iAction);
                }
            });
        }
        return utils.getMethodTool();
    }


    /**
     * {@link ActionTool#returnResult(int, Object)}
     */
    public void returnResult(final Object result) {
        returnResult(utils.getCount(), result);
    }

    /**
     * 直接返回指定值
     */
    public void returnResult(int index, final Object result) {
        returnResult(index, -1, result);
    }

    /**
     * {@link ActionTool#returnResult(int, Object)}
     */
    public void returnResult(int classIndex, int methodIndex, final Object result) {
        hookTool("returnResult", classIndex, methodIndex, new IActionTool() {
            @Override
            public Action action(Member member) {
                return new Action(utils.getTAG()) {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(result);
                    }
                };
            }
        });
    }

    /**
     * {@link ActionTool#doNothing(int)}
     */
    public void doNothing() {
        doNothing(utils.getCount());
    }

    /**
     * 是 hook 方法失效
     */
    public void doNothing(int index) {
        doNothing(index, -1);
    }

    /**
     * {@link ActionTool#doNothing(int)}
     */
    public void doNothing(int classIndex, int methodIndex) {
        hookTool("doNothing", classIndex, methodIndex, new IActionTool() {
            @Override
            public Action action(Member member) {
                return new Action(utils.getTAG(), PRIORITY_HIGHEST * 2) {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(null);
                    }
                };
            }
        });
    }
    
    private void hookTool(String name, int classIndex, int methodIndex, IActionTool tool) {
        boolean useMethodIndex = methodIndex != -1;
        if (utils.members.size() > classIndex) {
            MemberData data = utils.members.get(classIndex);
            if (data != null) {
                if (data.mClass == null) {
                    logW(utils.getTAG(), "index: " + classIndex + " this data class is null!");
                }
                if (lastIndex == -1) lastIndex = classIndex;
                else if (lastIndex != classIndex) {
                    lastIndex = classIndex;
                    count = 0;
                }
                int size = data.memberMap.size();
                ArrayList<Member> members = null;
                if (size > 2) {
                    if (count + 1 > size) {
                        logW(utils.getTAG(), name + " count > index, cant get memberMap! calss: " + data.mClass + "index: "
                                + classIndex + " size: " + size + " count: " + count);
                        return;
                    }
                    count = data.count;
                    // utils.setMethodCount(data.count);
                    members = data.memberMap.get(useMethodIndex ? methodIndex : count);
                    // count = count + 1;
                } else {
                    members = data.memberMap.get(0);
                    data.count = 0;
                }
                if (members == null) {
                    logW(utils.getTAG(), name + " don't have anything can hook. class is: " + data.mClass);
                    return;
                }
                if (isHooked(name, data, members)) {
                    logW(utils.getTAG(), "this method or constructor is hooked [" + name + "]! members: " + members);
                    return;
                }
                for (Member member : members) {
                    try {
                        XposedBridge.hookMethod(member, tool.action(member));
                    } catch (Throwable e) {
                        logE(utils.getTAG(), name + " hook method: " + member + " e: " + e);
                    }
                }
                if (useMethodIndex) data.count = count + 1;
                setState(name, data, members);
                utils.members.put(classIndex, data);
            } else {
                logW(utils.getTAG(), name + " member data is null!");
            }
        } else {
            logW(utils.getTAG(), name + " members size < index!");
        }
    }

    private void hookTool(String name, int classIndex, IActionTool tool) {
        hookTool(name, classIndex, -1, tool);
    }

    private boolean isHooked(String name, MemberData data, ArrayList<Member> members) {
        if (data.stateMap.get(members) == StateEnum.ALL) return true;
        switch (name) {
            case "after" -> {
                return data.stateMap.get(members) == StateEnum.AFTER;
            }
            case "before", "returnResult", "doNothing" -> {
                return data.stateMap.get(members) == StateEnum.BEFORE;
            }
            case "allAction" -> {
                if (data.stateMap.get(members) == StateEnum.AFTER ||
                        data.stateMap.get(members) == StateEnum.BEFORE) {
                    return true;
                }
            }
            default -> {
                return false;
            }
        }
        return false;
    }

    private void setState(String name, MemberData data, ArrayList<Member> members) {
        switch (name) {
            case "after" -> {
                data.stateMap.put(members, StateEnum.AFTER);
            }
            case "before", "returnResult", "doNothing" ->
                    data.stateMap.put(members, StateEnum.BEFORE);
            case "allAction" -> data.stateMap.put(members, StateEnum.ALL);
        }
    }

    private Action allActionTool(Member member, IAllAction iAllAction) {
        ParamTool<Object> paramTool = new ParamTool<>(member, utils.getTAG());
        StaticTool<Object> staticTool = new StaticTool<>(utils.getClassLoader(), utils.getTAG());
        return new Action(utils.getTAG()) {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                paramTool.setParam(param);
                iAllAction.before(paramTool, staticTool);
            }

            @Override
            protected void after(MethodHookParam param) throws Throwable {
                paramTool.setParam(param);
                iAllAction.after(paramTool, staticTool);
            }
        };
    }

    private Action afterTool(Member member, IAction iAction) {
        ParamTool<Object> paramTool = new ParamTool<>(member, utils.getTAG());
        StaticTool<Object> staticTool = new StaticTool<>(utils.getClassLoader(), utils.getTAG());
        return new Action(utils.getTAG()) {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                paramTool.setParam(param);
                iAction.action(paramTool, staticTool);
            }
        };
    }

    private Action beforeTool(Member member, IAction iAction) {
        ParamTool<Object> paramTool = new ParamTool<>(member, utils.getTAG());
        StaticTool<Object> staticTool = new StaticTool<>(utils.getClassLoader(), utils.getTAG());
        return new Action(utils.getTAG()) {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                paramTool.setParam(param);
                iAction.action(paramTool, staticTool);
            }
        };
    }

    public boolean actionSafe(String name, Object iAction) {
        if (iAction == null) {
            logW(utils.getTAG(), name + " is null!");
            return false;
        }
        return true;
    }

    interface IActionTool {
        Action action(Member member);
    }

    public MethodTool methodTool() {
        return utils.getMethodTool();
    }
}
