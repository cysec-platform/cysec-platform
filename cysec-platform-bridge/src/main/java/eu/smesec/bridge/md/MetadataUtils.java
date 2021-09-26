package eu.smesec.bridge.md;

import eu.smesec.bridge.generated.Metadata;
import eu.smesec.bridge.generated.Mvalue;
import eu.smesec.bridge.generated.ObjectFactory;

import eu.smesec.bridge.md.annotations.MdId;
import eu.smesec.bridge.md.annotations.MdNamespace;
import eu.smesec.bridge.md.annotations.MvKey;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;

public final class MetadataUtils {
  public static final String MD_RATING = "_cysec.rating";
  public static final String MD_BADGES = "_cysec.badges";
  public static final String MD_STATE = "_cysec.state";
  public static final String MD_RESUME = "_cysec.resume";
  public static final String MD_RECOMMENDED = "_cysec.recommended";
  public static final String MD_SKILLS = "_cysec.skills";
  public static final String MD_LOGIC = "_cysec.logic";
  public static final String MD_LAST_SELECTED = "_cysec.last-selected";

  public static final String MV_MICRO_SCORE = "micro_score";
  public static final String MV_MICRO_GRADE = "micro_grade";
  public static final String MV_ID = "id";
  public static final String MV_NAME = "name";
  public static final String MV_CURRENT = "current";
  public static final String MV_ACTIVE = "active";
  public static final String MV_CLASS = "class";
  public static final String MV_IMAGE = "image";
  public static final String MV_DESCRIPTION = "description";
  public static final String MV_LINK = "link";
  public static final String MV_Q_ID = "qid";
  public static final String MV_FQCN = "fqcn";
  public static final String MV_ORDER = "order";
  public static final String MV_STRENGTH = "strength";
  public static final String MV_STRENGTH_MAX = "strengthMax";
  public static final String MV_KNOW_HOW = "knowhow";
  public static final String MV_KNOW_HOW_MAX = "knowhowMax";
  public static final String MV_ENDURANCE = "endurance";
  public static final String MV_ENDURANCE_STATE = "enduranceState";
  public static final String MV_LOGIC_ONBEGIN = "logicOnBegin";
  public static final String MV_LOGIC_PREQUESTION = "logicPreQuestion";
  public static final String MV_LOGIC_POSTQUESTION = "logicPostQuestion";
  // Denotes if a recommendation is related to a specific coach or not
  public static final String MV_GENERAL = "general";

  @Deprecated
  public static class SimpleMvalue {
    private final boolean stringType;
    private final String value;

    SimpleMvalue(Mvalue mvalue) {
      JAXBElement<String> jaxb = mvalue.getStringValueOrBinaryValue();
      this.stringType = jaxb.getName().getLocalPart().equals("stringValue");
      this.value = jaxb.getValue();
    }

    public boolean isStringType() {
      return stringType;
    }

    public String getValue() {
      return value;
    }
  }

  private static ObjectFactory factory = new ObjectFactory();

  private MetadataUtils() {}

