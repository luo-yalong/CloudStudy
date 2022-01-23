package com.lyl.springcloud.controller;

import com.lyl.springcloud.entity.vo.PaymentVo;
import com.lyl.springcloud.entity.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @author 罗亚龙
 * @date 2022/1/21 14:07
 */
@RestController
@RequestMapping("/consumer")
public class OrderController {

    //public static final String PAYMENT_URL = "http://localhost:8001";
    public static final String PAYMENT_URL = "http://CLOUD-PAYMENT-SERVICE";

    @Resource
    private RestTemplate restTemplate;

    /**
     * 创建一个支付数据
     * @param payment 请求参数
     * @return 执行结果
     */
    @PostMapping("/payment/create")
    public Result create(@RequestBody PaymentVo payment) {
        return restTemplate.postForObject(PAYMENT_URL + "/payment/create", payment,Result.class);
    }

    /**
     * 通过id查询支付数据
     * @param id id
     * @return 支付数据
     */
    @GetMapping("/payment/{id:\\d+}")
    public Result getById(@PathVariable("id") Long id){
        return restTemplate.getForObject(PAYMENT_URL + "/payment/" + id,Result.class);
    }


}
