package com.lyl.springcloud.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 罗亚龙
 * @date 2022/2/8 14:16
 */
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("path_route_baidu_guonei", r -> r.path("/guonei").uri("http://news.baidu.com/guonei"))
                .route("path_route_baidu_guoji", r -> r.path("/guoji").uri("http://news.baidu.com/guoji"))
                .build();
    }
}
