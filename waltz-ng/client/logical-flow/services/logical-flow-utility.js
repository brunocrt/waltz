
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

import _ from "lodash";
import {authoritativeRatingColorScale} from "../../common/colors";
import {pickWorst} from "../../auth-sources/services/auth-sources-utils";


export default [
    () => {

        const enrich = (flows, flowEntities) => {
            const entitiesById = _.keyBy(flowEntities, 'id');

            return _.map(flows, f => ({
                ...f,
                sourceEntity: entitiesById[f.source.id],
                targetEntity: entitiesById[f.target.id]
            }));
        };

        const getDataTypes = (flows) => {
            return _.chain(flows)
                .map('dataType')
                .flatten()
                .uniq()
                .value();
        };

        const buildGraphTweakers = (appIds = [], decorators = []) => {

            const decoratorsByFlowId = _.groupBy(decorators, 'dataFlowId');
            const calcRating = (d) => {
                const flowId = d.data.id;
                const flowDecorators = decoratorsByFlowId[flowId] || [];
                const ratings = _.map(flowDecorators, 'rating');
                return pickWorst(ratings);
            };

            return {
                node : {
                    enter: (selection) => {
                        selection
                            .classed('wdfd-intra-node', d => _.includes(appIds, d.id))
                            .classed('wdfd-extra-node', d => ! _.includes(appIds, d.id))
                            .on('dblclick.unfix', d => { d.fx = null; d.fy = null; })
                    },
                    update: _.identity,
                    exit: _.identity
                },
                link : {
                    update: (selection) => {
                        return selection
                            .attr('stroke', d => {
                                const rating = calcRating(d);
                                return authoritativeRatingColorScale(rating);
                            })
                            .attr('fill', d => {
                                const rating = calcRating(d);
                                return authoritativeRatingColorScale(rating).brighter();
                            });
                            //
                            // .attr('marker-end', d => {
                            //     const rating = calcRating(d);
                            //     return `url(#arrowhead-${rating})`;
                            // })
                    },
                    enter: (selection) => {
                        return selection
                            .attr('stroke-width', 1.5);

                    },
                    exit: _.identity
                }
            };
        };


        const enrichDataTypeCounts = (dataTypeCounts = [], displayNameService) => {
            return _.chain(dataTypeCounts)
                .map(dc => {
                    const enriched = {
                        dataType: {
                            id: dc.entityReference.id,
                            name: displayNameService.lookup('dataType', dc.entityReference.id)
                        },
                        inbound: 0,
                        outbound: 0,
                        intra: 0,
                        total: 0
                    };
                    _.forEach(dc.tallies, t => {
                        enriched[_.lowerCase(t.id)] = t.count;
                        enriched.total += t.count;
                    });
                    return enriched;
                })
                .value();
        };


        return {
            enrich,
            getDataTypes,
            buildGraphTweakers,
            enrichDataTypeCounts
        };
    }
];
