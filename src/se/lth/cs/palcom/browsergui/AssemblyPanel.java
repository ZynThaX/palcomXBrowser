package se.lth.cs.palcom.browsergui;

import internal.org.kxml2.io.KXmlSerializer;
import ist.palcom.resource.descriptor.ASTNode;
import ist.palcom.resource.descriptor.AbstractServiceDecl;
import ist.palcom.resource.descriptor.Action;
import ist.palcom.resource.descriptor.AddressUse;
import ist.palcom.resource.descriptor.AltServiceDecl;
import ist.palcom.resource.descriptor.AltServiceDeclList;
import ist.palcom.resource.descriptor.AssemblyID;
import ist.palcom.resource.descriptor.AssignAction;
import ist.palcom.resource.descriptor.CommandEvent;
import ist.palcom.resource.descriptor.CommandInfo;
import ist.palcom.resource.descriptor.ConnectionDecl;
import ist.palcom.resource.descriptor.ConnectionDeclList;
import ist.palcom.resource.descriptor.ConnectionEvent;
import ist.palcom.resource.descriptor.ConstantUse;
import ist.palcom.resource.descriptor.ControlInfo;
import ist.palcom.resource.descriptor.DeviceAddress;
import ist.palcom.resource.descriptor.DeviceAddressDecl;
import ist.palcom.resource.descriptor.DeviceDecl;
import ist.palcom.resource.descriptor.DeviceDeclList;
import ist.palcom.resource.descriptor.DeviceID;
import ist.palcom.resource.descriptor.DeviceUse;
import ist.palcom.resource.descriptor.Event;
import ist.palcom.resource.descriptor.EventHandlerClause;
import ist.palcom.resource.descriptor.EventHandlerList;
import ist.palcom.resource.descriptor.EventHandlerScript;
import ist.palcom.resource.descriptor.GroupInfo;
import ist.palcom.resource.descriptor.Identifier;
import ist.palcom.resource.descriptor.InvokeAction;
import ist.palcom.resource.descriptor.List;
import ist.palcom.resource.descriptor.MimeType;
import ist.palcom.resource.descriptor.Opt;
import ist.palcom.resource.descriptor.PRDAssemblyD;
import ist.palcom.resource.descriptor.PRDAssemblyVer;
import ist.palcom.resource.descriptor.PRDService;
import ist.palcom.resource.descriptor.PRDServiceFMDescription;
import ist.palcom.resource.descriptor.ParamInfo;
import ist.palcom.resource.descriptor.ParamUse;
import ist.palcom.resource.descriptor.RuntimeIdentifier;
import ist.palcom.resource.descriptor.SelfDeviceDecl;
import ist.palcom.resource.descriptor.SendMessageAction;
import ist.palcom.resource.descriptor.ServiceDecl;
import ist.palcom.resource.descriptor.ServiceDeclList;
import ist.palcom.resource.descriptor.ServiceID;
import ist.palcom.resource.descriptor.ServiceUse;
import ist.palcom.resource.descriptor.SingleServiceDecl;
import ist.palcom.resource.descriptor.SynthesizedService;
import ist.palcom.resource.descriptor.SynthesizedServiceList;
import ist.palcom.resource.descriptor.SynthesizedServiceUse;
import ist.palcom.resource.descriptor.ThisService;
import ist.palcom.resource.descriptor.Use;
import ist.palcom.resource.descriptor.VariableDecl;
import ist.palcom.resource.descriptor.VariableList;
import ist.palcom.resource.descriptor.VariableUse;
import ist.palcom.resource.descriptor.VersionPart;
import ist.palcom.xml.XMLFactory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import se.lth.cs.palcom.assembly.AssemblyLoadException;
import se.lth.cs.palcom.assembly.OldschoolAssemblyLoader;
import se.lth.cs.palcom.browsergui.dnd.AssemblyGraphTransferHandler;
import se.lth.cs.palcom.browsergui.dnd.CommandWrapper;
import se.lth.cs.palcom.browsergui.dnd.DNDResourceWrapper;
import se.lth.cs.palcom.browsergui.dnd.ResourceTransferable;
import se.lth.cs.palcom.browsergui.dnd.ServiceCommandWrapper;
import se.lth.cs.palcom.browsergui.dnd.SynthServiceCommandWrapper;
import se.lth.cs.palcom.browsergui.views.GraphEditor;
import se.lth.cs.palcom.browsergui.views.XmlTextPane;
import se.lth.cs.palcom.discovery.ResourceException;
import se.lth.cs.palcom.discovery.proxy.PalcomDevice;
import se.lth.cs.palcom.discovery.proxy.PalcomService;
import se.lth.cs.palcom.discovery.proxy.Resource;
import se.lth.cs.palcom.discovery.proxy.ResourceListener;
import se.lth.cs.palcom.logging.Logger;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.ThreadedExecutor;

public class AssemblyPanel extends JPanel implements ChangeListener, MouseListener, ActionListener, KeyListener {
	public static String TAB_NAME_XML = "XML";
	public static String TAB_NAME_EDITOR = "Editor";
	public static String TAB_NAME_GRAPH = "Graph editor";
	private JTree assemblyTree;
	private XmlTextPane xmlText;
	private final BrowserApplication application;
	private AssemblyRootNode assemblyRoot;
	private GraphEditor assemblyGraph;
	private DefaultTreeModel model;
	private String filename;
	private String assemblyData;
	private String prevTab;
	public static String assemblyFormat = "3.0.14"; 
	private String oldAssemblyFormat = "AssemblyFormat"; //XXX: This was hardcoded until 3.0.14. It's still compatible, so keep this around for a while to avoid annoying warnings...
	
	private HashMap<String, String> deviceIDlookup = new HashMap<String, String>();
	private HashMap<ServiceID, String> serviceIDlookup = new HashMap<ServiceID, String>();
	private java.util.List<String> variableList = new LinkedList<String>();
	private java.util.List<String> declaredConnections = new LinkedList<String>();
	
	private final HashMap<Identifier, DeviceTreeNode> deviceTreeNodeMapping = new HashMap<Identifier, AssemblyPanel.DeviceTreeNode>();
	
	private final Executor taskExecutor = new ThreadedExecutor();
	
	private boolean unsaved = false;
	private JTabbedPane tabs;
	
	public AssemblyPanel(String filename, AssemblyTransferHandler ath, BrowserApplication app, PRDAssemblyD assembly) {
		application = app;
		this.filename = filename;
		
		model = new DefaultTreeModel(new DefaultMutableTreeNode("NULL"));
		assemblyTree = new JTree(model);
		
		assemblyTree.setDropMode(DropMode.ON);
		assemblyTree.setTransferHandler(ath);
		assemblyTree.setCellRenderer(new AssemblyCellRenderer());
		assemblyTree.addMouseListener(this);
		assemblyTree.setDragEnabled(true);
		
		ToolTipManager.sharedInstance().registerComponent(assemblyTree);
		setLayout(new BorderLayout());
		
		tabs = new JTabbedPane(JTabbedPane.BOTTOM);
		
		assemblyGraph = new GraphEditor();
		prevTab = TAB_NAME_GRAPH;
		tabs.add(TAB_NAME_GRAPH, new JScrollPane(assemblyGraph));
		
		tabs.add(TAB_NAME_EDITOR, new JScrollPane(assemblyTree));
		tabs.addChangeListener(this);
		
		
		assembly = (PRDAssemblyD) loadAssemblyTree(assembly).fullCopy();
		if (assembly.getName() == null || assembly.getName().equals("")) {
			assembly.setName(filename);
		}
		assemblyData = assemblyRoot.writeXML();
		xmlText = new XmlTextPane();
		xmlText.addKeyListener(this);
		
		xmlText.setText(assemblyData);
		assemblyGraph.setGraph(assemblyData);
		
		tabs.add(TAB_NAME_XML, new JScrollPane(xmlText));
		add(tabs);
	}
	
