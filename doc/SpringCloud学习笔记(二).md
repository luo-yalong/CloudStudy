# SpringCloud学习笔记(二)

​		上一篇，回顾了项目搭建的基本流程，同时完成了订单模块和支付模块的开发。本章将开始整合SpringCloud的各种技术。属于初级篇

## 1. Eureka ❌

### 1.1 Eureka 基础知识

1. **什么是服务治理**

   ​		Spring Cloud封装了 `Netflix` 公司开发的 `Eureka` 模块来实现服务治理

   ​		在传统的 `rpc` 远程调用框架中，管理每个服务与服务之前的依赖关系比较复杂，管理比较复杂，所以需要使用服务hi里，管理服务与服务之间的依赖关系，可以使用服务调用、负载均衡、容错等，实现服务的注册与发现。

2. **什么是服务注册与发现**

   ​		Eureka包含两个组件：`Eureka Server`和`Eureka Client`。

   ​		`Eureka Server`提供服务注册服务，各个节点启动后，会在`Eureka Server`中进行注册，这样`EurekaServer`中的服务注册表中将会存储所有可用服务节点的信息，服务节点的信息可以在界面中直观的看到。

   ​		`Eureka Client`是一个[java](https://baike.baidu.com/item/java/85979)客户端，用于简化与`Eureka Server`的交互，客户端同时也就是一个内置的、使用轮询(`round-robin`)负载算法的[负载均衡器](https://baike.baidu.com/item/负载均衡器/8852239)。

   ​		在应用启动后，将会向`Eureka Server`发送心跳,默认周期为30秒，如果`Eureka Server`在多个心跳周期内没有接收到某个节点的心跳，`Eureka Server`将会从服务注册表中把这个服务节点移除(默认90秒)。

   ​		Eureka Server之间通过复制的方式完成数据的同步，Eureka还提供了客户端缓存机制，即使所有的`Eureka Server`都挂掉，客户端依然可以利用缓存中的信息消费其他服务的API。综上，Eureka通过心跳检查、[客户端缓存](https://baike.baidu.com/item/客户端缓存/10237000)等机制，确保了系统的高可用性、灵活性和可伸缩性。

![image-20220122174930114](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/6ba138096738737d4e2c843a671c4946.jpeg)

### 1.2 构建单机版

#### 1.2.1 新建模块

​		新建一个 `Eureka Server` 模块。

#### 1.2.2 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-eureka-server7001</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <!--eureka-server-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <dependency>
            <groupId>com.lyl</groupId>
            <artifactId>cloud-api-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>
```

#### 1.2.3 写 `yml`

```yaml
server:
  port: 7001
eureka:
  instance:
    hostname: localhost
  client:
    #false 表示不向eureka注册自己
    register-with-eureka: false
    #false 表示自己就是注册中心，职责就是维护实例，不需要检索服务
    fetch-registry: false
    service-url:
      #设置和 eureka server 交互的地址查询服务和注册服务都需要依赖这个地址
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka
```

#### 1.2.4 主启动

**注意：`Eureka Server` 需要使用 `@EnableEurekaServer`注解，启动服务**

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaMain7001 {
    public static void main(String[] args) {
        SpringApplication.run(EurekaMain7001.class,args);
    }
}
```

#### 1.2.5 测试

​		在浏览器中输入：[http://localhost:7001/](http://localhost:7001/) ，结果页面如下

![image-20220122182732734](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/7a0ec0394cf349ac0b99129fef2fb3cd.jpeg)

### 1.3 支付微服务入驻 `Eureka Server`

#### 1.3.1 改 `pom`

​		在 `pom` 文件中添加 `Eureka client` 依赖

```xml
<!--eureka-client-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

#### 1.3.2 改 `yml`

```yaml
server:
  port: 8001

spring:
  application:
    name: cloud-payment-service
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource            # 当前数据源操作类型
    driver-class-name: com.mysql.cj.jdbc.Driver              # mysql驱动包
    url: jdbc:mysql://121.89.199.231:3306/cloud_study?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: Lyl123456

eureka:
  client:
    #表示不向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
      defaultZone: http://localhost:7001/eureka

mybatis-plus:
  type-aliases-package: com.lyl.springcloud.entity    #别名
  mapper-locations: classpath:mapper/*.xml
```

#### 1.3.3 主启动

​		`Eureka Client` 需要在启动类上面添加 `@EnableEurekaClient` 来启动`Eureka Client`

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author luoyalong
 */
@SpringBootApplication
@EnableEurekaClient
public class PaymentMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8001.class,args);
    }
}
```

#### 1.3.4 测试

​		启动 支付模块，然后打开 [http://localhost:7001/](http://localhost:7001/) ，结果页面如下

**注意：测试之前，需要先启动注册中心**

支付服务已经被注册进 `Eureka Server`

![image-20220122184854273](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/2df501372bb3420347b8aff006bd6fe8.jpeg)

### 1.4 订单微服务入驻 `Eureka Server`

#### 1.4.1 改 `pom`

​		在 `pom` 文件中添加 `Eureka client` 依赖

```xml
<!--eureka-client-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

#### 1.4.2 改 `yml`

```yaml
server:
  port: 80
spring:
  application:
    name: cloud-order-service

eureka:
  client:
    #表示不向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
      defaultZone: http://localhost:7001/eureka
```

#### 1.4.3 主启动

​			`Eureka Client` 需要在启动类上面添加 `@EnableEurekaClient` 来启动`Eureka Client`

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author 罗亚龙
 * @date 2022/1/21 14:04
 */
@SpringBootApplication
@EnableEurekaClient
public class OrderMain80 {

    public static void main(String[] args) {
        SpringApplication.run(OrderMain80.class,args);
    }

}
```

#### 1.4.4 测试

​		启动 订单模块，然后打开 [http://localhost:7001/](http://localhost:7001/) ，结果页面如下

![image-20220122190138940](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/eb57bbac67d8947160176acb897dcd61.jpeg)

**注意：测试之前，需要先启动注册中心**

订单服务已经被注册进 `Eureka Server`

### 1.5 Eureka集群

#### 1.5.1 集群基本介绍

![image-20220122190723339](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/84c304644374382403f34641cfa552f4.jpeg)

**集群的基本原理**

​		多个节点之间互相注册，相互守望。

![image-20220122190848715](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/c3eb8efeb14dc151465bcd58f1769caa.jpeg)

#### 1.5.2 搭建集群

​		新建一个 注册中心模块 ，两个 `Eureka Server` 构成集群

**==重点==**：在开始之前，需要先修改 电脑的 `hosts` 文件，新增以下内容

```reStructuredText
127.0.0.1  eureka7001.com
127.0.0.1  eureka7002.com
```

##### 1.5.2.1 新建模块

​		新建一个名为 `cloud-eureka-server7002` 的模块

##### 1.5.2.2 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-eureka-server7002</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <!--eureka-server-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <dependency>
            <groupId>com.lyl</groupId>
            <artifactId>cloud-api-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>
```

##### 1.5.2.3 改 `yml`

```yaml
server:
  port: 7002
eureka:
  instance:
    hostname: eureka7002.com #eureka服务端的实例名称
  client:
    #false 表示不向eureka注册自己
    register-with-eureka: false
    #false 表示自己就是注册中心，职责就是维护实例，不需要检索服务
    fetch-registry: false
    service-url:
      #设置和 eureka server 交互的地址查询服务和注册服务都需要依赖这个地址
      defaultZone: http://eureka7001.com:7001/eureka
```

##### 1.5.2.4 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaMain7002 {
    public static void main(String[] args) {
        SpringApplication.run(EurekaMain7002.class,args);
    }
}
```

##### 1.5.2.5 修改`cloud-eureka-server7001`  yml

​		修改 `cloud-eureka-server7001` yml，使 `Eureka Server` 互相注册

```yaml
server:
  port: 7001
eureka:
  instance:
    hostname: eureka7001.com
  client:
    #false 表示不向eureka注册自己
    register-with-eureka: false
    #false 表示自己就是注册中心，职责就是维护实例，不需要检索服务
    fetch-registry: false
    service-url:
      #设置和 eureka server 交互的地址查询服务和注册服务都需要依赖这个地址
      defaultZone: http://eureka7002.com:7002/eureka
```

##### 1.5.2.6 测试

​		分别启动两个注册中心，分别使用 http://eureka7001.com:7001  、 http://eureka7002.com:7002 打开监控页面，可以看到注册中心中有了对方

1. http://eureka7001.com:7001

   ![image-20220122194744961](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/a03aff787b78e33310d515affe359fa6.jpeg)

2. http://eureka7002.com:7002

   ![image-20220122194713334](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/fd0fa3cf2b90e84e004fcf5ac3aaa94b.jpeg)

### 1.6 将订单支付服务注册Eureka集群

#### 1.6.1 修改支付服务 `yml`

```yaml
server:
  port: 8001

spring:
  application:
    name: cloud-payment-service
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource            # 当前数据源操作类型
    driver-class-name: com.mysql.cj.jdbc.Driver              # mysql驱动包
    url: jdbc:mysql://121.89.199.231:3306/cloud_study?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: Lyl123456

eureka:
  client:
    #表示是否向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
#      defaultZone: http://localhost:7001/eureka  #单机版
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  #集群版

mybatis-plus:
  type-aliases-package: com.lyl.springcloud.entity    #别名
  mapper-locations: classpath:mapper/*.xml
```

#### 1.6.2 修改订单服务 `yml`

```yaml
server:
  port: 80
spring:
  application:
    name: cloud-order-service

eureka:
  client:
    #表示是否向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
#      defaultZone: http://localhost:7001/eureka  #单机版
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka
```

#### 1.6.3 测试Eureka集群

1. http://eureka7001.com:7001

   ![image-20220122214435802](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/3af7bfc222cdfbf0cac1d466c3764ab4.jpeg)

2. http://eureka7002.com:7002

   ![image-20220122214459476](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/1dd8f49dce2d156104ed831512bcfec0.jpeg)

3. 测试订单请求：http://localhost/consumer/payment/10

   ![image-20220122214619643](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/e16b67b8a2b52b0d39579fecd6e279d7.jpeg)

### 1.7 搭建支付服务集群

​		之前，我们已经实现了 `Eureka` 集群配置。为了保证高可用，我们需要将`支付服务` 也做成集群。

#### 1.7.1 新建模块

​		新建一个名为 `cloud-provider-payment8002` 模块，其中的代码几乎和 `cloud-provider-payment8001` 一样

#### 1.7.2 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-provider-payment8002</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>


    <dependencies>

        <!--eureka-client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!--公共模块-->
        <dependency>
            <groupId>com.lyl</groupId>
            <artifactId>cloud-api-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>1.1.10</version>
        </dependency>
        <!--mysql-connector-java-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <!--jdbc-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>
```

#### 1.7.3 改 `yml`

```yaml
server:
  port: 8002

spring:
  application:
    name: cloud-payment-service
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource            # 当前数据源操作类型
    driver-class-name: com.mysql.cj.jdbc.Driver              # mysql驱动包
    url: jdbc:mysql://121.89.199.231:3306/cloud_study?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: Lyl123456

eureka:
  client:
    #表示是否向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
      #      defaultZone: http://localhost:7001/eureka  #单机版
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  #集群版

mybatis-plus:
  type-aliases-package: com.lyl.springcloud.entity    #别名
  mapper-locations: classpath:mapper/*.xml
```

#### 1.7.4 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class PaymentMain8002 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8002.class,args);
    }
}
```

#### 1.7.5 业务类

​		复制 `cloud-provider-payment8001` 模块中的业务类即可。但是，为了区分 `Eureka` 注册中心到底调用的是那个服务，所以，我们需要对 这两个支付服务的 `Controller` 类，做一个修改，让请求的时候，返回服务的端口号。

==注意：两个支付服务都需要修改== 

**具体操作**

-  在 `PaymentController` 中注入一个 `serverPort` 端口号

  ```java
  @Value("${server.port}")
  private Integer serverPort;
  ```

- 修改获取方法，返回端口号

  ```java
  /**
   * 通过id查询支付数据
   * @param id id
   * @return 支付数据
   */
  @GetMapping("{id:\\d+}")
  public Result getById(@PathVariable("id") Long id){
      Payment payment = paymentService.getById(id);
      return Result.success("查询成功,serverPort: " + serverPort,payment);
  }
  ```

**完整代码**

```java
package com.lyl.springcloud.controller;

