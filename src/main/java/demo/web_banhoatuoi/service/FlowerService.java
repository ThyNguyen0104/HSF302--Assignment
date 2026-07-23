package demo.web_banhoatuoi.service;

import demo.web_banhoatuoi.entity.Flower;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FlowerService {
    List<Flower> getAllFlowers();
    Flower getFlowerById(int id);
    Flower saveFlower(Flower flower);
    void deleteFlower(int id);
}
