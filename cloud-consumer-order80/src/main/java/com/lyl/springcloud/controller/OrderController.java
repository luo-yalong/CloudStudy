package com.lyl.springcloud.controller;

import com.lyl.springcloud.entity.vo.PaymentVo;
import com.lyl.springcloud.entity.Result;
import com.lyl.springcloud.lb.LoadBalance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;
import java.util.List;

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
    private LoadBalance lb;
    @Resource
    private DiscoveryClient discoveryClient;

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

    /**
     * 使用 getForEntity
     * @param id
     * @return
     */
    @GetMapping("/payment/getForEntity/{id:\\d+}")
    public Result getForEntityById(@PathVariable("id") Long id){
        ResponseEntity<Result> responseEntity = restTemplate.getForEntity(PAYMENT_URL + "/payment/" + id, Result.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()){
            return responseEntity.getBody();
        }else {
            return Result.fail(444,"操作失败");
        }
    }

    @GetMapping("/payment/lb")
    public Result getServerPort(){
        List<ServiceInstance> instanceList = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
        ServiceInstance instance = lb.instances(instanceList);
        URI uri = instance.getUri();
        return restTemplate.getForObject(uri + "/payment/lb",Result.class);
    }

}
