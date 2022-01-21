package com.lyl.springcloud.entity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;

/**
 * (Payment)表实体类
 *
 * @author 罗亚龙
 * @since 2022-01-21 11:08:34
 */
@Data
@Accessors(chain = true)
public class PaymentVo {


    private BigInteger id;

    /**
     * serial
     */
    private String serial; 

}

