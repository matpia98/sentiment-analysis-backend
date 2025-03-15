package mat.pia.sentiment.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentRequest {
    
    @NotBlank(message = "Text cannot be empty")
    private String text;
    
    private String source;
}
