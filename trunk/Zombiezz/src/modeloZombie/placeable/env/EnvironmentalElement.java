package modeloZombie.placeable.env;

import java.awt.Color;

public abstract class EnvironmentalElement {
    protected final Color COLOR;

    public EnvironmentalElement(Color color_) {
        COLOR = color_;
    }

    public Color getColor() {
        return COLOR;
    }

}
