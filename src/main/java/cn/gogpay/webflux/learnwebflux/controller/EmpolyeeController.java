package cn.gogpay.webflux.learnwebflux.controller;

import cn.gogpay.webflux.learnwebflux.entity.Empolyee;
import cn.gogpay.webflux.learnwebflux.exception.MyExceptionHandler;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 以注解的方式使用webflux：使用方式与spring mvc基本一致
 *
 * @author ligaoyong@gogpay.cn
 * @date 2020/3/13 9:43
 */
@RestController
@RequestMapping("/api")
public class EmpolyeeController {

    /**
     * 控制器方法参数支持大多数与 spring mvc一致，只需要将有servlet的地方换成server即可
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/mono")
    public Mono<Empolyee> rest1(ServerHttpRequest request, ServerHttpResponse response){
        System.out.println("处理请求的线程："+Thread.currentThread().getName());
        Empolyee empolyee = new Empolyee();
        empolyee.setId("111");
        empolyee.setName("lgy");
        //Mono类似于Future 是非阻塞式的
        //类似于Future的get阻，Mono也提供的阻塞获取结果的方法block

        //与Futrue不同的是，当Mono有数据的时候，会自动通知当前线程，让当前线程把数据写回；
        // 在Mono没有数据这段时间，当前线程可以继续处理其他请求,不用阻塞，从而提升线程的利用率
        Mono<Empolyee> empolyeeMono = Mono.fromSupplier(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("提供数据的线程：" + Thread.currentThread().getName());
            return empolyee;
        });
        //阻塞知道empolyeeMono里面有数据
        // Empolyee block = empolyeeMono.block();
        System.out.println("继续处理的线程："+Thread.currentThread().getName());
        return empolyeeMono;
    }

    @GetMapping("all/mono")
    public Flux<Empolyee> rest2(){
        Empolyee empolyee = new Empolyee();
        empolyee.setId("111");
        empolyee.setName("lgy");

        Empolyee empolyee1 = new Empolyee();
        empolyee1.setId("222");
        empolyee1.setName("lgy");
        return Flux.just(empolyee,empolyee1);
    }

    @GetMapping("webclient/mono")
    public Mono<Empolyee> rest3(){
        WebClient client = WebClient.create("http://localhost:80");

        Mono<Empolyee> empolyeeMono = client.get().uri("/api/mono").retrieve().bodyToMono(Empolyee.class);

        return empolyeeMono;
    }

    /**
     * 异常处理器只能声明再controller中 不能声明再controllerAdvice
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public Empolyee allException(Exception e){
        Empolyee empolyee = new Empolyee();
        empolyee.setId("exception");
        empolyee.setName("出现异常了：" + e.getMessage());
        return empolyee;
    }

    @GetMapping("exception")
    public Flux<Exception> rest4(){
        throw new RuntimeException("发生异常");
    }
}
