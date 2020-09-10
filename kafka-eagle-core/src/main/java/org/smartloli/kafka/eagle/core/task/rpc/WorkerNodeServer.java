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
package org.smartloli.kafka.eagle.core.task.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringEncoder;
import org.smartloli.kafka.eagle.core.task.rpc.handler.WorkerNodeHandler;

import java.nio.charset.Charset;

/**
 * // NOTE
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class WorkerNodeServer {
    private final int port;

    public WorkerNodeServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.option(ChannelOption.SO_BACKLOG, 1024);
            sb.group(group, bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(this.port)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            ch.pipeline().addLast(new StringEncoder(Charset.forName("UTF-8")));
                            ch.pipeline().addLast(new WorkerNodeHandler());
                            ch.pipeline().addLast(new ByteArrayEncoder());
                        }
                    });
            ChannelFuture cf = sb.bind().sync();
            System.out.println(WorkerNodeServer.class + " Started and listening from master task: " + cf.channel().localAddress());
            cf.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        new WorkerNodeServer(8888).start();
    }
}
