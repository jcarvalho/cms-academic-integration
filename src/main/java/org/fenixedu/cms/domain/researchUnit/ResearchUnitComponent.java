package org.fenixedu.cms.domain.researchUnit;

import net.sourceforge.fenixedu.domain.organizationalStructure.ResearchUnit;

import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.domain.unit.UnitSiteComponent;
import org.fenixedu.cms.rendering.TemplateContext;

@ComponentType(name = "Research Unit", description = "Provides the research unit associated with the site")
public class ResearchUnitComponent extends UnitSiteComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        ResearchUnit researchUnit = (ResearchUnit) unit(page);
        componentContext.put("researchUnit", researchUnit);
        globalContext.put("researchUnit", researchUnit);
    }

}
