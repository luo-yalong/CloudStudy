package com.lyl.springcloud.controller;

import com.lyl.springcloud.entity.Payment;
import com.lyl.springcloud.entity.Result;
import com.lyl.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 罗亚龙
 * @date 2022/1/21 11:15
 */
@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Resource
    private PaymentService paymentService;

    @Value("${server.port}")
    private Integer serverPort;

    @Resource
    private DiscoveryClient discoveryClient;

    @GetMapping("/discovery")
    public Object discovery(){
        List<String> services = discoveryClient.getServices();
        //获取所有的服务名称
        for (String service : services) {
            log.info("***** 服务名称: " + service);
        }

        List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
        log.info("服务名ID\t主机\t端口号\tURI");
        for (ServiceInstance instance : instances) {
            log.info(instance.getServiceId() + "\t" + instance.getHost() + "\t" + instance.getPort() + "\t" + instance.getUri());
        }
        return discoveryClient;
    }

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

    @GetMapping("/lb")
    public Result getServerPort(){
        return Result.success(serverPort);
    }

    /**
     * 测试feign的超时
     * @return 端口号
     */
    @GetMapping("/timeout")
    public Result timeOut(){
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.success(serverPort);
    }
}
