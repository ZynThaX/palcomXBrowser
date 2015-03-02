package se.lth.cs.palcom.browsergui;

import java.awt.Component;

import javax.swing.JOptionPane;

import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;

public class AssemblyDockable extends EnvironmentDockable implements SaveStatusListener, CVetoClosingListener {
	private AssemblyPanel ap;
	
	public AssemblyDockable(BrowserApplication application, String idd, String title, AssemblyPanel ap) {
		super(idd, title, ap);
		this.ap = ap;
		ap.addSaveStatusListener(this);
		addVetoClosingListener(this);
	}

	public void saveStatusChanged(boolean saved, Component source) {
		setTitleText((saved ? "" : "* ") + title);
	}

	public void closed(CVetoClosingEvent vce) {
		if (ap.isUnsaved()) {
			vce.cancel();
		}
	}

	public void closing(CVetoClosingEvent vce) {
		if (ap.isUnsaved()) {
			int res = JOptionPane.showConfirmDialog(ap, "Save changes?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (res == JOptionPane.CANCEL_OPTION) {
				vce.cancel();
				return;
			} else if (res == JOptionPane.YES_OPTION) {
				ap.saveAssembly();
			} else {
				ap.setUnsaved(false);
				ap.reloadAssembly();
			}
		}
	}
}
