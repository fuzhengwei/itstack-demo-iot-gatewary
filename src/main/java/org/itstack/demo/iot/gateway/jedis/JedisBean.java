/**
 *
 */
package org.itstack.demo.iot.gateway.jedis;

import org.itstack.demo.iot.gateway.freeconfig.GetProperties;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * @Description: redisTemplate是单机版/jedisCluster是集群版
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年8月27日 下午3:53:22
 */
public class JedisBean {
    // 集群的配置

    private String clusterNodes = GetProperties.getValue("redis.cluster.node");
    private String password = GetProperties.getValue("redis.cluster.password");
    private Integer database = Integer.parseInt(GetProperties.getValue("redis.cluster.database"));
    private Integer timeout = Integer.parseInt(GetProperties.getValue("redis.cluster.timeout"));
    private Integer maxIdle = Integer.parseInt(GetProperties.getValue("redis.jedis.pool.max-idle"));
    private Integer minIdle = Integer.parseInt(GetProperties.getValue("redis.jedis.pool.min-idle"));
    private Integer maxTotal = Integer.parseInt(GetProperties.getValue("redis.jedis.pool.max-active"));
    private Integer maxWaitMillis = Integer.parseInt(GetProperties.getValue("redis.jedis.pool.max-wait"));

    @Bean
    public JedisCluster getJedisCluster() {
        String[] cNodes = clusterNodes.split(",");
        Set<HostAndPort> nodes = new HashSet<>();
        // 分割出集群节点
        for (String node : cNodes) {
            String[] hp = node.split(":");
            nodes.add(new HostAndPort(hp[0], Integer.parseInt(hp[1])));
        }
        // 创建集群对象
        return new JedisCluster(nodes, maxWaitMillis, 1000, 3, password, getPoolConfig());
    }

    private JedisPoolConfig getPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(maxIdle);//最大能够保持空闲状态的链接数
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        //最大建立连接等待时间。如果超过此时间将接到异常。设为-1表示无限制。maxWaitMillis
        //当调用borrow Object方法时，是否进行有效性检查   testOnBorrow=true
        //客户端超时时间单位是毫秒 默认是2000 redis.timeout=10000
        //连接池的最大数据库连接数redis.maxActive=600
        return poolConfig;
    }

}
