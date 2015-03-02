package se.lth.cs.palcom.browsergui;

import ist.palcom.resource.descriptor.DeviceID;

import java.util.LinkedList;
import java.util.List;

import se.lth.cs.palcom.device.AbstractDevice;

public class Favorites {
	private AbstractDevice device; //I don't like using device here...
	
	private final LinkedList<FavoriteListener> listeners = new LinkedList<FavoriteListener>();
	
	public Favorites(AbstractDevice device) {
		this.device = device;
	}

	public boolean isFavorite(String did) {
		return device.getConfigProperty("BROWSER_FAVORITES", did) != null;
	}
	
	public void addFavorite(DeviceID did) {
		device.setConfigProperty("BROWSER_FAVORITES", did.getString(), "True");
		notifyAdded(did);
	}
	
	public void removeFavorite(DeviceID did) {
		device.removeConfigProperty("BROWSER_FAVORITES", did.getString());
		notifyRemoved(did);
	}
	
	public synchronized void addListener(FavoriteListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeListener(FavoriteListener listener) {
		listeners.remove(listener);
	}
	
	private synchronized void notifyAdded(DeviceID did) {
		for (FavoriteListener listener : listeners) {
			listener.favoriteAdded(did);
		}
	}
	
	private synchronized void notifyRemoved(DeviceID did) {
		for (FavoriteListener listener : listeners) {
			listener.favoriteRemoved(did);
		}
	}
	
	public List<String> getFavorites() {
		String[] favs = device.getConfigKeys("BROWSER_FAVORITES");
		List<String> ret = new LinkedList<String>();
		
		for (String fav : favs) {
			ret.add(fav);
		}
		return ret;
	}
}
