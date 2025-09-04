/*-
 * #%L
 * CYSEC Platform Core
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
package eu.smesec.cysec.platform.core.auth;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CryptTypeTest {

    @Test
    void enumValues_shouldBeCorrect() {
        CryptType[] values = CryptType.values();
        assertThat(values).hasSize(4);
        assertThat(values).containsExactly(
            CryptType.MD5,
            CryptType.SHA256,
            CryptType.SHA512,
            CryptType.PLAIN
        );
    }

    @Test
    void getId_shouldReturnCorrectIds() {
        assertThat(CryptType.MD5.getId()).isEqualTo("1");
        assertThat(CryptType.SHA256.getId()).isEqualTo("5");
        assertThat(CryptType.SHA512.getId()).isEqualTo("6");
        assertThat(CryptType.PLAIN.getId()).isEqualTo("99");
    }

    @Test
    void getDefault_shouldReturnSHA512() {  // default should be secure
        assertThat(CryptType.getDefault()).isEqualTo(CryptType.SHA512);
    }

    @Test
    void getById_shouldReturnCorrectCryptType() {
        assertThat(CryptType.getById("1")).isEqualTo(CryptType.MD5);
        assertThat(CryptType.getById("5")).isEqualTo(CryptType.SHA256);
        assertThat(CryptType.getById("6")).isEqualTo(CryptType.SHA512);
        assertThat(CryptType.getById("99")).isEqualTo(CryptType.PLAIN);
    }

    @Test
    void getById_shouldReturnNullForUnknownId() {
        assertThat(CryptType.getById("0")).isNull();
        assertThat(CryptType.getById("100")).isNull();
        assertThat(CryptType.getById("invalid")).isNull();  
        assertThat(CryptType.getById("")).isNull();
        assertThat(CryptType.getById(null)).isNull(); // null input
    }

    @Test
    void valueOf_shouldReturnCorrectEnum() {
        assertThat(CryptType.valueOf("MD5")).isEqualTo(CryptType.MD5);
        assertThat(CryptType.valueOf("SHA256")).isEqualTo(CryptType.SHA256);
        assertThat(CryptType.valueOf("SHA512")).isEqualTo(CryptType.SHA512);
        assertThat(CryptType.valueOf("PLAIN")).isEqualTo(CryptType.PLAIN);
    }

    @Test
    void valueOf_shouldThrowExceptionForInvalidName() {
        assertThatThrownBy(() -> CryptType.valueOf("INVALID"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void name_shouldReturnCorrectName() {   // standard enum method
        assertThat(CryptType.MD5.name()).isEqualTo("MD5");
        assertThat(CryptType.SHA256.name()).isEqualTo("SHA256");
        assertThat(CryptType.SHA512.name()).isEqualTo("SHA512");
        assertThat(CryptType.PLAIN.name()).isEqualTo("PLAIN");
    }

    @Test
    void ordinal_shouldReturnCorrectOrdinal() {
        assertThat(CryptType.MD5.ordinal()).isEqualTo(0);
        assertThat(CryptType.SHA256.ordinal()).isEqualTo(1);
        assertThat(CryptType.SHA512.ordinal()).isEqualTo(2);
        assertThat(CryptType.PLAIN.ordinal()).isEqualTo(3);
    }

    @Test
    void toString_shouldReturnEnumName() {
        assertThat(CryptType.MD5.toString()).isEqualTo("MD5");
        assertThat(CryptType.SHA256.toString()).isEqualTo("SHA256");
        assertThat(CryptType.SHA512.toString()).isEqualTo("SHA512");
        assertThat(CryptType.PLAIN.toString()).isEqualTo("PLAIN");
    }

    @Test
    void getDefault_shouldAlwaysReturnSameInstance() {
        CryptType default1 = CryptType.getDefault();
        CryptType default2 = CryptType.getDefault();
        
        assertThat(default1).isSameAs(default2);
        assertThat(default1).isEqualTo(CryptType.SHA512);
    }

    @Test
    void enumComparison_shouldWorkCorrectly() {
        CryptType md5 = CryptType.MD5;
        CryptType anotherMd5 = CryptType.MD5;
        CryptType sha256 = CryptType.SHA256;
        
        assertThat(md5).isEqualTo(anotherMd5);
        assertThat(md5).isSameAs(anotherMd5);
        assertThat(md5).isNotEqualTo(sha256);
    }
}
