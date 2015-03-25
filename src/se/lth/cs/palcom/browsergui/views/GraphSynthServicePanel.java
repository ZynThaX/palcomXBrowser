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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import se.lth.cs.palcom.browsergui.AddCommandDialog;
import se.lth.cs.palcom.browsergui.AddSynthServiceDialog;
import se.lth.cs.palcom.browsergui.AssemblyDroptarget;
import se.lth.cs.palcom.browsergui.views.GraphSynthServiceMenues.RemoveSSMenu;
import se.lth.cs.palcom.discovery.proxy.Resource;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.ThreadedExecutor;

public class GraphSynthServicePanel extends JPanel {
	private JButton addSynthService;
	private GraphEditor ge;
	private JPanel servicePanel;
	private GraphSynthServiceMenues menues;
	private final Executor taskExecutor = new ThreadedExecutor();
	
	public GraphSynthServicePanel(GraphEditor ge) {
		this.ge = ge;
		menues = new GraphSynthServiceMenues();
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
		displayServices();
	}
	
	public void removeService(SynthesizedService ss) {
		ge.removeSynthService(ss);
		displayServices();
	}

	public void displayServices() {
		servicePanel.removeAll();
		for (SynthesizedService ss : ge.getSynthServices()) {
			servicePanel.add(new ServiceObjGUI(ss));
		}
		servicePanel.repaint();
	}

	public void toggle() {
		if (this.isVisible()) {
			this.setVisible(false);
		} else {
			this.setVisible(true);
		}
	}

	

