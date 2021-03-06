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
package com.generallycloud.baseio.component;

import com.generallycloud.baseio.buffer.ByteBuf;

/**
 * @author wangkai
 *
 */
public class ProtectedUtil {

    public static void finishHandshake(NioSocketChannel ch, Exception e) {
        ch.finishHandshake(e);
    }

    public static void readPlainRemainingBuf(NioSocketChannel ch, ByteBuf dst) {
        ch.readPlainRemainingBuf(dst);
    }

    public static void readSslPlainRemainingBuf(NioSocketChannel ch, ByteBuf dst) {
        ch.readSslPlainRemainingBuf(dst);
    }

}
