/**
 *
 */
package com.technologies.highstreet.netconf2snmpmediator.server.networkelement;

import com.technologies.highstreet.netconf.server.types.NetconfBoolean;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Micha
 *
 */
public class NodeEditConfig {

	private static final Logger LOG = LoggerFactory.getLogger(NodeEditConfig.class);

	// Unidirectional SNMP to Netconf
	private static Pattern Int2Boolean = Pattern.compile("int-to-boolean(?:-(\\d[0-9,]*)-(true|false))?$");
	private static Pattern IfValue = Pattern.compile("if-(\\d[0-9,]*)-(\\w+)-(\\w+)$");
	// Bidirectional SNMP to Netconf to SNMP
	private static Pattern Divide = Pattern.compile("divide-(\\d+)$");
	private static Pattern Map2Value = Pattern.compile("map-(\\d[0-9,]*)-((?:[0-9a-zA-Z-]+,?)+)");

	private static String EMPTY = "";

	private final String oid;
	private final String conversion;
	private final String defaultValue;
	/** xPath to the Element */
	private final String xPath;
	/** Element in the document */
	private final Element element;
	/** false:read-only true:read-write */
	private boolean readWrite;

	// Construct
	public NodeEditConfig(String xPath, Element e, String oid, String conversion, String readWrite, String def) {
		if (conversion == null) {
			throw new IllegalArgumentException("null conversion not allowed.");
		}
		this.element = e;
		this.oid = oid;
		this.conversion = conversion;
		this.defaultValue = def;
		this.xPath = xPath;
		setReadWrite(readWrite);
	}

	public NodeEditConfig(String xPath, Element e, String oid) {
		this(xPath, e, oid, e.hasAttribute("conversion") ? e.getAttribute("conversion") : EMPTY,
				e.hasAttribute("access") ? e.getAttribute("access") : EMPTY,
				e.hasAttribute("default") ? e.getAttribute("access") : e.getTextContent()

		);
	}

	// Getter

	public String getOid() {
		return oid;
	}

	public Element getElement() {
		return element;
	}

