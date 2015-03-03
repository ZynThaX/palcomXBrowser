package se.lth.cs.palcom.browsergui.dnd;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class GraphServiceTree {

	 
	Node root;
	
	
	public GraphServiceTree(){
		root = new Node(NodeType.SERVICELIST, "root");
	}
	
//	public void addRoot(NodeType nt, String name){
//		root.add(nt, name);
//	}
	
	public Node addNode(Node parent, NodeType nt, String name){
		if(parent == null){
			parent = root;
		}
		Node n = new Node(nt, name);
		parent.children.add(n);
		return n;
	}
	
	public class Node {
		ArrayList<Node> children;
		NodeType nt;
		String name;
		boolean added;
//		ArrayList<Command>
		public Node(NodeType nt, String name){
			children = new ArrayList<Node>();
			this.nt = nt;
			this.name = name;
			added = false;
		}
		
		public void add(NodeType nt, String name){
			children.add(new Node(nt, name));
		}
	}
	
	
	class AddServiceMenu extends JPopupMenu{
		public AddServiceMenu(ArrayList<Node> children){
			recAddMenu(this,children);
		}
		private void recAddMenu(JComponent jm, ArrayList<Node> children){
			for(final Node n:children){
				if(n.nt == NodeType.SERVICE){
					final JMenuItem item = new JMenuItem(n.name);
					
					item.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							System.out.println(n.name);
						}
					});
					
					jm.add(item);

				}else if(n.nt == NodeType.SERVICELIST){
					JMenu curr = new JMenu(n.name);
					jm.add(curr);
					recAddMenu(curr, n.children);
				}
			}
		}
	}
	
	
	
	
	public enum NodeType {
		SERVICE, SERVICELIST 
	}




	public void printTree(JComponent comp, int d, int e) {
//		AddServiceMenu menu = new AddServiceMenu(root.children);
//		
//		menu.show(comp, d, e);
	}

}
