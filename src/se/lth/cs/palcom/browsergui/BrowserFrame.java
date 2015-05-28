package se.lth.cs.palcom.browsergui;

import ist.palcom.resource.descriptor.PRDAssemblyD;
import ist.palcom.resource.descriptor.PRDAssemblyVer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import se.lth.cs.palcom.assembly.resource.ResourceProxy;
import se.lth.cs.palcom.assembly.resource.UnicastResourceProxy;
import se.lth.cs.palcom.browsergui.views.DynamicServiceView;
import se.lth.cs.palcom.browsergui.views.OctetServiceView;
import se.lth.cs.palcom.common.NoSuchDeviceException;
import se.lth.cs.palcom.communication.DefaultCommunicationManager;
import se.lth.cs.palcom.communication.connection.DynamicConnection;
import se.lth.cs.palcom.discovery.ResourceException;
import se.lth.cs.palcom.discovery.proxy.PalcomService;
import se.lth.cs.palcom.discovery.proxy.Resource;
import se.lth.cs.palcom.util.gui.network.MediaControlPanelPlus;
import se.lth.cs.palcom.util.gui.network.NetworkConfigurationPanel;
import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.event.CDockableAdapter;
import bibliothek.gui.dock.common.intern.CDockable;

public class BrowserFrame extends JFrame implements LogListener, BrowserSelectionListener, ActionListener, SaveStatusListener {
	private static final long serialVersionUID = -5569737609648988551L;

	private BrowserApplication application;
	private JTextArea eventLog;
	//private JTabbedPane servicesBrowsers;
	private EnvironmentDockable leftDock;
	private CDockable mainDock;
	private EnvironmentDockable bottomDock;
	private CControl control;
	
	private int panelcounter = 1;
	private int assemblycounter = 1;

	private AssemblyTransferHandler ath;
	private DefaultSingleCDockable configDock;
	
	private AssemblyDirectory assemblies;

	private final Favorites favorites;

	public BrowserFrame(BrowserApplication application, AssemblyDirectory assemblies) {
		this.application = application;
		this.assemblies = assemblies;
		favorites = new Favorites(application.getDevice());
		
		initComponents();
		application.addLogListener(this);
	}
	
	private void initComponents() {
    	setLayout(new BorderLayout());
        
        ath = new AssemblyTransferHandler();
        RemovableTreeBrowser btp = new RemovableTreeBrowser(application, ath, favorites);
        FavoriteBrowser ftp = new FavoriteBrowser(application, ath, favorites);
        XMLViewPanel xmlp = new XMLViewPanel();
        btp.addSelectionListener(xmlp);
        btp.addSelectionListener(this);
        
        eventLog = new JTextArea();
        eventLog.setColumns(20);
        eventLog.setRows(5);
        
        control = new CControl(this);
        control.setTheme(new EclipseTheme());
        
        add( control.getContentArea() );
        
        JPanel lpan = new JPanel(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        cons.weightx = 1;
        cons.anchor = GridBagConstraints.NORTHWEST;
        cons.gridx = 0;
        cons.gridy = 0;
        lpan.add(btp, cons);
        cons.gridy += 1;
        cons.weighty = 1;
        lpan.add(ftp, cons);
        lpan.setBackground(Color.WHITE);
        
        leftDock = new EnvironmentDockable("browser", "Browser", new JScrollPane(lpan));
        mainDock = new DefaultSingleCDockable("id2");
        bottomDock = new EnvironmentDockable("id3", "Log", xmlp);
        
        
        EnvironmentDockable projectDock = new EnvironmentDockable("projectDock", "Project", new ProjectPanel(application, this, assemblies));
        
        CGrid grid = new CGrid( control );
        grid.add( 0, 0, 1, 2, leftDock );
        grid.add( 1, 3, 3, 1, bottomDock );
        grid.add( 1, 0, 3, 3, mainDock );
        grid.add( 0, 2, 1, 1, projectDock );
        
        control.getContentArea().deploy( grid );
        
        DefaultSingleCDockable xmlDock = new DefaultSingleCDockable("xmlp", "XML", xmlp);
        control.addDockable(xmlDock);
        xmlDock.setLocation(bottomDock.getBaseLocation().aside());
        xmlDock.setVisible(true);
        
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Palcom Browser");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        
        if (application.getDevice().usesWP4()) {
        	configDock = new DefaultSingleCDockable("configDock", "Configuration", new MediaControlPanelPlus(application.getDevice()));
//        	configDock = new DefaultSingleCDockable("configDock", "Configuration", application.getDevice().getCommunicationManager().getMediaControlPanel());
//        	System.err.println("This is were it is currently added!");
//        	((DefaultCommunicationManager)(application.getDevice().getCommunicationManager())).addWP4IPv4MAO();
        } else { 
        	configDock = new DefaultSingleCDockable("configDock", "Configuration", new NetworkConfigurationPanel(application.getDevice(), ((MiniBrowserDevice)application.getDevice()).getNetwork()));
        }
        control.addDockable(configDock);
    }
	
	private void openConfiguration() {
		configDock.setLocation(CLocation.external(100, 100, 700, 500));
		configDock.setCloseable(true);
		configDock.setMinimizable(false);
        configDock.setVisible(true);
	}

	public void openAssembly(String name) {
		PRDAssemblyD av = application.loadAssembly(name);
		System.out.println("Assemblypanel created:" + name);
		AssemblyPanel ap = new AssemblyPanel(name, ath, application, av);
		ap.addSaveStatusListener(this);
		AssemblyDockable dock = new AssemblyDockable(application, "assemblyDock" + (++assemblycounter), name, ap);
		control.addDockable(dock);
		dock.setLocation(mainDock.getBaseLocation().aside());
		dock.setVisible(true);
		dock.setCloseable(true);
	}
	
    //TODO: Get rid of this generated crap
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        quit();
    }//GEN-LAST:event_formWindowClosing

