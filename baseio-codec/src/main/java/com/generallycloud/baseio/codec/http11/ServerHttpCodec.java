/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.codec.http11;

import java.io.IOException;
import java.util.List;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;

/**
 * @author wangkai
 *
 */
public class ServerHttpCodec extends AbstractHttpCodec{

    private static final byte[] PROTOCOL   = "HTTP/1.1 ".getBytes();
    private static final byte[] SERVER_CL  = "\r\nServer: baseio/0.0.1\r\nContent-Length: ".getBytes();
    private static final byte[] SET_COOKIE = "Set-Cookie:".getBytes();
    private int bodyLimit      = 1024 * 512;
    private int headerLimit    = 1024 * 8;
    private int websocketLimit = 1024 * 8;

    public ServerHttpCodec() {
    }

    public ServerHttpCodec(int headerLimit, int bodyLimit) {
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
    }
    
    public ServerHttpCodec(int headerLimit, int bodyLimit, int websocketLimit) {
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
        this.websocketLimit = websocketLimit;
    }
    
    @Override
    public Future createPINGPacket(SocketSession session) {
        return null;
    }

    @Override
    public Future createPONGPacket(SocketSession session, ChannelFuture ping) {
        return null;
    }

    @Override
    public ChannelFuture decode(SocketChannel channel, ByteBuf buffer) throws IOException {
        return new ServerHttpFuture(channel, headerLimit, bodyLimit);
    }
    
    private void encode(ByteBufAllocator allocator, ServerHttpFuture f, int length, byte[] array)
            throws IOException {
        ByteBuf buf = allocator.allocate(256);
        try {
            buf.put(PROTOCOL);
            buf.put(f.getStatus().getHeaderBinary());
            buf.put(SERVER_CL);
            buf.put(String.valueOf(length).getBytes());
            buf.putByte(R);
            buf.putByte(N);
            writeHeaders(f, buf);
            List<Cookie> cookieList = f.getCookieList();
            if (cookieList != null) {
                for (Cookie c : cookieList) {
                    writeBuf(buf, SET_COOKIE);
                    writeBuf(buf, c.toString().getBytes());
                    writeBuf(buf, R);
                    writeBuf(buf, N);
                }
            }
            writeBuf(buf, R);
            writeBuf(buf, N);
            if (length != 0) {
                writeBuf(buf, array, 0, length);
            }
        } catch (Exception e) {
            ReleaseUtil.release(buf);
            throw e;
        }
        f.setByteBuf(buf.flip());
    }
    
    @Override
    public void encode(SocketChannel channel, ChannelFuture readFuture) throws IOException {
        ByteBufAllocator allocator = channel.getByteBufAllocator();
        ServerHttpFuture f = (ServerHttpFuture) readFuture;
        if (f.isUpdateWebSocketProtocol()) {
            channel.setProtocolCodec(WebSocketCodec.WS_PROTOCOL_CODEC);
            channel.getSession().setAttribute(WebSocketFuture.SESSION_KEY_SERVICE_NAME, f.getFutureName());
        }
        f.setResponseHeader("Date",
                HttpHeaderDateFormat.getFormat().format(System.currentTimeMillis()));
        ByteArrayBuffer os = f.getBinaryBuffer();
        if (os != null) {
            encode(allocator, f, os.size(), os.array());
            return;
        }
        int writeSize = f.getWriteSize();
        if (writeSize == 0) {
            encode(allocator, f, 0, null);
            return;
        }
        encode(allocator, f, writeSize, f.getWriteBuffer());
    }

    @Override
    public String getProtocolId() {
        return "HTTP1.1";
    }

    @Override
    public void initialize(SocketChannelContext context) {
        WebSocketCodec.init(context, websocketLimit);
    }
    
}
