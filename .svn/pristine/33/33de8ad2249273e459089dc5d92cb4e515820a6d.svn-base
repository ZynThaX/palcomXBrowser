package se.lth.cs.palcom.browsergui;


import java.util.HashMap;
import java.util.List;

import ist.palcom.resource.descriptor.DeviceID;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import se.lth.cs.palcom.browsergui.BrowserTreePanel.UniverseTreeNode;
import se.lth.cs.palcom.discovery.proxy.CachedResourceListener;
import se.lth.cs.palcom.discovery.proxy.PalcomDevice;
import se.lth.cs.palcom.discovery.proxy.PalcomNetwork;
import se.lth.cs.palcom.discovery.proxy.Resource;
import se.lth.cs.palcom.discovery.proxy.ResourceListener;

public class FavoriteBrowser extends BrowserTreePanel implements FavoriteListener {
	private final Favorites favorites;
	private final DefaultMutableTreeNode root;
	
	public FavoriteBrowser(BrowserApplication application, AssemblyTransferHandler th, Favorites favorites) {
		super(application, th);
		
		this.favorites = favorites;
		favorites.addListener(this);

		List<String> favs = favorites.getFavorites();
        root = (DefaultMutableTreeNode)model.getRoot();
        for (String fav : favs) {
        	DeviceID did = new DeviceID(fav);
        	PalcomDevice dev = application.getDiscoveryManager().getDevice(did);
        	DeviceTreeNode dn = new DeviceTreeNode(dev);
        	dn.init();
        	root.add(dn);
        	deviceTreeNodeLookup.put(did, dn);
        }
        model.nodeStructureChanged(root);
	}
	
	protected DefaultMutableTreeNode createRootNode() {
		return new FavoriteUniverseTreeNode(application.getDevice().getDiscoveryManager().getNetwork());
	}
	
	
	protected class FavoriteUniverseTreeNode extends UniverseTreeNode implements CachedResourceListener {
		protected static final long serialVersionUID = -5789471736226010389L;
		
		protected PalcomNetwork network;
    	
		public FavoriteUniverseTreeNode(PalcomNetwork net) {
			super(net);
		}
		
		@Override
		public String getLabel() {
			return "Favorites";
		}

		@Override
		public void available(Resource resource) {
		}

		@Override
		public void unavailable(Resource resource) {
		}
    }


	public void favoriteAdded(DeviceID did) {
		PalcomDevice dev = application.getDiscoveryManager().getDevice(did);
    	final DeviceTreeNode dn = new DeviceTreeNode(dev);
    	dn.init();
    	root.add(dn);
    	deviceTreeNodeLookup.put(did, dn);
    	SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				model.addDevice(dn);
				model.nodesWereInserted(root, new int[] {root.getChildCount() - 1});
			}
		});
	}

	public void favoriteRemoved(DeviceID did) {
		final DeviceTreeNode dn = (DeviceTreeNode) deviceTreeNodeLookup.get(did);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int index = root.getIndex(dn);
				if (index >= 0) {
					root.remove(index);
					model.nodesWereRemoved(root, new int[] {index}, new Object[] {dn});
				}
			}
		});
	}
}
