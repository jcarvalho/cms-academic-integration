package org.fenixedu.cms.domain.executionCourse;

import java.util.Locale;

import net.sourceforge.fenixedu.domain.Summary;

import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.signals.DomainObjectEvent;
import org.fenixedu.commons.i18n.LocalizedString;

import com.google.common.eventbus.Subscribe;

public class SummaryListener {

    public static class Create {
        @Subscribe
        public void doIt(DomainObjectEvent<Summary> event) {
            updatePost(new Post(), event.getInstance());
        }
    }

    public static class Delete {
        @Subscribe
        public void doIt(DomainObjectEvent<Summary> event) {
            event.getInstance().getPost().delete();
        }
    }

    public static class Edit {
        @Subscribe
        public void doIt(DomainObjectEvent<Summary> event) {
            updatePost(event.getInstance().getPost(), event.getInstance());
        }
    }

    private static void updatePost(Post post, Summary summary) {
        LocalizedString professorshipName = makeLocalized(summary.getProfessorship().getPerson().getName());
        LocalizedString summaryShiftName = makeLocalized(summary.getShift().getPresentationName());
        ExecutionCourseSite site = summary.getExecutionCourse().getCmsSite();
        
        post.setSite(site);
        post.setSlug("summary-" + summary.getOid());
        post.setName(summary.getTitle().toLocalizedString());
        
        post.setCreatedBy(summary.getProfessorship().getPerson().getUser());
        post.setBody(summary.getSummaryText().toLocalizedString());
        post.setCreationDate(summary.getSummaryDateTime());
        
        post.addCategories(site.categoryForSlug("summary"));
        post.addCategories(site.categoryForSlug("summary-professor-" + summary.getProfessorship().getOid(), professorshipName));
        post.addCategories(site.categoryForSlug("summary-shift-" + summary.getShift().getOid(), summaryShiftName));
        
    }

    private static LocalizedString makeLocalized(String value) {
        LocalizedString.Builder builder = new LocalizedString.Builder();
        for (Locale locale : CoreConfiguration.supportedLocales()) {
            builder.with(locale, value);
        }
        return builder.build();
    }

}
