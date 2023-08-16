package DTManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.dataelement.ConnectedProperty;
import org.eclipse.basyx.submodel.metamodel.connected.submodelelement.operation.ConnectedOperation;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.operation.Operation;
import org.javafmi.wrapper.Simulation;



public class FMIEndpoint implements Endpoint {

	private double stepSize = 3.0;
	private TwinConfiguration twinConfig;
	private String fmuPath;
	
	Simulation simulation;
	
	@SuppressWarnings("static-access")
	public FMIEndpoint(TwinConfiguration config) {
		this.twinConfig = config;
		this.fmuPath = config.conf.getString("fmi.file_path");
		this.stepSize = config.conf.getDouble("fmi.step_size");
		String modelDescriptorPath = config.conf.getString("fmi.descriptor_path");
		simulation = new Simulation(this.fmuPath);
	}
	
	public List<Object> getAttributeValues(List<String> variables) {
		Object value = null;
		List<Object> values = new ArrayList<Object>();
		for(String var : variables) {
			value = simulation.read(var).asEnumeration();
			values.add(value);
		}
		return values;
	}
	
	public Object getAttributeValue(String variable) {
		String variableAlias = mapAlias(variable);
		double value = simulation.read(variableAlias).asDouble();
		return value;
	}
	
	public void setAttributeValues(List<String> variables,List<Double> values) {
		for(String var : variables) {
			int index = variables.indexOf(var);
			simulation.write(var).with(values.get(index));
		}
	}
	
	public void setAttributeValue(String variable,double value) {
		simulation.write(variable).with(value);
	}
	
	public void initializeSimulation(double startTime) {
		this.simulation.init(startTime);
	}
	
	private void terminateSimulation() {
		this.simulation.terminate();
	}
	
	private void doStep(double stepSize) {
		this.simulation.doStep(stepSize);
	}
	
	public void reinitializeFilter(double stepSize, double initialHeatTemperature, double initialBoxTemperature) {
		this.simulation.reset();
		this.simulation.
			write("initial_heat_temperature","initial_box_temperature").
			with(initialHeatTemperature,initialBoxTemperature);
			//this.doStep(stepSize);
	}
	
	private String mapAlias(String in) {
		String out = this.twinConfig.conf.getString("fmi.aliases." + in);
		return out;
	}

	@Override
	public void registerOperation(String name, Operation op) {
		// Not valid for this synchronous method
		
	}

	@Override
	public void registerConnectedOperation(String name, ConnectedOperation op) {
		// Not valid for this synchronous method
		
	}

	@Override
	public void registerAttribute(String name, Property prop) {
		// Not valid for this synchronous method
		
	}

	@Override
	public void registerConnectedAttribute(String name, ConnectedProperty prop) {
		// Not valid for this synchronous method
		
	}

	@Override
	public void executeOperation(String opName, List<?> arguments) {
		if (opName.equals("doStep")) {
			double stepSize = (double) arguments.get(0);
			if (arguments.size() > 1) {
				Map<String,Double> args = (Map<String, Double>) arguments.get(1);
				for (Map.Entry<String, Double> entry : args.entrySet()) {
				    this.setAttributeValue(entry.getKey(), entry.getValue());
				}
			}
			this.doStep(stepSize);
		} else if(opName.equals("terminateSimulation")) {
			this.terminateSimulation();
		} else if(opName.equals("reinitializeFilter")) {
			double stepSize = (double) arguments.get(0);
			double initialHeatTemperature = (double) arguments.get(1);
			double initialBoxTemperature = (double) arguments.get(2);
			this.reinitializeFilter(stepSize, initialHeatTemperature, initialBoxTemperature);
		}else if(opName.equals("heatingOperation")) {
			this.setAttributeValue("in_heater",1.0);
		}else if(opName.equals("coolingOperation")) {
			this.setAttributeValue("in_heater",0.0);
		}else if(opName.equals("initializeSimulation")) {
			double startTime = (double) arguments.get(0);
			this.initializeSimulation(startTime);
		}
		
	}
	
}
