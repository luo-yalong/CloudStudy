package com.lyl.springcloud.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * (Payment)表实体类
 *
 * @author 罗亚龙
 * @since 2022-01-21 11:08:34
 */
@Data
@Accessors(chain = true)
public class Payment implements Serializable{
    private static final long serialVersionUID = 893605518102438670L;

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private BigInteger id; 

    /**
     * serial
     */
    private String serial; 

}

