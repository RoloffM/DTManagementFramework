package edtincubator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.aas.registration.api.IAASRegistry;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.components.IComponent;
import org.eclipse.basyx.components.aas.AASServerComponent;
import org.eclipse.basyx.components.aas.configuration.AASServerBackend;
import org.eclipse.basyx.components.aas.configuration.BaSyxAASServerConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxMongoDBConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxMqttConfiguration;
import org.eclipse.basyx.components.configuration.BaSyxSQLConfiguration;
import org.eclipse.basyx.components.registry.RegistryComponent;
import org.eclipse.basyx.components.registry.configuration.BaSyxRegistryConfiguration;
import org.eclipse.basyx.components.registry.configuration.RegistryBackend;
import org.eclipse.basyx.components.servlet.submodel.SubmodelServlet;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.qualifier.qualifiable.IConstraint;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.submodel.metamodel.connected.ConnectedSubmodel;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.operation.ConnectedOperation;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.qualifier.qualifiable.Qualifier;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxHTTPServer;

import DTManager.TwinConfiguration;

public class AASInitializer {
	
	private static final String SERVER = "localhost";
	private static final int AAS_PORT = 4001;
	private static int EDGE_PORT = 4005;
	private static final int REGISTRY_PORT = 4000;
	private static String CONTEXT_PATH = "/aasServer"; //"/dtframework";
	private static final String AAS_CONFIG_PATH = "aas_config/aas.properties";
	private static final String AAS_CONTEXT_CONFIG_PATH = "aas_config/context.properties";
	private static final String AAS_MONGO_CONFIG_PATH = "aas_config/mongodb.properties";
	private static final String AAS_EVENTS_CONFIG_PATH = "aas_config/mqtt.properties";
	
	private static final String REGISTRY_CONFIG_PATH = "registry_config/registry.properties";
	private static final String REGISTRY_CONTEXT_CONFIG_PATH = "registry_config/context.properties";
	private static final String REGISTRY_MONGO_CONFIG_PATH = "registry_config/mongodb.properties";
	private static final String REGISTRY_EVENTS_CONFIG_PATH = "registry_config/mqtt.properties";
	private static final String DB_CONFIG_PATH = "registry_config/sql.properties";
	
	private static final String SM_EDGE_ID_SHORT = "OperationalSubmodelEdge";
	private static final String SM_EDGE_HEATING_OPERATION = "delegated_heating_operation";
	private static final String SM_EDGE_COOLING_OPERATION = "delegated_cooling_operation";
	
	
	private static IAASRegistry registry;
	public static String REGISTRYPATH;
	static String aasServerURL;
	static String aasEdgeURL;

	private static ConnectedAssetAdministrationShellManager aasManager;

	private static List<IComponent> startedComponents = new ArrayList<>();
	private static BaSyxHTTPServer edgeServer;
	
	public AASInitializer() {
		// TODO Auto-generated constructor stub
		REGISTRYPATH = "http://" + SERVER + ":" + String.valueOf(REGISTRY_PORT) + "/registry";
		registry = new AASRegistryProxy(REGISTRYPATH);
		
		aasManager = new ConnectedAssetAdministrationShellManager(registry);
		aasServerURL = "http://" + SERVER + ":" + String.valueOf(AAS_PORT) + "/" + CONTEXT_PATH;		
		aasEdgeURL = "http://"+ SERVER + ":" + EDGE_PORT + CONTEXT_PATH;

	}

	public AASInitializer(TwinConfiguration config) {
		registry = new AASRegistryProxy(config.getBasyxRegistryUrl());
		aasManager = new ConnectedAssetAdministrationShellManager(registry);
		aasServerURL = config.getBasyxServerUrl();
		EDGE_PORT = config.conf.getInt("basyx.vab_port");
		CONTEXT_PATH = config.conf.getString("basyx.vab_context_path");
	}
	
	public static void init() {
		// TODO Auto-generated constructor stub
		REGISTRYPATH = "http://" + SERVER + ":" + String.valueOf(REGISTRY_PORT) + "/registry/api/v1/registry";
		registry = new AASRegistryProxy(REGISTRYPATH);
		aasManager = new ConnectedAssetAdministrationShellManager(registry);
		aasServerURL = "http://" + SERVER + ":" + String.valueOf(AAS_PORT) + "/" + CONTEXT_PATH;
		aasEdgeURL = "http://"+ SERVER + ":" + EDGE_PORT + CONTEXT_PATH;

		//startRegistryComponent();
		//startAASComponent();
		startupEdgeServer();
	}

	public static void init(TwinConfiguration config) {
		// TODO Auto-generated constructor stub
		registry = new AASRegistryProxy(config.getBasyxRegistryUrl());
		aasManager = new ConnectedAssetAdministrationShellManager(registry);
		aasServerURL = config.getBasyxServerUrl();
		//startRegistryComponent();
		//startAASComponent();
		aasEdgeURL = config.getBasyxVabUrl();
		EDGE_PORT = config.conf.getInt("basyx.vab_port");
		CONTEXT_PATH = config.conf.getString("basyx.vab_context_path");
		startupEdgeServer();
	}

	
	private static void startRegistryComponent() {
		BaSyxContextConfiguration contextConfig = new BaSyxContextConfiguration();
		contextConfig.loadFromFile(REGISTRY_CONTEXT_CONFIG_PATH);
		BaSyxRegistryConfiguration registryConfig = new BaSyxRegistryConfiguration();
		registryConfig.loadFromFile(REGISTRY_CONFIG_PATH);
		BaSyxMongoDBConfiguration mongoConfig = new BaSyxMongoDBConfiguration();
		mongoConfig.loadFromFile(REGISTRY_MONGO_CONFIG_PATH);
		BaSyxMqttConfiguration mqttConfig = new BaSyxMqttConfiguration();
		mqttConfig.loadFromFile(REGISTRY_EVENTS_CONFIG_PATH);
		BaSyxSQLConfiguration sqlConfig = new BaSyxSQLConfiguration();
		sqlConfig.loadFromFile(DB_CONFIG_PATH);
		
		RegistryComponent registryServer = new RegistryComponent(contextConfig, mongoConfig);
		registryServer.startComponent();
		startedComponents.add(registryServer);
		

	}
	
