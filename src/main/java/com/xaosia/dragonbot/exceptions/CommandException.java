package com.xaosia.dragonbot.exceptions;

public class CommandException extends Exception {

    /**
     * Create a new CommandException
     *
     * @param message The error message.
     */
    public CommandException(String message) {
        super(message);
    }
}