package demo.web_banhoatuoi.service;

import demo.web_banhoatuoi.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    List<Category> getAllCategories();

    Category getCategoryById(int categoryId);
}
