package eu.smesec.bridge.md;

import eu.smesec.bridge.generated.Metadata;
import eu.smesec.bridge.md.annotations.MdId;
import eu.smesec.bridge.md.annotations.MdNamespace;
import eu.smesec.bridge.md.annotations.MvKey;
import java.util.Arrays;
import java.util.Map;

@MdNamespace(MetadataUtils.MD_RECOMMENDED)
public class Recommendation {
  @MdId protected String key;
  @MvKey(MetadataUtils.MV_NAME)
  protected String title;
  @MvKey(MetadataUtils.MV_DESCRIPTION)
  protected String description;
  @MvKey(MetadataUtils.MV_ORDER)
  protected int order;
  @MvKey(MetadataUtils.MV_LINK)
  protected String link;

  public Recommendation() {}

  public Recommendation(String key, String title, String description, int order, String link) {
    this.key = key;
    this.title = title;
    this.description = description;
    this.order = order;
    this.link = link;
  }

  public String getKey() {
    return key;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public int getOrder() {
    return order;
  }

  public String getLink() {
    return link;
  }

  @Deprecated
  public static Metadata toMd(Recommendation recommendation)  {
    return MetadataUtils.createMetadata(MetadataUtils.MD_RECOMMENDED + "." + recommendation.key,
          Arrays.asList(
                MetadataUtils.createMvalueStr(MetadataUtils.MV_NAME, recommendation.title),
                MetadataUtils.createMvalueStr(MetadataUtils.MV_DESCRIPTION, recommendation.description),
                MetadataUtils.createMvalueStr(MetadataUtils.MV_ORDER, Integer.toString(recommendation.order)),
                MetadataUtils.createMvalueStr(MetadataUtils.MV_LINK, recommendation.link)
          ));
  }

  @Deprecated
  public static Recommendation fromMd(Metadata md) {
    String key = md.getKey();
    Map<String, MetadataUtils.SimpleMvalue> values = MetadataUtils.parseMvalues(md.getMvalue());
    if (!key.startsWith(MetadataUtils.MD_RECOMMENDED + ".")) {
      throw new IllegalArgumentException("Metadata key has wrong namespace for recommendation");
    }
    return new Recommendation(key.substring(MetadataUtils.MD_RECOMMENDED.length()),
          values.get(MetadataUtils.MV_NAME).getValue(),
          values.get(MetadataUtils.MV_DESCRIPTION).getValue(),
          Integer.parseInt(values.get(MetadataUtils.MV_ORDER).getValue()),
          values.get(MetadataUtils.MV_LINK).getValue());
  }
}
