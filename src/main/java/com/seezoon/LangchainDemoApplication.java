package com.seezoon;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@SpringBootApplication
public class LangchainDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LangchainDemoApplication.class, args);
    }

    /**
     * 客户端订阅SSE
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        // 设置超时时间（根据业务需求调整）
        SseEmitter emitter = new SseEmitter(60_000L); // 60秒超时

        CompletableFuture.runAsync(() -> {
            try {
                String[] chunks = {"思考中...", "答案：", "SSE", " 是实时推送的理想协议。"};
                for (String chunk : chunks) {
                    emitter.send(
                            SseEmitter.event()
                                    .data(chunk) // 发送数据块
                                    .id(UUID.randomUUID().toString()) // 可选：事件ID
                    );
                    Thread.sleep(1000); // 模拟处理延迟
                }
                emitter.complete(); // 标记完成
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        // 连接关闭时清理资源
        emitter.onCompletion(() -> {
            System.out.println("ok");
        });
        emitter.onError(e -> {
            System.err.println("SSE连接异常: " + e.getMessage());
        });
        return emitter;
    }


}
