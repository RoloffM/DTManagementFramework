package DTManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.components.aas.aasx.AASXPackageManager;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.qualifier.qualifiable.IConstraint;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.dataelement.ConnectedProperty;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.operation.ConnectedOperation;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.qualifiable.Qualifier;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.support.bundle.AASBundle;
import org.eclipse.basyx.vab.manager.VABConnectionManager;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.xml.sax.SAXException;

public class TwinSchema {
	
	// From the Server
	private List<ConnectedProperty> attributes;
	private List<ConnectedOperation> operations;
	// From the File
	private List<Property> attributesAASX;
	private List<Operation> operationsAASX;
	
	private Map<String, ConnectedAssetAdministrationShell> dtAASMap = new HashMap<String, ConnectedAssetAdministrationShell>();

	// Specific to AAS
	ConnectedAssetAdministrationShellManager aasManager;
	AASRegistryProxy registry;
	ConnectedAssetAdministrationShell connectedAAS;
	
	// From the Server
	public Map<String,ISubmodel> submodelsMap;
	public Set<ISubmodel> submodels;
	public ISubmodel technicalDataSubmodel;
	public ISubmodel operationalDataSubmodel;
	// From the File
	public Set<ISubmodel> submodelsAASX;
	public ISubmodel technicalDataSubmodelAASX;
	public ISubmodel operationalDataSubmodelAASX;
	// For the VAB setting
	public SubmodelDescriptor dtDescriptor;
	public VABConnectionManager vabConnectionManagerVABServer;
	public IModelProvider connectedModel;
	public IAssetAdministrationShell objectAAS;
	
	public TwinConfiguration twinConfig;
	
	private static final String SM_EDGE_ID_SHORT = "OperationalSubmodelEdge";
	private static final String SM_EDGE_HEATING_OPERATION = "delegated_heating_operation";
	private static final String SM_EDGE_COOLING_OPERATION = "delegated_cooling_operation";
	public static String TEMPSM_EDGE_ENDPOINT = "http://localhost:4005/dtframework/" + SM_EDGE_ID_SHORT + "/submodel";
	
	public static final String EDGE_HEATING_OP_URL = TEMPSM_EDGE_ENDPOINT + "/submodelElements/" + SM_EDGE_HEATING_OPERATION + "/invoke";
	public static final String EDGE_COOLING_OP_URL = TEMPSM_EDGE_ENDPOINT + "/submodelElements/" + SM_EDGE_COOLING_OPERATION + "/invoke";
	
	public TwinSchema(String schemaFileName,String aasIdShort, TwinConfiguration config) {
		this.twinConfig = config;
			
		registry = new AASRegistryProxy(this.twinConfig.getBasyxRegistryUrl());
		aasManager = new ConnectedAssetAdministrationShellManager(registry);
		TEMPSM_EDGE_ENDPOINT = this.twinConfig.getBasyxVabUrl() + "/" + SM_EDGE_ID_SHORT + "/submodel";
		
		AASXPackageManager aasxManager = new AASXPackageManager(schemaFileName);
		Set<AASBundle> bundlesActual;
		try {
			bundlesActual = aasxManager.retrieveAASBundles();
		} catch (InvalidFormatException e) {
			bundlesActual = null;
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			bundlesActual = null;
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			bundlesActual = null;
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			bundlesActual = null;
			e.printStackTrace();
		}
		
		AASBundle objectAASX = findBundle(bundlesActual, aasIdShort);
		objectAAS = objectAASX.getAAS();
		/***** Configuration *****/
		this.submodelsAASX = objectAASX.getSubmodels();
		this.technicalDataSubmodelAASX = getBundleSubmodel(submodelsAASX, "TechnicalData");
		this.operationalDataSubmodelAASX = getBundleSubmodel(submodelsAASX, "OperationalData");
		//this.submodelsMap = objectAAS.getSubmodels(); // Not valid when working from file
		//this.technicalDataSubmodel = submodelsMap.get("TechnicalData"); // Not valid when working from file
		//this.operationalDataSubmodel = submodelsMap.get("OperationalData"); // Not valid when working from file
		this.operationsAASX = this.getOperationsAASX();
		this.attributesAASX = this.getAttributesAASX();
		
		/******* Adding Functionalities to the Operations *******/
		List<Operation> functionalOperations = this.setDelegateFunctionalities(this.getMapOperationsAASX());
		//this.operationalDataSubmodelAASX;
		
		
		/***** Uploading to Server *****/
		/*uploadAAS2Server(objectAAS,
				this.technicalDataSubmodelAASX,
				this.operationalDataSubmodelAASX);*/
		
		/*****
		 * Now the AAS is in the AAS Server and Registry Server.
		 * We can now get the AAS object from the server, so it is manageable
		 *  *****/
		
		
		
		/*ConnectedAssetAdministrationShell connectedAAS = aasManager
				.retrieveAAS(new ModelUrn("urn:dtexamples.into-cps.Incubator_AAS"));
		this.submodelsMap = connectedAAS.getSubmodels();
		this.submodels = new HashSet<ISubmodel>(submodelsMap.values());
		this.technicalDataSubmodel = getSubmodel(submodels, "TechnicalData");
		this.operationalDataSubmodel = getSubmodel(submodels, "OperationalData");
		this.operations = this.getOperations();
		this.attributes = this.getAttributes();
		
		
		System.out.println(this.getMapOperations().get("heating_operation"));*/
	}
		
//	public TwinSchema(String schemaFileName,String aasIdShort) {
//		this(schemaFileName, aasIdShort, NULL); 
//	}
	
	
	public class Attribute{
		String name;
		String type;
		Property prop;
		