import com.lyl.springcloud.entity.Payment;
import com.lyl.springcloud.entity.Result;
import com.lyl.springcloud.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author 罗亚龙
 * @date 2022/1/21 11:15
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Resource
    private PaymentService paymentService;

    @Value("${server.port}")
    private Integer serverPort;

    /**
     * 创建一个支付数据
     * @param payment 请求参数
     * @return 执行结果
     */
    @PostMapping("/create")
    public Result create(@RequestBody Payment payment) {
        boolean save = paymentService.save(payment);
        return save ? Result.success("添加成功") : Result.fail("添加失败");
    }

    /**
     * 通过id查询支付数据
     * @param id id
     * @return 支付数据
     */
    @GetMapping("{id:\\d+}")
    public Result getById(@PathVariable("id") Long id){
        Payment payment = paymentService.getById(id);
        return Result.success("查询成功,serverPort: " + serverPort,payment);
    }
}
```

#### 1.7.6 测试

​		上述修改完成之后，启动一个模块，进行测试，查看是否成功的返回了端口号。

![image-20220122222300560](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/0e7cdcd20bef7f37f90f391904709021.jpeg)

​		修改完成之后，我们需要查看集群中是否注册有多个支付服务。还是使用http://eureka7001.com:7001 或者 http://eureka7002.com:7002 来查看。

![image-20220122222808002](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/1f1745fb7d837c06dc491f098560d6f1.jpeg)

​		我们可以看到图中上线了两个支付服务。

---

​		在这之后，我们调用订单服务来查询 ，来查看调用的是哪个支付服务。

调用链接：http://localhost/consumer/payment/10

![image-20220122223110707](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/2babd9b1bce0e012ce941673edfdd56a.jpeg)

#### 1.7.7 修改支付服务

​		在上面调用的时候，我们发现每次调用都返回`8001`端口，没有一次使用到 `8002`端口。遇到这种情况不要慌，那是因为，我们在订单服务中将 `url` 写死了，只能访问到 `8001` 端口的支付服务。因此，我们需要修改订单服务，将固定的 `url` 改成调用支付服务的名称 **`CLOUD-PAYMENT-SERVICE`**

修改 `cloud-consumer-order80` 模块中的 `OrderController`

```java
//public static final String PAYMENT_URL = "http://localhost:8001";

public static final String PAYMENT_URL = "http://CLOUD-PAYMENT-SERVICE";
```

⭐️ 改完之后，不要急，还需要开启 `RestTemplate`  的负载均衡能力，否则会报错。

- 修改配置类 `ApplicationContextConfig` 中的 `RestTemplate` 添加注解 `@LoadBalanced`

  ```java
  package com.lyl.springcloud.config;
  
  import org.springframework.cloud.client.loadbalancer.LoadBalanced;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.web.client.RestTemplate;
  
  /**
   * @author 罗亚龙
   * @date 2022/1/21 14:26
   */
  @Configuration
  public class ApplicationContextConfig {
  
      @Bean
      @LoadBalanced
      public RestTemplate restTemplate(){
          return new RestTemplate();
      }
  }
  ```

之后，就可以重新启动服务，开始测试了。

#### 1.7.8 再次测试

​		在 `postman` 中调用 http://localhost/consumer/payment/10 ，来查看订单服务具体调用的是那个支付服务，发现每次都会切换端口号。一下 `8001`，一下 `8002`，交替出现。

![image-20220122224718663](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/41e56637df9e4be9375724578d449c17.jpeg)

![image-20220122224700303](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/b7d6f95c5edf1ba0d224ee8caed3c9d3.jpeg)

### 1.8 actuator微服务信息的完善

​		虽然说，目前我们已经基本完成了 `Eureka` 和 支付模块 的集群搭建，但是一些细节问题，还是要完善一下的。例如注册中心服务的id显示的ip地址和端口号，不美观，我们可以自定义实例id。同时鼠标悬浮在 实例id 上面的时候，浏览器的左下角不会显示服务的 ip和端口号。为了更好的体验，我们先修改 两个支付服务，其他可自行修改。

​		开始之前，我们先看一下原先的

![image-20220122230406166](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/da767e93be537e3cb6e96dab816e3451.jpeg)

修改 支付模块的 `yml` 文件，在 `Eureka` 的属性下面配置上

```yml
instance:
  #实例id,显示在Eureka注册中心的名字，默认是ip地址+端口号
  instance-id: Payment8001
  #访问路径是否显示ip地址
  prefer-ip-address: true
```

记得：`instance` 和 `client` 同级

完整的 `yml`  `Eureka`配置如下

```yml
eureka:
  client:
    #表示是否向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
#      defaultZone: http://localhost:7001/eureka  #单机版
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  #集群版
  instance:
    #实例id,显示在Eureka注册中心的名字，默认是ip地址+端口号
    instance-id: Payment8001
    #访问路径是否显示ip地址
    prefer-ip-address: true
```

修改完成之后，再次查看 `Eureka` 注册中心服务的实例id

![image-20220122230806076](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-22/543f91f3452d44b7f351d93464d975d3.jpeg)

⭐️ 注意：配置它们需要确保 `pom` 文件中有`spring-boot-starter-actuator` 这个依赖，它一般是和 `spring-boot-starter-web` 成对出现

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 1.9 服务发现 `Discovery`

​		**功能： ** 对于注册进 `Eureka` 里面的微服务，可以通过服务发现来获得该服务的信息

​		需要在==主启动==类上面添加注解 `@EnableDiscoveryClient`

#### 1.9.1 修改 `cloud-provider-payment8001` 中 `PaymentController`

​		向里面注入 `DiscoveryClient` ，并编写一个接口来发现服务

```java
@Resource
private DiscoveryClient discoveryClient;

@GetMapping("/discovery")
public Object discovery(){
    List<String> services = discoveryClient.getServices();
    //获取所有的服务名称
    for (String service : services) {
        log.info("***** 服务名称: " + service);
    }

    List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
    log.info("服务名ID\t主机\t端口号\tURI");
    for (ServiceInstance instance : instances) {
        log.info(instance.getServiceId() + "\t" + instance.getHost() + "\t" + instance.getPort() + "\t" + instance.getUri());
    }
    return discoveryClient;
}
```

#### 1.9.2 测试

​		等待重启，或者手工重启，调用接口。

![image-20220123104114769](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/72a7688edb123eb849b59163efc446ae.jpeg)

控制台打印

![image-20220123104151157](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/a5bdccf2991bf780ff7c3f2a3676f2f4.jpeg)

按照同样的方式修改 `cloud-provider-payment8002`

### 1.10 Eureka自我保护

#### 1.10.1 故障现象

​		概述：保护模式是一组客户端和 Eureka Server之前存在着网络分区场景下的保护，一旦进保护模式。`Eureka Server` 将会尝试==保护其服务注册表中的信息，不在删除服务注册表中的数据，也就是不会注销任务微服务==。

​		如果在 `Eureka Server` 首页看到了以下提示，则说明 `Eureka` 进入了==保护模式==

![image-20220123105331520](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/0c043ae69768361a5041b17137bbf615.jpeg)

#### 1.10.2 导致原因

**一句话：某时刻某一个微服务不可用了，`Eureka` 不会立即清理，依旧会对该微服务的信息进行保存**

> **设计思想**： 属于 CAP 里面的 AP 分支

1. **为什么会产生 `Eureka` 自我保护机制？**

   ​		为了防止 `Eureka Client` 可以正常运行，但是 与 `Eureka Server` 网络不通情况下，`Eureka Server` 不会立刻将 `Eureka Client`  服务剔除

2. **什么是自我保护模式 服务？**

   ​		默认情况下，如果 `Eureka Server` 在一定时间内没有接收到某个微服务实力实例的心跳， `Eureka Server` 将会注销该实例 （默认90秒）。但是当网络分区故障的发生（延时、卡顿、拥挤）时，微服务与 `Eureka Server` 之间无法保证正常的通信，以上行为可能变得非常危险了 ------ 因为微服务本身其实是健康的，==此时本不应该注销这个微服务== 。`Eureka` 通过自我保护模式 来解决这个问题  ------ 当 `Eureka Server` 节点在短时间内丢失过多客户端时（此时可能发生了网络分区故障），那个这个节点就会进入自我保护模式。 

   ![image-20220123110620345](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/2a15ce9c98cfb12e25019d2bf7b24286.jpeg)

3. **总结**

   ​		在自我保护模式下，`Eureka Server` 会保护服务注册表中的信息，不会注销任何服务实例。它的设计哲学就是宁可保留错误的服务注册信息，也不盲目注销任何可能健康的服务实例。

   ​		综上，自我保护模式是一种应对网络异常的安全保护模式，它的架构哲学是宁可同时保留所有微服务（健康的微服务和不健康的微服务）也不盲目注销任何健康的微服务。使用自我保护模式，可以让 `Eureka` 集群更加的健壮、稳定。

#### 1.10.3 禁用自我保护模式

1. 在 `Eureka` 中心配置 关闭自我保护模式

   1. 修改 `cloud-eureka-server7001`  模块的 `yml` 文件

      ```yaml
      eureka:
          server:
                # 关闭自我保护机制，保证不可用服务被及时删除
                enable-self-preservation: false
                # 如果一定时间内没有发送心跳包，就注销服务
                eviction-interval-timer-in-ms: 2000
      ```

   2. **完整代码**

      ```yml
      server:
        port: 7001
      eureka:
        instance:
          hostname: eureka7001.com
        client:
          #false 表示不向eureka注册自己
          register-with-eureka: false
          #false 表示自己就是注册中心，职责就是维护实例，不需要检索服务
          fetch-registry: false
          service-url:
            #设置和 eureka server 交互的地址查询服务和注册服务都需要依赖这个地址
            defaultZone: http://eureka7002.com:7002/eureka
        server:
          # 关闭自我保护机制，保证不可用服务被及时删除
          enable-self-preservation: false
          # 如果一定时间内没有发送心跳包，就注销服务
          eviction-interval-timer-in-ms: 2000
      ```

   3. **重启，查看关闭效果**

      ![image-20220123112910540](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/1d7a1e21effd7f03a94ddabf680da102.jpeg)

2. 修改 `cloud-provider-payment8001`  

   1. 修改 `yml` 

      ```yml
      eureka:
          instance:
              #Eureka客户端向服务端发送心跳的时间间隔，单位为秒（默认30秒）
              lease-renewal-interval-in-seconds: 1
              # Eureka 服务端在收到最后一次心跳后等待时间上限，单位为秒（默认是90秒），超时将会被剔除
              lease-expiration-duration-in-seconds: 2
      ```

   2. 完整代码

      ```yml
      server:
        port: 8001
      
      spring:
        application:
          name: cloud-payment-service
        datasource:
          type: com.alibaba.druid.pool.DruidDataSource            # 当前数据源操作类型
          driver-class-name: com.mysql.cj.jdbc.Driver              # mysql驱动包
          url: jdbc:mysql://121.89.199.231:3306/cloud_study?useUnicode=true&characterEncoding=utf-8&useSSL=false
          username: root
          password: Lyl123456
      
      eureka:
        client:
          #表示是否向eureka注册自己
          register-with-eureka: true
          #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
          fetch-registry: true
          service-url:
            #注册中心地址
      #      defaultZone: http://localhost:7001/eureka  #单机版
            defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  #集群版
        instance:
          #实例id,显示在Eureka注册中心的名字，默认是ip地址+端口号
          instance-id: Payment8001
          #访问路径是否显示ip地址
          prefer-ip-address: true
          #Eureka客户端向服务端发送心跳的时间间隔，单位为秒（默认30秒）
          lease-renewal-interval-in-seconds: 1
          # Eureka 服务端在收到最后一次心跳后等待时间上限，单位为秒（默认是90秒），超时将会被剔除
          lease-expiration-duration-in-seconds: 2
      
      mybatis-plus:
        type-aliases-package: com.lyl.springcloud.entity    #别名
        mapper-locations: classpath:mapper/*.xml
      ```

   3. 测试

      ​		依次启动 `Eureka` 注册中心和 `支付服务8001` ，查看`Eureka Server` 首页

      ![image-20220123114209631](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/3b66a38e11d97b9410f8dfb51245fdb2.jpeg)

      手动关闭 `支付模块8001` 假设出故障了

      ![`image-20220123131129073`](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/6e9578d66825a508f26bf85abf738d6c.jpeg)

### 1.11 `Eureka` 停止更新

​		官网上说：已经对 `Eureka` 停止更新了 ，可以使用 `Zookeeper` 替换 `Eureka`

## 2. Zookeeper

> 在开始之前，确保已经搭建好了 `zookeeper` 环境

**简介**

​		`zookeeper` 是一个 分布式协调工具，可以实现注册中心的功能

**安装Zookeeper**

​		[windows环境下安装zookeeper教程详解（单机版）_风轩雨墨的博客-CSDN博客_zookeeper安装教程](https://blog.csdn.net/qq_33316784/article/details/88563482)

### 2.1 支付模块注册进 `Zookeeper`

#### 2.1.1 新建支付模块

​		新建一个名为 `cloud-provider-payment8004` 的模块。此模块只是演示注册，所以去掉`mybatis-plus`、`Mysql` 等一些其他依赖

#### 2.1.2 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-provider-payment8004</artifactId>

    <dependencies>

        <!--zookeeper-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
        </dependency>

        <!--公共模块-->
        <dependency>
            <groupId>com.lyl</groupId>
            <artifactId>cloud-api-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

#### 2.1.3 改 `yml`

```yml
server:
  port: 8004

#服务别名，注册zookeeper到注册中心名称
spring:
  application:
    name: cloud-payment-service
  cloud:
    zookeeper:
      connect-string: localhost:2181
```

#### 2.1.4 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author 罗亚龙
 * @date 2022/1/23 13:41
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentMain8004 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8004.class,args);
    }
}
```

#### 2.1.5 业务类

```java
package com.lyl.springcloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author 罗亚龙
 * @date 2022/1/21 11:15
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {


    @Value("${server.port}")
    private Integer serverPort;

    /**
     * 获取注册进zookeeper服务的端口号
     * @return
     */
    @GetMapping("/zk")
    public String paymentZk(){
        return "Spring Cloud with Zookeeper: " + serverPort + "\t" + UUID.randomUUID().toString();
    }
}
```

#### 2.1.6 测试

1. 启动本地的 `zookeeper` 服务

   ​		打开本地的 `zookeeper`安装目录，到 `bin` 目录下，双击 `zkServer.cmd`

   ![image-20220123154259283](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/54608b3523ad0af2702fb35903d31429.jpeg)

2. 启动本地的 `zookeeper` 客户端

   ​		同样，在 `zookeeper`安装目录下 `bin` 文件夹红，双击 `zkCli.cmd`

   ![image-20220123154513309](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/0faa5eee7e25507c4f33bcda9afddd44.jpeg)

   **查看所有的服务**

   ```
   ls /
   ```

   ![image-20220123155416467](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/c3be72646da7c2ffb67b2ddf65c7803e.jpeg)

3. 启动支付模块，可以看到 `services` 中出现  支付模块的服务别名

   ![image-20220123154911929](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/20ab2b0f180b83dd9345876cc37fcd65.jpeg)

4. 测试接口

   访问 http://localhost:8004/payment/zk

   ![image-20220123155627627](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/9b1eab72f1dd7b49d8ab4297bbc31936.jpeg)

5. 服务节点是临时节点还是持久节点

   结论： 是临时节点。

   ![image-20220123161618862](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/88904b7e36dfce728d5e981ea998a1a0.jpeg)

### 2.2 订单服务注册进 `Zookeeper`

#### 2.2.1 新建模块

​		新建一个名为 `cloud-consumer-zk-order80` 模块

#### 2.2.2 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-consumer-zk-order80</artifactId>

    <dependencies>

        <!--zookeeper-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
        </dependency>

        <!--公共模块-->
        <dependency>
            <groupId>com.lyl</groupId>
            <artifactId>cloud-api-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

#### 2.2.3 改 `yml` 

```yml
server:
  port: 80

