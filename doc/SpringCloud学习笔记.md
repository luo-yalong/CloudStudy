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

