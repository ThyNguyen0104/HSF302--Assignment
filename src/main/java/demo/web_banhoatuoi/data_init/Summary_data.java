package demo.web_banhoatuoi.data_init;

import demo.web_banhoatuoi.entity.Order;
import demo.web_banhoatuoi.entity.OrderItem;
import demo.web_banhoatuoi.entity.Summary;
import demo.web_banhoatuoi.repository.OrderRepository;
import demo.web_banhoatuoi.repository.SummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class Summary_data implements CommandLineRunner {

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (summaryRepository.count() == 0) {
            // Get all orders
            List<demo.web_banhoatuoi.entity.Order> orders = orderRepository.findAll();

            // Calculate total income by iterating through each item in each order
            double totalIncome = 0.0;
            for (demo.web_banhoatuoi.entity.Order order : orders) {
                // Only calculate income for PAID orders
                if ("PAID".equals(order.getPaymentStatus())) {
                    // The totalAmount field on the Order already represents the sum of its items
                    // So, we can simply use it. This is more efficient.
                    totalIncome += order.getTotalAmount();
                }
            }

            // Create summary with correct totals
            Summary summary = new Summary();
            summary.setTotalIncome(totalIncome);
            summary.setTotalOrders(orders.size());
            summary.setSummaryDate(LocalDate.now());

            summaryRepository.save(summary);

            System.out.println("✅ Summary created with final order prices - Total Income from PAID orders: " + totalIncome + " VND");
        }
    }
}
