package cn.gogpay.webflux.learnwebflux.controller;

import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 服务端推送（Server Send Event）：
 *  在客户端发起一次请求后会保持该连接，服务器端基于该连接持续向客户端发送数据，从HTML5开始加入。
 *  (利用了响应头transfer-encoding: chunked？？？)
 */
@RestController
@RequestMapping("/api")
public class SSEController {

    /**
     * 每隔一秒返回一次数据：
     *  实现真正的响应式(变化传递：从服务端到前端组成一个响应式管道，后端的变化会传递给前端)
     *  (利用了响应头transfer-encoding: chunked？？？)
     * @return
     */
    @GetMapping("/stream")
    public Flux<String> sendTimePerSec() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(l -> "<br>"+LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
