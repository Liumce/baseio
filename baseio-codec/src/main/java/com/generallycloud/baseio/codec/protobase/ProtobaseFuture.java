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
package com.generallycloud.baseio.codec.protobase;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.BinaryFuture;
import com.generallycloud.baseio.protocol.TextFuture;

public class ProtobaseFuture extends BinaryFuture implements TextFuture {

    private int     binaryLenLimit;
    private byte[]  binaryReadBuffer;
    private int     futureId;
    private byte    futureType;
    private boolean isBroadcast;
    private String  readText;
    private int     channelId;
    private int     textLenLimit;

    public ProtobaseFuture() {}

    ProtobaseFuture(int textLengthLimit, int binaryLengthLimit) {
        this.textLenLimit = textLengthLimit;
        this.binaryLenLimit = binaryLengthLimit;
    }

    public ProtobaseFuture(int futureId) {
        this.futureId = futureId;
    }

    public int getFutureId() {
        return futureId;
    }

    public byte getFutureType() {
        return futureType;
    }

    public byte[] getReadBinary() {
        return binaryReadBuffer;
    }

    public int getReadBinarySize() {
        if (hasReadBinary()) {
            return binaryReadBuffer.length;
        }
        return 0;
    }

    @Override
    public String getReadText() {
        return readText;
    }

    public int getChannelId() {
        return channelId;
    }

    public int getChannelKey() {
        return channelId;
    }

    public boolean hasReadBinary() {
        return binaryReadBuffer != null;
    }

    public boolean isBroadcast() {
        return isBroadcast;
    }

    private void setHeartbeat(int len) throws IOException {
        if (len == ProtobaseCodec.PROTOCOL_PING) {
            setPing();
        } else if (len == ProtobaseCodec.PROTOCOL_PONG) {
            setPong();
        } else {
            throw new IOException("illegal length:" + len);
        }
    }

    @Override
    public boolean read(NioSocketChannel channel, ByteBuf src) throws IOException {
        if (src.remaining() < 4) {
            return false;
        }
        int len = src.getInt();
        if (len < 0) {
            setHeartbeat(len);
            return true;
        }
        if (len > src.remaining()) {
            src.skip(-4);
            return false;
        }
        byte h1 = src.getByte();
        futureType = src.getByte();
        isBroadcast = ((h1 & 0b10000000) != 0);
        boolean hasText = ((h1 & 0b00010000) != 0);
        boolean hasBinary = ((h1 & 0b00001000) != 0);
        if (((h1 & 0b01000000) != 0)) {
            futureId = src.getInt();
        }
        if (((h1 & 0b00100000) != 0)) {
            channelId = src.getInt();
        }
        int textLen = 0;
        int binaryLen = 0;
        if (hasText) {
            textLen = src.getInt();
            if (textLen > textLenLimit) {
                throw new IOException("over text limit" + textLen);
            }
        }
        if (hasBinary) {
            binaryLen = src.getInt();
            if (binaryLen > binaryLenLimit) {
                throw new IOException("over binary limit" + textLen);
            }
        }
        if (hasText) {
            src.markL();
            src.limit(src.position() + textLen);
            readText = StringUtil.decode(channel.getCharset(), src.nioBuffer());
            src.reverse();
            src.resetL();
        }
        if (hasBinary) {
            src.markL();
            src.limit(src.position() + binaryLen);
            this.binaryReadBuffer = src.getBytes();
            src.reverse();
            src.resetL();
        }
        return true;
    }

    public void setBroadcast(boolean broadcast) {
        this.isBroadcast = broadcast;
    }

    public void setFutureId(int futureId) {
        this.futureId = futureId;
    }

    public void setFutureType(byte futureType) {
        this.futureType = futureType;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        return getReadText();
    }

}
