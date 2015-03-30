package se.lth.cs.palcom.browsergui.dnd;


import ist.palcom.resource.descriptor.SynthesizedService;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import se.lth.cs.palcom.browsergui.views.GraphEditor;
import se.lth.cs.palcom.discovery.DeviceProxy;
import se.lth.cs.palcom.discovery.proxy.Resource;


public class AssemblyGraphTransferHandler extends TransferHandler{
	public static DataFlavor FLAVOR_RESOURCE = new DataFlavor(Resource.class, "resource");
	public static DataFlavor FLAVOR_SS = new DataFlavor(SynthesizedService.class, "synthesisedService");
	private GraphEditor graphEditor;
//	private DataFlavor flavors[];
	
	public AssemblyGraphTransferHandler(GraphEditor graphEditor){
		super();
		this.graphEditor = graphEditor;
//		flavors = new DataFlavor[] {FLAVOR_RESOURCE, FLAVOR_SS};
	}

	private static final long serialVersionUID = 1L;

	@Override
    public boolean importData(JComponent comp, Transferable t) {
		try {
			Object data;
	    	Object data2;
			data = t.getTransferData(FLAVOR_RESOURCE);
			data2 = t.getTransferData(FLAVOR_SS);
			if (!((data instanceof DeviceProxy)
					|| data2 instanceof SynthesizedService)) {
				return false;
			}
			if (data instanceof DeviceProxy){
				graphEditor.importDevice((int)comp.getMousePosition().getY(),(DeviceProxy)data);
			} else if(data2 instanceof SynthesizedService){
//				graphEditor.importDevice((int)comp.getMousePosition().getY(),(SynthesizedService)data);
				System.out.println("Dropped a synthesised service");
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
	
    @Override
    public boolean canImport(TransferSupport support) {
    	Object data;
    	Object data2;
		try {
			data = support.getTransferable().getTransferData(FLAVOR_RESOURCE);
			data2 = support.getTransferable().getTransferData(FLAVOR_SS);
			if ((data instanceof DeviceProxy) || data2 instanceof SynthesizedService) { 
				return true;
			} 
            return false;
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
    }
 
}
