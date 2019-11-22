package com.example.netty;

import com.example.netty.chat.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @date : 2019/11/21
 */
@SpringBootApplication
public class NettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);
        new NettyServer(2333).start();
    }

}
