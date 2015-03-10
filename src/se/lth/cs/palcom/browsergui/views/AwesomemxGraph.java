package se.lth.cs.palcom.browsergui.views;

import org.w3c.dom.Element;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
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
		if(cell instanceof mxCell){
			Object value = ((mxCell) cell).getValue();
			if (value instanceof Element){
				Element elt = (Element) value;
				if(elt.getAttributeNode("name") !=  null){
					return elt.getAttribute("name");
				}
			}
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
				if(elt.getAttributeNode("name") !=  null){
					return reduceNameLength(elt.getAttribute("name"));
				}
			}
		}
		return super.convertValueToString(cell);
	}		
	private String reduceNameLength(String name){
		if(name.length() > 8){
			return name.substring(0, 5) + "..";
		}
		return name;
	}
	
}
