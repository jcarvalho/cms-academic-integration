package org.fenixedu.cms.domain.researchUnit;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;
import static org.fenixedu.cms.domain.MigrationUtil.BUNDLE;
import static org.fenixedu.cms.domain.MigrationUtil.CATEGORY_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.EVENTS_SLUG;
import static org.fenixedu.cms.domain.MigrationUtil.EVENTS_TITLE;
import static org.fenixedu.cms.domain.MigrationUtil.SUBUNITS_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.UNIT_HOMEPAGE_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.UNIT_MEMBERS_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.UNIT_ORGANIZATION_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.VIEW_POST_TITLE;
import static org.fenixedu.cms.domain.MigrationUtil.VIEW_TEMPLATE;
import static pt.ist.fenixframework.FenixFramework.atomic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.portal.domain.MenuFunctionality;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.cms.domain.CMSFolder;
import org.fenixedu.cms.domain.Category;
import org.fenixedu.cms.domain.MigrationUtil;
import org.fenixedu.cms.domain.MigrationUtil.PageTemplate;
import org.fenixedu.cms.domain.Site;
import org.fenixedu.cms.domain.component.Component;
import org.fenixedu.cms.domain.component.ListCategoryPosts;
import org.fenixedu.cms.domain.component.StrategyBasedComponent;
import org.fenixedu.cms.domain.component.ViewPost;
import org.fenixedu.cms.domain.unit.HomeComponent;
import org.fenixedu.cms.domain.unit.Organization;
import org.fenixedu.cms.domain.unit.SubUnits;
import org.fenixedu.cms.domain.unit.UnitSite;
import org.fenixedu.cms.routing.CMSBackend;
import org.fenixedu.commons.i18n.LocalizedString;

import pt.ist.fenixframework.Atomic;

import com.google.common.collect.Iterables;

public class CreateResearchUnitSites extends CustomTask {

    private static final String RESEARCH_UNIT_SITE_FOLDER = "research-unit";
    private static final LocalizedString RESEARCH_UNIT_FOLDER_DESCRIPTION = getLocalizedString(BUNDLE,
            "researchUnit.folder.description");

    private static final String HOMEPAGE_PATH = "/publico/researchSite/viewResearchUnitSite.do?method=presentation";
    private static final String PUBLICATIONS_PATH =
            "/publico/researchSite/viewResearchUnitSiteResearch.do?method=showPublications";
    private static final String MEMBERS_PATH = "/publico/researchSite/viewResearchUnitSite.do?method=showResearchers";
    private static final String ORGANIZATION_PATH = "/publico/researchSite/viewResearchUnitSite.do?method=organization";
    private static final String EVENTS_PATH = "/publico/researchSite/manageResearchUnitAnnouncements.do?method=viewEvents";
    private static final String SUBUNITS_PATH = "/publico/researchSite/viewResearchUnitSite.do?method=subunits";

    private static final LocalizedString MEMBERS_TITLE = getLocalizedString(BUNDLE, "label.researchers");
    private static final LocalizedString SUBUNITS_TITLE = getLocalizedString(BUNDLE, "researchUnit.subunits");
    private static final LocalizedString ORGANIZATION_TITLE = getLocalizedString(BUNDLE, "researchUnit.organization");
    private static final LocalizedString HOMEPAGE_TITLE = getLocalizedString(BUNDLE, "researchUnit.homepage");

    private static final int TRANSACTION_SIZE = 30;

    private static final Map<String, PageTemplate> migrationTemplates = new HashMap<String, PageTemplate>();
    private static final List<PageTemplate> additionalTemplates = new ArrayList<PageTemplate>();
    private static CMSFolder ruFolder = null;

    @Override
    public void runTask() throws Exception {
        // MigrationUtil.deleteSiteClass(ResearchUnitSite.class);
        MigrationUtil.deleteMatchingFolder(RESEARCH_UNIT_SITE_FOLDER);

        Iterable<List<net.sourceforge.fenixedu.domain.ResearchUnitSite>> oldSitesChunks =
                Iterables.partition(MigrationUtil.sitesForClass(net.sourceforge.fenixedu.domain.ResearchUnitSite.class),
                        TRANSACTION_SIZE);

//        for (List<net.sourceforge.fenixedu.domain.ResearchUnitSite> chunk : oldSitesChunks) {
//            atomic(() -> chunk.stream().forEach(oldSite -> migrateResearchUnitSite(oldSite)));
//        }

        //only one chunck for testing
        atomic(() -> oldSitesChunks.iterator().next().stream().forEach(oldSite -> migrateResearchUnitSite(oldSite)));

    }

