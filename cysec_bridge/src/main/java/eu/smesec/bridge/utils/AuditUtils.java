package eu.smesec.bridge.utils;

import eu.smesec.bridge.generated.Audit;
import eu.smesec.bridge.generated.UserAction;
import java.time.LocalDateTime;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public final class AuditUtils {
  private static DatatypeFactory factory;

  static {
    try {
      factory = DatatypeFactory.newInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private AuditUtils() {}

  /**
   * <p>Creates a new audit.
   * Uses now as time stamp.</p>
   *
   * @param userId The id of the current user.
   * @param userAction The action of this audit.
   * @param before The old value before this audit.
   * @param after The new value after this audit.
   * @return the new audit object
   */
  public static Audit createAudit(String userId, UserAction userAction,
                                  String before, String after) {
    return createAudit(userId, userAction, before, after, LocalDateTime.now());
  }

  /**
   * <p>Creates a new audit.</p>
   *
   * @param userId The id of the current user
   * @param userAction The action of this audit
   * @param before The old value before this audit
   * @param after The new value after this audit
   * @param time The time stamp of this audit
   * @return the new audit object
   */
  public static Audit createAudit(String userId, UserAction userAction, String before,
                                  String after, LocalDateTime time) {
    Audit audit = new Audit();
    audit.setUser(userId);
    audit.setAction(userAction);
    audit.setBefore(before);
    audit.setAfter(after);
    audit.setTime(factory.newXMLGregorianCalendar(time.toString()));
    return audit;
  }

  /**
   * <p>Creates a new XmlGregorianCalendar object from now.</p>
   *
   * @return the gregorian representation of the current point
   */
  public static XMLGregorianCalendar now() {
    return toXmlGregorianCalendar(LocalDateTime.now());
  }

  /**
   * <p>Creates a new XmlGregorianCalendar object from an instant.</p>
   *
   * @param instant the point in time to be converted
   * @return  converted gregorian representation
   */
  public static XMLGregorianCalendar toXmlGregorianCalendar(LocalDateTime instant) {
    return factory.newXMLGregorianCalendar(instant.toString());
  }
}
