package demo.web_banhoatuoi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flower_id", nullable = false)
    private Flower flower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = true) // Nullable for guest users
    private Account account;

    @Column(name = "session_id") // To track guest carts
    private String sessionId;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "selected_style", length = 500) // To store the image path of the selected style
    private String selectedStyle;

}
