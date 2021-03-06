package com.baec.antiviral.lib.limit;

import org.apache.commons.lang3.tuple.Pair;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 本地主机工具类
 *
 *
 */
public class LocalHostUtil {

    /**
     * 获取主机名称
     * 
     * @return
     * @throws UnknownHostException
     */
    public static String getHostName() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    /**
     * 获取系统首选IP
     * 
     * @return
     * @throws UnknownHostException
     */
    public static String getLocalIP() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    /**
     * 获取所有网卡IP，排除回文地址、虚拟地址
     * 
     * @return
     * @throws SocketException
     */
    public static String[] getLocalIPs() throws SocketException {
        List<String> list = new ArrayList<>();
        Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
        while (enumeration.hasMoreElements()) {
            NetworkInterface intf = enumeration.nextElement();
            if (intf.isLoopback() || intf.isVirtual()) { //
                continue;
            }


            Enumeration<InetAddress> inets = intf.getInetAddresses();
            while (inets.hasMoreElements()) {
                InetAddress addr = inets.nextElement();
                if (addr.isLoopbackAddress() || !addr.isSiteLocalAddress() || addr.isAnyLocalAddress()) {
                    continue;
                }
                list.add(addr.getHostAddress());
            }
        }
        return list.toArray(new String[0]);
    }


    /**
     * 获取所有的MAC和IP，，排除回文地址、虚拟地址
     * @return
     * @throws SocketException
     */
    public static List<Pair<String, String>> getMacAndIPs() throws SocketException {
        List<Pair<String, String>> list = new ArrayList<>();
        Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
        while (enumeration.hasMoreElements()) {
            NetworkInterface intf = enumeration.nextElement();
            if (intf.isLoopback() || intf.isVirtual()) { //
                continue;
            }


            Enumeration<InetAddress> inets = intf.getInetAddresses();
            while (inets.hasMoreElements()) {
                InetAddress addr = inets.nextElement();
                if (addr.isLoopbackAddress() || !addr.isSiteLocalAddress() || addr.isAnyLocalAddress()) {
                    continue;
                }
                String ip = addr.getHostAddress();
                byte[] mac = intf.getHardwareAddress();
                StringBuffer sb = new StringBuffer("");
                for(int i=0; i< mac.length; i++) {
                    if(i!=0) {
                        sb.append("-");
                    }
                    //字节转换为整数
                    int temp = mac[i]&0xff;
                    String str = Integer.toHexString(temp);
                    //System.out.println("每8位:"+str);
                    if(str.length()==1) {
                        sb.append("0"+str);
                    }else {
                        sb.append(str);
                    }
                }
                String macAddr = sb.toString();
                list.add(Pair.of(macAddr ,ip));
            }
        }
        return list;
    }



    /**
     * 判断操作系统是否是Windows
     * 
     * @return
     */
    public static boolean isWindowsOS() {
        boolean isWindowsOS = false;
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") > -1) {
            isWindowsOS = true;
        }
        return isWindowsOS;
    }



    public static void main(String[] args) {
        try {
            System.out.println("主机是否为Windows系统：" + LocalHostUtil.isWindowsOS());
            System.out.println("主机名称：" + LocalHostUtil.getHostName());
            //System.out.println("系统网卡：" + LocalHostUtil.getLocalMac());
            System.out.println("系统首选IP：" + LocalHostUtil.getLocalIP());
            LocalHostUtil.getMacAndIPs().stream().forEach(p -> System.out.println("Mac："+p.getLeft()+" , IP："+p.getRight()));
        } catch (UnknownHostException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}