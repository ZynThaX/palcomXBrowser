package se.lth.cs.palcom.browsergui.views;


import internal.org.kxml2.io.KXmlParser;
import internal.org.xmlpull.v1.XmlPullParser;
import internal.org.xmlpull.v1.XmlPullParserException;
import se.lth.cs.palcom.discovery.DeviceProxy;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by jonas on 2015-05-21.
 */
public class GraphObjectsHandler {
    public ArrayList<GraphObjectPositioning> objects;
    public ArrayList<GraphConnectionsPositioning> connections;


    public GraphObjectsHandler(TreeMap<String, GraphDevice> graphDevices, TreeMap<String, GraphVariable> graphVariables) {
        objects = new ArrayList<GraphObjectPositioning>();
        connections = new ArrayList<GraphConnectionsPositioning>();

        for(String graphid:graphDevices.keySet()){
			GraphDevice gd = graphDevices.get(graphid);
			int x = (int) gd.cell.getGeometry().getX();
			int y = (int) gd.cell.getGeometry().getY();
            objects.add(new GraphObjectPositioning(gd.id,gd.type,x,y));
		}
		for(String graphid:graphVariables.keySet()){
			GraphVariable gv = graphVariables.get(graphid);
			String id = gv.variable.getIdentifier().getID();
			int x = (int) gv.cell.getGeometry().getX();
			int y = (int) gv.cell.getGeometry().getY();
            objects.add(new GraphObjectPositioning(id, "variable", x, y));
		}
    }

    public GraphObjectsHandler() {
        objects = new ArrayList<GraphObjectPositioning>();
        connections = new ArrayList<GraphConnectionsPositioning>();
    }

    public boolean addObject(String id, String type, int x, int y){
        return objects.add(new GraphObjectPositioning(id, type,x,y));
    }
    public ArrayList<Point> addConnection(String source, String target){
        GraphConnectionsPositioning connection = new GraphConnectionsPositioning(source, target);
        connections.add(connection);
        return connection.points;
    }
    public void clear(){
        objects.clear();
        connections.clear();
    }

    public String getXmlData(){
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version='1.0' encoding='ISO-8859-1' ?><!DOCTYPE AssemblyGraphData>\n");
        sb.append("<graphobjects>\n");
        for(GraphObjectPositioning gop:objects){
            sb.append("<graphobject id=\"").append(gop.id).append("\" type=\"").append(gop.type).append("\" x=\"");
            sb.append(gop.x).append("\" y=\"").append(gop.y).append("\" />\n");
        }
        sb.append("</graphobjects>\n");

        sb.append("<graphconnections>\n");
        for(GraphConnectionsPositioning gcp:connections){
            sb.append("<graphconnection source=\"").append(gcp.source).append("\" target=\"").append(gcp.target).append(" />\n");
            if(!gcp.points.isEmpty()){
                sb.append("<graphpoints>\n");
                for(Point p:gcp.points){
                    sb.append("<graphpoint x=\"").append(p.x).append("\" y=\"").append(p.y).append("\" />\n");
                }
                sb.append("</graphpoints>\n");
            }
        }
        sb.append("</graphconnections>\n");
        return sb.toString();
    }

    public GraphObjectsHandler(byte[] xmlData){
        objects = new ArrayList<GraphObjectPositioning>();
        connections = new ArrayList<GraphConnectionsPositioning>();
        try {
            XmlPullParser factory = new KXmlParser();
            factory.setInput(new InputStreamReader(new ByteArrayInputStream(xmlData, 0, xmlData.length)));
            factory.nextTag();

            xmlGoTo(factory, "graphobjects");
            factory.nextTag();

            while(!factory.getName().equals("graphobjects")) {
                String id = factory.getAttributeValue("", "id");
                String type = factory.getAttributeValue("", "type");
                if(factory.getName().equals("graphobject") && id != null && type!=null){
                    try{
                        int x = Integer.parseInt(factory.getAttributeValue("", "x"));
                        int y = Integer.parseInt(factory.getAttributeValue("", "y"));
                        objects.add(new GraphObjectPositioning(id, type, x, y));
                    }catch (NumberFormatException ee){

                    }
                }
                factory.nextTag();
            }
            //TODO connections
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void xmlGoTo(XmlPullParser factory, String tagName) throws XmlPullParserException, IOException {
        while(!factory.getName().equals(tagName)){
            factory.nextTag();
        }
    }
    public Point getObjectPoint(String id, String type) {
        for(GraphObjectPositioning gop:objects){
            if(gop.id.equals(id) && gop.type.equals(type)){
                return new Point(gop.x,gop.y);
            }
        }
        return null;
    }
}
