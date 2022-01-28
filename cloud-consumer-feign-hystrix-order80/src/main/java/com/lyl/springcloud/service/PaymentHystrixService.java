package com.lyl.springcloud.service;

import com.lyl.springcloud.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author luoyalong
 */
@Service
@FeignClient(value = "CLOUD-PAYMENT-HYSTRIX-SERVICE", fallback = PaymentHystrixServiceImpl.class)
public interface PaymentHystrixService {

    /**
     * 正常的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/payment/hystrix/ok/{id}")
    public Result hystrix_OK(@PathVariable("id") Integer id);

    /**
     * 超时的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/payment/hystrix/timeout/{id}")
    public Result hystrix_Timeout(@PathVariable("id") Integer id);

}
