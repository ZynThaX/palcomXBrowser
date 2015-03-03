package se.lth.cs.palcom.browsergui.dnd;

import ist.palcom.resource.descriptor.Address;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JComponent;

import se.lth.cs.palcom.browsergui.dnd.GraphServiceTree.Node;
import se.lth.cs.palcom.browsergui.dnd.GraphServiceTree.NodeType;
import se.lth.cs.palcom.browsergui.views.GraphEditor;
import se.lth.cs.palcom.discovery.DeviceProxy;
import se.lth.cs.palcom.discovery.ResourceException;
import se.lth.cs.palcom.discovery.ServiceListProxy;
import se.lth.cs.palcom.discovery.ServiceProxy;
import se.lth.cs.palcom.discovery.proxy.PalcomServiceList;
import se.lth.cs.palcom.discovery.proxy.PalcomServiceListPart;
import se.lth.cs.palcom.discovery.proxy.Resource;

import com.mxgraph.swing.handler.mxGraphTransferHandler;

public class AssemblyGraphTransferHandler extends mxGraphTransferHandler{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public boolean importData(JComponent comp, Transferable t) {
		
		System.out.println("  -ImportData in AssemblyGraphTransferHandler");
		((GraphEditor)comp).test();
		
		try {
			Object data = t.getTransferData(new DataFlavor(Resource.class, "resource"));
			if (!(data instanceof DeviceProxy)) {
				return false;
			}
			
			DeviceProxy res = (DeviceProxy)data;
			
			ArrayList<ServiceProxy> availableServices = new ArrayList<ServiceProxy>();
			
			GraphServiceTree gST = new GraphServiceTree();
			
		
			PalcomServiceList services = res.getServiceList();
			for(int i=0;i<services.getNumService();i++){	
				recrusiveGetServices(null, gST, services.getService(i));
				
				System.out.println(services.getService(i).getClass());

				
				System.out.println(services.getService(i));
					Address ser = services.getService(i).getAddress();
					for(int k= 0;k<ser.getNumChild();k++){
						
//						System.out.println(ser.getChild(k));
//						if(ser.getChild(k) instanceof ist.palcom.resource.descriptor.List){
//							System.out.println("Inside");
//							ist.palcom.resource.descriptor.List l = (ist.palcom.resource.descriptor.List) ser.getChild(k);
//							
//							for(int j=0;j<l.getNumChild();j++){
//								System.out.println(l.getChild(j));
//								System.out.println(l.getChild(j).getClass());
//								if(l.getChild(j) instanceof ist.palcom.resource.descriptor.CommandInfo){
//									ist.palcom.resource.descriptor.CommandInfo ci = (ist.palcom.resource.descriptor.CommandInfo) l.getChild(j);
//									System.out.println(ci.getID());
//									System.out.println(ci.getDirection());
//									
//								}
////								System.out.println(l.getChild(j).Define_Identifier_getLocalName(l.getChild(j), null));
//							}
//						}
//						System.out.println(ser.getChild(k).getClass());
					}
				
			}
			
			
			
//			TODO
//			
			gST.printTree(comp, (int)comp.getMousePosition().getX(), (int)comp.getMousePosition().getY());
			
			if(((GraphEditor)comp).addDevice(res)){
				System.out.println("Added : "+res.getName());
			} else {
				System.out.println("Failed to add. Already in workspace!");
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
	
	private void recrusiveGetServices(Node parent, GraphServiceTree gST, PalcomServiceListPart psp) throws ResourceException{
		if(psp instanceof ServiceProxy){
			gST.addNode(parent, NodeType.SERVICE, psp.getName());
//			availableServices.add(services.getService(i))
//			System.out.println(((ServiceProxy)services.getService(i)).getDescription());
		}else if(psp instanceof ServiceListProxy){
			ServiceListProxy slp = (ServiceListProxy)psp;
			Node newParent = gST.addNode(parent, NodeType.SERVICELIST, slp.getName() + " LIST");
			for(int i=0;i<slp.getNumService();i++){
				recrusiveGetServices(newParent, gST,slp.getService(i));
			}
//			System.out.println("Not implemented");//TODO
		}
	}

	
	
	public boolean canImport(JComponent comp, DataFlavor[] flavors){
    	return true;
	}

    @Override
    public boolean canImport(TransferSupport support) {
    	Object data;
		try {
			data = support.getTransferable().getTransferData(new DataFlavor(Resource.class, "resource"));
			if (!(data instanceof DeviceProxy)) { 
				return false;
			} 
            return true;
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
    }
}
