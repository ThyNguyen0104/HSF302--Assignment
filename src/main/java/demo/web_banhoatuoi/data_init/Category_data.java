package demo.web_banhoatuoi.data_init;

import demo.web_banhoatuoi.entity.Category;
import demo.web_banhoatuoi.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Component
@Order(1) // Add this - run first
public class Category_data implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            Category todaysFlowers = new Category();
            todaysFlowers.setCategoryName("today's flowers");

            Category datesFlowers = new Category();
            datesFlowers.setCategoryName("DATE's flowers");

            Category memorialFlowers = new Category();
            memorialFlowers.setCategoryName("Memorial's flowers");

            categoryRepository.save(todaysFlowers);
            categoryRepository.save(datesFlowers);
            categoryRepository.save(memorialFlowers);
        }
    }
}