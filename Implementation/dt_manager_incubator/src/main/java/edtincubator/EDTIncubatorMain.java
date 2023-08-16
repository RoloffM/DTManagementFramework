package edtincubator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import DTManager.DTManager;
import DTManager.DigitalTwin;
import DTManager.TwinConfiguration;
import DTManager.TwinSchema;
//import DTManager.createDigitalTwin;

public class EDTIncubatorMain {
	static DTManager dtManager;
	
	public static void main(String[] args) {
		TwinConfiguration actualConfig = new TwinConfiguration("actual_incubator.conf");
		TwinConfiguration plantConfig = new TwinConfiguration("experimental_incubator_plant.conf");
		TwinConfiguration controlConfig = new TwinConfiguration("experimental_incubator_controller.conf");
		TwinSchema schema = new TwinSchema("Incubator.aasx","Incubator_AAS", actualConfig);
		AASInitializer.init(actualConfig);

		dtManager = new DTManager("IncubatorManager",schema);
		dtManager.createDigitalTwin("actual_incubator",actualConfig);
		dtManager.createDigitalTwin("experimental_incubator_plant",plantConfig);
		dtManager.createDigitalTwin("experimental_incubator_controller",controlConfig);
		
	//	ArrayList<Object> twins = new ArrayList<Object>(actualConfig.conf.getObjectList("system.twins2"));
		ArrayList<String> twins = actualConfig.getSystemTwinProperty("name");
		for (Object twin : twins) {
		//	createDigitalTwin
			System.out.println(twin);
		}
		
		
		dtManager.registerAttributes("actual_incubator",
				schema.getAttributesAASX());
		
		dtManager.registerAttributes("experimental_incubator_plant",
				schema.getAttributesAASX());
		
		dtManager.registerOperations("actual_incubator",
				schema.getOperationsAASX());
		
		dtManager.registerOperations("experimental_incubator_plant",
				schema.getOperationsAASX());
		
		
		// Timer to initialize the Kalman filter at the same value as the RabbitMQ publisher
		Timer initializationTimer = new Timer();
		initializationTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				double currentRabbitMQTemperature = Double.valueOf(dtManager.getAttributeValue("Temperature", "actual_incubator").toString());
				System.out.println(currentRabbitMQTemperature);
				initializeSimulation(currentRabbitMQTemperature);				
			}
		}, 2800);
		
		// The thread is run every 3 seconds by the timer
		Thread eventThread = new Thread(() -> {
			new Timer().scheduleAtFixedRate(new TimerTask() {
				
				@Override
				public void run() {
					try {
						//dtManager.setAttributeValue("Temperature", 22.5, "experimental_incubator_plant");
						double currentActualTemperature = Double.valueOf(dtManager.getAttributeValue("Temperature","actual_incubator").toString());
						System.out.println("Current actual temperature: " + String.valueOf(currentActualTemperature));
						double currentExperimentalTemperature =  Double.valueOf(dtManager.getAttributeValue("Temperature","experimental_incubator_plant").toString());
						System.out.println("Current experimental temperature: " + String.valueOf(currentExperimentalTemperature));
						
						//Update FMI Simulation
						updateSimulation(currentActualTemperature,1.0);
						
						//Deviation checking
						deviationChecking(currentActualTemperature,currentExperimentalTemperature);
						
						//Control execution
						executeController(currentActualTemperature);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
						
				}
			}, 3000, 3000);
				
	});
	eventThread.start();
	
	/*double temperatureIn5Secs = dtManager.getAttributeValueAt("Temperature", "actual_incubator",5*1000); // RabbitMQEndpoint
	double temperatureIn5SecsExp = dtManager.getAttributeValueAt("Temperature", "experimental_incubator_plant",5*1000); // FMIEndpoint
	
	if(temperatureIn5Secs != temperatureIn5SecsExp) {
		List<Object> args = new ArrayList<Object>();
		args.add(0.85);
		dtManager.executeOperation("updateControlPolicy", args, "actual_incubator");
	}*/

	}
		
	/****** DT SERVICES *******/
	public static Map<String,Double> deviationCheckingOn(List<String> twinNames,List<String> attributes, double referenceValue, double tolerance) {
		Map<String,Double> valueMap = new HashMap<String,Double>();
		for (String attr : attributes) {
			for (String twinName : twinNames) {
				double value = Double.valueOf(dtManager.getAttributeValue(attr,twinName).toString());
				if (value > referenceValue * (1+tolerance)) {
					valueMap.put(twinName, value);
				}							
				else if(referenceValue * (1-tolerance) > value) {
					valueMap.put(twinName, value);
				}
			}
		}
		return valueMap;
	}
	
	public static void deviationChecking(double actualValue, double experimentalValue) {
		if (actualValue > experimentalValue * 1.1) {
			System.out.println("Alert: Higher temperature than expected");
		}							
		else if(0.9*experimentalValue > actualValue) {
			System.out.println("Alert: Lower temperature than expected");
		}
	}
	
	public static void executeController(double actualTemperature) {
		if (actualTemperature < 30) {
			System.out.println("Executing heating operation");
			List<String> messageArgs = new ArrayList<String>();
			messageArgs.add("{\"heater\":true}");
			messageArgs.add("{\"fan\":false}");
			dtManager.executeOperation("heating_operation", messageArgs,"actual_incubator");
			dtManager.executeOperation("heating_operation", null,"experimental_incubator_plant");

		}else if(actualTemperature > 40) {
			System.out.println("Executing cooling operation");
			List<String> messageArgs = new ArrayList<String>();
			messageArgs.add("{\"heater\":false}");
			messageArgs.add("{\"fan\":true}");
			dtManager.executeOperation("cooling_operation", messageArgs,"actual_incubator");
			dtManager.executeOperation("cooling_operation", null,"experimental_incubator_plant");
		}
	}
	
	public static void initializeSimulation(double realTemperature) {
		List<Object> arguments = new ArrayList<Object>();
		arguments.add(3.0);
		arguments.add(realTemperature);
		arguments.add(realTemperature);
		dtManager.executeOperation("reinitializeFilter", arguments, "experimental_incubator_plant");
	}
	
	public static void updateSimulation(double actualTemperature,double initialInHeater) {
		List<Object> arguments = new ArrayList<Object>();
		arguments.add(3.0);
		Map<String,Double> valueArgs = new HashMap<String,Double>();
		if (actualTemperature < 30) {
			valueArgs.put("in_heater", 1.0);
			arguments.add(valueArgs);
		}else if (actualTemperature > 40) {
			valueArgs.put("in_heater", 0.0);
			arguments.add(valueArgs);
		}else {
			valueArgs.put("in_heater", initialInHeater);
			arguments.add(valueArgs);
		}
		dtManager.executeOperation("doStep", arguments, "experimental_incubator_plant");
	}

}