		public String getName() {
			return this.name;
		}
		
		public Property getProperty() {
			return this.prop;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public void setProperty(Property prop) {
			this.prop = prop;
		}
	}
	
//	private void uploadAAS2Server(IAssetAdministrationShell iAAS, ISubmodel technicalDataSubmodel,
//			ISubmodel operationalDataSubmodel) {
//		// Creation/update of AASs in BaSyx server
//		aasManager.createAAS((AssetAdministrationShell) iAAS, aasServerURL);
//		//aasManager.createSubmodel(iAAS.getIdentification(), (Submodel) technicalDataSubmodel);
//		aasManager.createSubmodel(iAAS.getIdentification(), (Submodel) operationalDataSubmodel);
//	}
	
	
	private ConnectedAssetAdministrationShell uploadAAS2Server(IAssetAdministrationShell iAAS, ISubmodel technicalDataSubmodel,
			ISubmodel operationalDataSubmodel) {
		// Creation/update of AASs in BaSyx server
		aasManager.createAAS((AssetAdministrationShell) iAAS, this.twinConfig.getBasyxServerUrl());
		//aasManager.createSubmodel(iAAS.getIdentification(), (Submodel) technicalDataSubmodel);
		aasManager.createSubmodel(iAAS.getIdentification(), (Submodel) operationalDataSubmodel);
		
		ConnectedAssetAdministrationShell cAAS = aasManager.retrieveAAS(iAAS.getIdentification());
		return cAAS;
	}
	
	
	public static AASBundle findBundle(Set<AASBundle> bundles, String aasIdShort) {
		for (AASBundle aasBundle : bundles) {
			if (aasBundle.getAAS().getIdShort().equals(aasIdShort))
				return aasBundle;
		}
		return null;
	}

	public static ISubmodel getBundleSubmodel(Set<ISubmodel> submodels, String submodelIdShort) {
		for (ISubmodel submodel : submodels) {
			if (submodel.getIdShort().equals(submodelIdShort))
				return submodel;
		}
		return null;
	}
	
	public static ISubmodel getSubmodel(Set<ISubmodel> submodels, String submodelIdShort) {
		for (ISubmodel submodel : submodels) {
			if (submodel.getIdShort().equals(submodelIdShort))
				return submodel;
		}
		return null;
	}
	
	/****** From the File  *******/
	public Map<String, IOperation> getMapOperationsAASX(){
		ISubmodelElement seOperations = this.operationalDataSubmodelAASX.getSubmodelElement("Operations");
		Collection<ISubmodelElement> seOperationsCollection = (Collection<ISubmodelElement>) seOperations.getValue();
		Map<String, IOperation> operationsMap = new HashMap<String, IOperation>();
		for (ISubmodelElement op : seOperationsCollection) {
			operationsMap.put(op.getIdShort(), (IOperation) op);
		}
		return operationsMap;
	}
	
	public List<Operation> getOperationsAASX(){
		ISubmodelElement seOperations = this.operationalDataSubmodelAASX.getSubmodelElement("Operations");
		Collection<ISubmodelElement> seOperationsCollection = (Collection<ISubmodelElement>) seOperations.getValue();
		List<Operation> operationsList = new ArrayList<Operation>();
		for (ISubmodelElement op : seOperationsCollection) {
			operationsList.add((Operation) op);
		}
		return operationsList;
	}
	
