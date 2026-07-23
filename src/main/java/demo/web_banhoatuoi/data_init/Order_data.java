package demo.web_banhoatuoi.data_init;

import demo.web_banhoatuoi.entity.Flower;
import demo.web_banhoatuoi.entity.Order;
import demo.web_banhoatuoi.entity.OrderItem;
import demo.web_banhoatuoi.repository.OrderRepository;
import demo.web_banhoatuoi.repository.FlowerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@DependsOn("flower_data")
public class Order_data implements CommandLineRunner {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FlowerRepository flowerRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (orderRepository.count() > 0) {
            System.out.println("✅ Order data already exists. Skipping initialization.");
            return;
        }

        System.out.println("⏳ Initializing order data...");

        Flower rose = flowerRepository.findByFlowerName("rose").orElseThrow(() -> new RuntimeException("Flower 'rose' not found."));
        Flower tulip = flowerRepository.findByFlowerName("tulip").orElseThrow(() -> new RuntimeException("Flower 'tulip' not found."));
        Flower sunflower = flowerRepository.findByFlowerName("sunflower").orElseThrow(() -> new RuntimeException("Flower 'sunflower' not found."));
        Flower iris = flowerRepository.findByFlowerName("iris").orElseThrow(() -> new RuntimeException("Flower 'iris' not found."));

        List<Order> orders = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Order 1
        Order order1 = new Order();
        order1.setCustomerName("Nguyen Van A");
        order1.setPhone("0901234567");
        order1.setAddress("123 Le Loi St, District 1, Ho Chi Minh City");
        order1.setNote("Wedding bouquet - urgent delivery");
        order1.setPaymentStatus("PAID");
        order1.setTransactionId("TXN001");
        order1.setDeliveryDate(today.plusDays(2));
        OrderItem item1 = new OrderItem(null, order1, rose, 2, rose.getPrice() * 0.9, rose.getImagePath());
        order1.getItems().add(item1);
        order1.setTotalAmount(item1.getPriceAtPurchase() * item1.getQuantity());
        orders.add(order1);

        // Order 2
        Order order2 = new Order();
        order2.setCustomerName("Tran Thi B");
        order2.setPhone("0987654321");
        order2.setAddress("456 Nguyen Hue St, District 3, Ho Chi Minh City");
        order2.setNote("Birthday gift for mom");
        order2.setPaymentStatus("PENDING");
        order2.setDeliveryDate(today.plusDays(3));
        OrderItem item2 = new OrderItem(null, order2, tulip, 1, tulip.getPrice(), tulip.getImagePath());
        order2.getItems().add(item2);
        order2.setTotalAmount(item2.getPriceAtPurchase() * item2.getQuantity());
        orders.add(order2);

        // Order 3
        Order order3 = new Order();
        order3.setCustomerName("Le Van C");
        order3.setPhone("0912345678");
        order3.setAddress("789 Dong Khoi St, District 1, Ho Chi Minh City");
        order3.setNote("Office decoration");
        order3.setPaymentStatus("PAID");
        order3.setTransactionId("TXN002");
        order3.setDeliveryDate(today.plusDays(1));
        OrderItem item3 = new OrderItem(null, order3, sunflower, 6, sunflower.getPrice() * 0.9, sunflower.getImagePath());
        order3.getItems().add(item3);
        order3.setTotalAmount(item3.getPriceAtPurchase() * item3.getQuantity());
        orders.add(order3);

        // Order 4
        Order order4 = new Order();
        order4.setCustomerName("Pham Thi D");
        order4.setPhone("0934567890");
        order4.setAddress("321 Ham Nghi St, District 1, Ho Chi Minh City");
        order4.setNote("Anniversary surprise");
        order4.setPaymentStatus("FAILED");
        order4.setDeliveryDate(today.plusDays(4));
        OrderItem item4 = new OrderItem(null, order4, iris, 3, iris.getPrice(), iris.getImagePath());
        order4.getItems().add(item4);
        order4.setTotalAmount(item4.getPriceAtPurchase() * item4.getQuantity());
        orders.add(order4);

        // Order 5
        Order order5 = new Order();
        order5.setCustomerName("Hoang Van E");
        order5.setPhone("0956789012");
        order5.setAddress("654 Pasteur St, District 3, Ho Chi Minh City");
        order5.setNote("Get well soon bouquet");
        order5.setPaymentStatus("PAID");
        order5.setTransactionId("TXN003");
        order5.setDeliveryDate(today.plusDays(2));
        OrderItem item5 = new OrderItem(null, order5, rose, 4, rose.getPrice() * 0.9, rose.getAdditionalImages().split(",")[0].trim());
        order5.getItems().add(item5);
        order5.setTotalAmount(item5.getPriceAtPurchase() * item5.getQuantity());
        orders.add(order5);
        
        orderRepository.saveAll(orders);
        System.out.println("✅ Created " + orders.size() + " orders successfully!");
    }
}
