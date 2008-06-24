/* IndexQueue
 *
 * $Id$
 *
 * Created on 2:05:12 PM Jun 23, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourcestore.indexer;

import java.io.IOException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface IndexQueue {
	public final static int STATUS_DONE = 0;
	public final static int STATUS_FAIL = 1;
	public final static int STATUS_RETRY = 2;
	public void enqueue(String resourceFileName) throws IOException;
	public String dequeue() throws IOException;
	public void recordStatus(String resourceFileName, int status);
}
