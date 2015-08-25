package modeloZombie;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import modeloZombie.placeable.env.Obstacle;
import modeloZombie.utils.MovementType;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import sim.util.IntBag;
import ec.util.MersenneTwisterFast;

public class ObjectGrid2DExtended<T> extends ObjectGrid2D {
    //
    private static final long serialVersionUID = 1L;
    private final MersenneTwisterFast _random;
    private Map<T, Point2D> _elementMap;

    /**
     * 
     * TODO
     * 
     * @param width
     * @param height
     */
    public ObjectGrid2DExtended(int width, int height, MersenneTwisterFast random_) {
        super(width, height);
        _random = random_;
        _elementMap = new HashMap<T, Point2D>();
    }

    /**
     * 
     * TODO
     * 
     * @param values
     */
    public ObjectGrid2DExtended(ObjectGrid2D values, MersenneTwisterFast random_) {
        super(values);
        _random = random_;
    }

    /**
     * Gets all neighbors of a location that satisfy max( abs(x-X) , abs(y-Y) )
     * <= dist. This region forms a square 2*dist+1 cells across, centered at
     * (X,Y). If dist==1, this is equivalent to the so-called
     * "Moore Neighborhood" (the eight neighbors surrounding (X,Y)), plus (X,Y)
     * itself. Places each x and y value of these locations in the provided
     * IntBags xPos and yPos, clearing the bags first. Then places into the
     * result Bag the objects at each of those <x,y> locations clearning it
     * first. Returns the result Bag (constructing one if null had been passed
     * in). null may be passed in for the various bags, though it is more
     * efficient to pass in a 'scratch bag' for each one.
     */
    public ArrayList<T> getTNeighborsMaxDist(final Point2D loc_, final int dist, final boolean toroidal,
            ArrayList<T> result, IntBag xPos, IntBag yPos) {
        if (null != loc_) {
            int x = (int) loc_.getX();
            int y = (int) loc_.getY();
            if (xPos == null)
                xPos = new IntBag();
            if (yPos == null)
                yPos = new IntBag();
            if (result == null)
                result = new ArrayList<T>();

            getNeighborsMaxDistance(x, y, dist, toroidal, xPos, yPos);

            result.clear();
            for (int i = 0; i < xPos.numObjs; i++) {
                Object fieldContent = field[xPos.objs[i]][yPos.objs[i]];
                result.add((T) fieldContent);

            }
        }
        return result;
    }

    /**
     * 
     * <b>getPoint2DsInRange</b> TODO purpose
     * 
     * @param
     * @return
     */
    public ArrayList<Point2D> getPoint2DsInRange(Point2D origen_, Point2D destino_, int maxDist_,
            MovementType movementType_) {
        ArrayList<Point2D> coordArray = null;
        if (null != origen_) {
            int origenX = (int) origen_.getX();
            int origenY = (int) origen_.getY();

            int destinoX = (int) destino_.getX();
            int destinoY = (int) destino_.getY();

            IntBag xBag = new IntBag();
            IntBag yBag = new IntBag();
            super.getNeighborsMaxDistance(origenX, origenY, maxDist_, false, xBag, yBag);

            int distanciaOriginalX = Math.abs(origenX - destinoX);
            int distanciaOriginalY = Math.abs(origenY - destinoY);
            double distanciaOriginal = Math.sqrt(Math.pow(distanciaOriginalX, 2) + Math.pow(distanciaOriginalY, 2));

            coordArray = new ArrayList<Point2D>(xBag.size());

            for (int i = 0; i < xBag.size(); i++) {
                // Descartamos la posición de origen
                if (xBag.get(i) == origenX && yBag.get(i) == origenY)
                    continue;

                // measure distances
                int distanciaNuevaX = Math.abs(xBag.get(i) - destinoX);
                int distanciaNuevaY = Math.abs(yBag.get(i) - destinoY);
                double distanciaNueva = Math.sqrt(Math.pow(distanciaNuevaX, 2) + Math.pow(distanciaNuevaY, 2));

                // decide if the movement is valid depending on the type of
                // movement
                boolean aniadir = false;
                switch (movementType_) {

                case RANDOM:
                    aniadir = true;
                    break;
                case SEMI_RANDOM:
                    if (distanciaNuevaX <= distanciaOriginalX || distanciaNuevaY <= distanciaOriginalY) {
                        aniadir = true;
                    }
                    break;
                case INDIRECT:
                    if (distanciaNueva <= distanciaOriginal) {
                        aniadir = true;
                    }
                    break;
                case DIRECT:
                    if (distanciaNuevaX <= distanciaOriginalX && distanciaNuevaY <= distanciaOriginalY) {
                        aniadir = true;
                    }
                    break;
                }

                if (aniadir)
                    coordArray.add(new Point(xBag.get(i), yBag.get(i)));
            }
        }
        return coordArray;
    }

