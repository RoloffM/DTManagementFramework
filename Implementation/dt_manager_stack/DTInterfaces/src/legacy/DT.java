package legacy;


abstract class DigitalTwin<T>{
	String name;
    T actual;
    T expected;
    
    public DigitalTwin(String name, T actual, T expected){
		this.actual = actual;
		this.expected = expected;
	}
}



abstract class Stack extends{
	private int load;
	private Stack leftNeighbor;
	private Stack rightNeighbor;
	List<Operation>
	
	
	Stack getLeftNeighbor(){
		//...
	}
	
	void setLeftNeighbor(Stack n){
		//...
	}
	
	Stack getRightNeighbor(){
		//...
	}
	
	int getLoad(){
		//...
	}
	
	void setLoad(int load){
		//...
	}
}

/*
 * This code is running on the physical device/controller
 */
class ActualStack implements Stack{
	PhysicalStack stack;
    
    public moveLeft(){
        stack.moveLeft(); // this might also simply be an API call to the API offered by the PT
    }
    
    public moveRight(){
        stack.moveRight(); // this might also simply be an API call to the API offered by the PT
    }
}

/*
 * This code is an interface provided by the simulation engine
 */
class ExpectedStack implements Stack{
	StackSimulation simulationStack;	
	
    public moveRight(){
		simulationStack.moveRight();
	}
    
    public moveLeft(){
		simulationStack.moveLeft();
	}
}
