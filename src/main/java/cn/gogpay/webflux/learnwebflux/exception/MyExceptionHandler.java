package cn.gogpay.webflux.learnwebflux.exception;

import cn.gogpay.webflux.learnwebflux.entity.Empolyee;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * webflux下不会生效 需要声明再controller中
 *
 * @author ligaoyong@gogpay.cn
 * @date 2020/3/13 11:46
 */
@ControllerAdvice
public class MyExceptionHandler{

    @ExceptionHandler(value = Exception.class)
    public Empolyee allException(Exception e){
        Empolyee empolyee = new Empolyee();
        empolyee.setId("exception");
        empolyee.setName("出现异常了：" + e.getMessage());
        return empolyee;
    }

}
