/**
 *
 */
package org.itstack.demo.iot.gateway.masterpro;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.itstack.demo.iot.gateway.connect.ConfigDesign;
import org.itstack.demo.iot.gateway.connect.FlowListener;
import org.itstack.demo.iot.gateway.connect.ToClientDesign;
import org.itstack.demo.iot.gateway.connect.ToMasterDesign;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年9月3日 下午5:18:25
 */
public class MasterClientHandler extends ChannelInboundHandlerAdapter {
    private int masterid = 0;

    public MasterClientHandler(int masterid) {
        this.masterid = masterid;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        int masterlen = ConfigDesign.Server_IP_CONNECT.size();
        ConfigDesign.Server_IP_CONNECT.put("" + (++masterlen), ctx.channel());//1.轮询发送data，2，心跳在线维护

    }

    /**
     * 当服务端返回应答消息时，channelRead 方法被调用，从 Netty 的 ByteBuf 中读取并打印应答消息
     * 设计报文头，ip:port_报文
     * 读取前置发来的信息，踢掉在线终端设计：killclent_ip:port
     * 读取前置发来的信息，ip黑名单设计：addblacklist_ip:port  || removeblacklist_ip:port
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//	    	因编解码的不同，这里也不同，编解码应该是二进制的，解码应该把二进制结成string，看看做好了没。
//	    	找到CLIENT_IP_CONNECT内存中对应的客户端，msg是前置给出的ip端口和报文。

        String body = (String) msg;
        //踢掉在线终端功能
        if ("killclent".equals(body.split("_")[0])) {
            Channel ch = ConfigDesign.CLIENT_IP_CONNECT.get(body.split("_")[1]);
            ch.close();
            ConfigDesign.CLIENT_IP_CONNECT.remove(body.split("_")[1]);
            ToClientDesign.jedisCluster.hset(ctx.channel().remoteAddress().toString().substring(1), "ONLINE", "0");
            return;
        } else if ("blacklist".equals(body.split("_")[0])) {
            if (!ConfigDesign.BLACKLIST.contains(body.split("_")[1])) {//判断是否不存在
                ConfigDesign.BLACKLIST.add(body.split("_")[1]);
//	    			redis
            }
            return;
        } else if ("removeblacklist".equals(body.split("_")[0])) {
            if (ConfigDesign.BLACKLIST.contains(body.split("_")[1])) {//判断是否存在
                ConfigDesign.BLACKLIST.remove(body.split("_")[1]);
//	    			redis
            }
            return;
        } else {
            Channel ch = ConfigDesign.CLIENT_IP_CONNECT.get(body.split("_")[0]);
            ch.writeAndFlush(body.split("_")[1]);
            FlowListener.getInstance().writeFlow(body.split("_")[0], body.split("_")[1].length() / 2);
        }
    }

    /**
     * 当发生异常时，打印异常 日志，释放客户端资源
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        /**释放资源------这里应该是要重连的*/
//	        logger.warning("Unexpected exception from downstream : " + cause.getMessage());
        System.out.println("Unexpected exception from downstream : " + cause.getMessage());
        ctx.close();
    }

    /**
     * 运行过程中，服务器断开连接
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("运行中服务器断开连接，重连至服务端....");
        String ipport = ctx.channel().remoteAddress().toString().substring(1);
        System.out.println("运行中服务器断开连接，重连至服务端:" + ipport);
        final String ip = ipport.split(":")[0];
        final int port = Integer.parseInt(ipport.split(":")[1]);
        final EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(new Runnable() {
            @Override
            public void run() {
                ToMasterDesign thisServer = new ToMasterDesign(masterid, ip, port);
                thisServer.connect();
            }
        }, 1L, TimeUnit.SECONDS);
        super.channelInactive(ctx);


    }

    /**
     * 心跳请求处理，每4秒发送一次心跳请求;
     * 不是3次就停，而是一直发
     */

    /** 空闲次数 */
    private int idle_count = 1;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {

        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.WRITER_IDLE.equals(event.state())) { // 如果写通道处于空闲状态就发送心跳命令
                String str = "gateway_Heartbeat";
//	            	  System.out.println("This is client,write my heartbeat to my master："+str);
                System.out.println("master heartbeat: [" + new Date() + "]  " + ctx.channel().remoteAddress().toString().substring(1));

                ctx.channel().writeAndFlush(str);
            }
        }
    }

}
