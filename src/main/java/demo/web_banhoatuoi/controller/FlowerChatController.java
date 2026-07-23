package demo.web_banhoatuoi.controller;

import demo.web_banhoatuoi.service.CartService;
import demo.web_banhoatuoi.service.FlowerChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/customer/chat")
public class FlowerChatController {

    private final FlowerChatService flowerChatService;
    private final CartService cartService;

    public FlowerChatController(FlowerChatService flowerChatService, CartService cartService) {
        this.flowerChatService = flowerChatService;
        this.cartService = cartService;
    }

    @ModelAttribute("cartItemCount")
    public int cartItemCount(HttpSession session) {
        return cartService.getCartItemCount(session);
    }

    @GetMapping
    public String chatPage(Model model) {
        model.addAttribute("suggestions", new String[]{
                "Hoa n\u00e0o r\u1ebb nh\u1ea5t?",
                "C\u00f3 hoa rose kh\u00f4ng?",
                "Hoa n\u00e0o c\u00f2n h\u00e0ng?",
                "Th\u1eddi ti\u1ebft h\u00f4m nay?"
        });
        return "customer/chat";
    }

    @PostMapping("/ask")
    @ResponseBody
    public ChatResponse ask(@RequestBody ChatRequest request) {
        FlowerChatService.FlowerChatResult result = flowerChatService.answerWithImage(request.question());
        return new ChatResponse(result.answer(), result.imageUrl(), result.productUrl());
    }

    public record ChatRequest(String question) {
    }

    public record ChatResponse(String answer, String imageUrl, String productUrl) {
    }
}
