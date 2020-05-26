package eu.smesec.platform.jaxb;

import org.jvnet.jaxb2_commons.lang.JAXBCopyStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.PropertyObjectLocator;

import java.util.*;
import java.util.stream.Collectors;

public class ClassFieldCopyStrategy extends JAXBCopyStrategy {
  public static final JAXBCopyStrategy INSTANCE = new JAXBCopyStrategy();

  private Map<Class<?>, Set<String>> fields;

  public ClassFieldCopyStrategy() {
    this.fields = new HashMap<>();
  }

  public void addFields(Class<?> clazz, String ...fields) {
    this.fields.putIfAbsent(clazz, Arrays.stream(fields)
        .collect(Collectors.toSet()));
  }

  protected Object copyInternal(ObjectLocator locator, Object object) {
    if (locator instanceof PropertyObjectLocator) {
      PropertyObjectLocator pol = (PropertyObjectLocator) locator;
      Class<?> type = pol.getObject().getClass();
      String fieldName = pol.getPropertyName();
      Set<String> enabledFields = fields.get(type);
      if (enabledFields != null && enabledFields.contains(fieldName)) {
        return super.copyInternal(locator, object);
      }
    }
    return null;
  }
}
