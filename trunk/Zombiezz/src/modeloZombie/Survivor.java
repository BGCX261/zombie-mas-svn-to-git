package modeloZombie;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import modeloZombie.placeable.env.EnvironmentalElement;
import modeloZombie.placeable.env.Exit;
import modeloZombie.placeable.env.Obstacle;
import modeloZombie.utils.DirectionType;
import modeloZombie.utils.MovementType;
import modeloZombie.utils.ObstacleType;
import modeloZombie.utils.PrecautionType;
import sim.util.Bag;

public class Survivor extends Agent {
    private static final long serialVersionUID = 2538131246217591829L;

    private final Map<DirectionType, Point2D> DIRECTION = new HashMap<DirectionType, Point2D>();

    private static final int MIN_VELOCIDAD_SUPERVIVIENTE = 1;
    private static final int MAX_VELOCIDAD_SUPERVIVIENTE = 2;

    // private boolean _isActive;
    private final MovementType MOVE_TYPE;
    private final PrecautionType PREC_TYP;
    private final Weapon WEAPON;
    private final double AIM;

    private Double _initDist;
    private double _bestDist;
    private double _worstDistance;

    protected final static int VIZ_RANGE = 2;

    private int _numZombiesKilled;
    private int _numZombiesMissed;
    private int _numMoves;
    private int _numEvades;
    private int _numWait;
    private final int _numOfLives;

    private boolean _hasEscaped;

    /**
     * 
     * this is the original 1-argument constructor from version 1
     * 
     * @param theModel
     *            ModeloZombie
     * @param _speed
     *            int
     * @param moveType_
     *            int
     * @param precautionType_
     *            int
     * @param weapon_
     *            Weapon
     * @param aim_
     *            double
     */
    public Survivor(ModeloZombie theModel, int speed_, MovementType moveType_, PrecautionType precautionType_,
            Weapon weapon_, double aim_) {

        super(theModel, speed_, VIZ_RANGE, Color.RED);
        setNonPassableOBST();

        _isActive = true;
        MOVE_TYPE = moveType_;
        PREC_TYP = precautionType_;
        WEAPON = weapon_;
        AIM = aim_;
        _numZombiesKilled = 0;
        _numZombiesMissed = 0;
        _numMoves = 0;
        _numEvades = 0;
        _numWait = 0;
        _hasEscaped = false;
        _numOfLives = 0;

        _initDist = Double.POSITIVE_INFINITY;
        _bestDist = _initDist;
        _worstDistance = _initDist;

        System.out.println(" Surv.: " + ID + " Mov: " + MOVE_TYPE + " Prec: " + PREC_TYP + " Weapon: " + WEAPON);

    }

    /**
     * 
     * setPassableOBST TODO purpose
     * 
     * @params
     * @return Cristina 9 May 2011
     */
    private void setNonPassableOBST() {
        _nonPassableEnvElem.add(ObstacleType.LAKE);
        _nonPassableEnvElem.add(ObstacleType.WALL);
    }

    /**
     * 
     * eat TODO purpose
     * 
     * @params
     * @return Cristina 12 May 2011
     */
    private void eat(Zombie zombToKill_, ObjectGrid2DExtended<Agent> agentMap_) {
        agentMap_.removeFromMap(zombToKill_);
        _numZombiesKilled++;
        zombToKill_.getDieStoppable().stop();
    }

    /**
     * 
     * atacarZombie computes probability of shooting a zombie
     * 
     * @params
     * @return boolean Cristina 28 Apr 2011
     */
    private boolean tryAttackZombie() {
        double probAcierto = (AIM / 100) * (WEAPON.getPrecision() / 100);
        return (RAND.nextDouble() <= probAcierto);
    }

    /**
     * <b>attack</b>
     * <p>
     * if zombies in weapon range, randomly pick one of them and try shooting it
     * 
     * @params
     * @return Cristina 17 May 2011
     */
    @Override
    protected void attack(ArrayList<? extends Agent> actAgent_, ObjectGrid2DExtended<Agent> agentMap_) {
        Bag vecinos = agentMap_.getAreaAroundMeByType(this, Zombie.class, WEAPON.getRange());
        if (null != vecinos) {
            int zombToKillIndex = RAND.nextInt(vecinos.size());
            Zombie zombToKill = (Zombie) vecinos.get(zombToKillIndex);
            boolean killableZombie = tryAttackZombie();

            if (killableZombie) {
                eat(zombToKill, agentMap_);
            } else {
                _numZombiesMissed++;
            }
        }
    }