	public class ServiceObjGUI extends JPanel implements DragGestureListener,
			DragSourceListener, ActionListener {
		SynthesizedService ss;
		DragSource dragSource;
		JTree tree;
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
			tree = new JTree(model);
			model.setRoot(new SynthServiceNode(ss, this));
			DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) (tree
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
			tree.setBackground(new Color(196, 219, 255));
			tree.setScrollsOnExpand(false);
			tree.collapseRow(0);
			dragSource = new DragSource();
			dragSource.createDefaultDragGestureRecognizer(this,
					DnDConstants.ACTION_COPY_OR_MOVE, this);
			add(tree);
			
			
			tree.addMouseListener(new MouseListener() {
				public void mouseReleased(MouseEvent e) { doPopup(e);}
				public void mousePressed(MouseEvent e) {doPopup(e);}
				public void mouseExited(MouseEvent e) { }
				public void mouseEntered(MouseEvent e) { }
				public void mouseClicked(MouseEvent e) { }
			});
			
			
			
			
			this.addMouseListener(new MouseListener(){
				public void mouseReleased(MouseEvent e) {
				    if(SwingUtilities.isRightMouseButton(e)){
				        RemoveSSMenu rssm = menues.createRemoveSSMenu(ServiceObjGUI.this, ServiceObjGUI.this.ss);
				        rssm.show(ServiceObjGUI.this, e.getX(), e.getY());
				    }
				}
				public void mouseClicked(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
			});
			
			
			
		}
		
		private void doPopup(MouseEvent me) {
			if (me.isPopupTrigger()) {
				TreePath selectionPath = tree.getPathForLocation(me.getX(), me.getY());
				if (selectionPath == null) {
					return;
				}
				tree.setSelectionPath(selectionPath);
				TreeNode node = (TreeNode) selectionPath.getLastPathComponent();
				if (node instanceof AssemblyTreeNode) {
					((AssemblyTreeNode)node).showContextMenu(me.getX(), me.getY());
				}
			}
		}

		
		
		public void dragEnter(DragSourceDragEvent dsde) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		}
		public void dragOver(DragSourceDragEvent dsde) {}
		public void dropActionChanged(DragSourceDragEvent dsde) {}
		public void dragExit(DragSourceEvent dse) {
			this.setBorder(BorderFactory.createLineBorder(new Color(109, 134, 173)));
			dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
		}
		public void dragDropEnd(DragSourceDropEvent dsde) {
			this.setBorder(BorderFactory.createLineBorder(new Color(109, 134, 173)));
		}
		public void dragGestureRecognized(DragGestureEvent dge) {
			Transferable transferable = new StringSelection("hellow");
			this.setBorder(BorderFactory.createDashedBorder(Color.GREEN));
			dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable,
					this);
		}
		
		
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("removeSS")) {
				RemoveSSMenu rm = (RemoveSSMenu) ((JMenuItem)e.getSource()).getParent();
				removeService(rm.ss);
			} else if(cmd.equals("AddCommand")){
				final TreeNode node = (TreeNode) tree.getSelectionPath().getLastPathComponent();
				try {
					taskExecutor.execute(new Runnable() {
						public void run() {
							//String[] vals = twoStringsDialog.getStrings("Name", "Direction", "", "in");
							String[] vals = AddCommandDialog.getStrings(GraphSynthServicePanel.this);
							if (node instanceof ServiceDescriptionTreeNode) {
//								((ServiceDescriptionTreeNode)node).addCommand(vals[0], vals[1]);
//							} else if (node instanceof GroupTreeNode) {
//								((GroupTreeNode)node).addCommand(vals[0], vals[1]);
							}
						}
					});
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("Add command");
			} else if(cmd.equals("AddGroup")){
				System.out.println("Add group");
			} else if(cmd.equals("DeleteSSD")){
				System.out.println("Delete ss description");
			}

		}

	}
	

	private class SynthServiceNode extends AssemblyTreeNode<SynthesizedService> {
		public SynthServiceNode(SynthesizedService service, ServiceObjGUI ssobj) {
			super("");
			this.data = service;
			ServiceDescriptionTreeNode sdtNode = new ServiceDescriptionTreeNode(service.getPRDServiceFMDescription(), ssobj);
			add(sdtNode);
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
		JPopupMenu serviceDescrptionMenu;
		ServiceObjGUI ssobj;
		public ServiceDescriptionTreeNode(PRDServiceFMDescription description, ServiceObjGUI ssobj) {
			super("ServiceDescription");
			this.ssobj = ssobj;
			serviceDescrptionMenu = menues.createServiceDescriptionMenu(ssobj);
			this.data = description;
			
			for (int i = 0; i < data.getNumControlInfo(); ++i) {
				ControlInfo ci = data.getControlInfo(i);
				if (ci instanceof CommandInfo) {
//					add(new CommandTreeNode((CommandInfo) ci));
				} else if (ci instanceof GroupInfo) {
//					add(new GroupTreeNode((GroupInfo) ci));
				}
			}
		}

		@Override
		public String getLabel() {
			return "ServiceDescription";
		}

		@Override
		public void showContextMenu(int x, int y) {
			serviceDescrptionMenu.show(ssobj, x, y);
		}
//
//		void addCommand(String name, String direction) {
//			CommandInfo ci = new CommandInfo(name, direction);
//			ci.setCommandNumber(data.findHighestCommandNumber() + 1);
//			data.addControlInfo(ci);
//			add(new CommandTreeNode(ci));
//			SwingUtilities.invokeLater(new Runnable() {
//				public void run() {
//					 model.nodesWereInserted(ServiceDescriptionTreeNode.this,
//					 new int[] {getChildCount() - 1});
//				}
//			});
//			// setUnsaved(true);
//		}
//
//		public void addGroup(String name, String help) {
//			GroupInfo gi = new GroupInfo(name, help);
//			data.addControlInfo(gi);
//			// add(new GroupTreeNode(gi));
//			SwingUtilities.invokeLater(new Runnable() {
//				public void run() {
//					// model.nodesWereInserted(ServiceDescriptionTreeNode.this,
//					// new int[] {getChildCount() - 1});
//				}
//			});
//			// setUnsaved(true);
//		}
	}
}
