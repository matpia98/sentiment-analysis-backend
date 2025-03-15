package mat.pia.sentiment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentResponse {
    
    private String text;
    
    private SentimentType sentiment;
    
    private double confidence;
    
    private String analysis;
    
    private EmotionType primaryEmotion;
    
    private Map<EmotionType, Double> emotionScores;
    
    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();
    
    public enum SentimentType {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }
    
    public enum EmotionType {
        JOY,
        SADNESS,
        ANGER,
        FEAR,
        SURPRISE,
        DISGUST,
        TRUST,
        ANTICIPATION,
        NONE
    }
}
