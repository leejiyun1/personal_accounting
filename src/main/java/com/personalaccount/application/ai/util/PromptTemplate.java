package com.personalaccount.application.ai.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Component
public class PromptTemplate {

    public String loadTemplate(String templatePath, Map<String, String> variables) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            String template = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            for (Map.Entry<String, String> entry : variables.entrySet()) {
                template = template.replace("{" + entry.getKey() + "}", entry.getValue());
            }

            return template;
        } catch (IOException e) {
            log.error("프롬프트 템플릿 로드 실패: {}", templatePath, e);
            throw new RuntimeException("프롬프트 템플릿을 로드할 수 없습니다", e);
        }
    }
}