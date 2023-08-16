package StackExample;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

import DTManager.DTManager;
import DTManager.TwinConfiguration;
import DTManager.TwinSchema;
import Helpers.*;

public class Main {

	/**
	 * changes to be done for using this for the incubator
	 * 1. use different TwinSchema
	 * 2. use different Twin instantiations (createRabbitMQTwin instead of HenshinTwin, third twin, maybe one of them is enough for the deviation checking though) -> lines 27 and 28
	 * 2.1 register attributes/operations that are provided by the different DigitalTwin objects
	 * 3. not use the planner -> in the incubator, it's rather the configurator
	 * 4. change clock from event-based to time-based
	 * 5. what parameter do I want to deviation checking on?
	 * 6. what is the timespan I want to plan into the future after a deviation is identified?
	 * @param args
	 */
	public static void main(String[] args) {
		// create dt manager
		TwinSchema stackDefinitionModel = new TwinSchema();
		DigitalTwinManager twin1 = new DigitalTwinManager("stack example", stackDefinitionModel);
		// setup twins		  
		twin1.createHenshinTwin("actual", Paths.get("model", "model_five_stacks.xmi").toString(), Paths.get("model", "stack.henshin").toString());	// assumption: only one endpoint for each DT
		twin1.createHenshinTwin("expected", Paths.get("model", "model_five_stacks.xmi").toString(), Paths.get("model", "stack.henshin").toString());
		
		// initiate and start planning service
		List<OperationExecution> plan = getPlan();
		
		Thread executePlan = new Thread(() -> {
			executePlan(plan, twin1);
		});
		executePlan.start();
		// initiate and start deviation checking service
		checkForDeviation("stack1.load", twin1);
		// deviation found -> initiate and start replanning service
		try {
			executePlan.wait();
		}catch(InterruptedException ex) {}
		twin1.copyTwin("actual", "expected", null);
		executePlan.notify();
		
		Clock futureTime = twin1.getTimeFrom("actual"); 
		futureTime.increaseTime(5); // explanation: plan 5 time-periods ahead
		twin1.cloneTwin("expected", "futureActual", futureTime);
		List<OperationExecution> newPlan = getPlan();	// Assumption: the new plan is calculated before futureTime is reached
		
		
		twin1.copyTwin("actual", "expected", futureTime);	// explanation: sync actual and expected again
		// explanation: now execute the new plan instead of the old one
		try {
			executePlan.wait();
		}catch(InterruptedException ex) {}
		// execute new Plan
		executePlan = new Thread(() -> {
			executePlan(newPlan, twin1);
		});
	}
	
	
	public static void mainIncubator(String[] argus) {
		// create dt manager
		TwinSchema schema = new TwinSchema("Incubator.aasx","Incubator_AAS");
		DigitalTwinManager dtManager = new DigitalTwinManager("IncubatorManager", schema);
		// setup twins		  
		
		TwinConfiguration actualConfig = new TwinConfiguration("actual_incubator.conf");
		TwinConfiguration plantConfig = new TwinConfiguration("experimental_incubator_plant.conf");
		TwinConfiguration controlConfig = new TwinConfiguration("experimental_incubator_controller.conf");

		dtManager.createDigitalTwin("actual_incubator",actualConfig);
		dtManager.createDigitalTwin("experimental_incubator_plant",plantConfig);
		dtManager.createDigitalTwin("experimental_incubator_controller",controlConfig);
		
		
		dtManager.registerAttributes("actual_incubator",
				schema.getAttributesAASX());
		
		dtManager.registerAttributes("experimental_incubator_plant",
				schema.getAttributesAASX());
		
		dtManager.registerOperations("actual_incubator",
				schema.getOperationsAASX());
		
		dtManager.registerOperations("experimental_incubator_plant",
		schema.getOperationsAASX());
		
		// initiate and start deviation checking service
		checkForDeviation("Temperature", dtManager);
		// deviation found -> initiate and start replanning service
		String currentTemp = dtManager.getAttributeValue("Temperature", "actual_incubator");
		List<Parameter> params = new LinkedList<>();
		params.add(new Parameter("stepSize", "3.0"));
		params.add(new Parameter("initialRoomTemperature", currentTemp));
		params.add(new Parameter("initialBoxTemperature", currentTemp));
		
		dtManager.executeOperationOn("reinitializeFilter", params, new LinkedList<String>(["experimental_incubator_plant"]));
		
		Clock futureTime = twin1.getTimeFrom("experimental_incubator_plant"); 
		futureTime.increaseTime(5*1000); // explanation: plan 5 seconds ahead
		dtManager.cloneTwin("experimental_incubator_plant", "experimental_incubator_plant_future", futureTime);
		
		dtManager.executeOperationOn("heatingOperation", null, new LinkedList<String>(["experimental_incubator_plant_future"]));
		futureTime.increaseTime(3*1000);
		String expectedFutureValue = dtManager.getAttributeValueAt("Temperature", "experimental_incubator_plant_future", futureTime);
		String actualFutureValue = dtManager.getAttributeValueAt("Temperature", "actual", futureTime);
		deviationChecking(actualFutureValue, expectedFutureValue);	// compare Temperature Values after 8 seconds
	}
	
	public static void executePlan(List<OperationExecution> plan, DigitalTwinManager twinManager) {
		for(OperationExecution ex : plan) {
			twinManager.executeOperationOn(ex.getName(), ex.getParameters(), null);
		}
	}
	
	public static void checkForDeviation(String paramName, DigitalTwinManager twinManager) {
		while(true) {
			List<String> paramValues = twinManager.getAttributeValues(paramName, null);
			String lastValue = paramValues.get(0);
			for(String currentParam : paramValues) {
				if(!currentParam.equals(lastValue)) {
					return;
				}
			}
		}
	}
	
	public static List<OperationExecution> getPlan(){
		List<OperationExecution> result = null;
		OperationExecution moveLeft = new OperationExecution();
		moveLeft.setName("shiftLeft");
		moveLeft.addParameter(new Parameter("amount", "1"));
		moveLeft.addParameter(new Parameter("fromId", "1"));
		moveLeft.addParameter(new Parameter("toId", "2"));
		result.add(moveLeft);
		result.add(moveLeft);
		result.add(moveLeft);
		return result;
	}
}
