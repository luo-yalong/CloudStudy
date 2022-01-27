package com.lyl.springcloud.controller;

import com.lyl.springcloud.entity.Result;
import com.lyl.springcloud.service.PaymentHystrixService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
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
    public Result hystrix_Timeout(@PathVariable("id") Integer id){
        return paymentHystrixService.hystrix_Timeout(id);
    }
}
