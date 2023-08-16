package StackExample;

import at.ac.tuwien.big.momot.examples.stack.stack.StackPackage;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import  org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Unit;

public class TestHenshinSimulationEngine {

	

	   public static void main(String[] args) {
		   // Load/Register package dynamically
		   StackPackage.eINSTANCE.eClass();

		   // Load xmi model and set path to Henshin module
		   final String pathToModel = Paths.get("model", "model_five_stacks.xmi").toString();
		   final  String pathToModule = Paths.get("model", "stack.henshin").toString();

		   final HenshinResourceSet hrs = new HenshinResourceSet();
		   hrs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		   final Resource modelResource = hrs.getResource(pathToModel);
		   
		   // Init engine with model resource (as EgraphImpl instance) and Henshin module
		   HenshinSimulationEngine hse = new HenshinSimulationEngine(pathToModule, new EGraphImpl(modelResource));
		   
		   boolean executed = hse.moveLeft(3, "Stack_2", "Stack_1");
		   System.out.println("Executed successfully? " + executed);
		   
		   //// Alternatively, for more general use, pass Unit + parameters to simulation engine
		   
		   // Load module from path, get unit "ShiftLeft"
		   Module module = hrs.getModule(pathToModule);
		   Unit shiftLeftUnit = module.getUnit("shiftLeft");
		   
		   // Execute unit with passed parameter map
		   executed = hse.execute(shiftLeftUnit, 
				   Map.of("amount", 3, 
						   "fromId", "Stack_2", 
						   "toId", "Stack_1"));
		   
		   System.out.println("Executed successfully? " + executed);

	   }
}
