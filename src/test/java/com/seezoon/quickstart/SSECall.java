package com.seezoon.quickstart;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.client.RestClient;

@SpringBootTest
public class SSECall {

    private static final String url = "http://9.134.74.46:11434/api/generate";
    private static final String modeName = "deepseek-r1:1.5b";

    private RestClient restClient = RestClient.builder().messageConverters((list) -> {
        list.add(new NdJsonHttpMessageConverter());
    }).build();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void call() throws IOException {

        Map<String, String> params = new HashMap<>();
        params.put("model", modeName);
        params.put("prompt", "你是谁");

        InputStream inputStream = restClient.post().uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                //    .accept(MediaType.APPLICATION_NDJSON)
                .body(objectMapper.writeValueAsBytes(params)).exchange((req, resp) ->
                        resp.getBody(), false);

        // 读取 SSE 数据流
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("Received SSE event: " + line);
            // 根据 SSE 协议解析数据
            if (line.startsWith("data:")) {
                String eventData = line.substring(5).trim(); // 去掉 "data:" 前缀
                System.out.println("Event data: " + eventData);
            }
        }
    }


    public class NdJsonHttpMessageConverter extends AbstractHttpMessageConverter<InputStream> {


        public NdJsonHttpMessageConverter() {
            super(new MediaType("application", "x-ndjson"));
        }

        @Override
        protected boolean supports(Class<?> clazz) {
            return InputStream.class.isAssignableFrom(clazz);
        }

        @Override
        protected InputStream readInternal(Class<? extends InputStream> clazz, HttpInputMessage inputMessage)
                throws IOException {
            return inputMessage.getBody();
        }

        @Override
        protected void writeInternal(InputStream inputStream, HttpOutputMessage outputMessage)
                throws IOException, HttpMessageNotWritableException {
        }

    }
}
