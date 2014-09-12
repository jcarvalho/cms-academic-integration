package org.fenixedu.cms.domain.homepage;

import com.google.common.collect.Sets;
import net.sourceforge.fenixedu.domain.homepage.Homepage;
import org.fenixedu.bennu.cms.domain.Category;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.component.ComponentContextProvider;
import org.fenixedu.bennu.cms.domain.component.ComponentParameter;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.domain.component.DynamicComponent;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.commons.i18n.I18N;

@ComponentType(name = "Researcher Section Data Component",
        description = "Provides homepage owner's researcher section page data.")
public class HomepageResearcherComponent extends HomepageResearcherComponent_Base {

    String titleKey;
    String dataKey;

    @DynamicComponent
    public HomepageResearcherComponent(@ComponentParameter(value = "TitleKey", required = true)
    String titleKey, @ComponentParameter(value = "DataKey", required = true) String dataKey) {
        this.titleKey = titleKey;
        this.dataKey = dataKey;
    }

    @Override
    public void handle(Page page, TemplateContext local, TemplateContext global) {
        Homepage homepage = homepage(page.getSite());
        if (homepage != null) {
            global.put("researcher", homepage.getPerson().getUsername());
            global.put("sotisUrl", "https://sotis.tecnico.ulisboa.pt"); //FIXME get real configuration property when available
            global.put("language", I18N.getLocale().toLanguageTag());
            global.put("dataKey", dataKey);
            global.put("titleKey", titleKey);
        }
    }

    private Homepage homepage(Site site) {
        return (site instanceof HomepageSite) ? ((HomepageSite) site).getHomepage() : null;
    }

}
