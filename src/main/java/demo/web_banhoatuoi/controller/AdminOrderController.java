package demo.web_banhoatuoi.controller;

import demo.web_banhoatuoi.entity.Order;
import demo.web_banhoatuoi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String listOrders(@RequestParam(required = false) String status, Model model) {
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderService.findByPaymentStatus(status);
        } else {
            orders = orderService.findAll();
        }
        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        return "admin/order-list";
    }

    @GetMapping("/edit/{id}")
    public String editOrder(@PathVariable int id, Model model) {
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order ID: " + id));
        model.addAttribute("order", order);
        model.addAttribute("flowers", orderService.getAllFlowers());
        return "admin/order-form";
    }

    @PostMapping("/update")
    public String updateOrder(@ModelAttribute("order") Order order) {
        orderService.saveOrder(order);
        return "redirect:/admin/orders?success";
    }

    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable int id) {
        orderService.deleteOrder(id);
        return "redirect:/admin/orders?deleted";
    }

}

