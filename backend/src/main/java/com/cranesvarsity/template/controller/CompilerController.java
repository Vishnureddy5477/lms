package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.model.CodeRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/compiler")
public class CompilerController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/run")
    public ResponseEntity<String> runCode(@RequestBody CodeRequest request) {
        String url = "https://emkc.org/api/v2/piston/execute";

        Map<String, Object> body = new HashMap<>();
        body.put("language", request.getLanguage().toLowerCase());
        body.put("version", "*");
        
        Map<String, String> file = new HashMap<>();
        file.put("content", request.getCode());
        body.put("files", new Map[]{file});
        body.put("stdin", request.getInput() != null ? request.getInput() : "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}