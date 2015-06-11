package se.lth.cs.palcom.browsergui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import se.lth.cs.palcom.discovery.ServiceDescriptionProxy;
import se.lth.cs.palcom.discovery.ServiceProxy;
import se.lth.cs.palcom.discovery.proxy.Resource;

public class XMLViewPanel extends JPanel implements BrowserSelectionListener {
	private JTextArea xml;
	public XMLViewPanel() {
		setLayout(new BorderLayout());
		xml = new JTextArea();
		xml.setEditable(false);
		add(new JScrollPane(xml));
	}

	public void resourceSelected(Resource r) {
		if(r instanceof ServiceDescriptionProxy){
			ServiceDescriptionProxy sdp = (ServiceDescriptionProxy) r;
		}else if(r instanceof ServiceProxy){
			ServiceProxy sp = (ServiceProxy) r;
		}
		xml.setText(r.toString());
	}
}
