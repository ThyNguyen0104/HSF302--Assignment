package demo.web_banhoatuoi.repository;

import demo.web_banhoatuoi.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // Find category by name
    Optional<Category> findByCategoryName(String categoryName);

    // Check if category exists
    boolean existsByCategoryName(String categoryName);
}