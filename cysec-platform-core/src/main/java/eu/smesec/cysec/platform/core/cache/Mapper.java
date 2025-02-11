/*-
 * #%L
 * CYSEC Platform Core
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
package eu.smesec.cysec.platform.core.cache;

import eu.smesec.cysec.platform.bridge.execptions.MapperException;
import eu.smesec.cysec.platform.core.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.SAXParseException;

/**
 * Wrapper class to handle Jaxb marshalling and Unmarshalling.
 *
 * @param <T> Generated jaxb class.
 */
public class Mapper<T> {
  private final Class<T> classOfT;
  private final Marshaller marshaller;
  private final Unmarshaller unmarshaller;

  Mapper(Class<T> classOfT) {
    this.classOfT = classOfT;
    try {
      // create JAXB objects
      JAXBContext context = JAXBContext.newInstance(classOfT);
      this.unmarshaller = context.createUnmarshaller();
      this.marshaller = context.createMarshaller();
      this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    } catch (JAXBException je) {
      throw new RuntimeException(je);
    }
  }

  /**
   * Initializes an object into a xml file. The xml file will be created during this method.
   *
   * @param path The path of the non-existing xml file.
   * @param t The object, which should be initialized.
   * @throws MapperException If an IOError or a JAXBError occurred.
   */
  public void init(Path path, T t) throws MapperException {
    try (BufferedOutputStream os =
        new BufferedOutputStream(
            Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))) {
      marshaller.marshal(t, os);
    } catch (JAXBException e) {
      throw new MapperException(
          "General error during initializing "
              + classOfT.getName()
              + " into file "
              + path.toString()
              + ": "
              + e.getMessage());
    } catch (IOException ioe) {
      throw new MapperException(
          "IO error during initializing "
              + classOfT.getName()
              + " into file "
              + path.toString()
              + ": "
              + ioe.getMessage());
    }
  }

  /**
   * Marshals an object into a xml file.
   *
   * @param path The path of the existing xml file.
   * @param t The object, which should be marshalled.
   * @throws MapperException If an IOError or a JAXBError occurred.
   */
  public void marshal(Path path, T t) throws MapperException {
    Path temp = FileUtils.asTemp(path);
    try (BufferedOutputStream os =
        new BufferedOutputStream(
            Files.newOutputStream(
                temp,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING))) {
      marshaller.marshal(t, os);
      Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (JAXBException e) {
      throw new MapperException(
          "General error during marshalling "
              + classOfT.getName()
              + " into file "
              + path.toString()
              + ": "
              + e.getMessage());
    } catch (IOException ioe) {
      throw new MapperException(
          "IO error during marshalling "
              + classOfT.getName()
              + " into file "
              + path.toString()
              + ": "
              + ioe.getMessage());
    }
  }

  /**
   * Unmarshals an object from a file.
   *
   * @param path The path of the existing xml file, which the object should be unmarshalled from.
   * @return The unmarshalled object.
   * @throws MapperException If an IOError, SAXParseError or a JAXBError occurred,
   */
  public T unmarshal(Path path) throws MapperException {
    try (BufferedInputStream is =
        new BufferedInputStream((Files.newInputStream(path, StandardOpenOption.READ)))) {
      return classOfT.cast(unmarshaller.unmarshal(is));
    } catch (JAXBException je) {
      Throwable cause = je.getLinkedException();
      if (cause instanceof SAXParseException) {
        SAXParseException spe = (SAXParseException) cause;
        throw new MapperException(
            "Xml parse error during unmarshalling "
                + classOfT.getName()
                + " from file "
                + path
                + " in line "
                + spe.getLineNumber()
                + ":"
                + spe.getColumnNumber()
                + ": "
                + spe.getMessage(), spe);
      }
      throw new MapperException(
          "General error during unmarshalling "
              + classOfT.getName()
              + " from file "
              + path
              + ": "
              + je.getMessage(), je);
    } catch (IOException ioe) {
      throw new MapperException(
          "IO error during unmarshalling "
              + classOfT.getName()
              + " from file "
              + path
              + ": "
              + ioe.getMessage(), ioe);
    }
  }
}