    /**
     * 
     * <b>move</b> Moves the agent & has it interact with maps elements
     * 
     * @params
     * @return Cristina 15 May 2011
     */
    @Override
    protected void move(ArrayList<? extends Agent> actAgent_, ObjectGrid2DExtended<Agent> agentMap_,
            ObjectGrid2DExtended<EnvironmentalElement> envMap_) {
        Point2D destino = null;
        boolean avoided = false;
        Point2D thisLoc = agentMap_.getLocation(this);

        Set<EnvironmentalElement> exitSet = envMap_.getElementsOfType(Exit.class);
        if (null != exitSet) {
            for (Object objExit : exitSet) {
                if (objExit instanceof Exit) {
                    Point2D exitLoc = envMap_.getLocation((Exit) objExit);

                    destino = interactWithExit(exitLoc, agentMap_, envMap_);
                    if (destino == null) {
                        destino = obtainPossibleMove(actAgent_, exitLoc, agentMap_, envMap_);
                        if (destino != null) {
                            avoided = true;
                            _numMoves++;
                        }
                    }
                    if (destino != null) {
                        if (false == destino.equals(exitLoc)) {
                            agentMap_.moveElementTo(this, destino);
                            if (false == avoided) {
                                _numEvades++;
                            }
                        } else {
                            if (_isActive == false) {
                                actAgent_.remove(this);
                            }
                        }
                    } else {
                        _numWait++;
                    }
                }
            }
            if ((false == _hasEscaped) && (destino != null) && (true == _isActive)) {
                double dist = computeDistance(this, agentMap_, envMap_);
                if (dist < _bestDist) {
                    _bestDist = dist;
                } else if (dist > _worstDistance) {
                    _worstDistance = dist;
                }
            }
        }
    }

    /**
     * <b>obtainPossibleMove</b>
     * <p>
     * returns a possible move for the survivor after filtering all possible
     * movements by behaviour type
     * 
     * @param
     * @return Point2D
     *         <p>
     *         Cristina 9 Jun 2011
     */
    private Point2D obtainPossibleMove(ArrayList<? extends Agent> actAgent_, Point2D exitLoc_,
            ObjectGrid2DExtended<Agent> agentMap_, ObjectGrid2DExtended<EnvironmentalElement> envMap_) {

        Point2D destination = null;
        boolean couldMove = false;
        Point2D thisLoc = agentMap_.getLocation(this);

        ArrayList<Point2D> areaPosMove = filterByMovePrecType(thisLoc, exitLoc_, MOVE_TYPE, agentMap_);
        // if ((null == areaPosMove) && (false ==
        // MOVE_TYPE.equals(MOVE_TYPE.RANDOM))) {
        // areaPosMove = filterByMovePrecType(thisLoc, exitLoc,
        // MOVE_TYPE.RANDOM, agentMap_);
        // }
        if (null != areaPosMove) {
            ArrayList<Point2D> possibleLoc = new ArrayList<Point2D>();
            for (Point2D point2Move : areaPosMove) {
                couldMove = true;
                if (null != point2Move) {
                    EnvironmentalElement envObj = envMap_.getElemFromLoc(point2Move);
                    if (null != envObj && (envObj instanceof Obstacle)) {
                        couldMove = interactWithElement(actAgent_, envObj, agentMap_, envMap_);
                    }
                    if (true == couldMove) {
                        Agent agentFromMap = agentMap_.getElemFromLoc(point2Move);
                        if (true == interactWithAgent(actAgent_, agentFromMap, agentMap_)) {
                            possibleLoc.add(point2Move);
                        }
                    }
                }
            }
            if (false == possibleLoc.isEmpty()) {
                destination = possibleLoc.get(RAND.nextInt(possibleLoc.size()));
            }
        }
        return destination;
    }

    /**
     * <b>interactWithAgent</b> returns true if couldMove, else false
     * 
     * @params
     * @return boolean
     *         <p>
     *         Cristina 9 June 2011
     */
    @Override
    protected boolean interactWithAgent(ArrayList<? extends Agent> actAgent_, Agent agent_,
            ObjectGrid2DExtended<Agent> agentMap_) {

        boolean couldMove = true;
        if (null != agent_) {
            if (agent_ instanceof Survivor) {
                couldMove = false;
            } else if (agent_ instanceof Zombie) {
                eat((Zombie) agent_, agentMap_);
                couldMove = true;
            }
        }
        return couldMove;

    }

