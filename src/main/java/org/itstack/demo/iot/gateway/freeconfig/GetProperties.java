/**
 *
 */
package org.itstack.demo.iot.gateway.freeconfig;

import java.io.IOException;
import java.util.Properties;

/**
 * @Description:
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年9月16日 下午6:22:11
 */
public class GetProperties {

/*	public static Map<String,String> redismap = new HashMap<String,String>();
	public static String masteripport;
	public static String clientport;*/

    private static Properties properties = null;

    // 初始化
    static {
        properties = new Properties();
        try {
            properties.load(GetProperties.class.getClassLoader().getResourceAsStream("./commonConfig.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
      /*  
        // 返回Properties中包含的key-value的Set视图  
        Set<Entry<Object, Object>> set = properties.entrySet();  
        // 返回在此Set中的元素上进行迭代的迭代器  
        Iterator<Map.Entry<Object, Object>> it = set.iterator();  
        String key = null, value = null;  
        
        // 循环取出key-value  
        while (it.hasNext()) {  
  
            Entry<Object, Object> entry = it.next();  
  
            key = String.valueOf(entry.getKey());  
            value = String.valueOf(entry.getValue());  
            System.out.println(key +"==="+value);
            
        }
        
        masteripport = GetProperties.getValue("master.ip_port");
        clientport = GetProperties.getValue("client.port");*/
    }

    // 获取值
    public static String getValue(String key) {
        return properties.getProperty(key);
    }

    public static void main(String[] args) {
        String value = GetProperties.getValue("author.name");
        System.out.print("username=" + value);
    }

}
