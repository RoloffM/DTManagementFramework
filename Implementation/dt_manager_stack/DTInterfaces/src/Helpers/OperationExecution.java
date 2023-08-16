package Helpers;

import java.util.LinkedList;
import java.util.List;

public class OperationExecution {

	private String name;
	private List<Parameter> parameters;
	
	public OperationExecution() {
		this.parameters = new LinkedList<Parameter>();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void addParameter(Parameter param) {
		this.parameters.add(param);
	}
	
	public String getName() {
		return this.name;
	}
	
	public List<Parameter> getParameters(){
		return this.parameters;
	}
}
