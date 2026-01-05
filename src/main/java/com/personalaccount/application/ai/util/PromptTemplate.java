package com.personalaccount.application.ai.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PromptTemplate {

    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    public String loadTemplate(String templatePath, Map<String, String> variables) {
        // 캐시에서 템플릿 로드 (없으면 파일에서 읽어서 캐싱)
        String template = templateCache.computeIfAbsent(templatePath, path -> {
            try {
                ClassPathResource resource = new ClassPathResource(path);
                log.info("프롬프트 템플릿 캐싱 완료: {}", path);
                return new String(
                        resource.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8
                );
            } catch (IOException e) {
                log.error("프롬프트 템플릿 로드 실패: {}", path, e);
                throw new RuntimeException("프롬프트 템플릿을 로드할 수 없습니다", e);
            }
        });

        // 변수 치환
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            template = template.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return template;
    }
}