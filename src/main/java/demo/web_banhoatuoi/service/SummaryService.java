package demo.web_banhoatuoi.service;

import demo.web_banhoatuoi.entity.Summary;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SummaryService {
    List<Summary> getAllSummaries();
    Optional<Summary> getSummaryById(int id);
    Optional<Summary> getSummaryByDate(LocalDate date);
    List<Summary> getSummariesBetween(LocalDate startDate, LocalDate endDate);
    Summary getLatestSummary();
    boolean existsByDate(LocalDate date);
    Summary saveSummary(Summary summary);
}
