/** Modified DNDTabbedPane.java
 * http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html
 * originally written by Terai Atsuhiro.
 * so that tabs can be transfered from one pane to another.
 * eed3si9n.
 */

package se.lth.cs.palcom.browsergui.dnd;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

public class DNDPane extends JPanel {
	public static final long serialVersionUID = 1L;
	private static final int LINEWIDTH = 3;
	private static final String NAME = "TabTransferData";
	private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
	private static GhostGlassPane s_glassPane = new GhostGlassPane();

	private boolean m_isDrawRect = false;
	private final Rectangle2D m_lineRect = new Rectangle2D.Double();

	private final Color m_lineColor = new Color(0, 100, 255);
	private TabAcceptor m_acceptor = null;

	public DNDPane() {
		super();
		final DragSourceListener dsl = new DragSourceListener() {
			public void dragEnter(DragSourceDragEvent e) {
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			public void dragExit(DragSourceEvent e) {
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				m_lineRect.setRect(0, 0, 0, 0);
				m_isDrawRect = false;
				s_glassPane.setPoint(new Point(-1000, -1000));
				s_glassPane.repaint();
			}

			public void dragOver(DragSourceDragEvent e) {
				//e.getLocation()
				//This method returns a Point indicating the cursor location in screen coordinates at the moment

				TabTransferData data = getTabTransferData(e);
				if (data == null) {
					e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
					return;
				} // if

				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			public void dragDropEnd(DragSourceDropEvent e) {
				m_isDrawRect = false;
				m_lineRect.setRect(0, 0, 0, 0);
				// m_dragTabIndex = -1;

				if (hasGhost()) {
					s_glassPane.setVisible(false);
					s_glassPane.setImage(null);
				}
			}

			public void dropActionChanged(DragSourceDragEvent e) {
			}
		};

		final DragGestureListener dgl = new DragGestureListener() {
			public void dragGestureRecognized(DragGestureEvent e) {
			
			}
		};

		//dropTarget =
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl);
		m_acceptor = new TabAcceptor() {
			public boolean isDropAcceptable(DNDPane a_component, int a_index) {
				return true;
			}
		};
	}

	public TabAcceptor getAcceptor() {
		return m_acceptor;
	}

	public void setAcceptor(TabAcceptor a_value) {
		m_acceptor = a_value;
	}

	private TabTransferData getTabTransferData(DropTargetDropEvent a_event) {		
		try {
			TabTransferData data = (TabTransferData) a_event.getTransferable().getTransferData(FLAVOR);				
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private TabTransferData getTabTransferData(DropTargetDragEvent a_event) {
		try {
			TabTransferData data = (TabTransferData) a_event.getTransferable().getTransferData(FLAVOR);				
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private TabTransferData getTabTransferData(DragSourceDragEvent a_event) {
		try {
			TabTransferData data = (TabTransferData) a_event.getDragSourceContext().getTransferable().getTransferData(FLAVOR);				
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;		
	}

	private Point buildGhostLocation(Point a_location) {
		Point retval = new Point(a_location);
		
		retval = SwingUtilities.convertPoint(DNDPane.this, retval, s_glassPane);
		return retval;
	}

	class CDropTargetListener implements DropTargetListener {
		public void dragEnter(DropTargetDragEvent e) {
			// System.out.println("DropTarget.dragEnter: " + DNDTabbedPane.this);

			if (isDragAcceptable(e)) {
				e.acceptDrag(e.getDropAction());
			} else {
				e.rejectDrag();
			} // if
		}

		public void dragExit(DropTargetEvent e) {
			//System.out.println("DropTarget.dragExit: " + DNDTabbedPane.this);
			m_isDrawRect = false;
		}

		public void dropActionChanged(DropTargetDragEvent e) {
		}

		public void dragOver(final DropTargetDragEvent e) {
			TabTransferData data = getTabTransferData(e);

			repaint();
			if (hasGhost()) {
				s_glassPane.setPoint(buildGhostLocation(e.getLocation()));
				s_glassPane.repaint();
			}
		}

		public void drop(DropTargetDropEvent a_event) {
			m_isDrawRect = false;
			repaint();
		}

		public boolean isDragAcceptable(DropTargetDragEvent e) {
			return false;
		}

		public boolean isDropAcceptable(DropTargetDropEvent e) {
			return false;
		}
	}

	private boolean m_hasGhost = true;

	public void setPaintGhost(boolean flag) {
		m_hasGhost = flag;
	}

	public boolean hasGhost() {
		return m_hasGhost;
	}

	/**
	 * returns potential index for drop.
	 * @param a_point point given in the drop site component's coordinate
	 * @return returns potential index for drop.
	 */
	

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (m_isDrawRect) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setPaint(m_lineColor);
			g2.fill(m_lineRect);
		} // if
	}

	public interface TabAcceptor {
		boolean isDropAcceptable(DNDPane a_component, int a_index);
	}

	@Override
	public void remove(int index) {
		super.remove(index);
	}
}
	
	