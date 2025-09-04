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
package eu.smesec.cysec.platform.core.utils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import javax.ws.rs.core.PathSegment;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PathSegmentUtilsTest {

    @Test
    void combine_shouldReturnEmptyStringForEmptyList() {
        List<PathSegment> segments = Collections.emptyList();
        
        String result = PathSegmentUtils.combine(segments);
        
        assertThat(result).isEmpty();
    }

    @Test
    void combine_shouldReturnSingleSegmentPath() {
        PathSegment segment = mock(PathSegment.class);
        when(segment.getPath()).thenReturn("users");
        
        String result = PathSegmentUtils.combine(Collections.singletonList(segment));
        
        assertThat(result).isEqualTo("users");
    }

    @Test
    void combine_shouldCombineMultipleSegments() {
        PathSegment segment1 = mock(PathSegment.class);
        PathSegment segment2 = mock(PathSegment.class);
        PathSegment segment3 = mock(PathSegment.class);
        
        when(segment1.getPath()).thenReturn("api");
        when(segment2.getPath()).thenReturn("v1");
        when(segment3.getPath()).thenReturn("users");
        
        List<PathSegment> segments = Arrays.asList(segment1, segment2, segment3);
        
        String result = PathSegmentUtils.combine(segments);
        
        assertThat(result).isEqualTo("api/v1/users");
    }

    @Test
    void combine_shouldHandleEmptySegments() {
        PathSegment segment1 = mock(PathSegment.class);
        PathSegment segment2 = mock(PathSegment.class);
        PathSegment segment3 = mock(PathSegment.class);
        
        when(segment1.getPath()).thenReturn("api");
        when(segment2.getPath()).thenReturn("");
        when(segment3.getPath()).thenReturn("users");
        
        List<PathSegment> segments = Arrays.asList(segment1, segment2, segment3);
        
        String result = PathSegmentUtils.combine(segments);
        
        assertThat(result).isEqualTo("api//users");
    }

    @Test
    void combine_shouldHandleSpecialCharacters() {
        PathSegment segment1 = mock(PathSegment.class);
        PathSegment segment2 = mock(PathSegment.class);
        
        when(segment1.getPath()).thenReturn("api-v1");
        when(segment2.getPath()).thenReturn("user_profiles");
        
        List<PathSegment> segments = Arrays.asList(segment1, segment2);
        
        String result = PathSegmentUtils.combine(segments);
        
        assertThat(result).isEqualTo("api-v1/user_profiles");
    }

    @Test
    void combine_shouldHandleNumericSegments() {
        PathSegment segment1 = mock(PathSegment.class);
        PathSegment segment2 = mock(PathSegment.class);
        PathSegment segment3 = mock(PathSegment.class);
        
        when(segment1.getPath()).thenReturn("users");
        when(segment2.getPath()).thenReturn("123");
        when(segment3.getPath()).thenReturn("profile");
        
        List<PathSegment> segments = Arrays.asList(segment1, segment2, segment3);
        
        String result = PathSegmentUtils.combine(segments);
        
        assertThat(result).isEqualTo("users/123/profile");
    }
}
