package org.itstack.demo.iot.gateway.connect;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.itstack.demo.iot.gateway.divpro.DivChannelInitializer;
import org.itstack.demo.iot.gateway.freeconfig.GetProperties;
import org.itstack.demo.iot.gateway.jedis.Jedisclusters;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Description: server=gate
 * 客户端开发ok，本地唯一要改的是，心跳时间，不同设备或厂家的心跳时间是不同的
 * 所以，客户端也是需要一个多规约支持的心跳时间的设计，如果有通用，那取一个大值，包含所有小值也是有部分可行的
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年8月30日 上午10:04:44
 */
@Configuration
public class ToClientDesign implements Runnable {
    private int poolNum = 0;

    @Resource
    public static Jedisclusters jedisCluster;

    public ToClientDesign(int poolnum) {
        this.poolNum = poolnum;
    }

    @Override
    public void run() {
        //一旦 Channel 注册了，就处理该Channel对应的所有I/O 操作。
        EventLoopGroup bossGroup = new NioEventLoopGroup(ConfigDesign.BIZGROUPSIZE);
        EventLoopGroup workerGroup = new NioEventLoopGroup(ConfigDesign.BIZTHREADSIZE);
        try {
            /** 配置服务端的 NIO 线程池,用于网络事件处理，实质上他们就是 Reactor 线程组
             * bossGroup 用于服务端接受客户端连接，workerGroup 用于进行 SocketChannel 网络读写
             * */
            ServerBootstrap gateServer = new ServerBootstrap();
            gateServer.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, ConfigDesign.CACHESIZE)
                    .childHandler(new DivChannelInitializer());

            String port = GetProperties.getValue("client.port");
            System.out.println("read client config:" + port);
            ChannelFuture f = gateServer.bind(Integer.parseInt(port)).sync();
            System.out.println("bind: [" + new Date() + "]   服务器开始监听端口" + port + "，等待客户端连接.........");
            /**下面会进行阻塞,线程变为wait状态，等待服务器连接关闭
             * 服务器同步连接断开时,这句代码才会往下执行,
             * */
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            /**优雅退出，释放线程池资源*/
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }


}
