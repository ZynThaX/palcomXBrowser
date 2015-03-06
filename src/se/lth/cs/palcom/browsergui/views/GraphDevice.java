package se.lth.cs.palcom.browsergui.views;

import java.util.ArrayList;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

public class GraphDevice {
	final static int PORT_DIAMETER = 20;
	final int PORT_RADIUS = PORT_DIAMETER / 2;
	public static mxGeometry geo1 = new mxGeometry(0, 0.5, PORT_DIAMETER,	PORT_DIAMETER);
	public static mxGeometry geo2 = new mxGeometry(1.0, 0.5, PORT_DIAMETER,PORT_DIAMETER);

	Node root;
	int height;
	mxCell cell;
	private mxCell add;

	private ArrayList<mxCell> createdCells;

	public GraphDevice(mxCell cell, mxCell add){
		this.cell = cell;
		this.add = add;
		createdCells = new ArrayList<mxCell>();
		height = 40;
		root = new Node(NodeType.SERVICELIST, "root");
	}


	public String getId() {
		return cell.getId();
	}

	public void removeService(mxCell removeCell){
		recRemoveService(root, removeCell.getId());
	}
	private boolean recRemoveService(Node node, String id){
		if(node.nt == NodeType.SERVICELIST){
			for(Node n:node.children){
				if (recRemoveService(n, id)) return true;
			}
		}else{
			if(node.id.equals(id)){
				node.added = false;
				return true;
			}
		}
		return false;
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
		String id;
		mxCell nodeCell;
		
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
	
	public boolean addService(mxCell addCell){
		boolean ret = recAddService(root,addCell.getValue().toString());
		if(!hasUnAddedServices(root)){
			add.setVisible(false);
		}
		return ret;
	}
	
	public boolean hasUnAddedServices(Node node){
		if(node.nt == NodeType.SERVICELIST){
			for(Node n:node.children){
				if(hasUnAddedServices(n)) return true;
			}
		}else{
			return !node.added;
		}
		return false;
	}
	
	public boolean recAddService(Node node, String name){
		if(node.nt == NodeType.SERVICELIST){
			for(Node n:node.children){
				if (recAddService(n, name)) return true;
			}
		}else{
			if(name.equals(node.name)){
				node.added = true;
				return true;
			}
		}
		return false;
	}

	
	public enum NodeType {
		SERVICE, SERVICELIST 
	}


	public double increseHeight() {
		int oldHeight = height;
		height+=40;
		return oldHeight;
	}

	
	
//	class AddServiceMenu extends JPopupMenu{
//		public AddServiceMenu(ArrayList<Node> children){
//			recAddMenu(this,children);
//		}
//		private void recAddMenu(JComponent jm, ArrayList<Node> children){
//			for(final Node n:children){
//				if(n.added != true){	
//					if(n.nt == NodeType.SERVICE){
//						final JMenuItem item = new JMenuItem(n.name);
//		            	
//		            	
//						item.addActionListener(new ActionListener() {
//							public void actionPerformed(ActionEvent e) {
//								n.added = true;
//								mxCell newFunc = (mxCell) graph.insertVertex(cell, null, n.name, 0, height, 80, 30, "");
//								newFunc.setConnectable(false);
//								
//								
//								mxCell portI1 = new mxCell("", geo1,"shape=ellipse;perimter=ellipsePerimeter;fillColor=blue");
//								portI1.setVertex(true);		
//								graph.addCell(portI1, newFunc);		
//								
//								
//								mxCell portO1 = new mxCell("", geo2,"shape=ellipse;perimter=ellipsePerimeter;fillColor=green");
//								portO1.setVertex(true);		
//								graph.addCell(portO1, newFunc);		
//								height += 40;
//							}
//						});
//						
//						
//						
//						
//						jm.add(item);
//	
//					}else if(n.nt == NodeType.SERVICELIST){
//						JMenu curr = new JMenu(n.name);
//						jm.add(curr);
//						recAddMenu(curr, n.children);
//					}
//				}
//			}
//		}
//	}
//	
//	
//	
//
//
//
//	public void printTree(Component component, int d, int e) {
//		AddServiceMenu menu = new AddServiceMenu(root.children);
//		
//		menu.show(component, d, e);
//	}

}
