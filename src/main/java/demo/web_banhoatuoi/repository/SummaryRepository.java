package demo.web_banhoatuoi.repository;

import demo.web_banhoatuoi.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, Integer> {

    // Find summary by date
    Optional<Summary> findBySummaryDate(LocalDate date);

    // Find summaries by date range
    List<Summary> findBySummaryDateBetween(LocalDate startDate, LocalDate endDate);

    // Get latest summary
    Summary findTopByOrderBySummaryDateDesc();

    // Check if summary exists for date
    boolean existsBySummaryDate(LocalDate date);
}