    /**
     * 
     * obtenerPosicionVaciaAleatoria returns an empty position
     * 
     * @params
     * @return Point2D or null if none
     */
    public Point2D obtenerPosicionVaciaAleatoria(ArrayList<Point2D> posiciones) {
        if (posiciones.isEmpty())
            return null;

        ArrayList<Point2D> posicionesVacias = new ArrayList<Point2D>(posiciones.size());

        for (Object p : posiciones) {
            Point2D c = (Point2D) p;
            if (field[(int) c.getX()][(int) c.getY()] == null)
                posicionesVacias.add(c);
        }

        if (posicionesVacias.size() == 0)
            return null;
        else if (posicionesVacias.size() == 1)
            return posicionesVacias.get(0);
        else
            return posicionesVacias.get(_random.nextInt(posicionesVacias.size()));
    }

    /**
     * 
     * setEmptyBlock TODO adds an elem_ to the Map on a block of (width_,
     * length_) starting at a new empty location
     * 
     * @params
     * @return Cristina 10 May 2011
     */
    public void setEmptyBlock(int width_, int length_, T elem_) {
        Point2D newEmptyLoc = getRandEmptyLocation();

        if ((int) newEmptyLoc.getX() + width_ > getWidth()) {
            width_ = -width_;
        }
        if ((int) newEmptyLoc.getY() + length_ > getHeight()) {
            length_ = -length_;
        }
        while (((int) newEmptyLoc.getX() + width_ > getWidth()) && ((int) newEmptyLoc.getY() + length_ > getHeight())) {
            newEmptyLoc = getRandEmptyLocation();
        }

        for (int i = (int) newEmptyLoc.getX(); i < (int) newEmptyLoc.getX() + width_; i++) {
            for (int j = (int) newEmptyLoc.getY(); j < (int) newEmptyLoc.getY() + length_; j++) {
                if (get(i, j) == null) {
                    if (elem_ instanceof Obstacle) {
                        Obstacle obst = new Obstacle(((Obstacle) elem_).getType());
                        _elementMap.put((T) obst, new Point(i, j));
                        field[i][j] = obst;
                    }
                }
            }
        }
    }

    /**
     * 
     * <b>setEmptyBlock</b> adds an elem_ to the Map on a block of (width_,
     * length_) starting at <code>origin_</code> location
     * 
     * @param origin_
     *            Point2D
     * @param width_
     *            int
     * @param length_
     *            int
     * @param elem_
     *            T
     * @return Cristina 10 May 2011
     */
    public void setEmptyBlock(Point2D origin_, int width_, int length_, T elem_) {

        for (int i = (int) origin_.getX(); i < (int) origin_.getX() + width_; i++) {
            for (int j = (int) origin_.getY(); j < (int) origin_.getY() + length_; j++) {
                if (get(i, j) == null) {
                    if (elem_ instanceof Obstacle) {
                        Obstacle obst = new Obstacle(((Obstacle) elem_).getType());
                        _elementMap.put((T) obst, new Point(i, j));
                        field[i][j] = obst;
                    }
                }
            }
        }
    }

