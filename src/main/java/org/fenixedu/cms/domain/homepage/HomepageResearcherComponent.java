package org.fenixedu.cms.domain.homepage;

import net.sourceforge.fenixedu.domain.homepage.Homepage;

import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.component.ComponentParameter;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.domain.component.DynamicComponent;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.commons.i18n.I18N;

@ComponentType(name = "Researcher Section Data Component",
        description = "Provides homepage owner's researcher section page data.")
public class HomepageResearcherComponent extends HomepageResearcherComponent_Base {

    public HomepageResearcherComponent(String titleKey, String dataKey) {
        this(titleKey, "resources.ResearcherResources", dataKey);
    }

    @DynamicComponent
    public HomepageResearcherComponent(@ComponentParameter("Title Key") String titleKey,
            @ComponentParameter("Title Bundle") String titleBundle, @ComponentParameter("Data Key") String dataKey) {
        setTitleKey(titleKey);
        setDataKey(dataKey);
        setTitleBundle(titleBundle);
    }

    @Override
    public void handle(Page page, TemplateContext local, TemplateContext global) {
        Homepage homepage = homepage(page.getSite());
        if (homepage != null) {
            global.put("bundle", getTitleBundle());
            global.put("researcher", homepage.getPerson().getUsername());
            global.put("sotisUrl", "https://sotis.tecnico.ulisboa.pt"); //FIXME get real configuration property when available
            global.put("language", I18N.getLocale().toLanguageTag());
            global.put("dataKey", getDataKey());
            global.put("titleKey", getTitleKey());
        }
    }

    private Homepage homepage(Site site) {
        return (site instanceof HomepageSite) ? ((HomepageSite) site).getHomepage() : null;
    }

}
