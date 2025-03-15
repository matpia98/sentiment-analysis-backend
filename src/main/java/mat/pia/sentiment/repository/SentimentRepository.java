package mat.pia.sentiment.repository;

import mat.pia.sentiment.model.SentimentEntity;
import mat.pia.sentiment.model.SentimentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SentimentRepository extends JpaRepository<SentimentEntity, Long> {
    
    List<SentimentEntity> findBySentiment(SentimentResponse.SentimentType sentiment);
    
    List<SentimentEntity> findByConfidenceGreaterThanEqual(double confidenceThreshold);
    
    List<SentimentEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<SentimentEntity> findBySource(String source);
    
    List<SentimentEntity> findByApiProvider(String apiProvider);
}
