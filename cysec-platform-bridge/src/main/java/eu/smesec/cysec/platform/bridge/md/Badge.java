/*-
 * #%L
 * CYSEC Platform Bridge
 * %%
 * Copyright (C) 2020 - 2021 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.smesec.cysec.platform.bridge.md;

import eu.smesec.cysec.platform.bridge.generated.Metadata;
import eu.smesec.cysec.platform.bridge.md.annotations.MdId;
import eu.smesec.cysec.platform.bridge.md.annotations.MdNamespace;
import eu.smesec.cysec.platform.bridge.md.annotations.MvKey;

import java.util.Arrays;
import java.util.Map;

@MdNamespace(MetadataUtils.MD_BADGES)
public class Badge {
  @MdId
  protected String clazz;
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
