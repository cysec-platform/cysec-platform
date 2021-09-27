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
package eu.smesec.cysec.platform.bridge.utils;

import eu.smesec.cysec.platform.bridge.execptions.TokenExpiredException;
import eu.smesec.cysec.platform.bridge.generated.Token;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public final class TokenUtils {
  private static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static DatatypeFactory factory;

  static {
    try {
      factory = DatatypeFactory.newInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private TokenUtils() {}

  public static final String TOKEN_RESET = "reset";
//  public static final String TOKEN_REPLICA = "replica";

  public static String generateRandomAlphanumericToken(int length) {
    SecureRandom secureRandom = new SecureRandom();
    char[] buf = new char[length];
    for (int i = 0; i < length; i++) {
      buf[i] = alphabet.charAt(secureRandom.nextInt(alphabet.length()));
    }
    return new String(buf);
  }

  public static String generateRandomHexToken(int byteLength) {
    SecureRandom secureRandom = new SecureRandom();
    byte[] token = new byte[byteLength];
    secureRandom.nextBytes(token);
    return new BigInteger(1, token).toString(16); //hex encoding
  }

  public static Token createToken(String id, String value) {
    return createToken(id, value, null);
  }

  public static Token createToken(String id, String value, LocalDateTime expiry) {
    Token token = new Token();
    token.setId(id);
    token.setValue(value);
    if (expiry != null) {
      token.setExpiry(factory.newXMLGregorianCalendar(expiry.toString()));
    }
    return token;
  }

  public static boolean verifyToken(Token token, String value) throws TokenExpiredException {
    if (token == null) {
      throw new IllegalArgumentException("token cannot be null");
    }
    XMLGregorianCalendar tokenExpiry = token.getExpiry();
    if (tokenExpiry != null) {
      LocalDate expiry = LocalDate.of(tokenExpiry.getYear(),
            tokenExpiry.getMonth(),
            tokenExpiry.getDay());
      if (LocalDate.now().isAfter(expiry)) {
        throw new TokenExpiredException(token);
      }
    }
    return token.getValue().equals(value);
  }
}
