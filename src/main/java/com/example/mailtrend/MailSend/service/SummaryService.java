package com.example.mailtrend.MailSend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SummaryService {
    private final OkHttpClient openAiHttp;     // @Bean from OpenAIConfig
    private final HttpUrl openAiResponsesUrl;  // @Bean from OpenAIConfig
    private final String openAiAuthHeader;     // @Bean from OpenAIConfig

    @Value("${openai.model}") String model;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String summarize(String original, int maxWords) throws IOException {

        Objects.requireNonNull(openAiHttp, "openAiHttp is null");
        Objects.requireNonNull(openAiResponsesUrl, "openAiResponsesUrl is null");
        Objects.requireNonNull(openAiAuthHeader, "openAiAuthHeader is null");
        if (!openAiAuthHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Authorization header must start with 'Bearer '");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalStateException("openai.model is empty");
        }

        String json = """
        {
          "model": "%s",
          "input": [
            {
              "role": "system",
              "content": [
                {"type": "input_text", "text": "너는 뛰어난 한국어 요약가다. 핵심만 간결히, 불릿 3~5개로 정리해."}
              ]
            },
            {
              "role": "user",
              "content": [
                {"type": "input_text", "text": "아래 텍스트를 %d단어 이내로 요약해.\\n---\\n%s"}
              ]
            }
          ]
        }
        """.formatted(model, maxWords, escapeJson(original));

        Request req = new Request.Builder()
                .url(openAiResponsesUrl)
                .header("Authorization", openAiAuthHeader)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(json, JSON))
                .build();

        try (Response resp = openAiHttp.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String errBody = resp.body() != null ? resp.body().string() : "";
                throw new IOException("OpenAI error: " + resp.code() + " " + errBody);
            }
            String body = resp.body() != null ? resp.body().string() : "";
            return extractFirstOutputText(body);
        }
    }

    private static String escapeJson(String s) {
        return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"");
    }

    /** Responses API: output[].content[].(type=output_text).text 우선 추출 */
    private static String extractFirstOutputText(String body) {
        if (body == null || body.isBlank()) return "(요약 생성 실패)";
        try {
            JsonNode root = MAPPER.readTree(body);

            // 표준 경로
            for (JsonNode out : root.path("output")) {
                for (JsonNode c : out.path("content")) {
                    if ("output_text".equals(c.path("type").asText())) {
                        String t = c.path("text").asText("");
                        if (!t.isBlank()) return t.replace("\\n", "\n");
                    }
                }
            }

            // 최상위 output_text 문자열
            JsonNode ot = root.path("output_text");
            if (ot.isTextual() && !ot.asText().isBlank()) {
                return ot.asText().replace("\\n", "\n");
            }

            return "(요약 생성 실패)";
        } catch (Exception e) {
            return "(요약 생성 실패: parse error)";
        }
    }
}
