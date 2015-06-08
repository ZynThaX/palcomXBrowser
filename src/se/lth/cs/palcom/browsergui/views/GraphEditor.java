package se.lth.cs.palcom.browsergui.views;

import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.util.*;
import internal.org.xmlpull.v1.XmlPullParserException;

import ist.palcom.resource.descriptor.*;
import ist.palcom.resource.descriptor.List;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
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
import javax.swing.JPopupMenu;
import javax.swing.border.Border;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.w3c.dom.UserDataHandler;
import se.lth.cs.palcom.assembly.AssemblyLoadException;
import se.lth.cs.palcom.browsergui.AssemblyPanel;
import se.lth.cs.palcom.browsergui.BrowserApplication;
import se.lth.cs.palcom.browsergui.dnd.AssemblyGraphTransferHandler;
import se.lth.cs.palcom.browsergui.views.GraphDevice.Command;
import se.lth.cs.palcom.browsergui.views.GraphDevice.Node;
import se.lth.cs.palcom.browsergui.views.GraphDevice.NodeType;
import se.lth.cs.palcom.browsergui.views.GraphSynthServicePanel.ServiceObjGUI;
import se.lth.cs.palcom.browsergui.views.GraphVariablePanel.VariableObjGUI;
import se.lth.cs.palcom.discovery.DeviceProxy;
import se.lth.cs.palcom.discovery.DiscoveryManager;
import se.lth.cs.palcom.discovery.PalcomControlServiceDescription;
import se.lth.cs.palcom.discovery.PalcomServiceDescription;
import se.lth.cs.palcom.discovery.ResourceException;
import se.lth.cs.palcom.discovery.ServiceListProxy;
import se.lth.cs.palcom.discovery.ServiceProxy;
import se.lth.cs.palcom.discovery.proxy.*;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxMultiplicity;

public class GraphEditor extends JPanel {

	private static final long serialVersionUID = 8906103669540394160L;
	private HashSet<DeviceProxy> devices;
	private ArrayList<ServiceObjGUI> ssList;
	private ArrayList<VariableObjGUI> variableList;
	private String assemblyData;

	private AwesomemxGraph graph;
	private TreeMap<String, GraphDevice> graphDevices;
	private TreeMap<String, GraphVariable> graphVariables;
	private GraphDeviceView gDV;
	private JPanel southPanel;
	private JPanel northPanel;
	private JPanel centerPanel;
	final GraphVariablePanel varPanel;
	final GraphSynthServicePanel servicePanel;
	private AssemblyPanel assemblyPanel;

	private DiscoveryManager discoveryManager;
	public static int PORT_DIAMETER = 20;
	public static int PORT_RADIUS = PORT_DIAMETER / 2;

	public final static String SYNTHESISED_SERVICE_NAME = "Synthesised service";
	public Document xmlDocument;
	public TreeMap<String,String> usedColors;
	public ArrayList<String> availableColors;
    public TreeMap<String, CommandEvent> graphCommandEvents;
    public TreeMap<String, ActionEvent> graphActionEvents;
    public TreeMap<String, Command> graphCommands;

	private int nextServiceId = 1;
    private BrowserApplication app;
    private String filename;

    public String getNextServiceId(){
		return "s" + nextServiceId++;
	}
	public void updateServiceId(String serviceId){
		int tempServiceId = Integer.parseInt(serviceId.substring(1));
		if(tempServiceId > nextServiceId)
			nextServiceId = tempServiceId;
	}

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
	
