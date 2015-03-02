package se.lth.cs.palcom.browsergui;

import java.awt.Component;

public interface SaveStatusListener {
	void saveStatusChanged(boolean saved, Component source);
}
