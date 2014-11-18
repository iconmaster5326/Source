package com.iconmaster.source.link;

import com.iconmaster.source.prototype.Import;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author iconmaster
 */
public class LinkGraph {
	public static class LinkNode {
		public ArrayList<LinkNode> connectedTo = new ArrayList<>();
		public ArrayList<LinkNode> connectedFrom = new ArrayList<>();
		public String name;
		public Import data;

		public LinkNode(String name, Import data) {
			this.name = name;
			this.data = data;
		}
		
		public void link(LinkNode other) {
			connectedTo.add(other);
			other.connectedFrom.add(this);
		}
	}
	
	public HashMap<String,LinkNode> nodes = new HashMap<>();
	
	public void addNode(String from, String to, Import data) {
		LinkNode node;
		if (nodes.containsKey(to)) {
			node = nodes.get(to);
		} else {
			node = new LinkNode(to, data);
			nodes.put(to, node);
		}
		LinkNode node2;
		if (nodes.containsKey(from)) {
			node2 = nodes.get(from);
		} else {
			node2 = new LinkNode(from, null);
			nodes.put(from, node2);
		}
		
		node.link(node2);
	}
	
	public ArrayList<Import> getLinksWithDepsOf(int deps) {
		ArrayList<Import> a = new ArrayList<>();
		for (LinkNode node : nodes.values()) {
			int count=0;
			for (LinkNode dep : node.connectedTo) {
				if (!dep.data.compiled) {
					count++;
				}
			}
			if (count==deps) {
				a.add(node.data);
			}
		}
		return a;
	}
}
