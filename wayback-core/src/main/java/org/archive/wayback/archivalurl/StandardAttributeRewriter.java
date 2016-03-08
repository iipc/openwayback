package org.archive.wayback.archivalurl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;
import org.archive.wayback.replay.html.rules.AttributeModifyingRule;
import org.archive.wayback.replay.html.transformer.InlineCSSStringTransformer;
import org.archive.wayback.replay.html.transformer.JSStringTransformer;
import org.archive.wayback.replay.html.transformer.MetaRefreshUrlStringTransformer;
import org.archive.wayback.replay.html.transformer.SrcsetStringTransformer;
import org.archive.wayback.replay.html.transformer.URLStringTransformer;
import org.htmlparser.nodes.TagNode;

/**
 * Standard implementation of {@link AttributeRewriter}.
 * <p>
 * The {@code StandardAttributeRewriter} is an re-implementation of
 * attribute-rewrite process that used to be embedded in
 * {@link FastArchivalUrlReplayParseEventHandler}, under
 * {@code AttributeRewriter} interface.
 * </p>
 * <p>
 * It also offers simple configuration mechanism for adding/changing
 * attribute rewrite rules through {@link Properties} object. New rules
 * can be added easily through SpringFramework's support for {@code Properties}
 * object. Well-known rewrite rules for attributes, such as {@code A/@HREF},
 * {@code LINK/@HREF} and <code>&#42;/@ONLOAD</code>, are provided by default and
 * implemented with the same mechanism.
 * Use {@link #setConfigProperties(Properties)} to add more rewrite rules.
 * </p>
 * <p>
 * Just in case you need an entirely different set of rewrite rules, it is possible to
 * disable default rule set by setting {@code true} to {@code defaultRulesDisabled}
 * property.
 * </p>
 * <p>
 * If you need to configure rewrite rules from different sources than {@link Properties}
 * object, it'd be easiest to override {@link #loadRules()} method.
 * </p>
 * <p>
 * Note: caller of this class may implement more complex rewrite process, and may
 * by-pass this class for certain tags/attributes.
 * </p>
 * <p>
 * While this class is similar to {@link AttributeModifyingRule} class in functionality,
 * interface is more specific to attribute rewriting and it is easier to customize.
 * </p>
 * @see FastArchivalUrlReplayParseEventHandler
 * @since 1.8.1
 */
public class StandardAttributeRewriter implements AttributeRewriter {
	private static final Logger LOGGER = Logger.getLogger(StandardAttributeRewriter.class.getName());
	private Map<String, StringTransformer> transformers;

	private Map<String, TransformAttr> rules;
	
	/**
	 * name of properties file with default rewrite rules.
	 * it should be in the same package as this class.
	 */
	public static final String CONFIG_PROPERTIES = "attribute-rewrite.properties";
	
	private Properties configProperties;
	
	private boolean defaultRulesDisabled = false;
	
	private StringTransformer jsBlockTrans;
	
	private boolean unescapeAttributeValues = true;
	
	private Map<String, StringTransformer> customTransformers;
	
	/**
	 * set StringTransformer for rewriting JavaScript attribute values.
	 * (event handler attributes and also {@code HREF="javascript:..."}.)
	 * Note changing this property after bean initialization is not supported.
	 * @param jsBlockTrans StringTransformer
	 */
	public void setJsBlockTrans(StringTransformer jsBlockTrans) {
		this.jsBlockTrans = jsBlockTrans;
	}

	/**
	 * set {@link Properties} object with <em>additional</em> attribute rewrite rules.
	 * if you want this to replace entire rewrite rule, set {@code defaultRulesDisabled}
	 * to {@code true}.
	 * @param configProperties
	 */
	public void setConfigProperties(Properties configProperties) {
		this.configProperties = configProperties;
	}
	/**
	 * set {@code true} if you want to disable loading default rewrite
	 * rules from in-class-path properties file.
	 * @param defaultRulesDisabled
	 */
	public void setDefaultRulesDisabled(boolean defaultRulesDisabled) {
		this.defaultRulesDisabled = defaultRulesDisabled;
	}
	
	public boolean isDefaultRulesDisabled() {
		return defaultRulesDisabled;
	}

	/**
	 * add more {@link StringTransformer}s.
	 * <p>Caveat: change has effect only before calling {@link #init()}.</p>
	 * @param customTransformers a Map with transformer name
	 * as keys and corresponding {@link StringTransformer}
	 * object as values.
	 */
	public void setCustomTransformers(
			Map<String, StringTransformer> customTransformers) {
		this.customTransformers = customTransformers;
	}

