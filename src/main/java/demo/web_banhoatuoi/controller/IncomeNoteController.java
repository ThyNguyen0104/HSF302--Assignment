package demo.web_banhoatuoi.controller;

import demo.web_banhoatuoi.entity.Account;
import demo.web_banhoatuoi.entity.Order;
import demo.web_banhoatuoi.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/income")
public class IncomeNoteController {

    private final OrderService orderService;

    public IncomeNoteController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/note")
    public String listIncomeNotes(HttpSession session,
            @RequestParam(required = false) String keyword,
                                  Model model) {

        Account account = (Account) session.getAttribute("loggedInUser");
        if (account == null) return "redirect:/login";

        List<Order> notes;

        if (keyword != null && !keyword.trim().isEmpty()) {
            notes = orderService.searchOrders(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            notes = orderService.getAllOrders();
        }

        // FIX: Use getTotalAmount() which is the final price for the entire order
        double grandTotal = notes.stream()
                .mapToDouble(Order::getTotalAmount) 
                .sum();

        model.addAttribute("notes", notes);
        model.addAttribute("grandTotal", grandTotal);

        return "admin/income-note";
    }
}
