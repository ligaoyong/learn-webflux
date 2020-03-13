package cn.gogpay.webflux.learnwebflux.config;

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import reactor.netty.resources.LoopResources;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author ligaoyong@gogpay.cn
 * @date 2020/3/13 14:39
 */
@Configuration
public class NettyServerConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory();
        threadFactory.setThreadNamePrefix("business-pool");
        return new ThreadPoolExecutor(1,1,
                0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(10),
                threadFactory, new ThreadPoolExecutor.DiscardPolicy());
    }

    @Bean
    public ReactiveWebServerFactory reactiveWebServerFactory(){
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
        ReactorResourceFactory reactorResourceFactory = new ReactorResourceFactory();
        //设置业务线程组 只设置一个线程用于测试
        reactorResourceFactory.setLoopResources(LoopResources.create("my-thread-poll-",1,true));
        factory.setResourceFactory(reactorResourceFactory);
        return factory;
    }

}