	public TreeMap<String, String> getAllUsedColors(){
		return usedColors;
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

	public GraphObjectsHandler getUpdatedGraphData(){
		return new GraphObjectsHandler(graphDevices, graphVariables);
	}

	public GraphEditor(DiscoveryManager discoveryManager, AssemblyPanel assemblyPanel){
		this.discoveryManager = discoveryManager;
		this.assemblyPanel = assemblyPanel;
		ssList = new ArrayList<ServiceObjGUI>();
		variableList = new ArrayList<VariableObjGUI>();
		graph = new AwesomemxGraph(this);
		graphDevices = new TreeMap<String, GraphDevice>();
		graphVariables = new TreeMap<String, GraphVariable>();
		gDV = new GraphDeviceView(this);
		usedColors = new TreeMap<String, String>();
        graphCommandEvents = new TreeMap<String, CommandEvent>();
        graphActionEvents = new TreeMap<String, ActionEvent>();
        graphCommands = new TreeMap<String, Command>();
		availableColors = new ArrayList<String>();
		availableColors.add("#F44336");
		availableColors.add("#E91E63");
		availableColors.add("#9C27B0");
		availableColors.add("#673AB7");
		availableColors.add("#2196F3");
		availableColors.add("#009688");
		availableColors.add("#8BC34A");
		availableColors.add("#CDDC39");
		
		usedColors.put("text/plain", "#FFFFFF");
		usedColors.put("text/cmd", "#000000");
		usedColors.put("ping", "#000000");
		usedColors.put("image/jpeg", "#3366FF");
		usedColors.put("image/gif", "#33CCFF");
		usedColors.put("image/png", "#6633FF");
		usedColors.put("video/avi", "#FF33CC");
		usedColors.put("video/mpeg", "#FF3366");
		usedColors.put("video/mp4", "#CC33FF");
		usedColors.put("audio/mp4", "#33FF66");
		usedColors.put("audio/mpeg", "#CCFF33");
		updateMultiplicities();

		mxConstants.DEFAULT_HOTSPOT = 1;
		centerPanel = new JPanel();
		centerPanel.setAutoscrolls(true);
		centerPanel.setLayout(new BorderLayout());
		
		southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		xmlDocument = mxDomUtils.createDocument();
		
		final mxGraphComponent graphComponent = new mxGraphComponent(graph);
		new mxKeyboardHandler(graphComponent);



		graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				mxCell cell = (mxCell) graphComponent.getCellAt(e.getX(), e.getY());
				if (cell != null && graph.getLabel(cell).equalsIgnoreCase("+")) {
					String id = cell.getParent().getId();
					final GraphDevice gD = graphDevices.get(id);
					if (gD != null) {
						JPopupMenu menu = gDV.createServiceMenu(gD.root.children, id);
						menu.show(e.getComponent(), e.getX() - (int) menu.getPreferredSize().getWidth() / 2, e.getY());
					}
				} else if (cell != null && e.getButton() == MouseEvent.BUTTON3 && !cell.getParent().getId().equals("1")) {
					JPopupMenu menu = gDV.createRemoveServiceMenu(cell);
					menu.show(e.getComponent(), e.getX() - menu.getWidth() / 2, e.getY());
				} else if (cell != null && e.getButton() == MouseEvent.BUTTON3 && !cell.getId().equals("1")) {
					JPopupMenu menu = gDV.createRemoveGraphDeviceMenu(cell);
					menu.show(e.getComponent(), e.getX() - menu.getWidth() / 2, e.getY());
				}
			}
		});


        // TODO, catcha alla händelser
//		graphComponent.getConnectionHandler().addListener(mxEvent.CONNECT, new mxEventSource.mxIEventListener() {
//			public void invoke(Object o, mxEventObject mxEventObject) {
//				// connection was made
//				mxConnectionHandler mch = (mxConnectionHandler) o;
//				mxCell cell = (mxCell)mxEventObject.getProperties().get("cell");
//				mxCell par = (mxCell) graph.getDefaultParent();
//
////				System.out.println(par.getId());
////				System.out.println(cell.getValue() + " - " + cell.getParent().getId());
//
////				System.out.println();
//				Object[] conns = graph.getConnections(cell.getSource(), par);
//
//				for(int i=0;i<conns.length;i++){
//					mxCell c = (mxCell) conns[i];
//					mxCell tar = (mxCell) c.getTarget();
//					mxCell src = (mxCell) c.getSource();
//
//					System.out.println(src.getValue() + " - " + tar.getValue());
//				}
//			}
//		});
//		graphComponent.addListener(mxEvent.CELLS_MOVED, new mxEventSource.mxIEventListener() {
//			public void invoke(Object o, mxEventObject mxEventObject) {
//				System.out.println("Update occured");
//			}
//		});
//		graphComponent.addListener(mxEvent.CELLS_MOVED, new mxEventSource.mxIEventListener() {
//			public void invoke(Object o, mxEventObject mxEventObject) {
//				System.out.println("Update occured");
//			}
//		});

