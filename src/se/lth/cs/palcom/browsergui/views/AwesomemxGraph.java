package se.lth.cs.palcom.browsergui.views;

import org.w3c.dom.Element;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.view.mxGraph;

public class AwesomemxGraph extends mxGraph {
	public boolean isPort(Object cell)
	{
		mxGeometry geo = getCellGeometry(cell);
		return (geo != null) ? geo.isRelative() : false;
	}
	public String getToolTipForCell(Object cell)
	{
		if (model.isEdge(cell))
		{
			return convertValueToString(model.getTerminal(cell, true)) + " -> " +
				convertValueToString(model.getTerminal(cell, false));
		}
		return super.getToolTipForCell(cell);
	}
	public boolean isCellFoldable(Object cell, boolean collapse)
	{
		return false;
	}
	public boolean isCellMovable(Object cell){
		mxCell c = ((mxCell)cell);
		return c != null && c.getParent() != null && c.getParent().getId().equals("1");
		
	}

	public String convertValueToString(Object cell){
		if (cell instanceof mxCell){
			Object value = ((mxCell) cell).getValue();
			if (value instanceof Element){
				Element elt = (Element) value;
				if (elt.getTagName().toLowerCase().contains("source") || elt.getTagName().toLowerCase().contains("target")){
					return "";
				}
			}
		}
		return super.convertValueToString(cell);
	}		
	
	
}
