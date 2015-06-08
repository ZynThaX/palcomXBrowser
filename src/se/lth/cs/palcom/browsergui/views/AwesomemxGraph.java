package se.lth.cs.palcom.browsergui.views;

import internal.org.kxml2.io.KXmlParser;
import internal.org.xmlpull.v1.XmlPullParser;
import internal.org.xmlpull.v1.XmlPullParserException;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeMap;

import ist.palcom.resource.descriptor.*;
import org.w3c.dom.Element;

import se.lth.cs.palcom.assembly.AssemblyLoadException;
import se.lth.cs.palcom.assembly.OldschoolAssemblyLoader;
import se.lth.cs.palcom.browsergui.views.GraphDevice.Node;
import se.lth.cs.palcom.discovery.DeviceProxy;
import se.lth.cs.palcom.discovery.ResourceException;
import se.lth.cs.palcom.discovery.proxy.PalcomNetwork;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.view.mxGraph;

public class AwesomemxGraph extends mxGraph {

	private final GraphEditor ge;

	public AwesomemxGraph(GraphEditor ge){
		this.ge = ge;
	}

	public void cellsMoved(Object[] cells,
						   double dx,
						   double dy,
						   boolean disconnect,
						   boolean constrain){
		super.cellsMoved(cells, dx,dy,disconnect,constrain);
		ge.setUnsaved(true);

	}
//	public
//	public boolean isPort(Object cell)
//	{
//		mxGeometry geo = getCellGeometry(cell);
//		return (geo != null) ? geo.isRelative() : false;
//	}
	public String getToolTipForCell(Object cell)
	{
		if (model.isEdge(cell))
		{
			return convertValueToString(model.getTerminal(cell, true)) + " -> " +
				convertValueToString(model.getTerminal(cell, false));
		}
		if(cell instanceof mxCell){
			Object value = ((mxCell) cell).getValue();
			if (value instanceof Element){
				Element elt = (Element) value;
				if(elt.getAttributeNode("name") !=  null){
					return elt.getAttribute("name");
				}
			}
		}
		return super.getToolTipForCell(cell);
	}
	public boolean isCellFoldable(Object cell, boolean collapse)
	{
		return false;
	}
	public boolean isCellMovable(Object cell){
		mxCell c = ((mxCell)cell);
		return c != null && c.getParent() != null && c.getParent().getId().equals("1");

	}

