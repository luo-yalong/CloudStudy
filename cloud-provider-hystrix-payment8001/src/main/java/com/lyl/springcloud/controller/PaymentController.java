package com.lyl.springcloud.controller;

import com.lyl.springcloud.entity.Result;
import com.lyl.springcloud.service.HystrixService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author 罗亚龙
 * @date 2022/1/27 15:03
 */
@RestController
public class PaymentController {

    @Resource
    private HystrixService hystrixService;

    /**
     * 正常的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/payment/hystrix/ok/{id}")
    public Result hystrix_OK(@PathVariable("id") Integer id){
        return hystrixService.hystrix_OK(id);
    }

    /**
     * 超时的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/payment/hystrix/timeout/{id}")
    public Result hystrix_Timeout(@PathVariable("id") Integer id){
        return hystrixService.hystrix_Timeout(id);
    }


}
