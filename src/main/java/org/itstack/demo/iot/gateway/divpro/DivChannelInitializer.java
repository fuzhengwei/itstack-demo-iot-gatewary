package org.itstack.demo.iot.gateway.divpro;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @Description: 这里有一个心跳控制，和一个多规约handler控制
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年8月30日 下午2:48:22
 */
public class DivChannelInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * @Description:
     */
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        //服务端心跳监控，50秒钟没有读到数据
        channel.pipeline().addLast(new IdleStateHandler(DivMultiprotocolSelection.HEARTBEAT_TIME,
                0, 0, DivMultiprotocolSelection.TIME_UNIT));
        channel.pipeline().addLast(new StringDecoder());
        //channel.pipeline().addLast(new StringEncoder());
        channel.pipeline().addLast("encoder", new ProtobufEncoder());//组码发给master
        channel.pipeline().addLast(new DivServerHandler());

        switch (DivMultiprotocolSelection.PROTOCOL_TYPES) {
            case "104":
                channel.pipeline().addLast(new Check104Handler());
                break;
            case "376":
                channel.pipeline().addLast(new Check376Handler());
                break;
            default:
                channel.pipeline().addLast(new CheckAllPurposeHandler());
                break;
        }

    }

}
