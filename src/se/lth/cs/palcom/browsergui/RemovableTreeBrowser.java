package se.lth.cs.palcom.browsergui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;

import se.lth.cs.palcom.browsergui.BrowserTreePanel.DeviceRemovalWrapper;
import se.lth.cs.palcom.browsergui.BrowserTreePanel.DeviceTreeNode;
import se.lth.cs.palcom.discovery.proxy.PalcomDevice;

public class RemovableTreeBrowser extends BrowserTreePanel {
	private final Favorites favorites;
	
	public RemovableTreeBrowser(BrowserApplication application, AssemblyTransferHandler th, Favorites favorites) {
		super(application, th);
		
		this.favorites = favorites;
		DeviceJanitor dj = new DeviceJanitor();
        dj.setDaemon(true);
        dj.setName("RemovableTreeBrowser Node-janitor");
        dj.start();
        
        setBackground(Color.CYAN);
	}

	protected class DeviceJanitor extends Thread {
		protected boolean running = false;
		
		public void run() {
			while (running) {
				synchronized (devices) {
					try {
						while (devices.size() == 0) {
							devices.wait();
						}
						final DeviceRemovalWrapper dn = (DeviceRemovalWrapper) devices.removeFirst();
						if (dn.node.resource.isAvailable()) {
							continue;
						}
						long remaining = System.currentTimeMillis() - dn.lastSeen;
						if (remaining > timeout) {
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									model.removeDevice(dn.node);
									deviceTreeNodeLookup.remove(dn.node.resource.getDeviceID());
								}
							});
						} else if(remaining == 0) {
							devices.add(0, dn);
							devices.wait(timeout);
						} else {
							devices.add(0, dn);
							devices.wait(remaining);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public synchronized void start() {
			running = true;
			super.start();
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("fav")) {
			TreeNode node = (TreeNode) browserTree.getSelectionPath().getLastPathComponent();
			if (node instanceof FavoriteDeviceTreeNode) {
				FavoriteDeviceTreeNode dtn = (FavoriteDeviceTreeNode) node;
				if (dtn.isFavorite()) {
					dtn.setFavorite(false);
					favorites.removeFavorite(dtn.getResource().getDeviceID());
					//application.getDevice().removeConfigProperty("BROWSER_FAVORITES", dtn.getResource().getDeviceID().toString());
				} else {
					dtn.setFavorite(true);
					favorites.addFavorite(dtn.getResource().getDeviceID());
					//application.getDevice().setConfigProperty("BROWSER_FAVORITES", dtn.getResource().getDeviceID().toString(), "True");
				}
			}
		} else {
			super.actionPerformed(ae);
		}
	}
	
	protected DeviceTreeNode createDeviceNode(PalcomDevice res) {
		DeviceTreeNode dn = new FavoriteDeviceTreeNode(res);
		dn.init();
		return dn;
	}
	
	private class FavoriteDeviceTreeNode extends DeviceTreeNode {
		protected boolean favorite = false;
		
		public FavoriteDeviceTreeNode(PalcomDevice res) {
			super(res);
			
			if (application.getDevice().getConfigProperty("BROWSER_FAVORITES", resource.getDeviceID().toString()) != null) {
				setFavorite(true);
			}
		}
		
		public void setFavorite(boolean favorite) {
			this.favorite = favorite;
		}

		public boolean isFavorite() {
			return favorite;
		}
		
		public Icon getIcon() {
			String file = "";
			String did = resource.getDeviceID().getString();
			
			if (resource.getStatus() == 'G') {
				if (favorites.isFavorite(did)) {
					file = "images/device_fav_g.png";
				} else {
					file = "images/box-green.png";
				}
			} else if (resource.getStatus() == 'Y') {
				if (favorites.isFavorite(did)) {
					file = "images/device_fav_y.png";
				} else {
					file = "images/box-yellow.png";
				}
			} else {
				if (favorites.isFavorite(did)) {
					file = "images/device_fav_r.png";
				} else {
					file = "images/box-red.png";
				}
			}
			File img = new File(file);
			if (img.exists()) {
				ImageIcon imgic = new ImageIcon(img.getAbsolutePath());
				return imgic;
			} else {
				URL url = getClass().getResource("/" + file);
				if (url != null) {
					return new ImageIcon(url);
				}
				return null;
			}
		}
	}
	
}
