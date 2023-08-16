package StackExample;

import java.util.List;

import DTManager.Operation;
import DTManager.Property;
import Helpers.Clock;
import Helpers.Parameter;

public interface DigitalTwin{
	
	//public registerOperations(Endpoint endpoint, List<Operation> operations);
	
	//public registerAttributes(Endpoint endpoint, List<Attribute> attributes);
	
	public void registerOperations(List<Operation> operations);
	
	public void registerAttributes(List<Property> attributes);
	
	public String getAttributeValue(String attrName);
	
	public DigitalTwin getEmptyClone();
	//public Value getAttributeValueAt(String attrName, TimeStamp at);
	
	//public List<Value> getAttributeValueAt(List<String> attrNames, TimeStamp at);	// not yet used in the SDs, but should be of course also for the other get and set methods

	//public Value getAttributeValueDelta(String attrName, int numberOfEvents); // This change is to introduce discrete-event future
	// It requires a count of events instead of absolute event count to avoid sync problems
	
	public void setAttributeValue(String attrName, String val);
	
	//public void setAttributeValueAt(String attrName, Value val, TimeStamp at);
	
	// specific to discrete-time events
	// PHM: arguments should be a Dict instead, allows validation

	public String executeOperation(String opName, List<Parameter> arguments);
	
	public Clock getTime();
	
	public void setTime(Clock clock);
	
	//public Value executeOperationAt(String opName, List<Parameter> arguments, TimeStamp at);
		
	//public Value executeOperationDelta(String opName, List<Parameter> arguments, int numberOfEvents);

	
}