	public Map<String, IOperation> getConnectedOperationsAASX(){
		ISubmodelElement seOperations = this.operationalDataSubmodelAASX.getSubmodelElement("Operations");
		Collection<ISubmodelElement> seOperationsCollection = (Collection<ISubmodelElement>) seOperations.getValue();
		Map<String, IOperation> operationsMap = new HashMap<String, IOperation>();
		for (ISubmodelElement op : seOperationsCollection) {
			operationsMap.put(op.getIdShort(), (IOperation) op);
		}
		return operationsMap;
	}
	
	public Map<String, IProperty> getMapAttributesAASX(){
		ISubmodelElement seVariables = this.operationalDataSubmodelAASX.getSubmodelElement("Variables");
		Collection<ISubmodelElement> seVariablesCollection = (Collection<ISubmodelElement>) seVariables.getValue();
		Map<String, IProperty> variablesMap = new HashMap<String, IProperty>();
		for (ISubmodelElement op : seVariablesCollection) {
			variablesMap.put(op.getIdShort(), (IProperty) op);
		}
		return variablesMap;
	}
	
	public List<Property> getAttributesAASX(){
		ISubmodelElement seVariables = this.operationalDataSubmodelAASX.getSubmodelElement("Variables");
		Collection<ISubmodelElement> seVariablesCollection = (Collection<ISubmodelElement>) seVariables.getValue();
		List<Property> variablesList = new ArrayList<Property>();
		for (ISubmodelElement op : seVariablesCollection) {
			variablesList.add((Property) op);
		}
		return variablesList;
	}
	
	/******** From the Server ********/
	public Map<String, IOperation> getMapOperations(){
		ISubmodelElement seOperations = this.operationalDataSubmodel.getSubmodelElement("Operations");
		Collection<ISubmodelElement> seOperationsCollection = (Collection<ISubmodelElement>) seOperations.getValue();
		Map<String, IOperation> operationsMap = new HashMap<String, IOperation>();
		for (ISubmodelElement op : seOperationsCollection) {
			operationsMap.put(op.getIdShort(), (IOperation) op);
		}
		return operationsMap;
	}
	
	public List<ConnectedOperation> getOperations(){
		ISubmodelElement seOperations = this.operationalDataSubmodel.getSubmodelElement("Operations");
		Collection<ISubmodelElement> seOperationsCollection = (Collection<ISubmodelElement>) seOperations.getValue();
		List<ConnectedOperation> operationsList = new ArrayList<ConnectedOperation>();
		for (ISubmodelElement op : seOperationsCollection) {
			operationsList.add((ConnectedOperation) op);
		}
		return operationsList;
	}
	
	public Map<String, IProperty> getMapAttributes(){
		ISubmodelElement seVariables = this.operationalDataSubmodel.getSubmodelElement("Variables");
		Collection<ISubmodelElement> seVariablesCollection = (Collection<ISubmodelElement>) seVariables.getValue();
		Map<String, IProperty> variablesMap = new HashMap<String, IProperty>();
		for (ISubmodelElement op : seVariablesCollection) {
			variablesMap.put(op.getIdShort(), (IProperty) op);
		}
		return variablesMap;
	}
	
	public List<ConnectedProperty> getAttributes(){
		ISubmodelElement seVariables = this.operationalDataSubmodel.getSubmodelElement("Variables");
		Collection<ISubmodelElement> seVariablesCollection = (Collection<ISubmodelElement>) seVariables.getValue();
		List<ConnectedProperty> variablesList = new ArrayList<ConnectedProperty>();
		for (ISubmodelElement op : seVariablesCollection) {
			variablesList.add((ConnectedProperty) op);
		}
		return variablesList;
	}
	
	
	public void registerTwin(String name) {
		Asset asset = new Asset(name + "Asset", new CustomId("urn:dtexamples.into-cps." + name + "Asset"), AssetKind.INSTANCE);
		AssetAdministrationShell aas = new AssetAdministrationShell(name, new CustomId("urn:dtexamples.into-cps." + name), asset);
		Submodel operationalDataSubmodel = new Submodel("OperationalDataSubmodel", new CustomId("urn:dtexamples.into-cps." + name + ".OperationalDataSubmodel"));
		List<Operation> operations = this.getOperationsAASX();
		for (Operation op : operations) {
			operationalDataSubmodel.addSubmodelElement(op);
		}
		List<Property> properties = this.getAttributesAASX();
		for (Property prop : properties) {
			operationalDataSubmodel.addSubmodelElement(prop);
		}
		ConnectedAssetAdministrationShell cAAS = this.uploadAAS2Server(aas, null, operationalDataSubmodel);
		dtAASMap.put(name, cAAS);
	}
	
