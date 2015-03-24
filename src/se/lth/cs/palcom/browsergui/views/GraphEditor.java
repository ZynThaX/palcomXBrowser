package se.lth.cs.palcom.browsergui.views;

import internal.org.kxml2.io.KXmlParser;
import internal.org.xmlpull.v1.XmlPullParser;
import internal.org.xmlpull.v1.XmlPullParserException;
import ist.palcom.resource.descriptor.ASTNode;
import ist.palcom.resource.descriptor.CommandInfo;
import ist.palcom.resource.descriptor.ControlInfo;
import ist.palcom.resource.descriptor.List;
import ist.palcom.resource.descriptor.PRDServiceFMDescription;
import ist.palcom.resource.descriptor.ParamInfo;
import ist.palcom.resource.descriptor.SynthesizedService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import se.lth.cs.palcom.browsergui.dnd.AssemblyGraphTransferHandler;
import se.lth.cs.palcom.browsergui.views.GraphDevice.Command;
import se.lth.cs.palcom.browsergui.views.GraphDevice.Node;
import se.lth.cs.palcom.browsergui.views.GraphDevice.NodeType;
import se.lth.cs.palcom.browsergui.views.GraphDeviceView.AddServiceMenu;
import se.lth.cs.palcom.discovery.DeviceProxy;
import se.lth.cs.palcom.discovery.DiscoveryManager;
import se.lth.cs.palcom.discovery.PalcomControlServiceDescription;
import se.lth.cs.palcom.discovery.PalcomServiceDescription;
import se.lth.cs.palcom.discovery.ResourceException;
import se.lth.cs.palcom.discovery.ServiceListProxy;
import se.lth.cs.palcom.discovery.ServiceProxy;
import se.lth.cs.palcom.discovery.proxy.PalcomNetwork;
import se.lth.cs.palcom.discovery.proxy.PalcomServiceList;
import se.lth.cs.palcom.discovery.proxy.PalcomServiceListPart;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxMultiplicity;

public class GraphEditor extends JPanel {

	private static final long serialVersionUID = 8906103669540394160L;
	private HashSet<DeviceProxy> devices;
	private ArrayList<SynthesizedService> ssList;
	private String assemblyData;
	private mxGraph graph;
	private TreeMap<String, GraphDevice> graphDevices;
	private GraphDeviceView gDV;
	private JPanel southPanel;
	private JPanel centerPanel;
	private DiscoveryManager discoveryManager;

	public static int PORT_DIAMETER = 20;
	public static int PORT_RADIUS = PORT_DIAMETER / 2;

	public Document xmlDocument;
	public TreeMap<String,String> usedColors;
	public ArrayList<String> availableColors;
	
	public String getColor(String type){
		String foundColor = usedColors.get(type.toLowerCase());
		
		if(foundColor==null){
			Random rnd = new Random();
			String color = availableColors.remove(rnd.nextInt(availableColors.size()));
			usedColors.put(type.toLowerCase(), color);
			
			updateMultiplicities();
			return color;
		}else{
			return foundColor;
		}
	}
	
	private String reduceTypeName(String type){
		return type.replace("/", "");
	}
	
	public void updateMultiplicities(){
		Set<String> types = usedColors.keySet();
		mxMultiplicity[] multiplicities = new mxMultiplicity[types.size()*2];
		
		int i = 0;
		for(String type:types){
			type = reduceTypeName(type);
			multiplicities[i*2] = new mxMultiplicity(false, type + "Source", null, null, 0,
					"0", null, "Source Must Have No Incoming Edge", null, true);  
			
			multiplicities[i*2+1] = new mxMultiplicity(false, type+ "Target", null, null, 0,
					"100", Arrays.asList(new String[] {type + "Source"}), null, "Must be same type",true);
			i++;
		}
		
		graph.setMultiplicities(multiplicities);
	}
	
	
	public GraphEditor(DiscoveryManager discoveryManager){
		this.discoveryManager = discoveryManager;
		ssList = new ArrayList<SynthesizedService>();
		graph = new AwesomemxGraph();
		graphDevices = new TreeMap<String, GraphDevice>();
		gDV = new GraphDeviceView(this);
		usedColors = new TreeMap<String, String>();
		availableColors = new ArrayList<String>();
		availableColors.add("#F44336");
		availableColors.add("#E91E63");
		availableColors.add("#9C27B0");
		availableColors.add("#673AB7");
		availableColors.add("#2196F3");
		availableColors.add("#009688");
		availableColors.add("#8BC34A");
		availableColors.add("#CDDC39");
		mxConstants.DEFAULT_HOTSPOT = 1;
		centerPanel = new JPanel();
		centerPanel.setAutoscrolls(true);
		centerPanel.setLayout(new BorderLayout());
		
		southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		xmlDocument = mxDomUtils.createDocument();
		
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

//		Map<String, Object> style = graph.getStylesheet().getDefaultEdgeStyle();
//		style.put(mxConstants.STYLE_EDGE, mxEdgeStyle.OrthConnector);
		
		Map<String, Object> EdgeStyle = graph.getStylesheet().getDefaultEdgeStyle();
	    EdgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.OrthConnector);
	    EdgeStyle.put(mxConstants.STYLE_STROKEWIDTH, 1);
	    EdgeStyle.put(mxConstants.STYLE_ROUNDED, true);
//	    EdgeStyle.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ENTITY_RELATION); 
//	    EdgeStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
//	    EdgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
//	    EdgeStyle.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
//	    EdgeStyle.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
	    
	    
	    
