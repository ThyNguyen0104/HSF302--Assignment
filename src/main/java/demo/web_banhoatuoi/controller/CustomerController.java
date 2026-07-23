package demo.web_banhoatuoi.controller;

import demo.web_banhoatuoi.entity.*;
import demo.web_banhoatuoi.repository.*;
import demo.web_banhoatuoi.service.CartService;
import demo.web_banhoatuoi.service.QRCodeService;
import demo.web_banhoatuoi.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired private FlowerRepository flowerRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private VNPayService vnPayService;
    @Autowired private QRCodeService qrCodeService;
    @Autowired private CartService cartService;

    @ModelAttribute("cartItemCount")
    public int cartItemCount(HttpSession session) {
        return cartService.getCartItemCount(session);
    }

    @GetMapping({"/", "/home"})
    @Transactional(readOnly = true)
    public String home(@RequestParam(required = false) String search, @RequestParam(required = false) Integer categoryId, Model model) {
        List<Flower> flowers;
        if (search != null && !search.trim().isEmpty()) {
            flowers = flowerRepository.findByFlowerNameContainingIgnoreCase(search.trim());
        } else if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            flowers = (category != null) ? flowerRepository.findByCategory(category) : flowerRepository.findAll();
        } else {
            flowers = flowerRepository.findAll();
        }
        
        // Always fetch all categories to ensure they are displayed
        List<Category> categories = categoryRepository.findAll();
        
        model.addAttribute("flowers", flowers);
        model.addAttribute("categories", categories);
        model.addAttribute("search", search);
        model.addAttribute("selectedCategoryId", categoryId);
        
        return "customer/index";
    }

    @GetMapping("/order/{flowerId}")
    @Transactional(readOnly = true)
    public String orderForm(@PathVariable int flowerId, Model model) {
        Flower flower = flowerRepository.findById(flowerId).orElseThrow(() -> new RuntimeException("Flower not found"));
        List<String> additionalImagesList = new ArrayList<>();
        if (flower.getAdditionalImages() != null && !flower.getAdditionalImages().trim().isEmpty()) {
            additionalImagesList.addAll(Arrays.asList(flower.getAdditionalImages().split(",")));
        }
        model.addAttribute("flower", flower);
        model.addAttribute("order", new Order());
        model.addAttribute("additionalImages", additionalImagesList);
        return "customer/order";
    }

    @PostMapping("/buy-now-details")
    public String buyNowDetails(@RequestParam("flowerId") int flowerId, @RequestParam("quantity") int quantity, @RequestParam(value = "style", required = false) String style, Model model) {
        Flower flower = flowerRepository.findById(flowerId).orElseThrow(() -> new RuntimeException("Flower not found"));
        model.addAttribute("flower", flower);
        model.addAttribute("order", new Order());
        model.addAttribute("quantity", quantity);
        model.addAttribute("style", style);
        return "customer/buy_now_details";
    }

    @PostMapping("/order/{flowerId}")
    @Transactional
    public String submitSingleProductOrder(@PathVariable int flowerId, @ModelAttribute Order order, @RequestParam("quantity") int quantity, @RequestParam("style") String style, Model model, HttpSession session) {
        Flower flower = flowerRepository.findById(flowerId).orElseThrow(() -> new RuntimeException("Flower not found"));
        Account account = (Account) session.getAttribute("loggedInUser");
        if (account == null) return "redirect:/login";

        order.setCustomerName(account.getUserName());
        if (quantity > flower.getStock()) {
            model.addAttribute("error", "Số lượng vượt quá số lượng tồn kho!");
            model.addAttribute("flower", flower);
            model.addAttribute("order", order);
            model.addAttribute("quantity", quantity);
            model.addAttribute("style", style);
            return "customer/buy_now_details";
        }

        boolean isLoggedIn = session.getAttribute("loggedInUser") != null;
        double unitPrice = isLoggedIn ? flower.getPrice() * 0.9 : flower.getPrice();
        double totalAmount = unitPrice * quantity;

        order.setTotalAmount(totalAmount);
        order.setPaymentStatus("PENDING");
        
        OrderItem item = new OrderItem(null, order, flower, quantity, unitPrice, style);
        order.setItems(List.of(item));

        orderRepository.save(order);
        return "redirect:/customer/payment/" + order.getOrderId();
    }

    @GetMapping("/payment/{orderId}")
    @Transactional(readOnly = true)
    public String paymentPage(@PathVariable int orderId, Model model, HttpSession session) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        model.addAttribute("order", order);
        
        try {
            String orderInfo = "Thanh toan don hang " + orderId;
            String paymentUrl = vnPayService.createPaymentUrl(orderId, order.getTotalAmount(), orderInfo);
            String qrCodeBase64 = qrCodeService.generateQRCodeBase64(paymentUrl, 300, 300);
            model.addAttribute("paymentUrl", paymentUrl);
            model.addAttribute("qrCodeBase64", qrCodeBase64);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Không thể tạo liên kết thanh toán.");
        }
        return "customer/payment";
    }

    @GetMapping("/payment/return")
    @Transactional
    public String paymentReturn(@RequestParam Map<String, String> allParams, @RequestParam(required = false) Integer orderId, Model model, HttpSession session) {
        try {
            String responseCode = allParams.get("vnp_ResponseCode");
            if (orderId != null) {
                Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
                if ("00".equals(responseCode)) {
                    order.setPaymentStatus("PAID");
                    order.setTransactionId(allParams.get("vnp_TransactionNo"));

                    for (OrderItem item : order.getItems()) {
                        Flower flower = item.getFlower();
                        int newStock = flower.getStock() - item.getQuantity();
                        flower.setStock(Math.max(0, newStock));
                        flowerRepository.save(flower);
                    }

                    orderRepository.save(order);
                    model.addAttribute("order", order);
                    model.addAttribute("success", true);
                    model.addAttribute("message", "Thanh toán thành công!");
                    return "customer/order-confirmation";
                } else {
                    order.setPaymentStatus("FAILED");
                    orderRepository.save(order);
                    model.addAttribute("order", order);
                    model.addAttribute("success", false);
                    model.addAttribute("message", "Thanh toán thất bại. Vui lòng thử lại.");
                    return "customer/payment-error";
                }
            }
            model.addAttribute("message", "Không tìm thấy đơn hàng.");
            return "customer/payment-error";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Có lỗi xảy ra khi xử lý thanh toán.");
            return "customer/payment-error";
        }
    }

    @GetMapping("/order-history")
    @Transactional(readOnly = true)
    public String viewCustomerOrders(HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("loggedInUser");
        if (account == null) return "redirect:/login";
        model.addAttribute("orders", orderRepository.findByCustomerName(account.getUserName()));
        model.addAttribute("username", account.getUserName());
        return "customer/order-history";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("flowerId") int flowerId, @RequestParam(value = "quantity", defaultValue = "1") int quantity, @RequestParam(value = "style", required = false) String style, HttpSession session, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            cartService.addFlowerToCart(flowerId, quantity, style, session);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/customer/home");
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        model.addAttribute("cartItems", cartService.getCartItems(session));
        model.addAttribute("totalAmount", cartService.getCartTotalAmount(session));
        return "customer/cart";
    }

    @GetMapping("/cart/update")
    public String updateCart(@RequestParam("cartItemId") Long cartItemId, @RequestParam("quantity") int quantity, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            cartService.updateFlowerQuantity(cartItemId, quantity, session);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/customer/cart";
    }

    @GetMapping("/cart/remove")
    public String removeFromCart(@RequestParam("cartItemId") Long cartItemId, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            cartService.removeFlowerFromCart(cartItemId, session);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/customer/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam(value = "selectedItems", required = false) List<Long> selectedItems, HttpSession session, RedirectAttributes redirectAttributes) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất một sản phẩm để thanh toán.");
            return "redirect:/customer/cart";
        }
        session.setAttribute("checkoutItemIds", selectedItems);
        return "redirect:/customer/checkout";
    }

    @GetMapping("/checkout")
    public String showCheckout(Model model, HttpSession session) {
        List<Long> checkoutItemIds = (List<Long>) session.getAttribute("checkoutItemIds");
        if (checkoutItemIds == null || checkoutItemIds.isEmpty()) {
            return "redirect:/customer/cart";
        }
        List<CartItem> checkoutItems = cartItemRepository.findAllById(checkoutItemIds);
        double totalAmount = checkoutItems.stream().mapToDouble(item -> {
            boolean isLoggedIn = session.getAttribute("loggedInUser") != null;
            double unitPrice = isLoggedIn ? item.getFlower().getPrice() * 0.9 : item.getFlower().getPrice();
            return unitPrice * item.getQuantity();
        }).sum();

        model.addAttribute("checkoutItems", checkoutItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("order", new Order());
        return "customer/checkout";
    }

    @PostMapping("/place-order")
    @Transactional
    public String placeOrder(@ModelAttribute Order order, HttpSession session, RedirectAttributes redirectAttributes) {
        Account account = (Account) session.getAttribute("loggedInUser");
        if (account == null) return "redirect:/login";

        List<Long> checkoutItemIds = (List<Long>) session.getAttribute("checkoutItemIds");
        if (checkoutItemIds == null || checkoutItemIds.isEmpty()) {
            return "redirect:/customer/cart";
        }
        List<CartItem> checkoutItems = cartItemRepository.findAllById(checkoutItemIds);

        order.setCustomerName(account.getUserName());
        order.setPaymentStatus("PENDING");

        double totalAmount = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : checkoutItems) {
            boolean isLoggedIn = session.getAttribute("loggedInUser") != null;
            double unitPrice = isLoggedIn ? cartItem.getFlower().getPrice() * 0.9 : cartItem.getFlower().getPrice();
            totalAmount += unitPrice * cartItem.getQuantity();
            
            OrderItem orderItem = new OrderItem(null, order, cartItem.getFlower(), cartItem.getQuantity(), unitPrice, cartItem.getSelectedStyle());
            orderItems.add(orderItem);
        }
        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        orderRepository.save(order);

        cartItemRepository.deleteAllById(checkoutItemIds);
        session.removeAttribute("checkoutItemIds");

        return "redirect:/customer/payment/" + order.getOrderId();
    }
}
