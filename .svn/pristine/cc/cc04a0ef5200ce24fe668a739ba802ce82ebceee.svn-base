package se.lth.cs.palcom.browsergui;

import ist.palcom.resource.descriptor.Command;
import ist.palcom.resource.descriptor.CommandInfo;
import ist.palcom.resource.descriptor.ControlInfo;
import ist.palcom.resource.descriptor.PRDServiceFMDescription;
import ist.palcom.resource.descriptor.Param;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import se.lth.cs.palcom.assembly.resource.ResourceProxy;
import se.lth.cs.palcom.assembly.resource.UnicastResourceProxy;
import se.lth.cs.palcom.browsergui.views.components.FileProvider;
import se.lth.cs.palcom.browsergui.views.components.MimeProvider;
import se.lth.cs.palcom.browsergui.views.components.TextProvider;
import se.lth.cs.palcom.communication.connection.Connection;
import se.lth.cs.palcom.communication.connection.DynamicConnection;
import se.lth.cs.palcom.communication.connection.Readable;
import se.lth.cs.palcom.communication.connection.Writable;
import se.lth.cs.palcom.discovery.PalcomControlServiceDescription;
import se.lth.cs.palcom.discovery.ResourceException;
import se.lth.cs.palcom.discovery.proxy.CachedResource;
import se.lth.cs.palcom.discovery.proxy.CachedResourceListener;
import se.lth.cs.palcom.discovery.proxy.PalcomService;
import se.lth.cs.palcom.discovery.proxy.Resource;
import se.lth.cs.palcom.discovery.proxy.ResourceListener;
import se.lth.cs.palcom.service.command.PackagingFactory;

public class ServiceControlPanel extends JPanel implements CommandListener, CachedResourceListener {
	private GridBagConstraints c;
	private GridBagConstraints messageConstraints;
	private JPanel messageGrid;
	private final ResourceProxy rp;
	private PalcomService service;
	private Command lastCommand;
	private MessagePanel lastMessage;
	private BrowserApplication application;
	private Connection dc;
	private JLabel leftHeaderName;
	private JScrollPane messageScroller;
	private JScrollPane scroller;

