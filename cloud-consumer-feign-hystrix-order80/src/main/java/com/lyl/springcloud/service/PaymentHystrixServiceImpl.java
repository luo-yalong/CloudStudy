package com.lyl.springcloud.service;

import com.lyl.springcloud.entity.Result;
import org.springframework.stereotype.Component;

/**
 * @author 罗亚龙
 * @date 2022/1/28 16:22
 */
@Component
public class PaymentHystrixServiceImpl implements PaymentHystrixService {
    @Override
    public Result hystrix_OK(Integer id) {
        return Result.fail("-------------fallback  : hystrix_OK ");
    }

    @Override
    public Result hystrix_Timeout(Integer id) {
        return Result.fail("-------------fallback : hystrix_Timeout");
    }
}
