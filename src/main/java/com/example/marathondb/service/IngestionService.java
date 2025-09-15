package com.example.marathondb.service;

import com.example.marathondb.domain.Problem;
import com.example.marathondb.domain.Topic;
import com.example.marathondb.dto.ProblemIngestionDTO;
import com.example.marathondb.repository.ProblemRepository;
import com.example.marathondb.repository.TopicRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final ProblemRepository problemRepository;
    private final TopicRepository topicRepository;
    private final OllamaService ollamaService;

    @Transactional
    public void ingestProblems(List<ProblemIngestionDTO> problemDtos) {
        for (ProblemIngestionDTO dto : problemDtos) {
            String problemUrl = dto.getProblemPageUrl();

            if (problemUrl == null || problemUrl.trim().isEmpty() || problemUrl.contains("não encontrada")) {
                System.err.println("URL inválida ou não encontrada para o problema ID: " + dto.getProblemId() + ". A pular.");
                continue;
            }

            if (!problemRepository.existsByProblemUrl(problemUrl)) {
                Problem newProblem = new Problem();
                newProblem.setTitle(dto.getName());
                newProblem.setDifficulty(dto.getDifficulty());
                newProblem.setSource("SPOJ");
                newProblem.setProblemUrl(problemUrl);

                problemRepository.save(newProblem);
            }
        }
    }

    @Transactional
    public void enrichProblemsBySource(String source) {
        List<Problem> untaggedProblems = problemRepository.findProblemsBySourceAndWithoutTopics(source);

        final int BATCH_SIZE = 10;
        for (int i = 0; i < untaggedProblems.size(); i += BATCH_SIZE) {
            List<Problem> batch = untaggedProblems.subList(i, Math.min(i + BATCH_SIZE, untaggedProblems.size()));

            processProblemBatch(batch);

            try {
                if (i + BATCH_SIZE < untaggedProblems.size()) {
                    System.out.println("5 segundos para evitar limite de taxa.");
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Pausa interrompida.");
            }
        }
    }

    private void processProblemBatch(List<Problem> batch) {
        String problemCatalogForBatch = batch.stream()
                .map(p -> String.format("- ID: %d, Título: \"%s\"", p.getId(), p.getTitle()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format(
                """
                Você é um classificador de problemas de programação competitiva. 
                Abaixo está um catálogo de problemas. Para cada problema, gere uma lista de até 3 tags/tópicos relevantes.
                Sua resposta DEVE ser APENAS um objeto JSON. As chaves do JSON devem ser os IDs dos problemas, e os valores devem ser uma lista de strings com as tags.
                
                CATÁLOGO:
                %s
    
                Exemplo de resposta JSON:
                {
                  "214": ["math", "number theory"],
                  "397": ["graphs", "bfs"]
                }
                """,
                problemCatalogForBatch
        );

        String aiResponseJson = null;
        try {
            aiResponseJson = ollamaService.callOllama("gemma3:4b", prompt);

            if (aiResponseJson.trim().startsWith("```json")) {
                aiResponseJson = aiResponseJson.substring(7, aiResponseJson.length() - 3).trim();
            }

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, List<String>> tagsMap = objectMapper.readValue(aiResponseJson, new TypeReference<>() {});

            for (Problem problem : batch) {
                List<String> tags = tagsMap.get(String.valueOf(problem.getId()));
                if (tags != null && !tags.isEmpty()) {
                    System.out.println("Associando tags " + tags + " ao problema ID " + problem.getId());
                    Set<Topic> topicsForProblem = new HashSet<>();
                    for (String tagName : tags) {
                        Topic topic = topicRepository.findByName(tagName)
                                .orElseGet(() -> topicRepository.save(new Topic(null, tagName, null)));
                        topicsForProblem.add(topic);
                    }
                    problem.setTopics(topicsForProblem);
                }
            }

            problemRepository.saveAll(batch);
            System.out.println("Lote processado e salvo com sucesso.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}