package com.lyl.springcloud.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyl.springcloud.dao.PaymentDao;
import com.lyl.springcloud.entity.Payment;
import com.lyl.springcloud.service.PaymentService;
import org.springframework.stereotype.Service;

/**
 * (Payment)表服务实现类
 *
 * @author 罗亚龙
 * @since 2022-01-21 11:08:34
 */
@Service("paymentService")
public class PaymentServiceImpl extends ServiceImpl<PaymentDao, Payment> implements PaymentService {

}

