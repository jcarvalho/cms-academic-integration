package org.fenixedu.cms.domain.homepage;

import java.util.HashMap;

import net.sourceforge.fenixedu.domain.homepage.Homepage;

import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.scheduler.custom.CustomTask;
import org.fenixedu.cms.domain.MigrationUtil;
import org.fenixedu.cms.domain.MigrationUtil.PageTemplate;
import org.fenixedu.cms.domain.executionCourse.CreateExecutionCourseSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;

public class CreateHomepageSite extends CustomTask {
    private static final Logger log = LoggerFactory.getLogger(CreateExecutionCourseSite.class);

    private static final String BUNDLE = "resources.FenixEduCMSResources";

    private static final String RESEARCHER_SECTION_TEMPLATE = "researcherSection";
    private static final String PRESENTATION_TEMPLATE = "presentation";

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

    HashMap<String, PageTemplate> exceptionalPages;

    public CreateHomepageSite() {
        exceptionalPages = new HashMap<String, PageTemplate>();
        PageTemplate INTERESTS_PAGE =
                new PageTemplate(BundleUtil.getLocalizedString(BUNDLE, INTERESTS_KEY), null, RESEARCHER_SECTION_TEMPLATE,
                        new HomepageResearcherComponent(INTERESTS_KEY, BUNDLE, INTERESTS));
        PageTemplate PRIZES_PAGE =
                new PageTemplate(BundleUtil.getLocalizedString(BUNDLE, PRIZES_KEY), null, RESEARCHER_SECTION_TEMPLATE,
                        new HomepageResearcherComponent(PRIZES_KEY, BUNDLE, PRIZES));
        PageTemplate PATENTS_PAGE =
                new PageTemplate(BundleUtil.getLocalizedString(BUNDLE, PATENTS_KEY), null, RESEARCHER_SECTION_TEMPLATE,
                        new HomepageResearcherComponent(PATENTS_KEY, BUNDLE, PATENTS));
        PageTemplate PUBLICATIONS_PAGE =
                new PageTemplate(BundleUtil.getLocalizedString(BUNDLE, PUBLICATIONS_KEY), null, RESEARCHER_SECTION_TEMPLATE,
                        new HomepageResearcherComponent(PUBLICATIONS_KEY, BUNDLE, PUBLICATIONS));
        PageTemplate ACTIVITIES_PAGE =
                new PageTemplate(BundleUtil.getLocalizedString(BUNDLE, ACTIVITIES_KEY), null, RESEARCHER_SECTION_TEMPLATE,
                        new HomepageResearcherComponent(ACTIVITIES_KEY, BUNDLE, ACTIVITIES));
        PageTemplate PRESENTATION_PAGE =
                new PageTemplate(null, PRESENTATION, PRESENTATION_TEMPLATE, new HomepagePresentationComponent());

        exceptionalPages.put(INTERESTS_PATH, INTERESTS_PAGE);
        exceptionalPages.put(PRIZES_PATH, PRIZES_PAGE);
        exceptionalPages.put(ACTIVITIES_PATH, ACTIVITIES_PAGE);
        exceptionalPages.put(PATENTS_PATH, PATENTS_PAGE);
        exceptionalPages.put(PUBLICATIONS_PATH, PUBLICATIONS_PAGE);
        exceptionalPages.put(PRESENTATION_PATH, PRESENTATION_PAGE);
    }

    @Override
    public void runTask() throws Exception {
        MigrationUtil.deleteAllSites();

        /*David Matos's Homepage*/
        Homepage hp = FenixFramework.getDomainObject("910533118347");
        migrateHomepage(hp);
    }

    private void migrateHomepage(Homepage hp) {
        if (hp.isHomepageActivated()) {
            log.info("Migrating " + hp.getOwnersName() + "'s homepage");

            Site newSite = new HomepageSite(hp);
            newSite.setBennu(Bennu.getInstance());
            newSite.setTheme(CMSTheme.forType("fenixedu-default-theme"));
            newSite.setDescription(MigrationUtil.localized(hp.getDescription()));
            newSite.setAlternativeSite(hp.getAlternativeSite());
            newSite.setName(BundleUtil.getLocalizedString(BUNDLE, "homepage.title", hp.getOwnersName()));
            newSite.setSlug(hp.getPerson().getUsername());
            newSite.setCreatedBy(hp.getPerson().getUser());
            newSite.setStyle(hp.getStyle());
            newSite.setPublished(true);

            MigrationUtil.migrateSite(newSite, hp, exceptionalPages);

            newSite.setInitialPage(newSite.getPagesSet().stream().filter(page -> {
                return page.getSlug().equals(PRESENTATION);
            }).findAny().get());
        } else {
            log.warn(hp.getOwnersName() + "'s homepage is not activated, skipping migration");
        }

    }
}