#服务别名，注册zookeeper到注册中心名称
spring:
  application:
    name: cloud-order-zk-service
  cloud:
    zookeeper:
      connect-string: localhost:2181
```

#### 2.2.4 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author 罗亚龙
 * @date 2022/1/23 16:29
 */
@SpringBootApplication
@EnableDiscoveryClient
public class OrderZkMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderZkMain80.class,args);
    }
}
```

#### 2.2.5 业务类

1. **配置类**

   ```java
   package com.lyl.springcloud.config;
   
   import org.springframework.cloud.client.loadbalancer.LoadBalanced;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.web.client.RestTemplate;
   
   /**
    * @author 罗亚龙
    * @date 2022/1/23 16:28
    */
   @Configuration
   public class ApplicationContextConfig {
   
       @Bean
       @LoadBalanced
       public RestTemplate restTemplate(){
           return new RestTemplate();
       }
   }
   ```

2. **控制器**

   ```java
   package com.lyl.springcloud.controller;
   
   import lombok.extern.slf4j.Slf4j;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RestController;
   import org.springframework.web.client.RestTemplate;
   
   import javax.annotation.Resource;
   
   /**
    * @author 罗亚龙
    * @date 2022/1/23 16:31
    */
   @RestController
   @Slf4j
   public class OrderController {
   
       public static final String INVOKE_URL = "http://cloud-payment-service";
   
       @Resource
       private RestTemplate restTemplate;
       
       @GetMapping("/consumer/payment/zk")
       public String getPaymentInfo(){
           return restTemplate.getForObject(INVOKE_URL + "/payment/zk", String.class);
       }
   }
   ```

#### 2.2.6 测试

1. 查看 `zookeeper` 客户端的服务名称

   ![image-20220123163611557](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/e0a4c78e8439fa6ebe50a2b7222872a4.jpeg)

2. 查看接口

   调用 http://localhost/consumer/payment/zk

   ![image-20220123163734698](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/532206f564df147f203256b988851ca7.jpeg)

## 3. Consul 

### 3.1 简介

