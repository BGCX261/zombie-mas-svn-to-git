package modeloZombie.utils;

/**
 * TODO
 * 
 * @author Cristina
 * 
 */
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import modeloZombie.Agent;
import modeloZombie.ModeloZombie;
import modeloZombie.ObjectGrid2DExtendido;
import modeloZombie.Survivor;
import modeloZombie.Weapon;
import modeloZombie.placeable.env.EnvironmentalElement;
import modeloZombie.placeable.env.Exit;
import modeloZombie.placeable.env.Obstacle;

/**
 * TODO
 * 
 * @author Cristina
 * 
 */
public class InitTestMap {

    ModeloZombie theModel;
    // protected final MersenneTwisterFast RAND = theModel.random;
    // Maps : environment, agent
    private final ObjectGrid2DExtendido<EnvironmentalElement> _environmentMap;
    private final ObjectGrid2DExtendido<Agent> _agentMap;

    public InitTestMap(ModeloZombie theModel_) {
	theModel = theModel_;
	_environmentMap = theModel.getEnvironmentMap();
	_agentMap = theModel.getAgentMap();
    }

    public void initPresetedMap(int testNr_) {
	for (int i = 0; i < MovementType.values().length; i++) {
	    for (int j = 0; j < PrecautionType.values().length; j++) {
		for (int k = 0; k < WeaponType.values().length; k++) {
		    startSim(i, j, k);
		}
	    }

	}

    }

    public void startSim(Integer moveType_, Integer precType_, Integer weaponType_) {
	Survivor survivor = new Survivor(theModel, 2, MovementType.values()[moveType_],
	        PrecautionType.values()[precType_], new Weapon(WeaponType.values()[weaponType_]),
	        theModel.generateRandAim());
	// theModel.setActiveSurvivors(survivor);
	// theModel.setAllSurvivors(survivor, 0);
	addObstacles();
	addExits();
    }

    private void addObstacles() {

	final Integer NUM_LAKES = 2;
	final Integer NUM_MUD = 2;
	final Integer NUM_WALL = 2;

	ArrayList<Point2D> positions = new ArrayList<Point2D>();
	positions.add(new Point(25, 46));
	positions.add(new Point(85, 66));

	positions.add(new Point(45, 86));
	positions.add(new Point(65, 36));

	positions.add(new Point(69, 76));
	positions.add(new Point(84, 16));

	for (int i = 0; i < NUM_LAKES + NUM_MUD + NUM_WALL; i++) {
	    if (i < NUM_LAKES) {
		Obstacle obstacle = new Obstacle(ObstacleType.LAKE);
		_environmentMap.setEmptyBlock(positions.get(i), 6, 5, obstacle);
	    } else if (i > NUM_LAKES && i <= NUM_MUD) {
		Obstacle obstacle = new Obstacle(ObstacleType.MUD);
		_environmentMap.setEmptyBlock(positions.get(i), 6, 5, obstacle);
	    } else if (i > NUM_LAKES + NUM_MUD && i <= NUM_WALL) {
		Obstacle obstacle = new Obstacle(ObstacleType.WALL);
		_environmentMap.setEmptyBlock(positions.get(i), 6, 5, obstacle);
	    }
	}

    }

    private void addExits() {
	Point2D location = new Point(8, 26);

	Exit exit = new Exit();
	_environmentMap.addToMap(exit, location);
    }
}
