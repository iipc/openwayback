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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.netpreserve.openwayback.cdxlib.CdxLine;
import org.netpreserve.openwayback.cdxlib.CdxLineSchema;
import org.netpreserve.openwayback.cdxlib.CdxSource;
import org.netpreserve.openwayback.cdxlib.FieldType;
import org.netpreserve.openwayback.cdxlib.SearchResult;
import org.netpreserve.openwayback.cdxlib.processor.Processor;

/**
 * An iterator over a CdxSource with CdxLines sorted by distance to a timestamp.
 */
public class ClosestCdxIterator implements CdxIterator {

    final SearchResult forwardResult;

    final SearchResult backwardResult;

    final CdxIterator forwardIterator;

    final CdxIterator backwardIterator;

    final long timestamp;

    CdxLine nextLine;

    Candidate nextForwardCandidate;

    Candidate nextBackwardCandidate;

    public ClosestCdxIterator(CdxSource source, String url, String timestamp,
            CdxLineSchema outputFormat, List<Processor> processors) {

        String closestKey = url + " " + timestamp;

        forwardResult = source.search(closestKey, url + "~", outputFormat, processors, false);
        backwardResult = source.search(url, closestKey, outputFormat, processors, true);

        forwardIterator = forwardResult.iterator();
        backwardIterator = backwardResult.iterator();
        this.timestamp = timestampStringToSeconds(timestamp);
    }

    @Override
    public CdxLine next() {
        if (nextLine != null || hasNext()) {
            CdxLine line = nextLine;
            nextLine = null;
            return line;
        } else {
            return null;
        }
    }

    @Override
    public CdxLine peek() {
        if (hasNext()) {
            return nextLine;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        if (nextLine != null) {
            return true;
        }

        if (nextForwardCandidate == null && forwardIterator.hasNext()) {
            nextForwardCandidate = new Candidate(forwardIterator.next());
        }
        if (nextBackwardCandidate == null && backwardIterator.hasNext()) {
            nextBackwardCandidate = new Candidate(backwardIterator.next());
        }

        if (nextForwardCandidate == null && nextBackwardCandidate == null) {
            return false;
        }

        if (nextForwardCandidate == null
                || nextForwardCandidate.greaterDistanceThan(nextBackwardCandidate)) {
            nextLine = nextBackwardCandidate.line;
            nextBackwardCandidate = null;
        } else {
            nextLine = nextForwardCandidate.line;
            nextForwardCandidate = null;
        }

        return true;
    }

    @Override
    public void close() {
        forwardIterator.close();
        forwardResult.close();
        backwardIterator.close();
        backwardResult.close();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    private static long timestampStringToSeconds(String timestamp) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            return dateFormat.parse(timestamp).getTime() / 1000;
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    private class Candidate {

        final CdxLine line;

        final long distance;

        public Candidate(CdxLine line) {
            this.line = line;
            this.distance = Math.abs(
                    timestampStringToSeconds(String.valueOf(line.get(FieldType.b))) - timestamp);
        }

        public boolean greaterDistanceThan(Candidate o) {
            if (o == null) {
                return false;
            }
            return distance > o.distance;
        }

    }
}
