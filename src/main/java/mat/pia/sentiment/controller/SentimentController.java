package mat.pia.sentiment.controller;

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
        log.info("Received sentiment analysis request for text: {}", 
            request.getText().substring(0, Math.min(50, request.getText().length())));
        
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
}
