/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
import _ from "lodash";
import {selectBest} from "../ratings/directives/viewer/coloring-strategies";


/**
 * Calculates the set of capabilities required to
 * fully describe a given set of app capabilities.
 * This may include parent capabilities of the
 * explicit capabilities.
 *
 * @param appCapabilities
 * @param allCapabilities
 * @returns {*}
 */
function includeParentCapabilities(appCapabilities, allCapabilities) {
    const capabilitiesById = _.keyBy(allCapabilities, 'id');

    const toCapabilityId = appCap => appCap.capabilityId;
    const lookupCapability = id => capabilitiesById[id];
    const extractParentIds = c => ([c.level1, c.level2, c.level3, c.level4, c.level5]);

    return _.chain(appCapabilities)
        .map(toCapabilityId)
        .map(lookupCapability)
        .map(extractParentIds)
        .flatten()
        .compact()
        .uniq()
        .map(lookupCapability)
        .compact()
        .value();
}


function calculateCapabilities(allCapabilities, appCapabilities) {
    const explicitCapabilityIds = _.chain(appCapabilities)
            .map('capabilityId')
            .uniq()
            .value();

    const capabilities = includeParentCapabilities(appCapabilities, allCapabilities);

    return {
        initiallySelectedIds: explicitCapabilityIds,
        explicitCapabilityIds,
        capabilities
    };
}



function controller($scope,
                    $q,
                    $stateParams,
                    appGroupStore,
                    appStore,
                    complexityStore,
                    dataFlowViewService,
                    userService,
                    capabilityStore,
                    appCapabilityStore,
                    ratingStore,
                    technologyStatsService,
                    assetCostViewService,
                    bookmarkStore)
{
    const { id }  = $stateParams;

    const assetCosts = {
        stats: [],
        costs: [],
        loading: false
    };

    const vm = this;


    const isUserAnOwner = member =>
            member.role === 'OWNER'
            && member.userId === vm.user.userName;


    dataFlowViewService.initialise(id, 'APP_GROUP', 'EXACT')
        .then(flows => vm.dataFlows = flows);

    assetCostViewService.initialise(id, 'APP_GROUP', 'EXACT', 2015)
        .then(costs => vm.assetCostData = costs);

    bookmarkStore
        .findByParent({ id , kind: 'APP_GROUP'})
        .then(bookmarks => vm.bookmarks = bookmarks);


    appGroupStore.getById(id)
        .then(groupDetail => vm.groupDetail = groupDetail)
        .then(groupDetail => _.map(groupDetail.applications, 'id'))
        .then(appIds => $q.all([
            appStore.findByIds(appIds),
            complexityStore.findBySelector(id, 'APP_GROUP', 'EXACT'),
            capabilityStore.findAll(),
            appCapabilityStore.findApplicationCapabilitiesByAppIds(appIds),
            ratingStore.findByAppIds(appIds),
            technologyStatsService.findBySelector(id, 'APP_GROUP', 'EXACT')
        ]))
        .then(([
            apps,
            complexity,
            allCapabilities,
            appCapabilities,
            ratings,
            techStats
        ]) => {
            vm.applications = apps;
            vm.complexity = complexity;
            vm.allCapabilities = allCapabilities;
            vm.appCapabilities = appCapabilities;
            vm.ratings = ratings;
            vm.techStats = techStats;
        })
        .then(() => calculateCapabilities(vm.allCapabilities, vm.appCapabilities))
        .then(result => Object.assign(vm, result));

    userService
        .whoami()
        .then(u => vm.user = u);

    vm.isGroupEditable = () => {
        if (!vm.groupDetail) return false;
        if (!vm.user) return false;
        return _.some(vm.groupDetail.members, isUserAnOwner );
    };

    vm.ratingColorStrategy = selectBest;

    vm.onAssetBucketSelect = bucket => {
        $scope.$applyAsync(() => {
            assetCostViewService.selectBucket(bucket);
            assetCostViewService.loadDetail()
                .then(data => vm.assetCostData = data);
        })
    };

    vm.loadFlowDetail = () => dataFlowViewService.loadDetail();

    vm.assetCosts = assetCosts;
}

controller.$inject = [
    '$scope',
    '$q',
    '$stateParams',
    'AppGroupStore',
    'ApplicationStore',
    'ComplexityStore',
    'DataFlowViewService',
    'UserService',
    'CapabilityStore',
    'AppCapabilityStore',
    'RatingStore',
    'TechnologyStatisticsService',
    'AssetCostViewService',
    'BookmarkStore'
];


export default {
    template: require('./app-group-view.html'),
    controller,
    controllerAs: 'ctrl'
};
