package org.itstack.demo.iot.gateway.divpro;

import io.netty.channel.Channel;
import org.itstack.demo.iot.gateway.connect.ConfigDesign;

/**
 * @Description: 主站槽分区
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年9月11日 下午5:28:51
 */
public class MasterSlotsPartition {
    public static MasterSlotsPartition mster = new MasterSlotsPartition();

    public static MasterSlotsPartition getInstance() {
        return mster;
    }

    //分片
    public Channel getActiveMaster() {
        Channel channel = null;
        int masterSize = ConfigDesign.Server_IP_CONNECT.size();//主站个数，从1开始
        int masterIndex = ConfigDesign.MASTER_INDEX;//当前步数 +无穷
        int pos = 0;//要发往的主站编号
        for (int i = 0; i < masterSize; i++) {
            if (masterIndex >= i * 1000 && masterIndex < (i + 1) * 1000) {
                pos = i;
            }
            if (masterIndex == masterSize * 1000) {
                ConfigDesign.MASTER_INDEX = 0;
            }
        }
        ConfigDesign.MASTER_INDEX++;//放在置0后面
        for (int i = 0; i < masterSize; i++) {
            channel = ConfigDesign.Server_IP_CONNECT.get("" + (pos + 1));
            System.out.println("当前线程连接的master：" + channel.remoteAddress().toString());
            if (channel.isActive()) {//isOpen()、isRegistered()、isActive()和isWritable()
                return channel;
            } else {
                pos++;
                if (pos == masterSize) {
                    pos = 0;
                }
                continue;
            }

        }
        return channel;
    }

    //轮询 效率没有分片高
    public static Channel getMasterAverage() {
        Channel channel = null;
        int masterSize = ConfigDesign.Server_IP_CONNECT.size();
        int masterIndex = ConfigDesign.MASTER_INDEX;
        for (int i = 0; i < masterSize; i++) {
            channel = ConfigDesign.Server_IP_CONNECT.get(masterIndex);
            ConfigDesign.MASTER_INDEX++;
            masterIndex++;
            if (masterIndex >= masterSize) {
                ConfigDesign.MASTER_INDEX = 0;
            }
            if (!channel.isActive()) {
                continue;
            }
            return channel;
        }
        return channel;
    }

}
