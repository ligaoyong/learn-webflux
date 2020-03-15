package cn.gogpay.webflux.learnwebflux.reactorapi;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FluxAndMono {

    /**
     * Flux、Mono：代表发布者,实现了Publisher接口
     * Reactor中的发布者（Publisher）由Flux和Mono两个类定义
     * 既然是“数据流”的发布者，Flux和Mono都可以发出三种“数据信号”：元素值、错误信号、完成信号
     * 错误信号和完成信号都是终止信号:
     * 完成信号用于告知下游订阅者该数据流正常结束
     * 错误信号终止数据流的同时将错误传递给下游订阅者
     */
    @Test
    public void test1() {
        //发部数据流
        Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5);
        Integer[] array = {1, 2, 3, 4, 5};
        Flux<Integer> flux1 = Flux.fromArray(array);
        Flux<Integer> flux2 = Flux.fromIterable(Lists.list(array));
        Flux<Integer> flux3 = Flux.fromStream(Lists.list(array).stream());

        //发部空数据流
        Flux<Object> empty = Flux.empty();
        Mono<Object> empty1 = Mono.empty();
        Flux<Object> just = Flux.just();
        //发布错误信号
        Flux<Object> error = Flux.error(new Exception());
        Mono<Object> error1 = Mono.error(new Exception());

        //订阅前什么都不会发生，类似于Stream
        flux.subscribe(System.out::println);

        Mono.just(1).subscribe(
                System.out::println,    //数据流的处理
                System.err::println,    //错误信号的处理
                () -> System.out.println("Completed!") //完成信号的处理 会打印
        );

        Mono.error(new Exception("some error")).subscribe(
                System.out::println,    //数据流的处理
                System.err::println,    //错误信号的处理 会打印
                () -> System.out.println("Completed!") //完成信号的处理
        );
    }

    /**
     * 测试Flux和Mono的zip操作
     * 它对两个Flux/Mono流每次各取一个元素，合并为一个二元组（Tuple2）
     */
    @Test
    public void zip() {
        Flux<Integer> publisher1 = Flux.just(1, 3, 5, 7, 9);
        Flux<Integer> publisher2 = Flux.just(2, 4, 6, 8, 10);

        Flux<Tuple2<Integer, Integer>> zip = Flux.zip(publisher1, publisher2);
        //当前线程订阅
        zip.subscribe(tuple2 -> {
            System.out.println(tuple2.getT1() + " : " + tuple2.getT2());
        });
    }

    /**
     * 调度器Schedulers
     * 可以使用单独的线程来当Publisher
     * 也可以使用单独的线程来当Subscriber
     * 事实上这才是异步非阻塞的正确使用方式
     * <p>
     * 当前线程（Schedulers.immediate()）
     * 可重用的单线程（Schedulers.single()）
     * 弹性线程池（Schedulers.elastic()）
     * 固定大小线程池（Schedulers.parallel()），所创建线程池的大小与CPU个数等同
     * 自定义线程池（Schedulers.fromExecutorService(ExecutorService)）基于自定义的ExecutorService创建 Scheduler（虽然不太建议，不过你也可以使用Executor来创建）
     */
    @Test
    public void test3() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        System.out.println("主线程：" + Thread.currentThread().getName());
        Flux.just(1, 2, 3, 4, 5)
                .subscribeOn(Schedulers.elastic()) //使用指定的调度器去订阅处理数据流
                .subscribe(x -> {
                    System.out.println("订阅的线程：" + Thread.currentThread().getName());
                    System.out.println(x);
                }, null, () -> {
                    System.out.println("接收完成信号的线程：" + Thread.currentThread().getName()); //也是elastic调度器
                    countDownLatch.countDown();
                });
        countDownLatch.await();
    }

    /**
     * 测试调度器的publishOn、subscribeOn
     *  ：切换后续操作所使用到的线程
     */
    @Test
    public void test4() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        System.out.println("主线程：" + Thread.currentThread().getName());
        Flux.just(1, 2, 3, 4, 5) //在主线程中执行
                .publishOn(Schedulers.elastic()) //切换接下来发布操作执行的线程
                .map(x -> {
                    System.out.println("执行map的线程：" + Thread.currentThread().getName()); //在elastic线程中执行
                    return x + 1;
                }).subscribeOn(Schedulers.parallel()) //切换接下来执行订阅操作的线程
                .subscribe(x -> {
                    System.out.println("执行订阅的线程：" + Thread.currentThread().getName()); //在parallel中执行
                },null,() -> {
                    System.out.println("接收完成信号的线程：" + Thread.currentThread().getName()); //在parallel中执行
                    countDownLatch.countDown();
                });
        countDownLatch.await();
    }

    /**
     * 错误处理
     */
    @Test
    public void test5(){
        Flux.range(1, 6)
                .map(i -> 10/(i-3)) // 遇到错误，终止后续的数据流，这里只会处理i=1，i=2
                .map(i -> i*i)
                .subscribe(System.out::println, System.err::println);   //25 100

        //1. 捕获并返回一个静态的缺省值
        Flux.range(1, 6)
                .map(i -> 10/(i-3))
                .onErrorReturn(0) //遇到错误，使用0代替，终止后续流处理
                .map(i -> i*i)
                .subscribe(System.out::println, System.err::println); //25 100 0

        //2. 捕获并执行一个异常处理方法或计算一个候补值来顶替
        Flux.range(1, 6)
                .map(i -> 10/(i-3))
                .onErrorResume(throwable -> Flux.just(2)) //遇到错误，计算一个值代替，终止后续流处理
                .map(i -> i*i)
                .subscribe(System.out::println, System.err::println); //25 100 4

        //3. 捕获，并再包装为某一个业务相关的异常，然后再抛出业务异常
        Flux.range(1, 6)
                .map(i -> 10/(i-3))
                .onErrorMap(throwable -> new Exception("业务错误"))
                .map(i -> i*i)
                .subscribe(System.out::println, System.err::println); //25 100 业务错误
        System.out.println("-------------------------------------");
        //4. 捕获，记录错误日志，然后继续抛出
        Flux.range(1, 6)
                .map(i -> 10/(i-3))
                .doOnError(throwable -> System.out.println("记录错误")) //记录一下 然后继续往后传递异常
                .onErrorMap(throwable -> new Exception("业务错误"))
                .map(i -> i*i)
                .subscribe(System.out::println, System.err::println); //25 100 记录错误 业务错误
        System.out.println("-------------------------------------");
        //5、重试
        Flux.range(1, 6)
                .map(i -> 10/(i-3))
                .retry(1) //遇到错误，从第二个元素开始重新订阅
                .map(i -> i*i)
                .subscribe(System.out::println, System.err::println); //25 100 25 100 java.lang.ArithmeticException: / by zero
    }

    /**
     * 背压：
     *  下游订阅者通知上游发布者控制发布的速率
     *  订阅者决定发布者发布元素的速率
     */
    @Test
    public void test6(){
        Flux.range(1, 6)    // 1、Flux.range是一个快的Publisher
                .doOnRequest(n -> System.out.println("Request " + n + " values..."))    // 2、在每次request的时候打印request个数
                .subscribe(new BaseSubscriber<Integer>() {  // 3 通过重写BaseSubscriber的方法来自定义Subscriber
                    //订阅时执行：只会执行一次
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) { // 4、hookOnSubscribe定义在订阅的时候执行的操作：只会执行一次
                        System.out.println("Subscribed and make a request...");
                        request(1); // 5 订阅后首先向上游请求1个元素
                    }
                    //每收到元素时执行
                    @Override
                    protected void hookOnNext(Integer value) {  // 6 hookOnNext定义每次在收到一个元素的时候的操作
                        try {
                            TimeUnit.SECONDS.sleep(1);  // 7 sleep 1秒钟来模拟慢的Subscriber
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Get value [" + value + "]");    // 8 处理逻辑
                        request(1); // 9 每次处理完1个元素后再请求1个
                    }
                });
    }
}
