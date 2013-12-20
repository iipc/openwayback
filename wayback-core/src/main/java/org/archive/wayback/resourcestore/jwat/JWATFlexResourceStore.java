package org.archive.wayback.resourcestore.jwat;

import java.io.IOException;
import java.io.InputStream;

import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.FlexResourceStore;

public class JWATFlexResourceStore extends FlexResourceStore {
	
	@Override
    protected Resource loadResource(String path, InputStream is)
            throws IOException, ResourceNotAvailableException {
		
		return JWATResource.getResource(is, 0);
    }
}
