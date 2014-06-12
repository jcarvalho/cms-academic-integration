package org.fenixedu.cms.domain;

import java.util.Set;

import net.sourceforge.fenixedu.domain.CompetenceCourse;
import net.sourceforge.fenixedu.domain.CurricularCourse;
import net.sourceforge.fenixedu.domain.ExecutionSemester;

import org.fenixedu.commons.i18n.LocalizedString;

import com.google.common.base.Objects;

public class CompetenceCourseBean {
    private final CompetenceCourse competenceCourse;
    private final ExecutionSemester executionSemester;
    private final Set<CurricularCourse> curricularCourses;
    private final LocalizedString name;
    private final LocalizedString objectives;

    public CompetenceCourseBean(CompetenceCourse competenceCourse, Set<CurricularCourse> curricularCourses,
            ExecutionSemester executionSemester) {
        this.competenceCourse = competenceCourse;
        this.executionSemester = executionSemester;
        this.curricularCourses = curricularCourses;
        this.name = competenceCourse.getNameI18N(executionSemester).toLocalizedString();
        this.objectives = competenceCourse.getObjectivesI18N(executionSemester).toLocalizedString();
    }

    public CompetenceCourse getCompetenceCourse() {
        return competenceCourse;
    }

    public ExecutionSemester getExecutionSemester() {
        return executionSemester;
    }

    public Set<CurricularCourse> getCurricularCourses() {
        return curricularCourses;
    }

    public LocalizedString getName() {
        return name;
    }

    public LocalizedString getObjectives() {
        return objectives;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", this.name).add("objectives", this.objectives)
                .add("executionSemester", executionSemester).add("curricularCourses", curricularCourses).toString();
    }
}