  /**
   * <p>Creates a new Mvalue as string.</p>
   *
   * @param key The key of the mvalue.
   * @param value The string value of the mvalue.
   * @return a generated Mvalue
   */
  @Deprecated
  public static Mvalue createMvalueStr(String key, String value) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Mvalue key must be not null and not empty");
    }
    Mvalue mv = new Mvalue();
    mv.setKey(key);
    mv.setStringValueOrBinaryValue(factory.createMvalueStringValue(value));
    return mv;
  }

  /**
   * <p>Creates a new Mvalue as binary string.</p>
   *
   * @param key The key of the mvalue.
   * @param value The binary string value of the mvalue.
   * @return a generated Mvalue
   */
  @Deprecated
  public static Mvalue createMvalueBin(String key, String value) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Mvalue key must be not null and not empty");
    }
    Mvalue mv = new Mvalue();
    mv.setKey(key);
    mv.setStringValueOrBinaryValue(factory.createMvalueBinaryValue(value));
    return mv;
  }

  /**
   * <p>Creates a new metadata.</p>
   *
   * @param key The key of the metadata.
   * @param values The mvalues of the metadata.
   * @return a generated Metadata
   */
  @Deprecated
  public static Metadata createMetadata(String key, List<Mvalue> values) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Metadata key must be not null and not empty");
    }
    if (values == null || values.isEmpty()) {
      throw new IllegalArgumentException("values must be not empty");
    }
    Metadata md = new Metadata();
    md.setKey(key);
    Map<String, Mvalue> mvs = new HashMap<>(values.size());
    for (Mvalue mv : values) {
      mvs.put(mv.getKey(), mv);
    }
    md.getMvalue().addAll(mvs.values());
    return md;
  }

  /**
   * <p>Creates a simple mvalue from a generated mvalue.</p>
   *
   * @param mvalue The generated mvalue.
   * @return simple mvalue.
   */
  @Deprecated
  public static SimpleMvalue parseMvalue(Mvalue mvalue) {
    if (mvalue == null) {
      throw new IllegalArgumentException("Mvalue must be not null");
    }
    return new SimpleMvalue(mvalue);
  }

  /**
   * <p>Creates a dictionary of simple mvalues from a collection of generated mvalues.</p>
   *
   * @param values The generated mvalues
   * @return Dictionary of simple mvalues
   */
  @Deprecated
  public static Map<String, SimpleMvalue> parseMvalues(Collection<Mvalue> values) {
    Map<String, SimpleMvalue> result = new HashMap<>(values.size());
    for (Mvalue mv : values) {
      result.put(mv.getKey(), new SimpleMvalue(mv));
    }
    return result;
  }

  /**
   * Queries a collection of Mvalues for a specific key. In order to find a match, the mvalue key has to start with the
   * specified key.
   * @param key the key to look for
   * @param mvalues the collection of mvalues to query
   * @return A list of matching simple mvalues
   */
  @Deprecated
  public static Map<String, SimpleMvalue> getMvaluesByKey(String key, Map<String, SimpleMvalue> mvalues) {
    Map<String, SimpleMvalue> newMap = mvalues.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(key))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return newMap;
  }

  /**
   * <p>Casts a metadata structure into a data class.</p>
   *
   * <p>Uses reflection and metadata annotations.</p>
   *
   * @param md The metadata structure to prase
   * @param classOfT The class of the data Class
   * @return an instance of the data class, containing the content of md
   */
  public static <T> T fromMd(Metadata md, Class<T> classOfT) {
    try {
      String mdKey = md.getKey();
      if (!classOfT.isAnnotationPresent(MdNamespace.class)
            || !mdKey.startsWith(classOfT.getAnnotation(MdNamespace.class).value())) {
        throw new IllegalArgumentException("md key does not match with class namespace");
      }
      Map<String, JAXBElement<String>> values = md.getMvalue().stream()
            .collect(Collectors.toMap(Mvalue::getKey, Mvalue::getStringValueOrBinaryValue));
      T instance = classOfT.newInstance();
      for (Field field : classOfT.getDeclaredFields()) {
        // set field value
        try {
          if (field.isAnnotationPresent(MdId.class)) {
            String[] segments = mdKey.split("\\.");
            field.set(instance, segments[segments.length - 1]);
          } else if (field.isAnnotationPresent(MvKey.class)) {
            MvKey mvKeyAnnotation = field.getAnnotation(MvKey.class);
            JAXBElement<String> value = values.get(mvKeyAnnotation.value());
            if (value != null) {
              Object fieldValue = primitiveParser.get(field.getType().getName()).apply(value.getValue());
              field.set(instance, fieldValue);
            }
          }
        } catch (Exception e) {
          //todo log
        }
      }
      return instance;
    } catch (InstantiationException | IllegalAccessException iae) {
      throw new RuntimeException(iae);
    }
  }

  /**
   * <p>Casts a data class into a metadata structure.</p>
   *
   * <p>Uses reflection and metadata annotations.</p>
   *
   * @param <T> a metadata structure type
   * @param t data class instance
   * @return a metadata structure representing the data of the &lt;T&gt;
   */
  public static <T> Metadata toMd(T t) {
    try {
      Class<?> classOfT = t.getClass();
      if (!classOfT.isAnnotationPresent(MdNamespace.class)) {
        throw new IllegalArgumentException("MdNamespace annotation is missing");
      }
      StringBuilder key = new StringBuilder(classOfT.getAnnotation(MdNamespace.class).value());
      List<Mvalue> values = new ArrayList<>();
      for (Field field : t.getClass().getDeclaredFields()) {
        if (field.isAnnotationPresent(MdId.class)) {
          key.append(".").append(field.get(t).toString());
        } else if (field.isAnnotationPresent(MvKey.class)) {
          MvKey mvKeyAnnotation = field.getAnnotation(MvKey.class);
          Object value = field.get(t);
          if (value != null) {
            Mvalue mvalue = factory.createMvalue();
            mvalue.setKey(mvKeyAnnotation.value());
            JAXBElement<String> jaxb = mvKeyAnnotation.binary()
                  ? factory.createMvalueBinaryValue(value.toString())
                  : factory.createMvalueStringValue(value.toString());
            mvalue.setStringValueOrBinaryValue(jaxb);
            values.add(mvalue);
          }
        }
      }
      Metadata md = factory.createMetadata();
      md.setKey(key.toString());
      md.getMvalue().addAll(values);
      return md;
    } catch (IllegalAccessException ae) {
      throw new RuntimeException(ae);
    }
  }

  private static Map<String, Function<String, Object>> primitiveParser = new HashMap<>();
  static {
    primitiveParser.put("boolean", Boolean::parseBoolean);
    primitiveParser.put("byte", Byte::parseByte);
    primitiveParser.put("short", Short::parseShort);
    primitiveParser.put("int", Integer::parseInt);
    primitiveParser.put("long", Long::parseLong);
    primitiveParser.put("float", Float::parseFloat);
    primitiveParser.put("double", Double::parseDouble);
    primitiveParser.put("java.lang.String", s -> s);
  }
}