​		[官网](https://www.consul.io/intro/index.html)

​		`Consul` 是一套开源的分布式服务发现和配置管理系统，由 `HashiCorp` 公司用 Go语言开发。

​		提供了微服务系统中的服务治理、配置中心、控制总线等功能。这些功能中的每一个都可以根据需要单独使用，也可以一起使用以构建全方位的服务网格，总之 `Consul` 提供了一种完整的服务网格解决方案。

​		它具有很多优点。包括：基于 `raft` 协议，比较简洁；支持健康检查，同时支持 HTTP 和 DNS 协议，支持跨数据中心的 `WAN` 集群 ，提供图形界面跨平台，支持 `Linux` 、`Mac` 、`Windows`

**能干嘛**

![image-20220123202131344](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/5581d8a70d5d22910cacc22c839b0db4.jpeg)

**怎么下载**

​		[官网下载](https://www.consul.io/downloads)

**怎么玩**

​		https://www.springcloud.cc/spring-cloud-consul.html

### 3.2 安装并运行 `Consul`

​		下载：[官网下载](https://www.consul.io/downloads)

​		从官网下载对应系统的 `Consul`，并进行安装

​		在命令行输入：`consul agent -dev` 启动 `consul`

​		在浏览器中输入： http://localhost:8500 , 出现 `Consul` 的管理页面

![image-20220123214603082](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/fcd68f1d8346040837b71cf2c6897ec0.jpeg)



### 3.3 服务提供者

#### 3.3.1 新建 `module`

​		新建一个名为 `cloud-provider-consul-payment8006`的模块

#### 3.3.2 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-provider-consul-payment8006</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>

        <!--consul-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-consul-discovery</artifactId>
        </dependency>

        <!--公共模块-->
        <dependency>
            <groupId>com.lyl</groupId>
            <artifactId>cloud-api-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>
```

#### 3.3.3 改 `yml`

```yml
server:
  port: 8006
spring:
  application:
    name: cloud-payment-service
#####consul注册中心地址
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        service-name: ${spring.application.name}
```

#### 3.3.4 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PaymentMain8006 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8006.class,args);
    }
}
```

#### 3.3.5 业务类

```java
package com.lyl.springcloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    @Value("${server.port}")
    private Integer serverPort;

    /**
     * 获取注册进zookeeper服务的端口号
     * @return
     */
    @GetMapping("/consul")
    public String paymentZk(){
        return "Spring Cloud with Zookeeper: " + serverPort + "\t" + UUID.randomUUID().toString();
    }
}
```

#### 3.3.6 测试

​		启动新模块之后，可以看到注册中心，出现了服务实例。

![image-20220123214730703](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/304ce6650e512e9442703b3454de9299.jpeg)

​		测试 http://localhost:8006/payment/consul ，可以看到每次都会变更流水id

![image-20220123214809658](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/cd026a96e3ac60c1f04277b17f5d3676.jpeg)

### 3.4 服务消费者

#### 3.4.1 新建 `mudule`

​		新建一个名为 `cloud-consumer-consul-order80` 的模块

#### 3.4.2 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-consumer-consul-order80</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>

        <!--consul-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-consul-discovery</artifactId>
        </dependency>

        <!--公共模块-->
        <dependency>
            <groupId>com.lyl</groupId>
            <artifactId>cloud-api-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>
```

#### 3.4.3 改 `yml`

```yml
server:
  port: 80
spring:
  application:
    name: cloud-order-service
  #####consul注册中心地址
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        service-name: ${spring.application.name}
```

#### 3.4.4 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OrderConsulMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderConsulMain80.class,args);
    }
}
```

#### 3.4.5 业务类

**配置类**

```java
package com.lyl.springcloud.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationContextConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
```

**控制器**

```java
package com.lyl.springcloud.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@RestController
@RequestMapping("/consumer")
public class OrderController {

    public static final String INVOKE_URL = "http://cloud-payment-service";

    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/payment/consul")
    public String getPaymentInfo(){
        return restTemplate.getForObject(INVOKE_URL + "/payment/consul", String.class);
    }
}
```

#### 3.4.6 测试

​		启动新模块之后，可以看到注册中心，出现了服务实例。

![image-20220123214903083](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/026e98446e4aae75046028deae61c7e5.jpeg)

​		测试 http://localhost/consumer/payment/consul ，可以看到每次都会变更流水id

![image-20220123214927185](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/cd10e5bc9b7415667d97be621de3164b.jpeg)

关闭模块之后

![image-20220123215043612](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/71a72bbfc1a80ccf46cc4f63677acc46.jpeg)

## 4 三个注册中心异同点

| 组件名      | 语言 | CAP  | 健康检查 | 对外暴露接口 | SpringCloud集成 |
| ----------- | ---- | ---- | -------- | ------------ | --------------- |
| `Eureka`    | Java | AP   | 可配支持 | HTTP         | 已集成          |
| `Consul`    | Go   | CP   | 支持     | HTTP/DNS     | 已集成          |
| `Zookeeper` | Java | CP   | 支持     | 客户端       | 已集成          |

**CAP**

​		CAP原则又称CAP定理，指的是在一个分布式系统中，[一致性](https://baike.baidu.com/item/一致性/9840083)（Consistency）、[可用性](https://baike.baidu.com/item/可用性/109628)（Availability）、[分区容错性](https://baike.baidu.com/item/分区容错性/23734073)（Partition tolerance）。CAP 原则指的是，这三个[要素](https://baike.baidu.com/item/要素/5261200)最多只能同时实现两点，不可能三者兼顾。

- **C:  Consistency（强一致性）**
- **A:  Availability (可用性)**
- **P:  Partition tolerance (分区容错性)**

![img](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/c3af204999d3f6825f046c8b14538b65.jpeg)

> CAP理论关注粒度是数据，而不是整体系统涉及的策略

`Eureka` 属于 `AP` 架构

​		当网络分区出现后，为了保证可用性，系统B可以返回旧值，保证系统的可用性。

​		结论：违背了一致性 C 的要求，只满足可用性和分区容错性，即 `AP`

![image-20220123213550719](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/e58be0afa601ac61d1cff4163857d85e.jpeg)

`Zookeeper`/`Consul` 属于 `CP` 架构

​		当网络分区出现后，为了保证一致性，就必须拒绝请求，否则无法保证一致性。

​		结论：违背了可用性A的要求，只满足一致性和分区容错性，即 `CP`

![image-20220123213839606](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-23/52faeb9217fe4328a07b9c3828159b51.jpeg)

## 5. Ribbon负载均衡服务调用

### 5.1 简介

​		Spring Cloud Ribbon 是基于Netflix Ribbon实现的一套 ==客户端 负载均衡==的工具

​		简单的来说，`Ribbon` 是 `Netflix` 发布的开源项目，主要功能是提供 ==客户端软件的负载均衡算法和服务调用==。`Ribbon` 客户端组件提供一系列完善的配置项如链接超时、重试等，简单的来说，就是在配置文件中列出 `Load Balance` （简称 `LB`），后面所有的机器，`Ribbon` 会自动的帮我们基于某种规则（如简单轮询，随机连接等） 去连接这些机器。我们很容易使用 `Ribbon` 实现自定义的负载均衡算法。

**注意: `Ribbon` 目前进入了维护模式**

![image-20220124133950155](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-24/e621cc4ada3ab403be45eab33e6bd38f.jpeg)

### 5.2 LB（负载均衡）

**集中式 LB**

![image-20220124134155845](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-24/3ab6599b48f34d37e10d5a46b15f9166.jpeg)

**进程内 `LB`**

![image-20220124134122879](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-24/89508cda08e8dbc96651d49fb8676d49.jpeg)

> `Ribbon` 就是 负载均衡 + RestTemplate调用

### 5.3 `pom`

​		消费者模块 `cloud-consumer-order80` 之前已经集成了 `spring-cloud-starter-netflix-eureka-client` ，所以我们不用再次添加依赖了。因为 `spring-cloud-starter-netflix-eureka-client` 中已经集成了 `spring-cloud-starter-netflix-ribbon` 依赖。

![image-20220124135123667](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-24/1ecf5878ec97a774af9b027246012f7f.jpeg)

​		如果想要添加的话，也可以添加，但是没有必要。

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
  <version>2.2.1.RELEASE</version>
  <scope>compile</scope>
</dependency>
```

### 5.4 Api方法调用

​		`getForObject` 返回的是 `json` 字符串，而 `getForEntity` 返回了更加详细的信息，如：响应头、相应状态码、响应体等等。

**getForObject**

```java
/**
 * 通过id查询支付数据
 * @param id id
 * @return 支付数据
 */
@GetMapping("/payment/{id:\\d+}")
public Result getById(@PathVariable("id") Long id){
    return restTemplate.getForObject(PAYMENT_URL + "/payment/" + id,Result.class);
}
```

**getForEntity**

```java
/**
 * 使用 getForEntity
 * @param id
 * @return
 */
@GetMapping("/payment/getForEntity/{id:\\d+}")
public Result getForEntityById(@PathVariable("id") Long id){
    ResponseEntity<Result> responseEntity = restTemplate.getForEntity(PAYMENT_URL + "/payment/" + id, Result.class);
    if (responseEntity.getStatusCode().is2xxSuccessful()){
        return responseEntity.getBody();
    }else {
        return Result.fail(444,"操作失败");
    }
}
```

### 5.5 Ribbon核心组件 IRule

​		`IRule` : 根据特定算法从服务列表中选取一个要访问的服务

​		以下是 `Ribbon` 自带的负载均衡算法

![image-20220124140858574](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-24/b84a3ffaae22df963c9c84b01557bad7.jpeg)

### 5.6 替换负载规则

#### 5.6.1 修改 `cloud-consumer-order80` 

​		修改负载规则的时候，不能在 可以被 `@ComponentScan` 扫描到的包以及子包中，需要新建一个在 `OrderMain80` 的上级目录创建一个包。

![image-20220124142007860](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-24/dce9187ac82d4f4da6809b01c81768ee.jpeg)

创建一个 `com.lyl.myrule` 的包，存放负载规则

![image-20220124142050460](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-24/80b868001a4f20d7bcf7028ec8ef5625.jpeg)

#### 5.6.2 新建配置类

​		新建一个 `MySelfRule` 配置类，配置负载均衡规则。

```java
package com.lyl.myrule;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;

/**
 * @author 罗亚龙
 * @date 2022/1/24 14:22
 */
@Configurable
public class MySelfRule {

    @Bean
    public IRule myRule(){
        //定义为随机
        return new RandomRule();
    }
}
```

#### 5.6.3 修改启动类

​		启动类上添加注解 `@RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration = MySelfRule.class)`，配置我们配置的负载规则。

**完整代码**

```java
package com.lyl.springcloud;

import com.lyl.myrule.MySelfRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;

/**
 * @author 罗亚龙
 * @date 2022/1/21 14:04
 */
@SpringBootApplication
@EnableEurekaClient
@RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration = MySelfRule.class)
public class OrderMain80 {

    public static void main(String[] args) {
        SpringApplication.run(OrderMain80.class,args);
    }

}
```

#### 5.6.4 测试

​		测试之前，记得重新启动消费者模块

​		在浏览器中输入 http://localhost/consumer/payment/10，多次刷新查看端口号的变化，端口号已经变成随机出现的了。

### 5.7 手写轮询算法

#### 5.7.1 去除 `LoadBalance`

​		修改 `cloud-consumer-order80` 接口的配置类中的 `@LoadBalanced` 注解，同时去掉启动类上面的 `@RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration = MySelfRule.class)` 注解

```java
package com.lyl.springcloud.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author 罗亚龙
 * @date 2022/1/21 14:26
 */
@Configuration
public class ApplicationContextConfig {

    @Bean
    //@LoadBalanced  
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
```

**启动类**

```java
package com.lyl.springcloud;

import com.lyl.myrule.MySelfRule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;

/**
 * @author 罗亚龙
 * @date 2022/1/21 14:04
 */
@SpringBootApplication
@EnableEurekaClient
//@RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration = MySelfRule.class)
public class OrderMain80 {

    public static void main(String[] args) {
        SpringApplication.run(OrderMain80.class,args);
    }

}
```

#### 5.7.2 定义接口及实现类

1. 定义一个 `LoadBalance` 的接口

   ```java
   package com.lyl.springcloud.lb;
   
   import org.springframework.cloud.client.ServiceInstance;
   
   import java.util.List;
   
   public interface LoadBalance {
       
       ServiceInstance instances(List <ServiceInstance> serviceInstanceList);
   }
   ```

2. 实现 `LoadBalance` 接口

   ```java
   package com.lyl.springcloud.lb;
   
   import org.springframework.cloud.client.ServiceInstance;
   import org.springframework.stereotype.Component;
   
   import java.util.List;
   import java.util.concurrent.atomic.AtomicInteger;
   
   @Component
   public class MyLb implements LoadBalance {
   
       private AtomicInteger atomicInteger = new AtomicInteger(0);
   
       public final int getAndIncrement(){
           int current;
           int next;
           do {
               current = atomicInteger.get();
               next = current >= Integer.MAX_VALUE ? 0 : current + 1;
           }while (!atomicInteger.compareAndSet(current, next));
           System.out.println("第几次访问，次数：" + next);
           return next;
       }
   
       @Override
       public ServiceInstance instances(List<ServiceInstance> serviceInstanceList) {
           int index = getAndIncrement() % serviceInstanceList.size();
           return serviceInstanceList.get(index);
       }
   }
   ```

#### 5.7.3 改造控制器

​		改造 提供者的两个模块的控制器，都添加上一个接口

```java
@GetMapping("/lb")
public Result getServerPort(){
    return Result.success(serverPort);
}
```

​		改造 `cloud-consumer-order80` 模块的控制器

```java
@Resource
private LoadBalance lb;
@Resource
private DiscoveryClient discoveryClient;

@GetMapping("/payment/lb")
public Result getServerPort(){
    List<ServiceInstance> instanceList = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
    ServiceInstance instance = lb.instances(instanceList);
    URI uri = instance.getUri();
    return restTemplate.getForObject(uri + "/payment/lb",Result.class);
}
```

#### 5.7.4 测试

​		使用 http://localhost/consumer/payment/lb 来测试，可以看到我们的负载均衡实现了。

## 6. OpenFeign服务接口调用

### 6.1 简述

​		`Feign` 是一个声明式的 `Web` 服务客户端，让编写 `Web` 服务客户端变得非常容易，只需要创建一个接口并在接口上面添加注解即可。

### 6.2 `Feign` 和 `OpenFeign` 区别

![image-20220126211954438](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-26/ea5972b6158d201e2b755061eee79b6f.jpeg)

### 6.3 使用步骤

注解：`@FeignCLient`  

`OpenFeign` 在消费端使用

#### 6.3.1 创建新模块

​		创建一个名为 `cloud-consumer-feign-order80` 的模块

#### 6.3.2 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-consumer-feign-order80</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>

        <!--解决 feign get请求被重置为post请求-->
        <dependency>
            <groupId>io.github.openfeign</groupId>
            <artifactId>feign-httpclient</artifactId>
        </dependency>

        <!--openFeign-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!--eureka-client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>com.lyl</groupId>
            <artifactId>cloud-api-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>


</project>
```

#### 6.3.3 改 `yml`

```yml
server:
  port: 80

eureka:
  client:
    #表示是否向eureka注册自己
    register-with-eureka: false
    service-url:
      #注册中心地址
      #      defaultZone: http://localhost:7001/eureka  #单机版
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka

feign:
  httpclient:
    enabled: true
```

#### 6.3.4 主启动

​		主启动类上需要添加 `@EnableFeignClients` 注解

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrderFeignMain80 {
    public static void main(String[] args) {
        SpringApplication.run(OrderFeignMain80.class,args);
    }
}
```

#### 6.3.5 业务类

封装服务接口，使用注解 + 服务接口 来调用提供者提供的服务。

```java
package com.lyl.springcloud.service;

import com.lyl.springcloud.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 通过 注解 @FeignClient(服务名)
 *      和 提供者的服务接口，可以直接调用提供者提供的服务。
 */

@Component
@FeignClient(value = "CLOUD-PAYMENT-SERVICE")
public interface ProviderFeignService {

    /**
     * 通过id查询支付数据
     * @param id id
     * @return 支付数据
     */
    @GetMapping("/payment/{id:\\d+}")
    public Result getById(@PathVariable(value = "id")  Long id);

    @GetMapping("/payment/lb")
    public Result getServerPort();
}
```

**控制器调用**

```java
package com.lyl.springcloud.controller;

import cn.hutool.log.Log;
import com.lyl.springcloud.entity.Result;
import com.lyl.springcloud.service.ProviderFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/consumer")
public class OrderController {

    @Resource
    private ProviderFeignService feignService;

    /**
     * 通过id查询支付数据
     * @param id id
     * @return 支付数据
     */
    @GetMapping("/payment/{id:\\d+}")
    public Result getById(@PathVariable("id") Long id){
        log.info("查询的id = " + id);
        return feignService.getById(id);
    }

    @GetMapping("/payment/lb")
    public Result getServerPort(){
        return feignService.getServerPort();
    }
}
```

#### 6.3.6 测试

​		使用 http://localhost/consumer/payment/10 来测试使用 `openfeign` 来调用服务。



#### 6.3.7 报错处理

​		消费者模块 使用 `open feign` 调用其他 提供者的服务的时候，使用 `get` 方法调动其他服务的 `get` 方法会报 `405 ` 错误。这是因为，feign底层使用的是`httpurlconnection`的工具，而进行传递body的时候，会调用`getOutputStrean`方法，里边会判断是否是get请求，如果是get请求，则`自动转为post`请求，而远方的服务只能够支持`get`请求，因此会报错。

>  解决方案，使用 `feign-httpclient` 替换掉 `feign` 默认的 `httpurlconnection` 

在 `pom` 文件中添加依赖

```xml
<!--解决 feign get请求被重置为post请求-->
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-httpclient</artifactId>
</dependency>
```

修改 `yml` 文件

```yml
feign:
  httpclient:
    enabled: true  #默认为true,可以不用处理
```

此时，客户端就可以调用服务端的 `get` 方法了。且客户端和服务端的代码可以保持高度一致。

### 6.4 `OpenFeign` 超时控制

​		业务场景：服务提供方提供的接口，需要三秒钟才可以调用完成，属于正常的耗时方法。但是对于消费者来说，可能就会出现超时报错的情况。

#### 6.4.1 搭建测试环境

1. 在 `cloud-provider-payment` 模块中添加 耗时三秒的接口

   ```java
   /**
    * 测试feign的超时
    * @return 端口号
    */
   @GetMapping("/timeout")
   public Result timeOut(){
       try {
           TimeUnit.SECONDS.sleep(3);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
       return Result.success(serverPort);
   }
   ```

2. 在 `cloud-consumer-feign-order80` 模块中调用 服务端提供的 长耗时接口

   ​		**接口**：

   ```java
   /**
    * 服务端提供的超时接口
    * @return 端口号
    */
   @GetMapping("/timeout")
   public Result timeOut();
   ```

   ​		**控制器**

   ```java
   @GetMapping("/timeout")
   public Result timeout(){
       return feignService.timeOut();
   }
   ```

3. 调用服务端提供的接口

   ​		调用接口 ： http://localhost:8001/payment/timeout  ，大概三秒钟才可以显示出结果

   ![image-20220127133228789](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-27/031fd0e2e65b1360b9f8bd87a2b9c9a6.jpeg)

4. 客户端调用服务端提供的接口

   ​		调用客户端接口： http://localhost/consumer/payment/timeout ， 会直接报错。

   ![image-20220127133552756](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-27/ac2144ca770e12ea74da660553bce698.jpeg)

#### 6.4.2 超时配置

​		默认 `Feign` 客户端只会等待一秒钟，但是服务端处理需要超过一秒钟，导致 `feign` 客户端不想等待了，直接返回报错。为了避免这种情况，有时候我们还需要设置 `feign` 的超时控制。



==以下配置不可用==

---

​		~~在 `yml` 中开启配置~~

> ~~因为 `feign` 中集成了 `ribbon` , 所以 `feign` 的超时控制由 `ribbon` 来完成。~~

**过时配置：不可用**

```yml
# 设置 feign客户端的超时时间
ribbon:
  # 指的是链接建立的所用的时间，适用于网络情况正常的情况下，两端连接所用的时间
  ReadTimeOut: 5000
  # 指的是建立连接后从服务器读取到可用资源所用的时间
  ConnectTimeOut: 5000
```

---





==**新配置**== ： 使用 `feign` 自己的配置  

🎄参考链接： [Spring Cloud OpenFeign 超时与重试 - SegmentFault 思否](https://segmentfault.com/a/1190000041262968)

```yml
feign:
  client:
    config:
      default:
        #连接超时
        connectTimeout: 5000
        #读取超时
        readTimeout: 5000
```

 需要注意以下几点：

- 连接超时 (`connectTimeout`) 和 读取超时 (readTimeout) 同时配置时，才会生效。
- 超时单位为毫秒。
- 可根据服务名称单独定义超时。

比如， `provider-get` 服务提供的是查询接口，超时时间可以设置短一些：

```yaml
feign:
  client:
    config:
      provider-get:
        connectTimeout: 1000
        readTimeout: 6000
```



---



修改完成之后，重新调用 http://localhost/consumer/payment/timeout ，可以看到已经可以拿到结果了。

![image-20220127140117098](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-27/db8fe7cb9ae5781ac186b0f88861ecee.jpeg)

### 6.5 日志打印

​		`feign` 提供了日志打印功能，我们可以通过配置来调整日志打印级别，从而了解 `feign` 中的 `http` 请求的细节。说白了，就是 对 `feign` 接口的调用情况进行监控和输出。

#### 6.5.1 日志级别

<img src="https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-27/6918fb3dafd4f756c083c9df79b79ed3.jpeg" alt="image-20220127140418677" style="zoom:80%;" />

#### 6.5.2 配置Bean

​		在 `cloud-consumer-feign-order80` 中新建一个 `feign` 日志 `bean`

```java
package com.lyl.springcloud.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 罗亚龙
 * @date 2022/1/27 14:08
 */
@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel(){
        //配置feign的日志级别
        return Logger.Level.FULL;
    }
}
```

​		修改 `yml`

```yml
logging:
  level:
    # feign 日志以什么级别监控那个接口
    com.lyl.springcloud.service.ProviderFeignService: debug
```

#### 6.5.3 测试

​		调用 接口： http://localhost/consumer/payment/10

**后台日志**

![image-20220127141432223](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-27/e2f90829d6005d91a997ec93ca32a280.jpeg)

## 7. Hystrix 服务降级 ❌

### 7.1 简述

​		`Hystrix`是一个用于处理分布式系统的延迟和容错的开源库， 在分布式系统里,许多依赖不可避免的会调用失败,比如超时、异常等，`Hystrix`能够保证在一个依赖出问题的情况下， 不会导致整体服务失败，避免级联故障，以提高分布式系统的弹性。

​		**断路器** 本身是一种开关装置, 当某个服务单元发生故障之后，通过断路器的故障监控(类似熔断保险丝)，向调用方返回-个符合
预期的、可处理的备选响应(FallBack) ，而不是长时间的等待或者抛出调用方无法处理的异常，这样就保证了服务调用方的线程不会
被长时间、不必要地占用，从而避免了故障在分布式系统中的蔓延，乃至雪崩。

**能干吗**

- 服务降级
- 服务熔断
- 接近实时的监控
- ……
- 

简介：[Hystrix介绍 - 废物大师兄 - 博客园 (cnblogs.com)](https://www.cnblogs.com/cjsblog/p/9391819.html)

**官网**：[Netflix/Hystrix](https://github.com/Netflix/Hystrix)

> **Hystrix** 官宣：==停更维护==

### 7.2 三个重要的概念

#### 7.2.1 服务降级

​		当调用服务的时候，由于种种原因不能提供服务，返回一个可处理的备选响应。类似：服务器忙，请稍后再试，并让客户端返回一个友好的提示

**那些情况会触发降级：**

- 程序运行异常
- 超时
- 服务熔断触发服务降级
- 线程池、信号量打满也会导致服务降级

#### 7.2.2 服务熔断

​		类比于保险丝达到最大服务访问后，直接拒绝访问，拉闸限电，然后调用服务降级的方法并返回友好提示。

​		可以说就是 **保险丝**  ： **服务降级  ->   服务熔断   ->  恢复链路调用**

#### 7.2.3 服务限流

​		秒杀高并发等操作，严禁一窝蜂的过来拥挤，排队请求，一秒 N 个，有序进行

### 7.3 Hystrix支付微服务构建

#### 7.3.1 新建项目

​		新创建一个名为 `cloud-provider-hystrix-payment8001` 模块

#### 7.3.2 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-provider-hystrix-payment8001</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>

        <!--eureka-client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!--公共模块-->
        <dependency>
            <groupId>com.lyl</groupId>
            <artifactId>cloud-api-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>
```

#### 7.3.3 改 `yml`

```yml
server:
  port: 8001

spring:
  application:
    name: cloud-payment-hystrix-service

eureka:
  client:
    #表示是否向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
      #      defaultZone: http://localhost:7001/eureka  #单机版
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  #集群版
  instance:
    #实例id,显示在Eureka注册中心的名字，默认是ip地址+端口号
    instance-id: PaymentHystrix8001
    #访问路径是否显示ip地址
    prefer-ip-address: true
```

#### 7.3.4 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author 罗亚龙
 * @date 2022/1/27 15:02
 */
@SpringBootApplication
@EnableEurekaClient
public class PaymentHystrixMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentHystrixMain8001.class,args);
    }
}
```

#### 7.3.5 业务类

**接口**

```java
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
```

**实现类**

```java
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
```

**控制器**

```java
package com.lyl.springcloud.controller;

