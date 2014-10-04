package com.iconmaster.source.xml;

import com.iconmaster.source.element.Element;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author iconmaster
 */
public class ElementXML {
	public static Node toXML(Document doc, Node node, Element e) {
		Node base = XMLHelper.addTag(doc, node, "element", "");
		XMLHelper.addAttribute(doc, (org.w3c.dom.Element)base, "type", e.type.toString());
		XMLHelper.addAttribute(doc, (org.w3c.dom.Element)base, "range", e.range.toString());
		if (e.dataType!=null) {
			Node dt = XMLHelper.addTag(doc, base, "data_type", "");
			dt.appendChild(ElementXML.toXML(doc, dt, e.dataType));
		}
			XMLHelper.addTags(doc, (org.w3c.dom.Element) base, "directive", e.directives);
		for (int i=0;i<e.args.length;i++) {
			if (e.args[i]!=null) {
				Node arg = XMLHelper.addTag(doc, base, "arg"+i, "");
				if (e.args[i] instanceof Element) {
					ElementXML.toXML(doc, arg, (Element)e.args[i]);
				} else if (e.args[i] instanceof String) {
					XMLHelper.addAttribute(doc, (org.w3c.dom.Element) arg, "value", (String)e.args[i]);
				} else if (e.args[i] instanceof List) {
					ElementXML.toXML(doc, arg, (List)e.args[i]);
				}
			}
		}
		return base;
	}
	
	public static List<Node> toXML(Document doc, Node node, List<Element> e) {
		ArrayList<Node> a = new ArrayList<>();
		for (Element v : e) {
			a.add(toXML(doc,node,v));
		}
		return a;
	}
}
