package demo.web_banhoatuoi.service;

import demo.web_banhoatuoi.entity.Flower;
import demo.web_banhoatuoi.entity.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface OrderService {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(int id);
    void saveOrder(Order order);
    void deleteOrder(int id);
    List<Order> findByPaymentStatus(String status);
    List<Order> findAll();
    List<Flower> getAllFlowers();
    List<Order> searchOrders(String keyword);
}
