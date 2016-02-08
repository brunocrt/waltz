/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs;

import com.khartec.waltz.model.EntityReferenceKeyedGroup;
import com.khartec.waltz.model.IdGroup;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.app_capability.AppCapabilityService;
import com.khartec.waltz.service.capability_rating.CapabilityRatingService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;


public class AppCapabilityHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        AppCapabilityService appCapabilityService = ctx.getBean(AppCapabilityService.class);


        long appId = 636;

        List<IdGroup> associated = appCapabilityService.findAssociatedCapabilitiesByApplication(appId);

        associated.forEach(e -> System.out.println(e.key() + " --- " + e.values().size() + "\n\t"+e.values()));



    }

}