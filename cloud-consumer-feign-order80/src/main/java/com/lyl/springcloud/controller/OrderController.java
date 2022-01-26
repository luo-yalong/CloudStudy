package com.lyl.springcloud.controller;

import cn.hutool.log.Log;
import com.lyl.springcloud.entity.Result;
import com.lyl.springcloud.service.ProviderFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/consumer")
public class OrderController {

    @Resource
    private ProviderFeignService feignService;

    /**
     * 通过id查询支付数据
     * @param id id
     * @return 支付数据
     */
    @GetMapping("/payment/{id:\\d+}")
    public Result getById(@PathVariable("id") Long id){
        log.info("查询的id = " + id);
        return feignService.getById(id);
    }

    @GetMapping("/payment/lb")
    public Result getServerPort(){
        return feignService.getServerPort();
    }
}
