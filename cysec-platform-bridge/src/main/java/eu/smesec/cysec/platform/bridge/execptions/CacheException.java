/*-
 * #%L
 * CYSEC Platform Bridge
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
package eu.smesec.cysec.platform.bridge.execptions;

public class CacheException extends Exception {
//  public static final int GENERAL_ERROR = 1;
//  public static final int IO_ERROR = 10;
//  public static final int JAXB_ERROR = 20;
//  public static final int UNKOWN_COMMAND = 50;
//
//  public static final int COMPANY_ERROR = 100;
//  public static final int COMPANY_NOT_FOUND = 104;
//  public static final int COMPANY_ALREADY_EXISTS = 105;
//
//  public static final int USER_ERROR = 200;
//  public static final int USER_NOT_FOUND = 204;
//  public static final int USER_ALREADY_EXISTS = 205;

//  private int code;

  public CacheException(/* int code, */ String message) {
    super(message);
//    this.code = code;
  }

//  public int getCode() {
//    return code;
//  }
}
