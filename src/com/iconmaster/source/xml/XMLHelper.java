package com.iconmaster.source.xml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author iconmaster
 */
public class XMLHelper {
	public static Document blankDoc() {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			return dBuilder.newDocument();
		} catch (ParserConfigurationException ex) {
			Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	public static Document fromString(String text) {
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(text);

			doc.getDocumentElement().normalize();

			return doc;

		} catch (IOException | ParserConfigurationException | SAXException e) {

		}

		return null;
	}
	
	public static Node addTag(Document doc,Node e,String index,String value) {
		org.w3c.dom.Element tag = doc.createElement(index);
		tag.appendChild(doc.createTextNode(value));
		return e.appendChild(tag);
	}
	
	public static Node addTag(Document doc,String index,String value) {
		org.w3c.dom.Element tag = doc.createElement(index);
		tag.appendChild(doc.createTextNode(value));
		return doc.appendChild(tag);
	}
	
	public static String toString(Document doc) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			final StringBuilder sb = new StringBuilder();
			StreamResult result = new StreamResult(new OutputStream() {

				@Override
				public void write(int b) throws IOException {
					sb.append((char)b);
				}

			});
			
			transformer.transform(source, result);
			
			return sb.toString();
		} catch (TransformerConfigurationException ex) {
			Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
		} catch (TransformerException ex) {
			Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	public static String toFile(Document doc, File file) {
		try {
			doc.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			final StringBuilder sb = new StringBuilder();
			StreamResult result = new StreamResult(file);
			
			transformer.transform(source, result);
			
			return sb.toString();
		} catch (TransformerConfigurationException ex) {
			Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
		} catch (TransformerException ex) {
			Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	public static Node addAttribute(Document doc,org.w3c.dom.Element e,String index,String value) {
		e.setAttribute(index, value);
		return e;
	}
	
	public static String getTag(org.w3c.dom.Element e,String index) {
		NodeList nList = e.getElementsByTagName(index);
		if (nList.getLength()!=0) {
			Node nNode = nList.item(0);
			return nNode.getTextContent();
		}
		return null;
	}
	
	public static List<Node> addTags(Document doc, org.w3c.dom.Element e, String index, List values) {
		ArrayList<Node> a = new ArrayList<>();
		for (Object v : values) {
			a.add(addTag(doc,e,index,v.toString()));
		}
		return a;
	}
}
