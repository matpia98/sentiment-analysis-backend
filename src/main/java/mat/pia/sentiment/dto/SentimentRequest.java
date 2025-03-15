package mat.pia.sentiment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentRequest {
    
    @NotBlank(message = "Text for sentiment analysis cannot be empty")
    @Size(min = 1, max = 5000, message = "Text must be between 1 and 5000 characters")
    private String text;
}