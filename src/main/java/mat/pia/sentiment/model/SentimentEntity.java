package mat.pia.sentiment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sentiment_analyses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 2000)
    private String text;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SentimentResponse.SentimentType sentiment;
    
    @Column(nullable = false)
    private double confidence;
    
    @Column(length = 500)
    private String analysis;
    
    @Column
    @Enumerated(EnumType.STRING)
    private SentimentResponse.EmotionType primaryEmotion;
    
    @Column(length = 1000)
    private String emotionDetails;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(length = 100)
    private String source;
    
    @Column(length = 50)
    private String apiProvider;
}
