package demo.web_banhoatuoi.repository;

import demo.web_banhoatuoi.entity.Account;
import demo.web_banhoatuoi.entity.CartItem;
import demo.web_banhoatuoi.entity.Flower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find cart items by account
    List<CartItem> findByAccount(Account account);

    // Find cart items by session ID (for guest users)
    List<CartItem> findBySessionId(String sessionId);

    // Find a specific cart item by account, flower, and style
    Optional<CartItem> findByAccountAndFlowerAndSelectedStyle(Account account, Flower flower, String selectedStyle);

    // Find a specific cart item by session ID, flower, and style
    Optional<CartItem> findBySessionIdAndFlowerAndSelectedStyle(String sessionId, Flower flower, String selectedStyle);

    // Delete cart items by account
    void deleteByAccount(Account account);

    // Delete cart items by session ID
    void deleteBySessionId(String sessionId);
}