import com.lyl.springcloud.entity.Result;
import com.lyl.springcloud.service.HystrixService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author 罗亚龙
 * @date 2022/1/27 15:03
 */
@RestController
public class PaymentController {

    @Resource
    private HystrixService hystrixService;

    /**
     * 正常的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/payment/hystrix/ok/{id}")
    public Result hystrix_OK(@PathVariable("id") Integer id){
        return hystrixService.hystrix_OK(id);
    }

    /**
     * 超时的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/payment/hystrix/timeout/{id}")
    public Result hystrix_Timeout(@PathVariable("id") Integer id){
        return hystrixService.hystrix_Timeout(id);
    }

}
```

#### 7.3.6 测试

**启动项目**

- 启动 `cloud-eureka-server7001` 和 `cloud-eureka-server7002` 注册中心
- 启动 `cloud-provider-hystrix-payment8001` 支付微服务

查看 `Eureka` ，支付微服务已经注册进 `Eureka`

<img src="https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-27/7cc7d6fea89c8d772258602641c632ff.jpeg" alt="image-20220127153650196" style="zoom: 67%;" />



**测试**

- 调用正常服务： http://localhost:8001/payment/hystrix/ok/10

  ![image-20220127153846088](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-27/78337aebc831c19a846ad9c0f279b7a8.jpeg)

  

- 调用超时服务：http://localhost:8001/payment/hystrix/timeout/10

  ![image-20220127153830435](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-27/8f256b8457117e7b00f5538c1c2d4b0f.jpeg)

  ​	可以看到两个接口都可以正常返回数据

  #### 7.3.7 压力测试
  
  ​		使用 `JMeter` 进行并发压力测试，20000个线程同时访问 http://localhost:8001/payment/hystrix/timeout/10 接口，会导致 http://localhost:8001/payment/hystrix/ok/10 接口（没有加延迟的接口）响应速度变慢。
  
  
  
  **JMeter压测结论：	**上面的服务 提供者8001自己测试，假如此时外部的消费者80 也来访问，那消费者只能干等，最终导致消费端 80 不满意，服务端8001直接被拖死。

###  7.4 订单微服务调用支付服务出现卡顿

​		新建一个订单微服务，调用支付微服务。

#### 7.4.1 新建模块

​		新建一个名为 `cloud-provider-feign-hystrix-order80` 的订单模块。

#### 7.4.2 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-provider-feign-hystrix-order80</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>


        <!--解决 feign get请求被重置为post请求-->
        <dependency>
            <groupId>io.github.openfeign</groupId>
            <artifactId>feign-httpclient</artifactId>
        </dependency>

        <!--openFeign-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>

        <!--eureka-client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!--公共模块-->
        <dependency>
            <groupId>com.lyl</groupId>
            <artifactId>cloud-api-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>
```

#### 7.4.3 改 `yml`

```yml
server:
  port: 80

spring:
  application:
    name: cloud-provider-feign-hystrix-service

eureka:
  client:
    #表示是否向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
      #      defaultZone: http://localhost:7001/eureka  #单机版
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  #集群版
  instance:
    #实例id,显示在Eureka注册中心的名字，默认是ip地址+端口号
    instance-id: OrderFeignHystrix80
    #访问路径是否显示ip地址
    prefer-ip-address: true

# 为了解决调用正常长耗时接口超时
feign:
  client:
    config:
      default:
        # 连接时长 2秒
        connectTimeout: 2000
        # 读取时长 5秒
        readTimeout: 5000
```

#### 7.4.4 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
public class OrderFeignHystrixMain80 {

    public static void main(String[] args) {
        SpringApplication.run(OrderFeignHystrixMain80.class,args);
    }
}
```

#### 7.4.5 业务类

**服务接口**

```java
package com.lyl.springcloud.service;

import com.lyl.springcloud.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@FeignClient("CLOUD-PAYMENT-HYSTRIX-SERVICE")
public interface PaymentHystrixService {

    /**
     * 正常的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/payment/hystrix/ok/{id}")
    public Result hystrix_OK(@PathVariable("id") Integer id);

    /**
     * 超时的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/payment/hystrix/timeout/{id}")
    public Result hystrix_Timeout(@PathVariable("id") Integer id);

}
```

**控制器**

```java
package com.lyl.springcloud.controller;

import com.lyl.springcloud.entity.Result;
import com.lyl.springcloud.service.PaymentHystrixService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class OrderHystrixController {

    @Resource
    private PaymentHystrixService paymentHystrixService;

    /**
     * 正常的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/consumer/payment/hystrix/ok/{id}")
    public Result hystrix_OK(@PathVariable("id") Integer id){
        return paymentHystrixService.hystrix_OK(id);
    }

    /**
     * 超时的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/consumer/payment/hystrix/timeout/{id}")
    public Result hystrix_Timeout(@PathVariable("id") Integer id){
        return paymentHystrixService.hystrix_Timeout(id);
    }
}
```

#### 7.4.6 正常测试

1. 调用接口： http://localhost/consumer/payment/hystrix/ok/10 

   ![image-20220127215028967](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-27/d6c66eb6326a689763cba47db901733f.jpeg)

2. 调用超时接口： http://localhost/consumer/payment/hystrix/timeout/10

   ![image-20220127215053642](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-27/f47b7aab168419a64f4784c4dbe6520e.jpeg)

#### 7.4.7 压力测试

​		我们使用 `JMeter` 用 20000 个线程调用支付服务的超时接口，同时使用 20000 个线程调用订单的超时接口（订单的超时接口调用了支付服务的超时接口）。此时，调用支付接口的正常接口（没有加延迟的接口），接口会卡顿。同时订单服务调用的正常接口的耗时也会增加。

**原因分析：**

​		高并发的情况下，大流量涌入支付接口的超时接口（添加延迟的接口）， `tomcat` 线程池里的工作线程逐渐被挤占完毕，导致其他接口被困死了，造成了接口的卡顿。此时，订单服务调用支付接口，因为支付服务接口的卡顿，造成客户端的响应变得缓慢。

### 7.5 解决方案

1. 超时导致的服务器卡顿     ==>  服务降级
2. 服务器宕机  ==> 出错要有兜底



**解决方案*

- ![image-20220127220559384](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-27/4687eb1eddb9b69cc0ce6e2b9e5ffdb7.jpeg)

- 对方的服务（8001）超时了，调用者（80）不能一直卡死等待，必须有服务降级
- 对方的服务（8001）宕机了，调用者（80）不能一直卡死等待，必须有服务降级
- 对方的服务（8001）OK，调用者（80）自己出故障或有自我要求（自己的等待时间小于提供者），自己处理降级

#### 7.5.1 支付侧服务降级

​		支付侧可以进行服务降级： 设置自身调用超过时间的峰值，峰值内可以正常运行，超过了则需要有兜底的方法处理，做服务降级 `fallback`

1. 修改超时方法（timeout）

   ​		通过注解，设置如果接口调用超过 2秒 之后，调用 fallback方法 ：`hystrix_TimeoutHandler`

   ```java
   @Override
   @HystrixCommand(fallbackMethod = "hystrix_TimeoutHandler", commandProperties = {
           @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "2000")
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
   ```

2. 主启动类上添加注解 `@EnableCircuitBreaker`

   ```java
   package com.lyl.springcloud;
   
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
   import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
   
   /**
    * @author 罗亚龙
    * @date 2022/1/27 15:02
    */
   @SpringBootApplication
   @EnableEurekaClient
   @EnableCircuitBreaker  //启动断路器
   public class PaymentHystrixMain8001 {
       public static void main(String[] args) {
           SpringApplication.run(PaymentHystrixMain8001.class,args);
       }
   }
   ```

3. 测试，超时会返回友好的提示，同时对于接口异常（程序异常）也会返回 `fallback` 方法

   ![image-20220127223820625](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-27/475f5455931d6b375b66c2b85f0c4efc.jpeg)

#### 7.5.2 消费侧服务降级

​		消费侧也可以使用 `hystrix` 进行服务降级。

**修改 `cloud-consumer-feign-hystrix-order80` 模块**

1. 改 `pom`

   ​		查看 `pom` 文件，确保 `pom` 文件中有以下依赖

   ```xml
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
   </dependency>
   ```

2. 改 `yml`

   ```yml
   feign:
     #开启断路器
     hystrix:
       enabled: true
   ```

3. 主启动

   ​		启动类上面添加  `@EnableHystrix` 注解

   ```java
   package com.lyl.springcloud;
   
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
   import org.springframework.cloud.netflix.hystrix.EnableHystrix;
   import org.springframework.cloud.openfeign.EnableFeignClients;
   
   /**
    * @author luoyalong
    */
   @SpringBootApplication
   @EnableFeignClients
   @EnableEurekaClient
   //开启Hystrix
   @EnableHystrix
   public class OrderFeignHystrixMain80 {
   
