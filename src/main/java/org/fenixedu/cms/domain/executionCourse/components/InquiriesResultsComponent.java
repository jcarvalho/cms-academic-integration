package org.fenixedu.cms.domain.executionCourse.components;

import java.util.Optional;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import net.sourceforge.fenixedu.presentationTier.Action.publico.InquirieResultsBean;

import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.CMSComponent;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseSite;
import org.fenixedu.cms.rendering.TemplateContext;

@ComponentType(name = "InquiriesResults", description = "Inquirires Results of an Execution Course")
public class InquiriesResultsComponent implements CMSComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        InquirieResultsBean bean = new InquirieResultsBean(executionCourse);
        globalContext.put("hasAccess", Authenticate.isLogged() && Authenticate.getUser().getPerson() != null);
        globalContext.put("institutionAcronym", Unit.getInstitutionAcronym());
        globalContext.put("notAvailableMessage", notAvailableMessage(bean));
        globalContext.put("executionCourse", executionCourse);
        globalContext.put("inquiriesResultsBean", bean);
        globalContext.put("loginUrl", CoreConfiguration.getConfiguration().applicationUrl() + "/login");
    }

    private String notAvailableMessage(InquirieResultsBean bean) {
        return Optional.ofNullable(bean.getNewQucIsNotAvailableMessage()).orElseGet(() -> bean.getOldQucIsNotAvailableMessage());
    }

}
