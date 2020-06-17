package com.zmg.tools.client;


import com.zmg.tools.util.CommUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;


@Component
public class UDPClient {
    private Channel channel;
    private EventLoopGroup group;


    public UDPClient(){
    }


    @PostConstruct
    public void start(){
        new Thread(()->{
            group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioDatagramChannel.class).handler(new ServerHandler());
            try {
                channel = bootstrap.bind(0).sync().channel();
                channel.closeFuture().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                group.shutdownGracefully();
            }
        },"UDPClientMainThread").start();

    }


    public void send(byte[] buf, int startIndex,int endIndex,String hostName,int port) throws Exception{
        while(channel == null){
            Thread.sleep(20);
        }
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(buf,startIndex,endIndex);

        try {
            channel.writeAndFlush(new DatagramPacket(byteBuf, new InetSocketAddress(hostName,port))).sync();
        } catch (InterruptedException e) {
            throw e;
        }
    }



    @ChannelHandler.Sharable
    public class ServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
            System.out.println(datagramPacket.content().toString(CharsetUtil.UTF_8));
        }
    }

    public static void main(String[] args) throws Exception{
        UDPClient client = new UDPClient();
        client.start();
        byte[] bytes = CommUtils.createMagicPacket("80FA5B566D9B");
        client.send(bytes,0,bytes.length,"10.1.12.222",7003);
    }

}