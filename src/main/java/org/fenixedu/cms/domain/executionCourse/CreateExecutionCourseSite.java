package org.fenixedu.cms.domain.executionCourse;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Site;
import net.sourceforge.fenixedu.domain.messaging.Announcement;
import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.cms.routing.CMSBackend;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.portal.domain.MenuFunctionality;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.cms.domain.MigrationTask;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.spaces.domain.Space;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

public class CreateExecutionCourseSite extends MigrationTask {
    private static final Set<String> siteSlugs = Sets.newHashSet();
    private static final String BUNDLE = "resources.FenixEduCMSResources";
    private static final Logger log = LoggerFactory.getLogger(CreateExecutionCourseSite.class);

    private static final LocalizedString ANNOUNCEMENTS = getLocalizedString(BUNDLE, "label.announcement");

    @Override
    public void runTask() throws Exception {
        DateTime start = new DateTime();
        deleteAllSites();
        Set<Site> sites = Bennu.getInstance().getSiteSet();
        Set<net.sourceforge.fenixedu.domain.ExecutionCourseSite> oldSites = Sets.newHashSet(Iterables.filter(sites, net.sourceforge.fenixedu.domain.ExecutionCourseSite.class));

        oldSites.stream().forEach(s -> createExecutionCourseSite(s));
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

    @Atomic
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

        //migrateSummaries(newSite);
        //migrateAnnouncements(newSite);

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
                post.setReferedSubjectBegin(announcement.getReferedSubjectBegin());
                post.setReferedSubjectEnd(announcement.getReferedSubjectEnd());

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
        site.getExecutionCourse().getAssociatedSummariesSet().forEach(summary -> SummaryListener.updatePost(new Post(), summary));
    }

}
