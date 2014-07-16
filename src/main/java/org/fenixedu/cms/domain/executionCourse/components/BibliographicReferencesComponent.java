package org.fenixedu.cms.domain.executionCourse.components;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.degreeStructure.BibliographicReferences.BibliographicReference;
import net.sourceforge.fenixedu.domain.degreeStructure.CompetenceCourseInformation;

import org.fenixedu.bennu.cms.domain.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseSite;

import com.google.common.collect.Lists;

@ComponentType(type = "bibliographicReferences", name = "bibliographicReferences",
        description = "Bibliographic References for an Execution Course")
public class BibliographicReferencesComponent extends BibliographicReferencesComponent_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        globalContext.put("executionCourse", executionCourse);
        globalContext.put("mainReferences", mainReferences(executionCourse));
        globalContext.put("secundaryReferences", secundaryReferences(executionCourse));
        globalContext.put("optionalReferences", optionalReferences(executionCourse));
        globalContext.put("nonOptionalReferences", nonOptionalReferences(executionCourse));
    }

    public List<BibliographicReference> secundaryReferences(ExecutionCourse executionCourse) {
        return bibliographiReferences(executionCourse).stream().filter(b -> b.isSecondary()).collect(Collectors.toList());
    }

    public List<BibliographicReference> mainReferences(ExecutionCourse executionCourse) {
        return bibliographiReferences(executionCourse).stream().filter(b -> b.isMain()).collect(Collectors.toList());
    }

    public List<net.sourceforge.fenixedu.domain.BibliographicReference> optionalReferences(ExecutionCourse executionCourse) {
        return executionCourse.getOrderedBibliographicReferences().stream().filter(b -> b.isOptional())
                .collect(Collectors.toList());
    }

    public List<net.sourceforge.fenixedu.domain.BibliographicReference> nonOptionalReferences(ExecutionCourse executionCourse) {
        return executionCourse.getOrderedBibliographicReferences().stream().filter(b -> !b.isOptional())
                .collect(Collectors.toList());
    }

    public List<BibliographicReference> bibliographiReferences(ExecutionCourse executionCourse) {
        final List<BibliographicReference> references = Lists.newArrayList();
        for (CompetenceCourseInformation competenceCourseInfo : executionCourse.getCompetenceCoursesInformations()) {
            if (competenceCourseInfo.getBibliographicReferences() != null) {
                references.addAll(competenceCourseInfo.getBibliographicReferences().getBibliographicReferencesSortedByOrder());
            }
        }
        return references;
    }

}
