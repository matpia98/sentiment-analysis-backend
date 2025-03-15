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
    
    List<SentimentEntity> findByPrimaryEmotion(SentimentResponse.EmotionType primaryEmotion);
    
    List<SentimentEntity> findBySentimentAndPrimaryEmotion(
        SentimentResponse.SentimentType sentiment, 
        SentimentResponse.EmotionType primaryEmotion);
}
