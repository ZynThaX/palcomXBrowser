package se.lth.cs.palcom.browsergui.views;

import ist.palcom.resource.descriptor.SynthesizedService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import se.lth.cs.palcom.browsergui.AddSynthServiceDialog;
import se.lth.cs.palcom.browsergui.AssemblyPanel;

public class GraphSynthServicePanel extends JPanel{
	private JButton addSynthService;
	private GraphEditor ge;
	private JPanel servicePanel;
	public GraphSynthServicePanel(GraphEditor ge){
		this.ge = ge;
		final GraphSynthServicePanel thisPanel = this;
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY), BorderFactory.createEmptyBorder(10, 10 ,10, 10)));
		setLayout(new BorderLayout());
		servicePanel = new JPanel();
		servicePanel.setAutoscrolls(true);
		addSynthService = new JButton("Create new service");
		addSynthService.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				Component component = (Component) e.getSource();
				JFrame frame = (JFrame) SwingUtilities.getRoot(component);
				AddSynthServiceDialog assd = new AddSynthServiceDialog(frame, thisPanel);
			}
		});
		add(servicePanel, BorderLayout.CENTER);
		add(addSynthService, BorderLayout.EAST);
		setPreferredSize(new Dimension(100, 150));
		
	}
	public void addService(SynthesizedService ss){
		ge.addSynthService(ss);
		System.out.println("Created new synthesised service");
		displayServices();
		//TODO Show synthesised services in the GUI
	}
	
	public void displayServices(){
		servicePanel.removeAll();
		for(SynthesizedService ss : ge.getSynthServices()){
			servicePanel.add(new ServiceObjGUI(ss));
		}
	}
	public void toggle() {
		if(this.isVisible()){
			this.setVisible(false);
		} else {
			this.setVisible(true);
		}
	}
	
	private class ServiceObjGUI extends JPanel {
		SynthesizedService ss;
		public ServiceObjGUI(SynthesizedService ss){
			this.ss = ss;
			this.setPreferredSize(new Dimension(150, 150));
			this.setBackground(Color.gray);
			add(new JLabel(ss.getPRDServiceFMDescription().getID()));
		}
	}
	

}
