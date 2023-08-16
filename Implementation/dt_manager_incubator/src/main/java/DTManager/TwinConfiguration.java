package DTManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import org.eclipse.basyx.aas.metamodel.connected.ConnectedAssetAdministrationShell;

import java.io.File;

import com.typesafe.config.*;

public class TwinConfiguration {
	private Map<String, TwinSchema> twinSchemaMap = new HashMap<String, TwinSchema>(); // Mapping Twin from configuration to Twin in Schema
	public Config conf;
	
	public TwinConfiguration(String filename) {
		File file = new File(filename);   
		conf = ConfigFactory.parseFile(file);
	}
	
	public String getBasyxRegistryUrl() {
		return "http://" + this.conf.getString("basyx.registry_host") + ":" + String.valueOf(this.conf.getInt("basyx.registry_port")) + "/" + this.conf.getString("basyx.registry_path");
	}

	public String getBasyxServerUrl() {
		return "http://" + this.conf.getString("basyx.aas_host") + ":" + String.valueOf(this.conf.getInt("basyx.aas_port")) + this.conf.getString("basyx.aas_context_path");
	}

	public String getBasyxVabUrl() {
		return "http://" + this.conf.getString("basyx.vab_host") + ":" + String.valueOf(this.conf.getInt("basyx.vab_port")) + this.conf.getString("basyx.vab_context_path");
	}

	public ArrayList<String> getTwins() {
		return  new ArrayList<String>(this.conf.getStringList("system.twins"));
	}

	public ArrayList<String> getTwinNames() {	
		ArrayList<ConfigObject> twins = new ArrayList<ConfigObject>(this.conf.getObjectList("system2.twins"));
		ArrayList<String> twin_names = new ArrayList<String>();
		for (ConfigObject twin : twins) {
			twin_names.add(twin.get("name").render());
		}
		return twin_names;
	}

	public ArrayList<String> getSystemTwinProperty(String property) {	
		ArrayList<ConfigObject> twins = new ArrayList<ConfigObject>(this.conf.getObjectList("system2.twins"));
		ArrayList<String> twin_names = new ArrayList<String>();
		for (ConfigObject twin : twins) {
			twin_names.add(twin.get(property).render());
		}
		return twin_names;
	}

	public ArrayList<ConfigObject> getSystemTwinProperties() {	
		return new ArrayList<ConfigObject>(this.conf.getObjectList("system2.twins"));
	}

	
	private void configureTwins() {
		ArrayList<String> twins = new ArrayList<String>(this.conf.getStringList("system.twins"));
		for (String twin : twins) {
//			createDigitalTwin
		      System.out.println(twins);
		}
	}
}
