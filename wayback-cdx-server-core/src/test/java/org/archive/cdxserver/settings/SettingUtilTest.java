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
package org.archive.cdxserver.settings;

import org.archive.cdxserver.settings.SettingUtil;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import junit.framework.TestCase;
import org.archive.cdxserver.auth.AuthChecker;
import org.archive.cdxserver.auth.RemoteAuthChecker;

/**
 * Tests for SettingsUtil.
 * <p>
 * The settings used in this test can be found at src/test/resources/application.conf
 */
public class SettingUtilTest extends TestCase {

    /**
     * Test of createObject method, of class SettingUtil.
     */
    public void testCreateObject() {
        Config config = ConfigFactory.load();
        AuthChecker ac = SettingUtil.createObject("org.archive.cdxserver.auth.RemoteAuthChecker",
                AuthChecker.class, config.getConfig("authorization"));

        assertEquals("http://foo.bar.no", ((RemoteAuthChecker) ac).getAccessCheckUrl());
        assertNull(((RemoteAuthChecker) ac).getPublicCdxFields());
        assertNull(((RemoteAuthChecker) ac).getAllCdxFieldsAccessTokens());
        assertEquals(ImmutableList.of("foo", "bar"),
                ((RemoteAuthChecker) ac).getAllUrlAccessTokens());
    }

}
