package mat.pia.sentiment.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ValidationError> errors;
    
    public void addValidationError(String field, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new ValidationError(field, message));
    }
    
    public void addValidationErrors(Map<String, String> errorMap) {
        errorMap.forEach(this::addValidationError);
    }
    
    @Data
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}
