package demo.web_banhoatuoi.service;

import demo.web_banhoatuoi.entity.Summary;
import demo.web_banhoatuoi.repository.SummaryRepository;
import demo.web_banhoatuoi.service.SummaryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SummaryServiceImpl implements SummaryService {

    private final SummaryRepository summaryRepository;

    public SummaryServiceImpl(SummaryRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    @Override
    public List<Summary> getAllSummaries() {
        return summaryRepository.findAll();
    }

    @Override
    public Optional<Summary> getSummaryById(int id) {
        return summaryRepository.findById(id);
    }

    @Override
    public Optional<Summary> getSummaryByDate(LocalDate date) {
        return summaryRepository.findBySummaryDate(date);
    }

    @Override
    public List<Summary> getSummariesBetween(LocalDate startDate, LocalDate endDate) {
        return summaryRepository.findBySummaryDateBetween(startDate, endDate);
    }

    @Override
    public Summary getLatestSummary() {
        return summaryRepository.findTopByOrderBySummaryDateDesc();
    }

    @Override
    public boolean existsByDate(LocalDate date) {
        return summaryRepository.existsBySummaryDate(date);
    }

    @Override
    public Summary saveSummary(Summary summary) {
        return summaryRepository.save(summary);
    }
}
