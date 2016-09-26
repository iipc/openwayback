/*
 * Copyright 2016 The International Internet Preservation Consortium.
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
package org.netpreserve.resource.resolver.jaxrs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import org.netpreserve.commons.cdx.CdxFormat;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.SearchResult;
import org.netpreserve.commons.cdx.cdxrecord.CdxLineFormat;
import org.netpreserve.commons.cdx.formatter.CdxRecordFormatter;

/**
 *
 */
@Produces("application/vnd.org.netpreserve.cdx")
public class LegacyCdxWriter implements MessageBodyWriter<SearchResult> {

    private final CdxFormat outputFormat = CdxLineFormat.CDX11LINE;

    private final CdxRecordFormatter formatter = new CdxRecordFormatter(outputFormat);

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return genericType == SearchResult.class;
    }

    @Override
    public long getSize(SearchResult t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(SearchResult t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {

        try (SearchResult searchResult = t;) {

            Writer writer = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8);
            for (CdxRecord record : searchResult) {
                formatter.format(writer, record);
                writer.write('\n');
            }
            writer.flush();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new WebApplicationException(ex);
        }
    }

}