    public static CMSFolder getFolder() {
        if (ruFolder == null) {
            ruFolder =
                    new CMSFolder(PortalConfiguration.getInstance().getMenu(), RESEARCH_UNIT_SITE_FOLDER,
                            RESEARCH_UNIT_FOLDER_DESCRIPTION);
        }
        return ruFolder;
    }

    private static Map<String, PageTemplate> getMigrationTemplates() {
        if (migrationTemplates.isEmpty()) {
            migrationTemplates.put(HOMEPAGE_PATH,
                    new PageTemplate(HOMEPAGE_TITLE, null, UNIT_HOMEPAGE_TEMPLATE, Component.forType(HomeComponent.class)));
            migrationTemplates.put(MEMBERS_PATH, new PageTemplate(MEMBERS_TITLE, null, UNIT_MEMBERS_TEMPLATE,
                    StrategyBasedComponent.forType(ResearchUnitComponent.class)));
            migrationTemplates.put(SUBUNITS_PATH, new PageTemplate(SUBUNITS_TITLE, null, SUBUNITS_TEMPLATE,
                    StrategyBasedComponent.forType(SubUnits.class)));
            migrationTemplates.put(ORGANIZATION_PATH, new PageTemplate(ORGANIZATION_TITLE, null, UNIT_ORGANIZATION_TEMPLATE,
                    StrategyBasedComponent.forType(Organization.class)));
            //TODO Publications
            //exceptionalPages.put(PUBLICATIONS_PATH,);
        }
        return migrationTemplates;
    }

    public static Map<String, PageTemplate> getMigrationTemplates(Site newSite) {
        Map<String, PageTemplate> siteIndependentTemplates = getMigrationTemplates();
        Map<String, PageTemplate> migrationTemplates = new HashMap<String, PageTemplate>(siteIndependentTemplates);

        Category eventsCategory = newSite.categoryForSlug(EVENTS_SLUG, EVENTS_TITLE);
        ListCategoryPosts eventsComponent = new ListCategoryPosts(eventsCategory);

        migrationTemplates.put(EVENTS_PATH, new PageTemplate(EVENTS_TITLE, null, CATEGORY_TEMPLATE, eventsComponent));

        return migrationTemplates;
    }

    private static List<PageTemplate> getAdditionalTemplates() {
        if (additionalTemplates.isEmpty()) {
            additionalTemplates.add(new PageTemplate(VIEW_POST_TITLE, null, VIEW_TEMPLATE, false, Component
                    .forType(ViewPost.class)));
        }
        return additionalTemplates;
    }

    public static List<PageTemplate> getAdditionalTemplates(Site newSite) {
        return getAdditionalTemplates();
    }

    private Site migrateResearchUnitSite(net.sourceforge.fenixedu.domain.ResearchUnitSite oldSite) {

        UnitSite newSite = new UnitSite(oldSite.getUnit());

        newSite.setDescription(MigrationUtil.localized(oldSite.getDescription()));
        newSite.setName(oldSite.getUnit().getNameI18n().toLocalizedString());
        newSite.setSlug(MigrationUtil.createSlug(oldSite));
        newSite.setBennu(Bennu.getInstance());
        newSite.setTheme(MigrationUtil.THEME);
        newSite.setFunctionality(new MenuFunctionality(PortalConfiguration.getInstance().getMenu(), false, newSite.getSlug(),
                CMSBackend.BACKEND_KEY, "anyone", newSite.getDescription(), newSite.getName(), newSite.getSlug()));

        getFolder().addSite(newSite);

        MigrationUtil.migrateSite(newSite, oldSite, getMigrationTemplates(newSite));
        MigrationUtil.addPages(newSite, getAdditionalTemplates(newSite));

        MigrationUtil.migrateAnnouncements(newSite, oldSite.getUnit().getBoardsSet().iterator());

        newSite.setPublished(true);

        return newSite;
    }

    @Override
    public Atomic.TxMode getTxMode() {
        return Atomic.TxMode.READ;
    }

}
