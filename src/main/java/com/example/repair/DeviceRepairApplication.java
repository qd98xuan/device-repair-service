package com.example.repair;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 设备报修工单服务主启动类
 */
@SpringBootApplication
@MapperScan("com.example.repair.mapper")
public class DeviceRepairApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceRepairApplication.class, args);
    }

}
