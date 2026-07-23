package demo.web_banhoatuoi.controller;

import demo.web_banhoatuoi.entity.*;
import demo.web_banhoatuoi.repository.*;
import demo.web_banhoatuoi.service.CartService;
import demo.web_banhoatuoi.service.QRCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;

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
    @Autowired private PayOS payOS;
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

    @GetMapping("/search/suggestions")
    @ResponseBody
    public List<String> getSearchSuggestions(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return flowerRepository.findByFlowerNameContainingIgnoreCase(query.trim())
                .stream()
                .map(Flower::getFlowerName)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
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
    public String submitSingleProductOrder(@PathVariable int flowerId, @ModelAttribute Order order, @RequestParam("quantity") int quantity, @RequestParam("style") String style, Model model, HttpServletRequest request, HttpSession session) {
        Flower flower = flowerRepository.findById(flowerId).orElseThrow(() -> new RuntimeException("Flower not found"));
        Account account = (Account) session.getAttribute("loggedInUser");
        if (account == null || (!"USER".equalsIgnoreCase(account.getRole()) && !"CUSTOMER".equalsIgnoreCase(account.getRole()))) return "redirect:/login";

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
        order.setTransactionId(String.valueOf(System.currentTimeMillis()));
        
        OrderItem item = new OrderItem(null, order, flower, quantity, unitPrice, style);
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(item);
        order.setItems(orderItems);

        orderRepository.save(order);
        try {
            String checkoutUrl = getPayOSCheckoutUrl(order, request);
            return "redirect:" + checkoutUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/customer/payment/" + order.getOrderId();
        }
    }

    @GetMapping("/payment/{orderId}")
    @Transactional
    public String paymentPage(@PathVariable int orderId, Model model, HttpServletRequest request, HttpSession session) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        model.addAttribute("order", order);
        
        try {
            if (order.getTransactionId() == null || order.getTransactionId().trim().isEmpty()) {
                order.setTransactionId(String.valueOf(System.currentTimeMillis()));
                orderRepository.save(order);
            }
            String checkoutUrl = getPayOSCheckoutUrl(order, request);
            model.addAttribute("paymentUrl", checkoutUrl);
            model.addAttribute("qrCodeBase64", null);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Không thể tạo liên kết thanh toán: " + e.getMessage());
        }
        return "customer/payment";
    }

    @GetMapping("/payment/payos/return")
    @Transactional
    public String payosReturn(@RequestParam("orderId") int orderId, Model model) {
        try {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
            long orderCode = Long.parseLong(order.getTransactionId());
            PaymentLink paymentInfo = payOS.paymentRequests().get(orderCode);
            
            if (PaymentLinkStatus.PAID.equals(paymentInfo.getStatus())) {
                if (!"PAID".equals(order.getPaymentStatus())) {
                    order.setPaymentStatus("PAID");
                    order.setTransactionId(paymentInfo.getId());

                    for (OrderItem item : order.getItems()) {
                        Flower flower = item.getFlower();
                        int newStock = flower.getStock() - item.getQuantity();
                        flower.setStock(Math.max(0, newStock));
                        flowerRepository.save(flower);
                    }
                    orderRepository.save(order);
                }
                model.addAttribute("order", order);
                model.addAttribute("success", true);
                model.addAttribute("message", "Thanh toán thành công qua PayOS!");
                return "customer/order-confirmation";
            } else {
                order.setPaymentStatus("FAILED");
                orderRepository.save(order);
                model.addAttribute("order", order);
                model.addAttribute("success", false);
                model.addAttribute("message", "Giao dịch không thành công hoặc chưa hoàn tất.");
                return "customer/payment-error";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Có lỗi xảy ra khi xử lý phản hồi PayOS.");
            return "customer/payment-error";
        }
    }

    @GetMapping("/payment/payos/cancel")
    @Transactional
    public String payosCancel(@RequestParam("orderId") int orderId, Model model) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setPaymentStatus("CANCELLED");
                orderRepository.save(order);
                model.addAttribute("order", order);
            }
            model.addAttribute("success", false);
            model.addAttribute("message", "Bạn đã hủy thanh toán đơn hàng.");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Có lỗi xảy ra khi hủy thanh toán.");
        }
        return "customer/payment-error";
    }

    @GetMapping("/order-history")
    @Transactional(readOnly = true)
    public String viewCustomerOrders(HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("loggedInUser");
        if (account == null) return "redirect:/login";
        model.addAttribute("orders", orderRepository.findByCustomerNameOrderByOrderIdDesc(account.getUserName()));
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
    public String placeOrder(@ModelAttribute Order order, HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
        Account account = (Account) session.getAttribute("loggedInUser");
        if (account == null) return "redirect:/login";

        List<Long> checkoutItemIds = (List<Long>) session.getAttribute("checkoutItemIds");
        if (checkoutItemIds == null || checkoutItemIds.isEmpty()) {
            return "redirect:/customer/cart";
        }
        List<CartItem> checkoutItems = cartItemRepository.findAllById(checkoutItemIds);

        order.setCustomerName(account.getUserName());
        order.setPaymentStatus("PENDING");
        order.setTransactionId(String.valueOf(System.currentTimeMillis()));

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

        try {
            String checkoutUrl = getPayOSCheckoutUrl(order, request);
            return "redirect:" + checkoutUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/customer/payment/" + order.getOrderId();
        }
    }

    private String getPayOSCheckoutUrl(Order order, HttpServletRequest request) throws Exception {
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
        long orderCode = Long.parseLong(order.getTransactionId());

        String returnUrl = baseUrl + "/customer/payment/payos/return?orderId=" + order.getOrderId();
        String cancelUrl = baseUrl + "/customer/payment/payos/cancel?orderId=" + order.getOrderId();

        List<PaymentLinkItem> items = new ArrayList<>();
        for (OrderItem orderItem : order.getItems()) {
            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name(orderItem.getFlower().getFlowerName())
                    .price((long) orderItem.getPriceAtPurchase())
                    .quantity(orderItem.getQuantity())
                    .build();
            items.add(item);
        }

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount((long) order.getTotalAmount())
                .description("Thanh toan DH" + order.getOrderId())
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .items(items)
                .build();

        CreatePaymentLinkResponse checkoutResponse = payOS.paymentRequests().create(paymentData);
        return checkoutResponse.getCheckoutUrl();
    }
}
