package se.lth.cs.palcom.browsergui.views;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.w3c.dom.Element;

import se.lth.cs.palcom.browsergui.dnd.AssemblyGraphTransferHandler;
import se.lth.cs.palcom.browsergui.dnd.GraphServiceTree;
import se.lth.cs.palcom.discovery.DeviceProxy;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.view.mxGraph;


public class GraphEditor extends JPanel{

	private static final long serialVersionUID = 8906103669540394160L;
	private HashSet<DeviceProxy> devices;
	private String assemblyData;
	private mxGraph graph;
	private TreeMap<String,GraphServiceTree> graphServiceTrees;
	
	/**
	 * Creates a graph editor for the assemblies
	 */	
	public GraphEditor(){
		
//		this.setPreferredSize(new Dimension(800, 600));
		graph = new mxGraph(){
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
						if (elt.getTagName().toLowerCase().contains("source") || elt.getTagName().toLowerCase().contains("target")){
							return "";
						}
					}
				}
				return super.convertValueToString(cell);
			}		
			
		};
		
		graphServiceTrees = new TreeMap<String, GraphServiceTree>();
		final mxGraphComponent graphComponent = new mxGraphComponent(graph);
		new mxKeyboardHandler(graphComponent);
		
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent e){
				mxCell cell = (mxCell)graphComponent.getCellAt(e.getX(), e.getY());
				if (cell != null && graph.getLabel(cell).equalsIgnoreCase("+")){
					
					GraphServiceTree gST = graphServiceTrees.get(cell.getParent().getId());
					if(gST != null){
						System.out.println("INside");
						gST.printTree(e.getComponent(), e.getX(), e.getY());
					}
					
				}else if(cell != null && e.getButton() == MouseEvent.BUTTON3 && !cell.getParent().getId().equals("1")){
//					TODO, rightclick
				}
			}
		});
		

		
		graph.setHtmlLabels(true);
		graph.setAllowDanglingEdges(false);
		graph.setCellsDeletable(true);
		graph.setCellsResizable(false);;
		
		setLayout(new BorderLayout());
	    
		add(graphComponent,BorderLayout.CENTER);
		JLabel dropArea = new JLabel("<html>Drop<br>device<br>here</html>");
		
		Border paddingBorder = BorderFactory.createEmptyBorder(10,10,10,10);
		//JLabel will be involved for this border
		Border border = BorderFactory.createLineBorder(Color.BLUE);

		dropArea.setBorder(BorderFactory.createCompoundBorder(border,paddingBorder));
		
		dropArea.setBackground(Color.WHITE);
		dropArea.setOpaque(true);
//		dropArea.setForeground(Color.WHITE);
//		droparea.set
		dropArea.setTransferHandler(new AssemblyGraphTransferHandler(this));
		add(dropArea,BorderLayout.WEST);
		
		graphComponent.setToolTips(true);
		
		this.add(graphComponent);
//		graphComponent.setTr
		
		devices = new HashSet<DeviceProxy>();
//		TODO
//		importa graf-paket och initiera graf
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
	 * Creates the graph from the assemblyData
	 * @param assemblyData data in xml that contains assembly information
	 */
	public void setGraph(String assemblyData) {
		this.assemblyData = assemblyData;
		//TODO implement this function
	}

	public mxGraph getGraph() {
		return graph;
	}

	public void addGraphServiceTree(String key, GraphServiceTree gST) {
		// TODO Auto-generated method stub
		graphServiceTrees.put(key, gST);
	}

	
	
}
