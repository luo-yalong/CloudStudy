package com.lyl.springcloud;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;

/**
 * @author 罗亚龙
 * @date 2022/2/8 15:09
 */
@SpringBootTest
public class TimeTests {

    @Test
    public void testTime() {
        ZonedDateTime dateTime = ZonedDateTime.now();
        System.out.println("dateTime = " + dateTime);
    }
}
