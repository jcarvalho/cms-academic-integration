package org.fenixedu.cms.domain.scientificArea;

import net.sourceforge.fenixedu.domain.Employee;
import net.sourceforge.fenixedu.domain.organizationalStructure.Party;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;

import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.unit.UnitSiteComponent;

@ComponentType(description = "Shows the employees of the site's Scientific Area", name = "Scientific Area Employees")
public class ScientificAreaEmployees extends UnitSiteComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        Unit unit = unit(page);
        globalContext
                .put("employees",
                        unit.getAllCurrentNonTeacherEmployees().stream().map(Employee::getPerson)
                                .sorted(Party.COMPARATOR_BY_NAME_AND_ID));
    }

}
