package se.lth.cs.palcom.browsergui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class TabTransferable implements Transferable {
	public static final String NAME = "TabTransferData";
	public static final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
	
	private TabTransferData m_data = null;

	public TabTransferable(DNDTabbedPane a_tabbedPane, int a_tabIndex) {
		m_data = new TabTransferData(a_tabbedPane, a_tabIndex);
	}

	public Object getTransferData(DataFlavor flavor) {
		return m_data;
	}

	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] f = new DataFlavor[1];
		f[0] = FLAVOR;
		return f;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.getHumanPresentableName().equals(NAME);
	}		
}
