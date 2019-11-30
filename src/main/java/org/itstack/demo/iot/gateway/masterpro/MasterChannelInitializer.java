package org.itstack.demo.iot.gateway.masterpro;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.itstack.demo.iot.gateway.protobuf.MasterMessage;
import org.itstack.demo.iot.gateway.protobuf.Message;

import java.util.concurrent.TimeUnit;

/**
 * @Description: Filter
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年9月3日 下午5:01:10
 */
public class MasterChannelInitializer extends ChannelInitializer<SocketChannel> {

    int masterid = 0;

    public MasterChannelInitializer(int masterid) {
        this.masterid = masterid;
    }

    /**
     * @Description:
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //下面的handler要每10秒发一个心跳
        ch.pipeline().addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS));
        ch.pipeline().addLast("decoder", new ProtobufDecoder(Message.getDefaultInstance()));
        //ch.pipeline().addLast("encoder", new ProtobufEncoder());
        ch.pipeline().addLast(new StringEncoder());//组码发给客户端
        //在管道中添加我们自己的接收数据实现方法
        ch.pipeline().addLast(new MasterClientHandler(masterid));

    }

}
