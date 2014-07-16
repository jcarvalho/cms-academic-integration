package org.fenixedu.cms.domain.executionCourse.components;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.Attends;
import net.sourceforge.fenixedu.domain.Evaluation;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Mark;

import org.fenixedu.bennu.cms.domain.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseSite;

@ComponentType(type = "marks", name = "Marks", description = "Marks for an Execution Course")
public class MarksComponent extends MarksComponent_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        Map<Attends, Map<Evaluation, Mark>> attendsMap = attendsMap(executionCourse);
        globalContext.put("attendsMap", attendsMap);
        globalContext.put("numberOfStudents", attendsMap.size());
        globalContext.put("dont-cache-pages-in-search-engines", Boolean.TRUE);
        globalContext.put("evaluations", executionCourse.getOrderedAssociatedEvaluations());
    }

    private Map<Attends, Map<Evaluation, Mark>> attendsMap(ExecutionCourse executionCourse) {
        final Map<Attends, Map<Evaluation, Mark>> attendsMap =
                new TreeMap<Attends, Map<Evaluation, Mark>>(Attends.COMPARATOR_BY_STUDENT_NUMBER);
        for (final Attends attends : executionCourse.getAttendsSet()) {
            final Map<Evaluation, Mark> evaluationsMap = new TreeMap<Evaluation, Mark>(ExecutionCourse.EVALUATION_COMPARATOR);
            attendsMap.put(attends, evaluationsMap);
            for (final Evaluation evaluation : executionCourse.getAssociatedEvaluationsSet()) {
                if (evaluation.getPublishmentMessage() != null) {
                    evaluationsMap.put(evaluation, null);
                }
            }
            for (final Mark mark : attends.getAssociatedMarksSet()) {
                if (mark.getEvaluation().getPublishmentMessage() != null) {
                    evaluationsMap.put(mark.getEvaluation(), mark);
                }
            }
        }
        return attendsMap;
    }
}
