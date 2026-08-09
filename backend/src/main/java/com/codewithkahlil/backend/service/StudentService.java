package com.codewithkahlil.backend.service;

import com.codewithkahlil.backend.dto.response.AiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final RestTemplate restTemplate ;

    public String askAi(String question) {

        String url =
                "http://127.0.0.1:8000/ai/ask?question=" + question;

        AiResponse response =
                restTemplate.getForObject(url, AiResponse.class);

        return response.getAnswer();
    }
}
