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
import se.lth.cs.palcom.browsergui.TwoStringsDialog;
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
		serviceScrollPane.setBorder(BorderFactory.createBevelBorder(1));
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
		ge.addSynthService(new ServiceObjGUI(ss));
		displayServices();
	}
	
	public void removeService(SynthesizedService ss) {
		ge.removeSynthService(ss);
		displayServices();
	}

	public void displayServices() {
		servicePanel.removeAll();
		for (ServiceObjGUI ss : ge.getSynthServices()) {
			servicePanel.add(ss);
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

	
	private class ServiceTree extends JTree implements DragGestureListener, DragSourceListener{
		DragSource dragSource;
		ServiceObjGUI sobj;
		public ServiceTree(DefaultTreeModel model, ServiceObjGUI sobj) {
			super(model);
			this.sobj = sobj;
			dragSource = new DragSource();
			dragSource.createDefaultDragGestureRecognizer(this,
					DnDConstants.ACTION_COPY_OR_MOVE, this);
		}

		public void dragEnter(DragSourceDragEvent dsde) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		}
		public void dragOver(DragSourceDragEvent dsde) {}
		public void dropActionChanged(DragSourceDragEvent dsde) {}
		public void dragExit(DragSourceEvent dse) {
			sobj.setBorder(BorderFactory.createLineBorder(new Color(109, 134, 173), 2));
			dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
		}
		public void dragDropEnd(DragSourceDropEvent dsde) {
			sobj.setBorder(BorderFactory.createLineBorder(new Color(109, 134, 173), 2));
		}
		
		public void dragGestureRecognized(DragGestureEvent dge) {
			sobj.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createDashedBorder(Color.green),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			
			Transferable transferable = new Transferable() {
				public DataFlavor[] getTransferDataFlavors() {
					return new DataFlavor[] { new DataFlavor(SynthesizedService.class, "synthesisedService") };
				}

				public boolean isDataFlavorSupported(DataFlavor flavor) {
					if (!isDataFlavorSupported(flavor)) {
						return false;
					}
					return true;
				}

				public Object getTransferData(DataFlavor flavor)
						throws UnsupportedFlavorException, IOException {
					return sobj.ss;
				}
			};
			dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable,
					this);
		}
		
	}
	public class ServiceObjGUI extends JPanel implements ActionListener {
		SynthesizedService ss;
		DragSource dragSource;
		ServiceTree tree;
		DefaultTreeModel model;
		private final TwoStringsDialog twoStringsDialog = new TwoStringsDialog();
		public ServiceObjGUI(SynthesizedService ss) {
			this.ss = ss;
			this.setPreferredSize(new Dimension(130, 100));
			this.setLayout(new GridLayout(0, 1));
			Color deviceBlue = new Color(196, 219, 255);
			this.setBackground(deviceBlue);
			this.setBorder(BorderFactory.createLineBorder(new Color(109, 134, 173), 2));
			// this.setAutoscrolls(true);
			model = new DefaultTreeModel(
					new DefaultMutableTreeNode("NULL"));
			tree = new ServiceTree(model, this);
			model.setRoot(new SynthServiceNode(ss, this));
			DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) (tree
					.getCellRenderer());
			renderer.setBackgroundNonSelectionColor(deviceBlue);
			renderer.setBackgroundSelectionColor(deviceBlue);
			renderer.setTextNonSelectionColor(new Color(69, 60, 43));
			renderer.setTextSelectionColor(new Color(69, 60, 43));
			tree.setBackground(new Color(196, 219, 255));
//			tree.setScrollsOnExpand(true);
			tree.collapseRow(0);
//			dragSource = new DragSource();
//			dragSource.createDefaultDragGestureRecognizer(tree,
//					DnDConstants.ACTION_COPY_OR_MOVE, tree);
			JScrollPane scroll = new JScrollPane(tree);
			scroll.setBorder(null);
//			scroll.add(tree);
			add(scroll);
			
			
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

		
		
//		public void dragEnter(DragSourceDragEvent dsde) {
//			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
//		}
//		public void dragOver(DragSourceDragEvent dsde) {}
//		public void dropActionChanged(DragSourceDragEvent dsde) {}
//		public void dragExit(DragSourceEvent dse) {
//			this.setBorder(BorderFactory.createCompoundBorder(
//					BorderFactory.createLineBorder(new Color(109, 134, 173), 2),
//					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
//			dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
//		}
//		public void dragDropEnd(DragSourceDropEvent dsde) {
//			this.setBorder(BorderFactory.createCompoundBorder(
//					BorderFactory.createLineBorder(new Color(109, 134, 173), 2),
//					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
//		}
//		
//		public void dragGestureRecognized(DragGestureEvent dge) {
//			this.setBorder(BorderFactory.createCompoundBorder(
//					BorderFactory.createDashedBorder(Color.green),
//					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
//			
//			Transferable transferable = new Transferable() {
//				public DataFlavor[] getTransferDataFlavors() {
//					return new DataFlavor[] { new DataFlavor(SynthesizedService.class, "synthesisedService") };
//				}
//
//				public boolean isDataFlavorSupported(DataFlavor flavor) {
//					if (!isDataFlavorSupported(flavor)) {
//						return false;
//					}
//					return true;
//				}
//
//				public Object getTransferData(DataFlavor flavor)
//						throws UnsupportedFlavorException, IOException {
//					return ss;
//				}
//			};
//			dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable,
//					this);
//		}
//		
		
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			final TreeNode node = (TreeNode) tree.getSelectionPath().getLastPathComponent();
			if (cmd.equals("removeSS")) {
				RemoveSSMenu rm = (RemoveSSMenu) ((JMenuItem)e.getSource()).getParent();
				removeService(rm.ss);
			} else if(cmd.equals("AddCommand")){
				try {
					taskExecutor.execute(new Runnable() {
						public void run() {
							//String[] vals = twoStringsDialog.getStrings("Name", "Direction", "", "in");
							String[] vals = AddCommandDialog.getStrings(GraphSynthServicePanel.this);
							if (node instanceof SynthServiceNode) {
								((SynthServiceNode)node).addCommand(vals[0], vals[1]);
							} else if (node instanceof GroupTreeNode) {
								((GroupTreeNode)node).addCommand(vals[0], vals[1]);
							}
						}
					});
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
//				System.out.println("Add command");
			} else if(cmd.equals("AddGroup")){
				try {
					taskExecutor.execute(new Runnable() {
						public void run() {
							String[] vals = twoStringsDialog.getStrings("Name", "Help text", "", "");
							if (node instanceof SynthServiceNode) {
								((SynthServiceNode)node).addGroup(vals[0], vals[1]);
							} else if (node instanceof GroupTreeNode) {
								((GroupTreeNode)node).addGroup(vals[0], vals[1]);
							}
						}
					});
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
//				System.out.println("Add group");
			} else if(cmd.equals("DeleteSSD")){
				try {
					if (node instanceof SynthServiceNode) {
					taskExecutor.execute(new Runnable() {
						public void run() {
//							System.out.println("deleting service");
							removeService(((SynthServiceNode)node).ssobj.ss);					
						}
					});
					} else {
						((DefaultMutableTreeNode) node).removeFromParent();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
//								System.out.println("deleting");
								model.nodeStructureChanged(node);
//								model.reload(((DefaultMutableTreeNode) node).getParent());
//								tree.updateUI();
							}
						});
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else if(cmd.equals("AddParam")){
				final CommandTreeNode cmdNode = (CommandTreeNode) tree.getSelectionPath().getLastPathComponent();
				try {
					taskExecutor.execute(new Runnable() {
						public void run() {
							String[] vals = twoStringsDialog.getStrings("Name", "Type", "", "");
							cmdNode.addParam(vals[0], vals[1]);
						}
					});
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		}
		
		boolean equals(SynthesizedService ss){
//			System.out.println("checking equals");
			return this.ss == ss;
		}

	}
	

	private class SynthServiceNode extends AssemblyTreeNode<PRDServiceFMDescription> {
		JPopupMenu serviceDescriptionMenu;
		ServiceObjGUI ssobj;
		SynthesizedService service;
		public SynthServiceNode(SynthesizedService service, ServiceObjGUI ssobj) {
			super("");
			this.data = service.getPRDServiceFMDescription();
			this.ssobj = ssobj;
			this.service = service;
			serviceDescriptionMenu = menues.createServiceDescriptionMenu(ssobj, service);
//			ServiceDescriptionTreeNode sdtNode = new ServiceDescriptionTreeNode(service.getPRDServiceFMDescription(), ssobj);
//			add(sdtNode);
			this.setUserObject(getLabel());
		}

		@Override
		public String getLabel() {
			StringBuilder sb = new StringBuilder();
			sb.append(data.getID());
			sb.append(" (");
			if (service.getDistribution() == PRDService.BROADCAST) {
				sb.append("Broadcast");
			} else if (service.getDistribution() == PRDService.GROUPCAST) {
				sb.append("Groupcast");
			} else if (service.getDistribution() == PRDService.RADIOCAST) {
				sb.append("Radiocast");
			} else if (service.getDistribution() == PRDService.UNICAST) {
				sb.append("Unicast");
			}
			if (service.getReqAuth()) {
				sb.append(" (UserID)");
			}
			sb.append(")");

			return sb.toString();
		}
		@Override
		public void showContextMenu(int x, int y) {
			serviceDescriptionMenu.show(ssobj, x, y);
		}
		
		void addCommand(String name, String direction) {
			CommandInfo ci = new CommandInfo(name, direction);
			ci.setCommandNumber(data.findHighestCommandNumber() + 1);
			data.addControlInfo(ci);
			CommandTreeNode ctn = new CommandTreeNode(ci, ssobj);
			ctn.setUserObject(ctn.getLabel());
			add(ctn);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ssobj.model.nodesWereInserted(
							SynthServiceNode.this,
							new int[] { getChildCount() - 1 });
				}
			});
//			System.out.println("Added " + ci);
			// setUnsaved(true);
		}
		
		public void addGroup(String name, String help) {
			GroupInfo gi = new GroupInfo(name, help);
			data.addControlInfo(gi);
			GroupTreeNode gtn = new GroupTreeNode(gi, ssobj);
			gtn.setUserObject(gtn.getLabel());
			add(gtn);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ssobj.model.nodesWereInserted(SynthServiceNode.this,
					 new int[] {getChildCount() - 1});
				}
			});
			// setUnsaved(true);
		}
	}
	
	private class GroupTreeNode extends AssemblyTreeNode<GroupInfo> {
		ServiceObjGUI ssobj;
		JPopupMenu serviceDescriptionMenu;
		public GroupTreeNode(GroupInfo groupInfo, ServiceObjGUI ssobj) {
			super("");
			this.data = groupInfo;
			this.ssobj = ssobj;
			serviceDescriptionMenu = menues.createServiceDescriptionMenu(ssobj, ssobj.ss);
			for (int i = 0; i < groupInfo.getNumControlInfo(); ++i) {
				ControlInfo ci = groupInfo.getControlInfo(i);
				if (ci instanceof GroupInfo) {
					add(new GroupTreeNode((GroupInfo) ci, ssobj)); 
				} else {
					add(new CommandTreeNode((CommandInfo) ci, ssobj)); 
				}
			}
		}
		
		void addCommand(String name, String direction) {
			CommandInfo ci = new CommandInfo(name, direction);
			// Find and add command number 
			ASTNode n = data.getParent();
			while (n != null) {
				if (n instanceof PRDServiceFMDescription) {
					ci.setCommandNumber(((PRDServiceFMDescription)n).findHighestCommandNumber() + 1);
					break;
				}
				n = n.getParent();
			}
			data.addControlInfo(ci);
			CommandTreeNode ctn = new CommandTreeNode(ci, ssobj);
			ctn.setUserObject(ctn.getLabel());
			add(ctn); 
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ssobj.model.nodesWereInserted(GroupTreeNode.this, new int[] {getChildCount() - 1});
				}
			});
		}

		public void addGroup(String name, String help) {
			GroupInfo gi = new GroupInfo(name, help);
			data.addControlInfo(gi);
			GroupTreeNode gtn = new GroupTreeNode(gi, ssobj);
			gtn.setUserObject(gtn.getLabel());
			add(gtn); 
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ssobj.model.nodesWereInserted(GroupTreeNode.this, new int[] {getChildCount() - 1});
				}
			});
