package eu.smesec.bridge;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

public class CommandTest {
  private String function;
  private String[] arguments;
  private Command commandDto;

  @Before
  public void setUp() {
    function = "addBlock";
    arguments = new String[]{"b2", "b3", "b4"};
  }

  @After
  public void tearDown() {
    function = null;
    arguments = null;
  }

  @Test
  public void ctorTwoArguments() {
    commandDto = new Command(function, arguments);
    assertNotNull(commandDto);
    assertEquals(function, commandDto.getFunction());
    assertEquals(arguments, commandDto.getArguments());
  }


  @Test
  public void ctorOneArgument() {
    commandDto = new Command(function);
    assertNotNull(commandDto);
    assertEquals(function, commandDto.getFunction());
    assertNull(commandDto.getArguments());
  }
}
