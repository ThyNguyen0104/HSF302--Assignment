package demo.web_banhoatuoi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import demo.web_banhoatuoi.entity.Order;
import demo.web_banhoatuoi.entity.OrderItem;
import demo.web_banhoatuoi.entity.Flower;
import demo.web_banhoatuoi.repository.OrderRepository;
import demo.web_banhoatuoi.repository.FlowerRepository;
import vn.payos.PayOS;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class PayOSWebhookController {

    @Autowired
    private PayOS payOS;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FlowerRepository flowerRepository;

    @PostMapping("/payos-webhook")
    @Transactional
    public ResponseEntity<ObjectNode> handleWebhook(@RequestBody Webhook webhook) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        try {
            // Verify signature using the SDK
            WebhookData verifiedData = payOS.webhooks().verify(webhook);
            
            long orderCode = verifiedData.getOrderCode();
            Order order = orderRepository.findByTransactionId(String.valueOf(orderCode));
            
            if (order != null && !"PAID".equals(order.getPaymentStatus())) {
                order.setPaymentStatus("PAID");
                order.setTransactionId(verifiedData.getPaymentLinkId());
                
                // Deduct stock
                for (OrderItem item : order.getItems()) {
                    Flower flower = item.getFlower();
                    int newStock = flower.getStock() - item.getQuantity();
                    flower.setStock(Math.max(0, newStock));
                    flowerRepository.save(flower);
                }
                
                orderRepository.save(order);
            }
            
            response.put("error", 0);
            response.put("message", "Ok");
            response.set("data", mapper.valueToTree(verifiedData));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", -1);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
