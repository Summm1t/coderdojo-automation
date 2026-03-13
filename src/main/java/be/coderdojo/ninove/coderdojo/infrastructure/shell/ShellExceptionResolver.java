package be.coderdojo.ninove.coderdojo.infrastructure.shell;

import org.springframework.shell.command.CommandExceptionResolver;
import org.springframework.shell.command.CommandExecution;
import org.springframework.shell.command.CommandHandlingResult;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ShellExceptionResolver implements CommandExceptionResolver {

    @Override
    public CommandHandlingResult resolve(Exception ex) {
        if (ex instanceof CommandExecution.CommandParserExceptionsException parserEx) {
            String message = parserEx.getParserExceptions().stream()
                    .map(e -> e.getMessage())
                    .collect(Collectors.joining("\n"));
            System.err.println(message);
            System.exit(2);
        } else if (ex.getClass().getName().equals("org.springframework.shell.CommandNotFound")) {
            System.err.println(ex.getMessage() + ". Type 'help' to see available commands.");
            System.exit(1);
        }
        return null;
    }
}
