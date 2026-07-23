package demo.web_banhoatuoi.service;

import demo.web_banhoatuoi.entity.Account;
import demo.web_banhoatuoi.entity.CartItem;
import demo.web_banhoatuoi.entity.Flower;
import demo.web_banhoatuoi.repository.CartItemRepository;
import demo.web_banhoatuoi.repository.FlowerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private FlowerRepository flowerRepository;

    // Helper to get current user or session ID
    private String getSessionId(HttpSession session) {
        return session.getId();
    }

    private Account getLoggedInUser(HttpSession session) {
        return (Account) session.getAttribute("loggedInUser");
    }

    // Get cart items for current user/session
    public List<CartItem> getCartItems(HttpSession session) {
        Account account = getLoggedInUser(session);
        if (account != null) {
            return cartItemRepository.findByAccount(account);
        } else {
            return cartItemRepository.findBySessionId(getSessionId(session));
        }
    }

    // Add item to cart
    @Transactional
    public void addFlowerToCart(int flowerId, int quantity, String style, HttpSession session) {
        Flower flower = flowerRepository.findById(flowerId)
                .orElseThrow(() -> new RuntimeException("Flower not found"));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if (quantity > flower.getStock()) {
            throw new IllegalArgumentException("Quantity exceeds available stock.");
        }

        Account account = getLoggedInUser(session);
        String sessionId = getSessionId(session);

        Optional<CartItem> existingCartItem;
        if (account != null) {
            existingCartItem = cartItemRepository.findByAccountAndFlowerAndSelectedStyle(account, flower, style);
        } else {
            existingCartItem = cartItemRepository.findBySessionIdAndFlowerAndSelectedStyle(sessionId, flower, style);
        }

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            if (newQuantity > flower.getStock()) {
                throw new IllegalArgumentException("Total quantity exceeds available stock.");
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem newCartItem = new CartItem();
            newCartItem.setFlower(flower);
            newCartItem.setQuantity(quantity);
            newCartItem.setSelectedStyle(style); // Save the selected style
            if (account != null) {
                newCartItem.setAccount(account);
            } else {
                newCartItem.setSessionId(sessionId);
            }
            cartItemRepository.save(newCartItem);
        }
    }

    // Update item quantity in cart
    @Transactional
    public void updateFlowerQuantity(Long cartItemId, int quantity, HttpSession session) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Ensure the cart item belongs to the current user/session
        Account account = getLoggedInUser(session);
        String sessionId = getSessionId(session);

        boolean authorized = false;
        if (account != null && cartItem.getAccount() != null && cartItem.getAccount().equals(account)) {
            authorized = true;
        } else if (account == null && cartItem.getSessionId() != null && cartItem.getSessionId().equals(sessionId)) {
            authorized = true;
        }

        if (!authorized) {
            throw new SecurityException("Unauthorized access to cart item.");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem); // Remove if quantity is 0 or less
        } else {
            if (quantity > cartItem.getFlower().getStock()) {
                throw new IllegalArgumentException("Quantity exceeds available stock.");
            }
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    // Remove item from cart
    @Transactional
    public void removeFlowerFromCart(Long cartItemId, HttpSession session) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Ensure the cart item belongs to the current user/session
        Account account = getLoggedInUser(session);
        String sessionId = getSessionId(session);

        boolean authorized = false;
        if (account != null && cartItem.getAccount() != null && cartItem.getAccount().equals(account)) {
            authorized = true;
        }
        else if (account == null && cartItem.getSessionId() != null && cartItem.getSessionId().equals(sessionId)) {
            authorized = true;
        }

        if (!authorized) {
            throw new SecurityException("Unauthorized access to cart item.");
        }

        cartItemRepository.delete(cartItem);
    }

    // Get total number of items in cart
    public int getCartItemCount(HttpSession session) {
        return getCartItems(session).stream().mapToInt(CartItem::getQuantity).sum();
    }

    // Get total amount of cart
    public double getCartTotalAmount(HttpSession session) {
        List<CartItem> cartItems = getCartItems(session);
        double total = 0;
        Account account = getLoggedInUser(session);
        boolean isLoggedIn = (account != null);

        for (CartItem item : cartItems) {
            double unitPrice = isLoggedIn ? item.getFlower().getPrice() * 0.9 : item.getFlower().getPrice();
            total += unitPrice * item.getQuantity();
        }
        return total;
    }

    // Merge guest cart to user cart after login
    @Transactional
    public void mergeCarts(String guestSessionId, Account account) {
        List<CartItem> guestCartItems = cartItemRepository.findBySessionId(guestSessionId);
        for (CartItem guestItem : guestCartItems) {
            Optional<CartItem> existingUserCartItem = cartItemRepository.findByAccountAndFlowerAndSelectedStyle(account, guestItem.getFlower(), guestItem.getSelectedStyle());
            if (existingUserCartItem.isPresent()) {
                CartItem userItem = existingUserCartItem.get();
                userItem.setQuantity(userItem.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(userItem);
                cartItemRepository.delete(guestItem); // Delete the guest item after merging
            } else {
                guestItem.setAccount(account);
                guestItem.setSessionId(null); // Remove session ID as it's now linked to an account
                cartItemRepository.save(guestItem);
            }
        }
    }

    // Clear cart
    @Transactional
    public void clearCart(HttpSession session) {
        Account account = getLoggedInUser(session);
        if (account != null) {
            cartItemRepository.deleteByAccount(account);
        } else {
            cartItemRepository.deleteBySessionId(getSessionId(session));
        }
    }
}
