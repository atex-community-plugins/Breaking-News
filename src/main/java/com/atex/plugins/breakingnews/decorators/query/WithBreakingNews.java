package com.atex.plugins.breakingnews.decorators.query;

import com.polopoly.search.solr.QueryDecorator;
import org.apache.solr.client.solrj.SolrQuery;

public class WithBreakingNews implements QueryDecorator {

    public SolrQuery decorate(SolrQuery sourceQuery) {
        sourceQuery.addFilterQuery("breakingNews_b:true");
        return sourceQuery;
    }
}
