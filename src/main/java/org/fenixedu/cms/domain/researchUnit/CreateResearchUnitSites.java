package org.fenixedu.cms.domain.researchUnit;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.sourceforge.fenixedu.domain.messaging.Announcement;
import net.sourceforge.fenixedu.domain.messaging.AnnouncementBoard;
import net.sourceforge.fenixedu.domain.organizationalStructure.ResearchUnit;
import org.fenixedu.bennu.cms.domain.*;
import org.fenixedu.bennu.cms.routing.CMSBackend;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.portal.domain.MenuFunctionality;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.cms.domain.MigrationTask;
import org.fenixedu.cms.domain.researchUnit.componenets.HomeComponent;
import org.fenixedu.cms.domain.researchUnit.componenets.Organization;
import org.fenixedu.cms.domain.researchUnit.componenets.ResearchUnitComponent;
import org.fenixedu.cms.domain.researchUnit.componenets.SubUnits;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.spaces.domain.Space;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.Atomic;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

public class CreateResearchUnitSites extends MigrationTask {
    private static final Set<String> siteSlugs = Sets.newHashSet();

    public static final MultiLanguageString ANNOUNCEMENTS_NAME = new MultiLanguageString().with(MultiLanguageString.pt,
            "Anúncios");
    public static final MultiLanguageString EVENTS_NAME = new MultiLanguageString().with(MultiLanguageString.pt, "Eventos");

    private static final String THEME = "fenixedu-default-theme";
    private static final String BUNDLE = "resources.FenixEduCMSResources";
    private static final LocalizedString ANNOUNCEMENTS = getLocalizedString(BUNDLE, "label.announcement");
    private static final LocalizedString EVENTS = getLocalizedString(BUNDLE, "label.event");

    Logger log = LoggerFactory.getLogger(CreateResearchUnitSites.class);

    @Override
    public void runTask() throws Exception {
        List<net.sourceforge.fenixedu.domain.ResearchUnitSite> researchUnitSites =
                Lists.newArrayList(Iterables.filter(Bennu.getInstance().getSiteSet(),
                        net.sourceforge.fenixedu.domain.ResearchUnitSite.class));

        deleteAllSites();

        log.info(" [ creating research unit sites (existing " + researchUnitSites.size() + ") ]");

        for (net.sourceforge.fenixedu.domain.ResearchUnitSite oldSite : researchUnitSites) {
            //if (oldSite.getName().toLocalizedString().getContent().equals("INESC-ID/ESW")) {
                log.info("[ old site: " + oldSite.getExternalId() + ", path: " + oldSite.getReversePath() + " ]");
                create(oldSite);
            //}
        }

    }

    @Atomic
    private Site create(net.sourceforge.fenixedu.domain.ResearchUnitSite oldSite) {
        log.info("migrating old site '" + oldSite.getReversePath() + "'");
        ResearchUnitSite newSite = new ResearchUnitSite(oldSite.getUnit());

        newSite.setPublished(true);
        newSite.setDescription(localized(oldSite.getDescription()));
        newSite.setName(oldSite.getUnit().getNameI18n().toLocalizedString());
        newSite.setSlug(createSlug(oldSite.getReversePath()));
        newSite.setBennu(Bennu.getInstance());
        newSite.setTheme(CMSTheme.forType(THEME));
        newSite.setFunctionality(new MenuFunctionality(PortalConfiguration.getInstance().getMenu(), false, newSite.getSlug(),
                CMSBackend.BACKEND_KEY, "anyone", newSite.getDescription(), newSite.getName(), newSite.getSlug()));
        Page.create(newSite, null, null, getLocalizedString(BUNDLE, "label.viewPost"), true, "view", new ViewPost());

        createStaticPages(newSite, null, oldSite);
        createDynamicPages(newSite, sideMenu);
        createMenuComponents(newSite);
        migrateAnnouncements(newSite);
        log.info("[ New Site: " + newSite.getName().getContent() + " at " + newSite.getInitialPage().getAddress());
        return newSite;
    }

    private void migrateAnnouncements(ResearchUnitSite researchUnitSite) {
        ResearchUnit researchUnit = researchUnitSite.getResearchUnit();
        for (AnnouncementBoard board : researchUnit.getBoardsSet()) {

            boolean isAnnouncementsBoard = board.isPublicToRead() && board.getName().equalInAnyLanguage(ANNOUNCEMENTS_NAME);
            boolean isEventsBoard = board.isPublicToRead() && board.getName().equalInAnyLanguage(EVENTS_NAME);

            for (Announcement announcement : board.getAnnouncementSet()) {
                boolean hasSubject = announcement.getSubject() != null && !announcement.getSubject().isEmpty();
                boolean hasBody = announcement.getBody() != null && !announcement.getBody().isEmpty();
                if (hasSubject && hasBody) {
                    Post post = new Post();
                    post.setSite(researchUnitSite);

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

                    if (isAnnouncementsBoard) {
                        post.addCategories(researchUnitSite.categoryForSlug("announcement", ANNOUNCEMENTS));
                    }
                    if (isEventsBoard) {
                        post.addCategories(researchUnitSite.categoryForSlug("event", EVENTS));
                    }

                    announcement.getCategoriesSet().stream().map(ac -> localized(ac.getName()))
                            .map(name -> researchUnitSite.categoryForSlug(name.getContent(), name))
                            .forEach(category -> post.addCategories((Category) category));

                    Space campus = announcement.getCampus();
                    if (campus != null) {
                        String campusName = Optional.ofNullable(campus.getPresentationName()).orElse(campus.getName());
                        if (!Strings.isNullOrEmpty(campusName)) {
                            post.addCategories(researchUnitSite
                                    .categoryForSlug("campus-" + campus.getExternalId(), localizedStr(campusName)));
                        }
                    }
                }
            }
        }
    }

    private void createDynamicPages(Site site, Menu menu) {
        log.info("creating dynamic pages for site " + site.getSlug());
        Page.create(site, null, null,  getLocalizedString(BUNDLE, "label.viewPost"), true, "view", new ViewPost());

        Page.create(site, menu, null, getLocalizedString(BUNDLE, "label.researchers"), true, "members",
                new ResearchUnitComponent());
        Page.create(site, menu, null, getLocalizedString(BUNDLE, "reseachUnit.subunits"), true, "subunits", new SubUnits());

        Page.create(site, menu, null, getLocalizedString(BUNDLE, "reseachUnit.organization"), true, "unitOrganization",
                new Organization());

        Page homepage = Page.create(site, menu, null, getLocalizedString(BUNDLE, "researchUnit.homepage"), true, "unitHomepage",
                new HomeComponent());

        site.setInitialPage(homepage);

        Component eventsCategory = new ListCategoryPosts(site.categoryForSlug("event", EVENTS));
        Page.create(site, menu, null, getLocalizedString(BUNDLE, "researchUnit.events"), true, "category", eventsCategory);

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
}
