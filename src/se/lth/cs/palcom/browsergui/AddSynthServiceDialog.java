package se.lth.cs.palcom.browsergui;

import ist.palcom.resource.descriptor.AuthRole;
import ist.palcom.resource.descriptor.GroupID;
import ist.palcom.resource.descriptor.List;
import ist.palcom.resource.descriptor.LocalSID;
import ist.palcom.resource.descriptor.PRDService;
import ist.palcom.resource.descriptor.PRDServiceFMDescription;
import ist.palcom.resource.descriptor.SynthesizedService;
import ist.palcom.resource.descriptor.Topic;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import se.lth.cs.palcom.browsergui.AssemblyPanel.SynthServiceListNode;
import se.lth.cs.palcom.browsergui.views.GraphSynthServicePanel;

public class AddSynthServiceDialog extends JDialog implements ActionListener, ChangeListener {
	private static final long serialVersionUID = 151171697356756851L;
	private SynthServiceListNode target; //This is ugly as hell...
	public static String WINDOW_GRAPH = "graph";
	public static String WINDOW_TREE = "tree";
	
	private JTextField txtGroupTopic;
	private JLabel lblGroupTopic;
	private byte dist = 0;
	private boolean reqAuth;

	private JTextField txtName;

	private JCheckBox authBox;
	private String window2 = "";
	private GraphSynthServicePanel ssPanel;

	public AddSynthServiceDialog(SynthServiceListNode node) {
		target = node;
		initComponents();
		setSize(400, 300);
		setVisible(true);
		window2 = WINDOW_TREE;
	}
	
	public AddSynthServiceDialog(Frame parent, GraphSynthServicePanel ssPanel) {
//		target = node;
		super(parent, true);
		initComponents();
		setSize(400, 300);
		setVisible(true);
		window2 = WINDOW_GRAPH;
		this.ssPanel = ssPanel;
		System.out.println("windos is now : " + window2);
	}
	
	private void initComponents() {
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		ButtonGroup dists = new ButtonGroup();
		JRadioButton btnUnicast = new JRadioButton("Unicast");
		btnUnicast.addActionListener(this);
		btnUnicast.setActionCommand("Unicast");
		JRadioButton btnGroupcast = new JRadioButton("Group");
		btnGroupcast.addActionListener(this);
		btnGroupcast.setActionCommand("Groupcast");
		JRadioButton btnPubSub = new JRadioButton("Pub/Sub");
		btnPubSub.addActionListener(this);
		btnPubSub.setActionCommand("PubSub");
		JRadioButton btnBroadcast = new JRadioButton("Broadcast");
		btnBroadcast.addActionListener(this);
		btnBroadcast.setActionCommand("Broadcast");
		
		dists.add(btnUnicast);
		dists.add(btnPubSub);
		dists.add(btnGroupcast);
		dists.add(btnBroadcast);
		
		JLabel lblName = new JLabel("Name:");
		txtName = new JTextField();
		lblGroupTopic = new JLabel("Group / Topic");
		txtGroupTopic = new JTextField();
		
		JButton btnOk = new JButton("Ok");
		btnOk.addActionListener(this);
		btnOk.setActionCommand("Ok");
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(this);
		btnCancel.setActionCommand("Cancel");

		JSeparator jSeparator1 = new JSeparator();
		
		authBox = new JCheckBox("Requires UserID");
		authBox.addChangeListener(this);
		
		layout.setHorizontalGroup(
	            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                    .addComponent(btnUnicast)
	                    .addComponent(btnBroadcast)
	                    .addComponent(btnGroupcast)
	                    .addComponent(btnPubSub))
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
	                    .addGroup(layout.createSequentialGroup()
	                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                        	.addComponent(authBox)
	                            .addComponent(lblName)
	                            .addComponent(txtName, GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
	                            .addComponent(lblGroupTopic)
	                            .addComponent(txtGroupTopic, GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE))
	                        .addContainerGap())
	                    .addGroup(layout.createSequentialGroup()
	                        .addComponent(btnCancel)
	                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                        .addComponent(btnOk))))
	        );
	        layout.setVerticalGroup(
	            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(btnUnicast)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(btnBroadcast)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(btnGroupcast)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(btnPubSub)
	                .addContainerGap(168, Short.MAX_VALUE))
	            .addComponent(jSeparator1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(lblName)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(txtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                .addComponent(lblGroupTopic)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(txtGroupTopic, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                .addComponent(authBox)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 126, Short.MAX_VALUE)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(btnOk)
	                    .addComponent(btnCancel)))
	        );

	        pack();
		
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("Unicast")) {
			txtGroupTopic.setEnabled(false);
			lblGroupTopic.setEnabled(false);
			dist = PRDService.UNICAST;
		} else if (ae.getActionCommand().equals("Groupcast")) {
			txtGroupTopic.setEnabled(true);
			lblGroupTopic.setEnabled(true);
			dist = PRDService.GROUPCAST;
		} else if (ae.getActionCommand().equals("Broadcast")) {
			txtGroupTopic.setEnabled(false);
			lblGroupTopic.setEnabled(false);
			dist = PRDService.BROADCAST;
		} else if (ae.getActionCommand().equals("PubSub")) {
			txtGroupTopic.setEnabled(true);
			lblGroupTopic.setEnabled(true);
			dist = PRDService.RADIOCAST; //Really? I dont't think so...
		} else if (ae.getActionCommand().equals("Cancel")) {
			setVisible(false);
			dispose();
		} else if (ae.getActionCommand().equals("Ok")) {
			StringBuilder sb = new StringBuilder();
			boolean error = false;
			SynthesizedService sserv = new SynthesizedService();
			sserv.setReqAuth(reqAuth);

			List lst = new List(); //XXX: Not this!
			lst.add(new AuthRole("User1"));
			lst.add(new AuthRole("User2"));
			
			sserv.setRoleList(lst);
			
			if (txtName.getText() == null || txtName.getText().equals("")) {
				sb.append("Please enter a name.\n");
				error = true;
			} else {
				sserv.setPRDServiceFMDescription(new PRDServiceFMDescription(txtName.getText(), "", new List(), new LocalSID()));
			}
			
			if (dist == 0) {
				sb.append("Please choose a distribution.\n");
				error = true;
			}
			if (dist == PRDService.GROUPCAST) {
				if (txtGroupTopic.getText() == null || txtGroupTopic.getText().equals("")) {
					sb.append("Please enter a group.\n");
					error = true;
				} else {
					sserv.setGroupID(new GroupID(txtGroupTopic.getText()));
				}
			}
			if (dist == PRDService.RADIOCAST) {
				if (txtGroupTopic.getText() == null || txtGroupTopic.getText().equals("")) {
					sb.append("Please enter a topic.\n");
					error = true;
				} else {
					sserv.setTopic(new Topic(txtGroupTopic.getText()));
				}
			}
			
			if (error) {
				JOptionPane.showMessageDialog(null, sb.toString());
				return;
			}
			if(ssPanel == null){
				System.out.println("opened in tree");
			}
			if(target == null){
				System.out.println("opened in graph");
			}
			sserv.setDistribution(dist);
			System.out.println("window is: "+ window2);
			if(window2.equals(WINDOW_GRAPH)){
				ssPanel.addService(sserv);
			} else if(window2.equals(WINDOW_TREE)){
				target.addService(sserv);
			}
			setVisible(false);
			dispose();
		}
	}

	public void stateChanged(ChangeEvent ce) {
		reqAuth = authBox.isSelected();
	}
}
