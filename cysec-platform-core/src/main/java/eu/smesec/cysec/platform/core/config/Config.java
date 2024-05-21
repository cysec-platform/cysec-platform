/*-
 * #%L
 * CYSEC Platform Core
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
package eu.smesec.cysec.platform.core.config;

// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.jersey.logging.LoggingFeature;

public class Config {

  private static final java.util.logging.Logger LOGGER =
      Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  private static final String DEFAULT = "default";

  private List<String> sections = new ArrayList<>();
  private List<String> fields = new ArrayList<>();

  private String resourceFilename;

  interface Converters {

    String objectToString(Object o) throws IllegalArgumentException;

    Object stringToObject(String s) throws IllegalArgumentException;
  }

  private static class StringConverters implements Converters {
    @Override
    public String objectToString(Object o) {
      return (String) (o);
    }

    @Override
    public Object stringToObject(String s) {
      return s;
    }
  }

  private static class IntegerConverters implements Converters {
    @Override
    public String objectToString(Object o) {
      return "" + o;
    }

    @Override
    public Object stringToObject(String s) throws NumberFormatException {
      return Integer.parseInt(s);
    }
  }

  private static class BooleanConverters implements Converters {
    @Override
    public String objectToString(Object o) {
      return ((Boolean) (o) ? "true" : "false");
    }

    @Override
    public Object stringToObject(String s) {
      return s != null && ("true".equals(s.toLowerCase()) || "yes".equals(s.toLowerCase()));
    }
  }

  private enum ConfigSource {
    DEFAULT_VALUE,
    DEFAULT_SECTION,
    SECTION
  }

  private enum ConfigType {
    BOOLEAN(new BooleanConverters()),
    NUMERIC(new IntegerConverters()),
    SECTION_LIST(new StringConverters()),
    SECTION(new StringConverters()),
    STRING(new StringConverters());

    public static ConfigType getById(String id) {
      for (ConfigType c : values()) {
        if (c.name().toLowerCase().equals(id.toLowerCase())) {
          return c;
        }
      }
      return null;
    }

    private Converters converters;

    ConfigType(Converters converters) {
      this.converters = converters;
    }

    public Converters getConverters() {
      return converters;
    }
  }

  private class ConfigValue {
    private String value;
    private int lineNumber = -1;

    public ConfigValue(String value, int lineNumber) {
      this.value = value;
      this.lineNumber = lineNumber;
    }

    public String getValue() {
      return value;
    }

    public int getLineNumber() {
      return lineNumber;
    }

    public ConfigValue copy() {
      return new ConfigValue(value, lineNumber);
    }
  }

  private class ConfigElement implements Comparator<ConfigElement> {

    private String id;
    private String type;
    private String description;
    private String defaultValue;
    private Map<String, ConfigValue> currentValue = new HashMap<>();

    ConfigElement(String id, String type) {
      setId(id);
      setType(type);
      setDefaultValue(null);
      setDescription(null);
    }

    ConfigElement(String id, String type, String description) {
      this(id, type);
      setDescription(description);
    }

    ConfigElement(String id, String type, String description, String defValue) {
      this(id, type, description);
      setDefaultValue(defValue);
    }

    public ConfigElement copy() {
      ConfigElement ret = new ConfigElement(id, type, description);
      ret.defaultValue = defaultValue;
      // make deep copy of hashmap
      ret.currentValue = new HashMap<>();
      for (Map.Entry<String, ConfigValue> e : currentValue.entrySet()) {
        ret.currentValue.put(e.getKey(), e.getValue().copy());
      }
      return ret;
    }

    public final void setId(String id) {
      if (id == null) {
        throw new NullPointerException("id must not be null");
      }
      this.id = id.toLowerCase();
    }

    public final void setType(String type) {
      if (type == null) {
        throw new NullPointerException("type must not be null");
      } else if (ConfigType.getById(type) == null) {
        throw new IllegalArgumentException("type " + type + " is not a known config type");
      } else {
        this.type = type.toLowerCase();
      }
    }

    public final ConfigType getType() {
      return ConfigType.getById(this.type);
    }

    final void setDescription(String description) {
      this.description = description;
    }

    final String getDescription() {
      return this.description;
    }

    private String setValue(String section, String value, int lineNumber) {
      // make sure that we always have a section
      if (section == null) {
        section = DEFAULT;
      }
      if (!sections.contains(section) && !DEFAULT.equals(section)) {
        sections.add(section);
      }

      String ret = getValue(section);

      if (ret == null) {
        LOGGER.log(Level.FINE, "value for " + id + " is set to " + value);
      } else {
        LOGGER.log(Level.FINE, "value for " + id + " is modified to " + value);
      }
      // set value
      if (value != null) {
        currentValue.put(section, new ConfigValue(value, lineNumber));
      } else {
        currentValue.remove(section);
      }

      return ret;
    }

    private String getValue(String section) {
      // make sure that we always have a section
      if (section == null) {
        section = DEFAULT;
      }

      // get value
      String ret;
      if (currentValue.get(section) != null) {
        ret = currentValue.get(section).getValue();
      } else if (currentValue.get(DEFAULT) != null) {
        ret = currentValue.get(DEFAULT).getValue();
      } else {
        ret = defaultValue;
      }

      return ret;
    }

    private ConfigSource getValueSource(String section) {
      // make sure that we always have a section
      if (section == null) {
        section = DEFAULT;
      }

      // get value
      ConfigSource ret;
      if (currentValue.get(section) != null) {
        ret = ConfigSource.SECTION;
      } else if (currentValue.get(DEFAULT) != null) {
        ret = ConfigSource.DEFAULT_SECTION;
      } else {
        ret = ConfigSource.DEFAULT_VALUE;
      }

      return ret;
    }

    private String getDefaultValue() {
      return defaultValue;
    }

    public final String getStringValue(String section) {
      return (String) (getType().getConverters().stringToObject(getValue(section)));
    }

    public final String setStringValue(String section, String value, int lineNumber) {
      String ret = getStringValue(section);
      setValue(section, getType().getConverters().objectToString(value), lineNumber);
      return ret;
    }

    public final String getSectionListValue(String section) {
      return (String) (getType().getConverters().stringToObject(getValue(section)));
    }

    public final String setSectionListValue(String section, String value, int lineNumber) {
      String ret = getStringValue(section);
      setValue(section, getType().getConverters().objectToString(value), lineNumber);
      return ret;
    }

    public final String getSectionValue(String section) {
      return (String) (getType().getConverters().stringToObject(getValue(section)));
    }

    public final String setSectionValue(String section, String value, int lineNumber) {
      String ret = getStringValue(section);
      setValue(section, getType().getConverters().objectToString(value), lineNumber);
      return ret;
    }

    public final boolean getBooleanValue(String section) {
      return (Boolean) (getType().getConverters().stringToObject(getValue(section)));
    }

    public final boolean setBooleanValue(String section, boolean value, int lineNumber) {
      boolean ret = getBooleanValue(section);
      setValue(section, getType().getConverters().objectToString(value), lineNumber);
      return ret;
    }

    public final int getNumericValue(String section) {
      try {
        return (Integer) (getType().getConverters().stringToObject(getValue(section)));
      } catch (IllegalArgumentException ae) {
        LOGGER.log(
            Level.SEVERE,
            "Unable to parse "
                + id
                + "["
                + type
                + "; section:"
                + section
                + "]="
                + getValue(section)
                + "] as int (def:"
                + defaultValue
                + "/curr:"
                + getValue(section)
                + ")",
            ae);
        throw ae;
      }
    }

    public final int setNumericValue(String section, int value, int lineNumber) {
      int ret = getNumericValue(section);
      setValue(section, getType().getConverters().objectToString(value), lineNumber);
      return ret;
    }

    public final String unset(String section) {
      LOGGER.log(Level.FINE, "value for " + id + " is deleted (unset called)");
      synchronized (currentValue) {
        if (section == null) {
          for (String s : currentValue.keySet().toArray(new String[0])) {
            setValue(s, null, -1);
          }
          return null;
        } else {
          return setValue(section, null, -1);
        }
      }
    }

    @Override
    public int compare(ConfigElement o1, ConfigElement o2) {
      return o1.id.compareToIgnoreCase(o2.id);
    }

    public final void setDefaultValue(String newDefaultValue) {
      this.defaultValue = newDefaultValue;
      LOGGER.log(Level.FINE, "Default value set to " + id + "=" + this.defaultValue);
    }
  }

  private Config() {
    // All OK
  }

  /**
   * *
   *
   * <p>Creates an config object following the spec given in the resource file.
   *
   * @param ressourceFile filename of the resource file
   * @throws IOException if an error happens while reading the file
   */
  protected Config(String ressourceFile) throws IOException {
    this();
    if (ressourceFile != null) {
      readRessources(ressourceFile);
    }
  }

  private void setResouceFilename(String ressouceFilename) {
    this.resourceFilename = ressouceFilename;
  }

  public String getResouceFilename() {
    return this.resourceFilename;
  }

  private void readRessources(String resourceFile) throws IOException {
    if (resourceFile == null) {
      throw new IOException("resource file name may not be null");
    }
    setResouceFilename(resourceFile);
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                this.getClass().getClassLoader().getResourceAsStream(resourceFile),
                StandardCharsets.UTF_8))) {
      String line = reader.readLine();
      while (line != null) {
        if (Pattern.matches("\\s*//.*", line)) {
          // ignore comment lines
        } else if (Pattern.matches("\\s*", line)) {
          // ignore  empty lines
        } else {
          try (Scanner scanner = new Scanner(line)) {
            scanner.useDelimiter("\\s*;\\s*");
            while (scanner.hasNext()) {
              String token = scanner.next().trim();
              if ("boolean".equals(token.toLowerCase())) {
                String name = scanner.next().trim();
                boolean val = "true".equals(scanner.next().toLowerCase().trim());
                String desc = scanner.next().trim();
                createBooleanConfigValue(name, desc, val);
              } else if ("string".equals(token.toLowerCase())) {
                String name = scanner.next().trim();
                String val = scanner.next().trim();
                if ("".equals(val)) {
                  val = null;
                }
                String desc = scanner.next().trim();
                createStringConfigValue(name, desc, val);
              } else if ("numeric".equals(token.toLowerCase())) {
                String name = scanner.next().trim();
                int val = Integer.parseInt(scanner.next().trim());
                String desc = scanner.next().trim();
                createNumericConfigValue(name, desc, val);
              } else if ("section_list".equals(token.toLowerCase())) {
                String name = scanner.next().trim();
                String val = scanner.next().trim();
                if ("".equals(val)) {
                  val = null;
                }
                String desc = scanner.next().trim();
                createSectionListConfigValue(name, desc, val);
              } else if ("section".equals(token.toLowerCase())) {
                String name = scanner.next().trim();
                String val = scanner.next().trim();
                if ("".equals(val)) {
                  val = null;
                }
                String desc = scanner.next().trim();
                createSectionConfigValue(name, desc, val);
              } else {
                throw new IOException(
                    "encountered unknown field type: " + token + " (line was \"" + line + "\")");
              }
            }
          }
        }
        line = reader.readLine();
      }
    } catch (NullPointerException npe) {
      throw new IOException("unable to read resource file " + resourceFile, npe);
    }
  }

  static Config defaultConfig = null;
  private final Map<String, ConfigElement> configData = new ConcurrentHashMap<>();

  public static Config getDefault() throws IOException {
    return createConfig(null);
  }

  /**
   * *
   *
   * <p>Returns a deep copy of this config store.
   *
   * @return the copy
   */
  public Config copy() {
    Config dst = new Config();
    synchronized (configData) {
      Set<Map.Entry<String, ConfigElement>> it = configData.entrySet();
      for (Map.Entry<String, ConfigElement> p : it) {
        dst.configData.put(p.getKey(), p.getValue().copy());
      }
    }
    return dst;
  }

  /**
   * *
   *
   * <p>Reverts config store to all default values.
   */
  public void clear() {
    synchronized (configData) {
      Set<Map.Entry<String, ConfigElement>> it = configData.entrySet();
      for (Map.Entry<String, ConfigElement> p : it) {
        p.getValue().unset(null);
      }
    }
  }

  private String setValue(String section, String id, String value, int lineNumber)
      throws IOException {
    ConfigElement c = configData.get(id.toLowerCase());
    if (c == null) {
      throw new IOException("unknown key \"" + id + "\" when setting value");
    }
    String ret = c.getValue(section);

    if (c.getType() == ConfigType.NUMERIC) {
      setNumericValue(section, id, Integer.parseInt(value), lineNumber);
    } else if (c.getType() == ConfigType.BOOLEAN) {
      setBooleanValue(
          section,
          id,
          value != null
              && ("yes".equals(value.toLowerCase()) || "true".equals(value.toLowerCase())),
          lineNumber);
    } else if (c.getType() == ConfigType.STRING) {
      setStringValue(section, id, value, lineNumber);
    } else if (c.getType() == ConfigType.SECTION_LIST) {
      setSectionListValue(section, id, value, lineNumber);
    } else if (c.getType() == ConfigType.SECTION) {
      setSectionValue(section, id, value, lineNumber);
    } else {
      throw new NullPointerException("type not implemented");
    }
    return ret;
  }

  /**
   * *
   *
   * <p>Creates a new boolean config value in the store.
   *
   * @param id the name (id) of the new value
   * @param description the description for the value
   * @param dval the default value
   */
  public void createBooleanConfigValue(String id, String description, boolean dval) {
    synchronized (configData) {
      if (configData.get(id.toLowerCase()) == null) {
        ConfigElement ele = new ConfigElement(id, "boolean", description);
        configData.put(id.toLowerCase(), ele);
        ele.setDefaultValue(dval ? "true" : "false");
        LOGGER.log(Level.FINE, "Created boolean config variable " + id.toLowerCase());
        this.fields.add(id.toLowerCase());
      } else {
        throw new IllegalArgumentException("id \"" + id + "\" is already defined");
      }
    }
  }

  private static synchronized Config createConfig(String res) throws IOException {
    if (defaultConfig == null) {
      defaultConfig = new Config(res);
    }
    return defaultConfig;
  }

  /**
   * *
   *
   * <p>Sets a boolean value in the application config.
   *
   * @param section the section of the config to be affected (null for default section)
   * @param id key which should be set
   * @param value Value to be set in key
   * @param lineNumber the line number of the respective file (for error messages)
   * @return old value before setting to new value
   * @throws NullPointerException if key does not exist in configData
   * @throws ClassCastException if key is not of type boolean
   */
  public boolean setBooleanValue(String section, String id, boolean value, int lineNumber) {
    ConfigElement ele = configData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException("id " + id + " is not known to the config subsystem");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.BOOLEAN) {
      throw new ClassCastException(
          "config type missmatch when accessing ID "
              + id
              + " (expected: boolean; is: "
              + type.name()
              + ")");
    }
    return ele.setBooleanValue(section, value, lineNumber);
  }

  /**
   * *
   *
   * <p>Gets a boolean value from the application config.
   *
   * @param section the section of the config to be affected (null for default section)
   * @param id key which should be set
   * @return current value of the specified key
   * @throws NullPointerException if key does not exist in configData
   * @throws ClassCastException if key is not of type boolean
   */
  public boolean getBooleanValue(String section, String id) {
    ConfigElement ele = configData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException("id " + id + " is not known to the config subsystem");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.BOOLEAN) {
      throw new ClassCastException(
          "config type missmatch when accessing ID "
              + id
              + " (expected: boolean; is: "
              + type.name()
              + ")");
    }
    return ele.getBooleanValue(section);
  }

  /**
   * *
   *
   * <p>Creates a new numeric config value in the store.
   *
   * @param id the name (id) of the new value
   * @param description the description for the value
   * @param dval the default value
   */
  public void createNumericConfigValue(String id, String description, int dval) {
    synchronized (configData) {
      if (configData.get(id.toLowerCase()) == null) {
        ConfigElement ele = new ConfigElement(id, "numeric", description, "" + dval);
        configData.put(id.toLowerCase(), ele);
        LOGGER.log(
            Level.FINE,
            "Created numeric config variable " + id.toLowerCase() + "[numeric]=" + dval);
        this.fields.add(id.toLowerCase());
      } else {
        throw new IllegalArgumentException("id \"" + id + "\" is already defined");
      }
    }
  }

  /**
   * *
   *
   * <p>Creates a new section config value in the store.
   *
   * @param id the name (id) of the new value
   * @param description the description for the value
   * @param dval the default value
   */
  public void createSectionConfigValue(String id, String description, String dval) {
    synchronized (configData) {
      if (configData.get(id.toLowerCase()) == null) {
        ConfigElement ele = new ConfigElement(id, "section", description, dval);
        configData.put(id.toLowerCase(), ele);
        LOGGER.log(
            Level.FINE,
            "Created section config variable " + id.toLowerCase() + "[section]=" + dval);
        this.fields.add(id.toLowerCase());
      } else {
        throw new IllegalArgumentException("id \"" + id + "\" is already defined");
      }
    }
  }

  /**
   * *
   *
   * <p>Sets a numeric value in the application config.
   *
   * @param section section from which the value should be taken. null defaults to default section
   * @param id key which should be set
   * @param value Value to be set in key
   * @param lineNumber the line number of the respective file (for error messages)
   * @return old value before setting to new value
   * @throws NullPointerException if key does not exist in configData
   * @throws ClassCastException if key is not of type boolean
   */
  public int setNumericValue(String section, String id, int value, int lineNumber) {
    ConfigElement ele = configData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException("id " + id + " is not known to the config subsystem");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.NUMERIC) {
      throw new ClassCastException(
          "config type missmatch when accessing ID "
              + id
              + " (expected: numeric; is: "
              + type.name()
              + ")");
    }
    return ele.setNumericValue(section, value, lineNumber);
  }

  /**
   * *
   *
   * <p>Gets a numeric value from the application config.
   *
   * @param section section from which the value should be taken. null defaults to default section
   * @param id key which should be set
   * @return current value of the specified key
   * @throws NullPointerException if key does not exist in configData
   * @throws ClassCastException if key is not of type boolean
   */
  public int getNumericValue(String section, String id) {
    ConfigElement ele = configData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException("id " + id + " is not known to the config subsystem");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.NUMERIC) {
      throw new ClassCastException(
          "config type missmatch when accessing ID "
              + id
              + " (expected: numeric; is: "
              + type.name()
              + ")");
    }
    return ele.getNumericValue(section);
  }

  /**
   * *
   *
   * <p>Creates a section_list config item.
   *
   * <p>Creates a config item with a case insensitive identifier. The content of the item may not be
   * null.
   *
   * @param id Name of config item (case insensitive)
   * @param description Description of value to be written
   * @param dval Default content if not set
   * @return True if item did not exist and was successfully created
   */
  public boolean createSectionListConfigValue(String id, String description, String dval) {
    synchronized (configData) {
      if (configData.get(id.toLowerCase()) == null) {
        ConfigElement ele = new ConfigElement(id, "section_list", description);
        configData.put(id.toLowerCase(), ele);
        ele.setDefaultValue(dval);
        LOGGER.log(Level.FINE, "Created section_list config variable " + id.toLowerCase());
        this.fields.add(id.toLowerCase());
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * *
   *
   * <p>Set a section_list value to a config parameter.
   *
   * @param section section from which the value should be taken. null defaults to default section
   * @param id key which should be set
   * @param value Value to be set in key
   * @param lineNumber the line number of the respective file (for error messages)
   * @return the previously set value
   * @throws NullPointerException when id is unknown or value is null
   * @throws ClassCastException when id is not a String setting
   */
  public String setSectionListValue(String section, String id, String value, int lineNumber) {
    ConfigElement ele = configData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException("unable to get id " + id + " from config subsystem");
    }
    if (value == null) {
      throw new NullPointerException(
          "unable to set id " + id + " of config subsystem " + "(value may not be null)");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.SECTION_LIST) {
      throw new ClassCastException(
          "Unable to cast type to correct class (" + type.name() + " is not section_list)");
    }
    return ele.setSectionListValue(section, value, lineNumber);
  }

  /**
   * *
   *
   * <p>Sets the value of a section_list type.
   *
   * @param section section from which the value should be taken. 'Null' defaults to default section
   * @param id the id of the value to be retrieved
   * @return a list of sections
   * @throws NullPointerException when id is unknown
   * @throws ClassCastException when id is not a String setting
   */
  public String[] getSectionListValue(String section, String id) {
    ConfigElement ele = configData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException(
          "unable to get id " + id + " from config subsystem (unknown element)");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.SECTION_LIST) {
      throw new ClassCastException(
          "Unable to cast type to correct class (" + type.name() + " is not typed section_list)");
    }
    String secList = ele.getSectionListValue(section);
    if (secList == null) {
      return new String[0];
    } else {
      return secList.split("\\s*,\\s*");
    }
  }

  /**
   * *
   *
   * <p>Set a section value to a config parameter.
   *
   * @param section section from which the value should be taken. null defaults to default section
   * @param id key which should be set
   * @param value Value to be set in key
   * @param lineNumber the line number of the respective file (for error messages)
   * @return the previously set value
   * @throws NullPointerException when id is unknown or value is null
   * @throws ClassCastException when id is not a String setting
   */
  public String setSectionValue(String section, String id, String value, int lineNumber) {
    ConfigElement ele = configData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException("unable to get id " + id + " from config subsystem");
    }
    if (value == null) {
      throw new NullPointerException(
          "unable to set id " + id + " of config subsystem " + "(value may not be null)");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.SECTION) {
      throw new ClassCastException(
          "Unable to cast type to correct class (expected: section; is: " + type.name() + ")");
    }
    return ele.setSectionListValue(section, value, lineNumber);
  }

  /**
   * *
   *
   * <p>Gets the value of a section type.
   *
   * @param section section from which the value should be taken. 'Null' defaults to default section
   * @param id the id of the value to be retrieved
   * @return a section name
   * @throws NullPointerException when id is unknown
   * @throws ClassCastException when id is not a String setting
   */
  public String getSectionValue(String section, String id) {
    ConfigElement ele = configData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException(
          "unable to get value for \""
              + id
              + "\" from config subsystem (unknown element in section \""
              + section
              + "\")");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.SECTION) {
      throw new ClassCastException(
          "Unable to cast type to correct class (expected: section; is: " + type.name() + ")");
    }
    return ele.getSectionValue(section);
  }

  /**
   * *
   *
   * <p>Creates a String config item.
   *
   * <p>Creates a config item with a case insensitive identifier. The content of the item may not be
   * null.
   *
   * @param id Name of config item (case insensitive)
   * @param description Description of value to be written
   * @param dval Default content if not set
   * @return True if item did not exist and was successfully created
   */
  public boolean createStringConfigValue(String id, String description, String dval) {
    synchronized (configData) {
      if (configData.get(id.toLowerCase()) == null) {
        ConfigElement ele = new ConfigElement(id, "STRING", description);
        configData.put(id.toLowerCase(), ele);
        ele.setDefaultValue(dval);
        LOGGER.log(Level.FINE, "Created String config variable " + id.toLowerCase());
        this.fields.add(id.toLowerCase());
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * *
   *
   * <p>Removes a config value declaration from the config container.
   *
   * @param id the id of the value to be removed
   * @return true if the id did exist
   */
  public boolean removeConfigValue(String id) {
    synchronized (configData) {
      if (configData.get(id.toLowerCase()) == null) {
        return false;
      } else {
        configData.remove(id.toLowerCase());
        this.fields.remove(id.toLowerCase());
        return true;
      }
    }
  }

  /**
   * *
   *
   * <p>Set a String value to a config parameter.
   *
   * @param section section from which the value should be taken. 'null' defaults to default section
   * @param id Name of config item (case insensitive)
   * @param value Value to be set in key
   * @param lineNumber the line number of the respective file (for error messages)
   * @return the previously set value
   * @throws NullPointerException when id is unknown or value is null
   * @throws ClassCastException when id is not a String setting
   */
  public String setStringValue(String section, String id, String value, int lineNumber) {
    ConfigElement ele = configData.get(id.toLowerCase());
    if (ele == null || value == null) {
      throw new NullPointerException("unable to get id " + id + " from config subsystem");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.STRING) {
      throw new ClassCastException(
          "Unable to cast type to correct class (expected: string; is: " + type.name() + ")");
    }
    return ele.setStringValue(section, value, lineNumber);
  }

  /**
   * *
   *
   * <p>Sets the value of a string type.
   *
   * @param section section from which the value should be taken. 'null' defaults to default section
   * @param id the id of the value to be retrieved
   * @return the previously set value
   * @throws NullPointerException when id is unknown
   * @throws ClassCastException when id is not a String setting
   */
  public String getStringValue(String section, String id) {
    ConfigElement ele = configData.get(id.toLowerCase());
    if (ele == null) {
      throw new NullPointerException(
          "unable to get id " + id + " from config subsystem (unknown element)");
    }
    ConfigType type = ele.getType();
    if (type != ConfigType.STRING) {
      throw new ClassCastException(
          "Unable to cast type to correct class (expected: string; is: " + type.name() + ")");
    }
    return ele.getStringValue(section);
  }

  public Map<String, ConfigElement> getMap() {
    return configData;
  }

  /**
   * *
   *
   * <p>Loads a config file and validates input.
   *
   * <p>Loads and parses a file according to the resources configuration
   *
   * @param filename name of the property file to be read
   * @throws IOException if the file is not parsed properly
   */
  public void load(String filename) throws IOException {

    Pattern sectionPat = Pattern.compile("^\\s*\\[([a-zA-Z0-9_\\-]+)\\]\\s*$");
    Pattern keyValuePat = Pattern.compile("^\\s*([^=]+)\\s*=\\s*(.*)\\s*$");

    InputStream fstream = this.getClass().getClassLoader().getResourceAsStream(filename);
    if (fstream == null) {
      try {
        fstream = new FileInputStream(filename);
      } catch (FileNotFoundException fnfe) {
        LOGGER.log(Level.WARNING, "Unable to load config file \"" + filename + "\"", fnfe);
        throw fnfe;
      }
    }

    try (BufferedReader br =
        new BufferedReader(new InputStreamReader(fstream, StandardCharsets.UTF_8))) {
      String line;
      String section = null;
      int lineCounter = 1;
      while ((line = br.readLine()) != null) {
        if (Pattern.matches("\\s*//.*", line)) {
          // ignore comment lines
        } else if (Pattern.matches("\\s*", line)) {
          // ignore empty lines
        } else {
          Matcher m = sectionPat.matcher(line);

          if (m.matches()) {
            // set current section
            section = m.group(1);
            LOGGER.log(Level.FINE, "parsing section [" + section + "]");

          } else {
            // parse KV pair
            m = keyValuePat.matcher(line);
            if (m.matches()) {
              String key = m.group(1).trim();
              String value = m.group(2).trim();

              // add value to store
              setValue(section, key, value, lineCounter);
            } else {
              throw new IOException("unable to parse \"" + line + "\" (line:" + lineCounter + ")");
            }
          }
        }
        lineCounter++;
      }
    }
  }

  /**
   * *
   *
   * <p>Writes config to a string.
   *
   * <p>Writes a commented file according to the configuration
   *
   * @return The configuration as string
   * @throws IOException if error writing file
   */
  public String store() throws IOException {
    StringWriter bw = new StringWriter();
    store(bw);
    bw.close();
    return bw.toString();
  }

  /**
   * *
   *
   * <p>Writes a config file.
   *
   * <p>Writes a commented file according to the configuration
   *
   * @param filename name of the property file to be written
   * @throws IOException if error writing to file
   */
  public void store(String filename) throws IOException {

    // get list of sections

    try (BufferedWriter bw =
        new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8))) {
      store(bw);
    }
  }

  private void store(Writer bw) throws IOException {
    // Dump default section (all values in definition order)
    bw.write("[default]" + System.lineSeparator());
    dumpSection(null, bw, true);

    // Dump all other sections
    for (String section : sections) {
      bw.write(System.lineSeparator() + "[" + section + "]" + System.lineSeparator());
      dumpSection(section, bw, false);
    }
  }

  /**
   * Get the descriptive text of the named configuration item.
   *
   * @param id identification of the configuration item
   * @return the configuration item description
   */
  public String getDescription(String id) {
    ConfigElement c = configData.get(id.toLowerCase());
    return c == null ? null : c.getDescription();
  }

  private String getValue(String section, String id) {
    ConfigElement c = configData.get(id.toLowerCase());
    return c == null ? null : c.getValue(section);
  }

  /**
   * Gets the default value of the named configuration item.
   *
   * @param id identification of the configuration item
   * @return the configuration items' default value
   */
  public String getDefaultValue(String id) {
    ConfigElement c = configData.get(id.toLowerCase());
    return c == null ? null : c.getDefaultValue();
  }

  private void dumpSection(String section, Writer w, boolean withComments) throws IOException {
    for (String field : fields) {
      synchronized (this.configData) {
        if (this.configData.get(field.toLowerCase()) == null) {
          throw new IOException(
              "inconsistency deteceted in internal storage when querying field " + field);
        }
      }
      if (withComments) {
        w.write(
            "// ******************************************************************************"
                + System.lineSeparator());
        w.write("// name: " + field + System.lineSeparator());
        w.write(
            "// ******************************************************************************"
                + System.lineSeparator());
        w.write(wrap("// ", getDescription(field), 77) + System.lineSeparator());
        w.write(
            "// ******************************************************************************"
                + System.lineSeparator());
        w.write("// default: " + getDefaultValue(field) + System.lineSeparator());
      }
      if (configData.get(field.toLowerCase()).getValueSource(section) == ConfigSource.SECTION) {
        w.write(field + " = " + getValue(section, field) + System.lineSeparator());
      }
      if (withComments) {
        w.write(System.lineSeparator());
      }
    }
  }

  private static String wrap(String prefix, String string, int lineLength) {
    StringBuilder b = new StringBuilder();
    for (String line : string.split(Pattern.quote(System.lineSeparator()))) {
      b.append(wrapLine(prefix, line, lineLength));
    }
    return b.toString();
  }

  private static String wrapLine(String prefix, String line, int lineLength) {
    if (line.length() == 0) {
      return "";
    }
    if (line.length() <= lineLength) {
      return prefix + line;
    }
    String[] words = line.split(" ");
    StringBuilder allLines = new StringBuilder();
    StringBuilder trimmedLine = new StringBuilder().append(prefix);
    for (String word : words) {
      if (trimmedLine.length() + 1 + word.length() <= lineLength) {
        trimmedLine.append(word).append(" ");
      } else {
        allLines.append(trimmedLine).append(System.lineSeparator());
        trimmedLine = new StringBuilder().append(prefix);
        trimmedLine.append(word).append(" ");
      }
    }
    if (trimmedLine.length() > 0) {
      allLines.append(trimmedLine);
    }
    return allLines.toString();
  }
}
