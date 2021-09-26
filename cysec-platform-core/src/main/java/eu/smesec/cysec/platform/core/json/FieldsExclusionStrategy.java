package eu.smesec.cysec.platform.core.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FieldsExclusionStrategy implements ExclusionStrategy {
  private Set<String> exclusion;

  public FieldsExclusionStrategy(String... fieldNames) {
    exclusion = new HashSet<>(Arrays.asList(fieldNames));
  }

  @Override
  public boolean shouldSkipField(FieldAttributes f) {
    return exclusion.contains(f.getName());
  }

  @Override
  public boolean shouldSkipClass(Class<?> clazz) {
    return false;
  }
}
