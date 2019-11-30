/**
 *
 */
package org.itstack.demo.iot.gateway.jedis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.SortingParams;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @Description:
 * 集群中keys、del(多个值)、mset(多个值)废弃
 * 代码不提供flushDB等操作
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年8月27日 下午3:47:12
 */
@Component
public class Jedisclusters {
    @Autowired
    private JedisCluster jedisCluster;//jedisclusterBean启动时连接，这里获取到连接后的对象
//	https://blog.csdn.net/yifanSJ/article/details/79561791
//	https://www.cnblogs.com/c-xiaohai/p/8376364.html


    ////////////keys:value/////////////////
    public Boolean exists(String key) {
        return jedisCluster.exists(key);//判断某个键是否存在
    }

    public String set(String key, String value) {
        return jedisCluster.set(key, value);//新增<key,value>的键值对，返回字符串OK
    }

    public Long del(String key) {
        return jedisCluster.del(key);//删除键 成功返回1
    }

    public Long expire(String key, int second) {
        return jedisCluster.expire(key, second);//设置键key的过期时间为second秒，成功返回1
    }

    public Long ttl(String key) {
        return jedisCluster.ttl(key);//查看键key的剩余生存时间
    }

    public Long persist(String key) {
        return jedisCluster.persist(key);//移除键key的生存时间，成功返回1
    }

    public String type(String key) {
        return jedisCluster.type(key);//查看键key所存储的值的类型
    }

    public String get(String key) {
        return jedisCluster.get(key);//获取key的值
    }

    public Long append(String key, String value2) {
        return jedisCluster.append(key, value2);//在值后追加新的值【value1 + value2】
    }

    public String mset(String key, String value, String key2, String value2) {
        return jedisCluster.mset(key, value, key2, value2);//增加多个键值对,成功返回OK//且这样定义具体用再说
    }

    public List<String> mget(String key1, String key2) {
        return jedisCluster.mget(key1, key2);//获取多个键值对[]
    }

    public Long del(String key1, String key2) {
        return jedisCluster.del(new String[]{key1, key2});//删除多个键值对，返回删除的个数
    }

    public Long setnx(String key, String value) {
        return jedisCluster.setnx(key, value);//新增键值对防止覆盖原先值,返回1/0,1表示正常写入，0表示已有值，无法写入
    }

    public String setex(String key, String value, int second) {
        return jedisCluster.setex(key, second, value);//新增键值对并设置有效时间
    }

    public String getSet(String key, String value) {
        return jedisCluster.getSet(key, value);//获取原值，更新为新值
    }

    public String getrange(String key, int start, int end) {
        return jedisCluster.getrange(key, start, end);//获得key的值的子串，前闭后闭，(2，4)，表示2，3，4这3个字符
    }

    /***
     * 整数和浮点数
     * jedis.get("key1")
     */
    public Long incr(String key) {
        return jedisCluster.incr(key);//key的值加1
    }

    public Long decr(String key) {
        return jedisCluster.decr(key);//key的值减1
    }

    public Long incrBy(String key, int num) {
        return jedisCluster.incrBy(key, num);//将key的值加上整数num
    }

    public Long decrBy(String key, int num) {
        return jedisCluster.decrBy(key, num);//将key的值减去整数num
    }

    /////////////列表list////////////////////
    public Long lpush(String key, String[] value) {
//      jedis.lpush("collections", "ArrayList", "Vector", "Stack", "LinkedHashMap");
        return jedisCluster.lpush(key, value); /////////////////////单独查一查
    }

    public Long rpush(String key, String[] value) {
        return jedisCluster.rpush(key, value); //添加元素，从列表右端，与lpush相对应
    }

    public Long llen(String key) {
        return jedisCluster.llen(key);
    }

    public List<String> lrange(String key, int start, int end) {
        return jedisCluster.lrange(key, start, end);//自动int向上转换，end=-1，表示倒数第一个数据，-2是倒数第二
    }

    public Object lrem(String key, int count, String value) {
        return jedisCluster.lrem(key, count, value);//删除指定元素个数
    }

    public String ltrim(String key, int start, int end) {
        return jedisCluster.ltrim(key, start, end);//删除下表0-3区间之外的元素
    }

    public String lpop(String key) {
        return jedisCluster.lpop(key); //列表出栈（左端）
    }

    public String rpop(String key) {
        return jedisCluster.rpop(key); //列表出栈（右端）
    }

    public String lset(String key, int index, String value) {
        return jedisCluster.lset(key, index, value);//修改指定下标的内容
    }

    public String lindex(String key, int index) {
        return jedisCluster.lindex(key, index);//获取指定下标的内容
    }

    public List<String> sort(String key) {
        return jedisCluster.sort(key);//列表排序
    }

    public List<String> sortasc(String key) {
        SortingParams sortingParameters = new SortingParams();
        return jedisCluster.sort(key, sortingParameters.asc());//列表排序-升序
    }

    public List<String> sortdesc(String key) {
        SortingParams sortingParameters = new SortingParams();
        return jedisCluster.sort(key, sortingParameters.desc());//列表排序-降序
    }

/////////////set集合...省略////////////////////////

    /////////////散列hash////////////////////////public Object   jedisCluster
    public String hmset(String hash, Map<String, String> map) {
        return jedisCluster.hmset(hash, map);//直接添加map
    }

    public Long hset(String hash, String key, String value) {
        return jedisCluster.hset(hash, key, value);//一个值一个值的添加
    }

    public Map<String, String> hgetAll(String hash) {
        return jedisCluster.hgetAll(hash);//散列hash的所有键值对为
    }

    public Set<String> hkeys(String hash) {
        return jedisCluster.hkeys(hash);//散列hash的所有键为
    }

    public List<String> hvals(String hash) {
        return jedisCluster.hvals(hash);//散列hash的所有值为
    }

    public Long hdel(String hash, String key) {
        return jedisCluster.hdel(hash, key);//删除一个或者多个键值对
    }

    public Long hlen(String hash) {
        return jedisCluster.hlen(hash);//散列hash中键值对的个数
    }

    public Boolean hexists(String hash, String key) {
        return jedisCluster.hexists(hash, key);//散列hash中键值对的个数
    }

    public List<String> hmget(String hash, String key) {
        return jedisCluster.hmget(hash, key);//获取hash中的值
    }


    /**
     * 默认不提供，keys和flushdb类似
     1、scan在集群情况下不可用。
     2、集群情况下的keys命令需要自行封装，默认的JedisCluster是不提供的，
     可见RedisConnect.class
     **/
    public static TreeSet<String> keys(String pattern, JedisCluster cluster) {
        TreeSet<String> keys = new TreeSet<>();
        Map<String, JedisPool> clusterNodes = cluster.getClusterNodes();
        for (String k : clusterNodes.keySet()) {
            JedisPool jp = clusterNodes.get(k);
            Jedis connection = jp.getResource();
            try {
                keys.addAll(connection.keys(pattern));
            } catch (Exception e) {
            } finally {
                connection.close();//用完一定要close这个链接！！！  
            }
        }
        return keys;
    }


}
