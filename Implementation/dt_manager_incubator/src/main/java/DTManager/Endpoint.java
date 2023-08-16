package DTManager;

import java.util.List;

import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.dataelement.ConnectedProperty;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.operation.ConnectedOperation;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;

public interface Endpoint {
	public TwinConfiguration config = null;

	public void registerOperation(String name, Operation op);

	public void registerConnectedOperation(String name, ConnectedOperation op);

	public void registerAttribute(String name, Property prop);

	public void registerConnectedAttribute(String name, ConnectedProperty prop);
	
	public List<Object> getAttributeValues(List<String> variables);
	
	public Object getAttributeValue(String variable);
	
	public void setAttributeValues(List<String> variables,List<Double> values);
	
	public void setAttributeValue(String variable,double value);
	
	public void executeOperation(String opName, List<?> arguments);
	
}
