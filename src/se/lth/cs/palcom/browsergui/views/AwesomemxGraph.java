package se.lth.cs.palcom.browsergui.views;

import internal.org.kxml2.io.KXmlParser;
import internal.org.xmlpull.v1.XmlPullParser;
import internal.org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.w3c.dom.Element;

import se.lth.cs.palcom.browsergui.views.GraphDevice.Node;
import se.lth.cs.palcom.browsergui.views.GraphDevice.NodeType;
import se.lth.cs.palcom.discovery.DeviceProxy;
import se.lth.cs.palcom.discovery.ResourceException;
import se.lth.cs.palcom.discovery.proxy.PalcomNetwork;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;

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
	
	
	public void updateGraphWithData(PalcomNetwork pcn, String assemblyData, GraphEditor ge) {
		//se OldschoolAssemblyLoader
		
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
				boolean found = false;
				if(factory.getName().equals("Identifier") && id != null){					
					xmlGoTo(factory,"DID");
					
					for(int i = 0;i<networkDevices.size();i++){
						Object device = networkDevices.get(i);
						if(device instanceof DeviceProxy){
							DeviceProxy devicep = (DeviceProxy) device;
							if(devicep.getDeviceID().toString().equals(factory.getAttributeValue("", "id"))){
								GraphDevice gd = ge.importDevice(topPos, devicep);	
								devices.put(id, gd);
								found = true;		
								topPos+=100;
							}
						}
					}			
				}				
				if(!found && id != null){
					GraphDevice gd = ge.createGraphDevice(id, topPos, true);	
					devices.put(id, gd);		
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
