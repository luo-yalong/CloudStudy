package com.lyl.springcloud.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyl.springcloud.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (Payment)表数据库访问层
 *
 * @author 罗亚龙
 * @since 2022-01-21 11:08:34
 */
@Mapper
public interface PaymentDao extends BaseMapper<Payment> {
    
    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<Payment> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<Payment> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<Payment> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<Payment> entities);
}

