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

	String name;

	@Override
	public void setBeanName(String beanName) {
		// This method will be called after properties are set.
		// Name set explicitly shall take precedence over bean name.
		if (name == null)
			name = beanName;
	}

	public String getName() {
		return name;
	}

	/**
	 * Set name explicitly. Takes precedence over bean name.
	 * @param name rule name
	 */
	public void setName(String name) {
		this.name = name;
	}

	public abstract String rewrite(ReplayParseContext context, String policy,
			String input);
}
