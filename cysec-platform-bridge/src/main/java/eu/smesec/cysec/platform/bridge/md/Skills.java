package eu.smesec.cysec.platform.bridge.md;

import eu.smesec.cysec.platform.bridge.md.annotations.MdNamespace;
import eu.smesec.cysec.platform.bridge.md.annotations.MvKey;

@MdNamespace(MetadataUtils.MD_SKILLS)
public class Skills {
  @MvKey(MetadataUtils.MV_IMAGE)
  protected String image;

  @MvKey(MetadataUtils.MV_STRENGTH)
  protected double strength;
  @MvKey(MetadataUtils.MV_STRENGTH_MAX)
  protected double strengthMax;

  @MvKey(MetadataUtils.MV_KNOW_HOW)
  protected double knowHow;
  @MvKey(MetadataUtils.MV_KNOW_HOW_MAX)
  protected double knowHowMax;

  @MvKey(MetadataUtils.MV_ENDURANCE)
  protected int endurance;

  public Skills() {}

  public Skills(String image, double strength, double strengthMax,
                double knowHow, double knowHowMax, int endurance) {
    this.image = image;
    this.strength = strength;
    this.strengthMax = strengthMax;
    this.knowHow = knowHow;
    this.knowHowMax = knowHowMax;
    this.endurance = endurance;
  }

  public String getImage() {
    return image;
  }

  public double getStrength() {
    return strength;
  }

  public double getStrengthMax() {
    return strengthMax;
  }

  public double getKnowHow() {
    return knowHow;
  }

  public double getKnowHowMax() {
    return knowHowMax;
  }

  public int getEndurance() {
    return endurance;
  }
}
