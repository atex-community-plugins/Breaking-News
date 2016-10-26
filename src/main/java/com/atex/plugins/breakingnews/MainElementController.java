package com.atex.plugins.breakingnews;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.collections.ContentListUtil;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.manager.ModelProvider;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

public class MainElementController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(MainElementController.class.getName());

    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request,
                                            final TopModel topModel,
                                            final ControllerContext context) {

        final BaselinePolicy policy = (BaselinePolicy) ModelPathUtil.getBean(context.getContentModel());

        final Set<ContentId> articleIds = new HashSet<>();
        final List<Model> articles = new ArrayList<>();

        try {
            // try to get the articles from the publishing queue.

            final ContentList queueList = policy.getContentList("publishingQueue");
            if (queueList.size() > 0) {
                final ContentId queueId = queueList.getEntry(0).getReferredContentId();
                final ContentListProvider queuePolicy = (ContentListProvider) getCMServer(context).getPolicy(queueId);
                final ContentList articlesList = queuePolicy.getContentList();

                generateModels(context.getModelProvider(), articles, articlesList, articleIds);
            }

            // then try to get the articles from the article list.

            final ContentList articlesList = policy.getContentList("articles");
            generateModels(context.getModelProvider(), articles, articlesList, articleIds);

        } catch (CMException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        ModelPathUtil.set(topModel.getLocal(), "breakingNewsArticles", articles);
    }

    private void generateModels(final ModelProvider domain, final List<Model> articles, final ContentList articlesList,
                                final Set<ContentId> articleIds) {

        if ((articlesList != null) && (articlesList.size() > 0)) {
            final Iterator<ContentReference> it = ContentListUtil.iterator(articlesList);
            while (it.hasNext()) {
                final ContentId articleId = it.next().getReferredContentId();
                if (articleIds.contains(articleId)) {
                    continue;
                }
                articleIds.add(articleId);
                try {
                    articles.add(domain.getModel(articleId));
                } catch (Exception e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        }
    }

    private PolicyCMServer getCMServer(final ControllerContext context) {
        return getCmClient(context).getPolicyCMServer();
    }
}