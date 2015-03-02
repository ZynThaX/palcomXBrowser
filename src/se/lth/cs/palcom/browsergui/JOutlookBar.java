package se.lth.cs.palcom.browsergui;

import ist.palcom.resource.descriptor.ControlInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class JOutlookBar extends JPanel implements ActionListener {
	/**
	 * The top panel: contains the buttons displayed on the top of the
	 * JOutlookBar
	 */
	private JPanel topPanel = new JPanel(new GridLayout(1, 1));

	/**
	 * The bottom panel: contains the buttons displayed on the bottom of the
	 * JOutlookBar
	 */
	private JPanel bottomPanel = new JPanel(new GridLayout(1, 1));

	/**
	 * A LinkedHashMap of bars: we use a linked hash map to preserve the order
	 * of the bars
	 */
	private Map bars = new LinkedHashMap();

	/**
	 * The currently visible bar (zero-based index)
	 */
	private int visibleBar = 0;

	/**
	 * A place-holder for the currently visible component
	 */
	private JComponent visibleComponent = null;

	private GridBagConstraints constraints;

	private JComponent pparent;

	/**
	 * Creates a new JOutlookBar; after which you should make repeated calls to
	 * addBar() for each bar
	 */
	public JOutlookBar(JComponent parent) {
		setBackground(Color.CYAN);
		this.pparent = parent;
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		
		this.setLayout(new GridBagLayout());
		
		this.add(topPanel, constraints);
		
		constraints.gridy = 2;
		this.add(bottomPanel, constraints);
		
		this.visibleBar = -1;
	}

	/**
	 * Adds the specified component to the JOutlookBar and sets the bar's name
	 * 
	 * @param ci
	 *            The name of the outlook bar
	 * @param componenet
	 *            The component to add to the bar
	 */
	public void addBar(ControlInfo ci, JComponent component) {
		BarInfo barInfo = new BarInfo(ci, component);
		barInfo.getButton().addActionListener(this);
		
		this.bars.put(ci.getID(), barInfo);
		
		render();
	}
	
	public void addBar(ControlInfo ci, JButton component) {
		BarInfo barInfo = new BarInfo(ci, component);
		//barInfo.getButton().addActionListener(this);
		this.bars.put(ci.getID(), barInfo);
		render();
	}

	/**
	 * Adds the specified component to the JOutlookBar and sets the bar's name
	 * 
	 * @param name
	 *            The name of the outlook bar
	 * @param icon
	 *            An icon to display in the outlook bar
	 * @param componenet
	 *            The component to add to the bar
	 */
	public void addBar(String name, Icon icon, JComponent component) {
		BarInfo barInfo = new BarInfo(name, icon, component);
		barInfo.getButton().addActionListener(this);
		this.bars.put(name, barInfo);
		render();
	}

	/**
	 * Removes the specified bar from the JOutlookBar
	 * 
	 * @param name
	 *            The name of the bar to remove
	 */
	public void removeBar(String name) {
		this.bars.remove(name);
		render();
	}

	/**
	 * Returns the index of the currently visible bar (zero-based)
	 * 
	 * @return The index of the currently visible bar
	 */
	public int getVisibleBar() {
		return this.visibleBar;
	}

	/**
	 * Programmatically sets the currently visible bar; the visible bar index
	 * must be in the range of 0 to size() - 1
	 * 
	 * @param visibleBar
	 *            The zero-based index of the component to make visible
	 */
	public void setVisibleBar(int visibleBar) {
		if (visibleBar > 0 && visibleBar < this.bars.size() - 1) {
			this.visibleBar = visibleBar;
			render();
		}
	}

	/**
	 * Causes the outlook bar component to rebuild itself; this means that it
	 * rebuilds the top and bottom panels of bars as well as making the
	 * currently selected bar's panel visible
	 */
	public void render() {
		// Compute how many bars we are going to have where
		int totalBars = this.bars.size();
		int topBars = this.visibleBar + 1;
		int bottomBars = totalBars - topBars;
		
		Border padding = BorderFactory.createEmptyBorder(3, 3, 3, 3);
		// Get an iterator to walk through out bars with
		Iterator itr = this.bars.keySet().iterator();

		// Render the top bars: remove all components, reset the GridLayout to
		// hold to correct number of bars, add the bars, and "validate" it to
		// cause it to re-layout its components
		this.topPanel.removeAll();
		GridLayout topLayout = (GridLayout) this.topPanel.getLayout();
		topLayout.setRows(topBars);
		BarInfo barInfo = null;
		for (int i = 0; i < topBars; i++) {
			String barName = (String) itr.next();
			barInfo = (BarInfo) this.bars.get(barName);
			barInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.BLACK), padding));
			this.topPanel.add(barInfo);
		}
		this.topPanel.validate();

		// Render the center component: remove the current component (if there
		// is one) and then put the visible component in the center of this
		// panel
		if (this.visibleComponent != null) {
			this.remove(this.visibleComponent);
		}
		if (barInfo != null) {
			this.visibleComponent = barInfo.getComponent();
			constraints.gridy = 1;
			this.add(visibleComponent, constraints);
		}

		// Render the bottom bars: remove all components, reset the GridLayout
		// to
		// hold to correct number of bars, add the bars, and "validate" it to
		// cause it to re-layout its components
		this.bottomPanel.removeAll();
		GridLayout bottomLayout = (GridLayout) this.bottomPanel.getLayout();
		bottomLayout.setRows(bottomBars);
		for (int i = 0; i < bottomBars; i++) {
			String barName = (String) itr.next();
			barInfo = (BarInfo) this.bars.get(barName);
			barInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder((i == 0 && visibleBar < 0) ? 1 : 0, 1, 1, 1, Color.BLACK), padding));
			this.bottomPanel.add(barInfo);
		}
		this.bottomPanel.validate();

		// Validate all of our components: cause this container to re-layout its
		// subcomponents
		this.validate();
		
		if (pparent != null) {
			pparent.validate();
		}
	}

	/**
	 * Invoked when one of our bars is selected
	 */
	public void actionPerformed(ActionEvent e) {
		int currentBar = 0;
		for (Iterator i = this.bars.keySet().iterator(); i.hasNext();) {
			String barName = (String) i.next();
			BarInfo barInfo = (BarInfo) this.bars.get(barName);
			if (barInfo.getButton() == e.getSource()) {
				// Found the selected button
				if (this.visibleBar == currentBar) {
					this.visibleBar = -1;
				} else {
					this.visibleBar = currentBar;
				}
				render();
				return;
			}
			currentBar++;
		}
	}

	/**
	 * Debug, dummy method
	 */
	public static JPanel getDummyPanel(String name) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(name, JLabel.CENTER));
		return panel;
	}

	/**
	 * Internal class that maintains information about individual Outlook bars;
	 * specifically it maintains the following information:
	 * 
	 * name The name of the bar button The associated JButton for the bar
	 * component The component maintained in the Outlook bar
	 */
	class BarInfo extends JPanel {
		private String name;
		private JButton button;
		private JComponent component;

		public BarInfo(ControlInfo ci, JComponent component) {
			setLayout(new BorderLayout());
			this.name = ci.getID();
			this.component = component;
			this.button = new JButton(ci.getID() + " ...");
			
			setBackground(new Color(0.95f, 0.95f, 0.95f));
			add(button, BorderLayout.WEST);
			
			if (ci.getHelp() != null && !(ci.getHelp().equals(""))) {
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
				help.setToolTipText(ci.getHelp());
				add(help, BorderLayout.EAST);
			}
		}
		
		public BarInfo(ControlInfo ci, JButton button) {
			setLayout(new BorderLayout());
			this.name = ci.getID();
			this.component = null;
			this.button = button;
			
			setBackground(new Color(0.95f, 0.95f, 0.95f));
			add(button, BorderLayout.WEST);
			
			if (ci.getHelp() != null && !(ci.getHelp().equals(""))) {
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
				help.setToolTipText(ci.getHelp());
				add(help, BorderLayout.EAST);
			}
		}

		public BarInfo(String name, Icon icon, JComponent component) {
			this.name = name;
			this.component = component;
			this.button = new JButton(name, icon);
			component.setEnabled(false);
		}

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public JButton getButton() {
			return this.button;
		}

		public JComponent getComponent() {
			return this.component;
		}
	}
}