# itstack-demo-iot-gatewary

原文连接：[基于Netty实践搭建的物联网网关iot-gatway](https://mp.weixin.qq.com/s?__biz=MzIxMDAwMDAxMw==&mid=2650724958&idx=1&sn=d26b475519c3a6baaf2e66be8b683d08&scene=19#wechat_redirect)

物联网平台是很大的一个摊子，在设计上，此次上传了关系设计图，业务框架设计图欠奉。在代码上，我目前也只是做了两版版网关，支持多规约；多规约组解服务，目前也只支持3761规约的组装和解析；接口做了一个框架，改了几版，开始就是提供jar包调用，后来改成zk+dubbo注册模式，后来改成springboot的Restful服务；数据二次处理也是搭了个框架，具体看业务。
此次开发，按个人的开发与运维经验，结合以往的采集，做了一些功能添加和效率优化，代码完全个人重构。

netty网关，支持百万客户端连接，压力测试ing...，并优化了与服务端集群通信，以往轮询往多个服务器发消息，看似消息发送很平均，其实大大影响了效率，本次对平均算法做了优化,本次上传代码添加了很多功能，摒弃了以往只做心跳维护、数据转发的功能。

socket通信功能优化，提高系统利用率，每台服务器的处理能力比之前提高30%以上

## 系统功能
- 心跳维护(增加了客户端在线，但未发心跳的处理功能)
- 链路监控
- 报文监控
- 物联网卡流量监控
- 踢掉在线终端
- 在线维护
- ip黑名单
- 定向发到集群的某台服务器
- 多规约支持（代码里就配置了645/698/376/104/二进制/MQTT这几种规约，其实支持更多，个人感觉能支持所有规约，至少目前我见到的都能支持)
- 服务端序列化传输优化（protobuf序列化后的大小是json的10分之一，xml格式的20分之一，是二进制序列化的10分之一）

## 开发环境
1、jdk1.8【jdk1.7以下只能部分支持netty】
2、Netty4.1.36.Final【netty3.x 4.x 5每次的变化较大，接口类名也随着变化】

## 代码示例

```java
itstack-demo-iot-gatewary
└── src
    ├── main
    │   └── java
    │       └── org.itstack.demo.iot.gateway
    │           ├── connect
    │           │	├── ConfigDesign.java
    │           │	├── FlowListener.java	
    │           │	├── PoolDesign.java	
    │           │	├── ToClientDesign.java	
    │           │	└── ToMasterDesign.java
    │           ├── divpro
    │           │	├── Check104Handler.java
    │           │	├── Check376Handler.java
    │           │	├── CheckAllPurposeHandler.java
	│           │	├── DivChannelInitializer.java
	│           │	├── DivMultiprotocolSelection.java
	│           │	└── DivServerHandler.java
    │           ├── freeconfig
    │           │	└── GetProperties.java
    │           ├── jedis
    │           │	├── JedisBean.java
    │           │	└── Jedisclusters.java
    │           ├── link
    │           │	└── LinkToMonitor.java
    │           ├── masterpro
    │           │	├── MasterChannelInitializer.java
    │           │	├── MasterClientHandler.java
    │           │	└── MasterDisconnectListener.java
    │           ├── protobuf
    │           │	├── MasterMessage.java
    │           │	├── MasterMessage.proto
    │           │	├── Message.java
    │           │	└── MessageOrBuilder.java
    │           ├── start
    │           │	└── StartGate.java
    │           └── util
    │           	└── BasicDataTypeTransUtils.java
    │
    └── test
         └── java
             └── org.itstack.demo.test
                 └── ApiTest.java
```

** 部分代码模块讲解，全部代码，关注公众号：bugstack虫洞栈 | 回复iot-gateway获取完整代码 **

>connect/ConfigDesign.java | 简要配置信息

```java
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
```

>divpro/MasterSlotsPartition.java | 主站槽分区

```java
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
```

>protobuf/MasterMessage.proto | 消息协议定义

```java
syntax = "proto3";

package org.itstack.demo.iot.gateway.protobuf;

option java_package = "org.itstack.demo.iot.gateway.protobuf";
option java_multiple_files = true;
option java_outer_classname = "MasterMessage";

message Message {
	string ip = 2;
	string port = 3;
	string protocolId = 4;
	string content = 5;
	string property = 6;
}
```

