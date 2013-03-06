package org.medale.exsiter;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;
import org.medale.exsiter.Main.Functionality;

import com.jcraft.jsch.JSchException;

public class MainTest {

    @Test
    public void testGetFunctionalityToExecuteFromCommandLineInit() {
        String[][] args = { { "-init" }, { "-i" } };
        for (String[] currArgs : args) {
            Set<Functionality> functionality = Main
                    .getFunctionalityToExecuteFromCommandLine(currArgs);
            assertTrue(functionality.size() == 1);
            assertTrue(functionality.contains(Functionality.INIT));
        }
    }

    @Test
    public void testGetFunctionalityToExecuteFromCommandLineExplicitBackup() {
        String[][] args = { { "-backup" }, { "-b" } };
        for (String[] currArgs : args) {
            Set<Functionality> functionality = Main
                    .getFunctionalityToExecuteFromCommandLine(currArgs);
            assertTrue(functionality.size() == 1);
            assertTrue(functionality.contains(Functionality.BACKUP));
        }
    }

    public void runInit() throws IOException, JSchException {
        String[] args = { "-init", "-test" };
        Main.main(args);
    }

}
