package legacy;

class DeviationChecker{
	List<Rule> ruleSet;
	List<Deviation> deviations;
	Analyzer analyzer;
	
	public DeviationChecker(List<Rule> ruleSet, Analyzer analyzer){
		this.ruleSet = ruleSet;
		this.analyzer = analyzer;
		this.deviations = new LinkedList<Deviation>();
	}
	
	public check(DigitalTwin dt){
		for(Rule rule : ruleSet){
			Deviation result = rule.apply(dt.getActual(), dt.getExpected());
			if(result != null){
				deviations.add(result);
			}
		}
		if(!deviations.isEmpty()){
			analyzer.analyse(deviations);
		}
	}
}

deviationChecker.check("stack1");
