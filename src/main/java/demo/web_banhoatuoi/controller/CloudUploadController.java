package demo.web_banhoatuoi.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/admin/cloud-upload")
public class CloudUploadController {

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Trang giao diện upload ảnh lên cloud
     */
    @GetMapping
    public String cloudUploadPage() {
        return "admin/cloud-upload";
    }

    /**
     * REST API: Nhận file từ drag&drop, upload lên Cloudinary (hoặc local nếu chưa config)
     * Trả về JSON { success, url, publicId, width, height, format, bytes }
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "flowers") String folder
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "Không có file nào được chọn.");
            return ResponseEntity.badRequest().body(result);
        }

        // Kiểm tra định dạng file
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            result.put("success", false);
            result.put("message", "Chỉ chấp nhận file ảnh (jpg, png, webp, gif).");
            return ResponseEntity.badRequest().body(result);
        }

        // Kiểm tra kích thước (tối đa 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            result.put("success", false);
            result.put("message", "File quá lớn. Tối đa 10MB.");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            // Thử upload lên Cloudinary thật
            Map<?, ?> uploadResult = tryCloudinaryUpload(file, folder);

            if (uploadResult != null) {
                // Upload Cloudinary thành công
                String secureUrl = (String) uploadResult.get("secure_url");
                String publicId  = (String) uploadResult.get("public_id");
                int    width     = uploadResult.get("width")  != null ? (int) uploadResult.get("width")  : 0;
                int    height    = uploadResult.get("height") != null ? (int) uploadResult.get("height") : 0;
                String format    = (String) uploadResult.get("format");
                long   bytes     = uploadResult.get("bytes")  != null ? ((Number) uploadResult.get("bytes")).longValue() : 0L;

                result.put("success",  true);
                result.put("source",   "cloudinary");
                result.put("url",      secureUrl);
                result.put("publicId", publicId);
                result.put("width",    width);
                result.put("height",   height);
                result.put("format",   format);
                result.put("bytes",    bytes);
                result.put("message",  "✅ Upload Cloudinary thành công!");
            } else {
                // Fallback: lưu local, trả về mock cloud URL
                String localUrl = saveToLocal(file, folder);
                result.put("success",  true);
                result.put("source",   "local");
                result.put("url",      localUrl);
                result.put("publicId", "local/" + folder + "/" + file.getOriginalFilename());
                result.put("width",    0);
                result.put("height",   0);
                result.put("format",   getExtension(file.getOriginalFilename()));
                result.put("bytes",    file.getSize());
                result.put("message",  "✅ Lưu local thành công! (Cấu hình Cloudinary để upload cloud thật)");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi upload: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Thử upload lên Cloudinary thật. Trả null nếu không có credentials hợp lệ.
     */
    private Map<?, ?> tryCloudinaryUpload(MultipartFile file, String folder) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder",           "banhoatuoi/" + folder,
                    "use_filename",     true,
                    "unique_filename",  true,
                    "overwrite",        false,
                    "resource_type",    "image"
            );
            // Chuyển MultipartFile -> byte[] để upload
            byte[] bytes = file.getBytes();
            Map<?, ?> res = cloudinary.uploader().upload(bytes, options);

            // Nếu Cloudinary trả về URL chứa "demo" cloud name => không config thật
            String url = (String) res.get("secure_url");
            if (url != null && url.contains("res.cloudinary.com")) {
                return res;
            }
            return null;
        } catch (Exception e) {
            // Credentials chưa được cấu hình -> fallback local
            return null;
        }
    }

    /**
     * Lưu file vào local uploads/ và trả về URL có thể truy cập qua HTTP
     */
    private String saveToLocal(MultipartFile file, String folder) throws IOException {
        String uploadDir = new File("uploads/" + folder + "/").getAbsolutePath();
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) uploadPath.mkdirs();

        String fileName = UUID.randomUUID() + "_" + sanitizeFileName(file.getOriginalFilename());
        file.transferTo(new File(uploadPath, fileName));
        return "/uploads/" + folder + "/" + fileName;
    }

    private String sanitizeFileName(String name) {
        if (name == null) return "image.jpg";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getExtension(String filename) {
        if (filename == null) return "jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "jpg";
    }
}
