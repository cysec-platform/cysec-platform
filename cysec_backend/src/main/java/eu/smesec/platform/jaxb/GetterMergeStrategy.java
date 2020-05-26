package eu.smesec.platform.jaxb;

import org.jvnet.jaxb2_commons.lang.JAXBMergeStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GetterMergeStrategy extends JAXBMergeStrategy {
  private String getter;

  public GetterMergeStrategy(String getter) {
    this.getter = getter;
  }

  protected Object mergeInternal(ObjectLocator leftLocator, ObjectLocator rightLocator, Collection leftCollection, Collection rightCollection) {
    Collection<Object> merged = new ArrayList<>();
    Map<Object, Object> leftMap = new HashMap<>(leftCollection.size());
    try {
      // left lookup table
      for (Object item : leftCollection) {
        Class<?> clazz = item.getClass();
        Method m = clazz.getMethod(getter);
        leftMap.putIfAbsent(m.invoke(item), item);
      }
      // merge
      for (Object item : rightCollection) {
        Class<?> clazz = item.getClass();
        Method m = clazz.getMethod(getter);
        Object leftItem = leftMap.get(m.invoke(item));
        merged.add(leftItem != null ? merge(leftLocator, rightLocator, leftItem, item) : item);
      }
      return merged;
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
    }
    return rightCollection;
  }
}