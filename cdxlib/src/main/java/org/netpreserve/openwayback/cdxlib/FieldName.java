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
package org.netpreserve.openwayback.cdxlib;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Representation of a field name.
 */
public final class FieldName {

    private static final Map<String, FieldName> FIELDS_BY_NAME = new HashMap<>();

    private static final Map<Character, FieldName> FIELDS_BY_CODE = new HashMap<>();

    /**
     * Ceta tags (AIF).
     * Code 'M' in legacy CDX format
     */
    public static final FieldName ROBOT_FLAGS = forNameAndCode("robotflags", 'M');

    /**
     * Url key (massaged url e.g. Surt format).
     * Code 'N' in legacy CDX format
     */
    public static final FieldName URI_KEY = forNameAndCode("urlkey", 'N');

    /**
     * Language string.
     * Code 'Q' in legacy CDX format
     */
    public static final FieldName LANGUAGE = forNameAndCode("lang", 'Q');

    /**
     * Length.
     * Code 'S' in legacy CDX format
     */
    public static final FieldName LENGTH = forNameAndCode("length", 'S');

    /**
     * Compressed arc file offset.
     * Code 'V' in legacy CDX format
     */
    public static final FieldName OFFSET = forNameAndCode("offset", 'V');

    /**
     * Original Url.
     * Code 'a' in legacy CDX format
     */
    public static final FieldName ORIGINAL_URI = forNameAndCode("url", 'a');

    /**
     * Date.
     * Code 'b' in legacy CDX format
     */
    public static final FieldName TIMESTAMP = forNameAndCode("time", 'b');

    /**
     * File name.
     * Code 'g' in legacy CDX format
     */
    public static final FieldName FILENAME = forNameAndCode("filename", 'g');

    /**
     * New style checksum.
     * Code 'k' in legacy CDX format
     */
    public static final FieldName DIGEST = forNameAndCode("digest", 'k');

    /**
     * Mime type of original document.
     * Code 'm' in legacy CDX format
     */
    public static final FieldName MIME_TYPE = forNameAndCode("mime", 'm');

    /**
     * Redirect.
     * Code 'r' in legacy CDX format
     */
    public static final FieldName REDIRECT = forNameAndCode("redirect", 'r');

    /**
     * Response code.
     * Code 's' in legacy CDX format
     */
    public static final FieldName RESPONSE_CODE = forNameAndCode("statuscode", 's');

    /**
     * Comment.
     */
    public static final FieldName COMMENT = forNameAndCode("comment", '#');

    /**
     * Record id in warc file
     */
    public static final FieldName WARC_ID = forName("warcid");

    /**
     * Locator used to fetch the record
     */
    public static final FieldName LOCATOR = forName("loc");

    private final String name;

    private final char code;

    private FieldName(String name, char code) {
        this.name = Objects.requireNonNull(name).intern();
        this.code = code;
    }

    private FieldName(String name) {
        this(name, '?');
    }

    public static FieldName forName(String name) {
        FieldName field = FIELDS_BY_NAME.get(name);
        if (field == null) {
            field = new FieldName(name);
            FIELDS_BY_NAME.put(name, field);
        }
        return field;
    }

    public static FieldName forCode(char code) {
        FieldName field = FIELDS_BY_CODE.get(code);
        if (field == null) {
            throw new IllegalArgumentException("Illegal field code: " + code);
        }
        return field;
    }

    private static FieldName forNameAndCode(String name, char code) {
        FieldName field = FIELDS_BY_NAME.get(name);
        if (field == null) {
            field = new FieldName(name, code);
            FIELDS_BY_NAME.put(name, field);
            FIELDS_BY_CODE.put(code, field);
        }
        return field;
    }

    public String getName() {
        return name;
    }

    public char getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.name);
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
        final FieldName other = (FieldName) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

}
