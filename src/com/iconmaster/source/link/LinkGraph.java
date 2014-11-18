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
		public String name;
		public Import data;

		public LinkNode(String name, Import data) {
			this.name = name;
			this.data = data;
		}
		
		public void link(LinkNode other) {
			connectedTo.add(other);
		}
	}
	
	public HashMap<String,LinkNode> nodes = new HashMap<>();
	
	public void addNode(Import data) {
		LinkNode node;
		if (!nodes.containsKey(data.name)) {
			node = new LinkNode(data.name, data);
			nodes.put(data.name, node);
		}
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
			if (count==deps && !node.data.compiled) {
				a.add(node.data);
			}
		}
		return a;
	}
	
	public Import getImport(String name) {
		if (!nodes.containsKey(name)) {
			return null;
		}
		return nodes.get(name).data;
	}
	
	public void link(String from, String to) {
		nodes.get(from).link(nodes.get(to));
	}
	
	public int getHighestDep() {
		int n = 0;
		for (LinkNode node : nodes.values()) {
			if (node.connectedTo.size()>n) {
				n = node.connectedTo.size();
			}
		}
		return n;
	}
	
	public ArrayList<Import> getAllLinks() {
		ArrayList<Import> a = new ArrayList<>();
		for (LinkNode node : nodes.values()) {
			a.add(node.data);
		}
		return a;
	}
}
