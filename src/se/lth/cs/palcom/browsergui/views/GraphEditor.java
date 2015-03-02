package se.lth.cs.palcom.browsergui.views;


import java.util.HashSet;



import javax.swing.JLabel;

import se.lth.cs.palcom.discovery.DeviceProxy;


public class GraphEditor extends JLabel{

	private static final long serialVersionUID = 8906103669540394160L;
	private HashSet<DeviceProxy> devices;
	private String assemblyData;
	/**
	 * Creates a graph editor for the assemblies
	 */	
	public GraphEditor(){
		setText("This is the Graph Editor");
		devices = new HashSet<DeviceProxy>();
	}
	
	/**
	 * Adds a device to the Graph
	 * @param d device to add
	 * @return if successfully added return true else false
	 */
	public boolean addDevice(DeviceProxy d){
		if(devices.contains(d)){
			return false;
		}
		return devices.add(d);
	}
	
	/**
	 * Creates an XML of the complete graph
	 * @return xml as string
	 */
	public String getXML(){
		return assemblyData;
	}
	
	/**
	 * Creates a the graph from the assemblyData
	 * @param assemblyData data in xml that contains assembly information
	 */
	public void setGraph(String assemblyData) {
		this.assemblyData = assemblyData;
		//TODO implement this function
	}
	
	
}
