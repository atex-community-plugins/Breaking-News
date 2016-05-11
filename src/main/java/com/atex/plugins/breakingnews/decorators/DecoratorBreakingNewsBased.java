package com.atex.plugins.breakingnews.decorators;

import com.atex.plugins.baseline.collection.searchbased.decorators.DecoratorBasePolicy;
import com.atex.plugins.breakingnews.decorators.query.WithBreakingNews;
import com.polopoly.search.solr.QueryDecorator;

public class DecoratorBreakingNewsBased extends DecoratorBasePolicy {

    @Override
    public QueryDecorator getDecorator() {
        return new WithBreakingNews();
    }
}
