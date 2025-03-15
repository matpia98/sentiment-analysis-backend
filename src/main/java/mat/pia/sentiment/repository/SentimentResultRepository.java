package mat.pia.sentiment.repository;

import mat.pia.sentiment.model.SentimentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SentimentResultRepository extends JpaRepository<SentimentResult, Long> {
    
    List<SentimentResult> findBySentiment(String sentiment);
    
    List<SentimentResult> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<SentimentResult> findByConfidenceScoreGreaterThan(Double threshold);
}