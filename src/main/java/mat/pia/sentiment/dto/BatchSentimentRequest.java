package mat.pia.sentiment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mat.pia.sentiment.model.SentimentRequest;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchSentimentRequest {

    @NotEmpty(message = "Batch request must contain at least one text entry")
    @Size(max = 10, message = "Batch size cannot exceed 10 entries")
    private List<@Valid SentimentRequest> requests;
}