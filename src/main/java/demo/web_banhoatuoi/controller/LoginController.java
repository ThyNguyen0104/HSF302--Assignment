package demo.web_banhoatuoi.controller;

import demo.web_banhoatuoi.entity.Account;
import demo.web_banhoatuoi.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private AccountService accountService;
    @GetMapping("/login")
    public String showLoginPage() {
        return "admin/login";
    }
    @PostMapping("/login")
    public String handleLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            Model model) {

        Optional<Account> optionalAcc = accountService.getAccountByUserName(username.trim());

        if (optionalAcc.isEmpty()) {
            model.addAttribute("error", "Tài khoản không tồn tại!");
            return "admin/login";
        }

        Account account = optionalAcc.get();
        if (!account.getPassword().equals(password)) {
            model.addAttribute("error", "Sai mật khẩu!");
            return "admin/login";
        }
        request.getSession().setAttribute("loggedInUser", account);
        String role = account.getRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            return "redirect:/admin/dashboard";
        } else if ("USER".equalsIgnoreCase(role)) {
            return "redirect:/customer/home";
        } else {
            model.addAttribute("error", "Tài khoản không có quyền hợp lệ!");
            return "admin/login";
        }
    }

    // ✅ Logout
    @GetMapping("/logout")
    public String handleLogout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login?logout";
    }
    // 1. Hiển thị trang Đăng ký
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        // Gửi một đối tượng Account rỗng để Thymeleaf có thể bind data vào form
        model.addAttribute("account", new Account());
        return "admin/register"; // Giả sử file HTML là customer/register.html
    }

    // 2. Xử lý Đăng ký
    @PostMapping("/register")
    public String handleRegister(
            @ModelAttribute Account account,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        // 2a. Kiểm tra xác nhận mật khẩu (FRONTEND VALIDATION)
        if (!account.getPassword().equals(confirmPassword)) {
            // Gửi thông báo lỗi
            model.addAttribute("registrationError", "Xác nhận mật khẩu không khớp!");
            model.addAttribute("account", account); // Giữ lại data đã nhập
            return "admin/register";
        }

        // 2b. Kiểm tra username đã tồn tại chưa
        if (accountService.getAccountByUserName(account.getUserName()).isPresent()) {
            model.addAttribute("registrationError", "Tên đăng nhập đã tồn tại! Vui lòng chọn tên khác.");
            model.addAttribute("account", account);
            return "admin/register";
        }
        try {
            accountService.saveAccount(account);
        } catch (Exception e) {
            model.addAttribute("registrationError", "Đăng ký thất bại do lỗi hệ thống.");
            model.addAttribute("account", account);
            return "admin/register";
        }
        return "redirect:/login?success";
    }
}

