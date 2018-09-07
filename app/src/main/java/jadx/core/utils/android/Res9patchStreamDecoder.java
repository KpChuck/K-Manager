/**
 *  Copyright 2014 Ryszard Wiśniewski <brut.alll@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jadx.core.utils.android;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import jadx.core.utils.exceptions.JadxException;

/**
 * @author Ryszard Wiśniewski <brut.alll@gmail.com>
 */
public class Res9patchStreamDecoder {

    public void decode(InputStream in, OutputStream out) {

    }

    private NinePatch getNinePatch(byte[] data) throws JadxException,
            IOException {
        ExtDataInput di = new ExtDataInput(new ByteArrayInputStream(data));
        find9patchChunk(di);
        return NinePatch.decode(di);
    }

    private void find9patchChunk(DataInput di) throws JadxException,
            IOException {
        di.skipBytes(8);
        while (true) {
            int size;
            try {
                size = di.readInt();
            } catch (IOException ex) {
                throw new JadxException("Cant find nine patch chunk", ex);
            }
            if (di.readInt() == NP_CHUNK_TYPE) {
                return;
            }
            di.skipBytes(size + 4);
        }
    }

    private static final int NP_CHUNK_TYPE = 0x6e705463; // npTc
    private static final int NP_COLOR = 0xff000000;

    private static class NinePatch {
        public final int padLeft, padRight, padTop, padBottom;
        public final int[] xDivs, yDivs;

        public NinePatch(int padLeft, int padRight, int padTop, int padBottom,
                         int[] xDivs, int[] yDivs) {
            this.padLeft = padLeft;
            this.padRight = padRight;
            this.padTop = padTop;
            this.padBottom = padBottom;
            this.xDivs = xDivs;
            this.yDivs = yDivs;
        }

        public static NinePatch decode(ExtDataInput di) throws IOException {
            di.skipBytes(1);
            byte numXDivs = di.readByte();
            byte numYDivs = di.readByte();
            di.skipBytes(1);
            di.skipBytes(8);
            int padLeft = di.readInt();
            int padRight = di.readInt();
            int padTop = di.readInt();
            int padBottom = di.readInt();
            di.skipBytes(4);
            int[] xDivs = di.readIntArray(numXDivs);
            int[] yDivs = di.readIntArray(numYDivs);

            return new NinePatch(padLeft, padRight, padTop, padBottom, xDivs,
                    yDivs);
        }
    }
}