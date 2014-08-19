package org.fenixedu.cms.domain.homepage;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.homepage.Homepage;

import org.fenixedu.bennu.cms.domain.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.commons.i18n.I18N;

@ComponentType(type = "researcherSection", name = "Researcher Section Data Component",
        description = "Provides homepage owner's researcher section page data.")
public class HomepageResearcherComponent extends HomepageResearcherComponent_Base {

    public HomepageResearcherComponent(String titleKey, String dataKey) {
        setTitleKey(titleKey);
        setDataKey(dataKey);
    }

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext local, TemplateContext global) {
        Homepage homepage = homepage(page.getSite());
        if (homepage != null) {
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
