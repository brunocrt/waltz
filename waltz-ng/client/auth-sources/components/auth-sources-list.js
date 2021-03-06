/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {initialiseData} from "../../common";


const bindings = {
    authSources: '<',
    consumers: '<',
    orgUnits: '<'
};


const initialState = {
    authSources: [],
    orgUnits: [],
    orgUnitsById: {},
    authSourcesGroupedByOrgUnit: []
};


const template = require('./auth-sources-list.html');


function indexById(entities = []) {
    return _.keyBy(entities, 'id');
}


function calculate(authSources = [], orgUnits = [], allConsumers = []) {
    const orgUnitsById = indexById(orgUnits);
    const consumersByAuthSource = _.keyBy(allConsumers, 'key.id');

    const authSourcesGroupedByOrgUnit = _.chain(authSources)
        .map(authSource => {
            const declaringOrgUnit = orgUnitsById[authSource.parentReference.id];
            const consumers = consumersByAuthSource[authSource.id] || { value : [] };
            return Object.assign({}, authSource, {consumers : consumers.value, declaringOrgUnit});
        })
        .groupBy('parentReference.id')
        .map((v,k) => ({ orgUnit: orgUnitsById[k], authSources: v }) )
        .value();

    return {
        authSourcesGroupedByOrgUnit
    };
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = changes =>
        Object.assign(
            vm,
            calculate(
                vm.authSources,
                vm.orgUnits,
                vm.consumers));

    vm.showDetail = selected =>
        vm.selected = selected;
}


controller.$inject = [];


const component = {
    bindings,
    controller,
    template
};


export default component;