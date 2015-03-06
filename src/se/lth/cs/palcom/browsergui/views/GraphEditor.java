package se.lth.cs.palcom.browsergui.views;

import ist.palcom.resource.descriptor.Address;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import se.lth.cs.palcom.browsergui.dnd.AssemblyGraphTransferHandler;
import se.lth.cs.palcom.browsergui.dnd.GraphServiceTree;
import se.lth.cs.palcom.browsergui.views.GraphDevice.Node;
import se.lth.cs.palcom.browsergui.views.GraphDevice.NodeType;
import se.lth.cs.palcom.browsergui.views.GraphDeviceView.AddServiceMenu;
import se.lth.cs.palcom.discovery.DeviceProxy;
import se.lth.cs.palcom.discovery.ResourceException;
import se.lth.cs.palcom.discovery.ServiceListProxy;
import se.lth.cs.palcom.discovery.ServiceProxy;
import se.lth.cs.palcom.discovery.proxy.PalcomServiceList;
import se.lth.cs.palcom.discovery.proxy.PalcomServiceListPart;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

public class GraphEditor extends JPanel {

	private static final long serialVersionUID = 8906103669540394160L;
	private HashSet<DeviceProxy> devices;
	private String assemblyData;
	private mxGraph graph;
	private TreeMap<String, GraphDevice> graphDevices;
	private GraphDeviceView gDV;

	final static int PORT_DIAMETER = 20;
	final int PORT_RADIUS = PORT_DIAMETER / 2;
	public static mxGeometry geo1 = new mxGeometry(0, 0.5, PORT_DIAMETER,	PORT_DIAMETER);
	public static mxGeometry geo2 = new mxGeometry(1.0, 0.5, PORT_DIAMETER,PORT_DIAMETER);
	
	
	public GraphEditor(){
		graph = new AwesomemxGraph();
		graphDevices = new TreeMap<String, GraphDevice>();
		gDV = new GraphDeviceView(this);
		
		final mxGraphComponent graphComponent = new mxGraphComponent(graph);
		new mxKeyboardHandler(graphComponent);
		
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent e){
				mxCell cell = (mxCell)graphComponent.getCellAt(e.getX(), e.getY());
				if (cell != null && graph.getLabel(cell).equalsIgnoreCase("+")){
					String id = cell.getParent().getId();
					final GraphDevice gD = graphDevices.get(id);
					if(gD != null){
						AddServiceMenu menu = gDV.createServiceMenu(gD.root.children, id);
						menu.show(e.getComponent(), e.getX(), e.getY());
					}

				}else if(cell != null && e.getButton() == MouseEvent.BUTTON3 && !cell.getParent().getId().equals("1")){
					//TODO, rightclick
				}
			}
		});



		
		geo2.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
		geo2.setRelative(true);
		geo1.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
		geo1.setRelative(true);
		
		graph.setHtmlLabels(true);
		graph.setAllowDanglingEdges(false);
		graph.setCellsDeletable(true);
		graph.setCellsResizable(false);;

		setLayout(new BorderLayout());

		add(graphComponent,BorderLayout.CENTER);
		JLabel dropArea = new JLabel("<html>Drop<br>device<br>here</html>");
		Border paddingBorder = BorderFactory.createEmptyBorder(10,10,10,10);
		Border border = BorderFactory.createLineBorder(Color.BLUE);
		dropArea.setBorder(BorderFactory.createCompoundBorder(border,paddingBorder));
		dropArea.setBackground(Color.WHITE);
		dropArea.setOpaque(true);
		dropArea.setTransferHandler(new AssemblyGraphTransferHandler(this));


		add(dropArea,BorderLayout.WEST);
		graphComponent.setToolTips(true);
		this.add(graphComponent);
		devices = new HashSet<DeviceProxy>();
	}

	public void addVertex(String id, String name) {
		GraphDevice gd = graphDevices.get(id);
		
		mxCell newService = (mxCell) graph.insertVertex(gd.cell, null, name, 0, gd.increseHeight(), 80, 30, "");
		newService.setConnectable(false);

		mxCell portI1 = new mxCell("", geo1,"shape=ellipse;perimter=ellipsePerimeter;fillColor=blue");
		portI1.setVertex(true);		
		graph.addCell(portI1, newService);		


		mxCell portO1 = new mxCell("", geo2,"shape=ellipse;perimter=ellipsePerimeter;fillColor=green");
		portO1.setVertex(true);		
		graph.addCell(portO1, newService);		
		
		gd.addService(newService);
		
		graph.refresh();
	}
	
	
	/**
	 * Adds a device to the Graph
	 * 
	 * @param d
	 *            device to add
	 * @return if successfully added return true else false
	 */

	public boolean addDevice(DeviceProxy d) {
		if (devices.contains(d)) {
			return false;
		}
		return devices.add(d);
	}

	/**
	 * Creates an XML of the complete graph
	 * 
	 * @return xml as string
	 */
	public String getXML() {
		return assemblyData;
	}

	/**
	 * Creates the graph from the assemblyData
	 * 
	 * @param assemblyData
	 *            data in xml that contains assembly information
	 */
	public void setGraph(String assemblyData) {
		this.assemblyData = assemblyData;
	}

//	public mxGraph getGraph() {
//		return graph;
//	}

	public void addGraphDevice(String key, GraphDevice gd) {
		graphDevices.put(key, gd);
	}

	public void importDevice(int y, DeviceProxy data) throws ResourceException {
		DeviceProxy res = (DeviceProxy) data;
		mxCell cell = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, "<b>" + res.getName() + "</b>", 100, y, 80, 30, "verticalAlign=top;textAlign=center");
		cell.setConnectable(false);
		mxCell add = (mxCell) graph.insertVertex(cell, null, "+", 0, 20, 80, 20);

		graph.refresh();
		GraphDevice gd = new GraphDevice(cell, add);

		PalcomServiceList services = res.getServiceList();
		for (int i = 0; i < services.getNumService(); i++) {
			recrusiveGetServices(null, gd, services.getService(i));
			Address ser = services.getService(i).getAddress();
			for (int k = 0; k < ser.getNumChild(); k++) {
			}
		}

		addGraphDevice(cell.getId(), gd);
	}

	private void recrusiveGetServices(Node parent, GraphDevice gd,
			PalcomServiceListPart psp) throws ResourceException {
		if (psp instanceof ServiceProxy) {
			gd.addNode(parent, NodeType.SERVICE, psp.getName());
		} else if (psp instanceof ServiceListProxy) {
			ServiceListProxy slp = (ServiceListProxy) psp;
			Node newParent = gd.addNode(parent, NodeType.SERVICELIST,
					slp.getName() + " LIST");
			for (int i = 0; i < slp.getNumService(); i++) {
				recrusiveGetServices(newParent, gd, slp.getService(i));
			}
		}
	}
}