	private PRDAssemblyVer loadAssemblyTree(byte[] data) {
		PRDAssemblyD assembly = null;
		
		DeviceID did = application.getDevice().getDeviceID();
		
		PRDAssemblyVer version = null;
		if (data.length > 0) {
			try {
				assembly = OldschoolAssemblyLoader.parseAssembly(data, 0, data.length, null);
				loadAssemblyTree(assembly);
				if (!assembly.getFormat().equals(assemblyFormat) && !assembly.getFormat().equals(oldAssemblyFormat)) {
					JOptionPane.showMessageDialog(this, "Warning: The assembly format " + assembly.getFormat() + " doesn't match the expected format " + assemblyFormat + " It will probably still work if you got this far...");
				}
			} catch (AssemblyLoadException e) {
				e.printStackTrace();
			}
		} else {
			version = new PRDAssemblyVer();
			
			assembly = new PRDAssemblyD();
			assembly.setName(filename);
			assembly.setFormat(version.getFormat());
			
			version.setVersion(new AssemblyID(new VersionPart(did, "Version"), new VersionPart(did, "Version?"), new Opt(), new Opt(), "1.0"));
			version.setFormat(assemblyFormat);
			version.setDevices(new DeviceDeclList());
			version.setServices(new ServiceDeclList());
			version.setConnections(new ConnectionDeclList());
			version.setSynthesizedServices(new SynthesizedServiceList());
			version.setEventHandlerScript(new EventHandlerScript(new VariableList(new List()), new EventHandlerList(new List())));
			assembly.addPRDAssemblyVer((PRDAssemblyVer) version.fullCopy());
		}
		
		
		assemblyRoot = new AssemblyRootNode(assembly);
		
		model.setRoot(assemblyRoot);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				model.nodeChanged(assemblyRoot);
				model.nodeStructureChanged(assemblyRoot);
			}
		});
		
		return version;
	}
	
	private PRDAssemblyD loadAssemblyTree(PRDAssemblyD assembly) {
		DeviceID did = application.getDevice().getDeviceID();
		
		if (assembly == null) {
			assembly = new PRDAssemblyD();
			assembly.setName(filename);
			assembly.setFormat(assemblyFormat);
			
			VersionPart vp = new VersionPart(did, "1.0");
			assembly.setBaseVersion(vp);
		}
		
		if (!assembly.getFormat().equals(assemblyFormat) && !assembly.getFormat().equals(oldAssemblyFormat)) {
			JOptionPane.showMessageDialog(this, "The assembly format " + assembly.getFormat() + " doesn't match the expected format " + assemblyFormat);
		}
		
		PRDAssemblyVer version = null;
		if (assembly.getNumPRDAssemblyVer() > 0) {
			version = assembly.getPRDAssemblyVer(0); //XXX: For now, there must be exactly one version.
		}
		if (version != null) {
			for (int i = 0; i < version.getDevices().getNumDeviceDecl(); ++i) {
				DeviceDecl dd = version.getDevices().getDeviceDecl(i);
				if (dd instanceof DeviceAddressDecl) {
					deviceIDlookup.put(((DeviceAddressDecl)dd).getDeviceAddress().getDeviceID().getString(), dd.getNameID().getID());
				} else {
					// Handle self decl here
				}
			}
			for (int j = 0; j < version.getServices().getNumServiceDecl(); ++j) {
				AbstractServiceDecl sd = version.getServices().getServiceDecl(j).getDecl();
				String sid = sd.getLocalName().getID();
				Pattern p = Pattern.compile("\\d+");
				Matcher m = p.matcher(sid);
				if (m.find()) {
					int nc = Integer.parseInt(m.group());
					if (nc > serviceCounter) {
						serviceCounter = nc;
					}
				}
				if (sd instanceof SingleServiceDecl) {
					serviceIDlookup.put(((SingleServiceDecl)sd).getServiceID(), sid);
				} else if (sd instanceof AltServiceDeclList) {
					AltServiceDeclList asd = (AltServiceDeclList)sd;
					for (int i = 0; i < asd.getNumServiceDecl(); ++i) {
						AltServiceDecl dec = asd.getServiceDecl(i);
						serviceIDlookup.put(((AltServiceDecl)dec).getServiceID(), sid);
					}
				}
			}
			for (int i = 0; i < version.getConnections().getNumConnectionDecl(); ++i) {
				ConnectionDecl cd = version.getConnections().getConnectionDecl(i);
				Pattern p = Pattern.compile("\\d+");
				Matcher m = p.matcher(cd.getId());
				if (m.find()) {
					int nc = Integer.parseInt(m.group());
					if (nc > connectionCounter) {
						connectionCounter = nc;
					}
				}
			}
		} else {
			version = new PRDAssemblyVer();
			version.setVersion(new AssemblyID(new VersionPart(did, "Version"), new VersionPart(did, "Version?"), new Opt(), new Opt(), "1.0"));
			version.setFormat(assembly.getFormat());
			version.setDevices(new DeviceDeclList());
			version.setServices(new ServiceDeclList());
			version.setConnections(new ConnectionDeclList());
			version.setSynthesizedServices(new SynthesizedServiceList());
			version.setEventHandlerScript(new EventHandlerScript(new VariableList(new List()), new EventHandlerList(new List())));
			assembly.addPRDAssemblyVer((PRDAssemblyVer) version.fullCopy());
		}
		
		assemblyRoot = new AssemblyRootNode(assembly);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				model.setRoot(assemblyRoot);
				model.nodeChanged(assemblyRoot);
				model.nodeStructureChanged(assemblyRoot);
			}
		});
		
		return assembly;
	}

	public abstract class AssemblyTreeNode<Type extends ASTNode> extends DefaultMutableTreeNode implements AssemblyDroptarget {
		private static final long serialVersionUID = -177757859868743964L;
		
		protected Type data;
		
		public AssemblyTreeNode(String arg0) {
			super(arg0);
		}

		public abstract String getLabel();
		public String getTooltip() {return "";}
		public boolean acceptsDrop(Resource res) {
			return false;
		}
		public void acceptDrop(Resource res) {
			setUnsaved(true);
		}
		public void showContextMenu(int x, int y) {};
		
		public void removeFromParent() {
			if (data != null) {
				data.remove();
			}
			super.removeFromParent();
			setUnsaved(true);
		}
	}
	
	public class DeletableTreeNode<Type extends ASTNode> extends AssemblyTreeNode<Type> {

		public DeletableTreeNode(String str) {
			super(str);
		}

		@Override
		public String getLabel() {
			return (String)super.getUserObject();
		}

		@Override
		public void showContextMenu(int x, int y) {
			deviceMenu.show(assemblyTree, x, y);
		}
	}
	
	private class AssemblyRootNode extends AssemblyTreeNode<PRDAssemblyD> { //Doesn't have a parent type...
		private static final long serialVersionUID = -1619536617886202844L;

		public AssemblyRootNode(PRDAssemblyD ass) {
			super("AsseblyRoot");
			this.data = ass;
			
			if (data.getNumPRDAssemblyVer() > 0) {
				for (int i = 0; i < data.getNumPRDAssemblyVer(); ++i){
					add(new AssemblyVersionNode(data.getPRDAssemblyVer(i)));
				}
			}
		}

		@Override
		public String getLabel() {
			return data.getName();
		}

		public String writeXML() {
			try {
				ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
	
	            KXmlSerializer serializer = new KXmlSerializer();
				serializer.setOutput(resultStream, null);
	            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
	
	            serializer.startDocument("ISO-8859-1", null);
	            serializer.docdecl(" " + data.getTagName() + " SYSTEM \"" + XMLFactory.DTD + "\"");
	
	            data.writeXMLElement(serializer);
	            serializer.endDocument();
	
	            return new String(resultStream.toByteArray(), "UTF8");
				
			} catch (IOException e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}
	}
	
	private class AssemblyVersionNode extends AssemblyTreeNode<PRDAssemblyVer> {
		private static final long serialVersionUID = -5044051598599837246L;

		public AssemblyVersionNode(PRDAssemblyVer av) {
			super("AsseblyVersion");
			
			this.data = av;
			add(new DeviceListTreeNode(av.getDevices()));
			add(new ServiceListTreeNode(data.getServices()));
			add(new ConnectionListTreeNode(data.getConnections()));

			add(new ScriptTreeNode(data.getEventHandlerScript()));
			add(new SynthServiceListNode(data.getSynthesizedServices()));
		}

		@Override
		public String getLabel() {
			return data.getName() + " (" + data.getVersion().getLogicalVersion() + ")";
		}
	}
	
	class SynthServiceListNode extends AssemblyTreeNode<SynthesizedServiceList> {
		public SynthServiceListNode(SynthesizedServiceList synthesizedServiceList) {
			super("SS");
			this.data = synthesizedServiceList;
			
			for (int i = 0; i < data.getNumSynthesizedService(); ++i) {
				add(new SynthServiceNode(data.getSynthesizedService(i)));
			}
		}

		@Override
		public String getLabel() {
			return "Synthesised Services";
		}

		@Override
		public void showContextMenu(int x, int y) {
			synthServiceListMenu.show(AssemblyPanel.this, x, y);
		}
		
		void addService(SynthesizedService sserv) {
			data.addSynthesizedService(sserv);
			add(new SynthServiceNode(sserv));
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodesWereInserted(SynthServiceListNode.this, new int[] {getChildCount() - 1});
				}
			});
			setUnsaved(true);
		}
	}
	
	private class SynthServiceNode extends AssemblyTreeNode<SynthesizedService> {
		public SynthServiceNode(SynthesizedService service) {
			super("");
			this.data = service;
			add(new ServiceDescriptionTreeNode(service.getPRDServiceFMDescription()));
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
	
	private class DeviceListTreeNode extends AssemblyTreeNode<DeviceDeclList> {
		private static final long serialVersionUID = -2421337349327990306L;
		
		public DeviceListTreeNode(DeviceDeclList deviceDeclList) {
			super("DeviceList");
			this.data = deviceDeclList;
			
			for (int i = 0; i < deviceDeclList.getNumDeviceDecl(); ++i) {
				add(new DeviceTreeNode(deviceDeclList.getDeviceDecl(i)));
			}
		}
		@Override
		public String getLabel() {
			return "Devices";
		}

		@Override
		public boolean acceptsDrop(Resource res) {
			return (res instanceof PalcomDevice);
		}
		
		@Override
		public synchronized void acceptDrop(Resource res) {
			try {
				super.acceptDrop(res);
				if (!(res instanceof PalcomDevice)) {
					return;
				}
				PalcomDevice dev = (PalcomDevice)res;
				DeviceDecl decl;
					decl = new DeviceAddressDecl(new Identifier(dev.getName()), new DeviceAddress(dev.getDeviceID()));
				data.addDeviceDecl(decl);
				add(new DeviceTreeNode(decl));
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						model.nodesWereInserted(DeviceListTreeNode.this, new int[] {getChildCount() - 1});
					}
				});
				
				deviceIDlookup.put(dev.getDeviceID().getString(), dev.getName());
			} catch (ResourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		synchronized void makeSelf(DeviceAddressDecl decl, SelfDeviceDecl self) {
			for (int i = 0; i < data.getNumDeviceDecl(); ++i) {
				if (data.getDeviceDecl(i).equals(decl)) {
					data.getDeviceDeclList().removeChild(i);
					data.getDeviceDeclList().setChild(self, i);
					return;
				}
			}
		}
	}
	
	private static int serviceCounter = 0;
	private class ServiceListTreeNode extends AssemblyTreeNode<ServiceDeclList> {
		private static final long serialVersionUID = -4750164995531319708L;
		
		public ServiceListTreeNode(ServiceDeclList services) {
			super("Services");
			this.data = services;
			
			for (int i = 0; i < services.getNumServiceDecl(); ++i) {
				AbstractServiceDecl decl = services.getServiceDecl(i).getDecl();
				if (decl instanceof SingleServiceDecl) {
					add(new ServiceTreeNode(decl.getLocalName().getID(), decl));
				} else {
					add(new ServiceAlternativeTreeNode(decl.getLocalName().getID(), (AltServiceDeclList) decl));
				}
			}
		}

		@Override
		public String getLabel() {
			return "Services";
		}

		@Override
		public boolean acceptsDrop(Resource res) {
			return (res instanceof PalcomService);
		}

		@Override
		public void acceptDrop(Resource res) {
			super.acceptDrop(res);
			if (!(res instanceof PalcomService)) {
				return;
			}
			PalcomService srv = (PalcomService)res;
			Identifier sid = new Identifier("s" + (++serviceCounter));
			String serviceName = "Unknown"; 
			try {
				serviceName = srv != null ? srv.getName() : "Unknown";
			} catch (ResourceException e1) {
				e1.printStackTrace();
			}
			try {
				ServiceDecl decl = new ServiceDecl(sid, new SingleServiceDecl(srv.requiresAuth(), new Identifier(serviceName), new DeviceUse(new Identifier(deviceIDlookup.get(srv.getDeviceID().toString()))), srv.getServiceID(), srv.getInstanceID().getInstanceNumber())); //UID Here!
				data.addServiceDecl(decl);
				add(new ServiceTreeNode(decl.getLocalName().getID(), decl.getDecl()));
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						model.nodesWereInserted(ServiceListTreeNode.this, new int[] {getChildCount() - 1});
					}
				});
				serviceIDlookup.put(srv.getServiceID(), /*srv.getName()*/ sid.getID());
			} catch (ResourceException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void showContextMenu(int x, int y) {
			serviceListMenu.show(assemblyTree, x, y);
		}
		
		public void addAlternative(final String sid) {
			final AltServiceDeclList asldl = new AltServiceDeclList(allowsChildren, new List()); //XXX: UID Here!
			add(new ServiceAlternativeTreeNode(sid, asldl));
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodesWereInserted(ServiceListTreeNode.this, new int[] {getChildCount() - 1});
					data.addServiceDecl(new ServiceDecl(new Identifier(sid), asldl));
				}
			});
		}
	}
	
	private class DeviceTreeNode extends AssemblyTreeNode<DeviceDecl> { //TODO: Split into Self/Address
		private static final long serialVersionUID = -2089041593280417711L;
		
		final LinkedList<ServiceTreeNode> changeListeners = new LinkedList<AssemblyPanel.ServiceTreeNode>();
		private void notifyListeners() {
			for (ServiceTreeNode stn : changeListeners) {
				stn.setDeviceDecl(data);
			}
		}
		

		public DeviceTreeNode(DeviceDecl dev) {
			super(dev.getName());
			data = dev;
			if (dev instanceof DeviceAddressDecl) {
				DeviceAddressDecl dd = (DeviceAddressDecl) dev;
				setToolTipText(dd.getDeviceAddress().getDeviceID().toString());
				
				deviceTreeNodeMapping.put(dd.getNameID(), this);
			} else {
				// Handle self decl here
				
				/*
				 * OBS! This block was not implemented, so I implemented it
				 * similarly to the block above, using the local device's ID
				 * instead. I am, however, not familiar to this part of PalCom
				 * and as such cannot guarantee that this is the best nor a
				 * correct implementation. It seemed reasonable and it did fix a
				 * problem related to the usage of "Self".
				 * 
				 * Mattias N.
				 */
				SelfDeviceDecl self = (SelfDeviceDecl) dev;
				setToolTipText(application.getDevice().toString());
				
				deviceTreeNodeMapping.put(self.getNameID(), this);
			}
		}
		
		public void makeSelfDevice() {
			SelfDeviceDecl self = new SelfDeviceDecl();
			((DeviceListTreeNode)getParent()).makeSelf((DeviceAddressDecl) data, self);
			data = self;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodeChanged(DeviceTreeNode.this);
				}
			});
			setUnsaved(true);
			notifyListeners();
		}

		@Override
		public String getLabel() {
			if (data instanceof DeviceAddressDecl) {
				return data.getNameID().getID();
			} else {
				return "Self";
			}
		}

		@Override
		public void showContextMenu(int x, int y) {
			deviceMenu.show(assemblyTree, x, y);
		}

		@Override
		public String getTooltip() {
			if (data instanceof DeviceAddressDecl) {
				DeviceAddressDecl dd = (DeviceAddressDecl) data;
				return dd.getDeviceAddress().getDeviceID().toString();
			} else {
				return "Self";
			}
		}

		@Override
		public boolean acceptsDrop(Resource res) {
			return (res instanceof PalcomDevice);
		}

		@Override
		public void acceptDrop(Resource res) {
			try {
				PalcomDevice dev = (PalcomDevice)res;
				DeviceAddressDecl decl;
					decl = new DeviceAddressDecl(new Identifier(dev.getName()), new DeviceAddress(dev.getDeviceID()));
				data.setNameID(decl.getNameID());
				
				if (data instanceof DeviceAddressDecl) {
					((DeviceAddressDecl)data).setDeviceAddress(decl.getDeviceAddress());
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						model.nodeChanged(DeviceTreeNode.this);
					}
				});
				super.acceptDrop(res);
				setUnsaved(true);
				notifyListeners();
			} catch (ResourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private class ServiceTreeNode extends AssemblyTreeNode<AbstractServiceDecl> implements TransferableTreeNode {
		private static final long serialVersionUID = -2089041593280417711L;
		private String id;
		
		public ServiceTreeNode(String id, AbstractServiceDecl srv) {
			super("");
			data = srv;
			this.id = id;
			
			if (data instanceof SingleServiceDecl) {
				DeviceUse du = ((SingleServiceDecl)data).getDeviceUse();
				
				DeviceTreeNode dtn = deviceTreeNodeMapping.get(du.getIdentifier());
				dtn.changeListeners.add(this);
			}
		}
		
		public Transferable createTransferable() {
			return new ResourceTransferable<DNDResourceWrapper<String[]>>(new DNDResourceWrapper<String[]>(new String[]{id, String.valueOf(data.getrequiresUid())}));
		}
		@Override
		public String getLabel() {
			StringBuilder sb = new StringBuilder();
			if (isAlternative()) {
				AltServiceDecl asd = (AltServiceDecl)data;
				sb.append(asd.getDeviceUse().getIdentifier().getID());
				sb.append("-");
				sb.append(asd.getServiceName().getID());
				sb.append(" : Priority ");
				sb.append(asd.getPrio());
			} else {
				SingleServiceDecl ssd = (SingleServiceDecl)data;
				sb.append(id);
				sb.append(":");
				sb.append(ssd.getDeviceUse().getIdentifier().getID());
				sb.append(".");
				sb.append(ssd.getServiceName().getID());
			}
			return sb.toString();
		}
		
		@Override
		public void showContextMenu(int x, int y) {
			serviceMenu.show(assemblyTree, x, y);
		}
		
		public boolean isAlternative() {
			return (data instanceof AltServiceDecl);
		}
		
		public void setPriority(int prio) {
			if (!isAlternative()) {
				throw new RuntimeException("This is not an alternative");
			}
			((AltServiceDecl)data).setPrio(prio);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodeChanged(ServiceTreeNode.this);
				}
			});
		}
		
		public void setDeviceDecl(DeviceDecl dd) {
			if (data instanceof SingleServiceDecl) {
				((SingleServiceDecl)data).setDeviceUse(new DeviceUse(dd.getNameID()));
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						model.nodeChanged(ServiceTreeNode.this);
					}
				});
			}
		}

		@Override
		public void removeFromParent() {
			ASTNode t = data;
			ASTNode p = data.getParent();
			ASTNode gp = p.getParent();
			
			while (!(gp instanceof ServiceDeclList)) {
				t = p;
				p = gp;
				gp = gp.getParent();
			}
			/*boolean removed = */t.remove();
			
			super.removeFromParent();
		}
		
		
	}
	
	private class ServiceAlternativeTreeNode extends AssemblyTreeNode<AltServiceDeclList> implements TransferableTreeNode {
		private static final long serialVersionUID = 3631885755064405967L;
		
		private String identifier;
		private int prio = 0;
		
		public ServiceAlternativeTreeNode(String id, AltServiceDeclList asldl) {
			super(id);
			identifier = id;
			data = asldl;
			for (int i = 0; i < asldl.getNumServiceDecl(); ++i) {
				add(new ServiceTreeNode(id, asldl.getServiceDecl(i)));
			}
		}
		
		public Transferable createTransferable() {
			return new ResourceTransferable<DNDResourceWrapper<String[]>>(new DNDResourceWrapper<String[]>(new String[] {identifier, String.valueOf(false)}));
		}

		@Override
		public String getLabel() {
			return identifier;
		}

		public boolean acceptsDrop(Resource res) {
			return (res instanceof PalcomService);
		}

		@Override
		public void acceptDrop(Resource res) {
			super.acceptDrop(res);
			if (!(res instanceof PalcomService)) {
				return;
			}
			PalcomService srv = (PalcomService)res;
			String serviceName = "Unknown"; 
			try {
				serviceName = srv != null ? srv.getName() : "Unknown";
			} catch (ResourceException e1) {
				e1.printStackTrace();
			}
			AltServiceDecl decl;
			try {
				decl = new AltServiceDecl(srv.requiresAuth(), new Identifier(serviceName), new DeviceUse(new Identifier(deviceIDlookup.get(srv.getDeviceID().toString()))), srv.getServiceID(), srv.getInstanceID().getInstanceNumber(), ++prio); //XXX: UID Here!
				data.addServiceDecl(decl);
				add(new ServiceTreeNode(decl.getLocalName().getID(), decl));
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						model.nodesWereInserted(ServiceAlternativeTreeNode.this, new int[] {ServiceAlternativeTreeNode.this.getChildCount() - 1});
					}
				});
			} catch (ResourceException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void showContextMenu(int x, int y) {
			deviceMenu.show(assemblyTree, x, y);
		}
		
	}
	
	private static int connectionCounter = 0;
	private class ConnectionListTreeNode extends AssemblyTreeNode<ConnectionDeclList> {
		public ConnectionListTreeNode(ConnectionDeclList connectionDeclList) {
			super("Connection List");
			data = connectionDeclList;
			
			for (int i = 0; i < data.getNumConnectionDecl(); ++i) {
				add(new ConnectionTreeNode(data.getConnectionDecl(i)));
				declaredConnections.add(data.getConnectionDecl(i).getId());
			}
		}

		@Override
		public String getLabel() {
			return "Connections";
		}

		@Override
		public boolean acceptsDrop(Resource res) {
			return (/*res instanceof PalcomService ||*/ res instanceof DNDResourceWrapper<?>);
		}

		@Override
		public void acceptDrop(Resource res) {
			super.acceptDrop(res);
			//String idd = "";
			//String uid = "";
			/*if (res instanceof PalcomService) {
				PalcomService srv = (PalcomService)res;
				String serviceName = "Unknown"; 
				try {
					serviceName = srv != null ? srv.getName() : "Unknown";
				} catch (ResourceException e1) {
					e1.printStackTrace();
				}
				idd = serviceName;
				if (srv.requiresAuth()) {
					uid = (String) JOptionPane.showInputDialog(new JTextField(), "UserID", "UserID", JOptionPane.QUESTION_MESSAGE, null, null, null);
				}
			} else*/
			//if (res instanceof DNDResourceWrapper<?>) {
				DNDResourceWrapper<String[]> srv = (DNDResourceWrapper<String[]>) res;
				final String idd = srv.getResource()[0];
				final boolean ruid = Boolean.parseBoolean(srv.getResource()[1]);
				//if (ruid != null && ruid.equals("true")) {
				//	uid = (String) JOptionPane.showInputDialog(new JTextField(), "UserID", "UserID", JOptionPane.QUESTION_MESSAGE, null, null, null);
				//}
			//}
				
				try {
					taskExecutor.execute(new Runnable() {
						public void run() {
							Identifier uid = null;
							if (ruid) {
								String[] ret = AddParamDialog.instance.getStrings();
								boolean runtime = false;
								if (ret[2] != null) {
									runtime = Boolean.parseBoolean(ret[2]);
								}
								
								if (runtime) {
									uid = new RuntimeIdentifier(ret[0], ret[1]);
								} else {
									uid = new Identifier(ret[0]);
								}
							}
							
							ConnectionDecl conn = new ConnectionDecl(new ServiceUse(new Identifier(idd)), new ThisService(), "conn-" + (++connectionCounter), (ruid ? new Opt(uid) : new Opt()));
							data.addConnectionDecl(conn);
							
							add(new ConnectionTreeNode(conn));
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									model.nodesWereInserted(ConnectionListTreeNode.this, new int[] {getChildCount() - 1});
								}
							});
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}
	
	private final ConnectionMenu connectionMenu = new ConnectionMenu(); 
	private class ConnectionMenu extends JPopupMenu {
		public ConnectionMenu() {
			JMenuItem setId = new JMenuItem("Set CID");
			setId.addActionListener(AssemblyPanel.this);
			setId.setActionCommand("SetCID");
			add(setId);
			
			JMenuItem setUId = new JMenuItem("Set UserID");
			setUId.addActionListener(AssemblyPanel.this);
			setUId.setActionCommand("SetUID");
			add(setUId);
			
			JMenuItem del = new JMenuItem("Delete");
			del.setActionCommand("Delete");
			del.addActionListener(AssemblyPanel.this);
			add(del);
		}
	}
	private class ConnectionTreeNode extends AssemblyTreeNode<ConnectionDecl> implements TransferableTreeNode {
		public ConnectionTreeNode(ConnectionDecl connection) {
			super("conn");
			this.data = connection;
		}

		@Override
		public String getLabel() {
			StringBuilder sb = new StringBuilder();
			if (data.getId() != null && !(data.getId().equals(""))) {
				sb.append(data.getId());
				sb.append(":");
			}
			
			sb.append(data.getCustomer().getIdentifier().getID());
			sb.append(" -> ");
			sb.append(data.getProvider().getIdentifier().getID());
			
			if (data.hasUserId() && !(data.getUserId().equals(""))) {
				sb.append(" (");
				sb.append(data.getUserId().getID());
				sb.append(")");
			}
			
			return sb.toString();
		}

		@Override
		public void showContextMenu(int x, int y) {
			connectionMenu.show(assemblyTree, x, y);
		}

		public Transferable createTransferable() {
			return new ResourceTransferable<StringWrapper>(new StringWrapper(data.getId()));
		}
		
		public void setCID(String cid) {
			data.setId(cid);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodeChanged(ConnectionTreeNode.this);
				}
			});
			AssemblyPanel.this.setUnsaved(true);
		}
		public void setUID(String uid, String defaultValue, boolean runtime) {
			if (runtime) {
				System.err.println("((( " + uid + ", " + defaultValue + " )))");
				data.setUserId(new RuntimeIdentifier(uid, defaultValue));
			} else {
				data.setUserId(new Identifier(uid));
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodeChanged(ConnectionTreeNode.this);
				}
			});
			AssemblyPanel.this.setUnsaved(true);
		}
	}
	
	private class ConnectionWrapper implements Resource {
		ConnectionTreeNode node;
		
		public ConnectionWrapper(ConnectionTreeNode node) {
			super();
			this.node = node;
		}
		public boolean isAvailable() {
			return false;
		}
		public void addListener(ResourceListener listener) {}
		public void removeListener(ResourceListener listener) {}
	}
	
	private class ScriptTreeNode extends AssemblyTreeNode<EventHandlerScript> {
		public ScriptTreeNode(EventHandlerScript eventHandlerScript) {
			super("Script");
			this.data = eventHandlerScript;
			add(new VariableListTreeNode(data.getVariables()));
			add(new EventHandlerListTreeNode(data.getEventHandlers()));
		}

		@Override
		public String getLabel() {
			return "Script";
		}
	}
	
	private class VariableListTreeNode extends AssemblyTreeNode<VariableList> {
		public VariableListTreeNode(VariableList vars) {
			super("Variables");
			data = vars;
			
			for (int i = 0; i < data.getNumVariableDecl(); ++i) {
				add(new VariableTreeNode(data.getVariableDecl(i)));
				String str = data.getVariableDecl(i).getIdentifier().getID();
				if (!(variableList.contains(str))) {
					variableList.add(str);
				}
			}
		}

		@Override
		public String getLabel() {
			return "Variables";
		}

		@Override
		public void showContextMenu(int x, int y) {
			variableListMenu.show(assemblyTree, x, y);
		}
		
		public void addVariable(String type, String name) {
			VariableDecl dec = new VariableDecl(new MimeType(type), new Identifier(name));
			data.addVariableDecl(dec);
			add(new VariableTreeNode(dec));
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodesWereInserted(VariableListTreeNode.this, new int[] {getChildCount() - 1});
				}
			});
			setUnsaved(true);
		}
	}
	
	public class VariableTreeNode extends AssemblyTreeNode<VariableDecl> {
		public VariableTreeNode(VariableDecl dec) {
			super(dec.getName());
			this.data = dec;
		}

		@Override
		public String getLabel() {
			return data.getIdentifier().getID() + " (" + ((MimeType)data.getVariableType()).getTypeName() + ")";
		}

		@Override
		public void showContextMenu(int x, int y) {
			deviceMenu.show(assemblyTree, x, y);
		}
	}
	
	private class ParamTreeNode extends DeletableTreeNode<ParamInfo> {

		public ParamTreeNode(ParamInfo pi) {
			super("Param");
			data = pi;
		}

		@Override
		public String getLabel() {
			return data.getID() + " (" + data.getType() + ")";
		}
		
	}
	private class CommandTreeNode extends AssemblyTreeNode<CommandInfo> implements TransferableTreeNode {
		public CommandTreeNode(CommandInfo info) {
			super("");
			this.data = info;
			
			for (int i = 0; i < info.getNumParamInfo(); ++i) {
				add(new ParamTreeNode(info.getParamInfo(i)));
			}
		}

		public Transferable createTransferable() {
			TreeNode n = this;
			
			do {
				n = n.getParent();
				if (n == null) {
					throw new RuntimeException("Failed to find Service in tree.");
				}
			} while (!(n instanceof SynthServiceNode));
			return new ResourceTransferable<CommandWrapper>(new SynthServiceCommandWrapper(data, ((SynthServiceNode)n).data));
		}

		@Override
		public String getLabel() {
			return data.getID() + " (" + data.getDirection() + ")";
		}

		@Override
		public void showContextMenu(int x, int y) {
			commandMenu.show(assemblyTree, x, y);
		}

		public void addParam(String name, String type) {
			ParamInfo inf = new ParamInfo(name, type); 
			data.addParamInfo(inf);
			add(new ParamTreeNode(new ParamInfo(name ,type)));
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodesWereInserted(CommandTreeNode.this, new int[] {getChildCount() - 1});
				}
			});
			setUnsaved(true);
		}
	}
	
	private final CommandMenu commandMenu = new CommandMenu();
	private class CommandMenu extends DeletableDeviceMenu {
		public CommandMenu() {
			JMenuItem addParam = new JMenuItem("Add Parameter");
			addParam.addActionListener(AssemblyPanel.this);
			addParam.setActionCommand("AddParam");
			add(addParam);
		}
	}
	
	private class GroupTreeNode extends DeletableTreeNode<GroupInfo> {
		public GroupTreeNode(GroupInfo groupInfo) {
			super("");
			this.data = groupInfo;
			
			for (int i = 0; i < groupInfo.getNumControlInfo(); ++i) {
				ControlInfo ci = groupInfo.getControlInfo(i);
				if (ci instanceof GroupInfo) {
					add(new GroupTreeNode((GroupInfo) ci)); 
				} else {
					add(new CommandTreeNode((CommandInfo) ci)); 
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
			add(new CommandTreeNode(ci)); 
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodesWereInserted(GroupTreeNode.this, new int[] {getChildCount() - 1});
				}
			});
		}

		public void addGroup(String name, String help) {
			GroupInfo gi = new GroupInfo(name, help);
			data.addControlInfo(gi);
			add(new GroupTreeNode(gi)); 
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodesWereInserted(GroupTreeNode.this, new int[] {getChildCount() - 1});
				}
			});
			setUnsaved(true);
		}

		@Override
		public String getLabel() {
			return data.getID() + "(" + data.getHelp() + ")";
		}
		
		@Override
		public void showContextMenu(int x, int y) {
			serviceDescrptionMenu.show(assemblyTree, x, y);
		}
	}
	
	private class EventHandlerListTreeNode extends AssemblyTreeNode<EventHandlerList> {
		public EventHandlerListTreeNode(EventHandlerList eventHandlerList) {
			super("EventHandlers");
			data = eventHandlerList;
			
			for (int i = 0; i < data.getNumEventHandlerClause(); ++i) {
				add(new EventHandlerClauseTreenode(data.getEventHandlerClause(i)));
			}
		}

		@Override
		public String getLabel() {
			return "EventHandlers";
		}

		@Override
		public boolean acceptsDrop(Resource res) {
			return (res instanceof CommandWrapper || (res instanceof StringWrapper) && ((StringWrapper)res).string != null && !(((StringWrapper)res).string.equals("")));
		}

		@Override
		public void acceptDrop(final Resource res) {
			super.acceptDrop(res);
			if (res instanceof CommandWrapper) {
				CommandWrapper cw = (CommandWrapper)res;
				EventHandlerClause ehc = null;
				
				if (cw instanceof ServiceCommandWrapper) {
					ServiceCommandWrapper scw = (ServiceCommandWrapper)cw;
					ServiceID sid;
					try {
						sid = scw.service.getServiceID();
					} catch (ResourceException e) {
						e.printStackTrace();
						return;
					}
					if (!serviceIDlookup.containsKey(sid)) {
						throw new RuntimeException("Identifier for service " + sid + " doesn't exist");
					}
					ServiceUse su = new ServiceUse(new Identifier(serviceIDlookup.get(sid)));
					CommandEvent ce = new CommandEvent(cw.command.getID(), su, new Opt(cw.command));
					ehc = new EventHandlerClause(ce, new List());
				} else if (cw instanceof SynthServiceCommandWrapper) {
					SynthServiceCommandWrapper sscw = (SynthServiceCommandWrapper)cw;
					CommandEvent ce = new CommandEvent(sscw.command.getID(), new SynthesizedServiceUse(new Identifier(sscw.getId())), new Opt(sscw.command));
					ehc = new EventHandlerClause(ce, new List());
				} else {
					throw new RuntimeException("Like, no way dude.");
				}
				addClause(ehc);
			} else if (res instanceof StringWrapper) {
				try {
					taskExecutor.execute(new Runnable() {
						public void run() {
							ArrayList<String> lst = new ArrayList<String>(2);
							lst.add("Opened");
							lst.add("Closed");
							
							String type = selectStringDialog.selectString(lst, "Select type");
							EventHandlerClause ehc = new EventHandlerClause(new ConnectionEvent(type, ((StringWrapper)res).string), new List());
							
							addClause(ehc);
						}
					});
				} catch (InterruptedException e) {
					Logger.log("An InterruptedException was thrown in AssemblyPanel.EventHandlerListTreeNode.acceptDrop", Logger.CMP_COMMUNICATION, Logger.LEVEL_ERROR);
					e.printStackTrace();
				}
			} else {
				throw new RuntimeException("Wrong type of node dropped. " + res.getClass().getName() );
			}
			
		}

		void addClause(EventHandlerClause ehc) {
			data.addEventHandlerClause(ehc);
			
			add(new EventHandlerClauseTreenode(ehc));
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodesWereInserted(EventHandlerListTreeNode.this, new int[] {getChildCount() - 1});
				}
			});
		}

		@Override
		public void showContextMenu(int x, int y) {
			eventHandlerListMenu.show(assemblyTree, x, y);
		}
	}
	
	private class ServiceDescriptionTreeNode extends AssemblyTreeNode<PRDServiceFMDescription> {
		public ServiceDescriptionTreeNode(PRDServiceFMDescription description) {
			super("ServiceDescription");
			this.data = description;
			
			for (int i = 0; i < data.getNumControlInfo(); ++i) {
				ControlInfo ci = data.getControlInfo(i);
				if (ci instanceof CommandInfo) {
					add(new CommandTreeNode((CommandInfo) ci)); 
				} else if (ci instanceof GroupInfo) {
					add(new GroupTreeNode((GroupInfo) ci)); 
				}
			}
		}

		@Override
		public String getLabel() {
			return "ServiceDescription";
		}

		@Override
		public void showContextMenu(int x, int y) {
			serviceDescrptionMenu.show(assemblyTree, x, y);
		}
		
		void addCommand(String name, String direction) {
			CommandInfo ci = new CommandInfo(name, direction);
			ci.setCommandNumber(data.findHighestCommandNumber() + 1);
			data.addControlInfo(ci);
			add(new CommandTreeNode(ci)); 
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodesWereInserted(ServiceDescriptionTreeNode.this, new int[] {getChildCount() - 1});
				}
			});
			setUnsaved(true);
		}

		public void addGroup(String name, String help) {
			GroupInfo gi = new GroupInfo(name, help);
			data.addControlInfo(gi);
			add(new GroupTreeNode(gi)); 
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodesWereInserted(ServiceDescriptionTreeNode.this, new int[] {getChildCount() - 1});
				}
			});
			setUnsaved(true);
		}
	}
	
	private class InvokeTreeNode extends DeletableTreeNode<InvokeAction> {
		public InvokeTreeNode(InvokeAction ia) {
			super("Invoke");
			data = ia;
		}

		@Override
		public String getLabel() {
			StringBuilder sb = new StringBuilder(); //TODO: This could be moved to a new base-class, but since it's only used in one other place, I don't care -- for now...
			for (int j = 0; j < data.getNumParamValue(); ++j) {
				sb.append(data.getParamValue(j).getName());
				if ((j + 1) < data.getNumParamValue()) {
					sb.append(", ");
				}
			}
			StringBuilder ret = new StringBuilder();
			ret.append("Invoke ");
			ret.append(data.getCommand());
			ret.append("(");
			ret.append(sb.toString());
			ret.append(") on ");
			ret.append(data.getSynthesizedServiceUse().getIdentifier().getID());
			ret.append(" ");
			ret.append(data.getAddressingType());
			ret.append((data.hasAddressUse() ? " \"" + data.getAddressUse().getName() + "\"": ""));
			
			return  ret.toString();
		}
	}
	
	private class SendMessageTreeNode extends DeletableTreeNode<SendMessageAction> {
		public SendMessageTreeNode(SendMessageAction sma) {
			super("Message");
			data = sma;
		}

		@Override
		public String getLabel() {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < data.getNumParamValue(); ++j) {
				sb.append(data.getParamValue(j).getName());
				if ((j + 1) < data.getNumParamValue()) {
					sb.append(", ");
				}
			}
			return "Send " + data.getCommand() + "(" + sb.toString() + ") to " + data.getServiceExp().getIdentifier().getID();
		}
		
	}
	
	private class EventHandlerClauseTreenode extends AssemblyTreeNode<EventHandlerClause> {
		public EventHandlerClauseTreenode(EventHandlerClause clause) {
			super("Clause");
			this.data = clause;
			
			for (int i = 0; i < clause.getNumAction(); ++i) {
				Action act = clause.getAction(i);
				
				if (act instanceof SendMessageAction) {
					SendMessageAction sma = (SendMessageAction)act;
					add(new SendMessageTreeNode(sma));
				} else if (act instanceof InvokeAction) {
					InvokeAction ia = (InvokeAction)act;
					add(new InvokeTreeNode(ia));
				} else if (act instanceof AssignAction) {
					AssignAction aa = (AssignAction)act;
					add(new DeletableTreeNode(aa.getVariableUse().getName() + " = " + aa.getParamUse().getName()));
				}
			}
		}

		@Override
		public String getLabel() {
			if (data.getEvent() instanceof CommandEvent) {
				CommandEvent ce = (CommandEvent) data.getEvent();
				return "When " + ce.getCommandName() + " from " + ce.getServiceExp().getIdentifier().getID();
			} else if (data.getEvent() instanceof ConnectionEvent) {
				ConnectionEvent ce = (ConnectionEvent) data.getEvent();

				return "When connection " +  ce.getcid() + " " + ce.getType();
			}
			return "Ehhhh, no.";
		}

		@Override
		public boolean acceptsDrop(Resource res) {
			return (res instanceof CommandWrapper);
		}

		@Override
		public void acceptDrop(Resource res) {
			super.acceptDrop(res);
			final CommandWrapper cw = (CommandWrapper)res;
			
			final LinkedList<String> lst = new LinkedList<String>();
			final List uselist = new List();
			if (data.getEvent() instanceof CommandEvent) {
				CommandEvent ce = (CommandEvent) data.getEvent();
				for (int i = 0; i < ce.getCommandInfo().getNumParamInfo(); ++i ) {
					lst.add(ce.getCommandInfo().getParamInfo(i).getID());
				}
			}
			final StringBuilder sb = new StringBuilder();
			try {
				taskExecutor.execute(new Runnable() {
					public void run() {
						boolean isInvoke = false;
						boolean isUnicast = false;
						if (cw instanceof SynthServiceCommandWrapper) {
							isInvoke = true;
							SynthesizedService sserv = ((SynthServiceCommandWrapper) cw).getService();
							if (sserv.getDistribution() == PRDService.UNICAST) {
								isUnicast = true;
							}
						} else if (!(cw instanceof ServiceCommandWrapper)) {
							throw new RuntimeException("Unknown datatype " + cw + " dropped.");
						}
						Event ce = data.getEvent();
						CommandInfo cmd = null;
						if (ce instanceof CommandEvent) {
							cmd = ((CommandEvent)ce).getCommandInfo();
						}
						
						BetterAddInvokeDialog baid = new BetterAddInvokeDialog(cw.command, cmd, variableList, isInvoke && isUnicast);
						if (baid.showDialog(AssemblyPanel.this)) {
							return;
						}
						Iterator it = baid.getUseMap().values().iterator();
						for (int i = 0; it.hasNext(); ++i ) {
							Use u = (Use) it.next();
							if (u == null) {
								return;
							}
	
							uselist.add(u);
							sb.append(u.getName());
							if ((i + 1) < cw.command.getNumParamInfo()) {
								sb.append(", ");
							}
						}
						
						Action act = null;
						if (!isInvoke) {
							ServiceCommandWrapper scw = (ServiceCommandWrapper)cw;
							ServiceID sid;
							try {
								sid = scw.service.getServiceID();
							} catch (ResourceException e) {
								e.printStackTrace();
								return;
							}
							if (!serviceIDlookup.containsKey(sid)) {
								JOptionPane.showMessageDialog(AssemblyPanel.this, "Failed to Add action", "Identifier for service " + sid + " doesn't exist", JOptionPane.OK_OPTION);
								throw new RuntimeException("Identifier for service " + sid + " doesn't exist");
							}
							String idd = serviceIDlookup.get(sid);
							act = new SendMessageAction(cw.command.getID(), uselist, new ServiceUse(new Identifier(idd)));
							add(new DeletableTreeNode("Send " + cw.command.getID() + "(" + sb.toString() + ") to " + idd));
						} else {
							SynthServiceCommandWrapper sscw = (SynthServiceCommandWrapper)cw;

							//This is hackish. Replace with AddressFromConstantUse
							Opt addr = baid.getAddressing();
							if (addr.getNumChild() > 0 && addr.getChild(0) instanceof ConstantUse) {
								ConstantUse cu = (ConstantUse) addr.getChild(0);
								addr.removeAll();
								addr.addChild(new AddressUse(cu.getName()));
							}
							act = new InvokeAction(cw.command.getID(), uselist, new SynthesizedServiceUse(new Identifier(sscw.getId())), isUnicast ? baid.getAddressingType() : "ToAll", new Opt(), baid.getAddressing());
							add(new InvokeTreeNode((InvokeAction) act));
						}
						
						data.addAction(act);
						
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								model.nodesWereInserted(EventHandlerClauseTreenode.this, new int[] {getChildCount() - 1});
							}
						});
					}
				});
			} catch (InterruptedException e) {
				Logger.log("An InterruptedException was thrown in AssemblyPanel.EventHandlerClauseTreenode.acceptDrop", Logger.CMP_COMMUNICATION, Logger.LEVEL_ERROR);
				e.printStackTrace();
			}
			
		}

		@Override
		public void showContextMenu(int x, int y) {
			eventHandlerMenu.show(assemblyTree, x, y);
		}
		
		CommandInfo getCommand() {
			if (data.getEvent() instanceof CommandEvent) {
				return ((CommandEvent)data.getEvent()).getCommandInfo();
			}
			return null;
		}
		
		void addAssign(String variable, String param) {
			data.addAction(new AssignAction(new VariableUse(variable), new ParamUse(param)));
			
			add(new DeletableTreeNode(variable + " = " + param));
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.nodesWereInserted(EventHandlerClauseTreenode.this, new int[] {getChildCount() - 1});
				}
			});
		}
		
	}

	private class AssemblyCellRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 3104188226987130137L;

		public Component getTreeCellRendererComponent(JTree arg0, Object value,
				boolean arg2, boolean arg3, boolean arg4, int arg5, boolean arg6) {
			
			if (value instanceof AssemblyTreeNode) {
				AssemblyTreeNode ltn = (AssemblyTreeNode)value;
				
				super.getTreeCellRendererComponent(arg0, ltn.getLabel(), arg2, arg3, arg4, arg5, arg6);
				setToolTipText(ltn.getTooltip());
				
				return this;
			}
			return super.getTreeCellRendererComponent(arg0, value, arg2, arg3, arg4, arg5, arg6);
		}
    	
    }

	public void stateChanged(ChangeEvent ce) {
		JTabbedPane tabs = (JTabbedPane) ce.getSource();
		String tabName = tabs.getTitleAt(tabs.getSelectedIndex());
		
		if(prevTab.equals(TAB_NAME_XML)){
			assemblyData = xmlText.getText();
		} else if(prevTab.equals(TAB_NAME_EDITOR)){
			assemblyData = assemblyRoot.writeXML();
		} else if(prevTab.equals(TAB_NAME_GRAPH)){
			assemblyData = assemblyGraph.getXML();
		}
		
		xmlText.setText(assemblyData);
		try {
			loadAssemblyTree(assemblyData.getBytes("UTF8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assemblyGraph.setGraph(assemblyData);
		prevTab = tabName;
	}
	
	private class DeletableDeviceMenu extends JPopupMenu  {
		public DeletableDeviceMenu() {
			JMenuItem del = new JMenuItem("Delete");
			del.setActionCommand("Delete");
			del.addActionListener(AssemblyPanel.this);
			add(del);
		}
		
		public void show(Component comp, int x, int y) {
			super.show(comp, x, y);
		}
	}
	
	private final DeviceMenu deviceMenu = new DeviceMenu();
	private final class DeviceMenu extends DeletableDeviceMenu  {
		public DeviceMenu() {
			JMenuItem makeSelf = new JMenuItem("Make Self");
			makeSelf.setActionCommand("self");
			makeSelf.addActionListener(AssemblyPanel.this);
			add(makeSelf);
		}
	}
	
	private final ServiceMenu serviceMenu = new ServiceMenu();
	private class ServiceMenu extends DeletableDeviceMenu {
		public ServiceMenu() {
			super();
			JMenuItem prio = new JMenuItem("Change priority");
			prio.setActionCommand("Prio");
			prio.addActionListener(AssemblyPanel.this);
			add(prio);
		}
	}
	
	private final ServiceListMenu serviceListMenu = new ServiceListMenu();
	private class ServiceListMenu extends JPopupMenu {
		public ServiceListMenu() {
			JMenuItem del = new JMenuItem("Add service alternative");
			del.setActionCommand("AddAlt");
			del.addActionListener(AssemblyPanel.this);
			add(del);
		}
		
		public void show(Component comp, int x, int y) {
			super.show(comp, x, y);
		}
	}
	
	private final VariablesListMenu variableListMenu = new VariablesListMenu();
	private class VariablesListMenu extends JPopupMenu {
		public VariablesListMenu() {
			JMenuItem addvar = new JMenuItem("Add variable");
			addvar.setActionCommand("AddVar");
			addvar.addActionListener(AssemblyPanel.this);
			add(addvar);
		}
	}
	
	private final EventHandlerMenu eventHandlerMenu = new EventHandlerMenu();
	private class EventHandlerMenu extends DeletableDeviceMenu {
		public EventHandlerMenu() {
			super();
			JMenuItem addAssign = new JMenuItem("Add assign item");
			addAssign.setActionCommand("AddAssign");
			addAssign.addActionListener(AssemblyPanel.this);
			add(addAssign);
			
			JMenuItem addInvoke = new JMenuItem("Add Invoke item");
			add(addInvoke);                      
		}
	}
	
	private final EventHandlerListMenu eventHandlerListMenu = new EventHandlerListMenu();
	private class EventHandlerListMenu extends DeletableDeviceMenu {
		public EventHandlerListMenu() {
			super();
			//JMenuItem addConnectionHandler = new JMenuItem("Add Connection Handler");
			//addConnectionHandler.setActionCommand("AddConnectionHandler");
			//addConnectionHandler.addActionListener(AssemblyPanel.this);
			//add(addConnectionHandler);
		}
	}
	
	private final SynthServiceListMenu synthServiceListMenu = new SynthServiceListMenu();
	private class SynthServiceListMenu extends JPopupMenu {
		public SynthServiceListMenu() {
			JMenuItem addSynth = new JMenuItem("Add synthesised service.");
			addSynth.addActionListener(AssemblyPanel.this);
			addSynth.setActionCommand("AddSynth");
			add(addSynth);
		}
	}
	
	private final ServiceDescriptionMenu serviceDescrptionMenu = new ServiceDescriptionMenu(); 
	private class ServiceDescriptionMenu extends JPopupMenu {
		public ServiceDescriptionMenu() {
			JMenuItem addCommand = new JMenuItem("Add Command");
			addCommand.addActionListener(AssemblyPanel.this);
			addCommand.setActionCommand("AddCommand");
			add(addCommand);
			
			JMenuItem addGroup = new JMenuItem("Add Group");
			addGroup.addActionListener(AssemblyPanel.this);
			addGroup.setActionCommand("AddGroup");
			add(addGroup);
			
			JMenuItem del = new JMenuItem("Delete");
			del.setActionCommand("Delete");
			del.addActionListener(AssemblyPanel.this);
			add(del);
		}
	}
	
	private final SelectStringDialog selectStringDialog = new SelectStringDialog();
	private class SelectStringDialog extends JDialog implements ActionListener {
		private JList variableList;
		private boolean canceled = false;
		public SelectStringDialog() {
			setSize(200,200);
			setLocationRelativeTo(this);
			
			GroupLayout layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			variableList = new JList(new DefaultListModel());
			JScrollPane sp = new JScrollPane(variableList);
			
			JButton btnOk = new JButton("Ok");
			btnOk.setActionCommand("DialogOK");
			btnOk.addActionListener(this);
			JButton btnCancel = new JButton("Cancel");
			btnCancel.setActionCommand("DialogCancel");
			btnCancel.addActionListener(this);
			
			layout.setHorizontalGroup(
	            layout.createParallelGroup()
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(sp, GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
	                .addContainerGap()
	            ).addGroup(layout.createSequentialGroup()
	            	.addComponent(btnOk)
	            	.addComponent(btnCancel)
	            )
	        );
	        layout.setVerticalGroup(
	            layout.createSequentialGroup()
	            .addComponent(sp, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
	            .addGroup(layout.createParallelGroup()
	            	.addComponent(btnOk)
	            	.addComponent(btnCancel)
	            )
	        );
		}
		
		public synchronized String selectString(java.util.List<String> strings, String title) {
			canceled = false;
			this.setTitle(title);
			DefaultListModel mod = (DefaultListModel) variableList.getModel();
			mod.removeAllElements();
			for (String val : strings) {
				mod.addElement(val);
			}
			setVisible(true);
			pack();
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			setVisible(false);
			return (String) variableList.getSelectedValue();
		}

		public synchronized void actionPerformed(ActionEvent ae) {
			if (ae.getActionCommand().equals("DialogCancel")) {
				canceled = true;
			}
			notifyAll();
		}
		
		public boolean wasCanceled() {
			return canceled;
		}
	}
	
	private final AddVariableDialog addVariableDialog = new AddVariableDialog();
	private class AddVariableDialog extends JDialog {
		private JTextField nameTextField;
		private JTextField typeTextField;

		public AddVariableDialog() {
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
			btn1.addActionListener(AssemblyPanel.this);
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
			TreePath selectionPath = assemblyTree.getPathForLocation(me.getX(), me.getY());
			if (selectionPath == null) {
				return;
			}
			assemblyTree.setSelectionPath(selectionPath);
			TreeNode node = (TreeNode) selectionPath.getLastPathComponent();
			if (node instanceof AssemblyTreeNode) {
				((AssemblyTreeNode)node).showContextMenu(me.getX(), me.getY());
			}
		}
	}
	
	public void actionPerformed(ActionEvent ae) {
		AssemblyTreeNode atn = (AssemblyTreeNode) assemblyTree.getSelectionPath().getLastPathComponent();
		final TreeNode parent = atn.getParent();
		try {
		if (ae.getActionCommand().equals("Delete")) {
			atn.removeFromParent();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.reload(parent);
				}
			});
		} else if (ae.getActionCommand().equals("AddAlt") && atn instanceof ServiceListTreeNode) {
			String sid = (String) JOptionPane.showInputDialog(new JTextField(), "Add service alternative", "Add service alternative", JOptionPane.QUESTION_MESSAGE, null, null, null);
			((ServiceListTreeNode)atn).addAlternative(sid);
		} else if (ae.getActionCommand().equals("Prio") && atn instanceof ServiceTreeNode && ((ServiceTreeNode)atn).isAlternative()) {
			String sprio = (String) JOptionPane.showInputDialog(new JTextField(), "Add service alternative", "Add service alternative", JOptionPane.QUESTION_MESSAGE, null, null, null);
			int prio = Integer.parseInt(sprio);
			((ServiceTreeNode)atn).setPriority(prio);
		} else if (ae.getActionCommand().equals("AddVar") && atn instanceof VariableListTreeNode) {
			addVariableDialog.showDialog();
		} else if (ae.getActionCommand().equals("AddVarOk") && atn instanceof VariableListTreeNode) {
			addVariableDialog.setVisible(false);
			((VariableListTreeNode)atn).addVariable(addVariableDialog.getAssemblyType(), addVariableDialog.getName());
			variableList.add(addVariableDialog.getName());
		} else if (ae.getActionCommand().equals("AddAssign")) {
			final EventHandlerClauseTreenode tn = (EventHandlerClauseTreenode) assemblyTree.getSelectionPath().getLastPathComponent();
			CommandInfo ci = tn.getCommand();
			final java.util.List<String> params = new LinkedList<String>();
			params.add("Constant");
			for (int i = 0; i < ci.getNumParamInfo(); ++i) {
				params.add(ci.getParamInfo(i).getID());
			}
			
			
			taskExecutor.execute(new Runnable() {
				String varname;
				String paramname;
				public void run() {
					varname = selectStringDialog.selectString(variableList, "Choose Variable");
					if (selectStringDialog.wasCanceled()) {
						return;
					}
					paramname = selectStringDialog.selectString(params, "Choose Parameter");
					if (selectStringDialog.wasCanceled()) {
						return;
					}
					
					if (paramname.equals("Constant")) {
						paramname = JOptionPane.showInputDialog("Enter constant value");
					}
					if (paramname == null) {
						return;
					}
					
					tn.addAssign(varname, paramname);
				}
			});
			
		} else if (ae.getActionCommand().equals("AddSynth")) {
			final SynthServiceListNode node = (SynthServiceListNode) assemblyTree.getSelectionPath().getLastPathComponent();

			AddSynthServiceDialog assd = new AddSynthServiceDialog(node);
		} else if (ae.getActionCommand().equals("AddCommand")) {
			final TreeNode node = (TreeNode) assemblyTree.getSelectionPath().getLastPathComponent();
			taskExecutor.execute(new Runnable() {
				public void run() {
					//String[] vals = twoStringsDialog.getStrings("Name", "Direction", "", "in");
					String[] vals = AddCommandDialog.getStrings(AssemblyPanel.this);
					if (node instanceof ServiceDescriptionTreeNode) {
						((ServiceDescriptionTreeNode)node).addCommand(vals[0], vals[1]);
					} else if (node instanceof GroupTreeNode) {
						((GroupTreeNode)node).addCommand(vals[0], vals[1]);
					}
				}
			});
		} else if (ae.getActionCommand().equals("AddGroup")) {
			final TreeNode node = (TreeNode) assemblyTree.getSelectionPath().getLastPathComponent();
			taskExecutor.execute(new Runnable() {
				public void run() {
					String[] vals = twoStringsDialog.getStrings("Name", "Help text", "", "");
					if (node instanceof ServiceDescriptionTreeNode) {
						((ServiceDescriptionTreeNode)node).addGroup(vals[0], vals[1]);
					} else if (node instanceof GroupTreeNode) {
						((GroupTreeNode)node).addGroup(vals[0], vals[1]);
					}
				}
			});
		} else if (ae.getActionCommand().equals("AddParam")) {
			final CommandTreeNode node = (CommandTreeNode) assemblyTree.getSelectionPath().getLastPathComponent();
			
			taskExecutor.execute(new Runnable() {
				public void run() {
					String[] vals = twoStringsDialog.getStrings("Name", "Type", "", "");
					node.addParam(vals[0], vals[1]);
				}
			});
		} else if (ae.getActionCommand().equals("AddConnectionHandler")) {
			/*final EventHandlerListTreeNode node = (EventHandlerListTreeNode) assemblyTree.getSelectionPath().getLastPathComponent();
			new Thread() {
				public void run() {
					LinkedList<String> types = new LinkedList<String>();
					types.add("opened");
					types.add("closed");
					
					String type = selectStringDialog.selectString(types, "Type");
					
					//node.addClause(new EventHandlerClause(new ConnectionEvent(type), new List()));
				}
			}.start();
			*/
			throw new RuntimeException("This isn't implemented. Should it be?");
		} else if (ae.getActionCommand().equals("SetCID")) {
			final ConnectionTreeNode node = (ConnectionTreeNode) assemblyTree.getSelectionPath().getLastPathComponent();
			taskExecutor.execute(new Runnable() {
				public void run() {
					String cid = JOptionPane.showInputDialog("Enter CID");
					node.setCID(cid);
				}
			});
		} else if (ae.getActionCommand().equals("SetUID")) {
			final ConnectionTreeNode node = (ConnectionTreeNode) assemblyTree.getSelectionPath().getLastPathComponent();
			taskExecutor.execute(new Runnable() {
				public void run() {
					//String uid = JOptionPane.showInputDialog("Enter UID");
					String[] ret = AddParamDialog.instance.getStrings();
					boolean runtime = false;
					if (ret[2] != null) {
						runtime = Boolean.parseBoolean(ret[2]);
					}
					node.setUID(ret[0], ret[1], runtime);
				}
			});
		} else if (ae.getActionCommand().equals("self")) {
			final DeviceTreeNode node = (DeviceTreeNode) assemblyTree.getSelectionPath().getLastPathComponent();
			node.makeSelfDevice();
		}
		} catch (InterruptedException e) {
			Logger.log("An InterruptedException was thrown in AssemblyPanel.actionPerformed for actionCommand " + ae.getActionCommand(), Logger.CMP_COMMUNICATION, Logger.LEVEL_ERROR);
			e.printStackTrace();
		}
	}
	public boolean isUnsaved() {
		return unsaved;
	}
	public void setUnsaved(boolean unsaved) {
		this.unsaved = unsaved;
		notifySaveStatusListeners();
	}
	
	private LinkedList<SaveStatusListener> statusListeners = new LinkedList<SaveStatusListener>();
	public void addSaveStatusListener(SaveStatusListener ssl) {
		statusListeners.add(ssl);
	}
	public void removeSaveStatusListener(SaveStatusListener ssl) {
		statusListeners.remove(ssl);
	}
	private void notifySaveStatusListeners() {
		for (SaveStatusListener ssl : statusListeners) {
			ssl.saveStatusChanged(!unsaved, this);
		}
	}
	
	public byte[] getByteData() {
		try {
			String tabName = tabs.getTitleAt(tabs.getSelectedIndex());
			if (tabName.equals(TAB_NAME_XML)) {
				loadAssemblyTree(xmlText.getText().getBytes("UTF8"));
			}
			return assemblyRoot.writeXML().getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void reloadAssembly() {
		application.reloadAssembly(filename);
		setUnsaved(false);
	}
	public void saveAssembly() {
		application.writeAssembly(filename, getByteData());
		setUnsaved(false);
	}
	public void keyPressed(KeyEvent arg0) {
		setUnsaved(true);
	}
	public void keyReleased(KeyEvent arg0) {}
	public void keyTyped(KeyEvent arg0) {}
	
	public int getNextCommandNumber() {
		return 0;
	}
	
	private class StringWrapper implements Resource {
		public String string;
		public StringWrapper(String string) {
			super();
			this.string = string;
		}

		public boolean isAvailable() {
			return false;
		}

		public void addListener(ResourceListener listener) {}
		public void removeListener(ResourceListener listener) {}
	}
}