	public String convertValueToString(Object cell){
		if (cell instanceof mxCell){
			Object value = ((mxCell) cell).getValue();
			if (value instanceof Element){
				Element elt = (Element) value;
				if(elt.getAttributeNode("name") !=  null){
					return reduceNameLength(elt.getAttribute("name"));
				}
			}
		}
		return super.convertValueToString(cell);
	}
	private String reduceNameLength(String name){
		if(name.length() > 8){
			return name.substring(0, 5) + "..";
		}
		return name;
	}

	
	public void updateGraphWithData(PalcomNetwork pcn, String assemblyData, GraphObjectsHandler graphData, GraphEditor ge) throws IOException, AssemblyLoadException, XmlPullParserException, ResourceException {
		//se OldschoolAssemblyLoader

		ge.clear();

		//TODO, ta bort gammal data ut grafen och data-objekten

		XmlPullParser factory = new KXmlParser();
		int topPos = 10;
		TreeMap<String, GraphDevice> devices = new TreeMap<String, GraphDevice>();
		TreeMap<GraphDevice, ArrayList<String>> servicesToAdd = new TreeMap<GraphDevice, ArrayList<String>>();
		TreeMap<String, Node> serviceNodes = new TreeMap<String, Node>();
		TreeMap<String, Node> synthServices = new TreeMap<String, Node>();

		se.lth.cs.palcom.common.collections.List networkDevices = pcn.getDevices();

//		ArrayList<Connection> messageEvents = new ArrayList<Connection>();
//		ArrayList<VarConnection> variableAssignments = new ArrayList<VarConnection>();
		ArrayList<CellConnection> cellConnections = new ArrayList<CellConnection>();

		byte[] xmlBytes = assemblyData.getBytes("UTF8");
		PRDAssemblyD assembly = OldschoolAssemblyLoader.parseAssembly(xmlBytes, 0, xmlBytes.length, null);
		PRDAssemblyVer version = assembly.getPRDAssemblyVer(0);



		factory.setInput(new InputStreamReader(new ByteArrayInputStream(xmlBytes, 0, xmlBytes.length)));
		factory.nextTag();


		DeviceDeclList xmlDevices = version.getDevices();
		for(int i=0;i<xmlDevices.getNumDeviceDecl();i++){
			DeviceDecl dd = xmlDevices.getDeviceDecl(i);
			String identifier = dd.getNameID().getID(); //id


			if(dd instanceof DeviceAddressDecl){
				DeviceAddressDecl dad = (DeviceAddressDecl) dd;
				String did = dad.getDeviceAddress().getDeviceID().getString(); //deviceId

				boolean found = false;

				for(int j = 0;j<networkDevices.size();j++){
					Object device = networkDevices.get(j);
					if(device instanceof DeviceProxy){
						DeviceProxy devicep = (DeviceProxy) device;
						if(devicep.getDeviceID().toString().equals(did)){
							Point p = graphData.getObjectPoint(did, "device");
							if(p == null){
								p = new Point(150, topPos);
								topPos+=100;
							}
							GraphDevice gd = ge.importDevice(p, devicep);
							devices.put(identifier, gd);
							found = true;
						}
					}
				}
				if(!found){
					Point p = graphData.getObjectPoint(did, "device");
					if(p == null){
						p = new Point(150, topPos);
						topPos+=100;
					}
                    GraphDevice gd = ge.createGraphDevice(identifier, p, true, did, "device",dd);

					devices.put(identifier, gd);
				}
			}
		}


		ServiceDeclList services = version.getServices();
		for(int i=0;i<services.getNumServiceDecl();i++){
			ServiceDecl sd = services.getServiceDecl(i);
			String palcomServiceId = sd.getLocalName().getID();

			AbstractServiceDecl abstractServiceDecl = sd.getDecl();
			if(abstractServiceDecl instanceof SingleServiceDecl){
				SingleServiceDecl ssd = (SingleServiceDecl) abstractServiceDecl;
				String serviceName = ssd.getServiceName().getID();
				String deviceName = ssd.getDeviceUse().getIdentifier().getID();

				GraphDevice gd = devices.get(deviceName);

				if(gd != null){
                    Node node = gd.findOrAddNode(serviceName, ge, palcomServiceId, abstractServiceDecl);
                    serviceNodes.put(palcomServiceId, node);
                    if(gd.disconnected){
                        ge.updateServiceId(palcomServiceId);
//                        System.out.println("Scanning commands from disconnectd device: " + gd.name + " - " + gd.disconnected);
                        parseAndAddCommands(node, palcomServiceId, version);

                    }else{
                        //TODO, show all commands for node???
                    }
                    if(servicesToAdd.containsKey(gd)){
						servicesToAdd.get(gd).add(serviceName);
					}else{
						ArrayList<String> nodes = new ArrayList<String>();
						nodes.add(serviceName);
						servicesToAdd.put(gd, nodes);
					}
				}
			}
		}


//		version.getEventHandlerScript().getVariables()

		xmlGoTo(factory, "EventHandlerScript");

		xmlGoTo(factory, "VariableList");
		factory.nextTag();
		while(!factory.getName().equals("VariableList")){
			String type = factory.getAttributeValue("", "type");
			String id = factory.getAttributeValue("", "identifier");
			if(factory.getName().equals("VariableDecl") && type != null && id != null){
				VariableDecl vd =  new VariableDecl();
				vd.initializeFromElement(factory);
				ge.addVariable(vd);
				Point p = graphData.getObjectPoint(id, "variable");
				if (p != null){
					ge.importVariable(vd, p);
				}

			}
			factory.nextTag();
		}


		xmlGoTo(factory,"EventHandlerList");
		factory.nextTag();



		for(GraphDevice gd:servicesToAdd.keySet()){
			ArrayList<String> nodes = servicesToAdd.get(gd);
			for(String nodeName:nodes){
				ge.showService(gd.getId(), nodeName);
			}
		}


		SynthesizedServiceList sdl = version.getSynthesizedServices();
		for(int i= 0; i<sdl.getNumSynthesizedService();i++){
			SynthesizedService ss = sdl.getSynthesizedService(i);
			ge.addSynthService(ss);
			Point p = graphData.getObjectPoint(ss.getPRDServiceFMDescription().getID(), "synthesizedservice");
			if (p == null) p = new Point(100,100);
			if(p != null){
				GraphDevice gd = ge.importDevice(p,ss);
				synthServices.put(ss.getPRDServiceFMDescription().getID(), gd.root.children.get(0));
			}
		}

		EventHandlerList events = version.getEventHandlerScript().getEventHandlers();

		for(int i = 0;i<events.getChild(0).getNumChild();i++){
			String type = "ping";
			ASTNode event = events.getChild(0).getChild(i);
			if(event instanceof EventHandlerClause){
				EventHandlerClause ehc = (EventHandlerClause) event;
				CommandEvent ev = (CommandEvent)ehc.getEvent();

				String sourceId = ev.getServiceExp().getIdentifier().getID();
				String paramId = null;
				CommandInfo ci = ev.getCommandInfo();

				for(int j =0;j<ci.getNumParamInfo();j++){
					type = ci.getParamInfo(j).getType();
					break;
				}

				Node sourceNode = (ev.getServiceExp() instanceof SynthesizedServiceUse)?synthServices.get(sourceId):serviceNodes.get(sourceId);
				if(sourceNode != null)
                    sourceNode.addOutCommand(ci.getName(), type, ev);

                mxCell sourceCell = sourceNode.getCommandCell(ev.getCommandName());

				for(int j=0;j<ci.getNumParamInfo();j++){
					ParamInfo pi = ci.getParamInfo(j);
					paramId = pi.getID();
					//TODO, kan det finnas flera parametrar i en event clause?
				}

				for (int j = 0; j < ehc.getNumAction(); ++j) {
					Action act = ehc.getAction(j);
                    mxCell targetCell = null;
                    if (act instanceof AssignAction) {
                        AssignAction aa = (AssignAction)act;
						String varName = aa.getVariableUse().getName();
						if(aa.getParamUse().getName().equalsIgnoreCase(paramId)){
							GraphVariable gv = ge.getVariableCell(varName);
							if(gv != null){
								targetCell = gv.setVar;
							}
						}
					}else{
                        ActionWithParams awp = (ActionWithParams) act;
                        String targetCommand = awp.getCommand();
                        Node targetNode = null;
                        if(act instanceof SendMessageAction){
                            targetNode = serviceNodes.get(((SendMessageAction) act).getServiceExp().getIdentifier().getID());
                        }else if(act instanceof InvokeAction){
                            targetNode = synthServices.get(((InvokeAction)act).getSynthesizedServiceUse().getIdentifier().getID());
                        }
                        if(targetNode != null){
                            if(awp.getNumParamValue() > 0 && !(awp.getParamValue(0) instanceof  ParamUse)){
                                String paramName = awp.getParamValue(0).getName();
                                if (awp.getParamValue(0) instanceof VariableUse){
                                    GraphVariable gv = ge.getVariableCell(paramName);
                                    mxCell tempTarget = targetNode.getCommandCell(targetCommand);
                                    mxCell tempSource = gv.getOut;
                                    if(tempTarget != null && tempSource != null){
                                        cellConnections.add(new CellConnection(tempSource, tempTarget));
                                    }
                                    targetCell = gv.getVar;
                                }
                            }else{
                                targetCell = targetNode.getCommandCell(targetCommand);
                            }
                        }
                    }
                    if(sourceCell != null && targetCell != null){
                        cellConnections.add(new CellConnection(sourceCell, targetCell));
                    }
				}
			}
		}
		for(CellConnection cc:cellConnections){
			ge.addCellConnection(cc);
		}
	}