    private void quit() {

        if (unsaved.size() > 0) {
        	int res = JOptionPane.showConfirmDialog(null, "Save changes?", "Save?", JOptionPane.YES_NO_CANCEL_OPTION);
        	if (res == JOptionPane.CANCEL_OPTION) {
        		return;
        	} else if (res == JOptionPane.YES_OPTION) {
        		saveAll();
        	}
        }
        application.getDevice().stop();
        System.exit(1);
    }

	public void lineAppended(String line) {
		eventLog.append(line);
	}

	public void resourceSelected(Resource r) {
		
	}
	
	public void saveAll() {
		Iterator<AssemblyPanel> it = unsaved.keySet().iterator();
		while (it.hasNext()) {
			AssemblyPanel ap = it.next();
			ap.saveAssembly();
			it.remove();
		}
	}

	public void connactionAvailable(final PalcomService srv, final DynamicConnection dc) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
		        if (srv.getProtocol().equals("octetstream")) {
					OctetServiceView osv = new OctetServiceView(dc, srv);
					application.getService().addListener(dc, osv);
					
					String serviceName = "Unknown"; 
					try {
						serviceName = srv != null ? srv.getName() : "Unknown";
					} catch (ResourceException e1) {
						e1.printStackTrace();
					}
					DefaultSingleCDockable dd = new DefaultSingleCDockable("controlWindow" + (++panelcounter), serviceName, osv);
					dd.setCloseable(true);
			        control.addDockable(dd);
			        dd.setLocation(mainDock.getBaseLocation().aside());
			        dd.setVisible(true);
			        dd.addCDockableStateListener(new CDockableAdapter() {
			        	public void visibilityChanged(CDockable dockable) {
			        		try {
								dc.close();
							} catch (IllegalStateException e) {
								e.printStackTrace();
							} catch (NoSuchDeviceException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
			        	}
			        });
		        } 
			}
		});
	}
	
	public void addConnection(final ResourceProxy rp) {
		try {
			rp.waitForAvailable();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		final ServiceControlPanel scp = new ServiceControlPanel(rp, application);
		rp.addListener(scp);

		final DefaultSingleCDockable dd = new DefaultSingleCDockable("controlWindow" + (++panelcounter), rp.toString(), scp);
		dd.setCloseable(true);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				control.addDockable(dd);
				dd.setLocation(mainDock.getBaseLocation().aside());
				dd.setVisible(true);
				dd.addCDockableStateListener(new CDockableAdapter() {
		        	public void visibilityChanged(CDockable dockable) {
		        		try {
							rp.stop();
						} catch (IllegalStateException e) {
							e.printStackTrace();
						} 
		        	}
		        });
			}
		});
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Quit")) {
			quit();
		} else if (e.getActionCommand().equals("Save")) {
			saveAll();
		} else if (e.getActionCommand().equals("NetConfig")) {
			openConfiguration();
		}
	}

	
	ConcurrentHashMap<AssemblyPanel, AssemblyPanel> unsaved = new ConcurrentHashMap<AssemblyPanel, AssemblyPanel>();
	public void saveStatusChanged(boolean saved, Component source) {
		AssemblyPanel key = (AssemblyPanel)source;
		if (saved) {
			unsaved.remove(key);
		} else {
			unsaved.put(key, key);
		}
	}
}