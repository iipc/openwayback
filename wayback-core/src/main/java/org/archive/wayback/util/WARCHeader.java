/* WARCHeader
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.archive.io.warc.WARCWriter;
import org.archive.util.anvl.ANVLRecord;

public class WARCHeader {
	private void writeHeaderRecord(File target, File fieldsSrc, String id)
	throws IOException {

		WARCWriter writer = null;

		BufferedOutputStream bos =
			new BufferedOutputStream(new FileOutputStream(target));

		FileInputStream is = new FileInputStream(fieldsSrc);
		ANVLRecord ar = ANVLRecord.load(is);

		List<String> metadata = new ArrayList<String>(1);
		metadata.add(ar.toString());

		writer = new WARCWriter(null, bos, target, true, null,
				metadata);
		// Write a warcinfo record with description about how this WARC
		// was made.
		writer.writeWarcinfoRecord(target.getName(), "Made from "
				+ id + " by "
				+ this.getClass().getName());

	}

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("USAGE: tgtWarc fieldsSrc id");
			System.err.println("\ttgtWarc is the path to the target WARC.gz");
			System.err.println("\tfieldsSrc is the path to the text of the record");
			System.err.println("\t\tmake sure each line is terminated by \\r\\n");
			System.err.println("\t\tand that the file ends with a blank, \\r\\n terminiated line");
			System.err.println("\tid is the XXX in:");
			System.err.println("\t\tContent-Description: Made from XXX by org.archive.wayback.util.WARCHeader");
			System.err.println("\t\tof the header record... header...");
			System.exit(1);
		}
		File target = new File(args[0]);
		File fieldSrc = new File(args[1]);
		String id = args[2];
		WARCHeader header = new WARCHeader();
		try {
			header.writeHeaderRecord(target, fieldSrc, id);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

}
