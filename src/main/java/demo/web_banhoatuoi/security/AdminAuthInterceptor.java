package demo.web_banhoatuoi.security; // hoặc interceptor tùy bạn

import demo.web_banhoatuoi.entity.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Chống cache: ngăn trình duyệt hiển thị trang cũ khi nhấn back
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setDateHeader("Expires", 0); // proxy

        // Lấy thông tin user từ session
        Account account = (Account) request.getSession().getAttribute("loggedInUser");
        if (account == null || !"ADMIN".equalsIgnoreCase(account.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }
}
