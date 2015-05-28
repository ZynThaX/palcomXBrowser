package se.lth.cs.palcom.browsergui.views;

import com.mxgraph.model.mxCell;
import ist.palcom.resource.descriptor.VariableDecl;

/**
 * Created by jonas on 2015-04-29.
 */
public class GraphVariable {
    VariableDecl variable;
    mxCell cell;
    public mxCell setVar;
    public mxCell getVar;
    public mxCell getOut;

    public GraphVariable(VariableDecl variable, mxCell cell, mxCell setVar, mxCell getVar, mxCell getOut){
        this.variable = variable;
        this.cell = cell;
        this.setVar = setVar;
        this.getVar = getVar;
        this.getOut = getOut;
    }
}
