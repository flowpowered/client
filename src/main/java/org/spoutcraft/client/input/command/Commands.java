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
package org.spoutcraft.client.input.command;

import java.io.IOException;

import com.flowpowered.commands.CommandArguments;
import com.flowpowered.commands.CommandException;
import com.flowpowered.commands.CommandSender;
import com.flowpowered.commands.annotated.CommandDescription;
import org.spoutcraft.client.Game;

public class Commands {
    private final Game game;

    public Commands(Game game) {
        this.game = game;
    }

    @CommandDescription (name = "cls", usage = "cls", desc = "Clears the game console", help = "Use this command to clear the game console. This does not remove the text from the logs.")
    private void onCommandClear(CommandSender sender, CommandArguments args) throws CommandException {
        try {
            game.getInput().clear();
        } catch (IOException e) {
            throw new CommandException(e);
        }
    }

    @CommandDescription (name = "stop", usage = "stop", desc = "Stops the game", help = "Use this command only when you want to stop the game!")
    private void onCommandStop(CommandSender sender, CommandArguments args) throws CommandException {
        game.close();
    }

    @CommandDescription (name = "version", usage = "version", desc = "Displays the game version", help = "Use this command to display the game version.")
    private void onCommandVersion(CommandSender sender, CommandArguments args) throws CommandException {
        sender.sendMessage("Running version " + game.getVersion());
    }
}

