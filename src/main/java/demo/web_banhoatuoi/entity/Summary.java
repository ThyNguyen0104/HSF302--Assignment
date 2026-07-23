package demo.web_banhoatuoi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "summary")
public class Summary {

    @Id
    @Column(name = "summary_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int summaryId;

    @Column(name = "total_income", nullable = false)
    private double totalIncome; // Sum of all order prices

    @Column(name = "total_orders", nullable = false)
    private int totalOrders; // Count of orders

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (summaryDate == null) {
            summaryDate = LocalDate.now();
        }
    }
}