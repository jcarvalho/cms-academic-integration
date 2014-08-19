package org.fenixedu.cms.domain.homepage;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.fenixedu.domain.homepage.Homepage;

import org.fenixedu.bennu.cms.domain.CMSTemplate;
import org.fenixedu.bennu.cms.domain.CMSTheme;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.cms.domain.MigrationTask;
import org.fenixedu.cms.domain.MigrationUtils;
import org.fenixedu.cms.domain.executionCourse.CreateExecutionCourseSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;

public class CreateHomepageSite extends MigrationTask {
    Logger log = LoggerFactory.getLogger(CreateExecutionCourseSite.class);

    private static final String THEME = "fenixedu-default-theme";
    private static final String BUNDLE = "resources.FenixEduCMSResources";
    private static final String RESEARCHER_TEMPLATE_TYPE = "researcherSection";
    private static final String PRESENTATION_TEMPLATE_TYPE = "presentation";

    @Override
    public void runTask() throws Exception {
        MigrationUtils.deleteAllSites();
        /*David Matos's Homepage*/
        Homepage hp = FenixFramework.getDomainObject("910533118347");
        Site newSite;
        if (hp instanceof Homepage && hp.isHomepageActivated()) {
            newSite = new HomepageSite(hp);
        } else if (hp instanceof Homepage) {
            throw new Exception("Homepage is not activated");
        } else {
            throw new Exception("Not an Homepage domain object.");
        }

        newSite.setBennu(Bennu.getInstance());
        newSite.setTheme(CMSTheme.forType(THEME));
        newSite.setDescription(MigrationUtils.localized(hp.getDescription()));
        newSite.setAlternativeSite(hp.getAlternativeSite());
        newSite.setName(BundleUtil.getLocalizedString(BUNDLE, "homepage.title", hp.getOwnersName()));
        newSite.setSlug(hp.getPerson().getUsername());
        newSite.setCreatedBy(hp.getPerson().getUser());
        newSite.setStyle(hp.getStyle());
        newSite.setPublished(true);

        Map<String, Page> exceptionalPages = new HashMap<String, Page>();

        CMSTemplate researcherTemplate = newSite.getTheme().templateForType(RESEARCHER_TEMPLATE_TYPE);
        CMSTemplate presentationTemplate = newSite.getTheme().templateForType(PRESENTATION_TEMPLATE_TYPE);

        Page initialPage;

        exceptionalPages.put("/publico/viewHomepageResearch.do?method=showInterests", Page.createBasePage(researcherTemplate,
                new HomepageResearcherComponent("researcher.interests.title.complete", "interests")));
        exceptionalPages.put("/publico/viewHomepageResearch.do?method=showPrizes", Page.createBasePage(researcherTemplate,
                new HomepageResearcherComponent("researcher.PrizeAssociation.title.label", "prizes")));
        exceptionalPages.put("/publico/viewHomepageResearch.do?method=showParticipations", Page.createBasePage(
                researcherTemplate, new HomepageResearcherComponent("link.activitiesManagement", "activities")));
        exceptionalPages.put("/publico/viewHomepageResearch.do?method=showPatents",
                Page.createBasePage(researcherTemplate, new HomepageResearcherComponent("link.patentsManagement", "patents")));
        exceptionalPages.put("/publico/viewHomepageResearch.do?method=showPublications",
                Page.createBasePage(researcherTemplate, new HomepageResearcherComponent("link.Publications", "publications")));
        exceptionalPages.put("/publico/viewHomepage.do?method=show",
                initialPage = Page.createBasePage(presentationTemplate, new HomepagePresentationComponent()));

        migrateSite(newSite, hp, exceptionalPages, false);

        newSite.setInitialPage(initialPage);
    }
}