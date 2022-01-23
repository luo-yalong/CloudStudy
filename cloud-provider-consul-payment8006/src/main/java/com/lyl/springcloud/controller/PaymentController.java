package com.lyl.springcloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    @Value("${server.port}")
    private Integer serverPort;

    /**
     * 获取注册进zookeeper服务的端口号
     * @return
     */
    @GetMapping("/consul")
    public String paymentZk(){
        return "Spring Cloud with Zookeeper: " + serverPort + "\t" + UUID.randomUUID().toString();
    }
}
