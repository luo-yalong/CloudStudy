package com.lyl.springcloud.service;

import cn.hutool.core.util.IdUtil;
import com.lyl.springcloud.entity.Result;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author 罗亚龙
 * @date 2022/1/27 15:03
 */
@Slf4j
@Service
public class HystrixServiceImpl implements HystrixService {
    @Override
    public Result hystrix_OK(Integer id) {
        String str = "hystrix_OK  [" + Thread.currentThread().getName() + "]      参数：" + id;
        log.info(str);
        return Result.success("查询成功", str);
    }


    @Override
    @HystrixCommand(fallbackMethod = "hystrix_TimeoutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "5000")
    })
    public Result hystrix_Timeout(Integer id) {
        int timeoutTime = 3;
        try {
            TimeUnit.SECONDS.sleep(timeoutTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String str = "hystrix_Timeout  [" + Thread.currentThread().getName() + "]      参数：" + id + "    耗时：" + timeoutTime;
        log.info(str);
        return Result.success("查询成功", str);
    }

    /**
     * 服务降级（兜底的方法）
     * @param id
     * @return
     */
    public Result hystrix_TimeoutHandler(Integer id){
        String str = "hystrix_Timeout  [" + Thread.currentThread().getName() + "]  参数：" + id + "  8001 服务器繁忙或者接口异常 ";
        log.info(str);
        return Result.success("8001 服务器繁忙或者接口异常", str);
    }

    @Override
    @HystrixCommand(fallbackMethod = "paymentCircuitBreakerHandler",commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),  //是否开启断路器
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "10"),  //请求次数
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds",value = "10000"),  //时间窗口期
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "60")  // 失败率达到多少的时候跳闸
    })
    public Result paymentCircuitBreaker(Integer id) {
        if (id < 0){
            throw  new RuntimeException("*****id不能为负数");
        }
        String uuid = IdUtil.simpleUUID();

        String str = "调用成功：" + Thread.currentThread().getName() + "\t\t流水号：" + uuid;

        return Result.success(str);
    }

    private Result paymentCircuitBreakerHandler(Integer id){
        return Result.fail("id不能为负数，请稍后再试");
    }


}