    /**
     * 
     * <b>setEmptyBlockAtCenter</b> adds an elem_ to the Map on a block of
     * (width_, length_) having its center at <code>center_</code> location
     * 
     * @param center_
     *            Point2D
     * @param width_
     *            int
     * @param length_
     *            int
     * @param elem_
     *            T
     * @return Cristina 10 May 2011
     */
    public void setEmptyBlockAtCenter(Point2D center_, int width_, int length_, T elem_) {
        Point2D origin_ = new Point((int) (center_.getX()) - (width_ / 2), (int) (center_.getY()) - (length_ / 2));

        for (int i = (int) origin_.getX(); i < (int) origin_.getX() + width_; i++) {
            for (int j = (int) origin_.getY(); j < (int) origin_.getY() + length_; j++) {
                if (get(i, j) == null) {
                    if (elem_ instanceof Obstacle) {
                        Obstacle obst = new Obstacle(((Obstacle) elem_).getType());
                        _elementMap.put((T) obst, new Point(i, j));
                        field[i][j] = obst;
                    }
                }
            }
        }
    }

    /**
     * 
     * exists
     * <p>
     * returns whether or not element_ is in the Map( _elementMap or field[][])
     * 
     * @param
     * @return Cristina 28 Apr 2011
     */
    public boolean exists(T element_) {
        boolean exists = false;
        exists = (_elementMap.get(element_) != null);

        return exists;
    }

    /**
     * 
     * getLocation
     * <p>
     * if exists, it returns the location of given element in _elementMap
     * 
     * @param element_
     *            T
     * @return Point2D
     *         <p>
     *         Cristina 28 Apr 2011
     */
    public Point2D getLocation(T element_) {
        if (exists(element_)) {
            return _elementMap.get(element_);
        }
        return null;
    }

    /**
     * 
     * getRandLoc gets new empty random positions
     * 
     * @params
     * @return Cristina 26 Apr 2011
     */
    public Point2D getRandLoc_() {
        int x = _random.nextInt(width);
        int y = _random.nextInt(height);

        return new Point(x, y);
    }

    /**
     * 
     * getRandLoc gets new empty random positions
     * 
     * @return Cristina 26 Apr 2011
     */
    public Point2D getRandEmptyLocation() {
        int x = _random.nextInt(width);
        int y = _random.nextInt(height);

        while ((field[x][y] != null) && _elementMap.containsValue(new Point(x, y))) {
            // && (getElemFromLoc(new Point(x,y)) != null)
            x = _random.nextInt(width);
            y = _random.nextInt(height);
        }
        return new Point(x, y);
    }

    /**
     * <b>addToMap</b>
     * <p>
     * it removes previous inhabitants of given location in field[][] &&
     * _elementMap
     * <p>
     * adds given element to field[][] && _elementMap
     * 
     * @params
     * @return boolean
     * @author biggie
     */
    public boolean addToMap(T el_, Point2D pt_) {
        boolean added = false;
        if (!exists(el_)) {
            // Overwrite old location code TODO check if is overwrittable?
            // crosscheck with other maps?
            T oldEl = (T) field[(int) pt_.getX()][(int) pt_.getY()];
            T elemInMap = getElemFromLoc(pt_);
            if (null != oldEl && (elemInMap != null)) {
                removeFromMap(oldEl);
            }
            field[(int) pt_.getX()][(int) pt_.getY()] = el_;
            _elementMap.put(el_, pt_);
            added = true;
        }
        return added;
    }

    /**
     * 
     * addToRandEmptyLoc TODO
     * 
     * @params
     * @return Point2D
     *         <p>
     *         null, if element already in map,
     *         <p>
     *         the new location, else
     *         <p>
     *         Cristina 26 Apr 2011
     */
    public Point2D addToRandEmptyLoc(T el_) {
        if (!exists(el_)) {
            Point2D point = getRandEmptyLocation();
            addToMap(el_, point);
            return point;
        }
        return null;
    }

