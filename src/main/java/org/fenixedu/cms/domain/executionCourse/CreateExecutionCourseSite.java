package org.fenixedu.cms.domain.executionCourse;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Summary;
import net.sourceforge.fenixedu.domain.messaging.Announcement;

import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.bennu.signals.DomainObjectEvent;
import org.fenixedu.bennu.signals.Signal;
import org.fenixedu.cms.domain.MigrationUtil;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.spaces.domain.Space;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;

import com.google.common.base.Strings;

public class CreateExecutionCourseSite extends CustomTask {
    private static final Logger log = LoggerFactory.getLogger(CreateExecutionCourseSite.class);

    private static Integer numSites = 1;
    private static final LocalizedString ANNOUNCEMENTS = getLocalizedString("resources.FenixEduCMSResources",
            "label.announcement");

    @Override
    public void runTask() throws Exception {
        DateTime start = new DateTime();
        MigrationUtil.deleteAllSites();
        Set<ExecutionCourse> executionCourses = Bennu.getInstance().getExecutionCoursesSet();
        executionCourses.stream().map(e -> e.getSite()).filter(Objects::nonNull).forEach(site -> {
            log.info("{ number: " + numSites++ + ", oldPath: " + site.getReversePath() + " }");
            createExecutionCourseSite(site);
        });

        DateTime end = new DateTime();

        log.info("[ duration: " + Hours.hoursBetween(start, end) + "hours, " + Minutes.minutesBetween(start, end) + "minutes, "
                + Seconds.secondsBetween(start, end) + " ]");
        /*
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612946319"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612917134"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612898443"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612875684"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612846760"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612818202"));
        createExecutionCourseSite(oldExecutionCourseSiteByExecutionCourse("1610612802249"));
         */

    }

    private net.sourceforge.fenixedu.domain.ExecutionCourseSite oldExecutionCourseSiteByExecutionCourse(String externalId) {
        ExecutionCourse e = FenixFramework.getDomainObject(externalId);
        return e.getSite();
    }

    public void createExecutionCourseSite(net.sourceforge.fenixedu.domain.ExecutionCourseSite oldSite) {

        ExecutionCourse executionCourse = oldSite.getExecutionCourse();
        ExecutionCourseSite newSite = ExecutionCourseListener.create(executionCourse);

        newSite.setDescription(MigrationUtil.localized(oldSite.getDescription()));
        newSite.setAlternativeSite(oldSite.getAlternativeSite());
        newSite.setStyle(oldSite.getStyle());

        migrateSummaries(newSite);
        migrateAnnouncements(newSite);

        MigrationUtil.createStaticPages(newSite, null, oldSite);

    }

    private void migrateAnnouncements(ExecutionCourseSite site) {
        log.info("migrating announcements for site " + site.getSlug());
        for (Announcement announcement : site.getExecutionCourse().getBoard().getAnnouncementSet()) {
            boolean hasSubject = announcement.getSubject() != null && !announcement.getSubject().isEmpty();
            boolean hasBody = announcement.getBody() != null && !announcement.getBody().isEmpty();
            if (hasSubject && hasBody) {
                Post post = new Post();
                post.setSite(site);

                post.setCreatedBy(announcement.getCreator() != null ? announcement.getCreator().getUser() : null);
                post.setCreationDate(announcement.getCreationDate());
                post.setBody(MigrationUtil.localized(announcement.getBody()));
                post.setName(MigrationUtil.localized(announcement.getSubject()));
                post.setActive(announcement.getVisible());
                post.setLocation(MigrationUtil.localizedStr(announcement.getPlace()));
                post.setPublicationBegin(announcement.getPublicationBegin());
                post.setPublicationEnd(announcement.getPublicationEnd());

                post.addCategories(site.categoryForSlug("announcement", ANNOUNCEMENTS));

                announcement.getCategoriesSet().stream().map(ac -> MigrationUtil.localized(ac.getName()))
                        .map(name -> site.categoryForSlug(name.getContent(), name))
                        .forEach(category -> post.addCategories(category));

                Space campus = announcement.getCampus();
                if (campus != null) {
                    String campusName = Optional.ofNullable(campus.getPresentationName()).orElse(campus.getName());
                    if (!Strings.isNullOrEmpty(campusName)) {
                        post.addCategories(site.categoryForSlug("campus-" + campus.getExternalId(),
                                MigrationUtil.localizedStr(campusName)));
                    }
                }
            }
        }
    }

    private void migrateSummaries(ExecutionCourseSite site) {
        log.info("migrating summaries for site " + site.getSlug());
        site.getExecutionCourse().getAssociatedSummariesSet().forEach(summary -> {
            Signal.emit(Summary.CREATED_SIGNAL, new DomainObjectEvent<Summary>(summary));
        });
    }

}