>test/ApiTest.java

```java
/**
 * 虫洞栈：https://bugstack.cn
 * 公众号：bugstack虫洞栈 | 关注公众号回复iot-gatewary，获取工程源码
 * Create by fuzhengwei on 2019
 */
public class ApiTest {

    public static void main(String[] args) {
        System.out.println("hi 微信公众号：bugstack虫洞栈 | 欢迎关注获取专题文章和源码");
    }
    /**
     * 编译proto文件
     * protoc.exe -I=E:\itstack\GIT\itstack.org\itstack-demo-iot-gatewary\src\main\java\org\itstack\demo\iot\gateway\protobuf --java_out=E:\itstack\GIT\itstack.org\itstack-demo-iot-gatewary\src\main\java\ MasterMessage.proto
     */
}
```

## 测试结果

>启动StartGate服务

```java
2019-09-17 16:00:37.639  INFO 5080 --- [           main] start.StartGate                          : Started StartGate in 3.989 seconds (JVM running for 4.436)
read master config:127.0.0.1:7001,127.0.0.1:7002,127.0.0.1:7003
Link Listener: [Tue Sep 17 16:00:37 CST 2019]    Master.size:0   client.size:0...
read client config:10001
bind: [Tue Sep 17 16:00:38 CST 2019]   服务器开始监听端口10001，等待客户端连接.........
Login： [Tue Sep 17 16:00:38 CST 2019]   127.0.0.1:7001
Login： [Tue Sep 17 16:00:38 CST 2019]   127.0.0.1:7003
Login： [Tue Sep 17 16:00:38 CST 2019]   127.0.0.1:7002
Link Listener: [Tue Sep 17 16:00:47 CST 2019]    Master.size:1   client.size:0...
master heartbeat: [Tue Sep 17 16:00:51 CST 2019]  127.0.0.1:7002
Link Listener: [Tue Sep 17 16:00:57 CST 2019]    Master.size:2   client.size:0...
master heartbeat: [Tue Sep 17 16:01:01 CST 2019]  127.0.0.1:7002
Link Listener: [Tue Sep 17 16:01:07 CST 2019]    Master.size:3   client.size:0...
master heartbeat: [Tue Sep 17 16:01:11 CST 2019]  127.0.0.1:7002
master heartbeat: [Tue Sep 17 16:01:11 CST 2019]  127.0.0.1:7001
收到客户端连接:/127.0.0.1:59728
Link Listener: [Tue Sep 17 16:01:17 CST 2019]    Master.size:3   client.size:1...
物联网卡流量统计(首次):127.0.0.1:59728:4B
当前线程连接的master：/127.0.0.1:7002
master heartbeat: [Tue Sep 17 16:01:19 CST 2019]  127.0.0.1:7003
物联网卡流量监控:(缓存里不带单位):8B
当前线程连接的master：/127.0.0.1:7002
物联网卡流量监控:(缓存里不带单位):12B
当前线程连接的master：/127.0.0.1:7002
master heartbeat: [Tue Sep 17 16:01:21 CST 2019]  127.0.0.1:7001
物联网卡流量监控:(缓存里不带单位):16B
当前线程连接的master：/127.0.0.1:7002
物联网卡流量监控:(缓存里不带单位):20B
当前线程连接的master：/127.0.0.1:7002
客户端断开链接:/127.0.0.1:59728
Link Listener: [Tue Sep 17 16:01:27 CST 2019]    Master.size:3   client.size:0...
master heartbeat: [Tue Sep 17 16:01:29 CST 2019]  127.0.0.1:7003
master heartbeat: [Tue Sep 17 16:01:31 CST 2019]  127.0.0.1:7001
master heartbeat: [Tue Sep 17 16:01:32 CST 2019]  127.0.0.1:7002
```

------------

![微信公众号：bugstack虫洞栈，欢迎关注&获取源码](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9idWdzdGFjay5jbi93cC1jb250ZW50L3VwbG9hZHMvMjAxOS8wOC8lRTUlOTAlOEQlRTclODklODcucG5n?x-oss-process=image/format,png)

