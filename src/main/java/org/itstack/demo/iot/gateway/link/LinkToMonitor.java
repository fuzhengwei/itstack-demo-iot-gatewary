package org.itstack.demo.iot.gateway.link;

import org.itstack.demo.iot.gateway.connect.ConfigDesign;

import java.util.Date;

/**
 * @Description: 1.监控客户端和服务端的在线个数
 * @date: 2019年9月17日 上午10:53:10
 */
public class LinkToMonitor implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Link Listener: [" + new Date() + "]    " + "Master.size:" + ConfigDesign.Server_IP_CONNECT.size()
                        + "   client.size:" + ConfigDesign.CLIENT_IP_CONNECT.size() + "...");
                Thread.sleep(10000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
