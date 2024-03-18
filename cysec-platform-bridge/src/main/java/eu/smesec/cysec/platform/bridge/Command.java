/*-
 * #%L
 * CYSEC Platform Bridge
 * %%
 * Copyright (C) 2020 - 2024 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

public class Command {

  private String function;
  private String[] arguments;

  public Command(String function, String[] arguments) {
    this.function = function;
    this.arguments = arguments;
  }

  public Command(String function) {
    this.function = function;
    this.arguments = null;
  }

  public String getFunction() {
    return function;
  }

  public String[] getArguments() {
    return arguments;
  }
}
