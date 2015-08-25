package modeloZombie;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import modeloZombie.placeable.env.EnvironmentalElement;
import modeloZombie.placeable.env.Obstacle;
import modeloZombie.utils.ObstacleType;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import ec.util.MersenneTwisterFast;

/**
 * TODO
 * 
 * @author lorelay
 * 
 */
public abstract class Agent implements Steppable {
    private static final long serialVersionUID = 1L;
    protected final MersenneTwisterFast RAND;
    protected final int ID;
    protected static int _agentCount = 0;
    protected static boolean _isActive = false;
    protected Set<ObstacleType> _nonPassableEnvElem;
    protected final int VISIBILITY_RANGE;
    protected int SPEED;
    protected Stoppable _dieStoppable;
    protected final Color COLOR;

    /**
     * 
     * inits an agents characteristics
     * 
     * @param theModel
     * @param speed_
     * @param visibilityRange_
     * @param color_
     * 
     */
    public Agent(ModeloZombie theModel, int speed_, int visibilityRange_, Color color_) {
	RAND = theModel.random;
	ID = _agentCount;
	_agentCount++;
	SPEED = speed_;
	VISIBILITY_RANGE = visibilityRange_;
	_nonPassableEnvElem = new HashSet<ObstacleType>();
	COLOR = color_;
    }

    /**
     * <b>setDieStoppable</b> instantiates <code>_dieStoppable</code> for the
     * agent
     * 
     * @param dieStoppable_
     *            Stoppable
     * @return Cristina 6 May 2011
     */
    public final void setDieStoppable(Stoppable dieStoppable_) {
	_dieStoppable = dieStoppable_;
    }

    /**
     * <b>getDieStoppable</b> returns <code>_dieStoppable</code>
     * 
     * @param
     * @return Cristina 6 May 2011
     */
    public final Stoppable getDieStoppable() {
	return _dieStoppable;
    }

    /**
     * calls actions an agent makes for each step: move, attack
     */
    @Override
    public void step(SimState state) {
	if (state instanceof ModeloZombie) {
	    ModeloZombie zState = (ModeloZombie) state;
	    ObjectGrid2DExtended<Agent> agentMap = zState.getAgentMap();
	    ObjectGrid2DExtended<EnvironmentalElement> envMap = zState.getEnvironmentMap();

	    move(zState.getActiveSurv(), agentMap, envMap);
	    attack(zState.getActiveSurv(), agentMap);

	}
    }

    protected abstract void attack(ArrayList<? extends Agent> actAgent_, ObjectGrid2DExtended<Agent> agentMap_);

    protected abstract void move(ArrayList<? extends Agent> actAgent_, ObjectGrid2DExtended<Agent> agentMap_,
	    ObjectGrid2DExtended<EnvironmentalElement> envMap_);

    protected abstract boolean interactWithElement(ArrayList<? extends Agent> actAgent_, EnvironmentalElement object_,
	    ObjectGrid2DExtended<Agent> agentMap_, ObjectGrid2DExtended<EnvironmentalElement> envMap_);

    protected abstract boolean interactWithAgent(ArrayList<? extends Agent> actAgent_, Agent agent_,
	    ObjectGrid2DExtended<Agent> agentMap_);

    /**
     * 
     * <b>interactWithPassElem</b>
     * 
     * returns true if <code>couldMove</code>, else false
     * 
     * @param object_
     *            EnvironmentalElement
     * @return Cristina 10 May 2011
     */
    protected boolean interactWithPassElem(EnvironmentalElement object_) {
	boolean couldMove = true;
	if (null != object_) {
	    if (object_ instanceof Obstacle) {
		Obstacle obst = (Obstacle) object_;
		couldMove = !_nonPassableEnvElem.contains(obst.getType());
	    }
	}
	return couldMove;
    }

    /**
     * <b>getNewRandLoc</b> returns the new location for a random type of walk
     * 
     * @param agentMap_
     *            ObjectGrid2DExtended<Agent>
     * @return Cristina 10 May 2011
     */
    protected Point2D getNewRandLoc(ObjectGrid2DExtended<Agent> agentMap_) {
	Point2D currLoc = agentMap_.getLocation(this);

	if (currLoc != null) {
	    int newX;
	    int newY;
	    newX = (int) currLoc.getX() + RAND.nextInt(3) - 1;
	    newY = (int) currLoc.getY() + RAND.nextInt(3) - 1;

	    if (newX < 0) {
		newX++;
	    } else if (newX == agentMap_.getWidth()) {
		newX--;
	    }
	    if (newY < 0) {
		newY++;
	    } else if (newY == agentMap_.getHeight()) {
		newY--;
	    }
	    return new Point(newX, newY);
	}
	return null;

    }

    /**
     * 
     * <b>getColor</b> returns the <code>COLOR</code> of the agent
     * 
     * @param
     * @return <code>COLOR</code> Color Cristina 29 Jun 2011
     */
    public Color getColor() {
	return COLOR;
    }

}