	public boolean getReadWrite() {
		return readWrite;
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setReadWrite(String readWrite) {
		this.readWrite = readWrite.equals("read-write");
	}

	public boolean isTestNode() {
		return oid.equals("test");
	}

	// ---------------------------------------------------
	// Functions

	public boolean isConversionNeeded() {
		return !(this.conversion == null || this.conversion == "");
	}

	/**
	 * Small processor for simple attribute adaption
	 *
	 * @param value
	 *            that could be processes if processor is specified.
	 * @return processed result (could be the original one of no processor is
	 *         specified
	 */
	public String convertValueSnmp2Netconf(String value) {
		if (conversion.isEmpty() || this.isInternalConversionNeeded()) { // speed up
			return value;
		}

		String res = null;

		{
			Matcher matcher1 = Int2Boolean.matcher(conversion);
			if (matcher1.matches()) {

				if (matcher1.group(1) == null) {
					res = NetconfBoolean.getNetconfBoolean(value.equals("1"));
				} else if (matcher1.group(2).equals("true")) {
					res = NetconfBoolean
							.getNetconfBoolean(StringUtils.indexOfAny(value, matcher1.group(1).split(",")) > -1);
				} else {
					res = NetconfBoolean
							.getNetconfBoolean(StringUtils.indexOfAny(value, matcher1.group(1).split(",")) == -1);
				}
			}
		}

		if (res == null) {
			Matcher matcher2 = IfValue.matcher(conversion);
			if (matcher2.matches()) {
				res = StringUtils.indexOfAny(value, matcher2.group(1).split(",")) > -1 ? matcher2.group(2)
						: matcher2.group(3);
			}
		}

		if (res == null) {
			Matcher matcher3 = Divide.matcher(conversion);
			if (matcher3.matches()) {
				try {
					res = String.valueOf(Integer.valueOf(value) / Integer.valueOf(matcher3.group(1)));
				} catch (NumberFormatException e) {
					res = null;
				}
			}
		}

		if (res == null) {
			Matcher matcher4 = Map2Value.matcher(conversion);
			if (matcher4.matches()) {
				String a1[] = matcher4.group(1).split(",");
				String a2[] = matcher4.group(2).split(",");
				int idx = Arrays.asList(a1).indexOf(value);
				res = idx > -1 && idx < a2.length ? a2[idx] : null;
			}
		}

		if (res == null) {
			LOG.warn("S2N>No valid conversion rule '{}' oid {} Value='" + value + "'", conversion, oid);
			return value;
		} else {
			LOG.info("Data conversion {}->{}", value, res);
			return res;
		}
	}

	/**
	 * NETCONF -> SNMP Value. Small processor for simple attribute adaption
	 *
	 * @param value
	 *            that could be processes if processor is specified.
	 * @return processed result as String (could be the original one of no processor
	 *         is specified
	 */

	public Variable convertValueNetconf2SnmpString(String value) {
		if (conversion.isEmpty()) { // speed up
			return new OctetString(value);
		}
		if (value == null) {
			value = "";
		}

		Matcher matcher;
		Variable res = null;

		if (res == null) {
			matcher = Divide.matcher(conversion);
			if (matcher.matches()) {
				try {
					res = new Integer32(Integer.valueOf(value) * Integer.valueOf(matcher.group(1)));
				} catch (NumberFormatException e) {
					res = null;
				}
			}
		}

		if (res == null) {
			matcher = Map2Value.matcher(conversion);
			if (matcher.matches()) {
				String a1[] = matcher.group(1).split(",");
				String a2[] = matcher.group(2).split(",");
				int idx = Arrays.asList(a2).indexOf(value);
				if (idx > -1 && idx < a1.length) {
					// res = new Integer32(idx > -1 && idx < a1.length ? a1[idx] : "");
					try {
						res = new Integer32(Integer.valueOf(a1[idx]));
					} catch (NumberFormatException e) {
					}
				}
			}
		}

		if (res == null && (matcher = Int2Boolean.matcher(conversion)).matches()) {
			if (matcher.group(1) == null) { // Base string
				if (value.equals("true")) {
					res = new Integer32(1);
				} else if (value.equals("false")) {
					res = new Integer32(0);
				}
			} // Extension is not back convertible
		}

		if (res == null) {
			LOG.warn("N2S>No valid conversion rule '{}' oid {} Value='" + value + "'", conversion, oid);
			return new OctetString(value);
		} else {
			LOG.info("Data conversion {}->{}", value, res);
			return res;
		}
	}

	public String getValueFromNetconfMessageForOid(Document sourceMessage) {
		XPathExpression xpathForNode;
		XPathExpressionException e = null;
		try {
			xpathForNode = XPathFactory.newInstance().newXPath().compile(xPath);
			final Node node = (Node) xpathForNode.evaluate(sourceMessage, XPathConstants.NODE);
			if (node != null) {
				return node.getTextContent();
				// Proceed with error handling
			}
		} catch (XPathExpressionException e1) {
			e = e1;
		}
		LOG.warn("Can not find node for xPath {} {}", xPath, e != null ? e.toString() : "");
		return EMPTY;

	}

	/**
	 * @return content of related xml element node
	 */
	public String getTextContent() {
		return element.getTextContent();
	}

	/**
	 * @return number, representing the NetconfContent
	 */
	public int getTextContentConverted2Snmp() {
		return -1;
	}

	/**
	 * Set new value, received via SNMP to Netconf
	 *
	 * @return true if oldValue != value
	 */
	public boolean setConvertedSnmpValue2Xml(String snmpValue) {
		String oldValue = element.getTextContent();
		element.setTextContent(convertValueSnmp2Netconf(snmpValue));
		return !oldValue.equals(snmpValue);
	}

	public boolean isInternalConversionNeeded() {
		if (this.conversion == null)
			return false;
		return this.conversion.equals("internal");
	}
}
