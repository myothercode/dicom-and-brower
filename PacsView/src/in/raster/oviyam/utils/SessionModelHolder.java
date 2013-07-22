package in.raster.oviyam.utils;


import in.raster.oviyam.model.SessionModel;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-7-15
 * Time: 下午1:18
 * To change this template use File | Settings | File Templates.
 */
public class SessionModelHolder {
    private static final ThreadLocal<SessionModel> SESSION_VO_THREAD_LOCAL = new ThreadLocal<SessionModel>();

    /** 获取当前用户 */
    public static SessionModel getSessionVO() {
        return SESSION_VO_THREAD_LOCAL.get();
    }

    /** 设置当前用户 */
    public static void setSessionVO(SessionModel sessionVO) {
        SESSION_VO_THREAD_LOCAL.set(sessionVO);
    }

    /** 将当前用户移除 */
    public static void removeSessionVO() {
        SESSION_VO_THREAD_LOCAL.set(null);
    }
}
