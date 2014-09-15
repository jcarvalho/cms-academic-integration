package org.fenixedu.cms.domain.unit;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;

import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;

import static java.util.stream.Collectors.toList;

@ComponentType(name = "Subunits", description = "Subunits of a research unit that have a site")
public class SubUnits extends UnitSiteComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        globalContext.put("subunits", subUnitsWithSite(unit(page)));
    }

    private List<Unit> subUnitsWithSite(Unit unit) {
        return unit.getSubUnits().stream().filter(u -> u.getSite() != null).sorted(Unit.COMPARATOR_BY_NAME_AND_ID).collect(toList());
    }
}
