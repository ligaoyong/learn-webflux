package cn.gogpay.webflux.learnwebflux.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 以router和handler的方式使用webflux
 *
 * @author ligaoyong@gogpay.cn
 * @date 2020/3/13 10:09
 */
@Configuration
public class EmployeeRouter {

    @Bean
    public RouterFunction<ServerResponse> route(EmployeeHandler handler){
        //编写路由规则 路由到指定的handler
        //可以将多个路由连接起来
        return RouterFunctions.route(RequestPredicates.GET("/hello1"), handler::hello1)
                .andRoute(RequestPredicates.GET("/hello2"), handler::hello2)
                .andRoute(RequestPredicates.GET("/hello3"), handler::hello3);
    }



}
