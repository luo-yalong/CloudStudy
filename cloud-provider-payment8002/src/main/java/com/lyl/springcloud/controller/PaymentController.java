package com.lyl.springcloud.controller;

import com.lyl.springcloud.entity.Payment;
import com.lyl.springcloud.entity.Result;
import com.lyl.springcloud.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 罗亚龙
 * @date 2022/1/21 11:15
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Resource
    private PaymentService paymentService;


    @Value("${server.port}")
    private Integer serverPort;

    /**
     * 创建一个支付数据
     * @param payment 请求参数
     * @return 执行结果
     */
    @PostMapping("/create")
    public Result create(@RequestBody Payment payment) {
        boolean save = paymentService.save(payment);
        return save ? Result.success("添加成功") : Result.fail("添加失败");
    }

    /**
     * 通过id查询支付数据
     * @param id id
     * @return 支付数据
     */
    @GetMapping("{id:\\d+}")
    public Result getById(@PathVariable("id") Long id){
        Payment payment = paymentService.getById(id);
        return Result.success("查询成功,serverPort: " + serverPort,payment);
    }
}
