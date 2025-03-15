package mat.pia.sentiment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mat.pia.sentiment.model.SentimentResponse;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentDTO {
    private Long id;
    private String text;
    private SentimentResponse.SentimentType sentiment;
    private double confidence;
    private String analysis;
    private SentimentResponse.EmotionType primaryEmotion;
    private Map<SentimentResponse.EmotionType, Double> emotionScores;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    private String source;
    private String apiProvider;
}