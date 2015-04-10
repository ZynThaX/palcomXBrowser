package se.lth.cs.palcom.browsergui.views;

import ist.palcom.resource.descriptor.Identifier;
import ist.palcom.resource.descriptor.MimeType;
import ist.palcom.resource.descriptor.VariableDecl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import se.lth.cs.palcom.browsergui.views.GraphSynthServiceMenues.RemoveSSMenu;

public class GraphVariablePanel extends JPanel implements ActionListener{
	private JButton addSynthService;
	private GraphEditor ge;
	private JPanel leftPanel;
	private JPanel rightPanel;
	private GraphSynthServiceMenues menues;
	JPanel variableListPanel;
	
	public GraphVariablePanel(GraphEditor ge) {
		this.ge = ge;
		menues = new GraphSynthServiceMenues();
		final GraphVariablePanel thisPanel = this;
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		setLayout(new GridLayout(1, 2));
		
		setPreferredSize(new Dimension(100, 150));
		
		leftPanel = new JPanel();
		JScrollPane leftScroll = new JScrollPane(leftPanel);
//		leftScroll.setBorder(null);
		leftPanel.setBackground(Color.white);
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.add(new JLabel("Color White = img/jpeg"));
		leftPanel.add(new JLabel("Color Green = ping"));
		leftPanel.add(new JLabel("Color Red = text/json"));
		leftPanel.add(new JLabel("Color White = img/jpeg"));
		leftPanel.add(new JLabel("Color Green = ping"));
		leftPanel.add(new JLabel("Color Red = text/json"));
		leftPanel.add(new JLabel("Color White = img/jpeg"));
		leftPanel.add(new JLabel("Color Green = ping"));
		leftPanel.add(new JLabel("Color Red = text/json"));
		leftPanel.add(new JLabel("Color White = img/jpeg"));
		leftPanel.add(new JLabel("Color Green = ping"));
		leftPanel.add(new JLabel("Color Red = text/json"));
		leftScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10), BorderFactory.createLineBorder(Color.GRAY)));
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		variableListPanel = new JPanel();
		JScrollPane rightScroll = new JScrollPane(variableListPanel);
		variableListPanel.setBackground(Color.white);
		variableListPanel.setLayout(new BoxLayout(variableListPanel, BoxLayout.Y_AXIS));

		rightScroll.setBorder(BorderFactory.createBevelBorder(1));
		
		JButton createVarBtn = new JButton("Create variable");
		//rightPanel.add(variableListPanel, BorderLayout.WEST);
		//rightPanel.add(createVarBtn, BorderLayout.EAST);
		
		createVarBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Component component = (Component) e.getSource();
				JFrame frame = (JFrame) SwingUtilities.getRoot(component);
				AddVariableDialog assd = new AddVariableDialog(frame, thisPanel);
			}
		});
		
		JPanel leftWrapper = new JPanel(new BorderLayout());
		
		leftWrapper.add(new JLabel("Color description"), BorderLayout.NORTH);
		leftWrapper.add(leftScroll, BorderLayout.CENTER);
		
		JPanel rightWrapper = new JPanel(new BorderLayout());
		rightWrapper.add(rightScroll, BorderLayout.CENTER);
		rightWrapper.add(createVarBtn, BorderLayout.EAST);
		
		add(leftWrapper);
		add(rightWrapper);
		
	}

	public void toggle() {
		if (this.isVisible()) {
			this.setVisible(false);
		} else {
			this.setVisible(true);
		}
	}
	
	
	//private final TwoStringsDialog twoStringsDialog = new TwoStringsDialog();
	

	public void mouseClicked(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mousePressed(MouseEvent me) {
		doPopup(me);
	}
	public void mouseReleased(MouseEvent me) {
		doPopup(me);
	}
	
	private void doPopup(MouseEvent me) {
		if (me.isPopupTrigger()) {
//			TreePath selectionPath = assemblyTree.getPathForLocation(me.getX(), me.getY());
//			if (selectionPath == null) {
//				return;
//			}
//			assemblyTree.setSelectionPath(selectionPath);
//			TreeNode node = (TreeNode) selectionPath.getLastPathComponent();
//			if (node instanceof AssemblyTreeNode) {
//				((AssemblyTreeNode)node).showContextMenu(me.getX(), me.getY());
//			}
		}
	}
	
	public void addVariable(VariableDecl var) {
		ge.addVariable(new VariableObjGUI(var));
		displayVariables();
	}
	
	public void removeVariable(VariableDecl var) {
		ge.removeVariable(var);
		displayVariables();
	}

	public void displayVariables() {
		variableListPanel.removeAll();
		for (VariableObjGUI var : ge.getVariables()) {
			variableListPanel.add(var);
		}
		variableListPanel.repaint();
	}
	
	
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		//final TreeNode node = (TreeNode) tree.getSelectionPath().getLastPathComponent();
		if (cmd.equals("removeVariable")) {
			RemoveSSMenu rm = (RemoveSSMenu) ((JMenuItem)e.getSource()).getParent();
			//removeService(rm.ss);
		} else if(cmd.equals("AddVarOk")){
			AddVariableDialog window = (AddVariableDialog) SwingUtilities.windowForComponent((JButton)e.getSource());
			window.dispose();
			VariableDecl dec = new VariableDecl(new MimeType(window.getAssemblyType()), new Identifier(window.getName()));
			addVariable(dec);
//			System.out.println(dec);
		}
	}
	
	public class VariableObjGUI extends JPanel implements ActionListener {
		VariableDecl var;
		DragSource dragSource;
		//private final TwoStringsDialog twoStringsDialog = new TwoStringsDialog();
		public VariableObjGUI(VariableDecl var) {
			this.var = var;
			this.setPreferredSize(new Dimension(130, 20));
//			this.setSize(new Dimension(130, 20));
//			this.setLayout(new GridLayout(0, 1));
			Color deviceBlue = new Color(196, 219, 255);
			this.setBackground(deviceBlue);
			String type = ((MimeType)var.getVariableType()).getTypeName();
			String name = var.getIdentifier().getID();
//			setText(name + " (" + type + ")");
			add(new JLabel(name + " (" + type + ")"));
			
		}
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		boolean equals(VariableDecl var){
			return this.var == var;
		}
	}
	private class AddVariableDialog extends JDialog {
		private JTextField nameTextField;
		private JTextField typeTextField;

		public AddVariableDialog(JFrame frame, GraphVariablePanel thisPanel) {
			super(frame,false);
			setSize(200,200);
			setLocationRelativeTo(this);
			
			GroupLayout layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			JLabel lbl1 = new JLabel("Type");
			JLabel lbl2 = new JLabel("Name");
			nameTextField = new JTextField();
			typeTextField = new JTextField();
			JButton btn1 = new JButton("Ok");
			btn1.addActionListener(GraphVariablePanel.this);
			btn1.setActionCommand("AddVarOk");
			
			layout.setHorizontalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			    		  .addComponent(lbl1)
					      .addComponent(typeTextField)
					      .addComponent(lbl2)
					      .addComponent(nameTextField)
					      .addComponent(btn1)
				)
			);
			layout.setVerticalGroup(
			   layout.createSequentialGroup()
			      .addComponent(lbl1)
			      .addComponent(typeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			      .addComponent(lbl2)
			      .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			      .addComponent(btn1)
			);
			setVisible(true);
		}
		
		public String getName() {
			return nameTextField.getText();
		}
		
		public String getAssemblyType() {
			return typeTextField.getText();
		}
		
		public void showDialog() {
			nameTextField.setText("");
			typeTextField.setText("");
			setVisible(true);
		}
	}
	
	
}
