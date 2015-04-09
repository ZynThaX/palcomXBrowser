package se.lth.cs.palcom.browsergui.views;

import ist.palcom.resource.descriptor.ASTNode;
import ist.palcom.resource.descriptor.CommandInfo;
import ist.palcom.resource.descriptor.ControlInfo;
import ist.palcom.resource.descriptor.GroupInfo;
import ist.palcom.resource.descriptor.PRDService;
import ist.palcom.resource.descriptor.PRDServiceFMDescription;
import ist.palcom.resource.descriptor.ParamInfo;
import ist.palcom.resource.descriptor.SynthesizedService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import bibliothek.gui.dock.themes.border.BorderModifier;
import se.lth.cs.palcom.browsergui.AddCommandDialog;
import se.lth.cs.palcom.browsergui.AddSynthServiceDialog;
import se.lth.cs.palcom.browsergui.AssemblyDroptarget;
import se.lth.cs.palcom.browsergui.AssemblyPanel;
import se.lth.cs.palcom.browsergui.AssemblyPanel.AssemblyTreeNode;
import se.lth.cs.palcom.browsergui.TwoStringsDialog;
import se.lth.cs.palcom.browsergui.views.GraphSynthServiceMenues.RemoveSSMenu;
import se.lth.cs.palcom.discovery.proxy.Resource;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.ThreadedExecutor;

public class GraphVariablePanel extends JPanel implements ActionListener{
	private JButton addSynthService;
	private GraphEditor ge;
	private JPanel leftPanel;
	private JPanel rightPanel;
	private GraphSynthServiceMenues menues;
	
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
		JScrollPane rightScroll = new JScrollPane(rightPanel);
		rightScroll.setBorder(BorderFactory.createBevelBorder(1));
		JPanel variableListPanel = new JPanel();		
		variableListPanel.add(new JLabel("img/jpeg"));
		JButton createVarBtn = new JButton("Create variable");
		rightPanel.add(variableListPanel, BorderLayout.WEST);
		rightPanel.add(createVarBtn, BorderLayout.EAST);
		
		createVarBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Component component = (Component) e.getSource();
				JFrame frame = (JFrame) SwingUtilities.getRoot(component);
				AddVariableDialog assd = new AddVariableDialog(frame, thisPanel);
			}
		});
		
		add(leftScroll);
		add(rightScroll);
		
	}

	public void toggle() {
		if (this.isVisible()) {
			this.setVisible(false);
		} else {
			this.setVisible(true);
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
	
	private final TwoStringsDialog twoStringsDialog = new TwoStringsDialog();
	

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

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	
}
