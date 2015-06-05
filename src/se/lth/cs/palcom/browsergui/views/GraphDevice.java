package se.lth.cs.palcom.browsergui.views;

import java.util.ArrayList;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import ist.palcom.resource.descriptor.AbstractServiceDecl;
import ist.palcom.resource.descriptor.CommandEvent;
import ist.palcom.resource.descriptor.ServiceDecl;
import se.lth.cs.palcom.discovery.proxy.PalcomService;

public class GraphDevice implements Comparable {
	final static int PORT_DIAMETER = 20;
	final int PORT_RADIUS = PORT_DIAMETER / 2;
	public static mxGeometry geo1 = new mxGeometry(0, 0.5, PORT_DIAMETER,	PORT_DIAMETER);
	public static mxGeometry geo2 = new mxGeometry(1.0, 0.5, PORT_DIAMETER,PORT_DIAMETER);

	public final static int DEFAULT_HEIGHT = 40;
	final String xml;
	String name;
	Node root;
	int height;
	mxCell cell;
	private mxCell add;
	public boolean disconnected;
	public String id;
	public String type;

	private ArrayList<mxCell> createdCells;

	public GraphDevice(mxCell cell, mxCell add, boolean disconnected, String id, String type, String xml, String name){
		this.cell = cell;
		this.add = add;
		this.disconnected = disconnected;
		this.id = id;
		this.type = type;
		this.xml = xml;
		this.name = name;

		createdCells = new ArrayList<mxCell>();
		height = DEFAULT_HEIGHT;
		root = new Node(NodeType.SERVICELIST, "root", null, null);
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

	public mxCell hideService(String cellId){
		add.setVisible(true);
		return recHideService(root, cellId);
	}
	
	private mxCell recHideService(Node node, String id){
		if(node.nt == NodeType.SERVICELIST){
			for(Node n:node.children){
				mxCell removedCell = recHideService(n, id);
				if (removedCell != null) return removedCell;
			}
		}else{
			if(node.nodeCell != null && id.equals(node.nodeCell.getId())){
				node.removeCommandCells();
				node.added = false;
				return node.nodeCell;
			}
		}
		return null;
	}

	public Node addNode(Node parent, NodeType nt, String name, String palcomServiceId, AbstractServiceDecl asd){
		if(parent == null){
			parent = root;
		}
		Node n = new Node(nt, name, palcomServiceId, asd);
		parent.children.add(n);
		return n;
	}
	public Node findOrAddNode(String name, GraphEditor graphEditor, String palcomServiceId, AbstractServiceDecl asd){

		Node node = recFindNode(root, name);
		if (node == null){
			node = new Node(NodeType.SERVICE, name, palcomServiceId, asd);
			root.children.add(node);
		}else{
			node.palcomServiceId = palcomServiceId;
		}

		return node;
	}


	private Node recFindNode(Node parent, String name){
		if(parent.nt == NodeType.SERVICELIST){
			for(Node n:parent.children){
				Node retNode = recDisplayService(n, name);
				if (retNode != null) return retNode;
			}
		}else{
			if(name.equals(parent.name)){
				return parent;
			}
		}
		return null;
	}
	
	public ArrayList<Node> getUsedServices(){
		ArrayList<Node> nodes = new ArrayList<Node>();
		recGetUsedServices(root,nodes);
		return nodes;
	}
	private void recGetUsedServices(Node parent, ArrayList<Node> nodes){
		if(parent.nt == NodeType.SERVICELIST){
			for(Node n:parent.children){
				recGetUsedServices(n,nodes);
			}
		}else{
			if(parent.added){
				nodes.add(parent);
			}
		}
	}

	public class Command{
        CommandEvent ce;
        boolean in;
		String name;
		String type;
		mxCell commandCell;

		public Command(boolean in, String name, String type, CommandEvent ce){
			this.in = in;
			this.name = name;
			this.type = type;
            this.ce = ce;
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
		String palcomServiceId;
		AbstractServiceDecl asd;

		ArrayList<Command> inCommands;		
		ArrayList<Command> outCommands;
		
		public int getHeight(){
			return Math.max(30*Math.max(inCommands.size(), outCommands.size()),20);
		}
		
		public Node(NodeType nt, String name, String palcomServiceId, AbstractServiceDecl asd){
			children = new ArrayList<Node>();
			this.nt = nt;
			this.name = name;
			this.palcomServiceId = palcomServiceId;
			this.asd = asd;
			added = false;
			inCommands = new ArrayList<GraphDevice.Command>();
			outCommands = new ArrayList<GraphDevice.Command>();
		}
		
		public void add(NodeType nt, String name, String palcomServiceId, AbstractServiceDecl asd){
			children.add(new Node(nt, name, palcomServiceId, asd));
		}
		
		public void addCommand(boolean in, String name, String type, CommandEvent ce){
			if(in){
				inCommands.add(new Command(in, name, type, ce));
			}else{
				outCommands.add(new Command(in, name, type, ce));
			}
		}
		
		public ArrayList<Command> getInCommands(){
			return inCommands;
		}
		public ArrayList<Command> getOutCommands(){
			return outCommands;
		}

		public void removeCommandCells() {
			for(Command c:inCommands){
				c.commandCell = null;
			}
			for(Command c:outCommands){
				c.commandCell = null;
			}
		}

		public mxCell getCommandCell(String commandName){
			for(Command c:outCommands){
				if(c.getName().equalsIgnoreCase(commandName)){
					return c.commandCell;
				}
			}
			for(Command c:inCommands){
				if(c.getName().equalsIgnoreCase(commandName)){
					return c.commandCell;
				}
			}
			return null;
		}

	}
	
	public Node displayService(String name){
		Node node = recDisplayService(root, name);
		if(!hasUnAddedServices(root)){
			add.setVisible(false);
		}
		return node;
	}
	public Node recDisplayService(Node node, String name){
		if(node.nt == NodeType.SERVICELIST){
			for(Node n:node.children){
				Node retNode = recDisplayService(n, name);
				if (retNode != null) return retNode;
			}
		}else{
			if(name.equals(node.name)){
				node.added = true;
//				node.palcomServiceId = palcomServiceId;
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
