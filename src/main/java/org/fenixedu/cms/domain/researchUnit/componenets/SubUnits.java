package org.fenixedu.cms.domain.researchUnit.componenets;

import net.sourceforge.fenixedu.domain.organizationalStructure.ResearchUnit;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.researchUnit.ResearchUnitSite;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@ComponentType(name = "Subunits", description = "Subunits of a research unit that have a site")
public class SubUnits extends SubUnits_Base {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        ResearchUnit unit = ((ResearchUnitSite) page.getSite()).getResearchUnit();
        globalContext.put("subunits", subUnitsWithSite(unit));
    }

    private List<Unit> subUnitsWithSite(Unit unit) {
        Predicate<Unit> hasSite = subUnit -> subUnit.getSite() != null;
        return unit.getSubUnits().stream().filter(hasSite).sorted(Unit.COMPARATOR_BY_NAME_AND_ID).collect(toList());
    }
}
