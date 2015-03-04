package se.lth.cs.palcom.browsergui.dnd;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

public class GraphServiceTree {


	final static int PORT_DIAMETER = 20;
	final int PORT_RADIUS = PORT_DIAMETER / 2;
	public static mxGeometry geo1 = new mxGeometry(0, 0.5, PORT_DIAMETER,	PORT_DIAMETER);
	public static mxGeometry geo2 = new mxGeometry(1.0, 0.5, PORT_DIAMETER,PORT_DIAMETER);

	
	Node root;
	int height;
	mxGraph graph;
	mxCell cell;
	
	public GraphServiceTree(mxGraph graph, mxCell cell){
		root = new Node(NodeType.SERVICELIST, "root");
		height = 40;
		this.graph = graph;
		this.cell = cell;
		geo1.setRelative(true);
		geo2.setRelative(true);
		geo1.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
		geo2.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));

	}
	
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
				if(n.added != true){	
					if(n.nt == NodeType.SERVICE){
						final JMenuItem item = new JMenuItem(n.name);
		            	
		            	
						item.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								n.added = true;
								mxCell newFunc = (mxCell) graph.insertVertex(cell, null, n.name, 0, height, 80, 30, "");
								newFunc.setConnectable(false);
								
								
								mxCell portI1 = new mxCell("", geo1,"shape=ellipse;perimter=ellipsePerimeter;fillColor=blue");
								portI1.setVertex(true);		
								graph.addCell(portI1, newFunc);		
								
								
								mxCell portO1 = new mxCell("", geo2,"shape=ellipse;perimter=ellipsePerimeter;fillColor=green");
								portO1.setVertex(true);		
								graph.addCell(portO1, newFunc);		
								height += 40;
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
	}
	
	
	
	
	public enum NodeType {
		SERVICE, SERVICELIST 
	}




	public void printTree(Component component, int d, int e) {
		AddServiceMenu menu = new AddServiceMenu(root.children);
		
		menu.show(component, d, e);
	}

}
