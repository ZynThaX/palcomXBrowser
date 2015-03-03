/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DeviceBrowser.java
 *
 * Created on 2009-aug-07, 16:13:32
 */
package se.lth.cs.palcom.browsergui;

import internal.org.kxml2.io.KXmlSerializer;
import ist.palcom.resource.descriptor.AssemblyID;
import ist.palcom.resource.descriptor.ConnectionDeclList;
import ist.palcom.resource.descriptor.DeviceDeclList;
import ist.palcom.resource.descriptor.DeviceID;
import ist.palcom.resource.descriptor.EventHandlerList;
import ist.palcom.resource.descriptor.EventHandlerScript;
import ist.palcom.resource.descriptor.List;
import ist.palcom.resource.descriptor.Opt;
import ist.palcom.resource.descriptor.PRDAssemblyD;
import ist.palcom.resource.descriptor.PRDAssemblyVer;
import ist.palcom.resource.descriptor.ServiceDeclList;
import ist.palcom.resource.descriptor.SynthesizedServiceList;
import ist.palcom.resource.descriptor.VariableList;
import ist.palcom.resource.descriptor.VersionPart;
import ist.palcom.xml.XMLFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import se.lth.cs.palcom.assembly.AssemblyContainer;
import se.lth.cs.palcom.assembly.FSAssemblyManager;
import se.lth.cs.palcom.assembly.GUIRuntimeContext;
import se.lth.cs.palcom.assembly.OldschoolAssemblyLoader.OldAssemblyContainer;
import se.lth.cs.palcom.assembly.resource.ResourceProxy;
import se.lth.cs.palcom.assembly.resource.UnicastResourceProxy;
import se.lth.cs.palcom.common.PalComVersion;
import se.lth.cs.palcom.communication.connection.DynamicConnection;
import se.lth.cs.palcom.discovery.DiscoveryManager;
import se.lth.cs.palcom.discovery.ResourceException;
import se.lth.cs.palcom.discovery.proxy.PalcomService;
import se.lth.cs.palcom.logging.AbstractSink;
import se.lth.cs.palcom.logging.Logger;

/**
 *
 * @author mattias
 */
public class BrowserGUI implements BrowserApplication, AssemblyDirectory {
	private BrowserFrame frame;
	
    MiniBrowserDevice device;
    ListLogger loggingModel;
    
    public JMenuBar menubar;
	public JMenu mnuFile;
	public JMenu mnuLogStdout;
	//public JMenu mnuLogTab;
	public JMenu mnuLogging;
	public JMenuItem mnuQuit;
	
	private FSAssemblyManager assman;
	
