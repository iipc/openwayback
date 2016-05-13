/*
 * Copyright 2016 IIPC.
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
package org.netpreserve.openwayback.cdxlib.formatter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * A character stream that collects its output in a StringBuilder.
 * <p>
 * This class is similar to {@link StringWriter} except that it is not thread safe. This avoids
 * synchronization overhead when thread safety is not necessary.
 */
public class StringBuilderWriter extends Writer {

    private final StringBuilder sb;

    /**
     * Create a new StringBuilderWriter using an existing {@link StringBuilder}.
     * @param sb
     */
    public StringBuilderWriter(StringBuilder sb) {
        this.sb = sb;
    }

    /**
     * Create a new StringBuilderWriter using a {@link StringBuilder} with initial capacity of 128.
     */
    public StringBuilderWriter() {
        this.sb = new StringBuilder(128);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        sb.append(cbuf, off, len);
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        sb.append(csq, start, end);
        return this;
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        sb.append(csq);
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        sb.append(c);
        return this;
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        sb.append(str, off, off + len);
    }

    @Override
    public void write(String str) throws IOException {
        sb.append(str);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        sb.append(cbuf);
    }

    @Override
    public void write(int c) throws IOException {
        sb.append((char) c);
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    /**
     * Get the underlying StringBuilder.
     *
     * @return the underlying StringBuilder.
     */
    public StringBuilder getStringBuilder() {
        return sb;
    }

}
