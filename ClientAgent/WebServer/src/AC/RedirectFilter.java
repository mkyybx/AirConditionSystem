package AC;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Ice on 5/18/2017.
 */
public class RedirectFilter implements Filter{

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            Class.forName("AC.Controller");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
       /* try {
            StringBuilder addr = new StringBuilder(servletRequest.getRemoteAddr());
            addr.reverse().append(servletRequest.getRemoteAddr());
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(addr.toString().getBytes());
            String token = new BigInteger(1, md.digest()).toString(16);
            servletRequest.getRequestDispatcher("/ws/" + token);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }*/
    /*    HttpServletRequest req = (HttpServletRequest)servletRequest;
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
        }*/
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
