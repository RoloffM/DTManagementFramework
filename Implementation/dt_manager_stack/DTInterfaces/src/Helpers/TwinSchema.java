package Helpers;

import java.util.List;

public class TwinSchema {
	
	private List<Attribute> attributes;
	private List<Operation> operations;
	
	public List<Attribute> getAttributes(){
		return this.attributes;
	}
	
	public List<Operation> getOperations(){
		return this.operations;
	}
	
	public class Attribute{
		String name;
		String type;
		
		public String getName() {
			return this.name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
	}
	
	public class Operation{
		String name;
		List<String> parameters;
		
		public void setName(String name) {
			this.name = name;
		}
	}

}
