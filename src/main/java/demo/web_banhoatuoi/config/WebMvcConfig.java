package demo.web_banhoatuoi.config;

import demo.web_banhoatuoi.security.AdminAuthInterceptor;
import demo.web_banhoatuoi.security.UserAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /uploads/** to the physical uploads folder
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthInterceptor())
                .addPathPatterns("/admin/**") // bảo vệ tất cả trang admin
                .excludePathPatterns("/login", "/logout", "/css/**", "/js/**", "/images/**");
        registry.addInterceptor(new UserAuthInterceptor())
                // Thêm các đường dẫn mà chỉ user đã đăng nhập mới được phép truy cập
                .addPathPatterns("/cart/**", "/checkout", "/order/process", "/customer/profile", "/customer/orders")
                // Loại trừ đường dẫn tĩnh để tránh lỗi
                .excludePathPatterns("/login", "/logout", "/css/**", "/js/**", "/images/**");
    }
}
