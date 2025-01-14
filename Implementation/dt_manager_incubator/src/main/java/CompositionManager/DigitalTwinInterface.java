package CompositionManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.basyx.aas.metamodel.api.IAssetAdministrationShell;
import org.eclipse.basyx.submodel.metamodel.api.ISubmodel;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;

public class DigitalTwinInterface {
	private IAssetAdministrationShell actual;
	private IAssetAdministrationShell experimental;
	private ISubmodel technicalDataSubmodel;
	private ISubmodel operationalDataSubmodel;
	private ISubmodel technicalDataSubmodelExperimental;
	private ISubmodel operationalDataSubmodelExperimental;
	
	public DigitalTwinInterface(IAssetAdministrationShell actual,IAssetAdministrationShell experimental) {
		this.actual = actual;
		this.experimental = experimental;
	}
	
	public IAssetAdministrationShell getActual() {
		return this.actual;
	}
	
	public IAssetAdministrationShell getexperimental() {
		return this.experimental;
	}
	
	public void setActualSubmodels(ISubmodel technicalDataSubmodel,ISubmodel operationalDataSubmodel) {
		this.technicalDataSubmodel = technicalDataSubmodel;
		this.operationalDataSubmodel = operationalDataSubmodel;
	}
	
	public void setExperimentalSubmodels(ISubmodel technicalDataSubmodel,ISubmodel operationalDataSubmodel) {
		this.technicalDataSubmodelExperimental = technicalDataSubmodel;
		this.operationalDataSubmodelExperimental = operationalDataSubmodel;
	}
	
	public Set<ISubmodel> getActualSubmodels() {
		Set<ISubmodel> set = new HashSet<ISubmodel>();
		set.add(this.technicalDataSubmodel);
		set.add(this.operationalDataSubmodel);
		return set;
	}
	
	public Set<ISubmodel> getExperimentalSubmodels() {
		Set<ISubmodel> set = new HashSet<ISubmodel>();
		set.add(this.technicalDataSubmodelExperimental);
		set.add(this.operationalDataSubmodelExperimental);
		return set;
	}
	
	public ISubmodel getActualTechnicalDataSubmodel() {
		return this.technicalDataSubmodel;
	}
	
	public ISubmodel getExperimentalTechnicalDataSubmodel() {
		return this.technicalDataSubmodelExperimental;
	}
	
	public ISubmodel getActualOperationalDataSubmodel() {
		return this.operationalDataSubmodel;
	}
	
	public ISubmodel getExperimentalOperationalDataSubmodel() {
		return this.operationalDataSubmodelExperimental;
	}
	
	public Map<String, IOperation> getActualOperations(){
		ISubmodelElement seOperations = this.getActualOperationalDataSubmodel().getSubmodelElement("Operations");
		Collection<ISubmodelElement> seOperationsCollection = (Collection<ISubmodelElement>) seOperations.getValue();
		Map<String, IOperation> operationsMap = new HashMap<String, IOperation>();
		for (ISubmodelElement op : seOperationsCollection) {
			operationsMap.put(op.getIdShort(), (IOperation) op);
		}
		return operationsMap;
	}
	
	public Map<String, IOperation> getExperimentalOperations(){
		ISubmodelElement seOperations = this.getExperimentalOperationalDataSubmodel().getSubmodelElement("Operations");
		Collection<ISubmodelElement> seOperationsCollection = (Collection<ISubmodelElement>) seOperations.getValue();
		Map<String, IOperation> operationsMap = new HashMap<String, IOperation>();
		for (ISubmodelElement op : seOperationsCollection) {
			operationsMap.put(op.getIdShort(), (IOperation) op);
		}
		return operationsMap;
	}
	
	public Map<String, IProperty> getActualOperationalProperties(){
		ISubmodelElement seVariables = this.getActualOperationalDataSubmodel().getSubmodelElement("Variables");
		Collection<ISubmodelElement> seVariablesCollection = (Collection<ISubmodelElement>) seVariables.getValue();
		Map<String, IProperty> variablesMap = new HashMap<String, IProperty>();
		for (ISubmodelElement op : seVariablesCollection) {
			variablesMap.put(op.getIdShort(), (IProperty) op);
		}
		return variablesMap;
	}
	
	public Map<String, IProperty> getExperimentalOperationalProperties(){
		ISubmodelElement seVariables = this.getExperimentalOperationalDataSubmodel().getSubmodelElement("Variables");
		Collection<ISubmodelElement> seVariablesCollection = (Collection<ISubmodelElement>) seVariables.getValue();
		Map<String, IProperty> variablesMap = new HashMap<String, IProperty>();
		for (ISubmodelElement op : seVariablesCollection) {
			variablesMap.put(op.getIdShort(), (IProperty) op);
		}
		return variablesMap;
	}
}
