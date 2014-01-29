/**
 * This file is part of Client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013-2014 Spoutcraft <http://spoutcraft.org/>
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

import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.flowpowered.commands.CommandException;
import com.flowpowered.commands.CommandManager;
import com.flowpowered.commands.CommandProvider;
import com.flowpowered.commands.annotated.AnnotatedCommandExecutorFactory;
import com.flowpowered.commons.queue.SubscribableQueue;
import com.flowpowered.commons.ticking.TickingElement;
import com.github.wolf480pl.jline_log4j2_appender.ConsoleSetupMessage;
import jline.console.ConsoleReader;
import org.lwjgl.*;
import org.lwjgl.input.*;
import org.lwjgl.opengl.*;
import org.spoutcraft.client.Game;
import org.spoutcraft.client.input.command.Commands;
import org.spoutcraft.client.input.command.ConsoleCommandSender;
import org.spoutcraft.client.input.event.KeyboardEvent;
import org.spoutcraft.client.input.event.MouseEvent;

/**
 *
 */
public class Input extends TickingElement {
    private static final ConsoleReaderThread readerThread = new ConsoleReaderThread();
    private static final int TPS = 60;
    private final Game game;
    private boolean mouseCreated = false, keyboardCreated = false;
    private final SubscribableQueue<KeyboardEvent> keyboardQueue = new SubscribableQueue<>(false);
    private final SubscribableQueue<MouseEvent> mouseQueue = new SubscribableQueue<>(false);
    private final ConsoleCommandSender sender;

    public Input(Game game) {
        super("input", TPS);
        this.game = game;
        final CommandManager manager = new CommandManager(false);
        final CommandProvider provider = new CommandProvider() {
            @Override
            public String getName() {
                return "client";
            }
        };
        manager.setRootCommand(manager.getCommand(provider, "root"));
        sender = new ConsoleCommandSender(game, manager);
        new AnnotatedCommandExecutorFactory(manager, provider).create(new Commands(game));
    }

    @Override
    public void onStart() {
        game.getLogger().info("Starting input");

        keyboardQueue.becomePublisher();
        mouseQueue.becomePublisher();

        if (!readerThread.isAlive()) {
            if (!readerThread.ranBefore) {
                game.getLogger().info(new ConsoleSetupMessage(readerThread.reader, "Setting up console"));
            }
            readerThread.start();
        } else {
            readerThread.getRawCommandQueue().clear();
        }
    }

    @Override
    public void onTick(long dt) {
        // Exit game if we're asked to
        if (isCloseRequested()) {
            game.close();
        }
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
        final Iterator<String> iterator = readerThread.getRawCommandQueue().iterator();
        while (iterator.hasNext()) {
            final String command = iterator.next();
            try {
                sender.processCommand(command);
            } catch (CommandException e) {
                game.getLogger().error("Exception caught processing command [" + command + "]", e);
            }
            iterator.remove();
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
        game.getLogger().info("Stopping input");

        // We make sure to end of the game, else there's no way to stop it normally (no input!)
        game.close();
        if (Keyboard.isCreated()) {
            Keyboard.destroy();
        }
        keyboardCreated = false;
        keyboardQueue.unsubscribeAll();
        if (Mouse.isCreated()) {
            Mouse.destroy();
        }
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
        return Display.isCreated() && Display.isActive();
    }

    public boolean isCloseRequested() {
        return Display.isCreated() && Display.isCloseRequested();
    }

    public void setMouseGrabbed(boolean grabbed) {
        if (Mouse.isCreated()) {
            Mouse.setGrabbed(grabbed);
        }
    }

    public int getMouseX() {
        if (Mouse.isCreated()) {
            return Mouse.getX();
        }
        return 0;
    }

    public int getMouseY() {
        if (Mouse.isCreated()) {
            return Mouse.getY();
        }
        return 0;
    }

    public boolean isKeyDown(int key) {
        return Keyboard.isCreated() && Keyboard.isKeyDown(key);
    }

    public boolean isButtonDown(int button) {
        return Mouse.isCreated() && Mouse.isButtonDown(button);
    }

    public Game getGame() {
        return game;
    }

    // TODO: this shouldn't be exposed, not thread safe, doesn't fit into the threading model either
    public void clear() throws IOException {
        readerThread.getConsole().clearScreen();
    }

    private static class ConsoleReaderThread extends Thread {
        private volatile boolean running = false;
        private volatile boolean ranBefore = false;
        private final ConsoleReader reader;
        private final ConcurrentLinkedQueue<String> rawCommandQueue = new ConcurrentLinkedQueue<>();

        public ConsoleReaderThread() {
            super("command");
            setDaemon(true);

            try {
                reader = new ConsoleReader(System.in, System.out);
                reader.setBellEnabled(false);
                reader.setExpandEvents(false);
            } catch (Exception e) {
                throw new RuntimeException("Exception caught creating the console reader!", e);
            }
        }

        @Override
        public void run() {
            ranBefore = true;
            running = true;
            try {
                while (running) {
                    // TODO: this is broken in when using "gradle run", gets spammed to hell
                    //String command = reader.readLine(">");
                    String command = reader.readLine();

                    if (command == null || command.trim().length() == 0) {
                        continue;
                    }

                    rawCommandQueue.offer(command);
                }
            } catch (IOException e) {
                reader.shutdown();
            }
        }

        public ConsoleReader getConsole() {
            return reader;
        }

        public ConcurrentLinkedQueue<String> getRawCommandQueue() {
            return rawCommandQueue;
        }
    }
}
