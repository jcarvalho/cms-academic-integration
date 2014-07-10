package org.fenixedu.cms.domain;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Grouping;
import net.sourceforge.fenixedu.domain.Lesson;
import net.sourceforge.fenixedu.domain.Shift;
import net.sourceforge.fenixedu.domain.StudentGroup;

import org.fenixedu.bennu.cms.domain.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;

@ComponentType(type = "groups", name = "Groups", description = "Groups for an Execution Course")
public class ExecutionCourseGroups extends ExecutionCourseGroups_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        globalContext.put("groupings", executionCourse.getGroupings());
    }
    
    public static class GroupingBean {
        private Grouping grouping;
        private Map<Shift, SortedSet<StudentGroup>> shiftGroups;

        public GroupingBean(Grouping grouping) {
            this.grouping = grouping;
            this.shiftGroups = grouping.getStudentGroupsIndexedByShift();
            for (Entry<Shift, SortedSet<StudentGroup>> x : shiftGroups.entrySet()) {
                for (Lesson y : x.getKey().getAssociatedLessonsSet()) {

                }
                x.getValue().stream().forEach(z -> z.getShift().getExternalId());
            }
        }

    }
    
}
