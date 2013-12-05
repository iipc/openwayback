package org.archive.wayback.resourcestore;

import java.io.IOException;
import java.io.InputStream;

import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.jwat.wayback.JWATResource;

public class JWATFlexResourceStore extends FlexResourceStore {
	
	@Override
    protected Resource loadResource(String path, InputStream is)
            throws IOException, ResourceNotAvailableException {
		
		return JWATResource.getResource(is, 0);
    }
}
