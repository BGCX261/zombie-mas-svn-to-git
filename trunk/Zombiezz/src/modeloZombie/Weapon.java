package modeloZombie;

import modeloZombie.utils.WeaponType;

public class Weapon {

    private WeaponType _type;
    private int _range; // range
    private double _precision;
    private int _nrShots; // nrShots

    /**
     * 
     * initWeapon initializes the properties of a weapon with the args passed
     * 
     * @params PAN(0), (1,100.0,1);
     * @params PISTOL(1), (2,85.0,1);
     * @params UZI(2), (2,75.0,3);
     * @params AK47(3), (3,65.0,2);
     * @params SNIPER(4); (4,95.0,1);
     * @return lorelay Apr 15, 2011
     */
    public void initWeapon(int range_, double precision_, int nrShots_) {
        this._range = range_;
        _precision = precision_;
        this._nrShots = nrShots_;
    }

    /**
     * <b>Weapon</b> for each type of weapon, it instantiates it with it's own
     * characteristics(range, precision, nrShots)
     * 
     * @param type_
     *            WeaponType
     */
    public Weapon(WeaponType type_) {
        if (WeaponType.PAN == type_) {
            initWeapon(1, 100.0, 1);
        } else if (WeaponType.PISTOL == type_) {
            initWeapon(2, 85.0, 1);
        } else if (WeaponType.UZI == type_) {
            initWeapon(2, 75.0, 3);
        } else if (WeaponType.AK47 == type_) {
            initWeapon(3, 65.0, 2);
        } else if (WeaponType.SNIPER == type_) {
            initWeapon(4, 95.0, 1);
        }
        _type = type_;
    }

    /**
     * 
     * <b>getType</b> it returns the type of weapon it is
     * 
     * @params
     * @return WeaponType
     *         <p>
     *         lorelay Apr 14, 2011
     */
    public WeaponType getType() {
        return _type;
    }

    @Override
    public String toString() {
        String str;
        if (WeaponType.PAN == _type) {
            str = "Pan";
        } else if (WeaponType.PISTOL == _type) {
            str = "Pistol";
        } else if (WeaponType.UZI == _type) {
            str = "Uzi";
        } else if (WeaponType.AK47 == _type) {
            str = "AK47";
        } else {
            str = "Sniper";
        }
        return str;
    }

    /**
     * 
     * getRange returns the range of a weapon type
     * 
     * @params
     * @return int lorelay Apr 14, 2011
     */
    public int getRange() {
        return _range;
    }

    /**
     * 
     * getPrecision returns the precision of a weapon type
     * 
     * @params
     * @return double lorelay Apr 14, 2011
     */
    public double getPrecision() {
        return _precision;
    }

}
