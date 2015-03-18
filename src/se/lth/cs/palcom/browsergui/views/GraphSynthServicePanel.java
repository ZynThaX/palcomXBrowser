package se.lth.cs.palcom.browsergui.views;

import ist.palcom.resource.descriptor.ASTNode;
import ist.palcom.resource.descriptor.CommandInfo;
import ist.palcom.resource.descriptor.ControlInfo;
import ist.palcom.resource.descriptor.GroupInfo;
import ist.palcom.resource.descriptor.PRDService;
import ist.palcom.resource.descriptor.PRDServiceFMDescription;
import ist.palcom.resource.descriptor.SynthesizedService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import se.lth.cs.palcom.browsergui.AddSynthServiceDialog;
import se.lth.cs.palcom.browsergui.AssemblyDroptarget;
import se.lth.cs.palcom.browsergui.AssemblyPanel.AssemblyTreeNode;
import se.lth.cs.palcom.discovery.proxy.Resource;

public class GraphSynthServicePanel extends JPanel {
	private JButton addSynthService;
	private GraphEditor ge;
	private JPanel servicePanel;

	public GraphSynthServicePanel(GraphEditor ge) {
		this.ge = ge;
		final GraphSynthServicePanel thisPanel = this;
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		setLayout(new BorderLayout());
		servicePanel = new JPanel();
		// servicePanel.setCellRenderer(new ListRenderer());
		// servicePanel.setAutoscrolls(true);
		servicePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 10, 5));
		servicePanel.setBackground(Color.WHITE);
		JScrollPane serviceScrollPane = new JScrollPane(servicePanel);
		addSynthService = new JButton("Create new service");
		addSynthService.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Component component = (Component) e.getSource();
				JFrame frame = (JFrame) SwingUtilities.getRoot(component);
				AddSynthServiceDialog assd = new AddSynthServiceDialog(frame,
						thisPanel);
			}
		});
		add(serviceScrollPane, BorderLayout.CENTER);
		add(addSynthService, BorderLayout.EAST);
		setPreferredSize(new Dimension(100, 150));

	}

	public void addService(SynthesizedService ss) {
		ge.addSynthService(ss);
		System.out.println("Created new synthesised service");
		displayServices();
		// TODO Show synthesised services in the GUI
	}

	public void displayServices() {
		servicePanel.removeAll();
		for (SynthesizedService ss : ge.getSynthServices()) {
			servicePanel.add(new ServiceObjGUI(ss));
		}
	}

	public void toggle() {
		if (this.isVisible()) {
			this.setVisible(false);
		} else {
			this.setVisible(true);
		}
	}

	private class ServiceObjGUI extends JPanel implements DragGestureListener,
			DragSourceListener {
		SynthesizedService ss;
		DragSource dragSource;

		public ServiceObjGUI(SynthesizedService ss) {
			this.ss = ss;
			this.setPreferredSize(new Dimension(100, 100));
			Color deviceBlue = new Color(196, 219, 255);
			this.setBackground(deviceBlue);
			this.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(109, 134, 173), 1),
					BorderFactory.createEmptyBorder(2, 2, 2, 2)));
			// this.setAutoscrolls(true);
			DefaultTreeModel model = new DefaultTreeModel(
					new DefaultMutableTreeNode("NULL"));
			JTree t = new JTree(model);
			model.setRoot(new SynthServiceNode(ss));
			DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) (t
					.getCellRenderer());
			renderer.setBackgroundNonSelectionColor(deviceBlue);
			renderer.setBackgroundSelectionColor(deviceBlue);
			// renderer.setBorderSelectionColor(Color.GREEN);
			renderer.setTextNonSelectionColor(new Color(69, 60, 43));
			renderer.setTextSelectionColor(new Color(69, 60, 43));
			// JPanel p = new JPanel();
			// p.setBackground(new Color(196, 219, 255));
			// p.setPreferredSize(new Dimension(100, 100));
			// p.add(t);
			t.setBackground(new Color(196, 219, 255));
			t.setScrollsOnExpand(false);
			t.collapseRow(0);
			dragSource = new DragSource();
			dragSource.createDefaultDragGestureRecognizer(this,
					DnDConstants.ACTION_COPY_OR_MOVE, this);
			add(t);
		}

		public void dragEnter(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
			System.out.println("drag enter");
		}

		public void dragOver(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub

		}

		public void dropActionChanged(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub

		}

		public void dragExit(DragSourceEvent dse) {
			// TODO Auto-generated method stub
			this.setBorder(BorderFactory.createLineBorder(new Color(109, 134, 173)));

		}

		public void dragDropEnd(DragSourceDropEvent dsde) {
			// TODO Auto-generated method stub
			this.setBorder(BorderFactory.createLineBorder(new Color(109, 134, 173)));
		}

		public void dragGestureRecognized(DragGestureEvent dge) {
			Transferable transferable = new StringSelection("hellow");
			this.setBorder(BorderFactory.createDashedBorder(Color.GREEN));
			dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable,
					this);
		}

	}

	private class SynthServiceNode extends AssemblyTreeNode<SynthesizedService> {
		public SynthServiceNode(SynthesizedService service) {
			super("");
			this.data = service;
			add(new ServiceDescriptionTreeNode(
					service.getPRDServiceFMDescription()));
			this.setUserObject(getLabel());
		}

		@Override
		public String getLabel() {
			StringBuilder sb = new StringBuilder();
			sb.append(data.getPRDServiceFMDescription().getID());
			sb.append(" (");
			if (data.getDistribution() == PRDService.BROADCAST) {
				sb.append("Broadcast");
			} else if (data.getDistribution() == PRDService.GROUPCAST) {
				sb.append("Groupcast");
			} else if (data.getDistribution() == PRDService.RADIOCAST) {
				sb.append("Radiocast");
			} else if (data.getDistribution() == PRDService.UNICAST) {
				sb.append("Unicast");
			}
			if (data.getReqAuth()) {
				sb.append(" (UserID)");
			}
			sb.append(")");

			return sb.toString();
		}
	}

	public abstract class AssemblyTreeNode<Type extends ASTNode> extends
			DefaultMutableTreeNode implements AssemblyDroptarget {
		private static final long serialVersionUID = -177757859868743964L;

		protected Type data;

		public AssemblyTreeNode(String arg0) {
			super(arg0);
		}

		public abstract String getLabel();

		public String getTooltip() {
			return "";
		}

		public boolean acceptsDrop(Resource res) {
			return false;
		}

		public void acceptDrop(Resource res) {
			// setUnsaved(true);
		}

		public void showContextMenu(int x, int y) {
		};

		public void removeFromParent() {
			if (data != null) {
				data.remove();
			}
			super.removeFromParent();
			// setUnsaved(true);
		}
	}

	private class ServiceDescriptionTreeNode extends
			AssemblyTreeNode<PRDServiceFMDescription> {
		public ServiceDescriptionTreeNode(PRDServiceFMDescription description) {
			super("ServiceDescription");
			this.data = description;

			for (int i = 0; i < data.getNumControlInfo(); ++i) {
				ControlInfo ci = data.getControlInfo(i);
				if (ci instanceof CommandInfo) {
					// add(new CommandTreeNode((CommandInfo) ci));
				} else if (ci instanceof GroupInfo) {
					// add(new GroupTreeNode((GroupInfo) ci));
				}
			}
		}

		@Override
		public String getLabel() {
			return "ServiceDescription";
		}

		@Override
		public void showContextMenu(int x, int y) {
			// serviceDescrptionMenu.show(assemblyTree, x, y);
		}

		void addCommand(String name, String direction) {
			CommandInfo ci = new CommandInfo(name, direction);
			ci.setCommandNumber(data.findHighestCommandNumber() + 1);
			data.addControlInfo(ci);
			// add(new CommandTreeNode(ci));
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// model.nodesWereInserted(ServiceDescriptionTreeNode.this,
					// new int[] {getChildCount() - 1});
				}
			});
			// setUnsaved(true);
		}

		public void addGroup(String name, String help) {
			GroupInfo gi = new GroupInfo(name, help);
			data.addControlInfo(gi);
			// add(new GroupTreeNode(gi));
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// model.nodesWereInserted(ServiceDescriptionTreeNode.this,
					// new int[] {getChildCount() - 1});
				}
			});
			// setUnsaved(true);
		}
	}
}
