package demo.web_banhoatuoi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToCustomerHome() {
        return "redirect:/customer/home";
    }
}
