package modeloZombie;

// in this version, I hacked something together
// so that all the bugs will move before any of them
// grow.

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import modeloZombie.placeable.env.EnvironmentalElement;
import modeloZombie.placeable.env.Exit;
import modeloZombie.placeable.env.Health;
import modeloZombie.placeable.env.Obstacle;
import modeloZombie.utils.MovementType;
import modeloZombie.utils.ObstacleType;
import modeloZombie.utils.PrecautionType;
import modeloZombie.utils.WeaponType;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import Interface.SimResultsUI;
import ec.util.MersenneTwisterFast;

public class ModeloZombie extends SimState {
    //
    private static final long serialVersionUID = -1861173824789019848L;
    // min & max survivor speed

    private static final int ENV_WIDTH = 100;
    private static final int ENV_LENGTH = 100;

    private static final Integer NUM_EXIT = 1;
    private static final Integer NUM_HEALTH = 2;

    private static final Integer NUM_LAKES = 7;
    private static final Integer NUM_MUD = 4;
    private static final Integer NUM_WALL = 7;
    private static final Integer DIM_OBS = 15;

    // TypesOfMovement
    private MovementType _movement;
    private PrecautionType _precaution;
    // private WeaponType _weapon;

    // Maps : environment, agent
    private ObjectGrid2DExtended<EnvironmentalElement> _environmentMap;
    private ObjectGrid2DExtended<Agent> _agentMap;

    // 'log' files of the simulations
    private final String SIM_EVOLV_FILE_NAME = "output/z_partida_evol.txt";
    private final String SIM_RESULT_FILE_NAME = "output/z_partida_results.txt";
    private final String SURV_RESULT_FILE_NAME = "output/z_superv_results.txt";
    private PrintWriter _ficheroEvolucionPartida = null;
    private PrintWriter _ficheroResultadosPartida = null;
    private PrintWriter _fileSurvivorResults = null;

    private final int SURV_MIN_SPEED = 1;
    private final int SURV_MAX_SPEED = 2;
    private static final int INIT_NR_SURVIV = 1;
    private static final int INIT_NR_ZOMBIES = 19;
    private static final int NR_INCR_ZOMBIES = 1;
    private final int MAX_STEPS = 2 * (ENV_WIDTH + ENV_LENGTH);

    SimResultsUI simResultsFrame;

    private ArrayList<Survivor> _activeSurvivors;
    private Survivor[] _allSurvivors;
    private ArrayList<Zombie> _zombies;

    // For BENCHMARK
    private final static boolean BENCHMARK_MODE = true;
    private int nextMovementType = 0;
    private int nextPrecautionType = 0;
    private int nextWeapon = 0;
    private final static int BENCHMARK_SPEED = 2;
    private final static double BENCHMARK_AIM = 75.0;
    private final Integer BENCHMARK_SURV_VS = 3;
    private final Integer BENCHMARK_MAP_VS = 4;
    private Integer _survVs = 0;
    private Integer _mapVs = 0;

    private int _nrSimulationes = MovementType.values().length * PrecautionType.values().length
            * WeaponType.values().length * BENCHMARK_MAP_VS * BENCHMARK_SURV_VS * 100;

    /**
     * 
     * TODO
     * 
     * @param seed
     */
    public ModeloZombie(long seed) {
        super(new MersenneTwisterFast(seed), new Schedule());
    }

    /**
     * 
     * buildSchedule schedules Steppable events TODO purpose
     * <p>
     * build a step that makes all the agents move.
     * 
     * @params
     * @return lorelay Apr 15, 2011
     */
    public void buildSchedule() {
        for (Survivor sup : _activeSurvivors) {
            sup.setDieStoppable(schedule.scheduleRepeating(sup));
        }
        for (Zombie zom : _zombies) {
            zom.setDieStoppable(schedule.scheduleRepeating(zom));
        }
        schedule.scheduleRepeating(0, 3, new Steppable() {
            @Override
            public void step(SimState state) {
                for (Zombie zom : addMoreZombies()) {
                    zom.setDieStoppable(schedule.scheduleRepeating(zom));
                }
            }
        });
        schedule.scheduleRepeating(0, 4, new Steppable() {
            @Override
            public void step(SimState state) {
                outputData();
            }
        });
        schedule.scheduleRepeating(0, 5, new Steppable() {
            @Override
            public void step(SimState state) {
                checkIfDone(state);
            }
        });
    }

