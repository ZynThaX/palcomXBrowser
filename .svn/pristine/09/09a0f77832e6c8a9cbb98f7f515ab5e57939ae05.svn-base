package se.lth.cs.palcom.browsergui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

public class ProjectPanel extends JPanel implements MouseListener, ActionListener {
	private String[] filenames;
	private JList fileList;
	private BrowserFrame browserFrame;
	
	private JPopupMenu menu;
	private JMenuItem deleteItem;
	
	private BrowserApplication application;
	private AssemblyDirectory assemblies;
	private DefaultListModel model;
	
	public ProjectPanel(BrowserApplication application, BrowserFrame browserFrame, AssemblyDirectory assemblies) {
		this.browserFrame = browserFrame;
		this.application = application;
		this.assemblies = assemblies;
		setLayout(new BorderLayout());
		filenames = assemblies.getAssemblyNames();
		
		model = new DefaultListModel();
		for (String s: filenames) {
			model.addElement(s);
		}
		
		fileList = new JList(model);
		fileList.addMouseListener(this);
		fileList.setCellRenderer(new Renderer());
		add(new JScrollPane(fileList));
		initMenu();
	}
	
	private void initMenu() {
		menu = new JPopupMenu();
		JMenuItem addItem = new JMenuItem("New assembly");
		addItem.addActionListener(this);
		addItem.setActionCommand("Add");
		
		deleteItem = new JMenuItem("Remove assembly");
		deleteItem.addActionListener(this);
		deleteItem.setActionCommand("Del");
		
		JMenuItem exportItem = new JMenuItem("Export");
		exportItem.addActionListener(this);
		exportItem.setActionCommand("Export");
		
		menu.add(addItem);
		menu.add(deleteItem);
		menu.add(exportItem);
		
		
		JToolBar bar = new JToolBar();

		ImageIcon playIcon = null;
		String file = "images/Play-1-Hot-icon.png";
		File img = new File(file);
		if (img.exists()) {
			playIcon = new ImageIcon(img.getAbsolutePath());
		} else {
			URL url = getClass().getResource("/" + file);
			if (url != null) {
				playIcon = new ImageIcon(url);
			}
		}
		
		ImageIcon stopIcon = null;
		file = "images/stop-red-icon.png";
		img = new File(file);
		if (img.exists()) {
			stopIcon = new ImageIcon(img.getAbsolutePath());
		} else {
			URL url = getClass().getResource("/" + file);
			if (url != null) {
				stopIcon = new ImageIcon(url);
			}
		}
		
		JButton btnStart = new JButton(playIcon);
		btnStart.setBorder(BorderFactory.createEmptyBorder());
		btnStart.setActionCommand("StartAssembly");
		btnStart.addActionListener(this);
		JButton btnStop  = new JButton(stopIcon);
		btnStop.setActionCommand("StopAssembly");
		btnStop.addActionListener(this);
		btnStop.setBorder(BorderFactory.createEmptyBorder());
		
		bar.add(btnStart);
		bar.add(btnStop);
		
		// REMOVE THIS
		JButton btn = new JButton("Throw");
		btn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				new RuntimeException("--- Something happpened around here ---").printStackTrace();
			}
		});
		bar.add(btn);
		//////////////
		
		add(bar, BorderLayout.PAGE_START);
	}
	
	private void doPopup(MouseEvent me) {
		if (me.isPopupTrigger()) {
			if (fileList.getSelectedValue() == null) {
				deleteItem.setEnabled(false);
			} else {
				deleteItem.setEnabled(true);
			}
			menu.show(me.getComponent(), me.getX(), me.getY());
		}
	}
	
	public void mouseClicked(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mousePressed(MouseEvent me) {
		doPopup(me);
	}

	public void mouseReleased(MouseEvent me) {
		doPopup(me);
		if (me.getClickCount() > 1) {
			open();
		}
		
	}
	
	private void open() {
		String f = (String)fileList.getSelectedValue();
		browserFrame.openAssembly(f);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Add")) {
			String filename = JOptionPane.showInputDialog("Filename?");
			if (filename.contains(".")) {
				filename = filename.substring(0, filename.lastIndexOf("."));
			}
			final String fn = filename; 
			application.writeAssembly(filename, new byte[0]);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((DefaultListModel)fileList.getModel()).addElement(fn);
					browserFrame.openAssembly(fn);
				}
			});
		} else if (e.getActionCommand().equals("Del")) {
			String f = (String)fileList.getSelectedValue();
			int res = JOptionPane.showConfirmDialog(null, "Delete " + f + "?",
					"Confirm deletion", JOptionPane.YES_NO_OPTION);
			if (res == JOptionPane.YES_OPTION) {
				((DefaultListModel)fileList.getModel()).removeElement(f);
				application.removeAssembly(f);
			} 
		} else if (e.getActionCommand().equals("StartAssembly")) {
			assemblies.startAssembly((String) fileList.getSelectedValue());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fileList.validate();
					fileList.repaint();
				}
			});
			
		} else if (e.getActionCommand().equals("StopAssembly")) {
			assemblies.stopAssembly((String) fileList.getSelectedValue());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fileList.validate();
					fileList.repaint();
				}
			});
		} else if (e.getActionCommand().equals("Export")) {
			String name = ((String) fileList.getSelectedValue());

			JFileChooser fc = new JFileChooser();
			fc.setSelectedFile(new File(name.substring(name.indexOf(":") + 1) + ".ass"));
			//fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int ret = fc.showSaveDialog(null);
			
			if (ret == JOptionPane.OK_OPTION) {
				application.exportAssembly(name, fc.getSelectedFile());
			}
		}
	}
	
	private class Renderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			String name = (String)value;
			boolean loaded = assemblies.isLoaded(name);
			setText(name + (assemblies.isRunning(name) ? " (Running)" : ""));
			setForeground(loaded ? (isSelected ? Color.WHITE : Color.BLACK) : Color.RED);
			setBackground(isSelected ? Color.BLUE : Color.WHITE);
			return this;
		}
		
	}
}
