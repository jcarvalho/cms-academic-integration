package org.fenixedu.cms.domain.executionCourse;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.messaging.Announcement;
import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.cms.routing.CMSBackend;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.portal.domain.MenuFunctionality;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.cms.domain.MigrationTask;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.spaces.domain.Space;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

public class CreateExecutionCourseSite extends MigrationTask {
    private static final Set<String> siteSlugs = Sets.newHashSet();
    private static final String BUNDLE = "resources.FenixEduCMSResources";
    private static final LocalizedString ANNOUNCEMENTS = getLocalizedString(BUNDLE, "label.announcement");
    private static final int TRANSACTION_SIZE = 100;

    @Override
    public void runTask() throws Exception {
        //delete existing sites
        Set<org.fenixedu.bennu.cms.domain.Site> allSites = Bennu.getInstance().getSitesSet();
        Iterable<List<org.fenixedu.bennu.cms.domain.Site>> sitesChunks = Iterables.partition(allSites, 100);
        getLogger().info("removing all sites..");
        for(List<org.fenixedu.bennu.cms.domain.Site> siteChunk : sitesChunks) {
            getLogger().info("removing sites " + siteChunk.size());
            FenixFramework.atomic(()->siteChunk.stream().forEach(s->s.delete()));
        }

        //create execution course sites
        Set<net.sourceforge.fenixedu.domain.Site> sites = Bennu.getInstance().getSiteSet();
        getLogger().info("existing sites " + sites.size());

        Iterable<net.sourceforge.fenixedu.domain.ExecutionCourseSite> oldSites = Iterables.filter(sites, net.sourceforge.fenixedu.domain.ExecutionCourseSite.class);
        getLogger().info("starting migration of " + Iterables.size(oldSites) + " execution course sites.");

        Iterable<List<net.sourceforge.fenixedu.domain.ExecutionCourseSite>> oldSitesChunks = Iterables.partition(oldSites, TRANSACTION_SIZE);
        getLogger().info("creating sites for " + +Iterables.size(oldSitesChunks) + " chunks.");

        for(List<net.sourceforge.fenixedu.domain.ExecutionCourseSite> chunk : oldSitesChunks) {
            FenixFramework.atomic(()->create(chunk));
        }
    }

    private void create(List<net.sourceforge.fenixedu.domain.ExecutionCourseSite> oldSites) {
        getLogger().info("creating for sites " + oldSites.size());
        oldSites.stream().filter(oldSite->oldSite.getSiteExecutionCourse()!=null).forEach(oldSite->create(oldSite));
    }

    @Override public Atomic.TxMode getTxMode() {
        return Atomic.TxMode.READ;
    }

    private net.sourceforge.fenixedu.domain.ExecutionCourseSite oldExecutionCourseSiteByExecutionCourse(String externalId) {
        ExecutionCourse e = FenixFramework.getDomainObject(externalId);
        return e.getSite();
    }

    @Atomic
    public void create(net.sourceforge.fenixedu.domain.ExecutionCourseSite oldSite) {
        getLogger().info("{ number: " + siteSlugs.size() + ", oldPath: " + oldSite.getReversePath() + " }");

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
                post.setCreatedBy(announcement.getCreator() != null ? announcement.getCreator().getUser() : Authenticate.getUser());
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
