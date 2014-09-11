package org.fenixedu.cms.domain.homepage;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;
import static org.fenixedu.cms.domain.MigrationUtil.BUNDLE;
import static org.fenixedu.cms.domain.MigrationUtil.PRESENTATION_TEMPLATE;
import static org.fenixedu.cms.domain.MigrationUtil.RESEARCHER_SECTION_TEMPLATE;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.fenixedu.domain.homepage.Homepage;

import org.fenixedu.bennu.cms.domain.CMSFolder;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.component.StrategyBasedComponent;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.cms.domain.MigrationUtil;
import org.fenixedu.cms.domain.MigrationUtil.PageTemplate;
import org.fenixedu.cms.domain.executionCourse.CreateExecutionCourseSite;
import org.fenixedu.commons.i18n.LocalizedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;

public class CreateHomepageSite extends CustomTask {
    private static final Logger log = LoggerFactory.getLogger(CreateExecutionCourseSite.class);

    private static final String HOMEPAGE_FOLDER = "homepages"; //TODO switch to "homepage" once old homepages are deleted
    private static final LocalizedString HOMEPAGE_FOLDER_DESCRIPTION = getLocalizedString(BUNDLE, "homepage.folder.description");

    private static final String INTERESTS_PATH = "/publico/viewHomepageResearch.do?method=showInterests";
    private static final String PATENTS_PATH = "/publico/viewHomepageResearch.do?method=showPatents";
    private static final String PUBLICATIONS_PATH = "/publico/viewHomepageResearch.do?method=showPublications";
    private static final String ACTIVITIES_PATH = "/publico/viewHomepageResearch.do?method=showParticipations";
    private static final String PRIZES_PATH = "/publico/viewHomepageResearch.do?method=showPrizes";
    private static final String PRESENTATION_PATH = "/publico/viewHomepage.do?method=show";

    private static final String PRESENTATION = "presentation";

    private static final String INTERESTS = "interests";
    private static final String PATENTS = "patents";
    private static final String PUBLICATIONS = "publications";
    private static final String ACTIVITIES = "activities";
    private static final String PRIZES = "prizes";

    private static final String RESEARCHER_SECTION = "homepage.researcher.";
    private static final String INTERESTS_KEY = RESEARCHER_SECTION + INTERESTS;
    private static final String PATENTS_KEY = RESEARCHER_SECTION + PATENTS;
    private static final String PUBLICATIONS_KEY = RESEARCHER_SECTION + PUBLICATIONS;
    private static final String ACTIVITIES_KEY = RESEARCHER_SECTION + ACTIVITIES;
    private static final String PRIZES_KEY = RESEARCHER_SECTION + PRIZES;

    private static final LocalizedString INTERESTS_TITLE = getLocalizedString(BUNDLE, INTERESTS_KEY);
    private static final LocalizedString PATENTS_TITLE = getLocalizedString(BUNDLE, PATENTS_KEY);
    private static final LocalizedString PUBLICATIONS_TITLE = getLocalizedString(BUNDLE, PUBLICATIONS_KEY);
    private static final LocalizedString ACTIVITIES_TITLE = getLocalizedString(BUNDLE, ACTIVITIES_KEY);
    private static final LocalizedString PRIZES_TITLE = getLocalizedString(BUNDLE, PRIZES_KEY);
    private static final LocalizedString PRESENTATION_TITLE = getLocalizedString(BUNDLE, "homepage.presentation.title");

    private static final int TRANSACTION_SIZE = 30;

    private static final Map<String, PageTemplate> migrationTemplates = new HashMap<String, PageTemplate>();
    private static CMSFolder hpFolder = null;

    @Override
    public void runTask() throws Exception {
        MigrationUtil.deleteSiteClass(HomepageSite.class);
        MigrationUtil.deleteMatchingFolder(HOMEPAGE_FOLDER);

//        Iterable<List<Homepage>> hpChunks = Iterables.partition(MigrationUtil.sitesForClass(Homepage.class), TRANSACTION_SIZE);

//        for (List<Homepage> chunk : hpChunks) {
//            atomic(() -> chunk.stream().forEach(hp -> migrateHomepage(hp)));
//        }

//        atomic(() -> hpChunks.iterator().next().stream().forEach(hp -> migrateHomepage(hp)));

        /*David Matos's Homepage*/
        Homepage hp = FenixFramework.getDomainObject("910533118347");
        migrateHomepage(hp);
    }

    public static CMSFolder getFolder() {
        if (hpFolder == null) {
            hpFolder = new CMSFolder(PortalConfiguration.getInstance().getMenu(), HOMEPAGE_FOLDER, HOMEPAGE_FOLDER_DESCRIPTION);
        }
        return hpFolder;
    }

    public static Map<String, PageTemplate> getMigrationTemplates() {
        if (migrationTemplates.isEmpty()) {
            migrationTemplates.put(INTERESTS_PATH, new PageTemplate(INTERESTS_TITLE, null, RESEARCHER_SECTION_TEMPLATE,
                    new HomepageResearcherComponent(INTERESTS_KEY, BUNDLE, INTERESTS)));
            migrationTemplates.put(PRIZES_PATH, new PageTemplate(PRIZES_TITLE, null, RESEARCHER_SECTION_TEMPLATE,
                    new HomepageResearcherComponent(PRIZES_KEY, BUNDLE, PRIZES)));
            migrationTemplates.put(ACTIVITIES_PATH, new PageTemplate(ACTIVITIES_TITLE, null, RESEARCHER_SECTION_TEMPLATE,
                    new HomepageResearcherComponent(ACTIVITIES_KEY, BUNDLE, ACTIVITIES)));
            migrationTemplates.put(PATENTS_PATH, new PageTemplate(PATENTS_TITLE, null, RESEARCHER_SECTION_TEMPLATE,
                    new HomepageResearcherComponent(PATENTS_KEY, BUNDLE, PATENTS)));
            migrationTemplates.put(PUBLICATIONS_PATH, new PageTemplate(PUBLICATIONS_TITLE, null, RESEARCHER_SECTION_TEMPLATE,
                    new HomepageResearcherComponent(PUBLICATIONS_KEY, BUNDLE, PUBLICATIONS)));
            migrationTemplates.put(PRESENTATION_PATH, new PageTemplate(PRESENTATION_TITLE, PRESENTATION, PRESENTATION_TEMPLATE,
                    StrategyBasedComponent.forType(HomepagePresentationComponent.class)));
        }
        return migrationTemplates;
    }

    private Site migrateHomepage(Homepage hp) {
        if (hp.isHomepageActivated()) {
            log.info("Migrating " + hp.getOwnersName() + "'s homepage");

            Site newSite = new HomepageSite(hp);
            newSite.setTheme(MigrationUtil.THEME);

            getFolder().addSite(newSite);

            MigrationUtil.migrateSite(newSite, hp, getMigrationTemplates());

            newSite.setPublished(true);

            return newSite;

        } else {
            log.warn(hp.getOwnersName() + "'s homepage is not activated, skipping migration");
            return null;
        }
    }
}