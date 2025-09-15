package com.example.marathondb.service;

import com.example.marathondb.domain.*;
import com.example.marathondb.dto.ProblemSummaryDTO;
import com.example.marathondb.dto.StudyPlanResponseDTO;
import com.example.marathondb.repository.ProblemRepository;
import com.example.marathondb.repository.StudentRepository;
import com.example.marathondb.repository.StudyPlanRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final StudentRepository studentRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final ProblemRepository problemRepository;
    private final OllamaService ollamaService;

    private static final String MODEL_NAME = "gemma3:4b";

    @Transactional
    public StudyPlanResponseDTO generateStudyPlan(String userEmail) {
        Student student = studentRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        UserProfile profile = student.getUserProfile();

        String prompt = String.format(
                """
                Você é um tutor especialista em programação competitiva para estudantes universitários.
                Crie um roteiro de estudos inicial para um estudante com as seguintes características:
                - Período na faculdade: %d
                - Objetivo principal: %s
                - Estilo de aprendizado preferido: %s
                - Tempo disponível por semana: %s
                O roteiro deve ser conciso e em formato Markdown.
                """,
                profile.getCurrentPeriod(),
                profile.getLearningGoal(),
                profile.getLearningStyle(),
                profile.getTimeCommitment()
        );

        String aiResponse = ollamaService.callOllama(MODEL_NAME, prompt);

        StudyPlan newStudyPlan = new StudyPlan();
        newStudyPlan.setStudent(student);
        newStudyPlan.setTitle("Roteiro de Estudos de Programação Competitiva");
        newStudyPlan.setContent(aiResponse);
        newStudyPlan.setActive(true);

        StudyPlan savedPlan = studyPlanRepository.save(newStudyPlan);
        return mapToStudyPlanResponseDTO(savedPlan);
    }

    @Transactional
    public List<ProblemSummaryDTO> recommendProblems(String userEmail) {
        Student student = studentRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        List<Submission> submissions = student.getSubmissions();

        if (submissions.isEmpty()) {
            return List.of();
        }

        Set<String> weakTopicNames = submissions.stream()
                .filter(s -> !"OK".equals(s.getVerdict()))
                .flatMap(s -> s.getProblem().getTopics().stream())
                .map(Topic::getName)
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3).map(Map.Entry::getKey).collect(Collectors.toSet());

        if (weakTopicNames.isEmpty()) {
            weakTopicNames = submissions.stream()
                    .flatMap(s -> s.getProblem().getTopics().stream())
                    .map(Topic::getName)
                    .limit(5).collect(Collectors.toSet());
        }

        List<Problem> cfCandidates = problemRepository.findRandomUnsolvedProblemsBySource(student.getId(), "Codeforces", 25);
        List<Problem> uvaCandidates = problemRepository.findRandomUnsolvedProblemsBySource(student.getId(), "UVa Online Judge", 25);
        List<Problem> candidateProblems = new ArrayList<>(cfCandidates);
        candidateProblems.addAll(uvaCandidates);
        Collections.shuffle(candidateProblems);

        String problemCatalogForAI = candidateProblems.stream()
                .map(p -> String.format("{\"id\": %d, \"title\": \"%s\", \"rating\": %d, \"source\": \"%s\"}",
                        p.getId(), p.getTitle().replace("\"", "'"), p.getRating(), p.getSource()))
                .collect(Collectors.joining(",\n"));

        String prompt;
        if (weakTopicNames.isEmpty()) {
            prompt = String.format(
                    """
                    Você é um tutor de programação. Abaixo está um catálogo de problemas que um estudante não resolveu.
                    
                    CATÁLOGO:
                    [%s]
            
                    Sua tarefa é selecionar 8 problemas deste catálogo. Crie uma lista de prática interessante e com dificuldade variada.
                    Sua resposta DEVE SER APENAS um array JSON de objetos, onde cada objeto tem a chave "problemId".
                    Exemplo de resposta: [{"problemId": 15}, {"problemId": 2}]
                    """,
                    problemCatalogForAI
            );
        } else {
            prompt = String.format(
                    """
                    Você é um tutor de programação. Um estudante tem dificuldade nos tópicos: [%s].
                    Abaixo está um catálogo de problemas que ele não resolveu.
                    
                    CATÁLOGO:
                    [%s]
            
                    Sua tarefa é selecionar 8 problemas deste catálogo para ajudar o estudante a praticar seus pontos fracos.
                    Sua resposta DEVE SER APENAS um array JSON de objetos, onde cada objeto tem a chave "problemId".
                    Exemplo de resposta: [{"problemId": 15}, {"problemId": 2}]
                    """,
                    String.join(", ", weakTopicNames),
                    problemCatalogForAI
            );
        }

        try {
            String aiResponseJson = ollamaService.callOllama(MODEL_NAME, prompt);

            if (aiResponseJson.trim().startsWith("```json")) {
                aiResponseJson = aiResponseJson.substring(aiResponseJson.indexOf('['));
                aiResponseJson = aiResponseJson.substring(0, aiResponseJson.lastIndexOf(']') + 1);
            }
            aiResponseJson = aiResponseJson.replaceAll(",\\s*]", "]");

            ObjectMapper objectMapper = new ObjectMapper();
            List<RecommendationId> recommendedIds = objectMapper.readValue(aiResponseJson, new TypeReference<>() {});

            Set<Long> problemIds = recommendedIds.stream().map(RecommendationId::getProblemId).collect(Collectors.toSet());

            List<Problem> recommendedProblems = problemRepository.findAllById(problemIds);

            return recommendedProblems.stream()
                    .map(this::mapToProblemSummaryDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private StudyPlanResponseDTO mapToStudyPlanResponseDTO(StudyPlan plan) {
        StudyPlanResponseDTO dto = new StudyPlanResponseDTO();
        dto.setId(plan.getId());
        dto.setTitle(plan.getTitle());
        dto.setContent(plan.getContent());
        dto.setActive(plan.isActive());
        dto.setCreatedAt(plan.getCreatedAt());
        return dto;
    }

    private ProblemSummaryDTO mapToProblemSummaryDTO(Problem problem) {
        ProblemSummaryDTO dto = new ProblemSummaryDTO();
        dto.setTitle(problem.getTitle());
        dto.setSource(problem.getSource());
        dto.setProblemUrl(problem.getProblemUrl());
        dto.setRating(problem.getRating());
        if (problem.getTopics() != null) {
            dto.setTags(problem.getTopics().stream().map(Topic::getName).collect(Collectors.toSet()));
        }
        return dto;
    }

    public List<StudyPlanResponseDTO> getStudyPlansForUser(String userEmail) {
        Student student = studentRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        List<StudyPlan> plans = studyPlanRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());

        return plans.stream()
                .map(this::mapToStudyPlanResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteStudyPlan(Long planId, String userEmail) {
        Student student = studentRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plano de estudos não encontrado"));

        if (!plan.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("Você não tem permissão para excluir este plano.");
        }

        studyPlanRepository.deleteById(planId);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RecommendationId {
        @JsonProperty("problemId")
        private Long problemId;
    }
}