       public static void main(String[] args) {
           SpringApplication.run(OrderFeignHystrixMain80.class,args);
       }
   }
   ```

4. 业务类

   ​		业务类上面添加 `fallback` 方法

   ```java
   /**
    * 超时的方法
    * @param id id
    * @return Result
    */
   @GetMapping("/consumer/payment/hystrix/timeout/{id}")
   @HystrixCommand(fallbackMethod = "hystrix_TimeoutHandler", commandProperties = {
           @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1500")
   })
   public Result hystrix_Timeout(@PathVariable("id") Integer id){
       return paymentHystrixService.hystrix_Timeout(id);
   }
   
   /**
    * 服务降级（兜底的方法）
    * @param id
    * @return
    */
   public Result hystrix_TimeoutHandler(Integer id){
       String str = "客户端: hystrix_Timeout  [" + Thread.currentThread().getName() + "]  参数：" + id + "  80 服务器繁忙或者接口异常 ";
       log.info(str);
       return Result.success("客户端 80 服务器繁忙或者接口异常", str);
   }
   ```

#### 7.5.3 存在的问题

		1. 虽然目前完成了服务的降级，但是每一个需要降级的接口都需要一个 兜底的方法，造成了代码的大量冗余。同时，如果100 个接口需要服务降级，就需要写 100 个兜底的方法。（可以使用默认 fallback方法）
		2. 接口和兜底的方法拥挤在一起，没有分开。

#### 7.5.4 全局 `fallback` 方法

1. 编写一个全局 `fallback` 方法

   ```java
   /**
    * 全局超时方法
    * @return
    */
   public Result global_TimeOut(){
       String str = "global fallback方法 80 服务器繁忙或者接口异常 ";
       log.info(str);
       return Result.fail(str);
   }
   ```

2. 控制器类上面添加注解：`@DefaultProperties(defaultFallback = "global_TimeOut")`

3. 业务类上面添加 `@HystrixCommand` 注解

   ```java
   /**
    * 超时的方法
    * @param id id
    * @return Result
    */
   @GetMapping("/consumer/payment/hystrix/timeout/{id}")
   /*    @HystrixCommand(fallbackMethod = "hystrix_TimeoutHandler", commandProperties = {
           @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1500")
   })*/
   @HystrixCommand
   public Result hystrix_Timeout(@PathVariable("id") Integer id){
       return paymentHystrixService.hystrix_Timeout(id);
   }
   ```

   **注意** 以下代码可以自定义 `fallback` 方法

   ```java
   @HystrixCommand(fallbackMethod = "hystrix_TimeoutHandler", commandProperties = {
               @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1500")
       })
   ```

   

**完成代码**

```java
package com.lyl.springcloud.controller;

import com.lyl.springcloud.entity.Result;
import com.lyl.springcloud.service.PaymentHystrixService;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author luoyalong
 */
@Slf4j
@RestController
@DefaultProperties(defaultFallback = "global_TimeOut")
public class OrderHystrixController {

    @Resource
    private PaymentHystrixService paymentHystrixService;

    /**
     * 正常的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/consumer/payment/hystrix/ok/{id}")
    public Result hystrix_OK(@PathVariable("id") Integer id){
        return paymentHystrixService.hystrix_OK(id);
    }

    /**
     * 超时的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/consumer/payment/hystrix/timeout/{id}")
/*    @HystrixCommand(fallbackMethod = "hystrix_TimeoutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1500")
    })*/
    @HystrixCommand
    public Result hystrix_Timeout(@PathVariable("id") Integer id){
        return paymentHystrixService.hystrix_Timeout(id);
    }

    /**
     * 服务降级（兜底的方法）
     * @param id
     * @return
     */
    public Result hystrix_TimeoutHandler(Integer id){
        String str = "客户端: hystrix_Timeout  [" + Thread.currentThread().getName() + "]  参数：" + id + "  80 服务器繁忙或者接口异常 ";
        log.info(str);
        return Result.fail(str);
    }

    /**
     * 全局超时方法
     * @return
     */
    public Result global_TimeOut(){
        String str = "global fallback方法 80 服务器繁忙或者接口异常 ";
        log.info(str);
        return Result.fail(str);
    }
}
```

#### 7.5.5 统配服务降级 `feign fallback`

​		服务降级，客户端去调用服务端，碰上服务端宕机或关闭。

​		本次处理实在 客户端80 实现完成的，与服务端8001 没有关系，只需要为 `feign` 客户端定义的接口添加一个服务降级的实现类即可实现解耦。

​		未来我们要面临的异常：运行时异常，超时异常，宕机

​		实现 `feign` 客户端 `PaymentHystrixService` ，添加实现类 `PaymentHystrixServiceImpl` 即可为每个方法提供异常处理

**新建实现类**

```java
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
```

同时，在 `feign` 客户端的接口上需要指定 出现异常的时候 `fallback`  可以处理的类。

```
@FeignClient(value = "CLOUD-PAYMENT-HYSTRIX-SERVICE", fallback = PaymentHystrixServiceImpl.class)
```

**完整代码**

```java
package com.lyl.springcloud.service;

import com.lyl.springcloud.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author luoyalong
 */
@Service
@FeignClient(value = "CLOUD-PAYMENT-HYSTRIX-SERVICE", fallback = PaymentHystrixServiceImpl.class)
public interface PaymentHystrixService {

    /**
     * 正常的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/payment/hystrix/ok/{id}")
    public Result hystrix_OK(@PathVariable("id") Integer id);

    /**
     * 超时的方法
     * @param id id
     * @return Result
     */
    @GetMapping("/payment/hystrix/timeout/{id}")
    public Result hystrix_Timeout(@PathVariable("id") Integer id);

}
```



**测试**

​		当我们调用 `hystrix_OK` 的方法的时候，正常调用。

![image-20220128163426812](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-28/c83a65b06c1f02cd61d84de2d23fa420.jpeg)

**手动关闭 8001 服务端**

![image-20220128163504214](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-28/89d9f9fbee121933049fd799ffc9839b.jpeg)

### 7.6 服务熔断

#### 7.6.1 熔断机制概述

​		熔断机制是应对雪崩效应的一种微服务链路裱糊机制。当扇出链路的某个微服务出错不可用或者响应时间太长时，会进行服务的降级，进而熔断该节点微服务的调用，快速返回错误的响应信息。**当检测到该节点微服务调用响应正常后，恢复正常链路**

​		在SpringCloud框架中，熔断机制通过 `Hystrix` 实现。`Hystrix` 会监控微服务间调用的状况，当失败的调用到一定阈值，缺省是5秒内20次调用失败，就会启动熔断机制。熔断机制的注解是 `@HystrixCommand`

#### 7.6.2 修改 `cloud-provider-hystrix-payment8001` 模块

​		修改 `cloud-provider-hystrix-payment8001` 模块，编写一个新接口，测试断路器方法。

1. 添加一个 `service`接口

   ```java
   /**
    * 测试短路的方法
    * @param id id
    * @return
    */
   Result paymentCircuitBreaker(Integer id);
   ```

2. 实现 `service` 接口

   ```java
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
   ```

3. 在控制器内调用 新建的接口

   ```java
   /**
    * 服务熔断方法
    * @param id
    * @return
    */
   @GetMapping("/payment/circuit/{id}")
   public Result paymentCircuitBreaker(@PathVariable("id") Integer id){
       return hystrixService.paymentCircuitBreaker(id);
   }
   ```

4. 测试

   1. 调用正确接口：http://localhost:8001/payment/circuit/10

   2. 调用触发 `fallback` 接口：http://localhost:8001/payment/circuit/-10

   3. 多次调用错误的接口，之后再次调用正确的接口，正确的接口刚开始可能并不会返回正确的结果，需要等一会才会返回正确的结果。

   4. 具体的原因是：上述接口做了服务熔断。在10秒内10次调用中出现了百分之六十的错误率，则会触发熔断。之后偶尔调用正确的结果会返回熔断的方法是因为，断路器此时应该处于 半开的状态。

   5. 断路器什么时候起作用

      ![image-20220130191757347](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-30/a4b02caa3d1a604120442e0e3bb49902.jpeg)

   6. 断路器开启或者关闭的条件

      ![image-20220130192113717](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-30/237624f7a116ed07f094c6be3dd2a23e.jpeg)

### 7.7 `Hystrix` 图形监控

#### 7.7.1 简介

​		除了隔离依赖服务的调用之外，`Hystrix` 还提供了准实时的调用监控（Hystrix dashboard），Hystrix会持续的记录所有通过 `Hystrix` 发起的请求的执行信息，并以统计报表和图形的形式展示给用户，包括每秒执行多少请求多少成功，多少失败等。 `Netflix` 通过 `Hystrix-metrics-event-stream` 项目实现了对以上指标的监控。`SpringCloud` 也提供了 `Hystrix Dashboard` 的整合，对监控内容转化成可视化界面。 

#### 7.7.2 新建项目

​		新建一个名为 `cloud-consumer-hystrix-dashboard9001` 的项目

#### 7.7.3 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-consumer-hystrix-dashboard9001</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-netflix-hystrix-dashboard</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
        </dependency>
    </dependencies>

</project>
```

#### 7.7.4 改 `yml`

```yml
server:
  port: 9001
```

#### 7.7.5 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

@SpringBootApplication
@EnableHystrixDashboard
public class HystrixDashboardMain9001 {
    public static void main(String[] args) {
        SpringApplication.run(HystrixDashboardMain9001.class,args);
    }
}
```

#### 7.7.6 完善监控信息

​		==所有的 Provider 微服务提供类（8001/8002/8003）都需要监控依赖配置==

`actuator` 监控信息的完善

```xml
 <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

在 `服务提供者` 模块修改

 1. 配置 `Hystrix` 指定监控路径

     **`注意：`** 新版本 Hystrix **`需要在主启动类中指定监控路径`**，如果没有此项操作，在项目启动后，Hystrix Dashboard 会报: **`Unable to connect to Command Metric Stream`** 这样一个错误。配置内容如下：

    ```java
    /**
     * 此配置为了服务监控而配置，与服务容错本身无关，
     * SpringCloud升级后的坑，ServletRegistrationBean
     * 因为SpringBoot的默认路径不是"/hystrix.stream",
     * 只要在自己的项目里配置上下面的Servlet就可以了。
     * @return
     */
    @Bean
    public ServletRegistrationBean getServlet(){
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
    ```

    

#### 7.7.7 测试

​		依次启动项目：`provider8001`、`provider8002` 、`eureka7001`、`eureka7002` 和 `Dashboard9001` 。

然后，在浏览器中输入：http://localhost:9001/hystrix，可以在浏览器中看到以下图片。

![image-20220130200930579](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-30/865d3f79829a05ee9a93db7a5ce75caa.jpeg)

编写以下配置，可以看到以下信息：

http://localhost:8001/hystrix.stream

![image-20220130221036787](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-30/58eba0da2ac41214e616770d8d850593.jpeg)

点击 **Monitor Stream**， 注意，第一次可能显示 `loading` ，需要调用一个启动熔断器的接口，就会出现监控

![image-20220130221150351](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-30/ec6629b39e009270dd00427915261477.jpeg)
## 8. Gateway网关

​		[官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)

### 8.1 简介

<img src="https://spring.io/images/cloud-diagram-dark-b902fd07e60945a9a8930ca01f86bdf3.svg" alt="图片" style="zoom: 50%;" />

​		‎这个项目提供了一个库，用于在`Spring WebFlux`之上构建API网关。`Spring Cloud Gateway`旨在提供一种简单而有效的方法来路由到API，并为它们提供跨领域的关注点，例如：安全性，监控/指标和弹性。‎

`Spring Cloud Gateway` 是 `Spring Cloud` 的一个全新项目，该项目是基于 Spring 5.0，Spring Boot 2.0 和 Project Reactor 等技术开发的网关，它旨在为微服务架构提供一种简单有效的统一的 API 路由管理方式。

`Spring Cloud Gateway` 作为 `Spring Cloud` 生态系统中的网关，目标是替代 Netflix Zuul，其不仅提供统一的路由方式，并且基于 Filter 链的方式提供了网关基本的功能，例如：安全，监控/指标，和限流。

**相关概念:**

- **Route（路由）**：这是网关的基本构建块。它由一个 ID，一个目标 URI，一组断言和一组过滤器定义。如果断言为真，则路由匹配。

- **Predicate（断言）**：这是一个 Java 8 的 `Predicate`。输入类型是一个 `ServerWebExchange`。我们可以使用它来匹配来自 HTTP 请求的任何内容，例如 `headers` 或参数。

- **Filter（过滤器）**：这是`org.springframework.cloud.gateway.filter.GatewayFilter`的实例，我们可以使用它修改请求和响应。

**工作流程：**

<img src="https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-08/356653d0c87bf86baebaf2e6a2932d91.jpeg" alt="image-20220208112013838" style="zoom:67%;" />

​		客户端向 `Spring Cloud Gateway` 发出请求。如果 `Gateway Handler Mapping` 中找到与请求相匹配的路由，将其发送到 `Gateway Web Handler`。Handler 再通过指定的过滤器链来将请求发送到我们实际的服务执行业务逻辑，然后返回。

过滤器之间用虚线分开是因为过滤器可能会在发送代理请求之前（“pre”）或之后（“post”）执行业务逻辑。

**Spring Cloud Gateway 的特征：**

- 基于 Spring Framework 5，Project Reactor 和 Spring Boot 2.0

- 动态路由

- Predicates 和 Filters 作用于特定路由

- 集成 Hystrix 断路器

- 集成 Spring Cloud DiscoveryClient

- 易于编写的 Predicates 和 Filters

- 限流

- 路径重写

### 8.2 三大核心概念

#### 8.2.1 Router（路由）

​		路由是构建网关的基本模块，它是由 `ID`，目标 `URI` ，一系列的断言和过滤器组成，如果断言为 `true` 则匹配该路由

#### 8.2.2 Predicate（断言） 

​		参考的是 `Java8` 的 `java.util.function.Predicate`

​		开发人员可以匹配 `HTTP` 请求中的所有内容（例如请求头或者请求参数），如果请求与断言相匹配则进行路由

#### 8.2.3 Filter（过滤）

​		指的是 `Spring` 框架中 `GateWayFilter` 的实例，使用过滤器，可以在请i去被路由前或者之后对请求进行修改。 

### 8.3 工作流程

<img src="https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-08/356653d0c87bf86baebaf2e6a2932d91.jpeg" alt="image-20220208112013838" style="zoom:67%;" />

​		客户端向`Spring Cloud Gateway`发出请求。如果网关处理程序映射确定请求与路由匹配，则会将其发送到网关 Web 处理程序。此处理程序通过特定于请求的筛选器链运行请求。筛选器除以虚线的原因是筛选器可以在发送代理请求之前和之后运行逻辑。执行所有"预"筛选器逻辑。然后发出代理请求。发出代理请求后，将运行"post"筛选器逻辑。

### 8.4 搭建Gateway

#### 8.4.1 新建模块

​		新建一个名为 `cloud-gateway-service9527` 的模块

#### 8.4.2 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-gateway-service9527</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <!--eureka-client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
    </dependencies>

</project>
```

#### 8.4.2 改 `yml`

```yml
server:
  port: 9527

spring:
  application:
    name: cloud-gateway-service
  cloud:
    gateway:
      routes:  #路由
        - id: payment_route  #路由的id,没有固定规则但要求唯一，建议配合服务名
          uri: http://localhost:8001  #匹配提供服务的路由地址
          predicates:
            - Path=/payment/{id} #断言，路径相匹配进行路由

