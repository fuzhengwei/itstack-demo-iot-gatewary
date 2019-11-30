package org.itstack.demo.iot.gateway.masterpro;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import org.itstack.demo.iot.gateway.connect.ToMasterDesign;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 添加监听，处理重连
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年9月6日 上午11:20:51
 */
public class MasterDisconnectListener implements ChannelFutureListener {
    private int masterid = 0;
    private String ip = "";
    private int port = 0;

    public MasterDisconnectListener(int masterid, String ip, int port) {
        this.masterid = masterid;
        this.ip = ip;
        this.port = port;
    }

    /**
     * @Description: 启动是失败重连
     */
    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
//			System.out.println(new Date() + "    "+"HEARTBEAT(Master.size:"+ConfigDesign.Server_IP_CONNECT.size()
//					+"client.size:"+ConfigDesign.CLIENT_IP_CONNECT.size() +")...");
            return;
        }
        System.out.println("reconnection to master:  " + new Date() + "   ip:" + ip + "  port:" + port);
        final EventLoop loop = future.channel().eventLoop();
        loop.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    ToMasterDesign thisServer = new ToMasterDesign(masterid, ip, port);
                    thisServer.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1L, TimeUnit.SECONDS);//1L的含义？

    }

}
