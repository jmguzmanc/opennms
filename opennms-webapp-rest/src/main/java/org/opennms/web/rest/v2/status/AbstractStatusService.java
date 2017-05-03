/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2.status;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.support.QueryParameters;
import org.opennms.web.rest.v2.status.application.Query;

public abstract class AbstractStatusService<T extends SeveritySupplier, X extends Query> {

    public List<T> getStatus(X query) {
        final QueryParameters queryParameters = query.getParameters();
        final SearchCondition<SeverityFilter> filter = query.getSearchCondition();
        final CriteriaBuilder criteriaBuilder = getCriteriaBuilder(queryParameters);

        // The implementors do not know anything about status/severity, therefore it is not supported to order by severity
        // on dao level.
        if (queryParameters.getOrder().getColumn().equals("severity")) {
            criteriaBuilder.clearOrder();
            criteriaBuilder.offset(null);
            criteriaBuilder.limit(null);
        }

        // Query and apply filters
        List<T> collect = findMatching(query, criteriaBuilder);
        collect = apply(collect, filter);

        // sort manually if required
        if (queryParameters.getOrder().getColumn().equals("severity")) {
            Comparator<T> comparator = Comparator.comparing(T::getSeverity);
            if (queryParameters.getOrder().isDesc()) {
                comparator = comparator.reversed();
            }
            collect.sort(comparator);
            collect = subList(collect, queryParameters);
        }

        return collect;
    }

    private List<T> subList(List<T> list, QueryParameters queryParameters) {
        return queryParameters.getPage().apply(list);
    }

    public int count(X query) {
        final QueryParameters queryParameters = query.getParameters();
        final SearchCondition<SeverityFilter> filter = query.getSearchCondition();
        final CriteriaBuilder builder = getCriteriaBuilder(queryParameters);

        // Remove limit, offset and ordering to fetch count
        builder.limit(null);
        builder.offset(null);
        builder.clearOrder();

        // If a severity is given, we must count manually!
        if (filter != null && filter.getCondition() != null && filter.getCondition().getSeverity() != null) {
            List<T> collect = findMatching(query, builder);
            collect = apply(collect, filter);
            return collect.size();
        } else {
            return countMatching(builder.toCriteria());
        }
    }

    protected abstract int countMatching(Criteria criteria);

    protected abstract List<T> findMatching(X query, CriteriaBuilder criteriaBuilder);

    protected abstract CriteriaBuilder getCriteriaBuilder(QueryParameters queryParameters);

    private Predicate<T> getSeverityFilterPredicate(final SearchCondition<SeverityFilter> searchCondition) {
        if (searchCondition != null && searchCondition.getCondition().getSeverity() != null) {
            final OnmsSeverity searchSeverity = OnmsSeverity.get(searchCondition.getCondition().getSeverity());
            switch (searchCondition.getConditionType()) {
                case EQUALS:
                    return status -> status.getSeverity().equals(searchSeverity);
                case GREATER_OR_EQUALS:
                    return status -> status.getSeverity().isGreaterThanOrEqual(searchSeverity);
                case LESS_OR_EQUALS:
                    return status -> status.getSeverity().isLessThanOrEqual(searchSeverity);
                case NOT_EQUALS:
                    return status -> !status.getSeverity().equals(searchSeverity);
            }
        }
        // Include all
        return status -> true;
    }

    private List<T> apply(List<T> statusList, SearchCondition<SeverityFilter> searchCondition) {
        if (!statusList.isEmpty() && searchCondition != null) {
            final Predicate<T> filter = getSeverityFilterPredicate(searchCondition);
            return statusList.stream().filter(filter).collect(Collectors.toList());
        }
        return statusList; // don't filter
    }

}
