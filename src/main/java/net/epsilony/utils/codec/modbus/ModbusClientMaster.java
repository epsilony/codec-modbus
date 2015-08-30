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

import java.rmi.ConnectException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ChannelFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.epsilony.utils.codec.modbus.reqres.ModbusRequest;
import net.epsilony.utils.codec.modbus.reqres.ModbusResponse;

/**
 * @author <a href="mailto:epsilony@epsilony.net">Man YUAN</a>
 *
 */
public class ModbusClientMaster {

    private Bootstrap bootstrap;
    private ChannelFuture connectFuture;
    private Channel channel;
    private ReentrantLock lock = new ReentrantLock();
    protected SimpModbusMasterChannelInitializer initializer;

    private TransectionIdDispatcher transectionIdDispatcher = new SimpTransectionIdDispatcher();
    private EventLoopGroup group;
    private int inetPort;
    private String innetAddress;

    private boolean keepAlive = true;
    private int socketTimeout = 5000;
    private int connectTimeout = 5000;
    private long requestLifeTime = 5000;
    private boolean tcpNoDelay = true;
    private ChannelFactory<Channel> channelFactory = new ChannelFactory<Channel>() {

        @Override
        public Channel newChannel() {
            return new NioSocketChannel();
        }
    };

    protected ChannelFuture genConnectFuture() {

        initializer = new SimpModbusMasterChannelInitializer();
        initializer.setRequestLifeTime(requestLifeTime);
        bootstrap = new Bootstrap();
        bootstrap.group(group).channelFactory(channelFactory).handler(initializer)
                .option(ChannelOption.TCP_NODELAY, tcpNoDelay).option(ChannelOption.SO_KEEPALIVE, keepAlive)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .option(ChannelOption.SO_TIMEOUT, socketTimeout);
        return bootstrap.connect(innetAddress, inetPort);
    }

    public CompletableFuture<ModbusResponse> request(ModbusRequest req) {
        final CompletableFuture<ModbusResponse> result = new CompletableFuture<>();

        result.whenComplete((res, ex) -> {
            if (transectionIdDispatcher != null) {
                int transectionId = req.getTransectionId();
                if (transectionId >= 0) {
                    transectionIdDispatcher.repay(transectionId);
                }
            }
        });

        lock.lock();
        try {
            if (connectFuture == null) {
                connectFuture = genConnectFuture();
                connectFuture.addListener(new GenericFutureListener<Future<? super Void>>() {

                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        lock.lock();
                        try {
                            if (future.isSuccess()) {
                                channel = connectFuture.channel();
                                channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {

                                    @Override
                                    public void operationComplete(Future<? super Void> future) throws Exception {
                                        lock.lock();
                                        try {
                                            connectFuture = null;
                                            initializer
                                                    .clearExceptionally(new ConnectException("connection is closed!"));
                                            transectionIdDispatcher.reset();
                                        } finally {
                                            lock.unlock();
                                        }
                                    }
                                });
                            } else {
                                connectFuture = null;
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                });
            }

            connectFuture.addListener(new GenericFutureListener<Future<? super Void>>() {

                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    ChannelFuture channelFuture = (ChannelFuture) future;
                    if (future.isSuccess()) {
                        if (null != transectionIdDispatcher) {
                            int transectionId = transectionIdDispatcher.borrow();
                            req.setTransectionId(transectionId);
                            if (transectionId < 0) {
                                result.completeExceptionally(new TransectionDispatcherEmptyException());
                                return;
                            }
                        }

                        try {
                            initializer.register(result, req);
                        } catch (Throwable ex) {
                            result.completeExceptionally(ex);
                            return;
                        }

                        ChannelFuture writeFuture = channelFuture.channel().writeAndFlush(req);
                        writeFuture.addListener(new GenericFutureListener<Future<? super Void>>() {

                            @Override
                            public void operationComplete(Future<? super Void> future) throws Exception {
                                if (!future.isSuccess()) {
                                    if (future.isCancelled()) {
                                        initializer.removeExceptionally(req.getTransectionId(),
                                                new ConnectException("connection is canncelled"));
                                    } else {
                                        initializer.removeExceptionally(req.getTransectionId(), future.cause());
                                    }
                                }
                            }
                        });
                    } else if (future.isCancelled()) {
                        result.completeExceptionally(new ConnectException("connection is canncelled"));
                    } else {
                        result.completeExceptionally(future.cause());
                    }
                }

            });
        } finally {
            lock.unlock();
        }

        return result;

    }

    public long getRequestLifeTime() {
        return requestLifeTime;
    }

    public void setRequestLifeTime(long requestLifeTime) {
        this.requestLifeTime = requestLifeTime;
    }

    public EventLoopGroup getGroup() {
        return group;
    }

    public void setGroup(EventLoopGroup group) {
        this.group = group;
    }

    public int getInetPort() {
        return inetPort;
    }

    public void setInetPort(int inetPort) {
        this.inetPort = inetPort;
    }

    public String getInnetAddress() {
        return innetAddress;
    }

    public void setInnetAddress(String innetAddress) {
        this.innetAddress = innetAddress;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public ChannelFactory<Channel> getChannelFactory() {
        return channelFactory;
    }

    public void setChannelFactory(ChannelFactory<Channel> channelFactory) {
        this.channelFactory = channelFactory;
    }

    public TransectionIdDispatcher getTransectionIdDispatcher() {
        return transectionIdDispatcher;
    }

    public void setTransectionIdDispatcher(TransectionIdDispatcher transectionIdDispatcher) {
        this.transectionIdDispatcher = transectionIdDispatcher;
    }

}
