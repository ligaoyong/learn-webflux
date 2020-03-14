package cn.gogpay.webflux.learnwebflux.controller;

import cn.gogpay.webflux.learnwebflux.entity.Empolyee;
import cn.gogpay.webflux.learnwebflux.exception.MyExceptionHandler;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 以注解的方式使用webflux：使用方式与spring mvc基本一致
 *
 * @author ligaoyong@gogpay.cn
 * @date 2020/3/13 9:43
 */
@RestController
@RequestMapping("/api")
public class EmpolyeeController {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    private Semaphore semaphore = new Semaphore(0);

    /**
     * 控制器方法参数支持大多数与 spring mvc一致，只需要将有servlet的地方换成server即可
     *
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/mono")
    public Mono<Empolyee> rest1(ServerHttpRequest request, ServerHttpResponse response) {
        String random = "-" + Math.random();
        System.out.println("处理请求的线程：" + Thread.currentThread().getName() + random);
        Empolyee empolyee = new Empolyee();
        empolyee.setId("111");
        empolyee.setName("lgy");
        //Mono类似于Future 是非阻塞式的
        //类似于Future的get阻，Mono也提供的阻塞获取结果的方法block

        //与Futrue不同的是，当Mono有数据的时候，会自动通知当前线程，让当前线程把数据写回；
        // 在Mono没有数据这段时间，当前线程可以继续处理其他请求读写,不用阻塞，从而提升线程的利用率

        /**
         * 由线程池提供数据，并将数据写回
         * 主线程不会阻塞等待数据(spring mvc中要阻塞等待数据)，会接收更多请求
         * 这里就体现了webflux的核心
         *      也即：耗时的业务交给另外的线程来做，这样就不会阻塞处理请求的主线程
         */

        /**
         * 使用这种方式 经过测试 同样得10个线程
         * 使用1个线程来接受请求，其余9个线程来处理耗时操作
         *  吞吐量能达到2000/分钟
         *  响应时间：200+ms
         */
        Mono<Empolyee> empolyeeMono = Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("提供数据的线程：" + Thread.currentThread().getName() + random);
            return empolyee;
        }, threadPoolExecutor));
        /**
         * 使用这种方式(模拟阻塞) 经过测试 同样得10个线程
         * 10个线程即处理请求，也进行耗时操作
         *  吞吐量只能达到855/分钟
         *  响应时间：500+ms
         */
//        Mono<Empolyee> empolyeeMono = Mono.fromSupplier(() -> {
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("提供数据的线程：" + Thread.currentThread().getName() + random);
//            return empolyee;
//        });

        /**
         * 由上述两种方式可知：
         *      在并发量大、操作耗时等场景下(例如网关)
         *      在配置正确得情况下：webflux非阻塞有着更强得性能
         */

        //阻塞直到empolyeeMono里面有数据
        // Empolyee block = empolyeeMono.block();
        System.out.println("继续处理的线程：" + Thread.currentThread().getName() + random);
        return empolyeeMono;
    }

    @GetMapping("all/mono")
    public Flux<Empolyee> rest2() {
        Empolyee empolyee = new Empolyee();
        empolyee.setId("111");
        empolyee.setName("lgy");

        Empolyee empolyee1 = new Empolyee();
        empolyee1.setId("222");
        empolyee1.setName("lgy");
        return Flux.just(empolyee, empolyee1);
    }

    @GetMapping("webclient/mono")
    public Mono<Empolyee> rest3() {
        WebClient client = WebClient.create("http://localhost:80");

        Mono<Empolyee> empolyeeMono = client.get().uri("/api/mono").retrieve().bodyToMono(Empolyee.class);

        return empolyeeMono;
    }

    /**
     * 异常处理器只能声明再controller中 不能声明再controllerAdvice
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public Empolyee allException(Exception e) {
        Empolyee empolyee = new Empolyee();
        empolyee.setId("exception");
        empolyee.setName("出现异常了：" + e.getMessage());
        return empolyee;
    }

    @GetMapping("exception")
    public Flux<Exception> rest4() {
        throw new RuntimeException("发生异常");
    }
}
