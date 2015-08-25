package modeloZombie;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Set;

import modeloZombie.placeable.env.EnvironmentalElement;
import modeloZombie.placeable.env.Obstacle;
import modeloZombie.utils.ObstacleType;
import sim.util.Bag;

public class Zombie extends Agent {
    private static final long serialVersionUID = 1L;

    private final static int SPEED = 2;
    protected final static int VISIBILITY_RANGE = 1;

    private Set<Agent> _activeAgents;
    private final boolean _actAgModified = false;

    /**
     * calls a place randomly in empty new location method of Zombie TODO
     * 
     * @param theModel
     */
    public Zombie(ModeloZombie theModel) {
        super(theModel, SPEED, VISIBILITY_RANGE, Color.GREEN);
        _isActive = true;
        setNonPassableOBST();

    }

    /**
     * 
     * <b>setNonPassableOBST</b> sets which obstacle is non passable
     * 
     * @param
     * @return Cristina 9 May 2011
     */
    private void setNonPassableOBST() {
        _nonPassableEnvElem.add(ObstacleType.WALL);
    }

    /**
     * <b>eat</b>
     * <p>
     * 
     * removes from the agentMap & the schedule the survivor killed by a zombie
     * 
     * @param survivorToKill_
     *            Survivor
     * @param agentMap_
     *            ObjectGrid2DExtended<Agent>
     * @return Cristina 10 May 2011
     */
    private void eat(Survivor survivorToKill_, ObjectGrid2DExtended<Agent> agentMap_) {
        if (null != survivorToKill_) {
            agentMap_.removeFromMap(survivorToKill_);
            survivorToKill_.setIsActive(false);
            System.out.println(survivorToKill_ + " was eaten!");
            survivorToKill_.getDieStoppable().stop();
        }
    }

    /**
     * <b>attack</b>
     */
    @Override
    protected void attack(ArrayList<? extends Agent> actAgent_, ObjectGrid2DExtended<Agent> agentMap_) {
        Bag vecinos = agentMap_.getAreaAroundMeByType(this, Survivor.class, VISIBILITY_RANGE);
        if (null != vecinos) {
            int survToKillIndex = RAND.nextInt(vecinos.size());
            Survivor survivorToKill = (Survivor) vecinos.get(survToKillIndex);
            eat(survivorToKill, agentMap_);
            if (survivorToKill._isActive == false) {
                actAgent_.remove(survivorToKill);
            }
        }
    }

    /**
     * 
     * <b>move</b> Moves the agent & has it interact with maps elements
     * 
     * @param
     * @return Cristina 10 May 2011
     */
    @Override
    protected void move(ArrayList<? extends Agent> actAgent_, ObjectGrid2DExtended<Agent> agentMap_,
            ObjectGrid2DExtended<EnvironmentalElement> envMap_) {
        Point2D newLoc = getNewRandLoc(agentMap_);

        if (newLoc != null) {
            boolean couldMove = false;
            if (null != envMap_.getElemFromLoc(newLoc)) {
                couldMove = interactWithElement(actAgent_, envMap_.getElemFromLoc(newLoc), agentMap_, envMap_);
            } else if ((null != agentMap_.getElemFromLoc(newLoc))
                    || ((null != agentMap_.getElemFromLoc(newLoc)) && couldMove == true)) {
                couldMove = interactWithAgent(actAgent_, agentMap_.getElemFromLoc(newLoc), agentMap_);
            } else {
                // empty cell
                couldMove = true;
            }
            if (couldMove) {
                agentMap_.moveElementTo(this, newLoc);
            }
        }

    }

    /**
     * <b>interactWithElement</b>
     * <p>
     * removes from the schedule zombies that cross a life taker obstacle
     * <p>
     * returns true if couldMove, else false
     * 
     */
    @Override
    protected boolean interactWithElement(ArrayList<? extends Agent> actAgent_, EnvironmentalElement object_,
            ObjectGrid2DExtended<Agent> agentMap_, ObjectGrid2DExtended<EnvironmentalElement> envMap_) {

        boolean couldMove = true;
        if (null != object_) {
            couldMove = interactWithPassElem(object_);
            if (object_ instanceof Obstacle) {
                if ((true == couldMove)) {
                    couldMove = RAND.nextBoolean(((Obstacle) object_).getDecreaseSpeed());
                }
                if (true == ((Obstacle) object_).isZombieLifeTaker()) {
                    agentMap_.removeFromMap(this);
                    this._dieStoppable.stop();
                }
            }
        }

        return couldMove;
    }

    @Override
    protected boolean interactWithAgent(ArrayList<? extends Agent> actAgent_, Agent agent_,
            ObjectGrid2DExtended<Agent> agentMap_) {
        boolean couldMove = true;
        if (null != agent_) {
            if (agent_ instanceof Zombie) {
                couldMove = false;
            } else if (agent_ instanceof Survivor) {
                eat((Survivor) agent_, agentMap_);
                if (_isActive == false) {
                    actAgent_.remove(agent_);
                }
                couldMove = true;
            }
        }

        return couldMove;
    }

    /**
     * /** toString console text - results of simulations
     * 
     */
    @Override
    public String toString() {
        return "Zomb. " + ID;
    }

}
