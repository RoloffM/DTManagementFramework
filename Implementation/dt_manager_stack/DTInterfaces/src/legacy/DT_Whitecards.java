package legacy;

abstract class DigitalTwin<T>{
	String name;
    ActualTwin actual;
    ExpectedTwin expected;
    
    public DigitalTwin(String name, T actual, T expected){
		this.actual = actual;
		this.expected = expected;
	}
}

abstract class Stack implements TwinType{
	public Stack(){
		Operation moveLeft = new Operation();
		this.operations.add(moveLeft);
	}
}

interface TwinType{
	List<Property> properties;
	List<Operation> operations;
}
class ExpectedTwinExecutor implements Twin{
    HenshinEngine engine;
    
    executeOperation(Operation op){
		engine.execute(op);
	}
    
}

