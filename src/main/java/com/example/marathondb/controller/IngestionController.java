package com.example.marathondb.controller;

import com.example.marathondb.dto.ProblemIngestionDTO;
import com.example.marathondb.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @Value("${ingestion.api.key}")
    private String secretApiKey;

    @PostMapping("/problems")
    public ResponseEntity<String> receiveScrapedProblems(
            @RequestBody List<ProblemIngestionDTO> problems,
            @RequestHeader("X-API-KEY") String apiKey) {

        if (!secretApiKey.equals(apiKey)) {
            return ResponseEntity.status(403).body("Acesso negado: Chave de API inválida.");
        }

        ingestionService.ingestProblems(problems);

        return ResponseEntity.ok("Dados recebidos e processados com sucesso: " + problems.size() + " problemas.");
    }

    @PostMapping("/enrich-problems")
    public ResponseEntity<String> enrichProblems(@RequestHeader("X-API-KEY") String apiKey) {
        if (!secretApiKey.equals(apiKey)) {
            return ResponseEntity.status(403).body("Acesso negado: Chave de API inválida.");
        }

        new Thread(() -> ingestionService.enrichUvaProblemsWithTags()).start();

        return ResponseEntity.ok("Processo de enriquecimento de dados iniciado. Acompanhe os logs do servidor.");
    }
}