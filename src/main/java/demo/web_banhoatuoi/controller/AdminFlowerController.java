package demo.web_banhoatuoi.controller;

import demo.web_banhoatuoi.entity.Flower;
import demo.web_banhoatuoi.service.CategoryService;
import demo.web_banhoatuoi.service.FlowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/admin/flowers")
public class AdminFlowerController {

    @Autowired
    private FlowerService flowerService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listFlowers(Model model) {
        model.addAttribute("flowers", flowerService.getAllFlowers());
        return "admin/flower-list";
    }

    @GetMapping("/add")
    public String addFlowerForm(Model model) {
        model.addAttribute("flower", new Flower());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/flower-form";
    }

    @PostMapping("/save")
    public String saveFlower(@ModelAttribute Flower flower,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             @RequestParam(value = "additionalImageFiles", required = false) MultipartFile[] additionalFiles,
                             @RequestParam(value = "oldImagePath", required = false) String oldImagePath,
                             @RequestParam(value = "existingAdditionalImages", required = false) String existingAdditionalImages)
            throws IOException {

        String uploadDir = new File("uploads/flowers/").getAbsolutePath();
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) uploadPath.mkdirs();

        if (!imageFile.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            imageFile.transferTo(new File(uploadPath, fileName));
            flower.setImagePath("/uploads/flowers/" + fileName);
        } else {
            flower.setImagePath(oldImagePath);
        }

        StringBuilder sb = new StringBuilder();

        if (existingAdditionalImages != null && !existingAdditionalImages.isEmpty()) {
            sb.append(existingAdditionalImages);
        }

        if (additionalFiles != null && additionalFiles.length > 0) {
            for (MultipartFile f : additionalFiles) {
                if (!f.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + f.getOriginalFilename();
                    f.transferTo(new File(uploadPath, fileName));
                    if (sb.length() > 0) sb.append(",");
                    sb.append("/uploads/flowers/").append(fileName);
                }
            }
        }

        flower.setAdditionalImages(sb.toString());

        flowerService.saveFlower(flower);
        return "redirect:/admin/flowers";
    }



    @GetMapping("/edit/{id}")
    public String editFlowerForm(@PathVariable int id, Model model) {
        Flower flower = flowerService.getFlowerById(id);
        model.addAttribute("flower", flower);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/flower-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteFlower(@PathVariable int id) {
        flowerService.deleteFlower(id);
        return "redirect:/admin/flowers";
    }
}
