package eu.smesec.cysec.platform.core.jaxb;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.jvnet.jaxb2_commons.lang.JAXBCopyStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.PropertyObjectLocator;

public class FieldCopyStrategy extends JAXBCopyStrategy {
  private Set<String> fields;

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
