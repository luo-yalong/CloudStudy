# SpringCloud

## 1. 简介

### 1.1 SpringCLoud是什么？

​		SpringCloud ：分布式微服务架构的一站式解决方案，是多种微服务架构落地技术的几何体，俗称微服务全家桶

​		SpringCloud有哪些技术？

![image-20220120194157438](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-20/69c1c4d80547da274e66e2bb338106aa.jpeg)

### 1.2 SpringCloud技术栈

​		主要了解一下这些

![image-20220120194653417](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-20/036f6335da61fd79ca90d2386644ecdb.jpeg)

## 2. 技术选型

### 2.1 版本选择

​		从SpringCloud官网中查看对应的SpringCloud和SpringBoot版本。

**查看详细版本的方法：[详细版本](https://start.spring.io/actuator/info)**

```http
https://start.spring.io/actuator/info
```

**具体的技术选型**

![image-20220120200248694](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-20/47ff1b10f0122b486d7bcdefa517b89a.jpeg)

### 2.2 问题

1. **为什么Springboot已经有最新版了，还要用之前的版本？**

   答：因为需要照顾到SpringCloud，需要查看SpringCloud对应的Springboot版本。

## 3. SpringCloud技术变更

![image-20220120201806637](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-20/982d2dc7501d09370fc9fec48d4b4f06.jpeg)

## 4. 微服务架构编码构建

​		搭建一个订单支付模块，逐渐整合微服务其他技术

### 4.1 搭建父工程

#### 4.1.1 创建一个maven项目

![image-20220120204544841](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-20/fa2b2d3ec76fec994306cead9e9e6bd2.jpeg)

![image-20220120204738020](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-20/f0d9e70505ee8d177613807a1615b3c8.jpeg)

![image-20220120204749931](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-20/f8ca98e17ab671ebec3bc672cacba627.jpeg)

#### 4.1.2 修改基本配置

1. 修改文件编码

   ![image-20220120204308715](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-20/82d3f709492d1b7b5e7e1a6a2788cd51.jpeg)

2. 设置java编译版本

   ![image-20220120204355771](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-20/73c625abb8188d058b52b3b1a8b5c9f5.jpeg)

3. 启动注解支持

   ![image-20220120204413965](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-20/b8a646f616ff50ff11771a526d7b28d6.jpeg)

### 4.2 修改父工程的pom文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.lyl</groupId>
    <artifactId>CloudStudy</artifactId>
    <version>1.0-SNAPSHOT</version>
    <modules>
        <module>cloud-provider-payment8001</module>
    </modules>
    <!--一定要改成pom-->
    <packaging>pom</packaging>

    <!-- 统一管理jar包版本 -->
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <junit.version>4.12</junit.version>
        <log4j.version>1.2.12</log4j.version>
        <lombok.version>1.18.22</lombok.version>
        <mysql.version>8.0.27</mysql.version>
        <druid.version>1.1.10</druid.version>
        <mybatis-plus.version>3.4.3.4</mybatis-plus.version>
    </properties>

    <!-- 子模块继承之后，提供作用：
      锁定版本+子module不用写groupId和version -->
    <dependencyManagement>
        <dependencies>
            <!--spring boot 2.2.2-->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.2.2.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!--spring cloud Hoxton.SR1-->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Hoxton.SR1</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!--spring cloud alibaba 2.1.0.RELEASE-->
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>2.1.0.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>mysql</groupId>
                <artifactId>mysql-connector-java</artifactId>
                <version>${mysql.version}</version>
            </dependency>
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>druid</artifactId>
                <version>${druid.version}</version>
            </dependency>
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-boot-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>${junit.version}</version>
            </dependency>
            <dependency>
                <groupId>log4j</groupId>
                <artifactId>log4j</artifactId>
                <version>${log4j.version}</version>
            </dependency>
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
                <optional>true</optional>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>2.6.2</version>
                <configuration>
                    <fork>true</fork>
                    <addResources>true</addResources>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

### 4.3 跳过测试

​		打开 `maven` 侧边栏，点击 `箭头` 图标，跳过测试

![image-20220120220424466](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-20/28dcfe8f67a6937ef7f56d9feee96aeb.jpeg)

## 5. 订单支付模块

**新建模块通用流程**

- 建模块
- 改 `pom`
- 改 `yml`
- 主启动
- 业务类

### 5.1 新建模块

​		按照之前创建模块的流程，新建一个 `maven` 模块，名称为 `cloud-provider-payment8001`

**目录结构如图所示**

![image-20220121110028011](https://gitee.com/luoyalongLYL/upload_image_repo/raw/master/typroa/2022-01-21/1fdae56fe38885eccba1535457b878d4.jpeg)

### 5.2 改 `pom`

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

    <artifactId>cloud-provider-payment8001</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>


    <dependencies>
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

### 5.3 改 `yml`

```yaml
server:
  port: 8001

spring:
  application:
    name: cloud-payment-service
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource            # 当前数据源操作类型
    driver-class-name: com.mysql.cj.jdbc.Driver              # mysql驱动包
    url: jdbc:mysql://localhost:3306/my?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: 1234

mybatis-plus:
  type-aliases-package: com.lyl.springcloud.entities    #别名
  mapper-locations: classpath:mapper/*.xml
```

### 5.4 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author luoyalong
 */
@SpringBootApplication
public class PaymentMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8001.class,args);
    }
}
```

### 5.5 业务类

> 业务类的 `entity`  、`dao` 、`service` 、`serviceImpl `  和  `mapper.xml`  都可以使用 `idea` 的插件 `easy-code` 来自动生成。

1. **SQL**

​		在 `mysql` 上面创建一个 数据库，新建一张表

```mysql
CREATE TABLE `payment`(
	`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `serial` varchar(200) DEFAULT '',
	PRIMARY KEY (id)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4
```

---

2. 创建 `entity`

   ```java
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
   ```

3. 创建 `dao`

   1. dao

      ```java
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
      ```

   2. xml

      ```xml
      <?xml version="1.0" encoding="UTF-8"?>
      <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
      <mapper namespace="com.lyl.springcloud.dao.PaymentDao">
          
          <sql id="insert_column">
              serial    
          </sql>
          
          <sql id="all_column">
              id ,<include refid="insert_column"/>
          </sql>
          
          <resultMap type="com.lyl.springcloud.entity.Payment" id="PaymentMap">
              <id property="id" column="id"/>
              <result property="serial" column="serial"/>
          </resultMap>
      
          <!-- 批量插入 -->
          <insert id="insertBatch" keyProperty="id" useGeneratedKeys="true">
              insert into cloud_study.payment(<include refid="insert_column"/>)
              values
              <foreach collection="entities" item="entity" separator=",">
                  (#{entity.serial})
              </foreach>
          </insert>
          <!-- 批量插入或按主键更新 -->
          <insert id="insertOrUpdateBatch" keyProperty="id" useGeneratedKeys="true">
              insert into cloud_study.payment(<include refid="insert_column"/>)
              values
              <foreach collection="entities" item="entity" separator=",">
                  (#{entity.serial})
              </foreach>
              on duplicate key update
               serial = values(serial)          
          </insert>
      
      </mapper>
      ```

4. 创建 `service`

   1. 接口

      ```java
      package com.lyl.springcloud.service;
      
      import com.baomidou.mybatisplus.extension.service.IService;
      import com.lyl.springcloud.entity.Payment;
      
      /**
       * (Payment)表服务接口
       *
       * @author 罗亚龙
       * @since 2022-01-21 11:08:34
       */
      public interface PaymentService extends IService<Payment> {
          
      }
      ```

   2. 实现类

      ```java
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
      ```

5. 创建 `controller`

   ```java
   package com.lyl.springcloud.controller;
   
   import com.lyl.springcloud.entity.Payment;
   import com.lyl.springcloud.entity.Result;
   import com.lyl.springcloud.service.PaymentService;
   import org.springframework.web.bind.annotation.*;
   
   import javax.annotation.Resource;
   
   /**
    * @author 罗亚龙
    * @date 2022/1/21 11:15
    */
   @RestController
   @RequestMapping("/payment")
   public class PaymentController {
       @Resource
       private PaymentService paymentService;
   
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
           return Result.success("查询成功",payment);
       }
   }
   ```

6. 统计返回结果类

   ```java
   package com.lyl.springcloud.entity;
   
   import cn.hutool.core.collection.ListUtil;
   import lombok.AllArgsConstructor;
   import lombok.Data;
   import lombok.NoArgsConstructor;
   import lombok.experimental.Accessors;
   
   /**
    * @author 罗亚龙
    * @date 2022/1/21 13:14
    */
   @Data
   @Accessors(chain = true)
   @NoArgsConstructor
   @AllArgsConstructor
   public class Result {
   
       private Integer code;
       private String msg;
       private Object data = ListUtil.empty();
       
       public static Result success(){
           return success ("操作成功");
       }
   
       public static Result success(String msg) {
           return success(msg,ListUtil.empty());
       }
   
   
       public static Result success(Object data) {
           return success("操作成功", data);
       }
   
       public static Result success(String msg, Object data) {
           return success(200, msg, data);
       }
   
       public static Result success(Integer code, String msg, Object data){
           return new Result(code,msg,data);
       }
   
   
       public static Result fail() {
           return fail("操作失败");
       }
   
       public static Result fail(String msg) {
           return fail(400, msg);
       }
   
       public static Result fail(Integer code, String msg) {
           return fail(code, msg, ListUtil.empty());
       }
   
       public static Result fail(Object data) {
           return fail(400,"操作失败" , data);
       }
   
       public static Result fail(Integer code, String msg, Object data){
           return new Result(code,msg,data);
       }
       
   }
   ```

所有的类创建完成之后，启动项目，进行测试



## 6. 订单模块

### 6.1 新建模块

创建模块，按照几个步骤 ，依次创建

新建一个名称为 `cloud-consumer-order80` 的 `maven` 子模块

### 6.2 改 `pom`

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

    <artifactId>cloud-consumer-order80</artifactId>

    <dependencies>
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

### 6.3 改 `yml`

```yaml
server:
  port: 80
spring:
  application:
    name: cloud-order-service
```

### 6.4 主启动

```java
package com.lyl.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 罗亚龙
 * @date 2022/1/21 14:04
 */
@SpringBootApplication
public class OrderMain80 {

    public static void main(String[] args) {
        SpringApplication.run(OrderMain80.class,args);
    }
    
}
```
