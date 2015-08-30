/*
 *
 * The MIT License (MIT)
 * 
 * Copyright (C) 2013 Man YUAN <epsilon@epsilony.net>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.epsilony.utils.codec.modbus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import net.epsilony.utils.codec.modbus.handler.ResponseEventBus;
import net.epsilony.utils.codec.modbus.reqres.ModbusRequest;
import net.epsilony.utils.codec.modbus.reqres.ModbusResponse;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class SimpModbusMasterChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static long DEFAULT_REQUEST_LIFETIME = 5000;
    private Map<Integer, Entry> requestRecorder = new LinkedHashMap<>();
    private long requestLifeTime = DEFAULT_REQUEST_LIFETIME;
    private ResponseEventBus responseEventBus;
    private SocketChannel channel;

    public static class Entry {
        public final CompletableFuture<ModbusResponse> completableFuture;
        public final ModbusRequest request;
        public boolean trieved = false;

        public Entry(CompletableFuture<ModbusResponse> completableFuture, ModbusRequest request) {
            this.completableFuture = completableFuture;
            this.request = request;
        }
    }

    public void register(CompletableFuture<ModbusResponse> future, ModbusRequest request) {
        int transectionId = request.getTransectionId();
        if (requestRecorder.containsKey(transectionId)) {
            throw new TransectionIdConflictingException();
        }
        requestRecorder.put(transectionId, new Entry(future, request));
        if (requestLifeTime == 0) {
            return;
        }
        channel.eventLoop().schedule(() -> {
            Entry entry = requestRecorder.remove(transectionId);
            if (null == entry) {
                return;
            }
            entry.completableFuture.completeExceptionally(new TimeoutException(channel + " ," + request));
        } , requestLifeTime, TimeUnit.MILLISECONDS);
    }

    @Subscribe
    public void listen(ModbusResponse response) {
        Entry entry = requestRecorder.remove(response.getTransectionId());
        entry.completableFuture.complete(response);
    }

    public void clearExceptionally(Throwable ex) {
        for (Map.Entry<Integer, Entry> me : requestRecorder.entrySet()) {
            me.getValue().completableFuture.completeExceptionally(ex);
        }
        requestRecorder.clear();
    }

    public void removeExceptionally(int transectionId, Throwable ex) {
        Entry entry = requestRecorder.remove(transectionId);
        if (null == entry) {
            return;
        }
        entry.completableFuture.completeExceptionally(ex);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        channel = ch;
        responseEventBus = new ResponseEventBus();
        responseEventBus.getEventBus().register(this);
        ch.pipeline().addLast(new ModbusMasterCodec(transectionId -> {
            Entry entry = requestRecorder.get(transectionId);
            if (null == entry || entry.trieved == true) {
                return null;
            }
            entry.trieved = true;
            return entry.request;
        }), responseEventBus, new ChannelInboundHandlerAdapter() {

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {

                super.channelInactive(ctx);
            }

        });
    }

    public long getRequestLifeTime() {
        return requestLifeTime;
    }

    public void setRequestLifeTime(long requestLifeTime) {
        if (requestLifeTime < 0) {
            throw new IllegalArgumentException();
        }
        this.requestLifeTime = requestLifeTime;
    }

    public EventBus getResponseEventBus() {
        return responseEventBus.getEventBus();
    }

}