	private void initMenu() { //TODO: This is hackish, but leave it for now...
		menubar = new javax.swing.JMenuBar();
        mnuFile = new javax.swing.JMenu();
        mnuQuit = new javax.swing.JMenuItem();
        mnuLogging = new javax.swing.JMenu();
        mnuLogStdout = new javax.swing.JMenu();
        //mnuLogTab = new javax.swing.JMenu();
        
        mnuFile.setText("File");

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setActionCommand("Save");
        saveItem.addActionListener(frame);
        mnuFile.add(saveItem);

        mnuQuit.setText("Quit");
        mnuQuit.setActionCommand("Quit");
        mnuQuit.addActionListener(frame);
        
        mnuFile.add(mnuQuit);

        menubar.add(mnuFile);

        mnuLogging.setText("Logging");

        mnuLogStdout.setText("Standard out");
        mnuLogging.add(mnuLogStdout);

        //mnuLogTab.setText("Log tab");
        //mnuLogging.add(mnuLogTab);
        
        Logger.setLevel(Logger.LEVEL_ERROR);
        
        JMenuItem bulk = new JMenuItem("Bulk");
        bulk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Logger.setLevel(Logger.LEVEL_BULK);
			}
		});
        JMenuItem debug = new JMenuItem("Debug");
        debug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Logger.setLevel(Logger.LEVEL_DEBUG);
			}
		});
        JMenuItem info = new JMenuItem("Info");
        info.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Logger.setLevel(Logger.LEVEL_ERROR);
			}
		});
        JMenuItem warning = new JMenuItem("Warning");
        warning.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Logger.setLevel(Logger.LEVEL_WARNING);
			}
		});
        JMenuItem error = new JMenuItem("Error");
        error.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Logger.setLevel(Logger.LEVEL_ERROR);
			}
		});
        
        mnuLogStdout.add(bulk);
        mnuLogStdout.add(debug);
        mnuLogStdout.add(info);
        mnuLogStdout.add(warning);
        mnuLogStdout.add(error);

        JMenu configItem = new JMenu("Configuration");
        JMenuItem networkItem = new JMenuItem("Network");
        networkItem.addActionListener(frame);
        networkItem.setActionCommand("NetConfig");
        
        configItem.add(networkItem);
        configItem.add(mnuLogging);
        menubar.add(configItem);

        frame.setJMenuBar(menubar);
	}

    /** Creates new form DeviceBrowser */
    public BrowserGUI(String[] args) throws SocketException, FileNotFoundException {
        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        /*
        Logger.addSinkToAll(new DualFileLogger(
                new PrintStream(new File(tmpdir, "netbeans-browser.log")),
                new PrintStream(new File(tmpdir, "netbeans-browser-bulk.log"))));
                */
        Preferences prefs = Preferences.userRoot().node("/se/lth/cs/palcom");
        String uuid = prefs.get("device_uuid", null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            prefs.put("device_uuid", uuid);
        }
        String host;
        try {
            host = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            Logger.log("Unable to determine host name", Logger.CMP_SERVICE, Logger.LEVEL_WARNING);
            host = "Unknown";
        }
        Logger.log("Hostname: " + host + ", UUID: " + uuid, Logger.CMP_SERVICE, Logger.LEVEL_INFO);
        device = new MiniBrowserDevice(this, new DeviceID("C:" + uuid), "Palcom browser @ " + host, args);
        loggingModel = new ListLogger();

		assman = new FSAssemblyManager(device, new GUIRuntimeContext()); //XXX: Set RuntimeContext here!

        frame = new BrowserFrame(this, this);
        initMenu();

        //Logger.addSinkToAll(loggingModel);
        /*
        Logger.defaultSink.changeLevel(Logger.LEVEL_DEBUG);
        for (int ii = 0; ii < Logger.levelNames.length; ii++) {
            addLogLevel(mnuLogStdout, ii, Logger.defaultSink);
        }
        for (int ii = 0; ii < Logger.levelNames.length; ii++) {
            if (loggingModel.accept(ii)) {
                addLogLevel(mnuLogTab, ii, loggingModel);
            }
        }
        */
        
        /* Finally start the device, after everything is initialized */
        device.start();
        
        frame.setSize(1024, 768);
        frame.setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        //Logger.removeSink( Logger.CMP_MAL, Logger.defaultSink);
        //Logger.addSink( Logger.CMP_MAL, new SystemOutSink(Logger.LEVEL_BULK));
        //java.awt.EventQueue.invokeLater(new Runnable() {

            //public void run() {
                try {
                	BrowserApplication bg = new BrowserGUI(args);
                } catch (SocketException ex) {
                    Logger.log("Cannot open BrowserGUI", ex, Logger.CMP_SERVICE, "BrowserGUI.main()", Logger.LEVEL_ERROR);
                    //System.exit(1);
                } catch (FileNotFoundException e) {
                    Logger.log("Cannot open BrowserGUI", e, Logger.CMP_SERVICE, "BrowserGUI.main()", Logger.LEVEL_ERROR);
                    //System.exit(1);
                } catch (Exception e) {
                	Logger.log("Exception in BrowserGUI", e, Logger.CMP_SERVICE, "BrowserGUI.main()", Logger.LEVEL_ERROR);
                }
          //  }
        //});
    }

	private class ListLogger extends AbstractSink {
        public ListLogger() {
            super(Logger.LEVEL_INFO);
        }

        public void log(int component, Object componentID, int level, Object[] messageParts) {
        	for (LogListener ll : logListeners) {
        		ll.lineAppended(AbstractSink.toString(false, component, componentID, level, messageParts) + "\n");
        	}
        }

		public void close() {}
    }
	
	public MiniBrowserDevice getDevice() { return device; }
	public BrowserService getService() { return device.getBrowserService(); }
	
	/*
	private void addLogLevel(JMenu menu, final int level, final AbstractSink sink) {
        final JCheckBoxMenuItem pickLogLevel = new JCheckBoxMenuItem(Logger.levelNames[level], sink.accept(level));
        pickLogLevel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                sink.setAccept(level, pickLogLevel.getState());
            }
        });
        menu.add(pickLogLevel);
    }
    */
	
	private final CopyOnWriteArrayList<LogListener> logListeners = new CopyOnWriteArrayList<LogListener>();
	public void addLogListener(LogListener ll) {
		logListeners.add(ll);
	}
	public void removeLogListener(LogListener ll) {
		logListeners.remove(ll);
	}
	
	private HashMap<PalcomService, DynamicConnection> openConnections = new HashMap<PalcomService, DynamicConnection>();
	
	public void connectToService(final PalcomService res) {
		if (openConnections.containsKey(res)) {
			
		} else {
				//Temporary fix, replace with an executor-service or something...
			
				Thread t = new Thread() {
					public void run() {
						ResourceProxy rp;
						try {
							String uid = null;
							rp = (ResourceProxy) res.connectToProxy(getService().getConnectionHandler(), getService().getLocalAddress(), 7500, PalComVersion.DEFAULT_SERVICE_INTERACTION_PROTOCOL);
							if (res.requiresAuth()) {
								uid = (String) JOptionPane.showInputDialog(new JTextField(), "UserID", "UserID", JOptionPane.QUESTION_MESSAGE, null, null, null);
								((UnicastResourceProxy)rp).setUserId(uid);
							}
							rp.start();
							frame.addConnection(rp);
						} catch (ResourceException e) {
							e.printStackTrace();
						} //TODO: We need a better way to handle timeouts
					}
				};
				t.setDaemon(true);
				t.setName("BrowserGUI connect Thread (connecting to " + res + ")");
				t.start();
					}
	}
	
	public DiscoveryManager getDiscoveryManager() {
		return device.getDiscoveryManager();
	}

	public String[] getAssemblyNames() {
		return assman.getAssemblyNames();
	}
	
	public void writeAssembly(String filename, byte[] data) {
			try {
			if (assman.existsAssembly(filename)) {
				assman.updateAssembly(data, filename, false);
			} else {
				PRDAssemblyVer version = new PRDAssemblyVer();
				version.setName(filename);
				version.setVersion(new AssemblyID(new VersionPart(device.getDeviceID(), "Version"), new VersionPart(device.getDeviceID(), "Version?"), new Opt(), new Opt(), "1.0"));
				version.setFormat(AssemblyPanel.assemblyFormat);
				version.setDevices(new DeviceDeclList());
				version.setServices(new ServiceDeclList());
				version.setConnections(new ConnectionDeclList());
				version.setSynthesizedServices(new SynthesizedServiceList());
				version.setEventHandlerScript(new EventHandlerScript(new VariableList(new List()), new EventHandlerList(new List())));
				assman.createAssembly(version.getXMLData(), filename, ".ass", false);
			}
		} catch (Exception e) { //WTF
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void removeAssembly(String filename) {
		//try {
			assman.removeAssembly(filename);
			//device.getDeviceRootFileSystem().getFile("assemblies/" + filename).delete();
		//} catch (IOException e) {
		//	e.printStackTrace();
		//}
	}
	
	public PRDAssemblyD loadAssembly(String filename) {
		AssemblyContainer ac = assman.getAssembly(filename);
		if (ac instanceof OldAssemblyContainer) {
			AssemblyContainer oac = (AssemblyContainer)ac;
			return (PRDAssemblyD) oac.getAssemblyD().fullCopy();
		}
		return null;
	}
	
	public void exportAssembly(String filename, File outDir) {
		AssemblyContainer ac = assman.getAssembly(filename);
		if (ac instanceof OldAssemblyContainer) {
			OldAssemblyContainer oac = (OldAssemblyContainer)ac;
			
			FileOutputStream fout;
			try {
				
				if (!outDir.exists()) {
					outDir.createNewFile();
				}
				//This doesn't work for some reason...
				//File outFile = new File(outDir, oac.getName() + oac.getSuffix());
				//outFile.createNewFile(); 
				File outFile = outDir;
				//
				fout = new FileOutputStream(outFile);
				KXmlSerializer serializer = new KXmlSerializer();
				serializer.setOutput(fout, null);
				serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
				
				serializer.startDocument("ISO-8859-1", null);
				serializer.docdecl(" " + oac.getAssemblyVer().getTagName() + " SYSTEM \"" + XMLFactory.DTD + "\"");
				
				oac.getAssemblyVer().writeXMLElement(serializer);
				serializer.endDocument();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public PRDAssemblyVer reloadAssembly(String filename) {
		AssemblyContainer ac = assman.getAssembly(filename);
		if (ac instanceof OldAssemblyContainer) {
			OldAssemblyContainer oac = (OldAssemblyContainer)ac;
			oac.reload();
			return oac.getAssemblyVer();
		}
		return null;
	}

	public boolean isLoaded(String filename) {
		return assman.isAssemblyLoaded(filename);
	}

	public void startAssembly(String name) {
		try {
			assman.updateAssembly(name, true);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Uh-Oh", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public void stopAssembly(String name) {
		try {
			assman.updateAssembly(name, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isRunning(String filename) {
		return assman.isAssemblyEnabled(filename);
	}
}
