

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

function store($http, BaseApiUrl) {
    const base = BaseApiUrl + '/capability-rating';

    function findByParent(kind, id) {
        return $http
            .get(`${base}/parent/${kind}/${id}`)
            .then(result => result.data);
    }

    function findByParentAndPerspective(kind, id, perspectiveCode) {
        return $http
            .get(`${base}/parent/${kind}/${id}/${perspectiveCode}`)
            .then(result => result.data);
    }

    function update(action) {
        return $http
            .post(`${base}`, action);
    }


    function findByAppIds(ids) {
        return $http
            .post(`${base}/apps`, ids)
            .then(result => result.data);
    }


    return {
        findByParent,
        findByParentAndPerspective,
        findByAppIds,
        update
    };
}

store.$inject = ['$http', 'BaseApiUrl'];

export default store;