    /**
     * 
     * <b>removeFromMap</b>
     * <p>
     * tests given element existence only in _elementMap <b>,</b> removes given
     * element from _elementMap & field[][]
     * 
     * 
     * @params
     * @return Cristina 28 Apr 2011
     */
    public Point2D removeFromMap(T element_) {
        Point2D point = null;
        if (exists(element_)) {
            Point2D loc = getLocation(element_);
            if (field[(int) loc.getX()][(int) loc.getY()] != null) {
                point = _elementMap.remove(element_);
                field[(int) point.getX()][(int) point.getY()] = null;
            }
        }
        return point;
    }

    /**
     * 
     * Creates a new list from existing elements, where the element class
     * 
     * @param clazz_
     *            the spefic object type to look for. See
     *            {@link modeloZombie.placeable.EnvironmentalElements} for a
     *            hint of what those types might be.
     * @return Set<T>
     * @author biggie
     */
    public Set<T> getElementsOfType(Class<? extends T> clazz_) {
        Set<T> retSet = new HashSet<T>();

        for (T key : _elementMap.keySet()) {
            // if (exists(key)) {
            if (key.getClass().isAssignableFrom(clazz_)) {
                retSet.add(key);
            }
            // }
        }
        return retSet;
    }

    /**
     * 
     * getElemAtLoc returns an element from the given location in _elementMap
     * 
     * @params locSeeked_ Point2D
     * @return elem T ; null if not in _elementMap
     *         <p>
     *         Cristina 12 May 2011
     */
    public T getElemFromLoc(Point2D locSeeked_) {
        for (T el_ : _elementMap.keySet()) {
            Point2D elemLoc = getLocation(el_);
            if (locSeeked_.equals(elemLoc)) {
                return el_;
            }
        }
        return null;
    }

    /**
     * Returns a bag with neighboring cells to an object <code>el_</code> in the
     * map, with radius <code>rad</code>
     * 
     * @param el_
     *            element in the map
     * @param rad_
     *            area around it
     * @return Bag
     * @author biggie
     */
    public Bag getAreaAroundMe(T el_, int rad_) {
        Point2D pt = _elementMap.get(el_);
        return getAreaAroundMe(pt, rad_);
    }

    /**
     * 
     * <b>getLocAroundMeOfType</b> gets a Set<> of locations of a passed through
     * T class
     * 
     * @params
     * @return Set of Point2D
     *         <p>
     *         Cristina 5 May 2011
     */
    public Bag getAreaAroundMeByType(T el_, Class<? extends T> typeSeeked_, int rad_) {
        Bag foundTypes = null;
        Bag area = getAreaAroundMe(el_, rad_);
        if (area != null) {
            for (Object obj : area) {
                if (obj != null) {
                    if (obj.getClass().isAssignableFrom(typeSeeked_)) {
                        if (null == foundTypes) {
                            foundTypes = new Bag();
                        }
                        foundTypes.add(obj);
                    }
                }
            }
        }
        return foundTypes;
    }

    /**
     * Returns a bag with neighboring cells to an object <code>ag_</code> in the
     * map, with radius <code>rad</code>
     * 
     * @param el_
     *            element in the map
     * @param rad_
     *            area around it
     * @return Bag
     * @author biggie
     */
    public Bag getAreaAroundMe(Point2D pt_, int rad_) {
        Bag area = null;
        if (null != pt_) {
            area = getNeighborsMaxDistance((int) pt_.getX(), (int) pt_.getY(), rad_, false, null, null, null);
        }
        return area;
    }

    /**
     * moveElementTo if element exists in Map, it removes it from old location &
     * adds it to the new location
     * 
     * @params T element_
     * @params pt_ Point2D
     * @return boolean
     * @author biggie
     */
    public boolean moveElementTo(T element_, Point2D pt_) {
        boolean moved = false;
        if (exists(element_)) {
            removeFromMap(element_);
            moved = addToMap(element_, pt_);
        }
        return moved;
    }

}
