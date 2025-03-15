package mat.pia.sentiment.service;

import mat.pia.sentiment.dto.SentimentDTO;
import mat.pia.sentiment.model.SentimentRequest;
import mat.pia.sentiment.model.SentimentResponse;

import java.util.List;
import java.util.Optional;

public interface SentimentService {
    SentimentResponse analyzeSentiment(SentimentRequest request);
    SentimentDTO findById(Long id);
    List<SentimentDTO> findAll();
    List<SentimentDTO> findBySentimentType(SentimentResponse.SentimentType sentimentType);
}