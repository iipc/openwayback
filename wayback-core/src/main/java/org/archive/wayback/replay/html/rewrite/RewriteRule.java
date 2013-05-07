package org.archive.wayback.replay.html.rewrite;

import org.archive.wayback.replay.html.ReplayParseContext;
import org.springframework.beans.factory.BeanNameAware;

public abstract class RewriteRule implements BeanNameAware {
	
	String beanName;
	
	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	public String getBeanName()
	{
		return beanName;
	}

	public abstract String rewrite(ReplayParseContext context, String policy, String input);
}
