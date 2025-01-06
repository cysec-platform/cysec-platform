/*-
 * #%L
 * CYSEC Platform Bridge
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.smesec.cysec.platform.bridge;

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
