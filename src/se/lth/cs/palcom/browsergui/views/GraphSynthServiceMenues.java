package se.lth.cs.palcom.browsergui.views;

import ist.palcom.resource.descriptor.SynthesizedService;

import java.awt.Component;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import se.lth.cs.palcom.browsergui.views.GraphSynthServicePanel.ServiceObjGUI;

public class GraphSynthServiceMenues {
	
	public GraphSynthServiceMenues(){
	}
	
	
	public RemoveSSMenu createRemoveSSMenu(ServiceObjGUI serviceObjGUI, SynthesizedService ss) {
		return new RemoveSSMenu(serviceObjGUI, ss);
	}
	
	public JPopupMenu createCommandMenu(ServiceObjGUI serviceObjGUI) {
		return new CommandMenu(serviceObjGUI);
	}
	
	public JPopupMenu createServiceDescriptionMenu(ServiceObjGUI serviceObjGUI) {
		return new ServiceDescriptionMenu(serviceObjGUI);
	}

	public class RemoveSSMenu extends JPopupMenu  {
		SynthesizedService ss;
		public RemoveSSMenu(ServiceObjGUI serviceObjGUI, SynthesizedService ss) {
			this.ss = ss;
			JMenuItem rem = new JMenuItem("Remove synthesised service");
			rem.addActionListener(serviceObjGUI);
			rem.setActionCommand("removeSS");
			add(rem);
		}
		
		public void show(Component comp, int x, int y) {
			super.show(comp, x, y);
		}
	}
	
	public class CommandMenu extends JPopupMenu {
		public CommandMenu(ServiceObjGUI serviceObjGUI) {
			JMenuItem addParam = new JMenuItem("Add Parameter");
			addParam.addActionListener(serviceObjGUI);
			addParam.setActionCommand("AddParam");
			JMenuItem del = new JMenuItem("Delete");
			del.setActionCommand("Delete");
			del.addActionListener(serviceObjGUI);
			add(addParam);
			add(del);
		}
	}
	public class ServiceDescriptionMenu extends JPopupMenu {
		public ServiceDescriptionMenu(ServiceObjGUI serviceObjGUI) {
			JMenuItem addCommand = new JMenuItem("Add Command");
			addCommand.addActionListener(serviceObjGUI);
			addCommand.setActionCommand("AddCommand");
			add(addCommand);
			
			JMenuItem addGroup = new JMenuItem("Add Group");
			addGroup.addActionListener(serviceObjGUI);
			addGroup.setActionCommand("AddGroup");
			add(addGroup);
			
			JMenuItem del = new JMenuItem("Delete");
			del.setActionCommand("DeleteSSD");
			del.addActionListener(serviceObjGUI);
			add(del);
		}
		public void show(Component comp, int x, int y) {
			super.show(comp, x, y);
		}
	}

}
