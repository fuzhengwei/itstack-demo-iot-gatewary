package org.itstack.demo.iot.gateway.connect;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.itstack.demo.iot.gateway.masterpro.MasterChannelInitializer;
import org.itstack.demo.iot.gateway.masterpro.MasterDisconnectListener;

import java.util.Date;

/**
 * @Description: client------>master
 * 1.
 * 连接master服务器
 * 服务器active里回复我信息
 * 服务端来的channel，我给保存起来。
 * 2.用rediscluster的连接方式
 * 一个类做：本地启多个连接，map保存每个每个连接
 * 一个类做：校验map中的连接是否活着，死了怎么办
 * <p>
 * 1.要改前置与网关心跳时间，前置那边监控时间5s，本地客户端设置监控write为4s
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年8月30日 上午10:04:30
 */
public class ToMasterDesign implements Runnable {
    private int masterid = 0;
    private String ip = "";
    private int port = 0;

//	@Resource
//	public  Jedisclusters jedisCluster; //都调用的是ToClientDesign里的

    public ToMasterDesign(int masterid, String ip, int port) {
        this.masterid = masterid;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {
        connect();

    }

    public void connect() {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap gate = new Bootstrap();
            gate.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new MasterChannelInitializer(masterid));

            ChannelFuture channelFuture = gate.connect(ip, port).sync();

            if (channelFuture.channel().isActive()) {
                sendMsg(channelFuture.channel());
            }

            //负责监听启动时连接失败，重新连接功能 ,也有人使用channelInactive，直接进行重连
            channelFuture.addListener(new MasterDisconnectListener(masterid, ip, port)); //添加监听，处理重连

            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }


    public void sendMsg(Channel channel) {
        String str = "gateway_Login";
        System.out.println("Login： [" + new Date() + "]   " + channel.remoteAddress().toString().substring(1));
        channel.writeAndFlush(str);
    }

}
