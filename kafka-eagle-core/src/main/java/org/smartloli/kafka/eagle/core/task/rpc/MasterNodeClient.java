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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.smartloli.kafka.eagle.core.task.rpc.handler.MasterNodeHandler;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * // NOTE
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class MasterNodeClient {
    private final String host;
    private final int port;

    public MasterNodeClient() {
        this(0);
    }

    public MasterNodeClient(int port) {
        this("localhost", port);
    }

    public MasterNodeClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(this.host, this.port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringEncoder(Charset.forName("UTF-8")));
                            ch.pipeline().addLast(new MasterNodeHandler());
                            ch.pipeline().addLast(new ByteArrayEncoder());
                            ch.pipeline().addLast(new ChunkedWriteHandler());

                        }
                    });

            ChannelFuture cf = b.connect().sync();
            System.out.println("WorkerNodeServer has connected");

            cf.channel().closeFuture().sync();
            System.out.println("WorkerNodeServer has closed");

        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        new MasterNodeClient("127.0.0.1", 8888).start();
    }
}
