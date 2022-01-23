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
