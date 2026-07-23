package demo.web_banhoatuoi.repository;

import demo.web_banhoatuoi.entity.Flower;
import demo.web_banhoatuoi.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlowerRepository extends JpaRepository<Flower, Integer> {

    // Find flowers by name
    Optional<Flower> findByFlowerName(String flowerName);

    // Find flowers by category
    List<Flower> findByCategory(Category category);

    // Find flowers by category name
    List<Flower> findByCategoryCategoryName(String categoryName);

    // Find flowers by price range
    List<Flower> findByPriceBetween(double minPrice, double maxPrice);

    // Check if flower exists
    boolean existsByFlowerName(String flowerName);

    // Search flowers by name (partial match, case insensitive) - using Spring Data JPA method naming
    List<Flower> findByFlowerNameContainingIgnoreCase(String keyword);
}