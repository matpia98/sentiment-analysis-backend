package mat.pia.sentiment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentResponse {
    
    private String text;
    
    private SentimentType sentiment;
    
    private double confidence;
    
    private String analysis;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    public enum SentimentType {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }
}
