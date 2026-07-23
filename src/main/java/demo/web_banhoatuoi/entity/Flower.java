package demo.web_banhoatuoi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "flowers")
public class Flower {

    @Id
    @Column(name = "flower_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int flowerId;

    @Column(name = "flower_name", nullable = false)
    private String flowerName;

    @Column(name = "price")
    private double price;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "stock", nullable = false, columnDefinition = "int default 0")
    private int stock = 0;

    @Column(name = "additional_images", length = 2000)
    private String additionalImages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
