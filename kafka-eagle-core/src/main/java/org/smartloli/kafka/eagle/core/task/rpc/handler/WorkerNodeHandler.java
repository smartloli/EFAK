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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.smartloli.kafka.eagle.common.util.AppUtils;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.core.task.shard.ShardSubScan;
import org.smartloli.kafka.eagle.core.task.strategy.KSqlStrategy;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Receive and execute the assigned tasks of master.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class WorkerNodeHandler extends ChannelInboundHandlerAdapter {

    private KSqlStrategy ksql;
    private String type;

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
        if (isJson(rev)) {
            JSONObject object = JSON.parseObject(rev);
            if (object.getString(KConstants.Protocol.KEY).equals(KConstants.Protocol.HEART_BEAT)) {//
                this.type = KConstants.Protocol.HEART_BEAT;
            } else if (object.getString(KConstants.Protocol.KEY).equals(KConstants.Protocol.KSQL_QUERY)) {
                this.type = KConstants.Protocol.KSQL_QUERY;
                this.ksql = object.getObject(KConstants.Protocol.VALUE, KSqlStrategy.class);
                // this.ksql = JSON.parseObject(rev, KSqlStrategy.class);
            }
        }
    }

    public boolean isJson(String rev) {
        try {
            JSON.parse(rev);
            return true;
        } catch (Exception e) {
            ErrorUtils.print(this.getClass()).error("Parse rev[" + rev + "] to json has error, msg is ", e);
            return false;
        }
    }

    /**
     * Operation after reading the data sent by the master client.
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (KConstants.Protocol.HEART_BEAT.equals(this.type)) {//
            JSONObject object = new JSONObject();
            object.put("mem_used", AppUtils.getInstance().getProcessMemUsed());
            object.put("mem_max", AppUtils.getInstance().getProcessMemMax());
            object.put("cpu", AppUtils.getInstance().getProcessCpu());
            JSONArray array = new JSONArray();
            array.add(object);
            List<JSONArray> results = new ArrayList<>();
            results.add(array);
            ctx.writeAndFlush(Unpooled.copiedBuffer(results.toString(), CharsetUtil.UTF_8)).addListener(ChannelFutureListener.CLOSE);
        } else if (KConstants.Protocol.KSQL_QUERY.equals(this.type)) {
            if (this.ksql != null) {
                ctx.writeAndFlush(Unpooled.copiedBuffer(ShardSubScan.query(ksql).toString(), CharsetUtil.UTF_8)).addListener(ChannelFutureListener.CLOSE);
            }
        }

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
