package com.dmc.d1.cqrs.codegen;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by davidclelland on 19/05/2016.
 */
public class CodeGen {

    public static void main(String[] args) {
        checkState(args.length == 4);

        CodeGenType codeGenType = CodeGenType.valueOf(args[0]);
        String rootPackageToScan = args[1];
        String generatedSourceDirectory = args[2];
        String generatedPackageName = args[3];

        try {

            if (CodeGenType.COMMAND_HANDLER == codeGenType) {
                CommandHandlerGenerator generator = new CommandHandlerGenerator(rootPackageToScan,
                        generatedSourceDirectory,
                        generatedPackageName);

                generator.generate();
            } else if (CodeGenType.EVENT_HANDLER == codeGenType) {
                EventHandlerGenerator generator = new EventHandlerGenerator(rootPackageToScan,
                        generatedSourceDirectory,
                        generatedPackageName);

                generator.generate();
            } else if (CodeGenType.EVENT == codeGenType) {
                EventAndCommandGenerator generator = new EventAndCommandGenerator(
                        generatedSourceDirectory);

                generator.generate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate code", e);
        }
    }
}