        - id: payment_route2  #路由的id,没有固定规则但要求唯一，建议配合服务名
          uri: http://localhost:8001  #匹配提供服务的路由地址
          predicates:
            - Path=/payment/lb #断言，路径相匹配进行路由

eureka:
  client:
    #表示是否向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
      #      defaultZone: http://localhost:7001/eureka  #单机版
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  #集群版
  instance:
    #实例id,显示在Eureka注册中心的名字，默认是ip地址+端口号
    instance-id: Gateway9527
    #访问路径是否显示ip地址
    prefer-ip-address: true
```

#### 8.4.3 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author 罗亚龙
 * @date 2022/2/8 11:54
 */
@SpringBootApplication
@EnableEurekaClient
public class GatewayMain9527 {
    public static void main(String[] args) {
        SpringApplication.run(GatewayMain9527.class,args);
    }
}
```

#### 8.4.4 测试

**依次启动项目**

  		1. EurekaMain7001
  		2. EurekaMain7002
  		3. PaymentMain8001
  		4. GatewayMain9527

---

​		启动完成之后，在浏览中输入： http://eureka7001.com/7001  或者 http://eureka7002.com/7002 ， 查看服务是否已经注册进注册中心。

![image-20220208135535516](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-08/c68e719dcf5e8382d379845dfa45ab4d.jpeg)

​		之后，测试 `Payment8001` 服务的接口：http://localhost:8001/payment/10  和  http://localhost:8001/payment/lb  是否可以正常访问。然后，再次访问：http://localhost:9527/payment/10  和  http://localhost:9527/payment/lb ， 依然可以正常访问服务，证明 网关配置好了。

#### 8.4.5 注意

​		**注意 ** 路由需要在 `yml` 中配置。

```yml
spring:
  application:
    name: cloud-gateway-service
  cloud:
    gateway:
      routes:  #路由
        - id: payment_route  #路由的id,没有固定规则但要求唯一，建议配合服务名
          uri: http://localhost:8001  #匹配提供服务的路由地址
          predicates:
            - Path=/payment/{id} #断言，路径相匹配进行路由

        - id: payment_route2  #路由的id,没有固定规则但要求唯一，建议配合服务名
          uri: http://localhost:8001  #匹配提供服务的路由地址
          predicates:
            - Path=/payment/lb #断言，路径相匹配进行路由
```

### 8.5 Gateway的两种配置方式

#### 8.5.1 yml

​		第一种，使用 `yml` 来配置网关

#### 8.5.2 配置文件

​		此种方法属于硬编码

```java
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
```

### 8.6 动态路由

​		通过微服务名称来实现冬天路由。默认情况下Gateway会根据注册中心注册的服务列表，以注册中心上微服务名为路径创建 动态路由进行转发，从而实现动态路由的功能

#### 8.6.1 修改 yml

  1. 开启从注册中心动态创建路由的功能，利用微服务名进行路由

     ```yml
     spring.cloud.gateway.discovery.locator.enabled: true  #开启从注册中心动态创建路由的功能，利用微服务名进行路由
     ```

  2. 将固定的 uri `http://localhost:8001` 转换成 使用微服务名称`lb://CLOUD-PAYMENT-SERVICE`

```yml
server:
  port: 9527

spring:
  application:
    name: cloud-gateway-service
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true  #开启从注册中心动态创建路由的功能，利用微服务名进行路由
      routes:
        - id: payment_route  #路由的id,没有固定规则但要求唯一，建议配合服务名
          # uri: http://localhost:8001  #匹配提供服务的路由地址
          uri: lb://CLOUD-PAYMENT-SERVICE  #匹配后提供服务的路由地址
          predicates:
            - Path=/payment/{id} #断言，路径相匹配进行路由

        - id: payment_route2  #路由的id,没有固定规则但要求唯一，建议配合服务名
          #uri: http://localhost:8001  #匹配提供服务的路由地址
          uri: lb://CLOUD-PAYMENT-SERVICE  #匹配提供服务的路由地址
          predicates:
            - Path=/payment/lb #断言，路径相匹配进行路由

eureka:
  client:
    #表示是否向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
      #      defaultZone: http://localhost:7001/eureka  #单机版
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  #集群版
  instance:
    #实例id,显示在Eureka注册中心的名字，默认是ip地址+端口号
    instance-id: Gateway9527
    #访问路径是否显示ip地址
    prefer-ip-address: true
```

#### 8.6.2 启动服务

​		**依次启动服务**

1. EurekaMain7001
  2. EurekaMain7002
  3. PaymentMain8001
  4. PaymentMain8002
  5. GatewayMain9527

#### 8.6.3 测试

​		在浏览器中输入http://localhost:9527/payment/10  和  http://localhost:9527/payment/lb ， 查看返回的端口号，会依次变化，证明动态路由设置成功。

### 8.7 Predicate

​		`Predicate` 有多种匹配模式，在项目启动的过程中可以看到

<img src="https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-08/27d4311d33e3a3a1fa3b80b79287b07d.jpeg" alt="image-20220208150628695" style="zoom:67%;" />

在[官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gateway-request-predicates-factories)中有 11种

![image-20220208150728726](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-08/4cbff10d377d97236f53fc5c0e97447c.jpeg)

#### 8.7.1 After

​		表示在某个时间之后进行匹配路由。

```yml
predicates:
  - Path=/payment/{id} #断言，路径相匹配进行路由
  - After=2022-02-08T15:22:24.516+08:00[Asia/Shanghai]  #在某个时间点之后匹配，之前不可用
```

**时间的计算方式**

```java
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
```

#### 8.7.2 Cookie

​		`Cookie` 需要两个参数，一个是 `Cookie name` ，一个是正则表达式

​		路由规则会通过获取对应的 `Cookie Name`  值 和正则表达式去匹配，如果匹配上就会执行路由，如果没有匹配则不执行。

```yml
predicates:
  - Path=/payment/{id} #断言，路径相匹配进行路由
  #- After=2022-02-08T15:22:24.516+08:00[Asia/Shanghai]  #在某个时间点之后匹配，之前不可用
  - Cookie=username,zzyy # 请求必须携带cookie
```

使用 `curl` 命令测试

- 不带 `cookie` 测试

  ```shell
  curl http://localhost:9527/payment/10
  ```

  **执行情况**

  ![image-20220208153708009](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-08/2a23426aef138be3af4e892bfe33eb09.jpeg)

- 带 `cookie` 测试

  ```shell
  curl http://localhost:9527/payment/10 --cookie "username=zzyy"
  ```

  **执行情况**

  ![image-20220208153801566](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-08/785d8491c137a3ecf65bdd1cc1ee1aaa.jpeg)

###  8.8 Filter 

#### 8.8.1 **简介**

​		‎路由筛选器允许以某种方式修改传入的 HTTP 请求或传出的 HTTP 响应。路由筛选器的作用域为特定路由。`Spring Cloud Gateway`包括许多内置的`GatewayFilter Factory`。‎

​		可以做全局日志记录，统一网关鉴权……

**生命周期：**

-  **pre**
- **post**

**种类**

- **GatewayFilter**

  [GatewayFilters](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gatewayfilter-factories)

  

- **GlobalFilter**

