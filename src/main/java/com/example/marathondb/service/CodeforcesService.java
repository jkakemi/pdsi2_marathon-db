package com.example.marathondb.service;

import com.example.marathondb.domain.Problem;
import com.example.marathondb.domain.Student;
import com.example.marathondb.domain.Submission;
import com.example.marathondb.dto.CodeforcesApiResponseDTO;
import com.example.marathondb.dto.CodeforcesSubmissionDTO;
import com.example.marathondb.repository.ProblemRepository;
import com.example.marathondb.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
public class CodeforcesService {

    private final RestTemplate restTemplate;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

    private final String CODEFORCES_API_URL = "https://codeforces.com/api/user.status?handle=";

    @Transactional
    public void syncSubmissionsForStudent(Student student, String codeforcesHandle) {
        String apiUrl = CODEFORCES_API_URL + codeforcesHandle;

        CodeforcesApiResponseDTO response = restTemplate.getForObject(apiUrl, CodeforcesApiResponseDTO.class);

        if (response != null && "OK".equals(response.getStatus())) {
            for (CodeforcesSubmissionDTO subDto : response.getResult()) {
                if (subDto.getVerdict() == null) continue;

                if (!submissionRepository.existsBySourceSubmissionId(subDto.getId())) {

                    Problem problem = problemRepository
                            .findByContestIdAndProblemIndex(subDto.getProblem().getContestId(), subDto.getProblem().getIndex())
                            .orElseGet(() -> {
                                Problem newProblem = new Problem();
                                newProblem.setContestId(subDto.getProblem().getContestId());
                                newProblem.setProblemIndex(subDto.getProblem().getIndex());
                                newProblem.setTitle(subDto.getProblem().getName());

                                String url = String.format("https://codeforces.com/problemset/problem/%d/%s",
                                        subDto.getProblem().getContestId(), subDto.getProblem().getIndex());
                                newProblem.setProblemUrl(url);

                                newProblem.setSource("Codeforces");
                                return problemRepository.save(newProblem);
                            });

                    Submission newSubmission = new Submission();
                    newSubmission.setSourceSubmissionId(subDto.getId());
                    newSubmission.setVerdict(subDto.getVerdict());
                    newSubmission.setLanguage(subDto.getProgrammingLanguage());
                    newSubmission.setSubmissionTime(
                            LocalDateTime.ofInstant(Instant.ofEpochSecond(subDto.getCreationTimeSeconds()), TimeZone.getDefault().toZoneId())
                    );
                    newSubmission.setStudent(student);
                    newSubmission.setProblem(problem);

                    submissionRepository.save(newSubmission);
                }
            }
        }
    }
}