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
		System.out.println(r.getClass());
		if(r instanceof ServiceDescriptionProxy){
			ServiceDescriptionProxy sdp = (ServiceDescriptionProxy) r;
//			sdp.findCommand(0)
			System.out.println(sdp.toString());
		}else if(r instanceof ServiceProxy){
			ServiceProxy sp = (ServiceProxy) r;
			
//			sp.getDescription()
		}
		xml.setText(r.toString());
	}
}
