package se.lth.cs.palcom.browsergui.dnd;


import ist.palcom.resource.descriptor.SynthesizedService;
import ist.palcom.resource.descriptor.VariableDecl;

import java.awt.*;
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
	public static DataFlavor FLAVOR_VR = new DataFlavor(SynthesizedService.class, "variableDecl");
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
			Object data, data2, data3;
			data = t.getTransferData(FLAVOR_RESOURCE);
			data2 = t.getTransferData(FLAVOR_SS);
			data3 = t.getTransferData(FLAVOR_VR);
			if (!((data instanceof DeviceProxy)
					|| data2 instanceof SynthesizedService || data3 instanceof VariableDecl)) {
				return false;
			}
			if (data instanceof DeviceProxy){
				graphEditor.importDevice(new Point(150,(int)comp.getMousePosition().getY()),(DeviceProxy)data);
			} else if(data2 instanceof SynthesizedService){
				graphEditor.importDevice(new Point(150,(int)comp.getMousePosition().getY()),(SynthesizedService)data);
			} else if(data3 instanceof VariableDecl){
				graphEditor.importVariable((VariableDecl) data3, new Point(150, (int)comp.getMousePosition().getY()));
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
	
    @Override
    public boolean canImport(TransferSupport support) {
    	Object data, data2, data3;
		
		try {
			data = support.getTransferable().getTransferData(FLAVOR_RESOURCE);
			data2 = support.getTransferable().getTransferData(FLAVOR_SS);
			data3 = support.getTransferable().getTransferData(FLAVOR_VR);
			if ((data instanceof DeviceProxy) || data2 instanceof SynthesizedService || data3 instanceof VariableDecl) { 
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
