package org.spoutcraft.client.input;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import org.spoutcraft.client.Game;
import org.spoutcraft.client.input.event.KeyboardEvent;
import org.spoutcraft.client.input.event.MouseEvent;
import org.spoutcraft.client.network.message.ChannelMessage.Channel;
import org.spoutcraft.client.util.ticking.TickingElement;

/**
 *
 */
public class Input extends TickingElement {
    private static final int TPS = 60;
    private static final int EVENT_QUEUE_MAX = 100;
    private final Game game;
    private boolean mouseCreated = false, keyboardCreated = false;
    private final Map<Channel, ConcurrentLinkedQueue<KeyboardEvent>> keyboardQueues = new EnumMap<>(Channel.class);
    private final Map<Channel, ConcurrentLinkedQueue<MouseEvent>> mouseQueues = new EnumMap<>(Channel.class);

    public Input(Game game) {
        super("input", TPS);
        this.game = game;
        addChannel(Channel.INTERFACE);
    }

    private void addChannel(Channel channel) {
        keyboardQueues.put(channel, new ConcurrentLinkedQueue<KeyboardEvent>());
        mouseQueues.put(channel, new ConcurrentLinkedQueue<MouseEvent>());
    }

    @Override
    public void onStart() {
        System.out.println("Input start");
    }

    @Override
    public void onTick(long dt) {
        // Tries to create the input, only does so if it already hasn't been created
        createInputIfNecessary();
        if (keyboardCreated) {
            // For every keyboard event
            while (Keyboard.next()) {
                // Create a new event
                final KeyboardEvent event = new KeyboardEvent(
                        Keyboard.getEventCharacter(), Keyboard.getEventKey(),
                        Keyboard.getEventKeyState(), Keyboard.getEventNanoseconds());
                // Add to the queues, if we don't have an empty queue, return, there's nothing more to add
                if (!addToQueues(keyboardQueues.values(), event, EVENT_QUEUE_MAX)) {
                    break;
                }
            }
        }
        if (mouseCreated) {
            // For every mouse event
            while (Mouse.next()) {
                // We ignore events not caused by buttons to prevent them from filling the queue very quickly
                if (Mouse.getEventButton() == -1) {
                    continue;
                }
                // Create a new event
                final MouseEvent event = new MouseEvent(
                        Mouse.getEventX(), Mouse.getEventY(),
                        Mouse.getEventDX(), Mouse.getEventDY(),
                        Mouse.getEventDWheel(),
                        Mouse.getEventButton(), Mouse.getEventButtonState());
                // Add to the queues, if we don't have an empty queue, return, there's nothing more to add
                if (!addToQueues(mouseQueues.values(), event, EVENT_QUEUE_MAX)) {
                    break;
                }
            }
        }
    }

    private void createInputIfNecessary() {
        if (!keyboardCreated) {
            if (Display.isCreated()) {
                if (!Keyboard.isCreated()) {
                    try {
                        Keyboard.create();
                        keyboardCreated = true;
                    } catch (LWJGLException ex) {
                        throw new RuntimeException("Could not create keyboard", ex);
                    }
                } else {
                    keyboardCreated = true;
                }
            }
        }
        if (!mouseCreated) {
            if (Display.isCreated()) {
                if (!Mouse.isCreated()) {
                    try {
                        Mouse.create();
                        mouseCreated = true;
                    } catch (LWJGLException ex) {
                        throw new RuntimeException("Could not create mouse", ex);
                    }
                } else {
                    mouseCreated = true;
                }
                Mouse.setClipMouseCoordinatesToWindow(false);
            }
        }
    }

    private <T> boolean addToQueues(Collection<ConcurrentLinkedQueue<T>> queues, T item, int maxItems) {
        // Track if we have at least one queue not full
        boolean oneNotFull = false;
        // For to all queues
        for (Queue<T> queue : queues) {
            final int size = queue.size();
            // Only add if the queue is not full
            if (queue.size() < maxItems) {
                queue.add(item);
            }
            // If we still have room in the queue, mark that one is still not full
            if (size + 1 < maxItems) {
                oneNotFull = true;
            }
        }
        // Return if we still have a queue with room for items
        return oneNotFull;
    }

    @Override
    public void onStop() {
        System.out.println("Input stop");

        Keyboard.destroy();
        keyboardCreated = false;
        Mouse.destroy();
        mouseCreated = false;
        flushQueues();
    }

    private void flushQueues() {
        for (Queue<KeyboardEvent> queue : keyboardQueues.values()) {
            queue.clear();
        }
        for (Queue<MouseEvent> queue : mouseQueues.values()) {
            queue.clear();
        }
    }

    public Queue<KeyboardEvent> getKeyboardQueue(Channel channel) {
        return keyboardQueues.get(channel);
    }

    public Queue<MouseEvent> getMouseQueue(Channel channel) {
        return mouseQueues.get(channel);
    }

    public boolean isActive() {
        return Display.isActive();
    }

    public void setMouseGrabbed(boolean grabbed) {
        Mouse.setGrabbed(grabbed);
    }

    public int getMouseX() {
        return Mouse.getX();
    }

    public int getMouseY() {
        return Mouse.getY();
    }

    public boolean isKeyDown(int key) {
        return Keyboard.isKeyDown(key);
    }

    public boolean isButtonDown(int button) {
        return Mouse.isButtonDown(button);
    }

    public Game getGame() {
        return game;
    }
}
