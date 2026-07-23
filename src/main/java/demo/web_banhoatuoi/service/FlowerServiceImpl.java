package demo.web_banhoatuoi.service;

import demo.web_banhoatuoi.entity.Flower;
import demo.web_banhoatuoi.repository.FlowerRepository;
import demo.web_banhoatuoi.service.FlowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FlowerServiceImpl implements FlowerService {

    @Autowired
    private FlowerRepository flowerRepository;

    @Override
    public List<Flower> getAllFlowers() {
        return flowerRepository.findAll();
    }

    @Override
    public Flower getFlowerById(int id) {
        return flowerRepository.findById(id).orElse(null);
    }

    @Override
    public Flower saveFlower(Flower flower) {
        return flowerRepository.save(flower);
    }

    @Override
    public void deleteFlower(int id) {
        flowerRepository.deleteById(id);
    }
}
