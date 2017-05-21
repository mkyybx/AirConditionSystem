package AC;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by Ice on 5/18/2017.
 *//*
public class RedirectFilter implements Filter{
    private static final String LOGIN_PAGE = "login.html";
    private static final String CONTROL_PAGE = "control.html";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)servletRequest;
        HttpServletResponse resp = (HttpServletResponse)servletResponse;
        req.getCookies()
        HttpSession session = req.getSession(false);
        if (session == null) {
            session = req.getSession();
            session.setAttribute("Login",false);
            session.setAttribute("Redirected",true);
            resp.sendRedirect(LOGIN_PAGE);
        }
        else {
            if (((Boolean)(session.getAttribute("Redirected"))) == false) {
                if (((Boolean) (session.getAttribute("Login"))) == false) {
                    session.setAttribute("Redirected", true);
                    resp.sendRedirect(LOGIN_PAGE);
                } else {
                    session.setAttribute("Redirected", true);
                    resp.sendRedirect(CONTROL_PAGE);
                }
            }
            else session.setAttribute("Redirected", false);
        }
    }

    @Override
    public void destroy() {

    }
}
*/