	private static void startAASComponent() {
		BaSyxContextConfiguration contextConfig = new BaSyxContextConfiguration();
		contextConfig.loadFromFile(AAS_CONTEXT_CONFIG_PATH);
		BaSyxAASServerConfiguration aasServerConfig = new BaSyxAASServerConfiguration();
		aasServerConfig.loadFromFile(AAS_CONFIG_PATH);
		aasServerConfig.setAASBackend(AASServerBackend.MONGODB);
		BaSyxMongoDBConfiguration mongoConfig = new BaSyxMongoDBConfiguration();
		mongoConfig.loadFromFile(AAS_MONGO_CONFIG_PATH);
		BaSyxMqttConfiguration mqttConfig = new BaSyxMqttConfiguration();
		mqttConfig.loadFromFile(AAS_EVENTS_CONFIG_PATH);
		
		//AASServerComponent aasServer = new AASServerComponent(contextConfig, aasServerConfig);
		AASServerComponent aasServer = new AASServerComponent(contextConfig, aasServerConfig,mongoConfig);
		//aasServer.enableMQTT(mqttConfig);
		aasServer.startComponent();
		startedComponents.add(aasServer);

	}
	
	private static void startupEdgeServer() {

		BaSyxContextConfiguration contextConfig = new BaSyxContextConfiguration(EDGE_PORT, CONTEXT_PATH); // Edge Server for executing the operations
		BaSyxContext context = contextConfig.createBaSyxContext();
		
		/*
		 * Not required at this point
		IAssetAdministrationShell iAAS = aasManager
				.retrieveAAS(new ModelUrn("urn:dtexamples.into-cps.Incubator_AAS"));
		Map<String,ISubmodel> submodelsMap = iAAS.getSubmodels();
		Set<ISubmodel> submodels = new HashSet<ISubmodel>(submodelsMap.values());
		//ISubmodel technicalDataSubmodel = getSubmodel(submodels, "TechnicalData");
		ISubmodel operationalDataSubmodel = submodelsMap.get("OperationalData");
		*/
		
		Asset asset = new Asset("edge_asset", new CustomId("urn:dtexamples.into-cps.Edge_Asset"), AssetKind.INSTANCE);
		AssetAdministrationShell aas = new AssetAdministrationShell("edge", new CustomId("urn:dtexamples.into-cps.Edge"), asset);
		
		Submodel operationalDelegateSubmodel = new Submodel(SM_EDGE_ID_SHORT,new CustomId("operational.edge.submodel"));
		List<Operation> operations = setFunctionalities();
		for (Operation op : setFunctionalities()) {
			operationalDelegateSubmodel.addSubmodelElement(op);
		}
		aasManager.createAAS(aas, aasServerURL);
		aasManager.createSubmodel(aas.getIdentification(), operationalDelegateSubmodel);
		
		//SubmodelDescriptor descriptor = new SubmodelDescriptor(operationalDelegateSubmodel,
		//PHM		"http://"+ SERVER + ":" + EDGE_PORT + "/" + CONTEXT_PATH + "/" + SM_EDGE_ID_SHORT + "/submodel");
		SubmodelDescriptor descriptor = new SubmodelDescriptor(operationalDelegateSubmodel,
				aasEdgeURL + "/" + SM_EDGE_ID_SHORT + "/submodel");
		registry.register(aas.getIdentification(),descriptor);

		SubmodelServlet smServlet = new SubmodelServlet(operationalDelegateSubmodel);
		context.addServletMapping("/" + SM_EDGE_ID_SHORT + "/*", smServlet);

		
		
		edgeServer = new BaSyxHTTPServer(context);
		edgeServer.start();
		
		ConnectedAssetAdministrationShell connectedAAS = aasManager.retrieveAAS(new ModelUrn("urn:dtexamples.into-cps.Edge"));
		//ISubmodel tmpSubmodelEdge = aasManager.retrieveSubmodel(aas.getIdentification(),new CustomId("operational.edge.submodel"));
		ConnectedSubmodel tmpSubmodelEdge = (ConnectedSubmodel) connectedAAS.getSubmodel(new ModelUrn("operational.edge.submodel"));
		ConnectedOperation tmpOpEdge = (ConnectedOperation) tmpSubmodelEdge.getOperations().get(SM_EDGE_HEATING_OPERATION);
		
		//tmpOpEdge.invoke();
	}
	
	private static List<Operation> setFunctionalities() {
		Operation heatingOperation = new Operation(SM_EDGE_HEATING_OPERATION);

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
		
		Operation coolingOperation = new Operation(SM_EDGE_COOLING_OPERATION);

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
		operations.add(heatingOperation);
		operations.add(coolingOperation);
		return operations;
	}


}
