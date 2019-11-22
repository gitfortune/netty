package com.example.netty.chat;

import com.google.gson.Gson;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @date : 2019/11/21
 */
@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    Map<ChannelId,String> nameMap = new HashMap<>(50);
    /**
     * 一个 ChannelGroup 代表一个直播频道
     */
    private static Map<Integer, ChannelGroup> channelGroupMap = new ConcurrentHashMap<Integer,ChannelGroup>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                TextWebSocketFrame msg) throws Exception { // (1)
        Channel incoming = ctx.channel();

        Room room = new Gson().fromJson(msg.text(), Room.class);
        int roomId = room.getRoomId();

        // 房间列表中如果不存在则为该频道,则新增一个频道 ChannelGroup
        if (!channelGroupMap.containsKey(roomId)) {
            channelGroupMap.put(roomId, new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        }
        // 确定有房间号,才将客户端加入到频道中
        channelGroupMap.get(roomId).add(ctx.channel());
        ChannelGroup channelsGroup = channelGroupMap.get(roomId);

        if(!nameMap.containsKey(incoming.id())){
            nameMap.put(incoming.id(),ChineseName.getName());
        }

        for (Channel channel : channelsGroup) {
            if (channel != incoming){
                channel.writeAndFlush(new TextWebSocketFrame("[" + nameMap.get(incoming.id()) + "]:" + room.getMsg()));
            } else {
                channel.writeAndFlush(new TextWebSocketFrame("[我("+nameMap.get(incoming.id())+")]:" + room.getMsg() ));
            }
        }

    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)
        Channel incoming = ctx.channel();
        // Broadcast a message to multiple Channels
        incoming.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 加入"));

        System.out.println("Client:"+incoming.remoteAddress() +"加入");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
        Channel incoming = ctx.channel();

        // Broadcast a message to multiple Channels
        incoming.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 离开"));

        System.out.println("Client:"+incoming.remoteAddress() +"离开");

        // A closed Channel is automatically removed from ChannelGroup,
        // so there is no need to do "channels.remove(ctx.channel());"
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
        Channel incoming = ctx.channel();
        System.out.println("Client:"+incoming.remoteAddress()+"在线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
        System.out.println("Client:"+incoming.remoteAddress()+"掉线");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("Client:"+incoming.remoteAddress()+"异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }

}
