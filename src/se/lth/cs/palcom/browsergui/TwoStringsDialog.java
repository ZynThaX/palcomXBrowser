package se.lth.cs.palcom.browsergui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class TwoStringsDialog extends JDialog implements ActionListener {
		private JTextField txtField1;
		private JTextField txtField2;
		private JLabel lbl1;
		private JLabel lbl2;
		
		
		public TwoStringsDialog() {
			setSize(200,200);
			setLocationRelativeTo(this);
			
			GroupLayout layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			lbl1 = new JLabel();
			lbl2 = new JLabel();
			txtField1 = new JTextField();
			txtField2 = new JTextField();
			
			JButton btn1 = new JButton("Ok");
			btn1.addActionListener(this);
			
			layout.setHorizontalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			    		  .addComponent(lbl1)
					      .addComponent(txtField2)
					      .addComponent(lbl2)
					      .addComponent(txtField1)
					      .addComponent(btn1)
				)
			);
			layout.setVerticalGroup(
			   layout.createSequentialGroup()
			      .addComponent(lbl1)
			      .addComponent(txtField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			      .addComponent(lbl2)
			      .addComponent(txtField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			      .addComponent(btn1)
			);
		}
		
		public synchronized String[] getStrings(String lbl1, String lbl2, String def1, String def2) {
			this.lbl1.setText(lbl1);
			this.lbl2.setText(lbl2);
			
			txtField1.setText(def1);
			txtField2.setText(def2);
			
			setVisible(true);
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			setVisible(false);
			return new String[] {txtField1.getText(), txtField2.getText()};
		}

		public synchronized void actionPerformed(ActionEvent ae) {
			notifyAll();
		}
	}