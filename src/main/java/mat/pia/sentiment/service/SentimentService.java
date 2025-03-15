package mat.pia.sentiment.service;

import mat.pia.sentiment.dto.BatchSentimentRequest;
import mat.pia.sentiment.dto.BatchSentimentResponse;
import mat.pia.sentiment.dto.SentimentDTO;
import mat.pia.sentiment.model.SentimentRequest;
import mat.pia.sentiment.model.SentimentResponse;

import java.util.List;

public interface SentimentService {
    SentimentResponse analyzeSentiment(SentimentRequest request);
    
    BatchSentimentResponse analyzeBatch(BatchSentimentRequest batchRequest);
    
    SentimentDTO findById(Long id);
    
    List<SentimentDTO> findAll();
    
    List<SentimentDTO> findBySentimentType(SentimentResponse.SentimentType sentimentType);
    
    List<SentimentDTO> findByPrimaryEmotion(SentimentResponse.EmotionType emotionType);
    
    List<SentimentDTO> findBySentimentAndEmotion(
        SentimentResponse.SentimentType sentimentType, 
        SentimentResponse.EmotionType emotionType);
}