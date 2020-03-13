package cn.gogpay.webflux.learnwebflux.handler;

import cn.gogpay.webflux.learnwebflux.entity.Empolyee;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * 处理器
 *
 * @author ligaoyong@gogpay.cn
 * @date 2020/3/13 10:11
 */
@Component
public class EmployeeHandler {

    public Mono<ServerResponse> hello1(ServerRequest request){
        Empolyee empolyee = new Empolyee();
        empolyee.setId("111222333");
        empolyee.setName("lgy");
        return ServerResponse.ok().body(BodyInserters.fromValue(empolyee));
    }

    public Mono<ServerResponse> hello2(ServerRequest request){
        Empolyee empolyee = new Empolyee();
        empolyee.setId("lalallala");
        empolyee.setName("我是hello2");
        return ServerResponse.ok().body(BodyInserters.fromValue(empolyee));
    }

    public Mono<ServerResponse> hello3(ServerRequest request){
        Empolyee empolyee = new Empolyee();
        empolyee.setId("fefssefjsljlk");
        empolyee.setName(request.queryParam("name").get());
        return ServerResponse.ok().body(BodyInserters.fromValue(empolyee));
    }

}
