package org.fenixedu.cms.domain.unit;

import net.sourceforge.fenixedu.domain.thesis.Thesis;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.CMSComponent;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;
import org.fenixedu.bennu.core.security.Authenticate;
import pt.ist.fenixframework.FenixFramework;

import static java.util.stream.Collectors.groupingBy;

@ComponentType(name = "thesis", description = "Provides information for a specific thesis")
public class ThesisComponent implements CMSComponent {

    @Override public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        Thesis thesis = FenixFramework.getDomainObject((String) globalContext.getRequestContext()[1]);
        globalContext.put("thesis", thesis);
        globalContext.put("states", DepartmentDissertations.states);
        if(thesis!=null && thesis.getDissertation()!=null) {
            globalContext.put("isAccessible", thesis.getDissertation().isAccessible(Authenticate.getUser()));
        } else {
            globalContext.put("isAccessible", false);
        }
    }

}