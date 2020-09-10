/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.core.task.rpc.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;

import java.io.UnsupportedEncodingException;

/**
 * // NOTE
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class WorkerNodeHandler extends ChannelInboundHandlerAdapter {

    /**
     * When the client actively links the server link, the channel is active. In other words, the client and the server have established a communication channel and can transmit data.
     */
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ErrorUtils.print(this.getClass()).info(ctx.channel().localAddress().toString() + " channel active.");
    }

    /**
     * When the client takes the initiative to disconnect the link from the server, the channel is inactive. That is to say, the communication channel between the client and the server is closed and the data can not be transmitted.
     */
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ErrorUtils.print(this.getClass()).info(ctx.channel().localAddress().toString() + " channel inactive.");
    }

    private String getMessage(ByteBuf buf) {
        byte[] con = new byte[buf.readableBytes()];
        buf.readBytes(con);
        try {
            return new String(con, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Read the information sent by the master server
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        String rev = getMessage(buf);
        // Deal with something

    }

    /**
     * Operation after reading the data sent by the master client.
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("The server has finished receiving data.");
        // 第一种方法：写一个空的buf，并刷新写出区域。完成后关闭sock channel连接。
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Abnormal operation on the server
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        ErrorUtils.print(this.getClass()).error(cause.getMessage());
    }
}