		graph.setHtmlLabels(true);
		graph.setAllowDanglingEdges(false);
		graph.setCellsDeletable(true);
		graph.setCellsResizable(false);;

		
		setLayout(new BorderLayout());

		JLabel dropArea = new JLabel("<html>Drop<br>device<br>here</html>");
		Border paddingBorder = BorderFactory.createEmptyBorder(10,10,10,10);
		Color lightBlue = new Color(202, 221, 237);
		Border border = BorderFactory.createLineBorder(Color.GRAY);
		dropArea.setBorder(BorderFactory.createCompoundBorder(border,paddingBorder));
		dropArea.setBackground(new Color(217, 217, 217)); //new Color(202, 221, 237)
		dropArea.setForeground(Color.WHITE);
		dropArea.setOpaque(true);
		dropArea.setTransferHandler(new AssemblyGraphTransferHandler(this));


		southPanel.setAutoscrolls(true);
		southPanel.setBorder(BorderFactory.createLineBorder(lightBlue, 3));
		//southPanel.setPreferredSize(new Dimension(100, 150));
		final GraphSynthServicePanel servicePanel = new GraphSynthServicePanel(this);
		final String buttonLabel = "Synthesised Services";
		final JButton synthServiceBtn = new JButton("︾" + " " + buttonLabel);
		synthServiceBtn.setBackground(lightBlue);
		synthServiceBtn.setBorder(null);
		synthServiceBtn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(servicePanel.isVisible()){
					synthServiceBtn.setText("︽" + " " + buttonLabel);
				} else {
					synthServiceBtn.setText("︾" + " " + buttonLabel);
				}
				servicePanel.toggle();
			}
		});
		southPanel.add(synthServiceBtn, BorderLayout.NORTH);
		southPanel.add(servicePanel, BorderLayout.CENTER);
		graphComponent.setToolTips(true);
		//this.add(graphComponent);
		centerPanel.add(graphComponent);
		devices = new HashSet<DeviceProxy>();
		add(centerPanel, BorderLayout.CENTER);
		add(dropArea,BorderLayout.WEST);
		add(southPanel, BorderLayout.SOUTH);
	}

	public void addVertex(String id, String name) {
		ArrayList<Command> inCommands = new ArrayList<Command>();
		ArrayList<Command> outCommands = new ArrayList<Command>();
		
		GraphDevice gd = graphDevices.get(id);
				
		for(Command c:gd.addService(name).getCommands()){
			if(c.isIn()){
				inCommands.add(c);
			}else{
				outCommands.add(c);
			}
		}

		int height = Math.max(Math.max(inCommands.size(), outCommands.size())*30,20);
				
		mxCell newService = (mxCell) graph.insertVertex(gd.cell, null, name, 0, gd.increseHeight(height), 150, height, "");
		newService.setConnectable(false);
		
		createPorts(inCommands,true,newService);
		createPorts(outCommands,false,newService);
		
		graph.refresh();
	}
	
	private void createPorts(ArrayList<Command> commands, boolean isIn, mxCell parent){
		String css = "shape=ellipse;perimter=ellipsePerimeter;align=right;spacingRight=20;portConstraint=east;";
		double xRel = 1.0;
		String typeExtenstion = "Source";
		if(isIn){
			xRel = 0.0;
			css = "shape=ellipse;perimter=ellipsePerimeter;align=left;spacingLeft=20;portConstraint=west;";
			typeExtenstion = "Target";
		}		
		
		for(int i=0;i<commands.size();i++){
			Command c = commands.get(i);
			double top = (i*2+1)/(commands.size()*2.0);
			mxGeometry outGeo = new mxGeometry(xRel, top, PORT_DIAMETER, PORT_DIAMETER);
			outGeo.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
			outGeo.setRelative(true);
			String color = getColor(c.getType());

			Element elem = xmlDocument.createElement(reduceTypeName(c.type) + typeExtenstion);
			elem.setAttribute("name", c.name);
			
			
			mxCell port = new mxCell(elem, outGeo,css + "fillColor="+color);
			port.setVertex(true);		
			graph.addCell(port, parent);	
		}
	}

	public boolean addDevice(DeviceProxy d) {
		if (devices.contains(d)) {
			return false;
		}
		return devices.add(d);
	}

	public String getXML() {
		return assemblyData;
	}

	public void setGraph(String assemblyData) {
		this.assemblyData = assemblyData;
		updateGraphWithData();
	}

	private void updateGraphWithData() {
		//se OldschoolAssemblyLoader
		
		XmlPullParser factory = new KXmlParser();
		
		int topPos = 10;
		
		TreeMap<String, GraphDevice> devices = new TreeMap<String, GraphDevice>();
		TreeMap<String, Node> serviceNodes = new TreeMap<String, Node>();

		TreeMap<GraphDevice, ArrayList<String>> servicesToAdd = new TreeMap<GraphDevice, ArrayList<String>>();
		
		
		
		PalcomNetwork pcn = discoveryManager.getNetwork();
		se.lth.cs.palcom.common.collections.List networkDevices = pcn.getDevices();
		
		
		try {
			byte[] xmlBytes = assemblyData.getBytes("UTF8");
			
			factory.setInput(new InputStreamReader(new ByteArrayInputStream(xmlBytes, 0, xmlBytes.length)));
			factory.nextTag();
			
			xmlGoTo(factory,"DeviceDeclList");
			factory.nextTag();
			
			while(!factory.getName().equals("DeviceDeclList")){
				String id= factory.getAttributeValue("", "id");
				if(factory.getName().equals("Identifier") && id != null){
					
					xmlGoTo(factory,"DID");
					System.out.println("device id: " + factory.getAttributeValue("", "id"));
					for(int i = 0;i<networkDevices.size();i++){
						Object device = networkDevices.get(i);
						if(device instanceof DeviceProxy){
							DeviceProxy devicep =(DeviceProxy) device;
							System.out.println(devicep.getDeviceID());
							if(devicep.getDeviceID().toString().equals(factory.getAttributeValue("", "id"))){
								GraphDevice gd = importDevice(topPos, devicep);	
								devices.put(id, gd);
							}
						}
						System.out.println(networkDevices.get(i).getClass());
					}
					
					topPos+=100;
				}
				
				
				
				
				
				factory.nextTag();
			}
			
			xmlGoTo(factory,"ServiceDeclList");
			factory.nextTag();
			
			String serviceId;
			String[] names = new String[2];//Fullösning, fixa bättre!
			
			while(!factory.getName().equals("ServiceDeclList")){
				if(factory.getName().equals("ServiceDecl")){
					xmlGoTo(factory,"Identifier");
					serviceId = factory.getAttributeValue("", "id");
					
					xmlGoTo(factory,"SingleServiceDecl");
					xmlGoTo(factory,"Identifier");
					names[0] = factory.getAttributeValue("", "id");//Service name
					
					xmlGoTo(factory,"DeviceUse");
					xmlGoTo(factory,"Identifier");
					names[1] = factory.getAttributeValue("", "id");//device name
					
					GraphDevice gd = devices.get(names[1]);
					
					
//					Node serviceNode = gd.addNode(null, NodeType.SERVICE, names[0]);
					
					
					if(servicesToAdd.containsKey(gd)){
						servicesToAdd.get(gd).add(names[0]);
					}else{
						ArrayList<String> nodes = new ArrayList<String>();
						nodes.add(names[0]);
						servicesToAdd.put(gd, nodes);
					}
					
//					serviceNodes.put(serviceId, serviceNode);
					
					xmlGoTo(factory,"ServiceDecl");
				}
				factory.nextTag();
			}
			
			xmlGoTo(factory,"EventHandlerScript");
			xmlGoTo(factory,"EventHandlerList");
			factory.nextTag();
			
			String commandName;
			String direction;
			String type = "ping";
			
			while(!factory.getName().equals("EventHandlerList")){
				if(factory.getName().equals("EventHandlerClause")){
					// TODO, detta för att avgöra var anslutningarna e gjorda
					
					xmlGoTo(factory,"CommandEvent");
					commandName = factory.getAttributeValue("", "commandName");
					
					xmlGoTo(factory,"ServiceUse");
					xmlGoTo(factory,"Identifier");
					serviceId = factory.getAttributeValue("", "id");
					
					xmlGoTo(factory,"CmdI");
					direction = factory.getAttributeValue("", "direction");
					factory.nextTag();
					if(factory.getName().equals("PI")){
						type = factory.getAttributeValue("", "type");
					}
					
//					Node serviceNode = serviceNodes.get(serviceId);
//					
//					serviceNode.addCommand(direction.toLowerCase().equals("in"), commandName, type);
										
					xmlGoTo(factory,"EventHandlerClause");
				}
				factory.nextTag();
			}

			for(GraphDevice gd:servicesToAdd.keySet()){
				ArrayList<String> nodes = servicesToAdd.get(gd);
				for(String nodeName:nodes){
					addVertex(gd.getId(), nodeName);
				}
			}
			
		} catch (UnsupportedEncodingException e) {
			//TODO
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void xmlGoTo(XmlPullParser factory, String tagName) throws XmlPullParserException, IOException{
		while(!factory.getName().equals(tagName)){
			factory.nextTag();
		}
	}

	public void addGraphDevice(String key, GraphDevice gd) {
		graphDevices.put(key, gd);
	}
	
	public void addSynthService(SynthesizedService n){
		ssList.add(n);
	}
	
	public ArrayList<SynthesizedService> getSynthServices(){
		return ssList;
	}
	public GraphDevice importDevice(int y, DeviceProxy data) throws ResourceException {
		DeviceProxy res = (DeviceProxy) data;
		mxCell cell = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, "<b>" + res.getName() + "</b>", 150, y, 100, 30, "verticalAlign=top;textAlign=center");
		cell.setConnectable(false);
		mxCell add = (mxCell) graph.insertVertex(cell, null, "+", 0, 20, 150, 20);
		add.setConnectable(false);

		graph.refresh();
		GraphDevice gd = new GraphDevice(cell, add);

		PalcomServiceList services = res.getServiceList();
		
		for (int i = 0; i < services.getNumService(); i++) {
			recrusiveGetServices(null, gd, services.getService(i));
		}

		addGraphDevice(cell.getId(), gd);
		return gd;
	}

	private void recrusiveGetServices(Node parent, GraphDevice gd, PalcomServiceListPart psp) throws ResourceException {
		if (psp instanceof ServiceProxy) {
			Node node = gd.addNode(parent, NodeType.SERVICE, psp.getName());

			ServiceProxy sp = (ServiceProxy) psp;
			
			PalcomServiceDescription psd = sp.getDescription();
			
			if(psd instanceof PalcomControlServiceDescription){
				PalcomControlServiceDescription pcsd = (PalcomControlServiceDescription) psd;
				PRDServiceFMDescription psfmd = pcsd.getPRDServiceFMDescription();
				if(psfmd != null){
					for(int i =0;i<psfmd.getNumControlInfo();i++){
						ControlInfo ctrlInfo = psfmd.getControlInfo(i);
						if(ctrlInfo instanceof CommandInfo){
							CommandInfo comI = (CommandInfo) ctrlInfo;
							String name = comI.getID();
							String type = "ping";
							boolean isIn = comI.getDirection().toLowerCase().equals("in");
							
							typeloop:
							for(int j = 0; j < comI.getNumChild();j++){
								ASTNode astn = comI.getChild(j);
								if(astn instanceof List){
									List list = (List)comI.getChild(j);
									for(int k = 0;k<list.getNumChild();k++){
										ParamInfo pi = (ParamInfo) list.getChild(k);
										type = pi.getType();
										break typeloop;
										//TODO, finns det fall då det existerar flera types till en funktion?
									}
								}
							}
							node.addCommand(isIn, name, type);
						}
					}
				}
			}
				
		} else if (psp instanceof ServiceListProxy) {
			ServiceListProxy slp = (ServiceListProxy) psp;
			Node newParent = gd.addNode(parent, NodeType.SERVICELIST, slp.getName());
			for (int i = 0; i < slp.getNumService(); i++) {
				recrusiveGetServices(newParent, gd, slp.getService(i));
			}
		}
	}
}
