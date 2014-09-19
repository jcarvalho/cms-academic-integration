package org.fenixedu.cms.domain.executionCourse;

import static org.fenixedu.cms.domain.MigrationUtil.INITIAL_PAGE_TEMPLATE;

import java.util.Collection;

import net.sourceforge.fenixedu.domain.ExecutionCourse;

import org.fenixedu.bennu.signals.DomainObjectEvent;
import org.fenixedu.cms.domain.MigrationUtil;
import org.fenixedu.cms.domain.MigrationUtil.PageTemplate;
import org.fenixedu.cms.domain.Page;

import com.google.common.eventbus.Subscribe;

public class ExecutionCourseListener {

    @Subscribe
    public void doIt(DomainObjectEvent<ExecutionCourse> event) {
        create(event.getInstance());
    }

    public static ExecutionCourseSite create(ExecutionCourse executionCourse) {
        ExecutionCourseSite newSite = new ExecutionCourseSite(executionCourse);

        newSite.setTheme(MigrationUtil.THEME);
        CreateExecutionCourseSite.getFolder().addSite(newSite);

        Collection<PageTemplate> pages = CreateExecutionCourseSite.getMigrationTemplates(newSite).values();
        pages.addAll(CreateExecutionCourseSite.getAdditionalTemplates(newSite));

        MigrationUtil.addPages(newSite, pages);

        Page initialPage =
                newSite.getPagesSet().stream().filter(p -> p.getTemplate().getType().equals(INITIAL_PAGE_TEMPLATE)).findAny()
                        .orElse(null);
        if (initialPage != null) {
            newSite.setInitialPage(initialPage);
        }

        return newSite;
    }

}
