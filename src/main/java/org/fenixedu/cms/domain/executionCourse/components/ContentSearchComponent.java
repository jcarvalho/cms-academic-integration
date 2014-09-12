package org.fenixedu.cms.domain.executionCourse.components;

import javax.servlet.http.HttpServletRequest;

import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;

@ComponentType(name = "ContentSearch", description = "Search content of an Execution Course")
public class ContentSearchComponent extends ContentSearchComponent_Base {
    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {    }
}
