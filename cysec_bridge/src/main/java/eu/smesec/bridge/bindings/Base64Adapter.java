package eu.smesec.bridge.bindings;

import java.util.Base64;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Base64Adapter extends XmlAdapter<byte[], String> {

  @Override
  public String unmarshal(byte[] v) throws Exception {
    return Base64.getEncoder().encodeToString(v);
  }

  @Override
  public byte[] marshal(String v) throws Exception {
    return Base64.getDecoder().decode(v);
  }
}
