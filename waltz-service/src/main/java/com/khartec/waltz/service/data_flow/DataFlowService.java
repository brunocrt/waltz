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

package com.khartec.waltz.service.data_flow;

import com.khartec.waltz.data.data_flow.DataFlowDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.dataflow.DataFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class DataFlowService {

    private final DataFlowDao dataFlowDao;


    @Autowired
    public DataFlowService(DataFlowDao dataFlowDao) {
        checkNotNull(dataFlowDao, "dataFlowDao must not be null");
        
        this.dataFlowDao = dataFlowDao;
    }


    public List<DataFlow> findByEntityReference(EntityReference ref) {
        return dataFlowDao.findByEntityReference(ref);
    }


    public List<DataFlow> findByAppIds(List<Long> appIds) {
        return dataFlowDao.findByApplicationIds(appIds);
    }


    public int[] addFlows(List<DataFlow> flows) {
        return dataFlowDao.addFlows(flows);
    }


    public int[] removeFlows(List<DataFlow> flows) {
        return dataFlowDao.removeFlows(flows);
    }

}