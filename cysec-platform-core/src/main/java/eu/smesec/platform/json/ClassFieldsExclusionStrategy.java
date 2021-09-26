package eu.smesec.platform.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassFieldsExclusionStrategy implements ExclusionStrategy {
  private final Map<Class<?>, Set<String>> exclusion;

  public ClassFieldsExclusionStrategy() {
    this.exclusion = new HashMap<>();
  }

  public void ignoreClassFields(Class<?> clazz, String... fieldNames) {
    exclusion.put(clazz, new HashSet<>(Arrays.asList(fieldNames)));
  }

  @Override
  public boolean shouldSkipField(FieldAttributes f) {
    Set<String> exclusionFields = exclusion.get(f.getDeclaringClass());
    return exclusionFields != null && exclusionFields.contains(f.getName());
  }

  @Override
  public boolean shouldSkipClass(Class<?> clazz) {
    return false;
  }
}
