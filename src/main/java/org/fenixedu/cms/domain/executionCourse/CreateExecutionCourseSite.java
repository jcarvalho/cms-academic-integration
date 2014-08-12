package org.fenixedu.cms.domain.executionCourse;

import static java.util.stream.Collectors.toSet;
import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Summary;
import net.sourceforge.fenixedu.domain.messaging.Announcement;

import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.cms.routing.CMSBackend;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.portal.domain.MenuFunctionality;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.bennu.signals.DomainObjectEvent;
import org.fenixedu.bennu.signals.Signal;
import org.fenixedu.cms.domain.MigrationTask;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.spaces.domain.Space;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class CreateExecutionCourseSite extends MigrationTask {
    private static final Set<String> siteSlugs = Sets.newHashSet();
    private static final String BUNDLE = "resources.FenixEduCMSResources";
    private static final String PREFIX = "execution-course-";
    private static final Logger log = LoggerFactory.getLogger(CreateExecutionCourseSite.class);

    private static Integer numSites = 1;
    private static final LocalizedString ANNOUNCEMENTS = getLocalizedString(BUNDLE, "label.announcement");

    @Override
    public void runTask() throws Exception {
        DateTime start = new DateTime();
        deleteAllSites();
        Set<ExecutionCourse> executionCourses = Bennu.getInstance().getExecutionCoursesSet();
        Set<net.sourceforge.fenixedu.domain.ExecutionCourseSite> oldSites =
                executionCourses.stream().map(e -> e.getSite()).filter(s -> s != null).collect(toSet());

        Iterable<List<net.sourceforge.fenixedu.domain.ExecutionCourseSite>> partitions = Iterables.partition(oldSites, 5000);

        partitions.iterator().next().stream().forEach(s -> createExecutionCourseSite(s));
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
        log.info("{ number: " + siteSlugs.size() + ", oldPath: " + oldSite.getReversePath() + " }");

        ExecutionCourse executionCourse = oldSite.getExecutionCourse();

        ExecutionCourseSite newSite = new ExecutionCourseSite();
        newSite.setExecutionCourse(executionCourse);
        newSite.setPublished(true);
        newSite.setDescription(localized(oldSite.getDescription()));
        newSite.setName(executionCourse.getNameI18N().toLocalizedString());
        newSite.setSlug(createSlug(oldSite.getReversePath()));
        newSite.setBennu(Bennu.getInstance());
        newSite.setAlternativeSite(oldSite.getAlternativeSite());
        newSite.setStyle(oldSite.getStyle());
        newSite.setTheme(CMSTheme.forType("fenixedu-default-theme"));
        newSite.setFunctionality(new MenuFunctionality(PortalConfiguration.getInstance().getMenu(), false, newSite.getSlug(),
                CMSBackend.BACKEND_KEY, "anyone", newSite.getDescription(), newSite.getName(), newSite.getSlug()));

        createStaticPages(newSite, null, oldSite);

        migrateSummaries(newSite);
        migrateAnnouncements(newSite);

        ExecutionCourseListener.createDynamicPages(newSite, sideMenu);
        createMenuComponents(newSite);
    }

    private String createSlug(String oldPath) {
        String newSlug = oldPath.substring(1).replace("/", "-");
        while (siteSlugs.contains(newSlug)) {
            String randomSlug = UUID.randomUUID().toString().substring(0, 3);
            newSlug = Joiner.on("-").join(newSlug, randomSlug);
        }
        siteSlugs.add(newSlug);
        return newSlug;
    }

    private void migrateAnnouncements(ExecutionCourseSite site) {
        for (Announcement announcement : site.getExecutionCourse().getBoard().getAnnouncementSet()) {
            boolean hasSubject = announcement.getSubject() != null && !announcement.getSubject().isEmpty();
            boolean hasBody = announcement.getBody() != null && !announcement.getBody().isEmpty();
            if (hasSubject && hasBody) {
                Post post = new Post();
                post.setSite(site);

                post.setCreatedBy(announcement.getCreator() != null ? announcement.getCreator().getUser() : null);
                post.setCreationDate(announcement.getCreationDate());
                post.setBody(localized(announcement.getBody()));
                post.setName(localized(announcement.getSubject()));
                post.setActive(announcement.getVisible());
                post.setLocation(localizedStr(announcement.getPlace()));
                post.setPublicationBegin(announcement.getPublicationBegin());
                post.setPublicationEnd(announcement.getPublicationEnd());

                post.addCategories(site.categoryForSlug("announcement", ANNOUNCEMENTS));

                announcement.getCategoriesSet().stream().map(ac -> localized(ac.getName()))
                        .map(name -> site.categoryForSlug(name.getContent(), name))
                        .forEach(category -> post.addCategories(category));

                Space campus = announcement.getCampus();
                if (campus != null) {
                    String campusName = Optional.ofNullable(campus.getPresentationName()).orElse(campus.getName());
                    if (!Strings.isNullOrEmpty(campusName)) {
                        post.addCategories(site.categoryForSlug("campus-" + campus.getExternalId(), localizedStr(campusName)));
                    }
                }
            }
        }
    }

    private void migrateSummaries(ExecutionCourseSite site) {
        site.getExecutionCourse().getAssociatedSummariesSet().forEach(summary -> {
            Signal.emit(Summary.CREATED_SIGNAL, new DomainObjectEvent<Summary>(summary));
        });
    }

}
