package se.lth.cs.palcom.browsergui.views;

import ist.palcom.resource.descriptor.Identifier;
import ist.palcom.resource.descriptor.MimeType;
import ist.palcom.resource.descriptor.SynthesizedService;
import ist.palcom.resource.descriptor.VariableDecl;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
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
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import se.lth.cs.palcom.browsergui.views.GraphSynthServiceMenues.RemoveSSMenu;
import se.lth.cs.palcom.browsergui.views.GraphSynthServicePanel.ServiceObjGUI;

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
		
		leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		
		for (Map.Entry<String, String> entry : ge.getAllUsedColors().entrySet()) {
			String mime = entry.getKey();
			String color = entry.getValue();
			JLabel l1 = new JLabel(mime);
			l1.setIcon(new SimpleIcon(Color.decode(color)));
			leftPanel.add(l1);
			leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		}
		
		
		leftScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10), BorderFactory.createLineBorder(Color.GRAY)));
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		variableListPanel = new JPanel();
		JScrollPane rightScroll = new JScrollPane(variableListPanel);
		variableListPanel.setBackground(Color.white);
		variableListPanel.setLayout(new BoxLayout(variableListPanel, BoxLayout.Y_AXIS));
		rightScroll.setBorder(null);
		rightPanel.setBackground(Color.WHITE);
		rightPanel.add(rightScroll);
		rightPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(1), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
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
		rightWrapper.add(rightPanel, BorderLayout.CENTER);
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
			variableListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
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
	
	public class VariableObjGUI extends JLabel implements ActionListener, DragGestureListener, DragSourceListener{
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
			setOpaque(true);
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(109, 134, 173), 2),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			String type = ((MimeType)var.getVariableType()).getTypeName();
			String name = var.getIdentifier().getID();
			setText(name + " (" + type + ")");
			setIconTextGap(10);
			setIcon(new SimpleIcon(Color.decode(ge.getColor(type))));
			dragSource = new DragSource();
			dragSource.createDefaultDragGestureRecognizer(this,
					DnDConstants.ACTION_COPY_OR_MOVE, this);
			
			this.addMouseListener(new MouseListener(){
				public void mouseReleased(MouseEvent e) {
				    if(SwingUtilities.isRightMouseButton(e)){
				        JPopupMenu rssm = menues.createVaiableMenu(VariableObjGUI.this);
				        rssm.show(VariableObjGUI.this, e.getX(), e.getY());
				    }
				}
				public void mouseClicked(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
			});
			
		}
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("DeleteVariable")) {
				removeVariable(var);
			}
		}
		
		boolean equals(VariableDecl var){
			return this.var == var;
		}
		
		public void dragEnter(DragSourceDragEvent dsde) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
			
		}
		public void dragOver(DragSourceDragEvent dsde) {}
		public void dropActionChanged(DragSourceDragEvent dsde) {}
		public void dragExit(DragSourceEvent dse) {
			this.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(109, 134, 173), 2),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			
		}
		public void dragDropEnd(DragSourceDropEvent dsde) {
			this.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(109, 134, 173), 2),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			
		}
		public void dragGestureRecognized(DragGestureEvent dge) {
			this.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createDashedBorder(Color.green),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			
			Transferable transferable = new Transferable() {
				public DataFlavor[] getTransferDataFlavors() {
					return new DataFlavor[] { new DataFlavor(VariableDecl.class, "variableDecl") };
				}

				public boolean isDataFlavorSupported(DataFlavor flavor) {
					if (!isDataFlavorSupported(flavor)) {
						return false;
					}
					return true;
				}

				public Object getTransferData(DataFlavor flavor)
						throws UnsupportedFlavorException, IOException {
					return var;
				}
			};
			dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable,
					this);
			
		}
		
	}
	public class SimpleIcon implements Icon{

	    private int width = 16;
	    private int height = 16;
	    private Color fillColor;
	    
	    private BasicStroke stroke = new BasicStroke(4);
	    
	    public SimpleIcon(Color color){
	    	fillColor = color;
	    }
	    public void paintIcon(Component c, Graphics g, int x, int y) {
	        Graphics2D g2d = (Graphics2D) g.create();

	        g2d.setColor(fillColor);
	        g2d.fillOval(x +1 ,y + 1,width -2 ,height -2);

	        g2d.setColor(Color.BLACK);
	        g2d.drawOval(x +1 ,y + 1,width -2 ,height -2);

	        g2d.setStroke(stroke);

	        g2d.dispose();
	    }

	    public int getIconWidth() {
	        return width;
	    }

	    public int getIconHeight() {
	        return height;
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
