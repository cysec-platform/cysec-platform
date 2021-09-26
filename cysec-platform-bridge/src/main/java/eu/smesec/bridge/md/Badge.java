package eu.smesec.bridge.md;

import eu.smesec.bridge.generated.Metadata;
import eu.smesec.bridge.md.annotations.MdId;
import eu.smesec.bridge.md.annotations.MdNamespace;
import eu.smesec.bridge.md.annotations.MvKey;
import java.util.Arrays;
import java.util.Map;

@MdNamespace(MetadataUtils.MD_BADGES)
public class Badge {
  @MdId protected String clazz;
  @MvKey(MetadataUtils.MV_NAME)
  protected String name;
  @MvKey(MetadataUtils.MV_IMAGE)
  protected String imagePath;
  @MvKey(MetadataUtils.MV_DESCRIPTION)
  protected String description;

  public Badge(){}

  public Badge(String clazz, String name, String imagePath, String description) {
    this.clazz = clazz;
    this.name = name;
    this.imagePath = imagePath;
    this.description = description;
  }

  public String getClazz() {
    return clazz;
  }

  public String getName() {
    return name;
  }

  public String getImagePath() {
    return imagePath;
  }

  public String getDescription() {
    return description;
  }

  @Deprecated
  public static Metadata toMd(Badge badge)  {
    return MetadataUtils.createMetadata(MetadataUtils.MD_BADGES + "." + badge.getClazz(),
          Arrays.asList(
                MetadataUtils.createMvalueStr(MetadataUtils.MV_NAME, badge.getName()),
                MetadataUtils.createMvalueStr(MetadataUtils.MV_IMAGE, badge.getImagePath()),
                MetadataUtils.createMvalueStr(MetadataUtils.MV_DESCRIPTION, badge.getDescription())
          ));
  }

  @Deprecated
  public static Badge fromMd(Metadata md) {
    String key = md.getKey();
    Map<String, MetadataUtils.SimpleMvalue> values = MetadataUtils.parseMvalues(md.getMvalue());
    if (!key.startsWith(MetadataUtils.MD_BADGES + ".")) {
      throw new IllegalArgumentException("Metadata key has wrong namespace for badges");
    }
    return new Badge(key.substring(MetadataUtils.MD_BADGES.length()),
          values.get(MetadataUtils.MV_NAME).getValue(),
          values.get(MetadataUtils.MV_IMAGE).getValue(),
          values.get(MetadataUtils.MV_DESCRIPTION).getValue());
  }
}
