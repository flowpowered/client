/**
 * This file is part of Client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spoutcraft <http://spoutcraft.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spoutcraft.client.input;

import java.util.Queue;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import org.spoutcraft.client.Game;
import org.spoutcraft.client.input.event.KeyboardEvent;
import org.spoutcraft.client.input.event.MouseEvent;
import org.spoutcraft.client.util.queue.SubscribableQueue;
import org.spoutcraft.client.util.ticking.TickingElement;

/**
 *
 */
public class Input extends TickingElement {
    private static final int TPS = 60;
    private final Game game;
    private boolean mouseCreated = false, keyboardCreated = false;
    private final SubscribableQueue<KeyboardEvent> keyboardQueue = new SubscribableQueue<>(false);
    private final SubscribableQueue<MouseEvent> mouseQueue = new SubscribableQueue<>(false);

    public Input(Game game) {
        super("input", TPS);
        this.game = game;
    }

    @Override
    public void onStart() {
        System.out.println("Input start");

        keyboardQueue.becomePublisher();
        mouseQueue.becomePublisher();
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
                if (!keyboardQueue.add(event)) {
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
                if (!mouseQueue.add(event)) {
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

    @Override
    public void onStop() {
        System.out.println("Input stop");

        Keyboard.destroy();
        keyboardCreated = false;
        keyboardQueue.unsubscribeAll();
        Mouse.destroy();
        mouseCreated = false;
        mouseQueue.unsubscribeAll();
    }

    public Queue<KeyboardEvent> getKeyboardQueue() {
        return keyboardQueue;
    }

    public Queue<MouseEvent> getMouseQueue() {
        return mouseQueue;
    }

    public void subscribeToKeyboard() {
        keyboardQueue.subscribe();
    }

    public void subscribeToMouse() {
        mouseQueue.subscribe();
    }

    public void unsubscribeToKeyboard() {
        keyboardQueue.unsubscribe();
    }

    public void unsubscribeToMouse() {
        mouseQueue.unsubscribe();
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
