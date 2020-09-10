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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;

import java.nio.charset.Charset;

/**
 * // NOTE
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class MasterNodeHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * Sending data to the WorkerNodeServer.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ErrorUtils.print(this.getClass()).info("MasterNodeClient and WorkerNodeServer [" + ctx.channel().localAddress() + "] channel active.");
        String sendInfo = "Hello this is master node client";
        ctx.writeAndFlush(Unpooled.copiedBuffer(sendInfo, CharsetUtil.UTF_8));
    }

    /**
     * When the client takes the initiative to disconnect the link from the server, the channel is inactive. That is to say, the communication channel between the client and the server is closed and the data can not be transmitted.
     */
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ErrorUtils.print(this.getClass()).info("MasterNodeClient and WorkerNodeServer [" + ctx.channel().localAddress() + "] channel inactive.");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        ByteBuf buf = msg.readBytes(msg.readableBytes());
        System.out.println("Result: " + buf.toString(Charset.forName("utf-8")));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        ErrorUtils.print(this.getClass()).error(cause.getMessage());
    }
}