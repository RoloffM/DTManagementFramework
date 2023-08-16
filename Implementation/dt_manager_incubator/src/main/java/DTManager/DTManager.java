package DTManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.qualifier.qualifiable.IConstraint;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.dataelement.ConnectedProperty;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.operation.ConnectedOperation;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.qualifiable.Qualifier;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.vab.manager.VABConnectionManager;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.typesafe.config.ConfigObject;

public class DTManager {
	String name;
	public TwinSchema schema;
	@Deprecated
    DigitalTwin actual;
	@Deprecated
    Map<String, DigitalTwin> experimentalTwins;
    public Map<String, DigitalTwin> availableTwins;
    Clock internalClock;
    
    
	public DTManager(String name, TwinSchema schema) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.schema = schema;
		this.availableTwins = new HashMap<String, DigitalTwin>();
		experimentalTwins = new HashMap<String, DigitalTwin>();		
	}

	public DTManager(String name, TwinConfiguration config) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.availableTwins = new HashMap<String, DigitalTwin>();
//		this.schema = schema;

		// Create Twins based on config 
		ArrayList<ConfigObject> twinProps = config.getSystemTwinProperties();
		for (ConfigObject twinProp : twinProps) {
			DigitalTwin twin = new DigitalTwin(twinProp.get("name").render(),config);
			this.availableTwins.put(twinProp.get("name").render(), twin);
			TwinSchema schema = new TwinSchema(twinProp.get("schema_file").render(),twinProp.get("name").render(), config);
			twin.setSchema(schema);
			System.out.println(twinProp.get("name").render());
		}

		
