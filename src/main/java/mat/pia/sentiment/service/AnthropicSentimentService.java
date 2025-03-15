package mat.pia.sentiment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.List;
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
                "You are a sentiment analysis expert. Analyze the sentiment of the text provided by the user and respond with only one of these words: POSITIVE, NEGATIVE, or NEUTRAL. " +
                "Also provide a confidence score between 0 and 1, and a brief explanation. " +
                "Format your response as JSON with fields: sentiment, confidence, analysis.");

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
            double confidence = contentNode.path("confidence").asDouble(0.5);
            String analysis = contentNode.path("analysis").asText("No analysis provided");

            SentimentResponse.SentimentType sentimentType;
            try {
                sentimentType = SentimentResponse.SentimentType.valueOf(sentimentStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid sentiment type received: {}, defaulting to NEUTRAL", sentimentStr);
                sentimentType = SentimentResponse.SentimentType.NEUTRAL;
            }

            SentimentResponse response = SentimentResponse.builder()
                    .text(request.getText())
                    .sentiment(sentimentType)
                    .confidence(confidence)
                    .analysis(analysis)
                    .timestamp(LocalDateTime.now())
                    .build();

            SentimentEntity entity = SentimentEntity.builder()
                    .text(request.getText())
                    .sentiment(sentimentType)
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

        return "{\"sentiment\":\"NEUTRAL\",\"confidence\":0.5,\"analysis\":\"Could not extract valid JSON from Claude response\"}";
    }

    private SentimentDTO mapToDto(SentimentEntity entity) {
        return SentimentDTO.builder()
                .id(entity.getId())
                .text(entity.getText())
                .sentiment(entity.getSentiment())
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
}