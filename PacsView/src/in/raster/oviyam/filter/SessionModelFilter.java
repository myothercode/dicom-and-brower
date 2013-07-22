package in.raster.oviyam.filter;

import in.raster.oviyam.access.DataAccess;
import in.raster.oviyam.access.DataAccessImpl;
import in.raster.oviyam.model.SessionModel;
import in.raster.oviyam.utils.SessionModelHolder;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-7-15
 * Time: 下午1:09
 * To change this template use File | Settings | File Templates.
 */
public class SessionModelFilter implements Filter {
    public static final String SESSION_KEY="pacs_session_456123";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        SessionModelHolder.removeSessionVO();
        SessionModel sessionModel = getSessionModel((HttpServletRequest)servletRequest);
        SessionModelHolder.setSessionVO(sessionModel);
        if(sessionModel!=null)
        filterChain.doFilter(servletRequest,servletResponse);

    }

    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private SessionModel getSessionModel(HttpServletRequest request){
        /*得到CAS反馈回来的loginID*/
        AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();
        String loginId=principal.getName();

        HttpSession session = request.getSession(false);
        if(session==null) return null;
        SessionModel sessionModel=(SessionModel)session.getAttribute(SESSION_KEY);

        if(sessionModel !=null && sessionModel.getLoginId().equals(loginId)){
            /*如果sessionModel不为空且cas的loginid与当前session的id相等 就返回当前的sessionmodel */
            return sessionModel;
        }else{
            /*重新从数据库查询sessionmodel*/
            ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
            DataAccess dataAccess=applicationContext.getBean(DataAccessImpl.class);
            sessionModel=dataAccess.findSessionInfoByLoginId(loginId);
            session.removeAttribute(SESSION_KEY);
            session.setAttribute(SESSION_KEY,sessionModel);
            return sessionModel;
        }


    }
}
