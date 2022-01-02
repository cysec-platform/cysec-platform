/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2022 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
package eu.smesec.cysec.platform.core.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import eu.smesec.cysec.platform.bridge.generated.Mvalue;
import eu.smesec.cysec.platform.bridge.generated.ObjectFactory;

import java.lang.reflect.Type;
import javax.xml.bind.JAXBElement;

/** Gson Mavlue adapter class to handle JAXBElement class. */
public class MValueAdapter implements JsonSerializer<Mvalue>, JsonDeserializer<Mvalue> {
  private final ObjectFactory factory;

  public MValueAdapter() {
    factory = new ObjectFactory();
  }

  @Override
  public JsonElement serialize(Mvalue src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    obj.addProperty("key", src.getKey());
    obj.addProperty("link", src.getLink());
    JAXBElement<String> value = src.getStringValueOrBinaryValue();
    if (value.getName().getLocalPart().equals("stringValue")) {
      obj.addProperty("stringValue", value.getValue());
    } else {
      obj.addProperty("binaryValue", value.getValue());
    }

    return obj;
  }

  @Override
  public Mvalue deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    Mvalue mvalue = new Mvalue();
    mvalue.setKey(obj.get("key").getAsString());
    mvalue.setLink(obj.get("link").getAsString());
    if (obj.has("stringValue")) {
      mvalue.setStringValueOrBinaryValue(
          factory.createMvalueStringValue(obj.get("stringValue").getAsString()));
    } else {
      mvalue.setStringValueOrBinaryValue(
          factory.createMvalueBinaryValue(obj.get("binaryValue").getAsString()));
    }

    return mvalue;
  }
}
