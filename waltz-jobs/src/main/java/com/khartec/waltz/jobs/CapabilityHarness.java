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

import com.khartec.waltz.model.applicationcapability.CapabilityUsage;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.capability.CapabilityService;
import com.khartec.waltz.service.capability_usage.CapabilityUsageService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;


public class CapabilityHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        CapabilityUsageService capabilityUsageService = ctx.getBean(CapabilityUsageService.class);
        CapabilityService capabilityService = ctx.getBean(CapabilityService.class);

        CapabilityUsage usage = capabilityUsageService.getUsage(3000L);
        System.out.println(usage);


        // capabilityService.assignLevels();

        List<Capability> caps = capabilityService.findByAppIds(33L, 39L, 31L, 130L, 179L);
        caps.forEach(System.out::println);
        System.out.println(" :: " + caps.size());
    }



}