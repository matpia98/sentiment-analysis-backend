package mat.pia.sentiment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mat.pia.sentiment.dto.BatchSentimentRequest;
import mat.pia.sentiment.dto.BatchSentimentResponse;
import mat.pia.sentiment.dto.SentimentDTO;
import mat.pia.sentiment.exception.ApiException;
import mat.pia.sentiment.exception.ResourceNotFoundException;
import mat.pia.sentiment.model.SentimentEntity;
import mat.pia.sentiment.model.SentimentRequest;
import mat.pia.sentiment.model.SentimentResponse;
import mat.pia.sentiment.repository.SentimentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AnthropicSentimentService implements SentimentService {

    private final WebClient webClient;
    private final SentimentRepository sentimentRepository;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.model:claude-3-haiku-20240307}")
    private String model;

    @Value("${anthropic.max-tokens:1000}")
    private int maxTokens;

    @Autowired
    public AnthropicSentimentService(
            @Qualifier("anthropicWebClient") WebClient webClient,
            SentimentRepository sentimentRepository,
            ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.sentimentRepository = sentimentRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public SentimentResponse analyzeSentiment(SentimentRequest request) {
        log.info("Analyzing sentiment using Anthropic Claude for text: {}",
            request.getText().substring(0, Math.min(50, request.getText().length())));

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            
            requestBody.put("system",
                "You are a sentiment and emotion analysis expert. Analyze the text and provide:\n" +
                "1. Overall sentiment (POSITIVE, NEGATIVE, or NEUTRAL)\n" +
                "2. Primary emotion (choose one: JOY, SADNESS, ANGER, FEAR, SURPRISE, DISGUST, TRUST, ANTICIPATION, or NONE)\n" +
                "3. Emotion scores - rate each emotion (JOY, SADNESS, ANGER, FEAR, SURPRISE, DISGUST, TRUST, ANTICIPATION) from 0-1\n" +
                "4. Confidence score for overall sentiment (0-1)\n" +
                "5. Brief analysis explaining the emotional tone\n\n" +
                "Format response as JSON with fields: sentiment, primaryEmotion, emotionScores, confidence, analysis.");

            ObjectNode userNode = objectMapper.createObjectNode();
            userNode.put("role", "user");
            userNode.put("content", request.getText());

            // Add only the user message to the messages array
            requestBody.set("messages", objectMapper.createArrayNode().add(userNode));

            String responseJson = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseNode = objectMapper.readTree(responseJson);
            String content = responseNode.path("content").path(0).path("text").asText();

            String jsonContent = extractJsonFromContent(content);
            JsonNode contentNode = objectMapper.readTree(jsonContent);

            String sentimentStr = contentNode.path("sentiment").asText("NEUTRAL");
            String primaryEmotionStr = contentNode.path("primaryEmotion").asText("NONE");
            double confidence = contentNode.path("confidence").asDouble(0.5);
            String analysis = contentNode.path("analysis").asText("No analysis provided");
            
            Map<SentimentResponse.EmotionType, Double> emotionScores = new HashMap<>();
            JsonNode emotionScoresNode = contentNode.path("emotionScores");
            
            if (emotionScoresNode.isObject()) {
                for (SentimentResponse.EmotionType emotion : SentimentResponse.EmotionType.values()) {
                    if (emotion != SentimentResponse.EmotionType.NONE) {
                        double score = emotionScoresNode.path(emotion.name()).asDouble(0.0);
                        emotionScores.put(emotion, score);
                    }
                }
            }

            SentimentResponse.SentimentType sentimentType;
            try {
                sentimentType = SentimentResponse.SentimentType.valueOf(sentimentStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid sentiment type received: {}, defaulting to NEUTRAL", sentimentStr);
                sentimentType = SentimentResponse.SentimentType.NEUTRAL;
            }
            
            SentimentResponse.EmotionType primaryEmotion;
            try {
                primaryEmotion = SentimentResponse.EmotionType.valueOf(primaryEmotionStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid emotion type received: {}, defaulting to NONE", primaryEmotionStr);
                primaryEmotion = SentimentResponse.EmotionType.NONE;
            }

            SentimentResponse response = SentimentResponse.builder()
                    .text(request.getText())
                    .sentiment(sentimentType)
                    .primaryEmotion(primaryEmotion)
                    .emotionScores(emotionScores)
                    .confidence(confidence)
                    .analysis(analysis)
                    .timestamp(LocalDateTime.now())
                    .build();

            String emotionDetailsJson = objectMapper.writeValueAsString(emotionScores);
            
            SentimentEntity entity = SentimentEntity.builder()
                    .text(request.getText())
                    .sentiment(sentimentType)
                    .primaryEmotion(primaryEmotion)
                    .emotionDetails(emotionDetailsJson)
                    .confidence(confidence)
                    .analysis(analysis)
                    .createdAt(LocalDateTime.now())
                    .source(request.getSource())
                    .apiProvider("ANTHROPIC")
                    .build();

            sentimentRepository.save(entity);

            return response;

        } catch (WebClientResponseException e) {
            log.error("Error from Anthropic API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException("Failed to get response from Anthropic API: " + e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Error while analyzing sentiment with Anthropic Claude", e);
            throw new ApiException("Failed to analyze sentiment with Anthropic Claude", e);
        }
    }

    private String extractJsonFromContent(String content) {
        int jsonStart = content.indexOf("{");
        int jsonEnd = content.lastIndexOf("}");

        if (jsonStart >= 0 && jsonEnd >= 0 && jsonEnd > jsonStart) {
            return content.substring(jsonStart, jsonEnd + 1);
        }

        return "{\"sentiment\":\"NEUTRAL\",\"confidence\":0.5,\"analysis\":\"Could not extract valid JSON from Claude response\",\"primaryEmotion\":\"NONE\",\"emotionScores\":{}}";
    }

    private SentimentDTO mapToDto(SentimentEntity entity) {
        Map<SentimentResponse.EmotionType, Double> emotionScores = new HashMap<>();
        try {
            if (entity.getEmotionDetails() != null && !entity.getEmotionDetails().isEmpty()) {
                // Convert from string-based map to enum-based map
                Map<String, Double> rawMap = objectMapper.readValue(
                    entity.getEmotionDetails(),
                        new TypeReference<>() {
                        }
                );
                
                rawMap.forEach((key, value) -> {
                    try {
                        SentimentResponse.EmotionType emotion = SentimentResponse.EmotionType.valueOf(key);
                        emotionScores.put(emotion, value);
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown emotion type in stored data: {}", key);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error parsing emotion details JSON", e);
        }
        
        return SentimentDTO.builder()
                .id(entity.getId())
                .text(entity.getText())
                .sentiment(entity.getSentiment())
                .primaryEmotion(entity.getPrimaryEmotion())
                .emotionScores(emotionScores)
                .confidence(entity.getConfidence())
                .analysis(entity.getAnalysis())
                .createdAt(entity.getCreatedAt())
                .source(entity.getSource())
                .apiProvider(entity.getApiProvider())
                .build();
    }

    @Override
    public SentimentDTO findById(Long id) {
        return sentimentRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Sentiment", "id", id));
    }

    @Override
    public List<SentimentDTO> findAll() {
        return sentimentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SentimentDTO> findBySentimentType(SentimentResponse.SentimentType sentimentType) {
        return sentimentRepository.findBySentiment(sentimentType).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SentimentDTO> findByPrimaryEmotion(SentimentResponse.EmotionType emotionType) {
        return sentimentRepository.findByPrimaryEmotion(emotionType)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SentimentDTO> findBySentimentAndEmotion(
            SentimentResponse.SentimentType sentimentType, 
            SentimentResponse.EmotionType emotionType) {
        return sentimentRepository.findBySentimentAndPrimaryEmotion(sentimentType, emotionType)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BatchSentimentResponse analyzeBatch(BatchSentimentRequest batchRequest) {
        log.info("Processing batch sentiment analysis with {} requests", batchRequest.getRequests().size());
        
        List<SentimentResponse> results = batchRequest.getRequests().stream()
                .map(this::analyzeSentiment)
                .collect(Collectors.toList());
        
        int totalRequests = batchRequest.getRequests().size();
        int processedRequests = results.size();
        
        int positiveCount = 0;
        int negativeCount = 0;
        int neutralCount = 0;
        
        Map<SentimentResponse.SentimentType, Integer> sentimentCounts = new HashMap<>();
        Map<SentimentResponse.EmotionType, Integer> emotionCounts = new HashMap<>();
        double totalConfidence = 0;
        
        for (SentimentResponse response : results) {
            switch (response.getSentiment()) {
                case POSITIVE:
                    positiveCount++;
                    break;
                case NEGATIVE:
                    negativeCount++;
                    break;
                case NEUTRAL:
                    neutralCount++;
                    break;
            }
            
            sentimentCounts.merge(response.getSentiment(), 1, Integer::sum);
            
            emotionCounts.merge(response.getPrimaryEmotion(), 1, Integer::sum);
            
            totalConfidence += response.getConfidence();
        }
        
        SentimentResponse.SentimentType dominantSentiment = sentimentCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(SentimentResponse.SentimentType.NEUTRAL);
        
        SentimentResponse.EmotionType dominantEmotion = emotionCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(SentimentResponse.EmotionType.NONE);
        
        double averageConfidence = processedRequests > 0 ? totalConfidence / processedRequests : 0;
        
        BatchSentimentResponse.BatchSummary summary = BatchSentimentResponse.BatchSummary.builder()
                .totalRequests(totalRequests)
                .processedRequests(processedRequests)
                .positiveCount(positiveCount)
                .negativeCount(negativeCount)
                .neutralCount(neutralCount)
                .dominantSentiment(dominantSentiment)
                .dominantEmotion(dominantEmotion)
                .averageConfidence(averageConfidence)
                .build();
        
        return BatchSentimentResponse.builder()
                .results(results)
                .timestamp(LocalDateTime.now())
                .summary(summary)
                .build();
    }
}