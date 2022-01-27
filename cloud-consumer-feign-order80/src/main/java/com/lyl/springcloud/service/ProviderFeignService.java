package com.lyl.springcloud.service;

import com.lyl.springcloud.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 通过 注解 @FeignClient(服务名)
 *      和 提供者的服务接口，可以直接调用提供者提供的服务。
 */

@Component
@FeignClient(value = "CLOUD-PAYMENT-SERVICE")
public interface ProviderFeignService {

    /**
     * 通过id查询支付数据
     * @param id id
     * @return 支付数据
     */
    @GetMapping("/payment/{id:\\d+}")
    public Result getById(@PathVariable(value = "id")  Long id);

    /**
     * 测试服务端的负载均衡
     * @return 端口号
     */
    @GetMapping("/payment/lb")
    public Result getServerPort();

    /**
     * 服务端提供的超时接口
     * @return 端口号
     */
    @GetMapping("/payment/timeout")
    public Result timeOut();
}
