package se.lth.cs.palcom.browsergui;

import java.util.Collection;

import se.lth.cs.palcom.assembly.AssemblyLoadException;

public interface AssemblyDirectory {
	String[] getAssemblyNames();
	boolean isLoaded(String filename);
	boolean isRunning(String filename);
	
	//These should probably not be here
	void startAssembly(String name);
	void stopAssembly(String name);
}
