package org.fenixedu.cms.domain.department.componenets;

import net.sourceforge.fenixedu.domain.Department;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.department.DepartmentSite;

@ComponentType(name = "departmentHomepage", description = "Department")
public class DepartmentHomepage extends DepartmentHomepage_Base {

    @Override public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        Department department = ((DepartmentSite) page.getSite()).getDepartment();
        globalContext.put("department", department);
    }
}
