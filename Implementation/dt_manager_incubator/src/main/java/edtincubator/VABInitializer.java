package edtincubator;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.servlet.http.HttpServlet;

import org.eclipse.basyx.aas.manager.ConnectedAssetAdministrationShellManager;
import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.descriptor.SubmodelDescriptor;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.aas.registration.proxy.AASRegistryProxy;
import org.eclipse.basyx.aas.restapi.AASModelProvider;
import org.eclipse.basyx.aas.restapi.MultiSubmodelProvider;
import org.eclipse.basyx.components.configuration.BaSyxContextConfiguration;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.eclipse.basyx.submodel.restapi.SubmodelProvider;
import org.eclipse.basyx.vab.manager.VABConnectionManager;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.api.IConnectorFactory;
import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnectorFactory;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxHTTPServer;
import org.eclipse.basyx.vab.protocol.http.server.VABHTTPInterface;
import org.eclipse.basyx.vab.registry.api.IVABRegistryService;
import org.eclipse.basyx.vab.registry.memory.VABInMemoryRegistry;
import org.eclipse.basyx.vab.registry.proxy.VABRegistryProxy;
import org.eclipse.basyx.vab.registry.restapi.VABRegistryModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import DTManager.DigitalTwinInterface;
import DTManager.DTManager;
import DTManager.DigitalTwin;
import DTManager.TwinConfiguration;
import DTManager.TwinSchema;

public class VABInitializer {
	
	private static final String SERVER = "localhost";
	private static final int VAB_PORT = 4005;
	private static final int REGISTRY_PORT = 4000;
	private static final String CONTEXT_PATH = "/dtframeworkVAB";
	
	IVABRegistryService directory;
	IModelProvider directoryProvider;
	HttpServlet directoryServlet;
	IVABRegistryService directoryRegistry;
	IModelProvider directoryRegistryProvider;
	HttpServlet directoryRegistryServlet;
	public static VABConnectionManager vabConnectionManager;
	public static VABConnectionManager vabConnectionManagerVABServer;
	AASRegistryProxy registry;
	BaSyxContext context;
	
	DTManager dtManager;
	//DigitalTwin adtIncubator;
	//DigitalTwin edtIncubator;
	private static final String SM_EDGE_ID_SHORT = "OperationalSubmodelEdge";
	private static final String SM_EDGE_HEATING_OPERATION = "heating_operation";
	private static final String SM_EDGE_COOLING_OPERATION = "cooling_operation";
	
	
	public VABInitializer(DTManager dtManager) {
		this.dtManager = dtManager;
		// TODO Auto-generated constructor stub
		directory = new VABInMemoryRegistry();
		directoryProvider = new VABRegistryModelProvider(directory);
		directoryServlet = new VABHTTPInterface<IModelProvider>(directoryProvider);
		directoryRegistry = new VABInMemoryRegistry();
		directoryRegistryProvider = new VABRegistryModelProvider(directoryRegistry);
		IConnectorFactory connectorFactory = new HTTPConnectorFactory();
		directoryRegistryServlet = new VABHTTPInterface<IModelProvider>(directoryRegistryProvider);
		//vabConnectionManager = new VABConnectionManager(
		//		new VABRegistryProxy("http://localhost:4005/dtframeworkVAB/directoryRegistry/"), connectorFactory);
		// This is not yet supported by BaSyx
		vabConnectionManagerVABServer = new VABConnectionManager(
				new VABRegistryProxy(dtManager.schema.twinConfig.getBasyxVabUrl() + "/directory/"), connectorFactory);

		dtManager.schema.twinConfig.getBasyxVabUrl();
		context = settingUpContext();
	}
	
	public void init() {
		/***** Specific to VAB (AAS) *****/
		
		Asset asset = new Asset("edge_asset", new CustomId("urn:dtexamples.into-cps.Edge_Asset"), AssetKind.INSTANCE);
		AssetAdministrationShell aas = new AssetAdministrationShell("edge", new CustomId("urn:dtexamples.into-cps.Edge"), asset);
		
		Submodel operationalDelegateSubmodel = new Submodel(SM_EDGE_ID_SHORT,new CustomId("operational.edge.submodel"));
		List<Operation> operations = setFunctionalities();
		for (Operation op : setFunctionalities()) {
			operationalDelegateSubmodel.addSubmodelElement(op);
		}
		setFunctionalities();
		
		setVABInterfaces(aas,
				operationalDelegateSubmodel,
				context);
		
		
		startBaSyxVABServer(context);
		
		//dtManager.availableTwins.get("actual_incubator").setVABConnectionManager(vabConnectionManagerVABServer);
		//dtManager.availableTwins.get("experimental_incubator_plant").setVABConnectionManager(vabConnectionManagerVABServer);
		
		/***** Ends Specific to VAB (AAS) *****/
		
		
	}
	
	private void startBaSyxVABServer(BaSyxContext context) {
		/********* VAB Server *********/
		// VAB Context Server
		BaSyxHTTPServer vabServer = new BaSyxHTTPServer(context);
		vabServer.start();
	}
	
	private BaSyxContext settingUpContext() {
		// setting up the context to handle VAB invokes
		BaSyxContextConfiguration contextConfig = new BaSyxContextConfiguration();
		contextConfig.createBaSyxContext();// loadFromDefaultSource();
		contextConfig.setHostname(SERVER);
		contextConfig.setPort(VAB_PORT);
		contextConfig.setContextPath("/" + CONTEXT_PATH);
		// BaSyxContext context = contextConfig.createBaSyxContext();
		BaSyxContext context = new BaSyxContext("/" + CONTEXT_PATH, "", SERVER, VAB_PORT);

		return context;
	}
	
	private void setVABInterfaces(IAssetAdministrationShell iAAS,
			ISubmodel operationalDataSubmodel,
			BaSyxContext context) {
		
		SubmodelDescriptor descriptor = new SubmodelDescriptor(operationalDataSubmodel,
				"http://localhost:4005/dtframeworkVAB/actual_incubator/submodel");
		// Creating the providers and VAB interfaces
		AASModelProvider aasProvider = new AASModelProvider((AssetAdministrationShell) iAAS);
		SubmodelProvider aasODSMProvider = new SubmodelProvider((Submodel) operationalDataSubmodel);
		MultiSubmodelProvider aasMSMProvider = new MultiSubmodelProvider();
		aasMSMProvider.setAssetAdministrationShell(aasProvider);
		aasMSMProvider.addSubmodel(aasODSMProvider);
		HttpServlet aasServlet = new VABHTTPInterface<IModelProvider>(aasMSMProvider);

		// Adding registry mappings to VAB and Registry Server
		context.addServletMapping("/" + SM_EDGE_ID_SHORT + "/*", aasServlet);
		directory.addMapping(SM_EDGE_ID_SHORT, "http://"+ SERVER + ":" + VAB_PORT + "/" + CONTEXT_PATH + "/" + SM_EDGE_ID_SHORT);
		context.addServletMapping("/directory/*", directoryServlet);
		context.addServletMapping("/directoryRegistry/*", directoryRegistryServlet);
		// Adding submodel descriptors to Registry
		AASRegistryProxy registryProxy = new AASRegistryProxy(
				"http://" + SERVER + ":" + String.valueOf(REGISTRY_PORT) + "/registry/api/v1/registry");
		registryProxy.register(iAAS.getIdentification(), descriptor);
	}
	
	private List<Operation> setFunctionalities() {
		
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
