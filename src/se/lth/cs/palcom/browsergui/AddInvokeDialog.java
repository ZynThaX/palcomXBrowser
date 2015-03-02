package se.lth.cs.palcom.browsergui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;

public class AddInvokeDialog extends JDialog implements ActionListener {
		private JTextField txtField2;
		private JLabel lbl1;
		private JLabel lbl2;
		
		private final String[] addressTypes = {"ToAll", "uid", "Reply"};
		private final String[] valueTypes = {"Constant", "Param", "Connection"};
		
		private JComboBox list;
		private JComboBox vals;

		public AddInvokeDialog() {
			setSize(200,200);
			setLocationRelativeTo(this);
			
			GroupLayout layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			lbl1 = new JLabel();
			lbl2 = new JLabel();
			txtField2 = new JTextField();
			JButton btn1 = new JButton("Ok");
			btn1.addActionListener(this);
			
			list = new JComboBox(addressTypes);
			vals = new JComboBox(valueTypes);
			
			layout.setHorizontalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
		    		  .addComponent(lbl1)
				      .addComponent(txtField2)
				      .addComponent(lbl2)
				      .addComponent(list)
				      .addComponent(vals)
				      .addComponent(btn1)
				)
			);
			layout.setVerticalGroup(
			   layout.createSequentialGroup()
			      .addComponent(lbl1)
			      .addComponent(list, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			      .addComponent(vals, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			      .addComponent(lbl2)
			      .addComponent(txtField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			      .addComponent(btn1)
			);
		}
		
		public synchronized String[] getStrings(String lbl1, String lbl2, String def1, String def2) {
			this.lbl1.setText(lbl1);
			this.lbl2.setText(lbl2);
			
			txtField2.setText(def2);
			
			setVisible(true);
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			setVisible(false);
			return new String[] {(String) list.getSelectedItem(), txtField2.getText(), (String) vals.getSelectedItem()};
		}

		public synchronized void actionPerformed(ActionEvent ae) {
			notifyAll();
		}
	}