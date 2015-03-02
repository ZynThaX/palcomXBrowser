package se.lth.cs.palcom.browsergui;

import ist.palcom.resource.descriptor.DeviceID;

public interface FavoriteListener {
	void favoriteAdded(DeviceID did);
	void favoriteRemoved(DeviceID did);
}
