package com.lyl.springcloud.service;

import com.lyl.springcloud.entity.Result;
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
        return Result.success("查询成功",str);
    }

    @Override
    public Result hystrix_Timeout(Integer id) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String str = "hystrix_OK  [" + Thread.currentThread().getName() + "]      参数：" + id;
        log.info(str);
        return Result.success("查询成功",str);
    }
}
