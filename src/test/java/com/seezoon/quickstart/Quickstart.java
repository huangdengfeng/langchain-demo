package com.seezoon.quickstart;

import com.seezoon.LangchainDemoApplicationTests;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.junit.jupiter.api.Test;

public class Quickstart extends LangchainDemoApplicationTests {

    private static final String url = "http://9.134.74.46:11434";
    private static final String modeName = "deepseek-r1:1.5b";


    @Test
    public void helloworld() {
        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(modeName)
                .logRequests(true)
                .logResponses(true)
                .build();
        String answer = chatModel.chat("hello world");
        System.out.println(answer);
    }
}
