package org.itstack.demo.iot.gateway.start;

import org.itstack.demo.iot.gateway.connect.PoolDesign;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description:
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年9月10日 下午2:19:20
 */

@SpringBootApplication
public class StartGate {

    public static void main(String[] args) {
        SpringApplication.run(StartGate.class, args);
        PoolDesign startpool = new PoolDesign();
        startpool.start();
    }

}