	public Map<String,IProperty> getProperties(String twinName){
		ConnectedAssetAdministrationShell cAAS = this.dtAASMap.get(twinName);
		ISubmodel submodel = cAAS.getSubmodels().get("OperationalDataSubmodel");
		return submodel.getProperties();
	}
	
	public Map<String,IOperation> getOperations(String twinName){
		ConnectedAssetAdministrationShell cAAS = this.dtAASMap.get(twinName);
		ISubmodel submodel = cAAS.getSubmodels().get("OperationalDataSubmodel");
		return submodel.getOperations();
	}

	
	//At DT level
	/*public void setVABConnectionManager(VABConnectionManager manager) {
		this.vabConnectionManagerVABServer = manager;
		this.connectedModel = this.vabConnectionManagerVABServer.connectToVABElement(dtName);
	}*/
	
	private List<Operation> setFunctionalities(Map<String, IOperation> operationsMap) {
		Operation heatingOperation = (Operation) operationsMap.get("heating_operation");

		Function<Object[], Object> heatingOperationFunction = (arguments) -> {
			// System.out.println("heating_operation from invoke");
			if (arguments.length != 0) {
				String topic = "incubator.hardware.gpio.heater.on";
				String topic_2 = "incubator.hardware.gpio.fan.off";
				String msg = "{\"heater\":true}";
				String msg_fan = "{\"fan\":false}";
				
				/*this.endpoint.send(msg,topic);
				this.endpoint.send(msg_fan,topic_2);
				
				List args = new ArrayList<>();
				args.add("{\"heater\":true}");
				args.add("{\"fan\":false}");
				this.endpoint.execute(this.name, heatingOperation, args, topic, msg);*/
				System.out.println("Functionality inside the heating operation");
			}
			return null;
		};
		heatingOperation.setInvokable(heatingOperationFunction);
		
		Operation coolingOperation = (Operation) operationsMap.get("heating_operation");

		Function<Object[], Object> coolingOperationFunction = (arguments) -> {
			// System.out.println("cooling_operation from invoke");
			if (arguments.length != 0) {
				String topic = "incubator.hardware.gpio.heater.off";
				String topic_2 = "incubator.hardware.gpio.fan.on";
				String msg = "{\"heater\":false}";
				String msg_fan = "{\"fan\":true}";
				
				/*this.endpoint.send(msg,topic);
				this.endpoint.send(msg_fan,topic_2);
				
				List args = new ArrayList<>();
				args.add("{\"heater\":false}");
				args.add("{\"fan\":true}");
				
				this.endpoint.execute(this.name, coolingOperation, args, topic, msg);*/
				System.out.println("Functionality inside the cooling operation");
				
			}
			return null;
		};
		coolingOperation.setInvokable(coolingOperationFunction);
		List<Operation> operations = new ArrayList<Operation>();
		return operations;
	}

	private List<Operation> setDelegateFunctionalities(Map<String, IOperation> operationsMap) {
		Operation heatingOperation = (Operation) operationsMap.get("heating_operation");
		Qualifier q = new Qualifier();
		q.setType("invocationDelegation");
		q.setValue(EDGE_HEATING_OP_URL);
		ArrayList<IConstraint> qualifiers = new ArrayList<>();
		qualifiers.add(q);
		heatingOperation.setQualifiers(qualifiers);
		
		Operation coolingOperation = (Operation) operationsMap.get("heating_operation");
		Qualifier qCooling = new Qualifier();
		qCooling.setType("invocationDelegation");
		qCooling.setValue(EDGE_COOLING_OP_URL);
		ArrayList<IConstraint> qualifiersCooling = new ArrayList<>();
		qualifiersCooling.add(qCooling);
		coolingOperation.setQualifiers(qualifiersCooling);
		List<Operation> operations = new ArrayList<Operation>();
		operations.add(heatingOperation);
		operations.add(coolingOperation);
		return operations;
	}
}

