package legacy;

import java.util.List;

class DigitalTwin{
    DigitalTwin actual;
    List<DigitalTwin> experimentable;
}

interface Robot{
    public void move();
    public void stand();
    int x;
    int y;
}

class RobotDigitalTwin implements Robot extends DigitalTwin{

    RobotDigitalTwin(){
        this.actual = new ActualRobotDigitalTwin();
        this.experimentable.add(new ExpectedDigitalTwin());
    }
    public move(){
       //...
    }
}

class ActualRobotDigitalTwin implements Robot{
    public move(){
        physicalTwinRobot.moveXYZ();
    }
}

class ExpectedRobotDigitalTwin implements Robot{
    public move(){
        simulationEngine.moveXYZ()
    }
    
    public stand(){
        throw new NotImplementedExpection();
    }
}

class WorstCaseRobotDigitalTwin implements Robot{
    // ...
}



class DeviationChecker{
    private DigitalTwin actualState;
}