package org.medale.exsiter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.medale.exsiter.Main.Functionality;

public class MainTest {

    @Test
    public void testGetFunctionalityToExecuteFromCommandLineInit() {
        String[][] args = { { "-init" }, { "-i" } };
        for (String[] currArgs : args) {
            Functionality functionality = Main
                    .getFunctionalityToExecuteFromCommandLine(currArgs);
            assertEquals(Functionality.INIT, functionality);
        }
    }

    @Test
    public void testGetFunctionalityToExecuteFromCommandLineExplicitBackup() {
        String[][] args = { { "-backup" }, { "-b" }, {} };
        for (String[] currArgs : args) {
            Functionality functionality = Main
                    .getFunctionalityToExecuteFromCommandLine(currArgs);
            assertEquals(Functionality.BACKUP, functionality);
        }
    }

}
