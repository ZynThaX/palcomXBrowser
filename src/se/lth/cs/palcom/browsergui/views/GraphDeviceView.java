package se.lth.cs.palcom.browsergui.views;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import se.lth.cs.palcom.browsergui.views.GraphDevice.Node;
import se.lth.cs.palcom.browsergui.views.GraphDevice.NodeType;

import com.mxgraph.model.mxCell;

public class GraphDeviceView {
	
	GraphEditor ge;
	
	public GraphDeviceView(GraphEditor ge){
		this.ge = ge;
	}
	
	public AddServiceMenu createServiceMenu(ArrayList<Node> children, String id){
		return new AddServiceMenu(children, id);
	}
	
	public class AddServiceMenu extends JPopupMenu{
		public AddServiceMenu(ArrayList<Node> children, String id){
			recAddMenu(this,children,id);
		}
		
		private void recAddMenu(JComponent jm, ArrayList<Node> children, final String id){
			for(final Node n:children){
				if(n.added != true){	
					if(n.nt == NodeType.SERVICE){
						final JMenuItem item = new JMenuItem(n.name);
						item.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								ge.addVertex(id,n.name);
							}
						});
						jm.add(item);
					}else if(n.nt == NodeType.SERVICELIST){
						JMenu curr = new JMenu(n.name);
						jm.add(curr);
						recAddMenu(curr, n.children, id);
					}
				}
			}
		}
	}
}
