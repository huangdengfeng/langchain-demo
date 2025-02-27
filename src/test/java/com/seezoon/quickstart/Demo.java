package com.seezoon.quickstart;

import com.seezoon.LangchainDemoApplicationTests;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;

public class Demo extends LangchainDemoApplicationTests {
    private static final String url = "http://9.134.74.46:11434";
    private static final String modeName = "deepseek-r1:1.5b";

    @Test
    public void chatWithRole() {
        // ai service
        interface Assistant {
            String chat(String userMessage);
        }

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(modeName)
                .logRequests(true)
                .build();
        Assistant assistant = AiServices.create(Assistant.class, chatModel);
        String answer = assistant.chat("what is your name?");
        System.out.println(answer);
    }

    @Test
    public void responseWithAiService() {
        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(modeName)
                .logRequests(true)
                .build();
        ChatResponse answer = chatModel.chat(SystemMessage.systemMessage("你是数学老师，可以讲解数学题"),
                UserMessage.userMessage("8 + 20 等于多少，为什么"));
        System.out.println(answer.aiMessage().text());
    }

    @Test
    public void responseWithCustomJson() {
        // json schema
        record Person(String name, int age) {
        }
        // ai service
        interface PersonExtractor {
            Person extractPersonFrom(String text);
        }


        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(modeName)
                .logRequests(true)
                .build();
        PersonExtractor assistant = AiServices.create(PersonExtractor.class, chatModel);
        Person person = assistant.extractPersonFrom("黄登峰永远18岁");
        System.out.println(person);
    }


}
