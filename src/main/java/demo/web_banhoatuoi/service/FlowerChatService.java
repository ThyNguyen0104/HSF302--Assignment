package demo.web_banhoatuoi.service;

import demo.web_banhoatuoi.entity.Flower;
import demo.web_banhoatuoi.repository.FlowerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FlowerChatService {

    private final FlowerRepository flowerRepository;

    public FlowerChatService(FlowerRepository flowerRepository) {
        this.flowerRepository = flowerRepository;
    }

    public String answer(String rawQuestion) {
        return answerWithImage(rawQuestion).answer();
    }

    @Transactional(readOnly = true)
    public FlowerChatResult answerWithImage(String rawQuestion) {
        String question = normalize(rawQuestion);
        List<Flower> flowers = flowerRepository.findAll();

        if (question.isBlank()) {
            return textOnly("B\u1ea1n h\u00e3y nh\u1eadp c\u00e2u h\u1ecfi v\u1ec1 s\u1ea3n ph\u1ea9m hoa trong c\u1eeda h\u00e0ng.");
        }

        if (flowers.isEmpty()) {
            return textOnly("Hi\u1ec7n t\u1ea1i c\u1eeda h\u00e0ng ch\u01b0a c\u00f3 d\u1eef li\u1ec7u hoa \u0111\u1ec3 t\u01b0 v\u1ea5n.");
        }

        if (isOutOfScope(question)) {
            return textOnly(outOfScopeMessage());
        }

        if (containsAny(question, "re nhat", "gia thap nhat", "thap nhat", "gia re")) {
            return cheapestFlowerAnswer(flowers);
        }

        if (containsAny(question, "dat nhat", "gia cao nhat", "cao nhat")) {
            return mostExpensiveFlowerAnswer(flowers);
        }

        if (containsAny(question, "con hang", "ton kho", "san pham nao con", "hoa nao con")) {
            return textOnly(inStockAnswer(flowers));
        }

        if (containsAny(question, "het hang")) {
            return textOnly(outOfStockAnswer(flowers));
        }

        Optional<Flower> matchedFlower = findFlowerByQuestion(question, flowers);
        if (matchedFlower.isPresent()) {
            return flowerDetailAnswer(matchedFlower.get());
        }

        if (containsAny(question, "hoa", "bo hoa", "san pham", "gia", "mua", "tang", "category", "danh muc")) {
            return textOnly(generalFlowerAnswer(flowers));
        }

        return textOnly(outOfScopeMessage());
    }

    private FlowerChatResult cheapestFlowerAnswer(List<Flower> flowers) {
        return flowers.stream()
                .min(Comparator.comparingDouble(Flower::getPrice))
                .map(flower -> "Hoa r\u1ebb nh\u1ea5t hi\u1ec7n t\u1ea1i:\n"
                        + "- T\u00ean hoa: " + flower.getFlowerName() + "\n"
                        + "- Gi\u00e1: " + formatPrice(flower.getPrice()) + "\n"
                        + "- T\u1ed3n kho: " + flower.getStock() + " s\u1ea3n ph\u1ea9m")
                .map(answer -> withFlowerImage(answer, flowers.stream()
                        .min(Comparator.comparingDouble(Flower::getPrice))
                        .orElse(null)))
                .orElse(textOnly("Hi\u1ec7n t\u1ea1i c\u1eeda h\u00e0ng ch\u01b0a c\u00f3 s\u1ea3n ph\u1ea9m hoa."));
    }

    private FlowerChatResult mostExpensiveFlowerAnswer(List<Flower> flowers) {
        return flowers.stream()
                .max(Comparator.comparingDouble(Flower::getPrice))
                .map(flower -> "Hoa c\u00f3 gi\u00e1 cao nh\u1ea5t hi\u1ec7n t\u1ea1i:\n"
                        + "- T\u00ean hoa: " + flower.getFlowerName() + "\n"
                        + "- Gi\u00e1: " + formatPrice(flower.getPrice()) + "\n"
                        + "- T\u1ed3n kho: " + flower.getStock() + " s\u1ea3n ph\u1ea9m")
                .map(answer -> withFlowerImage(answer, flowers.stream()
                        .max(Comparator.comparingDouble(Flower::getPrice))
                        .orElse(null)))
                .orElse(textOnly("Hi\u1ec7n t\u1ea1i c\u1eeda h\u00e0ng ch\u01b0a c\u00f3 s\u1ea3n ph\u1ea9m hoa."));
    }

    private String inStockAnswer(List<Flower> flowers) {
        List<Flower> inStock = flowers.stream()
                .filter(flower -> flower.getStock() > 0)
                .collect(Collectors.toList());

        if (inStock.isEmpty()) {
            return "Hi\u1ec7n t\u1ea1i t\u1ea5t c\u1ea3 s\u1ea3n ph\u1ea9m hoa \u0111\u1ec1u h\u1ebft h\u00e0ng.";
        }

        String names = inStock.stream()
                .map(flower -> "- " + flower.getFlowerName() + ": " + flower.getStock() + " s\u1ea3n ph\u1ea9m")
                .collect(Collectors.joining("\n"));

        return "C\u00e1c hoa c\u00f2n h\u00e0ng:\n" + names;
    }

    private String outOfStockAnswer(List<Flower> flowers) {
        List<Flower> outOfStock = flowers.stream()
                .filter(flower -> flower.getStock() <= 0)
                .collect(Collectors.toList());

        if (outOfStock.isEmpty()) {
            return "Hi\u1ec7n t\u1ea1i ch\u01b0a c\u00f3 s\u1ea3n ph\u1ea9m n\u00e0o h\u1ebft h\u00e0ng.";
        }

        String names = outOfStock.stream()
                .map(flower -> "- " + flower.getFlowerName())
                .collect(Collectors.joining("\n"));

        return "C\u00e1c hoa \u0111ang h\u1ebft h\u00e0ng:\n" + names;
    }

    private Optional<Flower> findFlowerByQuestion(String question, List<Flower> flowers) {
        return flowers.stream()
                .filter(flower -> question.contains(normalize(flower.getFlowerName())))
                .findFirst();
    }

    private FlowerChatResult flowerDetailAnswer(Flower flower) {
        String categoryName = flower.getCategory() != null ? flower.getCategory().getCategoryName() : "ch\u01b0a c\u00f3 danh m\u1ee5c";
        String stockText = flower.getStock() > 0 ? "C\u00f2n " + flower.getStock() + " s\u1ea3n ph\u1ea9m" : "\u0110ang h\u1ebft h\u00e0ng";
        String description = displayDescription(flower);

        String answer = "C\u00f3 s\u1ea3n ph\u1ea9m n\u00e0y trong c\u1eeda h\u00e0ng:\n"
                + "- T\u00ean hoa: " + flower.getFlowerName() + "\n"
                + "- Danh m\u1ee5c: " + categoryName + "\n"
                + "- Gi\u00e1: " + formatPrice(flower.getPrice()) + "\n"
                + "- T\u00ecnh tr\u1ea1ng: " + stockText + "\n"
                + "- M\u00f4 t\u1ea3: " + description;

        return withFlowerImage(answer, flower);
    }

    private String generalFlowerAnswer(List<Flower> flowers) {
        String productSummary = flowers.stream()
                .limit(5)
                .map(flower -> "- " + flower.getFlowerName() + ": " + formatPrice(flower.getPrice()))
                .collect(Collectors.joining("\n"));

        return "C\u1eeda h\u00e0ng hi\u1ec7n c\u00f3 c\u00e1c s\u1ea3n ph\u1ea9m hoa:\n"
                + productSummary
                + "\n\nB\u1ea1n c\u00f3 th\u1ec3 h\u1ecfi: 'Hoa n\u00e0o r\u1ebb nh\u1ea5t?', 'C\u00f3 hoa rose kh\u00f4ng?', ho\u1eb7c 'Hoa n\u00e0o c\u00f2n h\u00e0ng?'.";
    }

    private String displayDescription(Flower flower) {
        String flowerName = normalize(flower.getFlowerName());
        return switch (flowerName) {
            case "rose" -> "B\u00f3 hoa h\u1ed3ng \u0111\u1eb7c bi\u1ec7t g\u1ed3m: 10 b\u00f4ng h\u1ed3ng \u0111\u1ecf t\u01b0\u01a1i, 2 c\u00e0nh hoa tr\u1eafng tinh kh\u00f4i, gi\u1ea5y g\u00f3i cao c\u1ea5p v\u00e0 ruy b\u0103ng l\u1ee5a.";
            case "tulip" -> "B\u00f3 hoa tulip sang tr\u1ecdng v\u1edbi: 7 c\u00e0nh hoa tulip t\u00edm, 4 c\u00e0nh hoa tr\u1eafng, l\u00e1 eucalyptus v\u00e0 gi\u1ea5y g\u00f3i nhung.";
            case "sunflower" -> "B\u00f3 hoa h\u01b0\u1edbng d\u01b0\u01a1ng t\u01b0\u01a1i s\u00e1ng: 4 c\u00e0nh hoa tr\u1eafng nh\u1eb9 nh\u00e0ng, 5 c\u00e0nh hoa h\u1ed3ng pastel, l\u00e1 xanh v\u00e0 ruy b\u0103ng v\u00e0ng.";
            case "iris" -> "B\u00f3 hoa iris cao c\u1ea5p bao g\u1ed3m: 5 c\u00e0nh hoa v\u00e0ng r\u1ef1c r\u1ee1, 4 c\u00e0nh hoa xanh t\u1ef1 nhi\u00ean, gi\u1ea5y g\u00f3i kim tuy\u1ebfn v\u00e0 n\u01a1 v\u00e0ng.";
            default -> flower.getDescription() != null && !flower.getDescription().isBlank()
                    ? flower.getDescription()
                    : "Ch\u01b0a c\u00f3 m\u00f4 t\u1ea3 cho s\u1ea3n ph\u1ea9m n\u00e0y.";
        };
    }

    private boolean isOutOfScope(String question) {
        return containsAny(question,
                "thoi tiet", "tin tuc", "bong da", "tour", "du lich", "khach san",
                "may bay", "phu quoc", "da nang", "ha noi", "stripe", "paypal");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String outOfScopeMessage() {
        return "Xin l\u1ed7i, t\u00f4i ch\u1ec9 c\u00f3 th\u1ec3 tr\u1ea3 l\u1eddi c\u00e1c c\u00e2u h\u1ecfi li\u00ean quan \u0111\u1ebfn s\u1ea3n ph\u1ea9m hoa trong c\u1eeda h\u00e0ng.";
    }

    private FlowerChatResult textOnly(String answer) {
        return new FlowerChatResult(answer, null, null);
    }

    private FlowerChatResult withFlowerImage(String answer, Flower flower) {
        if (flower == null || flower.getImagePath() == null || flower.getImagePath().isBlank()) {
            return textOnly(answer);
        }
        return new FlowerChatResult(answer, flower.getImagePath(), "/customer/order/" + flower.getFlowerId());
    }

    private String formatPrice(double price) {
        return String.format(Locale.US, "%,.0f", price).replace(",", ".") + " \u0111";
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        return normalized.toLowerCase(Locale.ROOT).replace('\u0111', 'd').trim();
    }

    public record FlowerChatResult(String answer, String imageUrl, String productUrl) {
    }
}