//		ArrayList<String> twinNames = config.getSystemTwinProperty("name");
//		for (String twinName : twinNames) {
//			DigitalTwin twin = new DigitalTwin(twinName,config);
//			this.availableTwins.put(twinName, twin);
//
//			// PHM: Need to get properties needed to create Schema
//
//			TwinSchema schema = new TwinSchema("Incubator.aasx","Incubator_AAS", config);
//			twin.setSchema(schema); //
//
////			this.createDigitalTwin(twinName, config);
//				System.out.println(twinName);
//			}
		
		
	}
	
	
	
	public void createDigitalTwin(String name, TwinConfiguration config) {
		DigitalTwin twin = new DigitalTwin(name,config);
		this.availableTwins.put(name, twin);
		this.schema.registerTwin(name);
	}
	
	@Deprecated
	public DigitalTwin createActualTwin(String name,TwinConfiguration config) {
		DigitalTwin twin = new DigitalTwin(name,config);
		this.actual = twin;
		return this.actual;
	}
	
	@Deprecated
	public DigitalTwin createExperimentalTwin(String name,TwinConfiguration config) {
		DigitalTwin twin = new DigitalTwin(name,config); 
		experimentalTwins.put(name,twin);
		return twin;
	}
	
	void deleteTwin(String name){
		this.availableTwins.remove(name);
	}
	
	public void copyTwin(String nameFrom, String nameTo, Clock time) {
		if(time != null && time.getNow() > getTimeFrom(nameFrom).getNow()) {
			this.waitUntil(time);
		}
		
		DigitalTwin to = this.availableTwins.get(nameTo);
		DigitalTwin from = this.availableTwins.get(nameFrom);
		for(ConnectedProperty att : this.schema.getAttributes()){
			copyAttributeValue(to, att.getIdShort(), from, att.getIdShort());
		}
		from.setTime(time);
	}
	
	void copyAttributeValue(DigitalTwin from, String fromAttribute, DigitalTwin to, String toAttribute){
		Object value = from.getAttributeValue(fromAttribute);
		to.setAttributeValue(toAttribute, value);
	}
	
	void cloneTwin(String nameFrom, String nameTo, Clock time){
		if(time != null && time.getNow() > getTimeFrom(nameFrom).getNow()) {
			this.waitUntil(time);
		}
		
		DigitalTwin from = this.availableTwins.get(nameFrom);
		this.availableTwins.put(nameTo, from);
		copyTwin(nameTo, nameFrom, null);
	}
	
	public void executeOperationOnTwins(String opName, List<?> arguments,List<String> twins) {
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
	
	public void executeOperation(String opName, List<?> arguments,String twinName) {
		DigitalTwin twin = this.availableTwins.get(twinName);
		twin.executeOperation(opName, arguments);
	}
	
	public void executeOperationAt(String opName, List<?> arguments, String twinName, Clock time) {
		if(time != null && time.getNow() > getTimeFrom(twinName).getNow()) {
			this.waitUntil(time);
		}
		DigitalTwin twin = this.availableTwins.get(twinName);
		twin.executeOperation(opName, arguments);
	}
	
	public Object getAttributeValue(String attName, String twinName) {
		DigitalTwin twin = this.availableTwins.get(twinName);
		Object value = twin.getAttributeValue(attName);
		return value;
	}
	
	public Object getAttributeValueAt(String attName, String twinName, Clock time) {
		if(time != null && time.getNow() > getTimeFrom(twinName).getNow()) {
			this.waitUntil(time);
		}
		DigitalTwin twin = this.availableTwins.get(twinName);
		Object value = twin.getAttributeValue(attName);
		return value;
	}
	
	public List<Object> getAttributeValues(String attName, List<String> twins) {
		List<String> twinsToCheck = twins;
		List<Object> values = null;
		if(twinsToCheck == null) {
			for(String temp : this.availableTwins.keySet()) {
				twinsToCheck.add(temp);
			}
		}
		for(String twin : twinsToCheck){
			DigitalTwin currentTwin = this.availableTwins.get(twin);
			Object value = currentTwin.getAttributeValue(attName);
			values.add(value);
		}
		return values;
	}
	
	public void setAttributeValue(String attrName, Object val, String twinName) {
		DigitalTwin twin = this.availableTwins.get(twinName);
		twin.setAttributeValue(attrName, val);
	}
	
	public void setAttributeValueAt(String attrName, Object val, String twinName, Clock time) {
		if(time != null && time.getNow() > getTimeFrom(twinName).getNow()) {
			this.waitUntil(time);
		}
		DigitalTwin twin = this.availableTwins.get(twinName);
		twin.setAttributeValue(attrName, val);
	}
	
	public void registerOperations(String twinName, List<Operation> operations) {
		DigitalTwin twin = this.availableTwins.get(twinName);
		/***** Missing validation of the arg operations to the existing operations ******/
		
		Map<String,IOperation> itsOperations = this.schema.getOperations(twinName);

		List<ConnectedOperation> operationsList = new ArrayList<ConnectedOperation>();
		for (Map.Entry<String, IOperation> entry : itsOperations.entrySet()) {
			IOperation op = entry.getValue();
			operationsList.add( (ConnectedOperation) op);
		}
		
		twin.registerConnectedOperations(operationsList);
	}
	
	public void registerAttributes(String twinName, List<Property> attributes) {
		DigitalTwin twin = this.availableTwins.get(twinName);
		/***** Missing validation of the arg attributes to the existing attributes ******/
		
		//this.schema.registerAttributes(String twinName, DigitalTwin twin, )
		
		Map<String,IProperty> itsProperties = this.schema.getProperties(twinName);

		List<ConnectedProperty> propertiesList = new ArrayList<ConnectedProperty>();
		for (Map.Entry<String, IProperty> entry : itsProperties.entrySet()) {
		    IProperty property = entry.getValue();
		    propertiesList.add((ConnectedProperty) property);
		}
		twin.registerConnectedAttributes(propertiesList);
	}
	
	
	// TIMING 
	public Clock getTimeFrom(String twinName) {
		DigitalTwin twin = this.availableTwins.get(twinName);
		return twin.getTime();
	}
		
	private void waitUntil(Clock time) {
		while(this.internalClock.getNow() != time.getNow()) {
			//Waits until
		}
	}
	
}
