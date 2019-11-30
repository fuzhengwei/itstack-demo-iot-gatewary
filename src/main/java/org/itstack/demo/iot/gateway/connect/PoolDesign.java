package org.itstack.demo.iot.gateway.connect;

import org.itstack.demo.iot.gateway.freeconfig.GetProperties;
import org.itstack.demo.iot.gateway.link.LinkToMonitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description:
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年8月30日 上午10:01:39
 */
public class PoolDesign {

    public Map<String, Integer> masterMap = new HashMap<String, Integer>();
    private ExecutorService executorService = Executors.newFixedThreadPool(50);

    public void getMaster() {
        String iports = GetProperties.getValue("master.ip_port");
        System.out.println("read master config:" + iports);
        String[] ips = iports.split(",");
        for (int i = 0; i < ips.length; i++) {
            String serverip = ips[i];
            int port = Integer.parseInt(serverip.split(":")[1]);
            masterMap.put(serverip.split(":")[0] + i, port);
        }
    }

    public void start() {
        //client
        ToClientDesign up = new ToClientDesign(1);
        executorService.execute(up);

        //master
        getMaster();
        int masterid = 11;
        Iterator<Entry<String, Integer>> entries = masterMap.entrySet().iterator();
        while (entries.hasNext()) {
            Entry<String, Integer> entry = entries.next();
            String ip = entry.getKey();
            ip = ip.substring(0, ip.length() - 1);
            int port = entry.getValue();
            ToMasterDesign down = new ToMasterDesign(masterid, ip, port);
            masterid++;
            executorService.execute(down);
        }
        //link
        LinkToMonitor link = new LinkToMonitor();
        executorService.execute(link);
    }

}
