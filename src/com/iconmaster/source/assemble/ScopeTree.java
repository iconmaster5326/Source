package com.iconmaster.source.assemble;

import com.iconmaster.source.compile.Operation;
import com.iconmaster.source.compile.Operation.OpType;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author iconmaster
 */
public class ScopeTree<T> {
	public ScopeTree<T> line;
	public ScopeTree<T> block;
	
	public ScopeTree<T> parent;
	
	public T data;
	
	public static interface TreeParser<K> {
		public K parse(ArrayList<Operation> code);
	}
	
	public static interface TreeIterator<K> {
		public void iterate(ScopeTree<K> tree);
	}
	
	public static interface TreeFlattener<K,L> {
		public L flatten(K data);
	}
	
	public ScopeTree() {
		
	}
	
	public ScopeTree(T data) {
		this.data = data;
	}
	
	public ScopeTree(ScopeTree<T> line, ScopeTree<T> block) {
		addLine(line);
		addBlock(block);
	}

	public ScopeTree(ScopeTree<T> line, ScopeTree<T> block, T data) {
		this(line,block);
		this.data = data;
	}
	
	public ScopeTree<T> addLine(ScopeTree<T> node) {
		if (node!=null) {
			node.parent = this;
		}
		line = node;
		return node;
	}
	
	public ScopeTree<T> addBlock(ScopeTree<T> node) {
		if (node!=null) {
			node.parent = this;
		}
		block = node;
		return node;
	}
	
	public ScopeTree<T> getRoot() {
		if (parent==null) {
			return this;
		}
		return parent.getRoot();
	}
	
	public T getRootValue() {
		return getRoot().data;
	}
	
	public ScopeTree<T> addLine(T value) {
		return addLine(new ScopeTree<>(value));
	}
	
	public ScopeTree<T> addBlock(T value) {
		return addBlock(new ScopeTree<>(value));
	}
	
	public static <V> ScopeTree<V> createTree(ArrayList<Operation> code, TreeParser<V> parser) {
		ScopeTree<V> tree = null;
		ArrayList<Operation> a = new ArrayList<>();
		int depth = 0;
		for (Operation op : code) {
			if (op.op==OpType.BEGIN) {
				depth++;
				if (depth==1) {
					if (tree==null) {
						tree = new ScopeTree<>(parser.parse(a));
					} else {
						tree.addLine(parser.parse(a));
					}
					a = new ArrayList<>();
				} else {
					a.add(op);
				}
			} else if (op.op==OpType.END) {
				depth--;
				if (depth==0) {
					if (tree==null) {
						tree = createTree(a,parser);
					} else {
						tree.addBlock(createTree(a,parser));
					}
					a = new ArrayList<>();
				} else {
					a.add(op);
				}
			} else {
				a.add(op);
			}
		}
		
		if (tree==null) {
			tree = new ScopeTree<>(parser.parse(a));
		} else {
			tree.addLine(parser.parse(a));
		}
		return tree;
	}
	
	public void iterate(TreeIterator<T> iter) {
		iter.iterate(this);
		
		if (block!=null) {
			block.iterate(iter);
		}
		
		if (line!=null) {
			line.iterate(iter);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(line==null?("{}"):("{"+line+"} "));
		sb.append(data);
		sb.append(block==null?("{}"):(" {"+block+"}"));
		return sb.toString();
	}
	
	public ScopeTree<T> reducedNode() {
		ScopeTree<T> node = this;
		while (true) {
			if (node.parent==null) {
				return node;
			}
			if (node.parent.line==node) {
				node = node.parent;
			} else {
				return node;
			}
		}
	}
	
	public T getReducedData() {
		return reducedNode().data;
	}
	
	public void setReducedData(T newData) {
		reducedNode().data = newData;
	}
	
	public Stack<T> contentsToNode() {
		Stack<T> st = new Stack<>();
		ScopeTree<T> node = this;
		while (node!=null) {
			st.push(node.data);
			node = node.parent;
		}
		//reverse the stack
		Stack<T> st2 = new Stack<>();
		for (T item : st) {
			st2.push(item);
		}
		return st2;
	}
	
	public <K> Stack<K> contentsToNode(TreeFlattener<T,K> f) {
		Stack<T> st = new Stack<>();
		ScopeTree<T> node = this;
		while (node!=null) {
			st.push(node.data);
			node = node.parent;
		}
		//reverse the stack
		Stack<K> st2 = new Stack<>();
		for (T item : st) {
			st2.push(f.flatten(item));
		}
		return st2;
	}
}
