/*
 * Copyright 2015 IIPC.
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
package org.netpreserve.openwayback.cdxlib.cdxsource;

import java.util.Objects;

/**
 *
 */
public final class SourceBlock implements Cloneable {

    final String key;

    final long offset;

    int length;

    final String location;

    int lineCount;

    public SourceBlock(final String key, final long offset, final int length) {
        this(key, offset, length, null, -1);
    }

    public SourceBlock(final String key, final long offset, final int length,
            final String location) {
        this(key, offset, length, location, -1);
    }

    public SourceBlock(final String key, final long offset, final int length,
            final String location, final int lineCount) {
        this.key = key;
        this.offset = offset;
        this.length = length;
        this.location = location;
        this.lineCount = lineCount;
    }

    @Override
    public SourceBlock clone() {
        try {
            return (SourceBlock) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getKey() {
        return key;
    }

    public long getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public String getLocation() {
        return location;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public SourceBlock merge(SourceBlock nextBlock) {
        return new SourceBlock(key, offset, length + nextBlock.length);
    }

    @Override
    public String toString() {
        return "Block{" + "key=" + key + ", offset=" + offset + ", length=" + length
                + (lineCount != -1 ? ", lines=" + lineCount : "") + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.key);
        hash = 29 * hash + (int) (this.offset ^ (this.offset >>> 32));
        hash = 29 * hash + this.length;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SourceBlock other = (SourceBlock) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        if (this.offset != other.offset) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        return true;
    }

}
