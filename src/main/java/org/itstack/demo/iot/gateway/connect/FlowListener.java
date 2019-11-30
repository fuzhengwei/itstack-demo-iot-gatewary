/**
 *
 */
package org.itstack.demo.iot.gateway.connect;

import org.itstack.demo.iot.gateway.utils.BasicDataTypeTransUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 流量统计
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年9月2日 下午3:47:52
 */
public class FlowListener {
    public static Map<String, String> FLOW_COUNT = new HashMap<String, String>();//ip : channel
    public static FlowListener flow = new FlowListener();

    public static FlowListener getInstance() {
        return flow;
    }

    public void writeFlow(String ip, int i) {
        String str = ConfigDesign.FLOW_COUNT.get(ip);//Byte, KB, MB, GB, TB, PB, EB, ZB, YB,DB,NB
        if (null == str) {
            if (i < 1024) {
                str = "," + "," + "," + "," + "," + "," + "," + "," + "," + "," + i + "";
                //ConfigDesign.FLOW_COUNT.put(ip, str);
            } else {
                //通道缓存设置的才1024B，这里最大转成kb
                str = "," + "," + "," + "," + "," + "," + "," + "," + "," + i / 1024 + "," + i % 1024;
            }
            ConfigDesign.FLOW_COUNT.put(ip, str);
            System.out.println("物联网卡流量统计(首次):" + ip + ":" + str.split(",")[10] + "B");
            return;
        }
        //非第一次
        String[] addkb = str.split(",");//1000MB,1023KB,1000
        //写一个回调就ok了按说，去掉单位，以‘，’分隔。

        int strlen = addkb.length;
        int b = BasicDataTypeTransUtils.dataTransString(addkb[strlen - 1]);
        int b2 = b + i;
        addkb[strlen - 1] = "" + b2;


        String[] transtring = bToBb(addkb);//[null, null, null, null, null, null, null, null, null, 1023, 1023] 翻译没了，第1024次
        String result = "";
        for (int k = 0; k < transtring.length; k++) {
            result = result + "," + transtring[k];
        }

        ConfigDesign.FLOW_COUNT.put(ip, result.substring(1));
//		System.out.println(result.substring(1));

    }

    public String[] bToBb(String[] str) {
        String[] newstr = new String[str.length];

        int flag = 0;
        int addnum = 0;
        int morenum = 0;
        String result = "";
        for (int i = str.length - 1; i > 0; i--) {
            if (null == str[i] || "".equals(str[i]) || "null".equals(str[i])) {
                break;
            }
            int b = BasicDataTypeTransUtils.dataTransString(str[i]);

            if (addnum != 0) {
                b = b + addnum;
                addnum = 0;
                morenum = 0;
            }


            if ("1022".equals(str[9]) && "1022".equals(str[10])) {
                System.err.println("暂停调试。。。");
            }

            if (b >= 1024) {
                addnum = (int) (b / 1024); //<1024k
                morenum = (int) (b % 1024);
//				flag = 1;
                newstr[i] = morenum + "";
                if ("".equals(str[i - 1])) {
                    newstr[i - 1] = "" + addnum;

                } else {
//					System.out.println(str[i-1]);
                    if ("null".equals(str[i - 1])) {
                        newstr[i - 1] = "" + addnum;
                    } else
                        newstr[i - 1] = (BasicDataTypeTransUtils.dataTransString(str[i - 1]) + addnum) + "";

                }

            } else {
                newstr[i] = b + "";
            }

        }
////Byte, KB, MB, GB, TB, PB, EB, ZB, YB,DB,NB
        if (null == newstr[0] || "".equals(newstr[0])) {
        } else {
            result += newstr[0] + "NB";
        }
        if (null == newstr[1] || "".equals(newstr[1])) {
        } else {
            result += newstr[1] + "DB";
        }
        if (null == newstr[2] || "".equals(newstr[2])) {
        } else {
            result += newstr[2] + "YB";
        }
        if (null == newstr[3] || "".equals(newstr[3])) {
        } else {
            result += newstr[3] + "ZB";
        }
        if (null == newstr[4] || "".equals(newstr[4])) {
        } else {
            result += newstr[4] + "EB";
        }
        if (null == newstr[5] || "".equals(newstr[5])) {
        } else {
            result += newstr[5] + "PB";
        }
        if (null == newstr[6] || "".equals(newstr[6])) {
        } else {
            result += newstr[6] + "TB";
        }
        if (null == newstr[7] || "".equals(newstr[7])) {
        } else {
            result += newstr[7] + "GB";
        }
        if (null == newstr[8] || "".equals(newstr[8])) {
        } else {
            result += newstr[8] + "MB";
        }
        if (null == newstr[9] || "".equals(newstr[9])) {
        } else {
            result += newstr[9] + "KB";
        }
        if (null == newstr[10] || "".equals(newstr[10])) {
        } else {
            result += newstr[10] + "B";
        }

        System.out.println("物联网卡流量监控:(缓存里不带单位):" + result);

        return newstr;
    }


    public static void main(String[] args) throws Exception {

        FlowListener flow = new FlowListener();
        Long i = 0L;
        while (i < 999999999) {
            if (i == 1024 * 1023) {
                System.out.println("11111111111111111111111111");
            }
            flow.writeFlow("123", 655370020);
            i++;
            Thread.sleep(3000);
        }

        flow.writeFlow("123", 12);
    }


}
