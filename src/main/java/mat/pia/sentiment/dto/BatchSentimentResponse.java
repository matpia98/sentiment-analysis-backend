package mat.pia.sentiment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mat.pia.sentiment.model.SentimentResponse;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchSentimentResponse {

    private List<SentimentResponse> results;
    private LocalDateTime timestamp;
    private BatchSummary summary;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchSummary {
        private int totalRequests;
        private int processedRequests;
        private int positiveCount;
        private int negativeCount;
        private int neutralCount;
        private SentimentResponse.SentimentType dominantSentiment;
        private SentimentResponse.EmotionType dominantEmotion;
        private double averageConfidence;
    }
}