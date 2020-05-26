package eu.smesec.platform.cache;

import eu.smesec.bridge.execptions.MapperException;
import eu.smesec.platform.utils.FileUtils;

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
 * <p>Wrapper class to handle Jaxb marshalling and Unmarshalling.</p>
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
   * <p>Initializes an object into a xml file.
   * The xml file will be created during this method.</p>
   *
   * @param path The path of the non-existing xml file.
   * @param t The object, which should be initialized.
   * @throws MapperException If an IOError or a JAXBError occurred.
   */
  public void init(Path path, T t) throws MapperException {
    try (BufferedOutputStream os = new BufferedOutputStream(Files.newOutputStream(path,
        StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))) {
      marshaller.marshal(t, os);
    } catch (JAXBException e) {
      throw new MapperException("General error during initializing " + classOfT.getName()
          + " into file " + path.toString() + ": " + e.getMessage());
    } catch (IOException ioe) {
      throw new MapperException("IO error during initializing " + classOfT.getName()
          + " into file " + path.toString() + ": " + ioe.getMessage());
    }
  }

  /**
   * <p>Marshals an object into a xml file.</p>
   *
   * @param path The path of the existing xml file.
   * @param t The object, which should be marshalled.
   * @throws MapperException If an IOError or a JAXBError occurred.
   */
  public void marshal(Path path, T t) throws MapperException {
    Path temp = FileUtils.asTemp(path);
    try (BufferedOutputStream os = new BufferedOutputStream(Files.newOutputStream(temp,
        StandardOpenOption.WRITE,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING))) {
      marshaller.marshal(t, os);
      Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (JAXBException e) {
      throw new MapperException("General error during marshalling " + classOfT.getName()
          + " into file " + path.toString() + ": " + e.getMessage());
    } catch (IOException ioe) {
      throw new MapperException("IO error during marshalling " + classOfT.getName()
          + " into file " + path.toString() + ": " + ioe.getMessage());
    }
  }

  /**
   * <p>Unmarshals an object from a file.</p>
   *
   * @param path The path of the existing xml file, which the object should be unmarshalled from.
   * @return The unmarshalled object.
   * @throws MapperException If an IOError, SAXParseError or a JAXBError occurred,
   */
  public T unmarshal(Path path) throws MapperException {
    try (BufferedInputStream is = new BufferedInputStream((Files.newInputStream(path,
        StandardOpenOption.READ)))) {
      return classOfT.cast(unmarshaller.unmarshal(is));
    } catch (JAXBException je) {
      Throwable cause = je.getLinkedException();
      if (cause instanceof SAXParseException) {
        SAXParseException spe = (SAXParseException) cause;
        throw new MapperException("Xml parse error during unmarshalling " + classOfT.getName()
            + " from file " + path.toString() + " in line " + spe.getLineNumber() + ":" + spe.getColumnNumber()
            + ": " + spe.getMessage());
      }
      throw new MapperException("General error during unmarshalling " + classOfT.getName()
          + " from file " + path.toString() + ": " + je.getMessage());
    } catch (IOException ioe) {
      throw new MapperException("IO error during unmarshalling " + classOfT.getName()
          + " from file " + path.toString() + ": " + ioe.getMessage());
    }
  }
}