	public ServiceControlPanel(final ResourceProxy rp, BrowserApplication application) {
		this.rp = rp; 
		this.application = application;
		rp.addListener(this);
		if (rp.isAvailable()) {
			available(rp);
		}
		
		setLayout(new GridLayout(1, 2, 0, 0));
		messageGrid = new JPanel();
		messageGrid.setLayout(new GridBagLayout());
		messageGrid.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		messageGrid.setBackground(Color.WHITE);
		JPanel leftPanel = new JPanel(new GridBagLayout());
		
		messageConstraints = new GridBagConstraints();
		messageConstraints.gridx = 0;
		messageConstraints.weightx = 0.75;
		messageConstraints.fill = GridBagConstraints.HORIZONTAL;
		
		c = new GridBagConstraints();

		leftPanel.setBorder(BorderFactory.createEmptyBorder());
		leftPanel.setBackground(Color.WHITE);
		
		scroller = new JScrollPane(leftPanel);
		add(scroller);
		messageScroller = new JScrollPane(messageGrid);
		add(messageScroller);
		
		GridBagConstraints constraints = (GridBagConstraints) c.clone();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1;
		
		JOutlookBar commandBar = new JOutlookBar(leftPanel);
		
		constraints.fill = GridBagConstraints.HORIZONTAL;
		JPanel leftHeader = new JPanel(new BorderLayout());
		StringBuilder sb = new StringBuilder(); 
		
		leftHeaderName = new JLabel(rp.toString(), getIcon(), SwingConstants.CENTER);
		Border padding = BorderFactory.createEmptyBorder(5,5,5,5);
		
		leftHeader.add(leftHeaderName, BorderLayout.WEST);
		leftHeader.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.BLACK), padding));
		
		File img = new File("images/system_help.png");
		ImageIcon icon = null;
		if (img.exists()) {
			icon = new ImageIcon(img.getAbsolutePath());
		} else {
			URL url = getClass().getResource("/images/system_help.png");
			if (url != null) {
				icon = new ImageIcon(url);
			}
		}
		JLabel help = new JLabel(icon);
		try {
			help.setToolTipText(service.getHelpText());
		} catch (ResourceException e1) {
			e1.printStackTrace();
		}
		leftHeader.add(help, BorderLayout.EAST);

		leftPanel.add(leftHeader, constraints);
		
		constraints.gridy = 2;
		constraints.fill = GridBagConstraints.BOTH;
		leftPanel.add(commandBar, constraints);
		
		PRDServiceFMDescription sd;
		try {
			sd = ((PalcomControlServiceDescription) service.getDescription()).getPRDServiceFMDescription();
		} catch (ResourceException e1) {
			return;
		}
		for (int i = 0; i < sd.getNumControlInfo(); ++i) {
			ControlInfo ci = sd.getControlInfo(i);
			if (!(ci instanceof CommandInfo)) {
				continue;
			}
			final CommandInfo cmd = (CommandInfo)ci;
			if (!(cmd.getDirection().equals("in"))) {
				continue;
			}
			
			final Command command = cmd.createCommand();
			/*
			 * FIXME: OBS! Command.invoke() will eventually be removed and
			 * replaced by the sendTo() method in each service. I'm not sure how
			 * this is supposed to work here though, as a PalComService doesn't
			 * have any send method.
			 */
			if (command.getNumParam() == 0) {
				JButton invoke = new JButton(command.getID());
		        invoke.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		                command.invoke((Writable) rp.getCurrentConnection(), new byte[0][0]); //TODO: Check that it's connected...
		                addMessage(command);
		            }
		        });
		        commandBar.addBar(sd.getControlInfo(i), invoke);
			} else {
				JPanel p = createParamPane(cmd);
				commandBar.addBar(sd.getControlInfo(i), p);
			}
		}
		invalidate();
	}
	class ParamPanel extends JPanel {
		protected final JButton invoke;

		public ParamPanel(JButton invoke) {
			this.invoke = invoke;
		}
		
		public void setEnabled(boolean enabled) {
			invoke.setEnabled(enabled);
		}
	}
	private ParamPanel createParamPane(final CommandInfo ci) {
		final Command cmd = ci.createCommand();
		GridBagConstraints con = new GridBagConstraints();
		JButton invoke = new JButton("Invoke");
		ParamPanel commandPanel = new ParamPanel(invoke);
		commandPanel.setLayout(new GridBagLayout());
        // TODO use name, not id
        commandPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
        commandPanel.setBackground(Color.WHITE);
        int ii;
        final MimeProvider[] providers = new MimeProvider[cmd.getNumParam()];
        for (ii = 0; ii < cmd.getNumParam(); ii++) {
            Param p = cmd.getParam(ii);
            JLabel info = new JLabel(p.getInfo().getID());
            con.gridy = ii;
            con.anchor = GridBagConstraints.LINE_END;
            con.weightx = 0.5;
            commandPanel.add(info, con);

            Component value; MimeProvider provider;
            final String mime = p.getInfo().getType();
            if (mime.equals("application/octet-stream") || mime.equals("application/x-jar")) {
                FileProvider b = new FileProvider();
                value = b; provider = b;
                b.setDialogTitle("Load file to send");
                b.setDialogType(JFileChooser.OPEN_DIALOG);
            } else if (mime.startsWith("image/")) {
                FileProvider image = new FileProvider();
                value = image; provider = image;
                image.setDialogTitle("Load image");
                image.setDialogType(JFileChooser.OPEN_DIALOG);
                /* TODO image.addAcceptFromMime(mime) */
            } else {
                TextProvider t = new TextProvider(10);
                value = t; provider = t;
            }
            con.anchor = GridBagConstraints.LINE_START;
            commandPanel.add(value, con);
            providers[ii] = provider;
        }
        invoke.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                byte[][] data = new byte[providers.length][];
                for(int ii = 0; ii < providers.length; ii++) {
                    data[ii] = providers[ii].getData();
                }
                cmd.invoke((Writable) rp.getCurrentConnection(), data);
                addMessage(cmd);
            }
        });

        commandPanel.add(invoke, new GridBagConstraints(0, ii, GridBagConstraints.REMAINDER, 1, 1, 1, GridBagConstraints.SOUTHEAST, 0, new Insets(0, 0, 0, 0), 0, 0));
        return commandPanel;
	}
	
	public void addMessage(final Command command) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (lastMessage != null && lastCommand != null && command.equals(lastCommand)) {
					lastMessage.dupe();
					ServiceControlPanel.this.validate();
					return;
				}
				lastCommand = command;
				
				final MessagePanel mp = new MessagePanel(command);
				lastMessage = mp;

				messageConstraints.gridy++;
				messageConstraints.fill = GridBagConstraints.HORIZONTAL;
				messageGrid.add(mp, messageConstraints);

				ServiceControlPanel.this.validate();
				JScrollBar bar = messageScroller.getVerticalScrollBar();
				bar.setValue(bar.getMaximum());
			}
		});
	}
	
	public void commandInvoked(Readable conn, Command command) {
        addMessage(command);
    }
	
	private class MessagePanel extends JPanel {
		private GridBagConstraints con;
		private Command cmd;
		private JLabel timeLbl;
		private JLabel commandIdLabel;
		private int dupes = 0;

		public void dupe() {
			commandIdLabel.setText(cmd.getID() + " (" + ++dupes + ") duplicates");
			timeLbl.setToolTipText("Last at " + new Date().toGMTString());
		}
		
		public MessagePanel(Command cmd) {
			this.cmd = cmd;
			setBackground(Color.WHITE);
			setLayout(new GridBagLayout());
			setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
			con = new GridBagConstraints();
			con.gridx = 0;
			con.gridy = 0;
			con.fill = GridBagConstraints.HORIZONTAL;
			con.anchor = GridBagConstraints.LINE_START;
	
			GridBagConstraints gbc = (GridBagConstraints) con.clone();
			gbc.weightx = 1.0f;
			gbc.gridwidth = 2;

			Color bg = null;
			String file = "";
			if (cmd.getInfo().getDirection().equals("in")) {
				bg = new Color(0.95f, 0.95f, 0.95f);
				file = "images/arrow_right.gif";
			} else if (cmd.getInfo().getDirection().equals("out")) {
				bg = new Color(0.85f, 0.85f, 0.85f);
				file = "images/arrow_left.gif";
			}
			
			File img = new File(file);
			ImageIcon icon = null;
			if (img.exists()) {
				icon = new ImageIcon(img.getAbsolutePath());
			} else {
				URL url = getClass().getResource("/" + file);
				if (url != null) {
					icon = new ImageIcon(url);
				}
			}

			JPanel cmdPanel = new JPanel(new GridBagLayout());
			GridBagConstraints cmdConstraints = new GridBagConstraints();
			cmdConstraints.weightx = 1.0f;
			cmdConstraints.fill = GridBagConstraints.BOTH;
			commandIdLabel = new JLabel(cmd.getID());
			cmdPanel.setBackground(bg);
			commandIdLabel.setIcon(icon);
			commandIdLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			
			int lowerBorder = 1;
			if (cmd.getNumParam() == 0){
				lowerBorder = 2;
			}
			cmdPanel.add(commandIdLabel, cmdConstraints);
			
			img = new File("images/clock.png");
			icon = null;
			if (img.exists()) {
				icon = new ImageIcon(img.getAbsolutePath());
			} else {
				URL url = getClass().getResource("/images/clock.png");
				if (url != null) {
					icon = new ImageIcon(url);
				}
			}
			timeLbl = new JLabel(icon);
			timeLbl.setToolTipText(new Date().toGMTString());
			
			Border border = BorderFactory.createMatteBorder(2, 2, lowerBorder, 2, Color.BLACK);
			Border padding = BorderFactory.createEmptyBorder(5,5,5,5);
			cmdPanel.setBorder(BorderFactory.createCompoundBorder(border, padding));
			cmdConstraints.gridx = 1;
			cmdConstraints.fill = GridBagConstraints.NONE;
			cmdConstraints.anchor = GridBagConstraints.LINE_END;
			cmdConstraints.weightx = 0;
			
			cmdPanel.add(timeLbl, cmdConstraints);
			add(cmdPanel, gbc);
			
			for (int i = 0; i < cmd.getNumParam(); ++i) {
				lowerBorder = 0;
				int leftborder = 0;
				int rightborder = 0;
				if (cmd.getNumParam() == (i+1)){
					lowerBorder = 2;
				}
				int lowerlblBorder = lowerBorder;
				final Param p = cmd.getParam(i);
				String mime = p.getInfo().getType();
				con.gridy += 1;
				GridBagConstraints lblconstraints = (GridBagConstraints) con.clone();
				GridBagConstraints dataconstraints = (GridBagConstraints) con.clone();
				
				
				JComponent pl = null;
				if (mime.startsWith("text/")) {	//this is kinda ugly...
					try {
						pl = new JLabel(new String(p.getData(), "UTF8"));
						pl.setBorder(BorderFactory.createCompoundBorder(border, padding));
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e); 
					}
				} else if (mime.startsWith("image/")) {
					lblconstraints.gridwidth = 2;
					dataconstraints.gridx -= 1; //Compensate for the plus one later. Ugly, but whatever...
					dataconstraints.gridy += 1;
					dataconstraints.gridwidth = 2;
					leftborder = 2;
					rightborder = 2;
					lowerlblBorder = 0;
					InputStream is = new ByteArrayInputStream(p.getData());
	                Image image = null;
					try {
						image = ImageIO.read(is);
					} catch (IOException e) {
						e.printStackTrace();
					}
					pl = new JLabel(new ImageIcon(image));
					pl.setBorder(BorderFactory.createCompoundBorder(border, padding));
				} else if(mime.equals("application/octet-stream")) {
					pl = new JButton("Save");
					((JButton)pl).addActionListener(new ActionListener() {
						
						public void actionPerformed(ActionEvent arg0) {
							JFileChooser fc = new JFileChooser();
							int val = fc.showSaveDialog(null);
							if (val == JFileChooser.APPROVE_OPTION) {
								File f = fc.getSelectedFile();
								try {
									FileOutputStream fstream = new FileOutputStream(f);
									System.err.println("--- writing " + new String(p.getData()));
									fstream.write(p.getData());
									fstream.close();
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					});
				} else {
					pl = new JLabel("Unsupported type: " + mime);
					pl.setBorder(BorderFactory.createCompoundBorder(border, padding));
				}
				JLabel pid = new JLabel(p.getInfo().getID() + " (" + mime + "): ");
				border = BorderFactory.createMatteBorder(0, 2, lowerlblBorder, rightborder, Color.BLACK);
				pid.setBorder(BorderFactory.createCompoundBorder(border, padding));
				
				add(pid, lblconstraints);
				dataconstraints.gridx += 1;
				border = BorderFactory.createMatteBorder(0, leftborder, lowerBorder, 2, Color.BLACK);
				add(pl, dataconstraints);
			}
		}
	}

	public void available(Resource resource) {
		synchronized (rp) {
			PalcomService srv = rp.getConnectedService();
			if (srv != null && !(srv.equals(service))) {
				if (service != null) {
					service.removeListener(this);
				}
				service = srv;
				service.addListener(this); //This will cause available to be called again, but the above test should prevent too deep recursion.
			}
			Connection tmp = rp.getCurrentConnection();
			if (tmp != null && !(tmp.equals(dc))) {
				dc = tmp;
				application.getService().addListener(tmp, this);
			}
		}
		updateStatus();
		
	}

	public void unavailable(Resource resource) {
		updateStatus();
	}

	private void updateStatus() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (leftHeaderName != null) {
					leftHeaderName.setIcon(getIcon());
				}
			}
		});
	}

	public void resourceDesynchronized(CachedResource r) {
		updateStatus();
	}

	public void resourceSynchronized(CachedResource r) {
		updateStatus();
	}

	public void resourceChanged(Resource r) {
		updateStatus();
	}

	public void resourceInvalidated(Resource r) {
		updateStatus();
	}
	
	public Icon getIcon() {
		String file = "";
		if (service == null) {
			return null;
		}
		byte status = 'R';
		try {
			status = service.getStatus().getStatus();
		} catch (ResourceException e) {
			//e.printStackTrace(); //I don't think we care about this...
		}
		
		if (status == 'G') {
			file = "images/box-green.png";
		} else if (status == 'Y') {
			file = "images/box-yellow.png";
		} else {
			file = "images/box-red.png";
		}
		File img = new File(file);
		if (img.exists()) {
			ImageIcon imgic = new ImageIcon(img.getAbsolutePath());
			return imgic;
		} else {
			URL url = getClass().getResource("/" + file);
			if (url != null) {
				return new ImageIcon(url);
			}
			return null;
		}
	}

	@Override
	public void validate() {
		super.validate();
		scroller.validate();
	}
}
