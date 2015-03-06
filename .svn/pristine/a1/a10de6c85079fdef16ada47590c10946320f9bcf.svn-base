package se.lth.cs.palcom.browsergui;

import ist.palcom.resource.descriptor.ConnectionIdUse;
import ist.palcom.resource.descriptor.ConstantUse;
import ist.palcom.resource.descriptor.ParamUse;
import ist.palcom.resource.descriptor.Use;
import ist.palcom.resource.descriptor.VariableUse;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class ParamDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 2417501716137878086L;
	
	private DefaultTreeModel model;
	private DefaultMutableTreeNode variblesNode;
	private DefaultMutableTreeNode paramsNode;
	private DefaultMutableTreeNode constantNode;
	private DefaultMutableTreeNode userIdNode;
	
	private JTree paramTree;
	private boolean aborted;

	private JButton btnCancel;
	private ParamDialog(String string, List<String> params, List<String> vars) {
		setSize(200,200);
		setLocationRelativeTo(this);
		
		setTitle(string);
		
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Options");
		variblesNode = new DefaultMutableTreeNode("Variables");
		for (String var : vars) {
			variblesNode.add(new DefaultMutableTreeNode(new VariableUse(var)));
		}
		
		paramsNode = new DefaultMutableTreeNode("Parameters");
		for (String par : params) {
			paramsNode.add(new DefaultMutableTreeNode(new ParamUse(par)));
		}
		
		userIdNode = new DefaultMutableTreeNode(new ConnectionIdUse());
		root.add(userIdNode);
		
		constantNode = new DefaultMutableTreeNode(new ConstantUse());
		root.add(constantNode);
		root.add(variblesNode);
		root.add(paramsNode);
		
		model = new DefaultTreeModel(root);
		
		paramTree = new JTree(model);
		JScrollPane sp = new JScrollPane(paramTree);
		paramTree.setCellRenderer(new Renderer());
		
		JButton btnOk = new JButton("Ok");
		btnOk.setActionCommand("DialogOK");
		btnOk.addActionListener(this);
		btnCancel = new JButton("Cancel");
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
        setVisible(true);
	}
	
	public static Use getUse(String string, List<String> params, List<String> vars) {
		ParamDialog diag = new ParamDialog(string, params, vars);
		
		synchronized (diag) {
			Use res = null;
			while (true && diag.aborted == false) {
				try {
					diag.wait();
					if (diag.aborted) {
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				TreePath sp = diag.paramTree.getSelectionPath();
				if (sp == null) {
					continue;
				}
				TreeNode tn = (TreeNode) sp.getLastPathComponent();
				if(!(tn instanceof DefaultMutableTreeNode)) {
					continue;
				}
				Object uo = ((DefaultMutableTreeNode)tn).getUserObject();
				if(uo instanceof Use) {
					res = (Use)uo;
					break;
				} else {
					continue;
				}
			}
			diag.setVisible(false);
			return res;
		}
	}
	
	public synchronized void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == btnCancel) {
			aborted = true;
		}
		notifyAll();
	}
	
	private class Renderer extends DefaultTreeCellRenderer {
		
		public Component getTreeCellRendererComponent(JTree arg0, Object value,
				boolean arg2, boolean arg3, boolean arg4, int arg5, boolean arg6) {
			
			Object val = value;
			TreeNode tn = (TreeNode) value;
			if(tn instanceof DefaultMutableTreeNode) {
				Object uo = ((DefaultMutableTreeNode)tn).getUserObject();
				if (uo instanceof ConstantUse) {
					val = "Constant";
				}
				else if(uo instanceof Use) {
					Use use = (Use)uo;
					val = use.getName();
				}
			}
			return super.getTreeCellRendererComponent(arg0, val, arg2, arg3, arg4, arg5, arg6);
		}
		
	}
	
}