    /**
     * interactWithElement returns true if couldMove, else false
     * 
     */
    @Override
    protected boolean interactWithElement(ArrayList<? extends Agent> actAgent_, EnvironmentalElement object_,
            ObjectGrid2DExtended<Agent> agentMap_, ObjectGrid2DExtended<EnvironmentalElement> envMap_) {
        boolean couldMove = true;
        if (null != object_) {
            couldMove = interactWithPassElem(object_);
            if (true == couldMove) {
                couldMove = RAND.nextBoolean(((Obstacle) object_).getDecreaseSpeed());

                /*
                 * if (true == ((Obstacle) object_).isSurvivorLifeTaker()) {
                 * System.out.println("Surv: " + ID + " Mov: " +
                 * getMovementType() + " Prec: " + getPrecautionType() +
                 * " was killed by " + ((Obstacle) object_).getType() + " !");
                 * _isActive = false; actAgent_.remove(this);
                 * agentMap_.removeFromMap(this); this._dieStoppable.stop(); }
                 */
                return couldMove;
            }
        }
        return false;
    }

    /**
     * interactWithExit TODO purpose
     * 
     * @params
     * @return Cristina 17 May 2011
     */
    private Point2D interactWithExit(Point2D exitLoc, ObjectGrid2DExtended<Agent> agentMap_,
            ObjectGrid2DExtended<EnvironmentalElement> envMap_) {
        Point2D destino = null;
        if (null != exitLoc) {
            ArrayList<EnvironmentalElement> areaAroundMe = envMap_.getTNeighborsMaxDist(agentMap_.getLocation(this),
                    VIZ_RANGE, false, null, null, null);
            if (null != areaAroundMe) {
                for (Object obj : areaAroundMe) {
                    if (obj != null) {
                        if (obj instanceof Exit && obj.equals(envMap_.getElemFromLoc(exitLoc))) {
                            destino = exitLoc;
                            if (true == tryToExit(agentMap_, destino)) {
                                survivorsEscape(agentMap_);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return destino;
    }

    /**
     * it moves survivor in the new location(Exit.loc) on envMap
     * 
     * @params survMap_ ObjectGrid2DExtendido<Survivor>
     * @params destino_ Point2D
     * @return void
     * @author Cristina 03 May 2011
     */
    private boolean tryToExit(ObjectGrid2DExtended<Agent> agentMap_, Point2D destino_) {
        boolean moved = agentMap_.moveElementTo(this, destino_);
        if (moved) {
            _hasEscaped = true;
        }
        return moved;
    }

    /**
     * 
     * getPrecautionSettings sets visibility range values to types of precaution
     * 
     * @params
     * @return precSettings Map<PrecautionType, Integer> Cristina 3 May 2011
     */
    private Map<PrecautionType, Integer> getPrecautionSettings() {
        Map<PrecautionType, Integer> precSettings = new HashMap<PrecautionType, Integer>();
        precSettings.put(PrecautionType.CAREFREE, 0);
        precSettings.put(PrecautionType.CAUTIOUS, 1);
        precSettings.put(PrecautionType.PARANOIAC, 2);

        return precSettings;
    }

    /**
     * filterByMovePrecType TODO purpose
     * 
     * @params
     * @return Cristina 16 May 2011
     */
    private ArrayList<Point2D> filterByMovePrecType(Point2D thisLoc_, Point2D exitLoc_, MovementType moveType_,
            ObjectGrid2DExtended<Agent> agentMap_) {
        ArrayList<Point2D> areaPosMove = null;
        if (null != thisLoc_ && null != exitLoc_) {
            areaPosMove = agentMap_.getPoint2DsInRange(thisLoc_, exitLoc_, SPEED, moveType_);
            if (null != areaPosMove) {
                areaPosMove = filterByPrecaucion(agentMap_, areaPosMove);
            }
        }
        return areaPosMove;
    }

    /**
     * Create the retreat Bag of movements look for zombies in survivor
     * reachable cells if there is a zombie in the reachable cell, discard
     * movement zombie in survivor's reachable cells => don't add movement if
     * he's not cautious of zombies look for zombie at: 2 cells away if
     * paranoiac , 1 cell away if cautious stop if there's a zombie didn't reach
     * end => there is a zombie if any movements, add them to retreat movements
     * 
     * filtrarSegunPrecaucion TODO purpose
     * 
     * @params
     * @return lorelay Apr 15, 2011
     */
    private ArrayList<Point2D> filterByPrecaucion(ObjectGrid2DExtended<Agent> agentMap_, ArrayList<Point2D> coordArray_) {
        int valuePrecaution = -1;
        if (true == PREC_TYP.equals(PREC_TYP.STUPID)) {
            return coordArray_;
        } else {
            for (Entry<PrecautionType, Integer> entryset : getPrecautionSettings().entrySet()) {
                if (PREC_TYP.equals(entryset.getKey())) {
                    valuePrecaution = entryset.getValue();
                    break;
                }
            }

            ArrayList<Point2D> coordArrayRet = new ArrayList<Point2D>(coordArray_.size());
            for (Point2D coordLoc : coordArray_) {
                boolean addToRet = false;
                Object obj = agentMap_.getElemFromLoc(coordLoc);
                if (null != obj) {
                    if (obj instanceof Zombie) {
                        addToRet = false;
                    }
                } else {
                    if (valuePrecaution == 0) {
                        addToRet = true;
                    } else if (valuePrecaution > 0) {
                        Bag bAux = agentMap_.getAreaAroundMe(coordLoc, valuePrecaution - 1);
                        if (null != bAux) {
                            boolean reachedEnd = true;
                            for (Object object : bAux) {
                                if (obj instanceof Zombie) {
                                    Zombie z = (Zombie) object;
                                    if (z != null) {
                                        reachedEnd = false;
                                        break;
                                    }
                                }
                            }
                            if (false == reachedEnd) {
                                addToRet = false;
                            } else {
                                addToRet = true;
                            }
                        }
                    }
                }

                if (addToRet) {
                    coordArrayRet.add(coordLoc);
                }
            }
            return coordArrayRet;
        }
    }

    /**
     * 
     * distance computes distance from survivor's current location 'till Exit
     * location
     * 
     * @params
     * @return Cristina 5 May 2011
     */
    public double computeDistance(Survivor surv_, ObjectGrid2DExtended<Agent> agentMap_,
            ObjectGrid2DExtended<EnvironmentalElement> envMap_) {
        Double minDistance = Double.POSITIVE_INFINITY;
        Set<EnvironmentalElement> exitSet = envMap_.getElementsOfType(Exit.class);
        if (exitSet.size() > 0) {
            for (EnvironmentalElement env : exitSet) {
                Point2D pointExit = envMap_.getLocation(env);
                Point2D pointSurv = agentMap_.getLocation(surv_);
                double dist = pointExit.distance(pointSurv);
                if (dist < minDistance) {
                    minDistance = dist;
                }
            }
        } else {
            System.err.println("@Survivor.computeDistance : no Exit in map!");
        }

        return minDistance;
    }

    /**
     * survivorsEscape sets the survivor as escaped and removes it from active
     * servivors and Survivor Map
     * 
     * @params activeSurvivors_ ArrayList<? extends Agent>
     * @param agentMap_
     *            ObjectGrid2DExtendido<Agent>
     * @return void Cristina 4 May 2011
     */
    public void survivorsEscape(ObjectGrid2DExtended<Agent> agentMap_) {
        _hasEscaped = true;
        _numMoves++;
        this.setIsActive(false);
        agentMap_.removeFromMap(this);
        System.out.println(this + " saved himself!");
        getDieStoppable().stop();
    }

    /**
     * TODO
     */
    /*
     * @Override protected boolean interactWithElement(EnvironmentalElement
     * object_, ObjectGrid2DExtendido<Agent> agentMap_,
     * ObjectGrid2DExtendido<EnvironmentalElement> envMap_) { boolean move =
     * false; boolean gotOut = false; Point2D destino = null;
     * 
     * move = interactWithPassableElem(object_);
     * 
     * Bag exitLoc = envMap_.getAreaAroundMeByType(this, Exit.class,
     * VISIBILITY_RANGE); Set<EnvironmentalElement> exitLocSet; if
     * (exitLocSet.size() > 0) { for (EnvironmentalElement exitPoint :
     * exitLocSet) { gotOut = tryToExit(agentMap_, envMap_.getLocation((Exit)
     * exitPoint)); if (gotOut) { survivorsEscape(activeSurvivor_, agentMap_); }
     * } } if (false == gotOut) {
     * 
     * Point2D newLoc = getNewRandLoc(agentMap_); if
     * (!(envMap_.getElemFromLoc(newLoc) instanceof Obstacle)) { //
     * field[newX][newY] agentMap_.moveElementTo(this, new Point(newX, newY)); }
     * //
     * 
     * // TODO behaviour If Obstacle
     * 
     * if (PREC_TYP.values().equals(PREC_TYP.STUPID) ||
     * PREC_TYP.values().equals(PREC_TYP.CAREFREE)) {
     * actionIfObstacle(agentMap_, envMap_, obj, destino); destino =
     * envMap_.getLocation((EnvironmentalElement) obj); }
     * 
     * }
     * 
     * return move; }
     */

    /**
     * 
     * getHasEscaped returns whether the survivor has escaped or not
     * 
     * @params
     * @return _hasEscaped boolean Cristina 28 Apr 2011
     */
    public boolean getHasEscaped() {
        return _hasEscaped;
    }

    /**
     * isEaten sets survivor as not escaped
     * 
     * @return void Cristina 27 Apr 2011
     */
    public void isEaten() {
        _hasEscaped = false;
    }

    /**
     * 
     * getDistanciaInicial returns TODO
     * 
     * @params
     * @return INI_DIST double Cristina 27 Apr 2011
     */
    // public double getDistanciaInicial() {
    // return INI_DIST;
    // }

    /**
     * 
     * getMejorDistancia TODO purpose
     * 
     * @params
     * @return Cristina 27 Apr 2011
     */
    // public double getMejorDistancia() {
    // return BEST_DIST;
    // }

    /**
     * getVelocidad returns _speed
     * 
     * @params
     * @return int Cristina 27 Apr 2011
     */
    public int getSpeed() {
        return SPEED;
    }

    /**
     * getMovementType returns movementType
     * 
     * @params
     * @return MovementType Cristina 27 Apr 2011
     */
    public MovementType getMovementType() {
        return MOVE_TYPE;
    }

    /**
     * 
     * getModoPrecaucion returns types Of Precaution for survivor
     * 
     * @params
     * @return PrecautionType Cristina 4 May 2011
     */
    public PrecautionType getPrecautionType() {
        return PREC_TYP;
    }

    /**
     * getArma getWeapon from survivor
     * 
     * @params
     * @return Weapon Cristina 4 May 2011
     */
    public Weapon getArma() {
        return WEAPON;
    }

    /**
     * 
     * getPunteria returns Aim from survivor
     * 
     * @params
     * @return double Cristina 4 May 2011
     */
    public double getPunteria() {
        return AIM;
    }

    /**
     * getNumZombiesMatados returns Number Of Killed Zombies from survivor
     * 
     * @params
     * @return int Cristina 4 May 2011
     */
    public int getNumZombiesMatados() {
        return _numZombiesKilled;
    }

    /**
     * getNumZombiesFallados returns number of Missed Zombies from survivor
     * 
     * @params
     * @return int Cristina 4 May 2011
     */
    public int getNumZombiesMissed() {
        return _numZombiesMissed;
    }

    /**
     * getNumMovimientos number of Movements from survivor
     * 
     * @params
     * @return int Cristina 4 May 2011
     */
    public int getNumMovimientos() {
        return _numMoves;
    }

    /**
     * 
     * getNumHuidas getNrOfEscapes from survivor
     * 
     * @params
     * @return int Cristina 28 Apr 2011
     */
    public int getNumHuidas() {
        return _numEvades;
    }

    /**
     * 
     * getNumEsperas getNrOfWaits from survivor
     * 
     * @params
     * @return int Cristina 28 Apr 2011
     */
    public int getNumEsperas() {
        return _numWait;
    }

    /**
     * getNrOfLives returns how much life a survivors still has
     * 
     * @params
     * @return int Cristina 27 Apr 2011
     */
    public int getNumOfLives() {
        return _numOfLives;
    }

    /**
     * setInitDist modifies the initial distance
     * 
     * @params initDist_ Double
     * @return Cristina 6 May 2011
     */
    public final void setInitDist(Double initDist_) {
        _initDist = initDist_;
    }

    /**
     * setActive TODO purpose
     * 
     * @params
     * @return Cristina 18 May 2011
     */
    public final void setIsActive(boolean isActive_) {
        _isActive = isActive_;
    }

    /**
     * isActive TODO purpose
     * 
     * @params
     * @return Cristina 18 May 2011
     */
    public final boolean isActive() {
        return _isActive;
    }

    /**
     * toString console text - results of simulations
     * 
     */
    @Override
    public String toString() {
        return "Surv: " + ID + "\t" + getMovementType().toString().toLowerCase() + "\t"
                + getPrecautionType().toString().toLowerCase() + "\t" + getArma().toString().toLowerCase();
    }

}
