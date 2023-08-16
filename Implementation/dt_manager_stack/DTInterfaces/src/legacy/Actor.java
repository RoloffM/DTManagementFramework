package StackExample;

class Actor{
	DTRepository repository;
	public static void main(String[] args){
		
		// 1. create actual DT
		// 1.1 create instance of data model for actual DT
		ActualStack actualStack1 = new ActualStack(); // change ActualStack to DigitalTwin in the future
		// 1.2 create PT
		PhysicalStack physicalStack1 = new PhysicalStack();
		// 1.3 create bi-directional link between actual DT and PT
		actualStack1.stack = physicalStack1;
		// 2. create expected DT
		// 2.1 create instance of data model for expected DT
		ExpectedStack expectedStack1 = new ExpectedStack();
		// 2.2 create Simulation for expected DT
		SimulationUnit.StackSimulation simulationStack1 = new SimulationUnit.StackSimulation();
		// 2.3 create bi-directional link between expected DT and simulation
		expectedStack1.stack = simulationStack1;
		// 3. link the expected and actual DT
		DigitalTwin<Stack> stack1 = new DigitalTwin<Stack>("stack1", actualStack1, expectedStack1);
		physicalStack1.stack = stack1;
		simulationStack1.stack = stack1;
		repository.add(stack1);
		
		// 1. create actual DT
		// 1.1 create instance of data model for actual DT
		ActualStack actualStack2 = new ActualStack(); // change ActualStack to DigitalTwin in the future
		// 1.2 create PT
		PhysicalStack physicalStack2 = new PhysicalStack();
		// 1.3 create bi-directional link between actual DT and PT
		actualStack2.stack = physicalStack2;
		physicalStack2.stack = actualStack2;
		// 2. create expected DT
		// 2.1 create instance of data model for expected DT
		ExpectedStack expectedStack2 = new ExpectedStack();
		// 2.2 create Simulation for expected DT
		SimulationUnit.StackSimulation simulationStack2 = new SimulationUnit.StackSimulation();
		// 2.3 create bi-directional link between expected DT and simulation
		expectedStack2.stack = simulationStack2;
		simulationStack2.stack = expectedStack2;
		// 3. link the expected and actual DT
		DigitalTwin<Stack> stack2 = new DigitalTwin<Stack>("stack2", actualStack2, expectedStack2);
		repository.add(stack2);
		
		// link stack1 and stack2 as neighbours
		expectedStack1.setRightNeighbor(expectedStack2);
		expectedStack2.setLeftNeighbor(expectedStack1);
		actualStack1.setRightNeighbor(actualStack2);
		actualStack2.setLeftNeighbor(actualStack1);
		
		
		DeviationChecker checker = new DeviationChecker (null, null);
		for(true){
			Plan p;
			for(int i = 0; i < 5; i++){
				Action a = p.getNextAction();
				actualStackSystem.execute(a);			
			}
			
		}
		
		
	}
}
