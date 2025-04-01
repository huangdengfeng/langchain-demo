package com.seezoon.quickstart;

import com.seezoon.LangchainDemoApplicationTests;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.IngestionResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

public class Demo extends LangchainDemoApplicationTests {

    private static final String url = "http://9.134.74.46:11434";
    private static final String modeName = "deepseek-r1:1.5b";
    private static final String embeddingModeName = "nomic-embed-text";

    @Test
    public void responseWithAiService() {
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
    public void chatWithRole() {
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


    @Test
    public void mutilChat() {
        // 类似chatModel.chat 传多个message ,每个message 有自己的类型
        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(modeName)
                .logRequests(true)
                .build();
        UserMessage first = UserMessage.userMessage("Hello, my name is Klaus");
        AiMessage firstAiMessage = chatModel.chat(first).aiMessage();
        UserMessage second = UserMessage.userMessage("What is my name?");

        ChatResponse secondAiMessage = chatModel.chat(first, firstAiMessage, second);
        System.out.println(secondAiMessage.aiMessage().text());
    }

    @Test
    public void memory() {
        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(modeName)
                .logRequests(true)
                .build();
        // 自定义存储（示例为内存存储，可替换为 JDBC、Redis 等）
        ChatMemoryStore store = new InMemoryChatMemoryStore();

        // 创建 ChatMemory，关联到特定会话 ID
        String sessionId = "user-123";
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .id(sessionId)
                .chatMemoryStore(store)
                .build();
        chatMemory.add(UserMessage.userMessage("Hello, my name is Klaus"));
        AiMessage aiMessage = chatModel.chat(chatMemory.messages()).aiMessage();
        chatMemory.add(aiMessage);
        AiMessage aiMessage1 = chatModel.chat(chatMemory.messages()).aiMessage();
        chatMemory.add(aiMessage1);
    }

    @Test
    public void embedding() {
        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl(url).modelName(embeddingModeName).logRequests(true).logResponses(true).build();
        Response<Embedding> fsa = embeddingModel.embed("黄登峰");
        Embedding content = fsa.content();
        System.out.println(content);
    }

    /**
     * 选择支持tools的模型 llama3.3、qwen2.5:7b等，一般可以和通过promot 让模型把对应字段输出，解析后再做逻辑
     */
    @Test
    public void tool() {
        class Tools {

            @Tool("三个数之和")
            int add(int a, int b, int c) {
                return a + b + c;
            }

            @Tool("两个数相乘")
            int multiply(int a, int b) {
                return a * b;
            }
        }
        // ai service
        interface Assistant {

            String chat(String userMessage);
        }

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(url)
                .timeout(Duration.ofSeconds(10))
                .modelName("qwen2.5:7b")
                .logRequests(true)
                .logResponses(true)
                .build();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .tools(new Tools())
                .build();

        String answer = assistant.chat("What is 1+2+3 and 3*4?");
        System.out.println(answer);
    }

    @Test
    public void mcp() {
        // ai service
        interface Assistant {

            String chat(String userMessage);
        }

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(url)
                .timeout(Duration.ofSeconds(10))
                .modelName("qwen2.5:7b")
                .logRequests(true)
                .logResponses(true)
                .build();

        // 标准输入
        McpTransport transport = new StdioMcpTransport.Builder()
                .command(List.of("ls", "-a"))
                .logEvents(true)
                .build();

        // http 方式加载
        McpTransport httpTransport = new HttpMcpTransport.Builder().sseUrl("xxx").timeout(Duration.ofSeconds(10))
                .logRequests(true).logResponses(true).build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport).build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .toolProvider(McpToolProvider.builder().mcpClients(mcpClient).build())
                .build();

        String answer = assistant.chat("打开文件 /data");
        System.out.println(answer);
    }


    @Test
    public void rag() {
        // 自定义个txt 根据内容生成
        // FileSystemDocumentLoader 也可以
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("docs");
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        IngestionResult ingest = EmbeddingStoreIngestor.ingest(documents, embeddingStore);
        // ai service
        interface Assistant {

            String chat(String userMessage);
        }

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(url)
                .modelName(modeName)
                .logRequests(true)
                .logResponses(true)
                .build();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel).contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                .build();
        String msg = assistant.chat("正不知那石头上面记着何人何事？看官请听。输出后面的段落");
        System.out.println(msg);

    }

}
