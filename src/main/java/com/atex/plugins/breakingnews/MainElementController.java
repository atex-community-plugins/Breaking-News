package com.atex.plugins.breakingnews;

import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.policy.ConfigurableContentListWrapper;
import com.polopoly.cm.app.policy.ContentListLazyModel;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policymvc.PolicyModelDomain;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;
import org.apache.commons.lang.StringUtils;

import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainElementController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(MainElementController.class.getName());


    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request,
                                           final TopModel topModel,
                                           final ControllerContext context) {

        String hiddenBreakingNewsArticlesInCookie = request.getCookieValue("breakingnews");
        PolicyModelDomain domain = getCmClient(context).getPolicyModelDomain();

        Set<String> hiddenBreakingNewsArticles = new HashSet<>();
        Set<String> hiddenBreakingNewsArticlesInCookieSet = new HashSet<>();

        if (hiddenBreakingNewsArticlesInCookie != null && !hiddenBreakingNewsArticlesInCookie.trim().isEmpty()) {
            String[] hiddenBreakingNewsArticlesInCookieArray = URLDecoder.decode(hiddenBreakingNewsArticlesInCookie).split(",");
            for (String hiddenBreakingNewsArticleInCookie : hiddenBreakingNewsArticlesInCookieArray) {
                hiddenBreakingNewsArticlesInCookieSet.add(hiddenBreakingNewsArticleInCookie);
            }
        }

        List<Model> articles = new ArrayList<>();
        try {

            BaselinePolicy policy = (BaselinePolicy) getCMServer(context).getPolicy(context.getContentId());
            ContentList queueList = policy.getContentList("publishingQueue");
            if (queueList.size() > 0) {
                ContentId queueId = queueList.getEntry(0).getReferredContentId();
                ContentListProvider queuePolicy = (ContentListProvider) getCMServer(context).getPolicy(queueId);
                ContentList articlesList = queuePolicy.getContentList();

                if (articlesList.size() > 0) {
                    for (int i = 0; i < articlesList.size(); i++) {
                        try {
                            ContentId articleContentId = articlesList.getEntry(i).getReferredContentId().getContentId();
                            articles.add(domain.getModel(articleContentId));
                            if (hiddenBreakingNewsArticlesInCookieSet.contains(articleContentId.getContentIdString())) {
                                hiddenBreakingNewsArticles.add(articleContentId.getContentIdString());
                            }
                        } catch (CMException e) {
                            continue;
                        }
                    }
                }
            }

        } catch (CMException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        ContentListLazyModel contentListLazyModel = (ContentListLazyModel) ModelPathUtil.get(context.getContentModel(), "articles/list");
        ConfigurableContentListWrapper configurableContentListWrapper = null;
        try {
            configurableContentListWrapper = (ConfigurableContentListWrapper) contentListLazyModel.getContentList();
            for (Iterator<ContentReference> iterator = configurableContentListWrapper.getListIterator(); iterator.hasNext(); ) {

                ContentReference contentReference = iterator.next();
                String articleContentId = contentReference.getReferredContentId().getContentIdString();
                articles.add(context.getModelProvider().getModel(articleContentId));

                if (hiddenBreakingNewsArticlesInCookieSet.contains(articleContentId)) {
                    hiddenBreakingNewsArticles.add(articleContentId);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        topModel.getLocal().setChild("hiddenBreakingNewsArticles", hiddenBreakingNewsArticles);
        topModel.getLocal().setChild("hiddenBreakingNewsArticlesInCookies", StringUtils.join(hiddenBreakingNewsArticles, ','));
        topModel.getLocal().setAttribute("breakingNewsArticles", articles);

    }

    private final PolicyCMServer getCMServer(final ControllerContext context) {
        return getCmClient(context).getPolicyCMServer();
    }
}