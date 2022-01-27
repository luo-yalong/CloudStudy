package com.lyl.springcloud.service;

import com.lyl.springcloud.entity.Result;

/**
 * @author 罗亚龙
 * @date 2022/1/27 15:03
 */
public interface HystrixService {

    /**
     * 正常的方法
     * @param id id
     * @return Result
     */
    Result hystrix_OK(Integer id);

    /**
     * 超时的方法
     * @param id id
     * @return Result
     */
    Result hystrix_Timeout(Integer id);
}