	/**
	 * set this property false if you want to disable unescaping
	 * (and corresponding re-escaping) of attribute values.
	 * <p>By default, HTML entities (such as <code>&amp;amp;</code>)
	 * in attribute values are unescaped before translation attempt,
	 * and then escaped back before writing out.  Although this is
	 * supposedly the right thing to do, it has a side-effect: all
	 * bare "<code>&amp;</code>" (not escaped as "<code>&amp;amp;</code>")
	 * will be replaced by "<code>&amp;amp;</code>".  Setting this property
	 * to <code>false</code> disables it.</p>
	 * <p>As URL rewrite does neither parse nor modify query part, it
	 * should mostly work without unescaping.  But there may be some
	 * corner cases where escaping is crucial.  Don't set this to {@code false}
	 * unless it's absolutely necessary.</p>
	 * @param unescapeAttributeValues <code>false</code> to disable unescaping
	 */
	public void setUnescapeAttributeValues(boolean unescapeAttributeValues) {
		this.unescapeAttributeValues = unescapeAttributeValues;
	}

	protected class TransformAttr implements Comparable<TransformAttr> {
		public final String attrName;
		private final StringTransformer transformer;
		public TransformAttr alt;
		public TransformAttr next;
		public TransformAttr(String attrName, StringTransformer transformer) {
			this.attrName = attrName;
			this.transformer = transformer;
		}
		public boolean apply(ReplayParseContext context, TagNode tag) {
			String orig = tag.getAttribute(attrName);
			if (orig == null) return false;
			// htmlparser does neither unescape HTML entities while it parses HTML, nor escape
			// HTML special chars while writing HTML.  So we take care of it for ourselves.
			// ParseContext.resolve() used to unescape URL (but did not escape back). It no longer
			// does in alignment with this change.
			if (unescapeAttributeValues)
				orig = StringEscapeUtils.unescapeHtml(orig);
			String transformed = transformer.transform(context, orig);
			if (unescapeAttributeValues)
				transformed = StringEscapeUtils.escapeHtml(transformed);
			tag.setAttribute(attrName, transformed);
			return true;
		}
		protected int specificity() {
			return 0;
		}
		@Override
		public int compareTo(TransformAttr o) {
			return specificity() - o.specificity();
		}
	}

	protected class TransformAttrIfAttrValue extends TransformAttr {
		private final String testAttrName;
		private final String testValue;
		public TransformAttrIfAttrValue(String attrName, StringTransformer transformer,
				String testAttrName, String testAttrValue) {
			super(attrName, transformer);
			this.testAttrName = testAttrName;
			this.testValue = testAttrValue;
		}
		@Override
		public boolean apply(ReplayParseContext context, TagNode tag) {
			String value = tag.getAttribute(testAttrName);
			if (!testValue.equalsIgnoreCase(value)) return false;
			return super.apply(context, tag);
		}
		@Override
		protected int specificity() {
			return 1;
		}
	}
	
	public void init() throws IOException {
		initTransformers();
		rules = new HashMap<String, TransformAttr>();
		loadRules();
	}
	
	/**
	 * Initialize {@code StringTransformer}s, register them to
	 * {@code transformers} map by their name.
	 */
	protected void initTransformers() {
		if (jsBlockTrans == null)
			jsBlockTrans = new JSStringTransformer();
		URLStringTransformer anchorTrans = new URLStringTransformer();
		anchorTrans.setJsTransformer(jsBlockTrans);

		transformers = new HashMap<String, StringTransformer>();
		transformers.put("fw", new URLStringTransformer("fw_"));
		transformers.put("if", new URLStringTransformer("if_"));
		transformers.put("cs", new URLStringTransformer("cs_"));
		transformers.put("js", new URLStringTransformer("js_"));
		transformers.put("im", new URLStringTransformer("im_"));
		transformers.put("oe", new URLStringTransformer("oe_"));
		transformers.put("an", anchorTrans);
		transformers.put("jb", jsBlockTrans);
		transformers.put("ci", new InlineCSSStringTransformer());
		transformers.put("mt", new MetaRefreshUrlStringTransformer());
                transformers.put("ss", new SrcsetStringTransformer());
		
		if (customTransformers != null) {
			transformers.putAll(customTransformers);
			customTransformers = null;
		}
	}
	
