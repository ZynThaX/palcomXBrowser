package se.lth.cs.palcom.browsergui.views;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.w3c.dom.Element;

import se.lth.cs.palcom.discovery.DeviceProxy;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.handler.mxGraphTransferHandler;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;


public class GraphEditor extends JPanel{

	private static final long serialVersionUID = 8906103669540394160L;
	private HashSet<DeviceProxy> devices;
	private String assemblyData;
	private mxGraph graph;
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

		final mxGraphComponent graphComponent = new mxGraphComponent(graph);
//		new mxKeyboardHandler(graphComponent);
//		
//		graphComponent.getGraphControl().addMouseListener(new MouseAdapter()
//		{
//			public void mouseReleased(MouseEvent e)
//			{
//				mxCell cell = (mxCell)graphComponent.getCellAt(e.getX(), e.getY());
//
//		        
//				if (cell != null && graph.getLabel(cell).equalsIgnoreCase("+")){
////					AddFunctionPop menu = new AddFunctionPop(cell);
////					System.out.println(menu.getWidth()/2);
//					
//					
////					menu.show(e.getComponent(), e.getX()-menu.getWidth()/2, e.getY());
//				
//				}else if(cell != null && e.getButton() == MouseEvent.BUTTON3 && !cell.getParent().getId().equals("1")){
////			        RemoveCellPop menu = new RemoveCellPop(cell);
//			        
//	                
////			        menu.show(e.getComponent(), e.getX()-menu.getWidth()/2, e.getY());
//				}
//			}
//		});
		
//		setLayout(new BorderLayout());
//		.add(graphComponent);
//		System.out.println("sdalfas " + this.getParent().getCl);
//		this.getParent().setLayout(new BorderLayout());
		
		graphComponent.setTransferHandler(new mxGraphTransferHandler (){
			public boolean canImport(JComponent comp, DataFlavor[] flavors){
				System.out.println(" -INSIDE");
				return true;
				
			}
			public boolean	importData(JComponent c, Transferable t){
				System.out.println(" -INSIDE 2");
				return false;
			}
			public Transferable createTransferable(JComponent c){
				System.out.println(" -INSIDE 3");
				
				return lastImported;
				
			}
			public mxGraphTransferable	createGraphTransferable(mxGraphComponent graphComponent, Object[] cells, ImageIcon icon){
				System.out.println(" -INSIDE 4");
				return null;
			}
			
			public mxGraphTransferable	createGraphTransferable(mxGraphComponent graphComponent, Object[] cells, mxRectangle bounds, ImageIcon icon){
				System.out.println(" -INSIDE 5");
				return null;
			}
			public Object getTransferData(DataFlavor flavor){
		       System.out.println(" -Inside 6");
		       return null;
		    }
		});

		
//		graphComponent.setTransferHandler(new AssemblyGraphTransferHandler());
////		new mxGraphHandler();
		mxGraphHandler gh = new mxGraphHandler(graphComponent){
			public void dragEnter(DropTargetDragEvent e){
				System.out.println("HEJEJJEJEJE");
				System.out.println(e.getSource());
			}
		};
//		gh.setEnabled(false);
//		gh.setMoveEnabled(false);
//		gh.setImagePreview(false) ;
//		gh.setLivePreview(false)  ;
//		gh.setMarkerEnabled(false) ;
//		gh.setSelectEnabled(false) ;
//		graph.set
		
//		gh.
//		graphComponent.
		
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

	public void test() {
		System.out.println("  -" +this.getClass());
		System.out.println("  -" +this.getParent().getClass());
		int x = (int)this.getMousePosition().getX();
		int y = (int)this.getMousePosition().getY();

		mxCell cell = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, "<b>"+"BLAH"+"</b>", x,y, 80, 30, "verticalAlign=top;textAlign=center");
		cell.setConnectable(false);
		
		mxCell add = (mxCell) graph.insertVertex(cell, null, "+", 0, 20, 80, 20);
		graph.refresh();
	}
	
	
}
