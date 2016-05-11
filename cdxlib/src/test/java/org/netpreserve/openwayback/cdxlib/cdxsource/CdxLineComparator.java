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

package org.netpreserve.openwayback.cdxlib.cdxsource;

import java.util.Comparator;

import org.netpreserve.openwayback.cdxlib.CdxRecord;

/**
 * Comparator for CdxLines using compareTo instead of equals.
 * <p>
 This is needed in these tests due to LegacyCdxLine not strictly following the equals() beeing
 consistent with compareTo(). For an explenation see {@link CdxLine#compareTo(CdxLine).
 */
public class CdxLineComparator implements Comparator<CdxRecord> {
    @Override
    public int compare(CdxRecord o1, CdxRecord o2) {
        return o1.compareTo(o2);
    }

}
