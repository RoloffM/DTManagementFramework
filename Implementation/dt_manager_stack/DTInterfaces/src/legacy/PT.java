package legacy;

class PhysicalStack {
	class StackInterface{
	private DigitalTwin<Stack> stack;
	private Robot robot;
	
	// this can/should of course be provided via some API
	public void moveLeft(int load){
		// perform this on the device(s)
		robot.take(this, load);
		robot.put(stack.getLeftNeighbor, load);
	}
	
	
	// this can/should of course be provided via some API
	public void moveRight(int load){
		// perform this on the device(s)
		robot.take(this, load);
		robot.put(stack.getRightNeighbor, load);
	}
	
	
	public void run(){
		robot = new Robot();
		robot.connect();
		for(true){
			int currentLoad = // get current load from device(s)
			stack.setLoad(currentLoad);
			Thread.wait(10);
		}
	}
	
}
