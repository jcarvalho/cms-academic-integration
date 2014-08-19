package org.fenixedu.cms.domain.researchUnit.componenets;

import com.google.common.collect.Lists;
import org.fenixedu.bennu.cms.domain.*;
import org.fenixedu.bennu.cms.rendering.TemplateContext;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@ComponentType(type = "home", name = "Unit Homepage", description = "Provides the latest events and announcements")
public class HomeComponent extends HomeComponent_Base {

    private static final long NUM_POSTS = 5;

    @Override public void handle(Page page, HttpServletRequest req, TemplateContext componentContext,
            TemplateContext global){
        global.put("announcements", postsForCategory(page.getSite().categoryForSlug("announcement")));
        global.put("events", postsForCategory(page.getSite().categoryForSlug("events")));
    }

    private List<Post> postsForCategory(Category category) {
        return category != null ? new PostsPresentationBean(category.getPostsSet()).getVisiblePosts(NUM_POSTS) : Lists.newArrayList();
    }

}
