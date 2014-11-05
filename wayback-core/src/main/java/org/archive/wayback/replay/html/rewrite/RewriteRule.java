package org.archive.wayback.replay.html.rewrite;

import org.archive.util.iterator.StringTransformer;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.springframework.beans.factory.BeanNameAware;

/**
 * {@code RewriteRule} is similar to {@link StringTransformer}, but it receives
 * policy name (rewrite rule name), from which it can extract parameters.
 * <p>
 * StringTransformer can be seen as super-interface of this class (although
 * {@code RewriteRule} is not an interface), used where transformation is
 * static. As {@link RewriteRule} does not provide consistent parameter parsing
 * service, it'd be good idea to define {@code ParameterizedStringTransformer}
 * with better service to replace {@code RewriteRule}.
 * </p>
 */
public abstract class RewriteRule implements BeanNameAware {

	String beanName;

	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getBeanName() {
		return beanName;
	}

	public abstract String rewrite(ReplayParseContext context, String policy,
			String input);
}
