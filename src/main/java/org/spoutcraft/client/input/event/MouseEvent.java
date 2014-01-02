package org.spoutcraft.client.input.event;

/**
 *
 */
public class MouseEvent {
    private final int x, y;
    private final int dx, dy;
    private final int dWheel;
    private final int button;
    private final boolean pressedDown;

    public MouseEvent(int x, int y, int dx, int dy, int dWheel, int button, boolean pressedDown) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.dWheel = dWheel;
        this.button = button;
        this.pressedDown = pressedDown;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDX() {
        return dx;
    }

    public int getDY() {
        return dy;
    }

    public int getDWheel() {
        return dWheel;
    }

    public int getButton() {
        return button;
    }

    public boolean wasPressedDown() {
        return pressedDown;
    }
}
