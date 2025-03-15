package mat.pia.sentiment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mat.pia.sentiment.model.SentimentResponse;

import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    private String source;
    private String apiProvider;
}