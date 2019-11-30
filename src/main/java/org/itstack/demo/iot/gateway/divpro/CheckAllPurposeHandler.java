/**
 *
 */
package org.itstack.demo.iot.gateway.divpro;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.itstack.demo.iot.gateway.connect.FlowListener;
import org.itstack.demo.iot.gateway.protobuf.Message;

/**
 * @Description:
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年9月11日 下午5:10:00
 */
public class CheckAllPurposeHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = msg.toString();
        String ipport = body.split("_")[0];
        String data = body.split("_")[1];
        FlowListener.getInstance().writeFlow(ipport, data.length() / 2);

        Channel channel = MasterSlotsPartition.getInstance().getActiveMaster();
        Message mast = Message.newBuilder().setIp(ipport.split(":")[0])
                .setPort(ipport.split(":")[1]).setProtocolId(DivMultiprotocolSelection.PROTOCOL_TYPES)
                .setContent(data).build();
//		 channel.write(mast);//这都是单线程往服务端发，很影响性能，考虑中间件
        channel.writeAndFlush(mast);
    }
}
