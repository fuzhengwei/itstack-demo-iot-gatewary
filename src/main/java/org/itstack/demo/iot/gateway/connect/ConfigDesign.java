/**
 *
 */
package org.itstack.demo.iot.gateway.connect;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年8月30日 上午11:13:13
 */
public class ConfigDesign {

    //用于分配处理业务线程的线程组个数
    public static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors();//硬件线程数
    //如果代码的瓶颈是在CPU这块的话，我会有7个线程在同时 竞争CPU周期，而不是更合理的4个线程。如果我的瓶颈是在内存这的话，那这个测试我可以获得7倍的性能提升
    //业务处理线程大小
    public static final int BIZTHREADSIZE = 4;
    //客户端与服务端通信缓存
    //用来初始化服务端可连接队列
    //服务端处理客户端连接请求是按顺序处理的，所以同一时间只能处理一个客户端连接，
    //多个客户端来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理，backlog参数指定了队列的大小
    public static final int CACHESIZE = 1024;
    //下行只有唯一标致，这里需要维护一个area和ip的对应关系,0904设计给前置ip，回也回ip，就不需要CLIENT_AREA_IP了。
    //public static Map <String,String> CLIENT_AREA_IP = new HashMap<String,String>();//adr_code : ip
    public static Map<String, Channel> CLIENT_IP_CONNECT = new HashMap<String, Channel>();//ip : channel
    //这样的问题是，一个ip可以有多个终端，即换SIM卡操作，这个无法避免,若上层设置数据库白名单就太死板了。如果唯一标识也是主键，那就只能提示终端冲突了。
    //一般情况而言，一个ip就可以说一个终端，在线维护那里，直接就替换ip对应的唯一标识，并在登陆日志提示，ip对应唯一标识改变。这解决了设备唯一性
    //如果换sim卡了，就是新设备登录，正常无影响。死终端会在数据库，但不会再redis在线列表，这就涉及到，在线设备信息同步问题了。
    //上层以后可以做一个删除客户端的操作，应用层删除数据库信息，前置删除在线终端，网关关闭终端连接。即[分手以后，是陌生人]
    //id也是从第一次的0累加的。
    public static Map<String, Channel> Server_IP_CONNECT = new HashMap<String, Channel>();//id : channel
    //连接的服务端句柄数组，采用轮询操作，
    //这里要做好服务端挂了之后，发往其他的Channel，并尝试重连服务端，即心跳维护
    public static int MASTER_INDEX = 0;//轮询map中的前置，逢map中存活的size，置零。
    //统计每个ip通信的流量，上下行都写在这，统计物联网卡的流量,这个流量也需要初始化的时候从redis读取一下
    public static Map<String, String> FLOW_COUNT = new HashMap<String, String>();//ip : flow
    //	ip黑名单监控（这种配置型的，需要加载到redis，启动时一次性从redis读取到本地）
    public static List<String> BLACKLIST = new ArrayList<String>();
    //写入的话，验证一下sync结束的finally操作。要确定一定能写进去，不会存在bug，否认换设计

}
