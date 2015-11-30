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

    A("url", "canonized url"),
    B("newsgroup", "news group"),
    C("rulespace", "rulespace category"),
    D("datoffset", "compressed dat file offset"),
    F("canonizedframe", "canonized frame"),
    G("multi-columm language description", "multi-columm language description"),
    H("canonizedhost", "canonized host"),
    I("canonizedimage", "canonized image"),
    J("canonizedjumppoint", "canonized jump point"),
    K("Some weird FBIS what's changed kinda thing", "Some weird FBIS what's changed kinda thing"),
    L("canonized link", "canonized link"),
    M("robotflags", "meta tags (AIF)", true),
    N("urlkey", "massaged url", true),
    P("canonizedpath", "canonized path"),
    Q("languagestring", "language string"),
    R("canonizedredirect", "canonized redirect"),
    S("length", "length", true),
    U("uniqness", "uniqness"),
    V("offset", "compressed arc file offset", true),
    X("canonizedurlhref", "canonized url in other href tages"),
    Y("canonizedurlsrc", "canonized url in other src tags"),
    Z("canonizedurlscript", "canonized url found in script"),
    a("original", "original url", true),
    b("timestamp", "date", true),
    c("oldstylechecksum", "old style checksum"),
    d("uncompressed_dat_offset", "uncompressed dat file offset"),
    e("ip", "IP"),
    f("frame", "frame"),
    g("filename", "file name", true),
    h("originalhost", "original host"),
    i("image", "image"),
    j("original jump point", "original jump point"),
    k("digest", "new style checksum", true),
    l("link", "link"),
    m("mimetype", "mime type of original document", true),
    n("arc_document_length", "arc document length"),
    o("port", "port"),
    p("originalpath", "original path"),
    r("redirect", "redirect", true),
    s("statuscode", "response code", true),
    t("title", "title"),
    v("uncompressedoffset", "uncompressed arc file offset"),
    x("urlhref", "url in other href tags"),
    y("urlsrc", "url in other src tags"),
    z("urlscript", "url found in script"),
    custom("custom", "custom field"),
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
