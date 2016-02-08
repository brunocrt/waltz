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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.capability_rating.AppRatingChangesAction;
import com.khartec.waltz.service.capability_rating.CapabilityRatingService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.user.UserService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class CapabilityRatingEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "capability-rating");

    private final CapabilityRatingService ratingService;
    private final ChangeLogService changeLogService;
    private final UserService userService;


    @Autowired
    public CapabilityRatingEndpoint(CapabilityRatingService ratingService, ChangeLogService changeLogService, UserService userService) {
        checkNotNull(ratingService, "ratingService must not be null");
        this.ratingService = ratingService;
        this.changeLogService = changeLogService;
        this.userService = userService;
    }


    @Override
    public void register() {

        String byParentPath = mkPath(BASE_URL, "parent", ":kind", ":id");
        String byParentAndPerspectivePath = mkPath(byParentPath, ":perspectiveCode");
        String byAppIdsPath = mkPath(BASE_URL, "apps");


        getForList(byParentPath, (request, response) -> ratingService.findByParent(getEntityReference(request)));

        getForList(byParentAndPerspectivePath, (request, response) ->
                ratingService.findByParentAndPerspective(
                        getEntityReference(request),
                        request.params("perspectiveCode")));

        post(mkPath(BASE_URL), (request, response) -> {
            requireRole(userService, request, Role.RATING_EDITOR);
            AppRatingChangesAction appRatingChangesAction = readBody(request, AppRatingChangesAction.class);

            int changeCount = ratingService.update(appRatingChangesAction);

            changeLogService.write(ImmutableChangeLog.builder()
                    .userId(getUser(request).userName())
                    .message("Updated capability ratings ( " + changeCount + " )")
                    .parentReference(appRatingChangesAction.application())
                    .severity(Severity.INFORMATION)
                    .build());

            return changeCount;
        });

        postForList(byAppIdsPath, ((request, response) -> ratingService.findByAppIds(readBody(request, Long[].class))));
    }
}