//			setUnsaved(true);
		}

		@Override
		public String getLabel() {
			return data.getID() + "(" + data.getHelp() + ")";
		}
		
		@Override
		public void showContextMenu(int x, int y) {
			serviceDescriptionMenu.show(ssobj, x, y);
		}
	}
	
	private class CommandTreeNode extends AssemblyTreeNode<CommandInfo> {
		JPopupMenu commandMenu;
		ServiceObjGUI ssobj;
		public CommandTreeNode(CommandInfo info, ServiceObjGUI ssobj) {
			super("");
			this.data = info;
			this.ssobj = ssobj;
			commandMenu = menues.createCommandMenu(ssobj);
			for (int i = 0; i < info.getNumParamInfo(); ++i) {
				add(new ParamTreeNode(info.getParamInfo(i), ssobj));
			}
		}


		@Override
		public String getLabel() {
			return data.getID() + " (" + data.getDirection() + ")";
		}

		@Override
		public void showContextMenu(int x, int y) {
			commandMenu.show(ssobj, x, y);
		}

		public void addParam(String name, String type) {
			ParamInfo inf = new ParamInfo(name, type); 
			data.addParamInfo(inf);
			add(new ParamTreeNode(new ParamInfo(name ,type), ssobj));
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ssobj.model.nodesWereInserted(CommandTreeNode.this, new int[] {getChildCount() - 1});
				}
			});
//			setUnsaved(true);
		}
	}
	
	private class ParamTreeNode extends AssemblyTreeNode<ParamInfo> {
		ServiceObjGUI ssobj;
		JPopupMenu paramMenu;
		public ParamTreeNode(ParamInfo pi, ServiceObjGUI ssobj) {
			super("Param");
			data = pi;
			this.ssobj = ssobj;
			paramMenu = menues.createDeviceMenu(ssobj);
			setUserObject(getLabel());
		}

		@Override
		public String getLabel() {
			return data.getID() + " (" + data.getType() + ")";
		}
		
		@Override
		public void showContextMenu(int x, int y) {
			paramMenu.show(ssobj, x, y);
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
	
}
