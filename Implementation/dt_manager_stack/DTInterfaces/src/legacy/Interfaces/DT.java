package Interfaces;

abstract class DigitalTwinInterface{
	String name;
    AASX actual;
    List<AASX> experimental;
    
    void setActualTwin(AASX actual){
		//...
	}
	
	void addExperimentalTwin(AASX twin){
		// ...
	}
}

interface DTMapping{
	private Endpoint endpoint;
	
	public void executeOperation();
	
	public Value getCurrentParameterValue();
	
}

class HenshinDTMapping implements DTMapping{
	endpoint = new HenshinEndpoint();
	private Map<String, ITransformationVariable> operations;
	
	public void executeOperation(String opName, List<Parameter> params){
		operations.get(opName);
		endpoint.
	}
}
