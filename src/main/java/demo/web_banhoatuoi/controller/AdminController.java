package demo.web_banhoatuoi.controller;

import demo.web_banhoatuoi.entity.Account;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @GetMapping("/dashboard")
    public String showDashboard(HttpServletRequest request) {
        Account account = (Account) request.getSession().getAttribute("loggedInUser");
        if (account == null || !"ADMIN".equalsIgnoreCase(account.getRole())) {
            return "redirect:/login";
        }
        return "admin/dashboard";
    }
}