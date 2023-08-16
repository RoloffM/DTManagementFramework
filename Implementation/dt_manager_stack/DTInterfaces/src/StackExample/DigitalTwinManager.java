package StackExample;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

import Helpers.Clock;
import Helpers.Parameter;
import Helpers.TwinSchema;
import Helpers.TwinSchema.Attribute;

public class DigitalTwinManager{
	String name;
    Map<String, DigitalTwin> availableTwins;
    TwinSchema schema;
    Clock clock;
	// Check the initialization sequence diagram for changes
	
    
    // CRUD Operations
	public DigitalTwinManager(String name, TwinSchema definition){
		this.name = name;
		this.schema = definition;
		this.availableTwins = new HashMap<String, DigitalTwin>();
		this.clock = new Clock();
	}
    
    void createHenshinTwin(String name, final String modulePath, final String modelPath){
    	final HenshinResourceSet hrs = new HenshinResourceSet();
		hrs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		final Resource modelResource = hrs.getResource(modelPath);
		
		DigitalTwin twin = new HenshinTwin(modulePath, new EGraphImpl(modelResource));
		this.availableTwins.put(name, twin);
	}
	
	void deleteTwin(String name){
		
	}
	
	// CLONING
	void cloneTwin(String nameFrom, String nameTo, Clock time){
		if(time != null && time.getNow() > getTimeFrom(nameFrom).getNow()) {
			this.waitUntil(time);
		}
		
		DigitalTwin from = this.availableTwins.get(nameFrom);
		this.availableTwins.put(nameTo, from);
		copyTwin(nameTo, nameFrom, null);
	}
	
	void copyTwin(String nameFrom, String nameTo, Clock time){
		if(time != null && time.getNow() > getTimeFrom(nameFrom).getNow()) {
			this.waitUntil(time);
		}
		
		DigitalTwin to = this.availableTwins.get(nameTo);
		DigitalTwin from = this.availableTwins.get(nameFrom);
		for(Attribute att : this.schema.getAttributes()){
			copyAttributeValue(to, att.getName(), from, att.getName());
		}
		from.setTime(time);
	}
	
	void copyAttributeValue(DigitalTwin from, String fromAttribute, DigitalTwin to, String toAttribute){
		String v = from.getAttributeValue(fromAttribute);
		to.setAttributeValue(toAttribute, v);
	}
	
	// BX Sync
	
	void executeOperation(String twinName, String opName, List<Parameter> arguments) {
		DigitalTwin currentTwin = this.availableTwins.get(twinName);
		currentTwin.executeOperation(opName, arguments);
	}
	
	void executeOperationOn(String opName, List<Parameter> arguments, List<String> twins){
		List<String> twinsToCheck = twins;
		if(twinsToCheck == null) {
			for(String temp : this.availableTwins.keySet()) {
				twinsToCheck.add(temp);
			}
		}
		for(String twin : twinsToCheck){
			DigitalTwin currentTwin = this.availableTwins.get(twin);
			currentTwin.executeOperation(opName, arguments);
		}
	} 
	
	void executeOperationAt(String opName, List<Parameter> arguments, String twinName, Clock time) {
		if(time != null && time.getNow() > getTimeFrom(twinName).getNow()) {
			this.waitUntil(time);
		}
		DigitalTwin twin = this.availableTwins.get(twinName);
		twin.executeOperation(opName, arguments);
	}
	
	List<String> getAttributeValues(String attName, List<String> twins) {
		List<String> result = new LinkedList<String>();
		List<String> twinsToCheck = twins;
		if(twinsToCheck == null) {
			for(String temp : this.availableTwins.keySet()) {
				twinsToCheck.add(temp);
			}
		}
		
		for(String twin : twinsToCheck){
			DigitalTwin currentTwin = this.availableTwins.get(twin);
			result.add(currentTwin.getAttributeValue(attName));
		}
		
		return result;
	}
	
	String getAttributeValue(String attName, String twinName) {
		DigitalTwin currentTwin = this.availableTwins.get(twinName);
		return currentTwin.getAttributeValue(attName);
		
	}
	
	// TIMING 
	public Clock getTimeFrom(String twinName) {
		DigitalTwin twin = this.availableTwins.get(twinName);
		return twin.getTime();
	}
	
	private void waitUntil(Clock time) {
		
	}
}
