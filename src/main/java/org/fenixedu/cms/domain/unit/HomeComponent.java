package org.fenixedu.cms.domain.unit;

import java.util.Collections;
import java.util.List;

import org.fenixedu.bennu.cms.domain.Category;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.CMSComponent;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.domain.component.PostsPresentationBean;
import org.fenixedu.bennu.cms.domain.wraps.Wrap;
import org.fenixedu.bennu.cms.rendering.TemplateContext;

@ComponentType(name = "Unit Homepage", description = "Provides the latest events and announcements")
public class HomeComponent implements CMSComponent {

    private static final long NUM_POSTS = 5;

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext global) {
        global.put("announcements", postsForCategory(page.getSite().categoryForSlug("announcement")));
        global.put("events", postsForCategory(page.getSite().categoryForSlug("events")));
    }

    private List<Wrap> postsForCategory(Category category) {
        return category != null ? new PostsPresentationBean(category.getPostsSet()).getVisiblePosts(NUM_POSTS) : Collections
                .emptyList();
    }

}
