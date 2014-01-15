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
package org.spoutcraft.client;

import org.spoutcraft.client.input.Input;
import org.spoutcraft.client.network.Network;
import org.spoutcraft.client.nterface.Interface;
import org.spoutcraft.client.physics.Physics;
import org.spoutcraft.client.universe.Universe;

/**
 * The game class.
 */
public class Game {
    private final Object wait = new Object();
    private volatile boolean running = false;
    private final Universe universe;
    private final Physics physics;
    private final Interface nterface;
    private final Network network;
    private final Input input;

    static {
        try {
            Class.forName("org.spoutcraft.client.universe.block.material.Materials");
        } catch (Exception ex) {
            System.out.println("Couldn't load the default materials");
        }
    }

    public Game() {
        universe = new Universe(this);
        physics = new Physics(this);
        nterface = new Interface(this);
        network = new Network(this);
        input = new Input(this);
    }

    private void start() {
        universe.start();
        physics.start();
        nterface.start();
        input.start();
        network.start();
    }

    private void stop() {
        nterface.stop();
        physics.stop();
        universe.stop();
        network.stop();
        input.stop();
    }

    public Universe getUniverse() {
        return universe;
    }

    public Physics getPhysics() {
        return physics;
    }

    public Interface getInterface() {
        return nterface;
    }

    public Network getNetwork() {
        return network;
    }

    public Input getInput() {
        return input;
    }

    /**
     * Starts the game and causes the current thread to wait until the {@link #exit()} method is called. When this happens, the thread resumes and the game is stopped. Interrupting the thread will not
     * cause it to exit, only calling {@link #exit()} will.
     */
    public void open() {
        start();
        running = true;
        synchronized (wait) {
            while (isRunning()) {
                try {
                    wait.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
        stop();
    }

    /**
     * Wakes up the thread waiting for the game to exit (by having called {@link #open()}) and allows it to resume it's activity to trigger the end of the game.
     */
    public void exit() {
        running = false;
        synchronized (wait) {
            wait.notifyAll();
        }
    }

    /**
     * Returns true if the game is running, false if otherwise.
     *
     * @return Whether or not the game is running
     */
    public boolean isRunning() {
        return running;
    }
}