//		graphComponent.getConnectionHandler().addListener(mxEvent.MARK, new mxEventSource.mxIEventListener() {
//			public void invoke(Object o, mxEventObject mxEventObject) {
//				System.out.println("Change occured");
//			}
//		});
		Map<String, Object> EdgeStyle = graph.getStylesheet().getDefaultEdgeStyle();
	    EdgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.OrthConnector);
	    EdgeStyle.put(mxConstants.STYLE_STROKEWIDTH, 1);
	    EdgeStyle.put(mxConstants.STYLE_ROUNDED, true);


		graph.setHtmlLabels(true);
		graph.setAllowDanglingEdges(false);
		graph.setCellsDeletable(true);
		graph.setCellsResizable(false);;
		graph.setDropEnabled(false);
		graph.setCellsEditable(false);

		setLayout(new BorderLayout());

		JLabel dropArea = new JLabel("<html>Drop<br>device, <br>variable<br> or SS here</html>");
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
		northPanel.setAutoscrolls(true);
		northPanel.setBorder(BorderFactory.createLineBorder(lightBlue, 3));
		//southPanel.setPreferredSize(new Dimension(100, 150));
		servicePanel = new GraphSynthServicePanel(this);
		varPanel = new GraphVariablePanel(this);
		varPanel.setVisible(false);
		servicePanel.setVisible(false);
		final String varBtnLabel = "Variables";
		final JButton varBtn = new JButton("︾" + " " + varBtnLabel);
		varBtn.setBackground(lightBlue);
		varBtn.setBorder(null);
		varBtn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(varPanel.isVisible()){
					varBtn.setText("︾" + " " + varBtnLabel);
				} else {
					varBtn.setText("︽" + " " + varBtnLabel);
				}
				varPanel.toggle();
			}
		});
		final String buttonLabel = "Synthesised Services (SS)";
		final JButton synthServiceBtn = new JButton("︽" + " " + buttonLabel);
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
		northPanel.add(varPanel, BorderLayout.NORTH);
		northPanel.add(varBtn, BorderLayout.CENTER);
		
		graphComponent.setToolTips(true);
		//this.add(graphComponent);
		centerPanel.add(graphComponent);
		devices = new HashSet<DeviceProxy>();
		add(centerPanel, BorderLayout.CENTER);
		add(dropArea,BorderLayout.WEST);
		add(southPanel, BorderLayout.SOUTH);
		add(northPanel, BorderLayout.NORTH);
	}

	public void showService(String id, String name) {
		showService(graphDevices.get(id), name);
	}
	public void showService(GraphDevice gd, String name) {
		Node n = gd.displayService(name);

		if(n.palcomServiceId == null && !gd.type.equalsIgnoreCase("synthesizedservice")){
			String newServiceId = getNextServiceId();
			n.palcomServiceId = newServiceId;
		}
		
		mxCell nodeCell = (mxCell) graph.insertVertex(gd.cell, null, name, 0, 0, 150, n.getHeight(),"fillColor=none");
		nodeCell.setConnectable(false);


		n.nodeCell = nodeCell;
		
		createPorts(n.getInCommands(), true, nodeCell);
		createPorts(n.getOutCommands(), false, nodeCell);

		gd.rerender();
		graph.refresh();
	}
	
	public void hideService(String parentId, String cellId){
		GraphDevice gd = graphDevices.get(parentId);
		mxCell removedCell = gd.hideService(cellId);
		graph.removeCells(new Object[]{removedCell});
		//TODO, ta bort child cells? connections.
		gd.rerender();
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


            c.commandCell = port;

            port.setVertex(true);
            graph.addCell(port, parent);
            graphCommands.put(port.getId(), c);
        }
	}

	public boolean addDevice(DeviceProxy d) {
		if (devices.contains(d)) {
			return false;
		}
		return devices.add(d);
	}

	public String getXML() throws CloneNotSupportedException {
        int connectionId = 0;
        PRDAssemblyVer version = new PRDAssemblyVer();
        version.setFormat("3.0.14");
        DeviceID did = app.getDevice().getDeviceID();
        version.setName(filename);
        version.setVersion(new AssemblyID(new VersionPart(did, "Version"), new VersionPart(did, "Version?"), new Opt(), new Opt(), "1.0"));

        DeviceDeclList devices = new DeviceDeclList();
        ServiceDeclList services = new ServiceDeclList();
        ConnectionDeclList connections = new ConnectionDeclList();
        EventHandlerScript eventHandler = new EventHandlerScript();
        VariableList variables = new VariableList();
        EventHandlerList events = new EventHandlerList();
        SynthesizedServiceList synthServices = new SynthesizedServiceList();

        for(ServiceObjGUI ssObj : ssList){
            synthServices.addSynthesizedService(ssObj.ss);
        }
        for(VariableObjGUI varObj : variableList){
            variables.addVariableDecl(varObj.var);
        }

		for(String graphId:graphDevices.keySet()){
			GraphDevice gd = graphDevices.get(graphId);
			if(!gd.type.equalsIgnoreCase("synthesizedservice")){
                devices.addDeviceDecl(gd.xml);
                for(Node node:gd.getUsedServices()){
                    ServiceUse su = new ServiceUse(new Identifier(node.palcomServiceId));
                    ServiceDecl decl = new ServiceDecl(new Identifier(node.palcomServiceId), node.asd);
                    services.addServiceDecl(decl);
                    boolean serviceHasConn = false;
					for(Command c:node.outCommands){

                        if(c.commandCell != null){
                            EventHandlerClause ehc = new EventHandlerClause();

                            Object[] edges = graph.getEdges(c.commandCell);
                            CommandEvent ce = c.ce;
                            ce.setServiceExp(su);

                            ehc.setEvent(ce);

                            if(!serviceHasConn && edges.length > 0){
                                ConnectionDecl conn = new ConnectionDecl(su, new ThisService(), "conn-" + (++connectionId),  new Opt());
                                connections.addConnectionDecl(conn);
                                serviceHasConn = true;
                            }
                            ArrayList<Action> actions = new ArrayList<Action>();

                            CommandInfo ci = ce.getCommandInfo();
                            String paramName = null;
                            for(int j =0;j<ci.getNumParamInfo();j++){
                                paramName = ci.getParamInfo(j).getID();
                                break;
                            }
                            for(int i=0;i<edges.length;i++){
                                mxCell target = (mxCell)((mxCell) edges[i]).getTarget();
                                System.out.println("Edge from: " + c.name + " to: " + target.getAttribute("name"));
                                actions.addAll(getTargetActions(target, paramName, null));
                            }
                            for(Action a:actions){
                                ehc.addAction(a);
                            }
                            if(actions.size() > 0){
                                events.addEventHandlerClause(ehc);
                            }

						}else{
							System.out.println("Commandcell is null: " + c.getName());
						}
					}
				}
			}else{
                for(Node node:gd.getUsedServices()){
                    for(Command c:node.outCommands){
                        if(c.commandCell != null){
                            EventHandlerClause ehc = new EventHandlerClause();
                            Object[] edges = graph.getEdges(c.commandCell);
                            CommandEvent ce = c.ce;
                            ce.setServiceExp(new SynthesizedServiceUse(new Identifier(gd.id)));
                            ehc.setEvent(ce);
                            ArrayList<Action> actions = new ArrayList<Action>();
                            CommandInfo ci = ce.getCommandInfo();
                            String paramName = null;
                            for(int j =0;j<ci.getNumParamInfo();j++){
                                paramName = ci.getParamInfo(j).getID();
                                break;
                            }
                            for(int i=0;i<edges.length;i++){
                                mxCell target = (mxCell)((mxCell) edges[i]).getTarget();
                                actions.addAll(getTargetActions(target, paramName, null));
                            }
                            for(Action a:actions){
                                ehc.addAction(a);
                            }
                            if(actions.size() > 0){
                                events.addEventHandlerClause(ehc);
                            }
                        }else{
                            System.out.println("Commandcell is null: " + c.getName());
                        }
                    }
                }
            }
		}
        eventHandler.setEventHandlers(events);
        eventHandler.setVariables(variables);
        version.setDevices(devices);
        version.setServices(services);
        version.setConnections(connections);
        version.setEventHandlerScript(eventHandler);
        version.setSynthesizedServices(synthServices);
        System.out.println("Bam bam bammmmm");

        System.out.println(version);

        return version.toString();
	}

    private ArrayList<Action> getTargetActions(mxCell target, String paramName, String variableName) throws CloneNotSupportedException {
        ArrayList<Action> actions = new ArrayList<Action>();
        List params = new List();
        if(paramName != null){
            params.add(new ParamUse(paramName));
        }
        if(variableName != null){
            params.add(new VariableUse(variableName));
        }

        Command targetCommand = graphCommands.get(target.getId());
        GraphVariable targetVar = graphVariables.get(getTopParentCell(target).getId());
        if(targetVar != null){
            if(target.getAttribute("name").equalsIgnoreCase("set variable")){
                AssignAction aa = new AssignAction(new VariableUse(targetVar.variable.getIdentifier().getID()), new ParamUse(paramName));
                actions.add(aa);
            }else{
                Object[] edges = graph.getEdges(targetVar.getOut);
                for(int i=0;i<edges.length;i++){
                    mxCell varTarget = (mxCell)((mxCell) edges[i]).getTarget();
                    actions.addAll(getTargetActions(varTarget, null, targetVar.variable.getIdentifier().getID()));
                }
            }
        }else if(targetCommand != null && targetCommand.awp != null){
            String actionName = targetCommand.awp.getCommand();
            if (targetCommand.awp instanceof SendMessageAction){
                SendMessageAction sma = new SendMessageAction(actionName, params, new ServiceUse(new Identifier(targetCommand.parent.palcomServiceId)));
                actions.add(sma);
            }else if(targetCommand.awp instanceof InvokeAction){
                InvokeAction ia = new InvokeAction(actionName, params, ((InvokeAction) targetCommand.awp).getSynthesizedServiceUse(), "", new Opt(), new Opt());
                actions.add(ia);
            }
        }else{
            System.out.println("Commandevent ActionWithParam is null, is it not a recieving command?");
        }


        return actions;
    }

    private mxCell getTopParentCell(mxCell cell){
        if(cell.getParent().getId().equalsIgnoreCase("1")){
            return cell;
        }else{
            return getTopParentCell((mxCell)cell.getParent());
        }
    }

	public void setGraph(String assemblyData, GraphObjectsHandler graphData, BrowserApplication app, String filename) {
		this.assemblyData = assemblyData;
        this.app = app;
        this.filename = filename;

		try {
			graph.updateGraphWithData(discoveryManager.getNetwork(), assemblyData, graphData, this);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AssemblyLoadException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (ResourceException e) {
			e.printStackTrace();
		}


	}

	public void addGraphDevice(String key, GraphDevice gd) {
		graphDevices.put(key, gd);
	}
	public void removeGraphDevice(String key, mxCell removeCell) {
		graphDevices.remove(key);
		graph.removeCells(new Object[]{removeCell});
	}
	public void addSynthService(SynthesizedService synSer){
		servicePanel.addService(synSer);
	}
	public void addSynthService(ServiceObjGUI serviceObjGUI){
		ssList.add(serviceObjGUI);
	}
	public void removeSynthService(SynthesizedService ss) {
		for(int i = 0; i<ssList.size(); i++){
			if(ssList.get(i).equals(ss)){
				ssList.remove(i);
				break;
			}
		}	
	}
	
	public ArrayList<VariableObjGUI> getVariables(){
		return variableList;
	}
	
	public void addVariable(VariableObjGUI varObjGUI){
		variableList.add(varObjGUI);
	}
	public void addVariable(VariableDecl varDec){
		varPanel.addVariable(varDec);
	}
	public void removeVariable(VariableDecl var) {
		for(int i = 0; i<variableList.size(); i++){
			if(variableList.get(i).equals(var)){
				variableList.remove(i);
				break;
			}
		}	
	}
	
	public ArrayList<ServiceObjGUI> getSynthServices(){
		return ssList;
	}

	public GraphDevice importDevice(Point p, DeviceProxy data) throws ResourceException {
		PalcomDevice dev = (PalcomDevice)data;
		DeviceDecl decl = new DeviceAddressDecl(new Identifier(dev.getName()), new DeviceAddress(dev.getDeviceID()));

		GraphDevice gd = createGraphDevice(data.getName(), p, false, data.getDeviceID().toString(), "device", decl);

		PalcomServiceList services = data.getServiceList();

		for (int i = 0; i < services.getNumService(); i++) {
			recrusiveGetServices(null, gd, services.getService(i), false);
		}

		return gd;
	}
	
	public GraphDevice importDevice(Point p, SynthesizedService data) throws ResourceException {
		PRDServiceFMDescription prds = data.getPRDServiceFMDescription();

		GraphDevice gd = createGraphDevice(prds.getID(), p, false, prds.getID(), "synthesizedservice", null);
		Node parent = gd.addNode(null, NodeType.SERVICE, prds.getID(),null,null);

		parseCommands(parent, prds, true);
		showService(gd, prds.getID());
		
		return gd;
	}

	public void importVariable(VariableDecl variable, Point p) {

		String name = variable.getIdentifier().getID();
		String type = ((MimeType)variable.getVariableType()).getTypeName();

		mxCell cell = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, "<b>" + name + "</b>", p.x, p.y, 100, 50, "verticalAlign=top;textAlign=center;fillColor=#9999FF");
		cell.setConnectable(false);


		String css1 = "shape=ellipse;perimter=ellipsePerimeter;align=left;spacingLeft=20;portConstraint=west;";
		String css2 = "shape=ellipse;perimter=ellipsePerimeter;align=right;spacingRight=20;portConstraint=east;";

		mxGeometry geo1 = new mxGeometry(0.0, 0.25, PORT_DIAMETER, PORT_DIAMETER);
		mxGeometry geo2 = new mxGeometry(0.0, 0.75, PORT_DIAMETER, PORT_DIAMETER);
		mxGeometry geo3 = new mxGeometry(1.0, 0.5, PORT_DIAMETER, PORT_DIAMETER);
		geo1.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
		geo2.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
		geo3.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
		geo1.setRelative(true);
		geo2.setRelative(true);
		geo3.setRelative(true);

		Element elem1 = xmlDocument.createElement(reduceTypeName(type) + "Target");
		Element elem2 = xmlDocument.createElement("pingTarget");
		Element elem3 = xmlDocument.createElement(reduceTypeName(type) + "Source");
		elem1.setAttribute("name", "set variable");
		elem2.setAttribute("name", "get variable");
		elem3.setAttribute("name", "variable");
		mxCell port1 = new mxCell(elem1, geo1,css1 + "fillColor="+getColor(type));
		mxCell port2 = new mxCell(elem2, geo2,css1 + "fillColor="+getColor("ping"));
		mxCell port3 = new mxCell(elem3, geo3,css2 + "fillColor="+getColor(type));
		port1.setVertex(true);
		port2.setVertex(true);
		port3.setVertex(true);
		graph.addCell(port1, cell);
		graph.addCell(port2, cell);
		graph.addCell(port3, cell);

		graph.refresh();
		graphVariables.put(cell.getId(), new GraphVariable(variable, cell, port1, port2, port3));
	}

	public GraphDevice createGraphDevice(String name, Point p, boolean disconnected, String id, String type, DeviceDecl xml){
		String bgType = type.equalsIgnoreCase("synthesizedservice") ? "#c4dcff" : "#99CCFF";
		String bg = disconnected? "#A0A0A0" : bgType;
		
		mxCell cell = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, "<b>" + name + "</b>", p.x, p.y, 100, 30, "verticalAlign=top;textAlign=center;fillColor="+bg);
		cell.setConnectable(false);
		mxCell add = (mxCell) graph.insertVertex(cell, null, "+", 0, 20, 150, 20, "fillColor=none");
		add.setConnectable(false);
		graph.refresh();
		GraphDevice gd = new GraphDevice(cell, add, disconnected, id, type, xml,name);
		addGraphDevice(cell.getId(), gd);
		return gd;
	}
	
	private void parseCommands(Node parent, PRDServiceFMDescription prds, boolean isSynthesized){
		if(prds != null){
			for(int i =0;i<prds.getNumControlInfo();i++){
				recursiveGetCommands(parent, prds.getControlInfo(i), isSynthesized);
			}
		}else{
			System.out.println("prds is null, is device description not synced yet?");
		}
	}
	
	private void recursiveGetCommands(Node parent, ControlInfo ci, boolean isSynthesized){
		if(ci instanceof CommandInfo){
			CommandInfo comI = (CommandInfo) ci;
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
            if(isIn){
                ActionWithParams awp;
                if(isSynthesized){
                    awp = new InvokeAction(name, new List(), new SynthesizedServiceUse(new Identifier(parent.name)), "", new Opt(), new Opt());
                }else{
                    awp = new SendMessageAction(name, new List(), null);
                }
                parent.addInCommand(name, type, awp);
            }else{
                CommandEvent ce = new CommandEvent(comI.getID(), null, new Opt(comI));
                parent.addOutCommand(name, type, ce);
            }
        }else if(ci instanceof GroupInfo){
			GroupInfo gi = (GroupInfo) ci;
			for(int i=0;i<gi.getNumControlInfo();i++){
				recursiveGetCommands(parent, gi.getControlInfo(i),isSynthesized);
			}
		}
	}
	
	private void recrusiveGetServices(Node parent, GraphDevice gd, PalcomServiceListPart psp, boolean isSynthesized) throws ResourceException {
		if (psp instanceof ServiceProxy) {
			PalcomService ps = (PalcomService) psp;
			String serviceName = "Unknown";
			try {
				serviceName = ps != null ? ps.getName() : "Unknown";
			} catch (ResourceException e) {}
			SingleServiceDecl decl = null;
			try {
				decl = new SingleServiceDecl(ps.requiresAuth(), new Identifier(serviceName), new DeviceUse(new Identifier(gd.name)), ps.getServiceID(), ps.getInstanceID().getInstanceNumber());
			} catch (ResourceException e) {}

			Node node = gd.addNode(parent, NodeType.SERVICE, psp.getName(),null, decl);

			ServiceProxy sp = (ServiceProxy) psp;
			PalcomServiceDescription psd = sp.getDescription();

			if(psd instanceof PalcomControlServiceDescription){
				PalcomControlServiceDescription pcsd = (PalcomControlServiceDescription) psd;
				PRDServiceFMDescription psfmd = pcsd.getPRDServiceFMDescription();
				parseCommands(node, psfmd, isSynthesized);
			}
		} else if (psp instanceof ServiceListProxy) {
			ServiceListProxy slp = (ServiceListProxy) psp;
			Node newParent = gd.addNode(parent, NodeType.SERVICELIST, slp.getName(),null,null);
			for (int i = 0; i < slp.getNumService(); i++) {
				recrusiveGetServices(newParent, gd, slp.getService(i), isSynthesized);
			}
		}
	}


	public void clear() {
		graphVariables.clear();
		graphDevices.clear();
		ssList.clear();
		variableList.clear();
		graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));
	}

	public void setUnsaved(boolean b) {
		assemblyPanel.setUnsaved(b);
	}

	public GraphVariable getVariableCell(String varName){
		for(String varKey:graphVariables.keySet()){
			GraphVariable gv = graphVariables.get(varKey);

			if(gv.variable.getIdentifier().getID().equalsIgnoreCase(varName)){
				return gv;
			}
		}
		return null;
	}

	public void addCellConnection(AwesomemxGraph.CellConnection cc) {
		Object parent = graph.getDefaultParent();
        if(graph.getEdgesBetween(cc.sourceCell, cc.targetCell).length == 0){
            graph.insertEdge(parent, null, "", cc.sourceCell, cc.targetCell);
        }
    }
}
