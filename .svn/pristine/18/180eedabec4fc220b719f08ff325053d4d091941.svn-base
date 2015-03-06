package se.lth.cs.palcom.browsergui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JRootPane;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.MissingCDockableStrategy;
import bibliothek.gui.dock.common.intern.CControlAccess;

public class EnvironmentDockable extends DefaultSingleCDockable {
    private CControl control;
    protected String title;
    
    public EnvironmentDockable(String idd, String title, Component cmp){
        super(idd, title, cmp);
        this.title = title;
        
        setCloseable( false );
        setMinimizable( false );
        
        setLayout( new GridLayout( 1, 1 ) );
        
        control = new CControl();
        control.setMissingStrategy( MissingCDockableStrategy.STORE );
    }
    
    public CControl getEnvironmentControl(){
        return control;
    }
}
