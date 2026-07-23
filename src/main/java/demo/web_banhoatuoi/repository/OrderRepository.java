package demo.web_banhoatuoi.repository;

import demo.web_banhoatuoi.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Find orders by customer name
    List<Order> findByCustomerName(String customerName);

    // Find orders by phone
    List<Order> findByPhone(String phone);

    // Find pending order by phone and customer name (for updating existing order)
    Order findByPhoneAndCustomerNameAndPaymentStatus(String phone, String customerName, String paymentStatus);

    // Find pending order by phone (for updating existing order)
    Order findByPhoneAndPaymentStatus(String phone, String paymentStatus);

    List<Order> findOrdersByPaymentStatus(String paymentStatus);

    // FIX: Updated query to join through OrderItem to find flower name
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi JOIN oi.flower f WHERE " +
           "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.flowerName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Order> searchByKeyword(@Param("keyword") String keyword);
}
