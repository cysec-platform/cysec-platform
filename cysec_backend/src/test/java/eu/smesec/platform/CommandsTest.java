package eu.smesec.platform;

import eu.smesec.bridge.Commands;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CommandsTest {

    @Test
    public void testToString() {

        String expected = "loadBlock";
        String actual = Commands.LOAD_BLOCK.toString();

        assertEquals(expected, actual);

        expected = "updateAvailableBlocks";
        actual = Commands.UPDATE_AVAILABLE_BLOCKS.toString();

        assertEquals(expected, actual);
    }
}