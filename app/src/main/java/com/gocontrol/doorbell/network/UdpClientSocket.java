package com.gocontrol.doorbell.network;
/**
 * Copyright 2015
 * All right reserved.
 * UTP服务类.
 * @author
 * @version 1.0
 * Creation date: 下午10:32:31
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;


public class UdpClientSocket {
    private byte[] buffer = new byte[1024];
    
    private DatagramSocket ds = null;

    private DatagramPacket packet = null;

    private InetSocketAddress socketAddress = null;

    private String orgIp;

    /**
     * 构造函数，绑定主机和端口.
     * @param host 主机
     * @param port 端口
     * @throws Exception
     */
    public UdpClientSocket(String host, int port) throws Exception {
        socketAddress = new InetSocketAddress(host, port);
        ds = new DatagramSocket(socketAddress);
        System.out.printf(this.toString() + " == " +"服务端启动!");
    }
    /**
     * 构造函数，绑定主机和端口.
     * @param port 端口
     * @throws Exception
     */
    public UdpClientSocket(int port) throws Exception {
    	//InetAddress host = InetAddress.getByName("127.0.0.1");
        //socketAddress = new InetSocketAddress(host, port);
        //ds = new DatagramSocket(socketAddress);
    	ds = new DatagramSocket(port);
        System.out.printf(this.toString() + " == " +"服务端启动!");
    }
    
    public final String getOrgIp() {
        return orgIp;
    }

    /**
     * 设置超时时间，该方法必须在bind方法之后使用.
     */
    public final void setSoTimeout(int timeout) throws Exception {
        ds.setSoTimeout(timeout);
    }

    /**
     * 获得超时时间.
     * @return 返回超时时间.
     * @throws Exception
     */
    public final int getSoTimeout() throws Exception {
        return ds.getSoTimeout();
    }

    /**
     * 绑定监听地址和端口.
     * @param host 主机IP
     * @param port 端口
     */
    public final void bind(String host, int port) throws SocketException {
        socketAddress = new InetSocketAddress(host, port);
        ds = new DatagramSocket(socketAddress);
    }


    /**
     * 接收数据包，该方法会造成线程阻塞.
     * @return 返回接收的数据串信息
     * @throws IOException
     */
    public final byte[] receive() throws IOException {
        packet = new DatagramPacket(buffer, 0, buffer.length);
        ds.receive(packet);
        orgIp = packet.getAddress().getHostAddress();
        
        UdpClientODP.getInstance().setServerAddress(orgIp);
       
        int len = packet.getLength();
        System.out.printf("received UDP data length:%d\n", len );
        
        byte[] info = new byte[len];
        System.arraycopy(packet.getData(), 0 , info, 0, len);
        return info;
    }

    /**
     * 将响应包发送给请求端.
     * @param bytes 回应报文
     * @throws IOException
     */
    public final void response(String info) throws IOException {
        System.out.printf(this.toString() + " == " +"客户端地址 : " + packet.getAddress().getHostAddress()
                + ",端口：" + packet.getPort());
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length, packet
                .getAddress(), packet.getPort());
        dp.setData(info.getBytes());
        ds.send(dp);
    }

    public final void sendData(byte[] info , String serverIP, int port) throws IOException {
        
        DatagramPacket dp = new DatagramPacket(info, info.length, InetAddress.getByName(serverIP), port);
        dp.setData(info);
        ds.send(dp);
    }
    /**
     * 设置报文的缓冲长度.
     * @param bufsize 缓冲长度
     */
    public final void setLength(int bufsize) {
        packet.setLength(bufsize);
    }

    /**
     * 获得发送回应的IP地址.
     * @return 返回回应的IP地址
     */
    public final InetAddress getResponseAddress() {
        return packet.getAddress();
    }

    /**
     * 获得回应的主机的端口.
     * @return 返回回应的主机的端口.
     */
    public final int getResponsePort() {
        return packet.getPort();
    }

    /**
     * 关闭udp监听口.
     * @author
     * Creation date: 
     */
    public final void close() {
        try {
            ds.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 测试方法.
     * @param args
     * @throws Exception
     * @author
     * Creation date: 
     */
    public static void main(String[] args) throws Exception {
        String serverHost = "127.0.0.1";
        int serverPort = 3344;
        UdpClientSocket udpServerSocket = new UdpClientSocket(serverHost, serverPort);
        while (true) {
            udpServerSocket.receive();
            udpServerSocket.response("hello,sterning!");
            
        }
    }
}