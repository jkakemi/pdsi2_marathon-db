package com.example.marathondb.controller;

import com.example.marathondb.dto.ProblemIngestionDTO;
import com.example.marathondb.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.Transactional;
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

    @PostMapping("/enrich/{source}")
    public ResponseEntity<String> enrichProblemsBySource(
            @PathVariable String source,
            @RequestHeader("X-API-KEY") String apiKey) {

        if (!secretApiKey.equals(apiKey)) {
            return ResponseEntity.status(403).body("Acesso negado: Chave de API inválida.");
        }

        String sourceToProcess;
        if ("spoj".equalsIgnoreCase(source)) {
            sourceToProcess = "SPOJ";
        } else {
            return ResponseEntity.badRequest().body("Fonte desconhecida. Atualmente, apenas 'spoj' é suportado.");
        }

        ingestionService.enrichProblemsBySource(sourceToProcess);

        return ResponseEntity.ok("Processo de enriquecimento para '" + sourceToProcess + "' iniciado.");
    }
}