    private void parseAndAddCommands(Node node, String palcomServiceId, PRDAssemblyVer version) {
        EventHandlerList events = version.getEventHandlerScript().getEventHandlers();

        for(int i = 0;i<events.getChild(0).getNumChild();i++) {
            String type = "ping";
            ASTNode event = events.getChild(0).getChild(i);
            if (event instanceof EventHandlerClause) {
                EventHandlerClause ehc = (EventHandlerClause) event;
                CommandEvent ev = (CommandEvent)ehc.getEvent();

                String sourceId = ev.getServiceExp().getIdentifier().getID();
                String sourceCommand = ev.getCommandName();

                CommandInfo ci = ev.getCommandInfo();
                for(int j =0;j<ci.getNumParamInfo();j++){
                    ParamInfo pinfo = ci.getParamInfo(j);
                    type = pinfo.getType();
                    break;
                }
                if(sourceId.equalsIgnoreCase(palcomServiceId)){
                    node.addOutCommand(sourceCommand, type, ev);
                }

                for (int j = 0; j < ehc.getNumAction(); ++j) {
                    Action act = ehc.getAction(j);
                    if (act instanceof SendMessageAction) {
                        SendMessageAction sma = (SendMessageAction)act;
                        String targetId = sma.getServiceExp().getIdentifier().getID();
                        String targetCommand = sma.getCommand();

                        if(targetId.equalsIgnoreCase(palcomServiceId)){
                            type = "ping";

                            if(sma.getNumParamValue() > 0){
                                String paramName = sma.getParamValue(0).getName();
                                GraphVariable gv = ge.getVariableCell(paramName);
                                type = ((MimeType)gv.variable.getVariableType()).getTypeName();
                            }

                            node.addInCommand(targetCommand, type, sma);
                        }
                    }
                }
            }

        }
    }

    public class CellConnection{
		mxCell sourceCell, targetCell;
		public CellConnection(mxCell sourceCell, mxCell targetCell){
			this.sourceCell = sourceCell;
			this.targetCell = targetCell;
		}
	}
	private void xmlGoTo(XmlPullParser factory, String tagName) throws XmlPullParserException, IOException{
		while(!factory.getName().equals(tagName)){
			factory.nextTag();
		}
	}
}
