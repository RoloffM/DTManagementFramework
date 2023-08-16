package StackExample;

import Interfaces.SimulationEngine;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Helpers.Clock;
import Helpers.Parameter;

import org.eclipse.emf.henshin.interpreter.Assignment;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.AssignmentImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Unit;

public class HenshinTwin implements DigitalTwin{
	// engine for execution of graph transformations
	private Engine engine;
	// module contains the available units and rules from the loaded module
	private Module module;
	private String moduleUri;
	// the graph representation of the model state
	private EGraph model;
	private Clock clock;
		
	/**
	 * 
	 * 
	 * @param moduleUri .. path to henshin module
	 * @param model .. Graph representation from model
	 */
	public HenshinTwin(final String moduleUri, final EGraph model) {
		this.model = model;
		this.moduleUri = moduleUri;
		final HenshinResourceSet hrs = new HenshinResourceSet();
		this.module = hrs.getModule(moduleUri);
		this.engine = new EngineImpl();
	}
	
	public HenshinTwin getEmptyClone() {
		HenshinTwin result = new HenshinTwin(this.moduleUri, this.model);
		return result;
	}
	
	
	/**
	 * @param unit .. Unit to execute
	 * @param parameters .. parameter mapping
	 * @return true if execution was successful, false otherwise
	 */
	@Override
	public String executeOperation(String opName, List<Parameter> arguments) {
		final Unit unit = this.module.getUnit(opName);
		final UnitApplication application = new UnitApplicationImpl(engine, model, unit, null);

		for(Parameter param : arguments) {
			application.setParameterValue(param.getName(), param.getValue());
		}
	
		boolean executed = application.execute(null);
		
		return "" + executed;
	}
	
	@Override
	public String getAttributeValue(String attrName) {
		// TODO: get attribute from this.model
		// TODO: get and return current attribute value
		return null;
	}
	
	public void setAttributeValue(String attrName, String val){
		// TODO: get attribute from this.model
		// TODO: change attribute value here
	}
	
	public void setTime(Clock clock) {
		this.clock = clock;
	}
	
	public Clock getTime() {
		return this.clock;
	}
	
	/**
	 * @param load .. load to shift
	 * @param fromId .. id of stack to shift from
	 * @param toId .. id of stack to shift to
	 * @return true if execution was successful, false otherwise
	 */
	public boolean moveLeft(int load, String fromId, String toId){
		final Unit unit = this.module.getUnit("shiftLeft");

		final UnitApplication application = new UnitApplicationImpl(engine, model, unit, null);
	
		application.setParameterValue("amount", load);
		application.setParameterValue("fromId", fromId);
		application.setParameterValue("toId", toId);
		
		boolean executed = application.execute(null);
		return executed;
	}

		
//		@Outdated
//		public moveRight(int load){
//			Stack param1 = expectedStack;
//			Stack param2 = expectedStack.getRightNeighbor();
//			// third parameter is the load
//			
//			ITransformationVariable var = null // set this to moveRight using on the method parameters
//			final Unit unit = this.module.getUnit(var.getUnit().getName());
//
//			final UnitApplication application = new UnitApplicationImpl(engine, mre.getGraph(), unit, null);
//
//			for(final Parameter param : var.getUnit().getParameters()) {
//				application.setParameterValue(param.getName(), var.getParameterValue(param));
//			}
//			application.execute(null);
//			int currentLoad = // get updated load from Simulation
//			expectedStack.setLoad(currentLoad);
//		}
//	}
}
