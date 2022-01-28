package com.lyl.springcloud.controller;

import com.lyl.springcloud.entity.Result;
import com.lyl.springcloud.service.PaymentHystrixService;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author luoyalong
 */
@Slf4j
@RestController
@DefaultProperties(defaultFallback = "global_TimeOut")
public class OrderHystrixController {

    @Resource
    private PaymentHystrixService paymentHystrixService;

    /**
     * 正常的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/consumer/payment/hystrix/ok/{id}")
    public Result hystrix_OK(@PathVariable("id") Integer id){
        return paymentHystrixService.hystrix_OK(id);
    }

    /**
     * 超时的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/consumer/payment/hystrix/timeout/{id}")
/*    @HystrixCommand(fallbackMethod = "hystrix_TimeoutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1500")
    })*/
    @HystrixCommand
    public Result hystrix_Timeout(@PathVariable("id") Integer id){
        return paymentHystrixService.hystrix_Timeout(id);
    }

    /**
     * 服务降级（兜底的方法）
     * @param id
     * @return
     */
    public Result hystrix_TimeoutHandler(Integer id){
        String str = "客户端: hystrix_Timeout  [" + Thread.currentThread().getName() + "]  参数：" + id + "  80 服务器繁忙或者接口异常 ";
        log.info(str);
        return Result.fail(str);
    }

    /**
     * 全局超时方法
     * @return
     */
    public Result global_TimeOut(){
        String str = "global fallback方法 80 服务器繁忙或者接口异常 ";
        log.info(str);
        return Result.fail(str);
    }
}
