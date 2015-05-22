package se.lth.cs.palcom.browsergui.views;

import internal.org.kxml2.io.KXmlParser;
import internal.org.xmlpull.v1.XmlPullParser;
import internal.org.xmlpull.v1.XmlPullParserException;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.TreeMap;

import ist.palcom.resource.descriptor.*;
import ist.palcom.resource.descriptor.Event;
import org.w3c.dom.Element;

import se.lth.cs.palcom.assembly.AssemblyLoadException;
import se.lth.cs.palcom.assembly.OldschoolAssemblyLoader;
import se.lth.cs.palcom.browsergui.views.GraphDevice.Node;
import se.lth.cs.palcom.browsergui.views.GraphDevice.NodeType;
import se.lth.cs.palcom.discovery.DeviceProxy;
import se.lth.cs.palcom.discovery.ResourceException;
import se.lth.cs.palcom.discovery.proxy.PalcomNetwork;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;

import static se.lth.cs.palcom.browsergui.views.GraphVariablePanel.*;

public class AwesomemxGraph extends mxGraph {
	
//	public 
	public boolean isPort(Object cell)
	{
		mxGeometry geo = getCellGeometry(cell);
		return (geo != null) ? geo.isRelative() : false;
	}
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
	
	
	public void updateGraphWithData(PalcomNetwork pcn, String assemblyData, GraphObjectsHandler graphData, GraphEditor ge) {
		//se OldschoolAssemblyLoader

		ge.clear();

		//TODO, ta bort gammal data ut grafen och data-objekten

		XmlPullParser factory = new KXmlParser();
		int topPos = 10;
		TreeMap<String, GraphDevice> devices = new TreeMap<String, GraphDevice>();
		TreeMap<GraphDevice, ArrayList<String>> servicesToAdd = new TreeMap<GraphDevice, ArrayList<String>>();
		TreeMap<String, Node> serviceNodes = new TreeMap<String, Node>();
		se.lth.cs.palcom.common.collections.List networkDevices = pcn.getDevices();
		
		
		try {
			byte[] xmlBytes = assemblyData.getBytes("UTF8");
			
			factory.setInput(new InputStreamReader(new ByteArrayInputStream(xmlBytes, 0, xmlBytes.length)));
			factory.nextTag();
			
			xmlGoTo(factory,"DeviceDeclList");
			factory.nextTag();
			
			while(!factory.getName().equals("DeviceDeclList")){
				String id = factory.getAttributeValue("", "id");

				String deviceId = "";

				boolean found = false;
				if(factory.getName().equals("Identifier") && id != null){					
					xmlGoTo(factory,"DID");
					deviceId = factory.getAttributeValue("", "id");

					for(int i = 0;i<networkDevices.size();i++){
						Object device = networkDevices.get(i);
						if(device instanceof DeviceProxy){
							DeviceProxy devicep = (DeviceProxy) device;
							if(devicep.getDeviceID().toString().equals(factory.getAttributeValue("", "id"))){
								Point p = graphData.getObjectPoint(deviceId, "device");
								if(p == null){
									p = new Point(150, topPos);
									topPos+=100;
								}
								GraphDevice gd = ge.importDevice(p, devicep);
								devices.put(id, gd);
								found = true;
							}
						}
					}			
				}
				if(!found && id != null){
					//TODO, hitta x och y till deviceId
					Point p = graphData.getObjectPoint(deviceId, "device");

					if(p == null){
						p = new Point(150, topPos);
						topPos+=100;
					}

					GraphDevice gd = ge.createGraphDevice(id, p, true, deviceId, "device");
					devices.put(id, gd);
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
					xmlGoTo(factory, "Identifier");
					names[1] = factory.getAttributeValue("", "id");//device name
					
					
					GraphDevice gd = devices.get(names[1]);
					
					if(gd != null){
						if(gd.disconnected){
							Node node = gd.findOrAddNode(names[0]);
							serviceNodes.put(serviceId, node);
						}
						if(servicesToAdd.containsKey(gd)){
							servicesToAdd.get(gd).add(names[0]);
						}else{
							ArrayList<String> nodes = new ArrayList<String>();
							nodes.add(names[0]);
							servicesToAdd.put(gd, nodes);
						}
					}
					
					xmlGoTo(factory,"ServiceDecl");
				}
				factory.nextTag();
			}
			
			xmlGoTo(factory,"EventHandlerScript");

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

			
			String commandName;
			String direction;
			String type = "ping";
			
			while(!factory.getName().equals("EventHandlerList")){
				if(factory.getName().equals("EventHandlerClause")){
					// TODO, detta för att avgöra var anslutningarna e gjorda, skapa anslutningar mellan enheter
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
					
					Node serviceNode = serviceNodes.get(serviceId);
					if(serviceNode != null){
						serviceNode.addCommand(direction.toLowerCase().equals("in"), commandName, type);
					}

					xmlGoTo(factory,"EventHandlerClause");
				}
				factory.nextTag();
			}

			xmlGoTo(factory, "SynthesizedServiceList");
			factory.nextTag();

			try {
				PRDAssemblyD assembly = OldschoolAssemblyLoader.parseAssembly(xmlBytes, 0, xmlBytes.length, null);
				PRDAssemblyVer version = assembly.getPRDAssemblyVer(0);

				SynthesizedServiceList sdl = version.getSynthesizedServices();
				for(int i= 0; i<sdl.getNumSynthesizedService();i++){
					SynthesizedService ss = sdl.getSynthesizedService(i);
					ge.addSynthService(ss);
					Point p = graphData.getObjectPoint(ss.getPRDServiceFMDescription().getID(), "synthesizedservice");
					if(p != null){
						ge.importDevice(p,ss);
					}
				}


				EventHandlerList events = version.getEventHandlerScript().getEventHandlers();
				for(int i = 0;i<events.getChild(0).getNumChild();i++){
//					System.out.println(events.getChild(0).getChild(i));
					ASTNode event = events.getChild(0).getChild(i);
					if(event instanceof EventHandlerClause){
						EventHandlerClause ehc = (EventHandlerClause) event;
						Event ev = ehc.getEvent();
						//TODO, hämta ut event trigger (commandNAme) samt vilken device som anropat den (s1,s2..)
						//eventuella parametrar skall också hållas reda på
						for(int j=0;j<ev.getNumChild();j++){
							Object obj = ev.getChild(j);

							System.out.println(obj.getClass() + "      " + obj);
						}
						System.out.println("ev = " + ev + "\n");

						for (int j = 0; j < ehc.getNumAction(); ++j) {
							Action act = ehc.getAction(j);
							System.out.println("AC = " + act);
							if (act instanceof SendMessageAction) {
								SendMessageAction sma = (SendMessageAction)act;



//								add(new SendMessageTreeNode(sma));
							} else if (act instanceof InvokeAction) {
								InvokeAction ia = (InvokeAction)act;
//								add(new InvokeTreeNode(ia));
							} else if (act instanceof AssignAction) {
								AssignAction aa = (AssignAction)act;
//								add(new DeletableTreeNode(aa.getVariableUse().getName() + " = " + aa.getParamUse().getName()));
							}
							System.out.println();
						}


//						for(int j=0;j<ehc.getNumAction();j++){
//							Action ac = ehc.getAction(j);
//
//							System.out.println("ac = " + ac + "\n");
//						}
					}
//					System.out.println(event.getClass());


//					events.getChildNoTransform();
				}

			} catch (AssemblyLoadException e) {
				e.printStackTrace();
			}


			for(GraphDevice gd:servicesToAdd.keySet()){
				ArrayList<String> nodes = servicesToAdd.get(gd);
				for(String nodeName:nodes){
					ge.addVertex(gd.getId(), nodeName);
				}
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ResourceException e) {
			e.printStackTrace();
		}
	}
	private void xmlGoTo(XmlPullParser factory, String tagName) throws XmlPullParserException, IOException{
		while(!factory.getName().equals(tagName)){
			factory.nextTag();
		}
	}
}
