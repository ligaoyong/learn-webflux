package cn.gogpay.webflux.learnwebflux.HttpHandlerServer;

import org.reactivestreams.Publisher;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 *
 * 使用底层模型httpHandler来处理http请求，并且运行再netty中
 *  手动启动服务器
 *  (webHandler是高层模型)
 * @author ligaoyong@gogpay.cn
 * @date 2020/3/13 11:03
 */
public class HttpHandlerNetty {

    /**
     * 手动启动netty去处理http服务
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        HttpHandler httpHandler = (serverHttpRequest, serverHttpResponse) -> {
            System.out.println("netty服务器接收到请求："+serverHttpRequest.getPath().value());
            Publisher publisher = (subscriber) -> {
            };
            serverHttpResponse.writeWith(publisher);
            return Mono.empty();
        };
        ReactorHttpHandlerAdapter httpHandlerAdapter = new ReactorHttpHandlerAdapter(httpHandler);

        //启动netty 所有的http请求都会交给httpHandler来处理
        HttpServer httpServer = HttpServer.create().host("0.0.0.0").port(8080).handle(httpHandlerAdapter);
        DisposableServer disposableServer = httpServer.bindNow();
        Thread.currentThread().join();
    }

}
