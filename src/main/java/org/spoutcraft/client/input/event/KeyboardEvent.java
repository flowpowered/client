package org.spoutcraft.client.input.event;

/**
 *
 */
public class KeyboardEvent {
    private final char character;
    private final int key;
    private final boolean pressedDown;
    private final long nanoseconds;

    public KeyboardEvent(char character, int key, boolean pressedDown, long nanoseconds) {
        this.character = character;
        this.key = key;
        this.pressedDown = pressedDown;
        this.nanoseconds = nanoseconds;
    }

    public char getCharacter() {
        return character;
    }

    public int getKey() {
        return key;
    }

    public boolean wasPressedDown() {
        return pressedDown;
    }

    public long getNanoseconds() {
        return nanoseconds;
    }
}
