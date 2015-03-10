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
//		System.out.println("Added node: "+ name);
		return n;
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
		
		ArrayList<Command> commands;
		
		public Node(NodeType nt, String name){
			children = new ArrayList<Node>();
			this.nt = nt;
			this.name = name;
			added = false;
			commands = new ArrayList<GraphDevice.Command>();
		}
		
		public void add(NodeType nt, String name){
			children.add(new Node(nt, name));
		}
		
		public void addCommand(boolean in, String name, String type){
//			System.out.println("  added command: " + name );
			commands.add(new Command(in, name, type));
		}
		
		public ArrayList<Command> getCommands(){
			return commands;
		}
	}
	
	public Node addService(String name){
		Node node = recAddService(root,name);
		if(!hasUnAddedServices(root)){
			add.setVisible(false);
		}
		return node;
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

	
	public enum NodeType {
		SERVICE, SERVICELIST 
	}


	public double increseHeight(int addHeight) {
		int oldHeight = height;
		height+=addHeight+10;
		return oldHeight;
	}

}
