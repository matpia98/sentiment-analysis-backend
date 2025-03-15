package mat.pia.sentiment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentResponse {
    
    private Long id;
    private String text;
    private String sentiment;
    private Double confidenceScore;
    private String explanation;
    private String analyzedAt;
}