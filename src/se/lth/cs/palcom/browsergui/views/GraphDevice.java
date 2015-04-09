package se.lth.cs.palcom.browsergui.views;

import java.util.ArrayList;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

public class GraphDevice implements Comparable {
	final static int PORT_DIAMETER = 20;
	final int PORT_RADIUS = PORT_DIAMETER / 2;
	public static mxGeometry geo1 = new mxGeometry(0, 0.5, PORT_DIAMETER,	PORT_DIAMETER);
	public static mxGeometry geo2 = new mxGeometry(1.0, 0.5, PORT_DIAMETER,PORT_DIAMETER);

	public final static int DEFAULT_HEIGHT = 40;
	Node root;
	int height;
	mxCell cell;
	private mxCell add;
	public boolean disconnected;
	
	private ArrayList<mxCell> createdCells;

	public GraphDevice(mxCell cell, mxCell add, boolean disconnected){
		this.cell = cell;
		this.add = add;
		this.disconnected = true;
		createdCells = new ArrayList<mxCell>();
		height = DEFAULT_HEIGHT;
		root = new Node(NodeType.SERVICELIST, "root");
	}
	
	public void rerender(){
		if(!hasUnAddedServices(root)){
			height = DEFAULT_HEIGHT-20;
		}else{
			height = DEFAULT_HEIGHT;			
		}
		recRerender(root);
		
		cell.getGeometry().setHeight(height);
	}
	
	private void recRerender(Node node){
		if(node.nt == NodeType.SERVICELIST){
			for(Node n:node.children){
				recRerender(n);
			}
		}else{
			if(node.nodeCell != null && node.added){
				node.nodeCell.getGeometry().setY(height);
				height += node.getHeight();
			}
		}
	}


	public String getId() {
		return cell.getId();
	}

	public mxCell removeService(String cellId){
		add.setVisible(true);
		return recRemoveService(root, cellId);
	}
	
	private mxCell recRemoveService(Node node, String id){
		if(node.nt == NodeType.SERVICELIST){
			for(Node n:node.children){
				mxCell removedCell = recRemoveService(n, id);
				if (removedCell != null) return removedCell;
			}
		}else{
			if(node.nodeCell != null && id.equals(node.nodeCell.getId())){
				node.added = false;
//				node.nodeCell.removeFromParent();
				return node.nodeCell;
			}
		}
		return null;
	}

	public Node addNode(Node parent, NodeType nt, String name){
		if(parent == null){
			parent = root;
		}
		Node n = new Node(nt, name);
		parent.children.add(n);
		return n;
	}
	public Node findOrAddNode(String name){
		Node node = recFindNode(root, name);
		if(node == null){
			node = new Node(NodeType.SERVICE, name);
			root.children.add(node);
		}
		return node;
	}
	private Node recFindNode(Node parent, String name){
		if(parent.nt == NodeType.SERVICELIST){
			for(Node n:parent.children){
				Node retNode = recAddService(n, name);
				if (retNode != null) return retNode;
			}
		}else{
			if(name.equals(parent.name)){
				return parent;
			}
		}
		return null;
	}
	
	
	public class Command{
		boolean in;
		String name;
		String type;
		public Command(boolean in, String name, String type){
			this.in = in;
			this.name = name;
			this.type = type;
		}
		
		String getName(){
			return name;
		}
		boolean isIn(){
			return in;
		}
		String getType(){
			return type;
		}
	}
	public class Node {
		ArrayList<Node> children;
		NodeType nt;
		String name;
		boolean added;
		String id;
		mxCell nodeCell;
		
		ArrayList<Command> inCommands;		
		ArrayList<Command> outCommands;
		
		public int getHeight(){
			return Math.max(30*Math.max(inCommands.size(), outCommands.size()),20);
		}
		
		public Node(NodeType nt, String name){
			children = new ArrayList<Node>();
			this.nt = nt;
			this.name = name;
			added = false;
			inCommands = new ArrayList<GraphDevice.Command>();
			outCommands = new ArrayList<GraphDevice.Command>();
		}
		
		public void add(NodeType nt, String name){
			children.add(new Node(nt, name));
		}
		
		public void addCommand(boolean in, String name, String type){
			if(in){
				inCommands.add(new Command(in, name, type));
			}else{
				outCommands.add(new Command(in, name, type));
			}
		}
		
		public ArrayList<Command> getInCommands(){
			return inCommands;
		}
		public ArrayList<Command> getOutCommands(){
			return outCommands;
		}
	}
	
	public Node addService(String name){
		Node node = recAddService(root,name);
		if(!hasUnAddedServices(root)){
			add.setVisible(false);
		}
		return node;
	}
	public Node recAddService(Node node, String name){
		if(node.nt == NodeType.SERVICELIST){
			for(Node n:node.children){
				Node retNode = recAddService(n, name);
				if (retNode != null) return retNode;
			}
		}else{
			if(name.equals(node.name)){
				node.added = true;
				return node;
			}
		}
		return null;
	}

	private boolean hasUnAddedServices(Node node){
		if(node.nt == NodeType.SERVICELIST){
			for(Node n:node.children){
				if(hasUnAddedServices(n)) return true;
			}
		}else{
			return !node.added;
		}
		return false;
	}

	public enum NodeType {
		SERVICE, SERVICELIST 
	}


	public int compareTo(Object o) {	
		if(o instanceof GraphDevice){
			GraphDevice gd = (GraphDevice) o;
			return cell.getId().compareTo(gd.cell.getId());
		}else{
			return -1;
		}
	}

}