	protected void loadRules() throws IOException {
		// first load default rewrite rules from in-class-path resource.
		// make it a fatal error if load fails.
		if (!defaultRulesDisabled) {
			Properties defaultRules = new Properties();
			InputStream is = getClass().getResourceAsStream(CONFIG_PROPERTIES);
			defaultRules.load(is);
			loadRulesFromProperties(defaultRules);
		}
		if (configProperties != null) {
			loadRulesFromProperties(configProperties);
		} else {
			if (defaultRulesDisabled) {
				LOGGER.warning("No attribute rewrite rule is configured"
						+ " (defaultRulesDisabeld==true, and configProperties==null");
			}
		}		
	}

	/**
	 * Regular expression for parsing tag name part of rule keys.
	 */
	protected static final String RE_TAG = "\\p{Alpha}[-\\p{Alnum}]*";
	protected static final String RE_ATTR = RE_TAG;
	
	protected static final Pattern RE_TAG_ATTR_TYPE =
			Pattern.compile("(" + RE_TAG + "|\\*)(?:\\[(" + RE_ATTR + ")=([^]]*)\\])?\\.(" + RE_ATTR + ")\\.type");
	
	/**
	 * build rewrite rule set from textual description in {@code p}.
	 * rule set is a collection of a sub-tree for each element, stored in {@code rules}
	 * Map. {@code rules} maps tag name to a tree of {@code TransformAttr}. {@code TransformAttr}
	 * tree is in essence a linked list of {@code TransformAttr} grouped by attribute name. In each
	 * group, {@code TransformAttr}s are sorted by their <em>specificity</em>. Within the same
	 * specificity, the {@code TransformAttr} added last comes on top (so that rules added through
	 * {@code Properties} override default rules.). As the order of keys in a single {@code Properties}
	 * is unspecified, there's no guarantee later line overrides former lines with the same key.
	 * @param p Properties with rewrite rules.
	 */
	protected void loadRulesFromProperties(Properties p) {
		for (String key : p.stringPropertyNames()) {
			Matcher m = RE_TAG_ATTR_TYPE.matcher(key);
			if (m.matches()) {
				String tagName = m.group(1);
				String testAttrName = m.group(2);
				String testAttrValue = m.group(3);
				String attrNameMatch = m.group(4);
				String transformerName = p.getProperty(key);
				StringTransformer transformer = transformers.get(transformerName);
				if (transformer == null) {
					LOGGER.warning("Unknown transformer name \"" +
							transformerName + "\" for key \"" + key + "\"");
					continue;
				}

				TransformAttr t;
				if (testAttrName != null) {
					t = new TransformAttrIfAttrValue(attrNameMatch,
							transformer, testAttrName, testAttrValue);
				} else {
					t = new TransformAttr(attrNameMatch, transformer);
				}
				TransformAttr ta = rules.get(tagName);
				TransformAttr pta = null;
				while (true) {
					if (ta == null) {
						if (pta == null) {
							rules.put(tagName, t);
						} else {
							pta.next = t;
						}
						break;
					}
					// TransformAttr for the same attrName are chained
					// on "alt" link, sorted by specificity, descending.
					if (ta.attrName.equals(t.attrName)) {
						TransformAttr palt = null;
						while (true) {
							if (t.compareTo(ta) >= 0) {
								t.alt = ta;
								t.next = ta.next;
								ta.next = null;
								if (palt == null) {
									if (pta == null) {
										rules.put(tagName, t);
									} else {
										pta.next = t;
									}
								} else {
									palt.next = t;
								}
								break;
							}
							palt = ta;
							ta = ta.alt;
							if (ta == null) {
								palt.alt = t;
								break;
							}
						}
						break;
					}
					pta = ta;
					ta = ta.next;
				}
				continue;
			}

			LOGGER.warning("unrecogized key syntax \"" + key + "\"");
		}
	}

	@Override
	public void rewrite(ReplayParseContext context, TagNode tag) {
		String tagName = tag.getTagName();
		TransformAttr ta = rules.get(tagName);
		while (ta != null) {
			for (TransformAttr attrta = ta; attrta != null; attrta = attrta.alt) {
				if (attrta.apply(context, tag))
					break;
			}
			ta = ta.next;
		}
		// TODO: should we check if anyElementRules is rewriting
		// an attribute which has been rewritten by per-element rules?
		ta = rules.get("*");
		while (ta != null) {
			for (TransformAttr attrta = ta; attrta != null; attrta = attrta.alt) {
				if (attrta.apply(context, tag))
					break;
			}
			ta = ta.next;
		}
	}
}
