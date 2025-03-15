package mat.pia.sentiment.controller;

import mat.pia.sentiment.dto.BatchSentimentRequest;
import mat.pia.sentiment.dto.BatchSentimentResponse;
import mat.pia.sentiment.dto.SentimentDTO;
import mat.pia.sentiment.exception.ResourceNotFoundException;
import mat.pia.sentiment.model.SentimentEntity;
import mat.pia.sentiment.model.SentimentRequest;
import mat.pia.sentiment.model.SentimentResponse;
import mat.pia.sentiment.service.SentimentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sentiment")
@Slf4j
@CrossOrigin(origins = "*")
public class SentimentController {

    private final SentimentService sentimentService;
    
    @Autowired
    public SentimentController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }
    
    @PostMapping("/analyze")
    public ResponseEntity<SentimentResponse> analyzeSentiment(@Valid @RequestBody SentimentRequest request) {
        log.info("Received sentiment analysis request for text: {}", request.getText());
        SentimentResponse response = sentimentService.analyzeSentiment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<SentimentDTO>> getSentimentHistory() {
        log.info("Retrieving sentiment analysis history");
        List<SentimentDTO> history = sentimentService.findAll();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<SentimentDTO> getSentimentById(@PathVariable Long id) {
        log.info("Retrieving sentiment analysis with ID: {}", id);
        SentimentDTO sentiment = sentimentService.findById(id);
        return ResponseEntity.ok(sentiment);
    }

    @GetMapping("/history/type/{type}")
    public ResponseEntity<List<SentimentDTO>> getSentimentByType(
            @PathVariable SentimentResponse.SentimentType type) {
        log.info("Retrieving sentiment analyses with type: {}", type);
        List<SentimentDTO> results = sentimentService.findBySentimentType(type);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/history/emotion/{emotion}")
    public ResponseEntity<List<SentimentDTO>> getSentimentByEmotion(
            @PathVariable SentimentResponse.EmotionType emotion) {
        log.info("Retrieving sentiment analyses with emotion: {}", emotion);
        List<SentimentDTO> results = sentimentService.findByPrimaryEmotion(emotion);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/history/type/{type}/emotion/{emotion}")
    public ResponseEntity<List<SentimentDTO>> getSentimentByTypeAndEmotion(
            @PathVariable SentimentResponse.SentimentType type,
            @PathVariable SentimentResponse.EmotionType emotion) {
        log.info("Retrieving sentiment analyses with type: {} and emotion: {}", type, emotion);
        List<SentimentDTO> results = sentimentService.findBySentimentAndEmotion(type, emotion);
        return ResponseEntity.ok(results);
    }
    
    @PostMapping("/analyze/batch")
    public ResponseEntity<BatchSentimentResponse> analyzeBatchSentiment(
            @Valid @RequestBody BatchSentimentRequest batchRequest) {
        log.info("Received batch sentiment analysis request with {} texts", 
                batchRequest.getRequests().size());
        
        BatchSentimentResponse response = sentimentService.analyzeBatch(batchRequest);
        
        log.info("Completed batch sentiment analysis. Dominant sentiment: {}, Dominant emotion: {}", 
                response.getSummary().getDominantSentiment(), 
                response.getSummary().getDominantEmotion());
        
        return ResponseEntity.ok(response);
    }
}
