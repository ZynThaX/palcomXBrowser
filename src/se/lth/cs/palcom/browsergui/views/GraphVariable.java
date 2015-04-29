package se.lth.cs.palcom.browsergui.views;

import com.mxgraph.model.mxCell;
import ist.palcom.resource.descriptor.VariableDecl;

/**
 * Created by jonas on 2015-04-29.
 */
public class GraphVariable {
    VariableDecl variable;
    mxCell cell;
    public GraphVariable(VariableDecl variable, mxCell cell){
        this.variable = variable;
        this.cell = cell;
    }
}
