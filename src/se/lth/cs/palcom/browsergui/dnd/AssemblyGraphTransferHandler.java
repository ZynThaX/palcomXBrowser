package se.lth.cs.palcom.browsergui.dnd;


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
	
	private GraphEditor graphEditor;
	
	public AssemblyGraphTransferHandler(GraphEditor graphEditor){
		super();
		this.graphEditor = graphEditor;
	}

	private static final long serialVersionUID = 1L;

	@Override
    public boolean importData(JComponent comp, Transferable t) {
		try {
			Object data = t.getTransferData(new DataFlavor(Resource.class, "resource"));
			if (!(data instanceof DeviceProxy)) {
				return false;
			}
			
			graphEditor.importDevice((int)comp.getMousePosition().getY(),(DeviceProxy)data);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
		
    }
}