  [Gateway GlobalFilters](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#global-filters)

#### 8.8.2 自定义全局过滤器

​		需要实现 连个接口： `GlobalFilter, Ordered`

​		定义一个简单的全局过滤器，实现日志监控，和过滤不带 `uname` 参数的请求。

```java
package com.lyl.springcloud.filter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author 罗亚龙
 * @date 2022/2/8 16:17
 */
@Component
@Slf4j
public class MyLogGatewayFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("**************come in MyLogGatewayFilter: " + DateUtil.now());
        String uname = exchange.getRequest().getQueryParams().getFirst("uname");
        if (StrUtil.isBlank(uname)){
            log.info("*******用户名为null，非法用户，/(ㄒoㄒ)/~~");
            exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
```

**测试**

- 调用 http://localhost:9527/payment/10

  ![image-20220208163701666](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-08/ce6755194ea88a6b62ecff6b348c5b71.jpeg)

- 调用 http://localhost:9527/payment/10?uname=zy

  正常访问

  ![image-20220208163737694](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-08/95a547040a6bdbc3a5260300f551f156.jpeg)

  控制台

  ![image-20220208163807645](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-08/d738f947532247af3eb54f949744c0d4.jpeg)

## 9. Config分布式配置中心

### 9.1 简介

​		微服务意味着要将单体应用中的业务拆分成一个个子服务，每个服务的粒度相对较小，因此系统中会出现大量的服务。由于每个服务都需要必要的配置信息才能运行，所以一套集中式的、动态的配置管理设施是必不可缺少的。

​		`SpringCloud` 提供了 `ConfigServer` 来解决这个问题，我们的每一个微服务自己带着一个 `application.yml` ，上百个配置文件的管理……  /(ㄒoㄒ)/~~

<img src="https://img-blog.csdnimg.cn/20210713141522914.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L215X2Fpcg==,size_16,color_FFFFFF,t_70" alt="在这里插入图片描述" style="zoom:67%;" />

​		`Spring Cloud Config` 为微服务架构中的微服务提供集中化的外部配置支持，配置服务器为各个不同微服务应用的所有环境提供了一个中心化的外部配置。

​		`SpringCloud Config` 分为==服务端==和==客户端==两部分。

​		服务端也称为分布式配置中心，他是一个独立的微服务应用，用来连接配置服务器并为客户端提供获取配置信息，加密/解密 信息等访问接口。

​		客户端则是通过制定的配置中心来管理应用资源，以及与业务相关的配置内容，并在启动的时候从配置中心获取和加载配置信息配置服务器默认采用git来存储配置信息，这样就有助于对环境配置进行版本管理，并且可以通过git客户端工具来方便的管理和访问配置内容。

**能干吗**

- 集中管理配置文件
- 不同环境不同配置，动态化的配置更新，分环境部署比如dev/test/prod/beta/release
- 运行期间动态调整配置,不再需要在每个服务部署的机器上编写配置文件,服务会向配置中心统一拉取配置自己的信息，
- 当配置发生变动时，服务不需要重启即可感知到配置的变化并应用新的配置
- 将配置信息以REST接口的形式暴露

  - post、curl 访问刷新均可……


**与GitHub整合配置**

​		由于 `SpringCloud Config` 默认是使用 `Git` 来存储配置文件（也有其他方式，比如支持SVN和本地文件），但最推荐的还是 `Git` ，而且使用的是 `http/https` 访问的形式

### 9.2 服务端配置与测试

#### 9.2.1 创建github仓库

​		在 `github` 上面新建仓库，用于存放配置文件

![image-20220208210728799](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-08/8efd3248305e50fefe72c73787d6ba0b.jpeg)

​		创建仓库之后，添加一些配置文件到仓库中。

#### 9.2.2 新建模块

​		新建一个名为 `cloud-config-server3344` 的模块

#### 9.2.3 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-config-server3344</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!--监控信息的完善-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
        <!--eureka-client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>
    </dependencies>

</project>
```

#### 9.2.4 改 `yml`

**注意： 此时 `uri` 我使用的是ssh加速之后的地址，原地址可能会导致该模块报错：==USERAUTH fail==**

```yml
server:
  port: 3344

spring:
  application:
    name: cloud-config-center
  cloud:
    config:
      server:
        git:
          # ssh地址（也可以使用 https地址，不过需要配置username和password）,我配置了加速之后的地址
          uri: git@git.zhlh6.cn:luo-yalong/SpringCloud-config.git
          # 搜索的目标
          search-paths:
            - SpringCloud-config

# 读取的分支
      label: master

eureka:
  client:
    #表示是否向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
      #      defaultZone: http://localhost:7001/eureka  #单机版
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  #集群版
  instance:
    #实例id,显示在Eureka注册中心的名字，默认是ip地址+端口号
    instance-id: Config3344
    #访问路径是否显示ip地址
    prefer-ip-address: true
```

#### 9.2.5 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * @author luoyalong
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigMain3344 {
    public static void main(String[] args) {
        SpringApplication.run(ConfigMain3344.class,args);
    }
}
```

#### 9.2.6 其他设置

1. 修改 `hosts` 文件

   ​		修改 `hosts` 文件，在其中添加域名映射

   ```reStructuredText
   127.0.0.1	config-3344.com
   ```

#### 9.2.7 测试

1. **依次启动项目**

   - EurekaMain7001
   - EurekaMain7002
   - ConfigMain3344

2. **测试**

   ​		在浏览器中输入 http://eureka7001.com:7001  可以查看到配置中心已经注册进注册中心了。

   <img src="https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/bae5c9634f5a2711d74fd0ad6ea056e4.jpeg" alt="image-20220209103717829" style="zoom: 50%;" />

   ---

   具体查看 `GitHub`仓库中配置文件的拼接方法可以参考[官网](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/)

   The HTTP service has resources in the following form:

   - **application：**应用名称，例如：application，config
   - **profile：**环境名称，例如：dev，test，prod
   - **label：** `git` 分支，例如：master，dev

   ```apl
   /{application}/{profile}[/{label}]
   /{application}-{profile}.yml
   /{label}/{application}-{profile}.yml
   /{application}-{profile}.properties
   /{label}/{application}-{profile}.properties
   ```

   For example:

   ```apl
   curl localhost:8888/foo/development
   curl localhost:8888/foo/development/master
   curl localhost:8888/foo/development,db/master
   curl localhost:8888/foo-development.yml
   curl localhost:8888/foo-db.properties
   curl localhost:8888/master/foo-db.properties
   ```

   ​	在浏览器中输入 http://config-3344.com:3344/master/config-dev.yml，可以查看开发环境配置

   ![image-20220209103856013](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/d16bc48000f6d407744ddd027ce47f20.jpeg)

   ​	在浏览器中输入 http://config-3344.com:3344/master/config-test.yml，可以查看测试环境配置

   ![image-20220209103955941](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/330172e37fca10966d5ce06f6214e952.jpeg)

   ​	

### 9.3 Config客户端配置与测试

​		`config` 客户端需要从配置中心读取配置文件。

#### 9.3.1 新建Config客户端模块

​		新建一个名为 `cloud-config-client-3355` 的模块

#### 9.3.2 改 `pom`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>CloudStudy</artifactId>
        <groupId>com.lyl</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>cloud-config-client-3355</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!--监控信息的完善-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!--config客户端-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <!--eureka-client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>
    </dependencies>

</project>
```

#### 9.3.3 改 `yml`

​		不同与之前的 `application.yml` ，当前模块我们需要新建一个 `bootstrap.yml` 文件替换掉 `application.yml`文件

---

**介绍**

​		`application.yml` 是用户级的资源配置项

​		`bootstrap.yml` 是系统级的资源配置项

`Bootstrap`属性有高优先级，默认情况下,它们不会被本地配置覆盖。Bootstrap context和Application Context'有着不同的约定
所以新增了-个`bootstrap.yml`文件，保证`Bootstrap Context`和 `Application Context`配置的分离。

要将 `Client` 模块下的 `application.yml` 文件改为 `bootstrap.yml` ，这是很关键的，因为`bootstrap.yml`是比application.yml先加载的。

`bootstrap.yml`优先级高于`application.yml`

---

```yml
server:
  port: 3355

spring:
  application:
    name: cloud-config-client
  cloud:
    config:
      # 读取的分支
      label: master
      name: config  #配置文件的名称
      profile: dev  #读取的后缀名称
      #上述综合：master分支上config-dev.yml的配置文件被读取http://config-3344.com:3344/master/config-dev.yml
      uri: http://localhost:3344 #配置中心地址

eureka:
  client:
    #表示是否向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
      #      defaultZone: http://localhost:7001/eureka  #单机版
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  #集群版
  instance:
    #实例id,显示在Eureka注册中心的名字，默认是ip地址+端口号
    instance-id: ConfigClient3355
    #访问路径是否显示ip地址
    prefer-ip-address: true
```

#### 9.3.4 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author 罗亚龙
 * @date 2022/2/9 11:03
 */
@SpringBootApplication
@EnableEurekaClient
public class ConfigClientMain3355 {
    public static void main(String[] args) {
        SpringApplication.run(ConfigClientMain3355.class,args);
    }
}
```

#### 9.3.5 业务类

​		读取配置文件中的信息

```java
package com.lyl.springcloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 罗亚龙
 * @date 2022/2/9 11:05
 */
@RestController
public class ConfigClientController {

    @Value("${config.info}")
    private String configInfo;

    @GetMapping("/getConfigInfo")
    public String getConfigInfo(){
        return configInfo;
    }
}
```

#### 9.3.6 测试

1. **依次启动项目**

   - 启动 EurekaMain7001
   - 启动 EurekaMain7002
   - 启动 ConfigMain3344
   - 启动 ConfigClientMain3355

2. **测试**

    1. 测试配置中心

       ​		在浏览器中输入：http://config-3344.com:3344/master/config-dev.yml ，可以看到配置信息

       ![image-20220209112144127](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/ea89706c75d9184cef091696439e72ba.jpeg)

    2. 测试Config客户端

       ​		在浏览器中输入：http://localhost:3355/getConfigInfo ,  可以从配置中心获取到配置的信息

       ![image-20220209112254518](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/5385a63c50f3e9852162c38f0b766bd2.jpeg)

       ​		因为 `config` 客户端的 `yml` 文件中配置的是 ： `master` 分支的 `dev` 文件，所以，获取到的是 `dev` 环境的配置文件。现在，我们修改 `config` 客户端的 `yml` 文件，使其获取 `master` 分支的 `prod` 文件

       **修改之后的 `yml`文件如下所示**

       <img src="https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/915f07aea0d1dccddb1022672b4df384.jpeg" alt="image-20220209112643632" style="zoom: 50%;" />

       之后，再次使用 http://localhost:3355/getConfigInfo  获取配置中心的配置，显示如下：

       ![image-20220209112835600](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/11fe38a1a9ab0a26146365f7d05ac089.jpeg)

#### 9.3.7 总结

​		成功实现了`客户端 3355` 访问 `配置中心 3344` 通过 `GitHub` 获取配置信息的功能

### 9.4 分布式的动态刷新问题

  1. 现在修改 `GitHub` 上面的配置文件，加 age 或者修改版本号，配置中心可以立即获取到 `GitHub` 上面变化的配置文件，但是 `客户端 3355` 并没有立即响应的新配置。

      1. 修改 `GitHub` 上 `config-dev.yml` 的版本号

         ![image-20220209113644881](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/1e077b8befe7b6b95ceb00f808757d99.jpeg)

         <img src="https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/72b482ba24a6bff96b38776e1302eff4.jpeg" alt="image-20220209113748168" style="zoom: 67%;" />

     	2. 刷新配置中心获取 `dev` 配置

         ​		在浏览器中输入 http://config-3344.com:3344/config-dev.yml ，响应立即发生变化

         ![image-20220209113932427](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/001e819cceb0fbe11fd8a74aff20a12b.jpeg)

     	3. `config客户端3355` 获取配置中心配置

         ​		在浏览器中输入：http://localhost:3355/getConfigInfo  ，配置并没有变化，此时需要重新启动 客户端，才可以让响应发生变化。

         ![image-20220209114109141](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/2b4f131ce96d2a6ae57a26ffaa4528c5.jpeg)

  2. 动态刷新问题

     1. `Linux` 运维修改了 `GitHub` 上的配置文件内容做调整
     2. 刷新 3344 ， 发现 `ConfigServer` 配置中心立即响应
     3. 刷新 3355 ， 发现 `ConfigClient` 客户端没有任何响应
     4. 3355 没有变化，除非自己重启或者重新加载
     5. 难道每次运维修改配置文件，客户端都需要重启？？？噩梦

### 9.5 Config动态刷新之手动版

#### 9.5.1 概述

​		为了在运维工程师修改 `GitHub` 中的配置文件之后，`config客户端` 不需要重启就可以加载到变化了的配置 ，我们需要添加一些配置。

#### 9.5.2 改 `pom` 引入 `actuator` 监控

```xml
<!--监控信息的完善-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

#### 9.5.3 暴露监控端点

​		在 `Config客户端3355` 的 `bootstrap.yml` 中添加以下内容

```yml
#暴露监控端点      
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

**完整 `bootstrap.yml`**

```yml
server:
  port: 3355

spring:
  application:
    name: cloud-config-client
  cloud:
    config:
      # 读取的分支
      label: master
      name: config  #配置文件的名称
      profile: dev  #读取的后缀名称
      #上述综合：master分支上config-dev.yml的配置文件被读取http://config-3344.com:3344/master/config-dev.yml
      uri: http://localhost:3344 #配置中心地址

#暴露监控端点
management:
  endpoints:
    web:
      exposure:
        include: "*"

eureka:
  client:
    #表示是否向eureka注册自己
    register-with-eureka: true
    #表示是否需要从 eureka-server 抓取已有的注册信息，单节点无所谓，集群必须为true
    fetch-registry: true
    service-url:
      #注册中心地址
      #      defaultZone: http://localhost:7001/eureka  #单机版
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka  #集群版
  instance:
    #实例id,显示在Eureka注册中心的名字，默认是ip地址+端口号
    instance-id: ConfigClient3355
    #访问路径是否显示ip地址
    prefer-ip-address: true
```

#### 9.5.4 修改业务类

​		在业务类上面添加 `@RefreshScope` 注解，实现刷新功能

**完整代码如下：**

```java
package com.lyl.springcloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 罗亚龙
 * @date 2022/2/9 11:05
 */
@RestController
@RefreshScope
public class ConfigClientController {

    @Value("${config.info}")
    private String configInfo;

    @GetMapping("/getConfigInfo")
    public String getConfigInfo(){
        return configInfo;
    }
}
```

#### 9.5.5 测试

​		此时，修改 `GitHub` 上面的配置文件，查看配置中心，配置中心的响应发生改变，但是 `Config客户端3355`  上的 http://localhost:3355/getConfigInfo 接口中的响应没有发生改变。此时，依旧需要重新启动才能加载到最新的文件。

​		这似乎不是动态刷新，我们做的这几步好像是多余的，没有生效。

​		别着急，在运维工程师修改 `Github` 中的配置文件之后，需要发送一个 `post` 请求，使配置刷新

```shell
curl -X POST "http://localhost:3355/actuator/refresh"
```

![image-20220209133112824](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/6d86f0c9d60f2c7f5ea115c63d657074.jpeg)

之后，再次调用 http://localhost:3355/getConfigInfo 就会发现不需要重启项目，配置也会生效了

![image-20220209133222796](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-02-09/6c4f2942768352695c22353897b475b7.jpeg)

#### 9.5.6 手动版动态刷新总结

- 运维工程师修改 `Github` 上的配置文件
- 发送 `post` 请求，刷新配置

### 9.6 后序问题

​		目前，我们基本上完成了配置的动态刷新功能。

​		但是还是有一些问题的：

​				假如目前有多个微服务客户端 `3355、3366、3377……` ，每个微服务都需要手动执行一次 `post` 请求，手动刷新？有些麻烦。虽然可以利用脚本自动批量刷新，但是如果我们需要精确的刷新某些配置，可能就力不从心了。

​				==可否广播，一次通知，处处生效？== 

​				我们想大范围的自动刷新，就需要引入 `Spring Cloud Bus` 消息总线了。



