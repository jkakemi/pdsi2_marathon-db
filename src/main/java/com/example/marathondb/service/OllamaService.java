package com.example.marathondb.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OllamaService {

    private final WebClient ollamaClient;

    public String callOllama(String modelName, String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "prompt", prompt,
                "stream", false
        );

        try {
            return ollamaClient.post()
                    .uri("/api/generate")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(jsonNode -> jsonNode.get("response").asText())
                    .block();
        } catch (Exception e) {
            System.err.println("Erro ao chamar a API do Ollama: " + e.getMessage());
            throw new RuntimeException("Falha na comunicação com o Ollama.", e);
        }
    }
}