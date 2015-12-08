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
package org.netpreserve.openwayback.cdxlib;

/**
 *
 */
public enum FieldType {

    /**
     * Canonized url.
     */
    A("url", "canonized url"),
    /**
     * News group.
     */
    B("newsgroup", "news group"),
    /**
     * Rulespace category.
     */
    C("rulespace", "rulespace category"),
    /**
     * Compressed dat file offset.
     */
    D("datoffset", "compressed dat file offset"),
    /**
     * Canonized frame.
     */
    F("canonizedframe", "canonized frame"),
    /**
     * Multi-columm language description.
     */
    G("multi-columm language description", "multi-columm language description"),
    /**
     * Canonized host.
     */
    H("canonizedhost", "canonized host"),
    /**
     * Canonized image.
     */
    I("canonizedimage", "canonized image"),
    /**
     * Canonized jump point.
     */
    J("canonizedjumppoint", "canonized jump point"),
    /**
     * Some weird FBIS what's changed kinda thing.
     */
    K("Some weird FBIS what's changed kinda thing", "Some weird FBIS what's changed kinda thing"),
    /**
     * Canonized link.
     */
    L("canonized link", "canonized link"),
    /**
     * Ceta tags (AIF).
     */
    M("robotflags", "meta tags (AIF)", true),
    /**
     * Url key (massaged url e.g. Surt format).
     */
    N("urlkey", "massaged url", true),
    /**
     * Canonized path.
     */
    P("canonizedpath", "canonized path"),
    /**
     * Language string.
     */
    Q("languagestring", "language string"),
    /**
     * Canonized redirect.
     */
    R("canonizedredirect", "canonized redirect"),
    /**
     * Length.
     */
    S("length", "length", true),
    /**
     * Uniqness.
     */
    U("uniqness", "uniqness"),
    /**
     * Compressed arc file offset.
     */
    V("offset", "compressed arc file offset", true),
    /**
     * Canonized url in other href tags.
     */
    X("canonizedurlhref", "canonized url in other href tags"),
    /**
     * Canonized url in other src tags.
     */
    Y("canonizedurlsrc", "canonized url in other src tags"),
    /**
     * Canonized url found in script.
     */
    Z("canonizedurlscript", "canonized url found in script"),
    /**
     * Original Url.
     */
    a("original", "original url", true),
    /**
     * Date.
     */
    b("timestamp", "date", true),
    /**
     * Old style checksum.
     */
    c("oldstylechecksum", "old style checksum"),
    /**
     * Uncompressed dat file offset.
     */
    d("uncompressed_dat_offset", "uncompressed dat file offset"),
    /**
     * IP.
     */
    e("ip", "IP"),
    /**
     * Frame.
     */
    f("frame", "frame"),
    /**
     * File name.
     */
    g("filename", "file name", true),
    /**
     * Original host.
     */
    h("originalhost", "original host"),
    /**
     * Image.
     */
    i("image", "image"),
    /**
     * Original jump point.
     */
    j("original jump point", "original jump point"),
    /**
     * New style checksum.
     */
    k("digest", "new style checksum", true),
    /**
     * Link.
     */
    l("link", "link"),
    /**
     * Mime type of original document.
     */
    m("mimetype", "mime type of original document", true),
    /**
     * Arc document length.
     */
    n("arc_document_length", "arc document length"),
    /**
     * Port.
     */
    o("port", "port"),
    /**
     * Original path.
     */
    p("originalpath", "original path"),
    /**
     * Redirect.
     */
    r("redirect", "redirect", true),
    /**
     * Response code.
     */
    s("statuscode", "response code", true),
    /**
     * Title.
     */
    t("title", "title"),
    /**
     * Uncompressed arc file offset.
     */
    v("uncompressedoffset", "uncompressed arc file offset"),
    /**
     * Url in other href tags.
     */
    x("urlhref", "url in other href tags"),
    /**
     * Url in other src tags.
     */
    y("urlsrc", "url in other src tags"),
    /**
     * Url found in script.
     */
    z("urlscript", "url found in script"),
    /**
     * Custom field.
     */
    custom("custom", "custom field"),
    /**
     * Comment.
     */
    comment("comment", "comment");

    private final String name;

    private final String description;

    private final boolean supported;

    private FieldType(final String name, final String description, final boolean supported) {
        this.name = name;
        this.description = description;
        this.supported = supported;
    }

    private FieldType(final String name, final String description) {
        this.name = name;
        this.description = description;
        this.supported = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSupported() {
        return supported;
    }

    public static FieldType forName(String name) {
        for (FieldType f : values()) {
            if (f.name.equals(name)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Illegal field name: " + name);
    }

    public static FieldType forCode(char code) {
        FieldType result;
        for (FieldType f : values()) {
            if (f.name().charAt(0) == code) {
                return f;
            }
        }
        if ('#' == code) {
            result = comment;
        } else {
            throw new IllegalArgumentException("Illegal field code: " + code);
        }
        return result;
    }

    public static FieldType forCodeOrValue(String name) {
        FieldType result;
        if (name.length() == 1) {
            result = forCode(name.charAt(0));
        } else {
            result = forName(name);
        }
        return result;
    }

}
