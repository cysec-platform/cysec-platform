package eu.smesec.platform.utils;

import eu.smesec.bridge.generated.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.glassfish.jersey.logging.LoggingFeature;
import org.xml.sax.SAXException;

public final class Validator {
  private static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  private static Pattern regexWord = Pattern.compile("\\w*");
  private static Pattern regexWordSpace = Pattern.compile("[\\w ]*");
  private static Pattern regexEmail = Pattern.compile("\\w+(\\.\\w+)*\\@\\w+(\\.\\w+)+");
  private static Pattern regexAnswer = Pattern.compile("[^\\/\\>\\<\\;\\?\\*\\!\\&\\{\\}]+");

  private Validator() {}

  public static boolean validateXml(Path xsd, Path xml) {
    try {
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = factory.newSchema(new StreamSource(Files.newBufferedReader(xsd)));
      javax.xml.validation.Validator validator = schema.newValidator();
      validator.validate(new StreamSource(Files.newBufferedReader(xml)));
    } catch (IOException | SAXException e) {
      logger.warning("XML File did not validate! Reason: " + e.getMessage());
      return false;
    }
    return true;
  }


  /**
   * <p>Checks if the input contains only word character: a-zA-Z_0-9.</p>
   *
   * @param word input to check
   * @return <code>true</code> if the input contains only word character,
   *     or <code>false</code> otherwise
   */
  public static boolean validateWord(String word) {
    return word != null && regexWord.matcher(word).matches();
  }

  /**
   * <p>Checks if the input contains only word character: a-zA-Z_ 0-9.</p>
   *
   * @param word input to check
   * @return <code>true</code> if the input contains only word character,
   *     or <code>false</code> otherwise
   */
  public static boolean validateWordSpace(String word) {
    return word != null && regexWordSpace.matcher(word).matches();
  }

  /**
   * <p>Checks if the mail address is valid.</p>
   *
   * @param email email address to check
   * @return <code>true</code> if the email address is valid, or <code>false</code> otherwise
   */
  public static boolean validateEmail(String email) {
    return email != null && regexEmail.matcher(email).matches();
  }

  /**
   * <p>Checks if the input contains non of the characters: /><;?*!&{}  .</p>
   *
   * @param input input to check
   * @return <code>true</code> if the input contains non of the specified characters,
   *     or <code>false</code> otherwise
   */
  public static boolean validateAnswer(String input) {
    return input != null && regexAnswer.matcher(input).matches();
  }

  public static boolean validateUser(User user) {
    return validateWord(user.getUsername())
          && validateEmail(user.getEmail())
          && validateWordSpace(user.getFirstname())
          && validateWordSpace(user.getSurname())
          && user.getRole().stream().allMatch(Validator::validateWord);
  }
}
