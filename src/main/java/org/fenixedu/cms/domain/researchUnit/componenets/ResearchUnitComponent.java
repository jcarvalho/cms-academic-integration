package org.fenixedu.cms.domain.researchUnit.componenets;

import net.sourceforge.fenixedu.domain.organizationalStructure.ResearchUnit;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.researchUnit.ResearchUnitSite;

@ComponentType(name = "Research Unit", description = "Provides the research unit associated with the site")
public class ResearchUnitComponent extends ResearchUnitComponent_Base {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        ResearchUnit researchUnit = ((ResearchUnitSite) page.getSite()).getResearchUnit();
        componentContext.put("researchUnit", researchUnit);
        globalContext.put("researchUnit", researchUnit);
    }

}
