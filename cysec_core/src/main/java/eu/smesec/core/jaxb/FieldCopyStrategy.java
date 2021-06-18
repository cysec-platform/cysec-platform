package eu.smesec.core.jaxb;

import org.jvnet.jaxb2_commons.lang.JAXBCopyStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.PropertyObjectLocator;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class FieldCopyStrategy extends JAXBCopyStrategy {
  private final Set<String> fields;

  public FieldCopyStrategy(String... fields) {
    this.fields = Arrays.stream(fields).collect(Collectors.toSet());
  }

  protected Object copyInternal(ObjectLocator locator, Object object) {
    if (locator instanceof PropertyObjectLocator) {
      PropertyObjectLocator pol = (PropertyObjectLocator) locator;
      String fieldName = pol.getPropertyName();
      if (fields.contains(fieldName)) {
        return super.copyInternal(locator, object);
      }
      return null;
    }
    return super.copyInternal(locator, object);
  }
}
