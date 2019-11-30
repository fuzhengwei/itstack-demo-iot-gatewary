package org.itstack.demo.iot.gateway.divpro;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.itstack.demo.iot.gateway.connect.ConfigDesign;

/**
 * @Description:
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年8月30日 下午2:49:52
 */
public class DivServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //ctx.channel().remoteAddress().toString() == /127.0.0.1:53290
        ConfigDesign.CLIENT_IP_CONNECT.put(ctx.channel().remoteAddress().toString().substring(1), ctx.channel());
        System.out.println("收到客户端连接:" + ctx.channel().remoteAddress().toString());
//		 	ToClientDesign.jedisCluster.hset(ctx.channel().remoteAddress().toString().substring(1),"ONLINE","1");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {//ctx.close();//close会自动调用本方法
        System.out.println("客户端断开链接:" + ctx.channel().remoteAddress().toString());
        //设计在线缓存
//		 	ToClientDesign.jedisCluster.hset(ctx.channel().remoteAddress().toString().substring(1),"ONLINE","0");
        ConfigDesign.CLIENT_IP_CONNECT.remove(ctx.channel().remoteAddress().toString().substring(1), ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String) msg;//因为有解码器，这里不需要在转ByteBuf再转string了
        body = ctx.channel().remoteAddress().toString().substring(1) + "_" + body;
        ctx.fireChannelRead(body);//转到下一个handler，body是下一个handler的msg
    }
	    
/*	    //当有客户端连接的时候，会触发
	    @Override
	    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
	      //保存所有的连接Channel
	    	ConfigDesign.CLIENT_IP_CONNECT.put(ctx.channel().remoteAddress().toString().substring(1),ctx.channel());
	    }
	    */
    /**
     * 超时处理，如果HEARTBEAT_TIME秒没有收到客户端的心跳，就触发;
     */
    /**
     * 空闲次数
     */
    private int idle_count = 1;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.READER_IDLE.equals(event.state())) { // 如果读通道处于空闲状态，说明没有接收到心跳命令
                if (idle_count > 2) {
                    System.out.println("超过两次无客户端请求，关闭该channel");
                    ctx.channel().close();
                }
                System.out.println("已等待" + DivMultiprotocolSelection.HEARTBEAT_TIME
                        + DivMultiprotocolSelection.TIME_UNIT
                        + "还没收到客户端发来的消息");
                idle_count++;
            }//服务端就监控一个读就行了
        } else {
            super.userEventTriggered(ctx, obj);
        }
    }

    /**
     * 当发生异常时，打印异常 日志，释放客户端资源
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        /**释放资源*/
        System.out.println("Unexpected exception from downstream : " + cause.getMessage());
        ctx.close();
        //ToClientDesign.jedisCluster.hset(ctx.channel().remoteAddress().toString().substring(1),"ONLINE","0");
    }
}
