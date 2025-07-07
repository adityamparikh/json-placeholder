package dev.aparikh.jsonplaceholder.service;

import dev.aparikh.jsonplaceholder.config.ClaudeConfig;
import dev.aparikh.jsonplaceholder.dto.claude.ClaudeRequest;
import dev.aparikh.jsonplaceholder.dto.claude.ClaudeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ClaudeService
 */
@ExtendWith(MockitoExtension.class)
class ClaudeServiceTest {

    @Mock
    private WebClient claudeWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ClaudeConfig.ClaudeProperties claudeProperties;
    private ClaudeService claudeService;

    @BeforeEach
    void setUp() {
        claudeProperties = new ClaudeConfig.ClaudeProperties();
        claudeProperties.setApiKey("test-api-key");
        claudeProperties.setDefaultModel("claude-3-5-sonnet-20241022");
        claudeProperties.setDefaultMaxTokens(1000);
        claudeProperties.setDefaultTemperature(0.7);
        claudeProperties.setTimeoutSeconds(30L);

        claudeService = new ClaudeService(claudeWebClient, claudeProperties);
    }

    @Test
    void testCompleteSuccess() {
        // Given
        String prompt = "Hello, how are you?";
        String expectedResponse = "I'm doing well, thank you!";

        ClaudeResponse mockResponse = new ClaudeResponse(
                "msg_123",
                "message",
                "assistant",
                List.of(new ClaudeResponse.Content("text", expectedResponse, null, null)),
                "claude-3-5-sonnet-20241022",
                "end_turn",
                null,
                new ClaudeResponse.Usage(10, 15, null, null)
        );

        // Mock WebClient chain
        when(claudeWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(ClaudeRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ClaudeResponse.class)).thenReturn(Mono.just(mockResponse));

        // When & Then
        StepVerifier.create(claudeService.complete(prompt))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void testCompleteWithSystemPrompt() {
        // Given
        String prompt = "Analyze this text";
        String systemPrompt = "You are a text analyzer";
        String expectedResponse = "This text appears to be a request for analysis.";

        ClaudeResponse mockResponse = new ClaudeResponse(
                "msg_124",
                "message",
                "assistant",
                List.of(new ClaudeResponse.Content("text", expectedResponse, null, null)),
                "claude-3-5-sonnet-20241022",
                "end_turn",
                null,
                new ClaudeResponse.Usage(15, 20, null, null)
        );

        // Mock WebClient chain
        when(claudeWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(ClaudeRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ClaudeResponse.class)).thenReturn(Mono.just(mockResponse));

        // When & Then
        StepVerifier.create(claudeService.complete(prompt, systemPrompt))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void testAnalyzeTextSentiment() {
        // Given
        String text = "I love this product!";
        String analysisType = "sentiment";
        String expectedResponse = "This text expresses positive sentiment with enthusiasm.";

        ClaudeResponse mockResponse = new ClaudeResponse(
                "msg_125",
                "message",
                "assistant",
                List.of(new ClaudeResponse.Content("text", expectedResponse, null, null)),
                "claude-3-5-sonnet-20241022",
                "end_turn",
                null,
                new ClaudeResponse.Usage(20, 25, null, null)
        );

        // Mock WebClient chain
        when(claudeWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(ClaudeRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ClaudeResponse.class)).thenReturn(Mono.just(mockResponse));

        // When & Then
        StepVerifier.create(claudeService.analyzeText(text, analysisType))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void testGenerateContentStory() {
        // Given
        String prompt = "Write a short story about a robot";
        String contentType = "story";
        Double creativity = 0.9;
        String expectedResponse = "Once upon a time, there was a curious robot named Zephyr...";

        ClaudeResponse mockResponse = new ClaudeResponse(
                "msg_126",
                "message",
                "assistant",
                List.of(new ClaudeResponse.Content("text", expectedResponse, null, null)),
                "claude-3-5-sonnet-20241022",
                "end_turn",
                null,
                new ClaudeResponse.Usage(25, 150, null, null)
        );

        // Mock WebClient chain
        when(claudeWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(ClaudeRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ClaudeResponse.class)).thenReturn(Mono.just(mockResponse));

        // When & Then
        StepVerifier.create(claudeService.generateContent(prompt, contentType, creativity))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void testHealthCheckSuccess() {
        // Given
        ClaudeResponse mockResponse = new ClaudeResponse(
                "msg_health",
                "message",
                "assistant",
                List.of(new ClaudeResponse.Content("text", "Hello!", null, null)),
                "claude-3-5-sonnet-20241022",
                "end_turn",
                null,
                new ClaudeResponse.Usage(5, 3, null, null)
        );

        // Mock WebClient chain
        when(claudeWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(ClaudeRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ClaudeResponse.class)).thenReturn(Mono.just(mockResponse));

        // When & Then
        StepVerifier.create(claudeService.healthCheck())
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testHealthCheckFailure() {
        // Given
        when(claudeWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(ClaudeRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ClaudeResponse.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        // When & Then
        StepVerifier.create(claudeService.healthCheck())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void testConversation() {
        // Given
        List<ClaudeRequest.Message> messages = List.of(
                new ClaudeRequest.Message("user", "Hello"),
                new ClaudeRequest.Message("assistant", "Hi there!"),
                new ClaudeRequest.Message("user", "How are you?")
        );
        String systemPrompt = "You are a helpful assistant";
        String expectedResponse = "I'm doing great, thank you for asking!";

        ClaudeResponse mockResponse = new ClaudeResponse(
                "msg_conv",
                "message",
                "assistant",
                List.of(new ClaudeResponse.Content("text", expectedResponse, null, null)),
                "claude-3-5-sonnet-20241022",
                "end_turn",
                null,
                new ClaudeResponse.Usage(30, 20, null, null)
        );

        // Mock WebClient chain
        when(claudeWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(ClaudeRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ClaudeResponse.class)).thenReturn(Mono.just(mockResponse));

        // When & Then
        StepVerifier.create(claudeService.conversation(messages, systemPrompt))
                .expectNext(expectedResponse)
                .verifyComplete();
    }
}
