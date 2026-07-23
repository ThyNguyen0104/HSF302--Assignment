package demo.web_banhoatuoi.data_init;

import demo.web_banhoatuoi.entity.Flower;
import demo.web_banhoatuoi.entity.Category;
import demo.web_banhoatuoi.repository.FlowerRepository;
import demo.web_banhoatuoi.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component("flower_data")
@Order(1)
public class Flower_data implements CommandLineRunner {

    @Autowired
    private FlowerRepository flowerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        if (categoryRepository.count() == 0) {
            System.out.println("⏳ Initializing category data...");
            categoryRepository.save(new Category(0, "today's flowers"));
            categoryRepository.save(new Category(0, "DATE's flowers"));
            categoryRepository.save(new Category(0, "Memorial's flowers"));
            System.out.println("✅ Created 3 categories successfully!");
        }

        if (flowerRepository.count() == 0) {
            System.out.println("⏳ Initializing flower data...");

            Category todaysFlowers = categoryRepository.findByCategoryName("today's flowers").get();
            Category dateFlowers = categoryRepository.findByCategoryName("DATE's flowers").get();
            Category memorialFlowers = categoryRepository.findByCategoryName("Memorial's flowers").get();

            List<Flower> flowers = new ArrayList<>();

            // Assign some flowers to each category
            Flower rose = new Flower(0, "rose", 150000, "/images/rose.jpg", "Bo hoa hong dac biet gom: 10 bong hong do tuoi, 2 canh hoa trang tinh khoi, giay goi cao cap va ruy bang lua.", 24, "/images/rose-1.jpg,/images/rose-2.jpg,/images/rose-3.jpg", todaysFlowers);
            flowers.add(rose);

            Flower tulip = new Flower(0, "tulip", 250000, "/images/tulip.jpg", "Bo hoa tulip sang trong voi: 7 canh hoa tulip tim, 4 canh hoa trang, la eucalyptus va giay goi nhung.", 15, "/images/tulip-1.jpg,/images/tulip-2.jpg,/images/tulip-3.jpg", dateFlowers);
            flowers.add(tulip);

            Flower sunflower = new Flower(0, "sunflower", 350000, "/images/sunflower.jpg", "Bo hoa huong duong tuoi sang: 4 canh hoa trang nhe nhang, 5 canh hoa hong pastel, la xanh va ruy bang vang.", 40, "/images/sunflower-1.jpg,/images/sunflower-2.jpg,/images/sunflower-3.jpg", memorialFlowers);
            flowers.add(sunflower);

            Flower iris = new Flower(0, "iris", 450000, "/images/iris.jpg", "Bo hoa iris cao cap bao gom: 5 canh hoa vang ruc ro, 4 canh hoa xanh tu nhien, giay goi kim tuyen va no vang.", 12, "/images/iris-1.jpg,/images/iris-2.jpg", todaysFlowers);
            flowers.add(iris);

            flowerRepository.saveAll(flowers);
            System.out.println("✅ Created " + flowers.size() + " flower sets successfully!");
        } else {
            System.out.println("✅ Flower data already exists. Skipping initialization.");
        }
    }
}
