/**
 *
 */
package org.itstack.demo.iot.gateway.utils;

import java.math.BigInteger;

/**
 * @ClassName: BasicDataTypeTransUtils
 * @Description:
 * @version: v1.0.0
 * @author: wbl
 * @date: 2019年8月8日 下午2:11:16
 */
public class BasicDataTypeTransUtils {
    /**
     * @MethodsName: dataTrans
     * @Description: 十进制转换， 仅局限于比较常用的二进制、八进制、十六进制
     * @param:
     * @return:
     * @date: 2019年8月8日
     */
    public static String dataTrans(int num, int radix) {
        String resu = "";//默认length=0

        if (radix == 2) {
            resu = Integer.toBinaryString(num);
            if (resu.length() < 8) {
                resu = "00000000".substring(0, (8 - resu.length())) + resu;
            }
        } else if (radix == 8) {
            resu = Integer.toOctalString(num);
        } else if (radix == 16) {
            resu = Integer.toHexString(num);
            if (resu.length() == 1) {
                resu = "0" + resu;
            }
            resu = resu.toUpperCase();
        }
        return resu;
    }

    //    16进制字符串转10进制
    public static String dataTransLong16(String arg) {
        long str = Long.parseLong(arg, 16);
        return "" + str;
    }

    //    16进制字符串转10进制
    public static String dataTrans16(String arg) {
        int uint = Integer.parseInt(arg, 16);
        return "" + uint;
    }

    //	  10进制转16进制   len是补全的位数
    public static String dataTransString16(String arg, int len) {
        String str = Integer.toHexString(dataTransString(arg));
        if (str.length() < len) {
            for (int i = 0; i < len - str.length(); i++) {
                str = "0" + str;
            }

        }
        return str;
    }


    //    Integer转int
    public static int dataTransInteger(Integer arg) {
        return Integer.valueOf(arg);
    }

    //	     字符串转int
    public static int dataTransString(String arg) {
        return Integer.parseInt(arg);
    }

    public static Long dataTransStringL(String arg) {
        return Long.parseLong(arg);
    }

    //	  ASCII转16进制字符串 且逆序
    public static String dataTransASCII(String arg) {
        char[] chars = arg.toCharArray();

        StringBuffer hex = new StringBuffer();
        for (int i = chars.length - 1; i >= 0; i--) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }

    //	  16进制字符串转ASCII且逆序
    public static String dataTransASCIIToHex(String hex) {
        String str = "";
        for (int i = 0; i < hex.length() - 1; i += 2) {
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            str = (char) decimal + str;
        }
        return str;
    }

    //  	   二进制转换为16进制
    public static String dataTransBinary(String bin) {
        int num = binaryToDecimal(bin);
        return dataTrans(num, 16);
    }

    //  	   二进制转为10进制
    public static int binaryToDecimal(String binarySource) {
        BigInteger bi = new BigInteger(binarySource, 2);    //转换为BigInteger类型
        return Integer.parseInt(bi.toString());        //转换成十进制
    }

    //  	   将int转二进制的第几位到第几位转为10进制
    public static int binaryToInt(int num, int start, int end) {
        String bnum = dataTrans(num, 2);
        String newnum = bnum.substring(start, end);
        if (newnum.length() < 8) {
            newnum = "00000000".substring(0, 8 - newnum.length()) + newnum;
        }

        return binaryToDecimal(newnum);
    }


    /**
     * @MethodsName: main
     * @Description:
     * @param:
     * @return:
     * @date: 2019年8月8日
     */
    public static void main(String[] args) {
        System.out.println(dataTrans(0, 16));

        System.out.println(dataTransASCII("DC"));
        System.out.println(dataTransASCIIToHex("4344"));


    }

}