    /**
     * finish closes the files where the simulation results were written
     * 
     * @params
     * @return lorelay Apr 26, 2011
     */
    @Override
    public void finish() {
        super.finish();

        _fileSurvivorResults.close();
        _ficheroResultadosPartida.close();
        _ficheroEvolucionPartida.close();
    }

    /**
     * 
     * reset TODO purpose
     * 
     * @params
     * @return Cristina 27 Apr 2011
     */
    public void reset() {
        super.finish();
        // TODO
        resetSimConfig();
        super.start();
        startSimulation();
        if (null != simResultsFrame) {
            simResultsFrame.disposeOfFrame();
        }
    }

    /**
     * 
     */
    @Override
    public void start() {
        super.start();
        startSimulation();
        try {
            _ficheroEvolucionPartida = new PrintWriter(new FileWriter(SIM_EVOLV_FILE_NAME, true));
            _ficheroResultadosPartida = new PrintWriter(new FileWriter(SIM_RESULT_FILE_NAME, true));
            _fileSurvivorResults = new PrintWriter(new FileWriter(SURV_RESULT_FILE_NAME, true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null != simResultsFrame) {
            simResultsFrame.disposeOfFrame();
        }
    }

    /**
     * startSimulation initializes space, e TODO
     * 
     * @params
     * @return Cristina 28 Apr 2011
     */
    private void startSimulation() {
        Map<ObstacleType, Integer> obstacles = getObstacleSettings();

        initSpaces();
        if (BENCHMARK_MODE) {
            addPreSetObstacles();
            addPreSetExit();
            makePreSetSurvivors();

        } else {
            addObstaclesRand(obstacles);
            addHealthRand(NUM_HEALTH);
            addExitRand(NUM_EXIT);

            System.out.println("--------------------------");
            generateRandSurvivors(INIT_NR_SURVIV);
            placeRandomly(_activeSurvivors, _agentMap);
        }
        System.out.println("--------------------------");
        for (Survivor surv : _activeSurvivors) {
            surv.setInitDist(surv.computeDistance(surv, _agentMap, _environmentMap));
        }

        generateZombies(INIT_NR_ZOMBIES);
        placeRandomly(_zombies, _agentMap);

        buildSchedule();

    }

    /**
     * resetSimConfig TODO purpose
     * 
     * @param
     * @return Cristina 30 Jun 2011
     */
    private void resetSimConfig() {

        if (++nextMovementType == MovementType.values().length) {
            nextMovementType = 0;
            if (++nextPrecautionType == PrecautionType.values().length) {
                nextPrecautionType = 0;
                if (++nextWeapon == WeaponType.values().length) {
                    nextWeapon = 0;
                    if (++_survVs == BENCHMARK_SURV_VS) {
                        _survVs = 0;
                        if (++_mapVs == BENCHMARK_MAP_VS) {
                            _mapVs = 0;
                        }
                    }
                }
            }
        }
    }

    /**
     * 
     * <b>generateRandSurvivors</b>
     * <p>
     * generates a number of <code>numSurv_</code> of random type of
     * Survivors(speed, movement, precaution, weapon, aim)
     * 
     * @param numSurv_
     *            int
     * 
     * @return void lorelay Apr 15, 2011
     */
    private void generateRandSurvivors(int numSurv_) {
        _activeSurvivors = new ArrayList<Survivor>(numSurv_);
        _allSurvivors = new Survivor[numSurv_];

        for (int i = 0; i < numSurv_; i++) {
            int randMovIdx = random.nextInt(MovementType.values().length);
            int randPrecIdx = random.nextInt(PrecautionType.values().length);
            int randTypIdx = random.nextInt(WeaponType.values().length);

            Survivor survivor = new Survivor(this, generateRandSpeed(), MovementType.values()[randMovIdx],
                    PrecautionType.values()[randPrecIdx], new Weapon(WeaponType.values()[randTypIdx]),
                    generateRandAim());
            _activeSurvivors.add(survivor);
            _allSurvivors[i] = survivor;
        }
    }

    /**
     * 
     * <b>makePreSetSurvivors</b> generates a preset type of survivor
     * 
     * @return void Cristina 14 Jun 2011
     */
    private void makePreSetSurvivors() {
        // We select the parameters of the survivor depending on if we're in
        // benchmark mode or not
        int speed = BENCHMARK_SPEED;
        int movIdx = nextMovementType;
        int precIdx = nextPrecautionType;
        int weaponIdx = nextWeapon;
        double aim = BENCHMARK_AIM;
        int numSurv_ = 0;

        ArrayList<Point2D> survLoc = new ArrayList<Point2D>();
        if (_survVs == 0) {
            survLoc.add(new Point(70, 70));
            numSurv_ = 1;
        } else if (_survVs == 1) {
            // surv together
            survLoc.add(new Point(70, 70));
            survLoc.add(new Point(70, 69));
            survLoc.add(new Point(69, 69));
            survLoc.add(new Point(69, 70));
            numSurv_ = 4;
        } else if (_survVs == 2) {
            // surv spread around
            survLoc.add(new Point(10, 10));
            survLoc.add(new Point(10, 90));
            survLoc.add(new Point(90, 10));
            survLoc.add(new Point(90, 90));
            numSurv_ = 4;
        } else {
            throw new IllegalArgumentException("_survVs not valid");
        }

        _activeSurvivors = new ArrayList<Survivor>();
        _allSurvivors = new Survivor[numSurv_];

        for (int i = 0; i < numSurv_; i++) {
            Survivor survivor = new Survivor(this, speed, MovementType.values()[movIdx],
                    PrecautionType.values()[precIdx], new Weapon(WeaponType.values()[weaponIdx]), aim);
            _activeSurvivors.add(survivor);
            _allSurvivors[i] = survivor;

            _agentMap.addToMap(survivor, survLoc.get(i));

        }

    }

    /**
     * generateZombies TODO purpose
     * 
     * @params
     * @param numZombies_
     *            TODO
     * @return Cristina 28 Apr 2011
     */
    private void generateZombies(int numZombies_) {
        _zombies = new ArrayList<Zombie>(numZombies_);
        for (int i = 0; i < numZombies_; i++) {
            _zombies.add(new Zombie(this));
        }
    }

    /**
     * initSpaces TODO purpose
     * 
     * @params
     * @return Cristina 28 Apr 2011
     */
    private void initSpaces() {
        _environmentMap = new ObjectGrid2DExtended<EnvironmentalElement>(ENV_WIDTH, ENV_LENGTH, random);
        _agentMap = new ObjectGrid2DExtended<Agent>(ENV_WIDTH, ENV_LENGTH, random);
    }

    /**
     * 
     * addHealth adds a health object to the environment space
     * 
     * @params numHealth_ int
     * @return void Cristina 27 Apr 2011
     */
    private void addHealthRand(int numHealth_) {
        for (int i = 0; i < numHealth_; i++) {
            Health health = new Health();
            _environmentMap.addToMap(health, _environmentMap.getRandEmptyLocation());
        }
    }

    /**
     * 
     * addObstacles adds different types of obstacles to the environment Map at
     * a random location
     * 
     * @params obsSetting_ Map<ObstacleType, Integer>
     * @return void Cristina 26 Apr 2011
     */
    private void addObstaclesRand(Map<ObstacleType, Integer> obsSetting_) {

        // TEST of functionality
        // EnvironmentalElement obst = new Obstacle(ObstacleType.LAKE);
        // _environmentMap.addToMap(obst, new Point(1, 1));
        // System.out.println(_environmentMap.getElemFromLoc(new Point(1, 1)));
        // _environmentMap.removeFromMap(obst);
        // System.out.println(_environmentMap.addToRandEmptyLoc(obst));
        // _environmentMap.removeFromMap(obst);
        // till here

        for (Entry<ObstacleType, Integer> entryset : obsSetting_.entrySet()) {
            for (int i = 0; i < entryset.getValue(); i++) {
                Obstacle obstacle = new Obstacle(entryset.getKey());
                _environmentMap.setEmptyBlock(random.nextInt(DIM_OBS) + 6, random.nextInt(DIM_OBS) + 5, obstacle);
            }
        }

    }

    /**
     * 
     * getObstacleSettings returns the number of each type of obstacle that is
     * needed
     * 
     * @params
     * @return obsSettings Map<ObstacleType, Integer> Cristina 26 Apr 2011
     */
    private Map<ObstacleType, Integer> getObstacleSettings() {
        Map<ObstacleType, Integer> obsSettings = new HashMap<ObstacleType, Integer>();
        obsSettings.put(ObstacleType.LAKE, NUM_LAKES);
        obsSettings.put(ObstacleType.MUD, NUM_MUD);
        obsSettings.put(ObstacleType.WALL, NUM_WALL);

        return obsSettings;
    }

    /**
     * 
     * addExits adds Exit type to the environment Map at a random location
     * 
     * @param numExit_
     *            int
     * @return lorelay Apr 15, 2011
     */
    private void addExitRand(int numExit_) {
        for (int i = 0; i < numExit_; i++) {
            Exit exit = new Exit();
            _environmentMap.addToRandEmptyLoc(exit);
        }
    }

    /**
     * 
     * <b>addPreSetObstacles</b> adds preseted obstacles according to the map
     * version <code>mapVs_</code>
     * 
     * @params mapVs_ Integer
     * @return void Cristina 14 Jun 2011
     */
    private void addPreSetObstacles() {

        final Integer obstDim = 8;
        ArrayList<Point2D> positions = new ArrayList<Point2D>();

        if (_mapVs == 0) {
            return;
        } else if (_mapVs > 0) {
            if (_survVs == 2) {
                if (_mapVs == 1 || _mapVs == 2) {
                    positions.add(new Point(17, 25));
                    positions.add(new Point(17, 75));
                    positions.add(new Point(25, 83));
                    positions.add(new Point(25, 17));

                    positions.add(new Point(75, 17));
                    positions.add(new Point(75, 83));
                    positions.add(new Point(83, 25));
                    positions.add(new Point(83, 75));

                    positions.add(new Point(25, 25));
                    positions.add(new Point(25, 75));
                    positions.add(new Point(75, 25));
                    positions.add(new Point(75, 75));

                    // positions.add(new Point(9, 25));
                    // positions.add(new Point(25, 9));
                    // positions.add(new Point(9, 75));
                    // positions.add(new Point(75, 9));
                    //
                    // positions.add(new Point(25, 91));
                    // positions.add(new Point(91, 25));
                    // positions.add(new Point(75, 91));
                    // positions.add(new Point(91, 75));
                }

                if (_mapVs == 3) {
                    positions.add(new Point(30, 38));
                    positions.add(new Point(30, 62));
                    positions.add(new Point(38, 30));
                    positions.add(new Point(62, 30));

                    positions.add(new Point(38, 70));
                    positions.add(new Point(62, 70));
                    positions.add(new Point(70, 38));
                    positions.add(new Point(70, 62));

                    positions.add(new Point(62, 38));
                    positions.add(new Point(38, 62));
                    positions.add(new Point(62, 62));
                    positions.add(new Point(38, 38));

                    positions.add(new Point(22, 38));
                    positions.add(new Point(22, 62));
                    positions.add(new Point(38, 22));
                    positions.add(new Point(62, 22));

                    positions.add(new Point(38, 78));
                    positions.add(new Point(62, 78));
                    positions.add(new Point(78, 38));
                    positions.add(new Point(78, 62));

                    positions.add(new Point(24, 24));
                    positions.add(new Point(24, 76));
                    positions.add(new Point(76, 76));
                    positions.add(new Point(76, 24));
                }
            } else if (_survVs == 0 || _survVs == 1) {
                if (_mapVs == 1 || _mapVs == 2) {
                    // positions.add(new Point(50, 66));
                    positions.add(new Point(50, 58));
                    positions.add(new Point(50, 50));
                    positions.add(new Point(58, 50));
                    // positions.add(new Point(66, 50));
                }
                if (_mapVs == 3) {
                    positions.add(new Point(41, 57));
                    positions.add(new Point(41, 49));
                    positions.add(new Point(41, 41));
                    positions.add(new Point(49, 41));
                    positions.add(new Point(57, 41));

                    positions.add(new Point(57, 57));
                }

            }
        }
        Obstacle obstacle = null;

        if (_mapVs == 1 || _mapVs == 3) {
            obstacle = new Obstacle(ObstacleType.MUD);
        } else if (_mapVs == 2) {
            obstacle = new Obstacle(ObstacleType.LAKE);
        } else {
            throw new IllegalArgumentException("_mapVs not valid value");
        }

        for (Point2D point : positions) {

            _environmentMap.setEmptyBlockAtCenter(point, obstDim, obstDim, obstacle);

        }

    }

    /**
     * 
     * <b>addPreSetExit</b> adds preseted exit to the environmental map
     * 
     * @params
     * @return void Cristina 14 Jun 2011
     */
    private void addPreSetExit() {
        Point2D location;
        if (_survVs == 0 || _survVs == 1) {
            location = new Point(30, 30);
        } else if (_survVs == 2) {
            location = new Point(50, 50);
        } else {
            throw new IllegalArgumentException("_survVs invalid value");
        }

        Exit exit = new Exit();
        _environmentMap.addToMap(exit, location);
    }

    /**
     * 
     * <b>addPreSetHealth</b> adds preseted health to the environmental map
     * 
     * @params
     * @return void Cristina 14 Jun 2011
     */
    private void addPreSetHealth() {
        Point2D location = new Point(79, 36);

        Health health = new Health();
        _environmentMap.addToMap(health, location);
    }

    /**
     * 
     * <b>placeRandomly</b> places agents in agentMap - at a new random empty
     * location (envMap && agentMap)
     * 
     * @param agents_
     *            ArrayList<? extends Agent>
     * @params agentMap_ ObjectGrid2DExtendido<Agent>
     * @return void Cristina 3 May 2011
     */
    public void placeRandomly(ArrayList<? extends Agent> agents_, ObjectGrid2DExtended<Agent> agentMap_) {
        for (Agent agent : agents_) {
            Point2D placingLoc = agentMap_.getRandEmptyLocation();
            while (null != _environmentMap.getElemFromLoc(placingLoc)) {
                placingLoc = agentMap_.getRandEmptyLocation();
            }
            agentMap_.addToMap(agent, placingLoc);
        }
    }

    /**
     * 
     * addMoreZombies
     * <p>
     * increases the number of zombies by NR_INCR_ZOMBIES
     * <p>
     * adds created Zombie to agentMap
     * 
     * @params
     * @return zombie ArrayList<Zombie> lorelay Apr 15, 2011
     */
    public ArrayList<Zombie> addMoreZombies() {
        ArrayList<Zombie> zombie = new ArrayList<Zombie>(NR_INCR_ZOMBIES);
        for (int i = 0; i < NR_INCR_ZOMBIES; i++) {
            zombie.add(new Zombie(this));
            placeRandomly(zombie, _agentMap);
        }
        return zombie;
    }

    /**
     * checkIfDone checks if end of simulation by:
     * 
     * no more zombies reached maximum of steps/ time's up
     * 
     * @params state SimState
     * @return Cristina 28 Apr 2011
     */
    public void checkIfDone(SimState state) {
        if ((null != _activeSurvivors && _activeSurvivors.isEmpty()) || state.schedule.time() >= MAX_STEPS) {
            writeSimResults();
            schedule.reset();
            System.out.println("END of simulation");

            if (--_nrSimulationes > 0)
                reset();
        }
    }

    /**
     * <b>writeSimResults</b> writes the simulations' results to files
     * 
     * @params
     * @return <p>
     *         lorelay Apr 15, 2011
     */
    public void writeSimResults() {
        double mediaSpeed = 0;
        double mediaMovementType = 0;
        double mediaPrecautionType = 0;
        double mediaWeaponRange = 0;
        double mediaWeaponPrecision = 0;
        double mediaDisparosArma = 0;
        double mediaAim = 0;
        double mediaNumKilledZombies = 0;
        double mediaNumMissedZombies = 0;
        double mediaMovement = 0;
        double mediaNumHuidas = 0;
        double mediaNumWaits = 0;
        double mediaNumTurnosVida = 0;
        double fraccionEscaped = 0;
        double fraccionGotKilled = 0;
        double fraccionSurvived = 0;
        String strResult = "";

        for (Survivor survivor : _allSurvivors) {
            _fileSurvivorResults.println(ENV_WIDTH
                    + "\t"
                    + ENV_LENGTH
                    + "\t"
                    + INIT_NR_ZOMBIES
                    + "\t"
                    + INIT_NR_SURVIV
                    + "\t"
                    + NR_INCR_ZOMBIES
                    + "\t"
                    + MAX_STEPS
                    + "\t"
                    + _mapVs
                    + "\t"
                    + _survVs
                    + "\t"
                    + survivor.getSpeed()
                    + "\t"
                    + survivor.getMovementType().toString().toLowerCase()
                    + "\t"
                    + survivor.getPrecautionType().toString().toLowerCase()
                    + "\t"
                    + survivor.getArma().toString().toLowerCase()
                    + "\t"
                    + survivor.getPunteria()
                    + "\t"
                    + survivor.getNumZombiesMatados()
                    + "\t"
                    + survivor.getNumZombiesMissed()
                    + "\t"
                    + survivor.getNumMovimientos()
                    + "\t"
                    + survivor.getNumHuidas()
                    + "\t"
                    + survivor.getNumEsperas()
                    + "\t"
                    + survivor.getNumOfLives()
                    + "\t"
                    + (survivor.getHasEscaped() ? "escaped" : (_activeSurvivors.contains(survivor) ? "survived"
                            : "died")));

            strResult = strResult
                    // + "\t"
                    + survivor.toString()
                    + "\t"
                    + (survivor.getHasEscaped() ? "escaped" : (_activeSurvivors.contains(survivor) ? "survived"
                            : "died")) + "\n ";

            mediaSpeed += survivor.getSpeed();
            mediaMovementType = mediaMovementType + survivor.getMovementType().ordinal();
            mediaPrecautionType = mediaPrecautionType + survivor.getPrecautionType().ordinal();
            mediaWeaponRange += survivor.getArma().getRange();
            mediaWeaponPrecision += survivor.getArma().getPrecision();
            mediaDisparosArma += survivor.getNumZombiesMatados() + survivor.getNumZombiesMissed();
            mediaAim += survivor.getPunteria();
            mediaNumKilledZombies += survivor.getNumZombiesMatados();
            mediaNumMissedZombies += survivor.getNumZombiesMissed();
            mediaMovement += survivor.getNumMovimientos();
            mediaNumHuidas += survivor.getNumHuidas();
            mediaNumWaits += survivor.getNumEsperas();
            mediaNumTurnosVida += survivor.getNumOfLives();
            if (survivor.getHasEscaped())
                fraccionEscaped++;
            else if (_activeSurvivors.contains(survivor))
                fraccionSurvived++;
            else
                fraccionGotKilled++;
        }
        if (!BENCHMARK_MODE) {
            simResultsFrame = new SimResultsUI("Predator-Prey Simulaion Results", strResult);
        }

        mediaSpeed /= INIT_NR_SURVIV;
        mediaMovementType /= INIT_NR_SURVIV;
        mediaPrecautionType /= INIT_NR_SURVIV;
        mediaWeaponRange /= INIT_NR_SURVIV;
        mediaWeaponPrecision /= INIT_NR_SURVIV;
        mediaDisparosArma /= INIT_NR_SURVIV;
        mediaAim /= INIT_NR_SURVIV;
        mediaNumKilledZombies /= INIT_NR_SURVIV;
        mediaNumMissedZombies /= INIT_NR_SURVIV;
        mediaMovement /= INIT_NR_SURVIV;
        mediaNumHuidas /= INIT_NR_SURVIV;
        mediaNumWaits /= INIT_NR_SURVIV;
        mediaNumTurnosVida /= INIT_NR_SURVIV;
        fraccionEscaped /= INIT_NR_SURVIV;
        fraccionGotKilled /= INIT_NR_SURVIV;
        fraccionSurvived /= INIT_NR_SURVIV;

        String strResultSim = new String(ENV_WIDTH + "\t" + ENV_LENGTH + "\t" + INIT_NR_ZOMBIES + "\t" + INIT_NR_SURVIV
                + "\t" + NR_INCR_ZOMBIES + "\t" + MAX_STEPS + "\t" + BENCHMARK_MAP_VS + "\t" + BENCHMARK_SURV_VS + "\t"
                + mediaSpeed + "\t" + mediaMovementType + "\t" + mediaPrecautionType + "\t" + mediaWeaponRange + "\t"
                + mediaWeaponPrecision + "\t" + mediaDisparosArma + "\t" + mediaAim + "\t" + mediaNumKilledZombies
                + "\t" + mediaNumMissedZombies + "\t" + mediaMovement + "\t" + mediaNumHuidas + "\t" + mediaNumWaits
                + "\t" + mediaNumTurnosVida + "\t" + fraccionEscaped + "\t" + fraccionGotKilled + "\t"
                + fraccionSurvived);

        _ficheroResultadosPartida.println(strResultSim);

    }

    /**
     * <b>getEnvironmentMap</b> returns environment map containing
     * EnvironmentalElement
     * 
     * @params
     * @return <b>_environmentMap</b>
     *         ObjectGrid2DExtendido<EnvironmentalElement>
     *         <p>
     *         Cristina 5 May 2011
     */
    public ObjectGrid2DExtended<EnvironmentalElement> getEnvironmentMap() {
        return _environmentMap;
    }

    /**
     * 
     * <b>getAgentMap</b> returns agent map containing Zombie and Survivor
     * 
     * @params
     * @return <code>_agentMap</code> ObjectGrid2DExtendido<Agent>
     *         <p>
     *         Cristina 4 May 2011
     */
    public ObjectGrid2DExtended<Agent> getAgentMap() {
        return _agentMap;
    }

    /**
     * 
     * <b>getActiveSurvivors</b> returns the survivors that are still
     * active/alive
     * 
     * @params
     * @return _supervivientesActivos ArrayList<Survivor>
     *         <p>
     *         Cristina Apr 15, 2011
     */
    public ArrayList<Survivor> getActiveSurv() {
        return _activeSurvivors;
    }

    /**
     * 
     * <b>getEnvironmentSizeX</b> returns environment width
     * 
     * @params
     * @return int
     *         <p>
     *         Cristina 27 Apr 2011
     */
    public int getEnvironmentSizeX() {
        return ENV_WIDTH;
    }

    /**
     * 
     * <b>getEnvironmentSizeY</b> returns environment lenght
     * 
     * @params
     * @return int
     *         <p>
     *         Cristina Apr 15, 2011
     */
    public int getEnvironmentSizeY() {
        return ENV_LENGTH;
    }

    /**
     * 
     * outputData TODO BROKEN
     * <p>
     * while survivors are beeing removed from map, this method as it is, can't
     * function !!
     * 
     * @params
     * @return Cristina May 6, 2011
     */
    public void outputData() {
        /*
         * if (null != _allSurvivors && (false ==
         * _supervivientesActivos.isEmpty())) { int ticks = (int)
         * schedule.time();
         * 
         * double distanciaMedia = 0; Set<EnvironmentalElement> exitSet =
         * _environmentMap.getElementsOfType(Exit.class); if (false ==
         * exitSet.isEmpty()) { for (EnvironmentalElement envElem : exitSet) {
         * Point2D exitPoint = _environmentMap.getLocation(envElem);
         * 
         * for (Survivor survivor : _allSurvivors) { Point2D survPoint =
         * _agentMap.getLocation(survivor);
         * 
         * distanciaMedia += exitPoint.distance(survPoint); } distanciaMedia /=
         * _allSurvivors.length;
         * 
         * _ficheroEvolucionPartida.println(ticks + "\t" + 1.0 * _zombies.size()
         * / (ENV_WIDTH * ENV_LENGTH) + "\t" + 1.0 *
         * _supervivientesActivos.size() / (ENV_WIDTH * ENV_LENGTH) + "\t" +
         * distanciaMedia); } } }
         */

    }

    /**
     * 
     * <b>setNumSimulaciones</b> sets the initial number of simulations
     * 
     * @params numSimulaciones int
     * @return void
     *         <p>
     *         Cristina 6 May 2011
     */
    public void setNumSimulaciones(int numSimulaciones) {
        _nrSimulationes = numSimulaciones;
    }

    /**
     * 
     * <b>generateRandAim</b> returns randomly generated survivor Aim
     * 
     * @params
     * @return double
     *         <p>
     *         Cristina 26 Apr 2011
     */
    public double generateRandAim() {
        return 100 * random.nextDouble();
    }

    /**
     * 
     * <b>generateRandSpeed</b> returns randomly generated survivor speed
     * between <code>_survMinSpeed</code> and <code>_survMaxSpeed</code>
     * 
     * @params
     * @return int
     *         <p>
     *         Cristina 6 May 2011
     */
    private int generateRandSpeed() {
        int speed;
        if (SURV_MAX_SPEED == SURV_MIN_SPEED) {
            speed = SURV_MAX_SPEED;
        } else {
            speed = random.nextInt(SURV_MAX_SPEED - SURV_MIN_SPEED + 1) + SURV_MIN_SPEED;
        }
        return speed;
    }

    /**
     * getMapVs TODO purpose
     * 
     * @params
     * @return Cristina 14 Jun 2011
     */
    public final Integer getMapVs() {
        return _mapVs;
    }

    /**
     * setMapVs TODO purpose
     * 
     * @params
     * @return Cristina 14 Jun 2011
     */
    public final void setMapVs(Integer mapVs_) {
        _mapVs = mapVs_;
    }

    /**
     * getSurvVs TODO purpose
     * 
     * @params
     * @return Cristina 14 Jun 2011
     */
    public final Integer getSurvVs() {
        return _survVs;
    }

    /**
     * setSurvVs TODO purpose
     * 
     * @params
     * @return Cristina 14 Jun 2011
     */
    public final void setSurvVs(Integer survVs_) {
        _survVs = survVs_;
    }

    /**
     * getNextMovementType TODO purpose
     * 
     * @params
     * @return Cristina 30 Jun 2011
     */
    public final int getNextMovementType() {
        return nextMovementType;
    }

    /**
     * setNextMovementType TODO purpose
     * 
     * @params
     * @return Cristina 30 Jun 2011
     */
    public final void setNextMovementType(int nextMovementType_) {
        nextMovementType = nextMovementType_;
    }

    /**
     * getNextPrecautionType TODO purpose
     * 
     * @params
     * @return Cristina 30 Jun 2011
     */
    public final int getNextPrecautionType() {
        return nextPrecautionType;
    }

    /**
     * setNextPrecautionType TODO purpose
     * 
     * @params
     * @return Cristina 30 Jun 2011
     */
    public final void setNextPrecautionType(int nextPrecautionType_) {
        nextPrecautionType = nextPrecautionType_;
    }

    /**
     * getNextWeapon TODO purpose
     * 
     * @params
     * @return Cristina 30 Jun 2011
     */
    public final int getNextWeapon() {
        return nextWeapon;
    }

    /**
     * setNextWeapon TODO purpose
     * 
     * @params
     * @return Cristina 30 Jun 2011
     */
    public final void setNextWeapon(int nextWeapon_) {
        nextWeapon = nextWeapon_;
    }

    /**
     * getSurvivorsInitNr returns the initial number of survivors
     * 
     * @params
     * @return INIT_NR_SURVIV int Cristina 27 Apr 2011
     */
    public int getSurvivorsInitNr() {
        return INIT_NR_SURVIV;
    }

    /**
     * getZombiesInitNr returns the initial number of zombies
     * 
     * @params
     * @return int Cristina 27 Apr 2011
     */
    public int getZombiesInitNr() {
        return INIT_NR_ZOMBIES;
    }

    /**
     * 
     * <b>getNumSimulations</b> returns the number of simulations
     * 
     * @params
     * @return int
     *         <p>
     *         Cristina Apr 15, 2011
     */
    public final int getNrSimulationes() {
        return _nrSimulationes;
    }

    /**
     * setNrSimulationes TODO purpose
     * 
     * @params
     * @return Cristina 14 Jun 2011
     */
    public final void setNrSimulationes(int nrSimulationes_) {
        _nrSimulationes = nrSimulationes_;
    }
}
