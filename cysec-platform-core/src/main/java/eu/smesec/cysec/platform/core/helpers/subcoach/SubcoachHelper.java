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
package eu.smesec.cysec.platform.core.helpers.subcoach;

import eu.smesec.cysec.platform.bridge.FQCN;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.generated.Answer;
import eu.smesec.cysec.platform.bridge.generated.Question;
import eu.smesec.cysec.platform.bridge.generated.SubcoachInstances;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;

import java.util.List;

public class SubcoachHelper {

    public static FQCN getFirstFqcn(String companyId, FQCN fqcn, CacheAbstractionLayer cal, String instantiatorId) throws CacheException {
        InstantiatorData data = InstantiatorData.ofInstantiatorId(companyId, fqcn, cal, instantiatorId);
        if (data.instances.isEmpty()) {
            throw new IllegalStateException("Cannot get first instance if no subcoaches are instantiated");
        }

        return FQCN.from(fqcn.getRootCoachId(), data.subcoachId, data.instances.get(0).getSubcoachId());
    }


    public final static class InstantiatorData {
        private final String instantiatorId;
        private final String subcoachId;
        private final List<SubcoachInstances.SubcoachInstance> instances;

        private InstantiatorData(String instantiatorId, String subcoachId, List<SubcoachInstances.SubcoachInstance> instances) {
            this.instantiatorId = instantiatorId;
            this.subcoachId = subcoachId;
            this.instances = instances;
        }

        public static InstantiatorData ofInstantiatorId(String companyId, FQCN fqcn, CacheAbstractionLayer cal, String instantiatorId) throws CacheException {
            Answer answer = cal.getAnswer(companyId, fqcn, instantiatorId);
            Question instantiatorQuestion = cal.getQuestion(fqcn.getCoachId(), instantiatorId);
            return new InstantiatorData(instantiatorId, instantiatorQuestion.getSubcoachId(), answer.getSubcoachInstances().getSubcoachInstance());
        }
    }
}
