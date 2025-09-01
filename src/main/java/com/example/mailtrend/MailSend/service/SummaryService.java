package com.example.mailtrend.MailSend.service;

import lombok.RequiredArgsConstructor;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;   // ✅ Tomcat 말고 OkHttp
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SummaryService {
    private final OkHttpClient openAiHttp;     // @Bean from OpenAIConfig
    private final HttpUrl openAiResponsesUrl;  // @Bean from OpenAIConfig
    private final String openAiAuthHeader;     // @Bean from OpenAIConfig

    @Value("${openai.model}") String model;

    public String summarize(String original, int maxWords) throws IOException {
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
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response resp = openAiHttp.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                throw new IOException("OpenAI error: " + resp.code() + " " + (resp.body() != null ? resp.body().string() : ""));
            }
            String body = resp.body() != null ? resp.body().string() : "";
            return extractFirstOutputText(body);
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"");
    }

    private static String extractFirstOutputText(String body) {
        int i = body.indexOf("\"output_text\":[");
        if (i < 0) return "(요약 생성 실패)";
        int start = body.indexOf('"', i + 15) + 1;
        int end = body.indexOf('"', start);
        return body.substring(start, end).replace("\\n", "\n");
    }
}
