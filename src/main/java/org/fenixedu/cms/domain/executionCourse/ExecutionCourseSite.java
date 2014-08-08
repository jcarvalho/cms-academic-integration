package org.fenixedu.cms.domain.executionCourse;

import java.util.Objects;
import java.util.SortedSet;
import java.util.stream.Stream;

import net.sourceforge.fenixedu.domain.CurricularCourse;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.ExecutionSemester;

import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.commons.i18n.LocalizedString;

import pt.ist.fenixframework.Atomic;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

public class ExecutionCourseSite extends ExecutionCourseSite_Base {

    public ExecutionCourseSite() {
    }

    public ExecutionCourseSite(ExecutionCourse executionCourse) {
        setExecutionCourse(executionCourse);
        setPublished(true);
        setDescription(createDescription(executionCourse));
        setName(executionCourse.getNameI18N().toLocalizedString());
        setSlug(createSlug(executionCourse));
        updateMenuFunctionality();
        setBennu(Bennu.getInstance());
    }

    private LocalizedString createDescription(ExecutionCourse executionCourse) {
        ExecutionSemester period = executionCourse.getExecutionPeriod();
        SortedSet<CurricularCourse> courses = executionCourse.getCurricularCoursesSortedByDegreeAndCurricularCourseName();

        Stream<LocalizedString> objectives =
                courses.stream().map(CurricularCourse::getCompetenceCourse).filter(Objects::nonNull)
                        .map(c -> c.getObjectivesI18N(period)).filter(Objects::nonNull)
                        .map(MultiLanguageString::toLocalizedString);

        return objectives.findAny().orElse(new LocalizedString());
    }

    @Override
    @Atomic
    public void delete() {
        setExecutionCourse(null);
        super.delete();
    }

    public String createSlug(ExecutionCourse executionCourse) {
        final ExecutionSemester executionSemester = getExecutionCourse().getExecutionPeriod();
        String acronym = getExecutionCourse().getSigla();
        Integer semester = executionSemester.getSemester();
        String executionYear = executionSemester.getExecutionYear().getYear().replace('/', '-');
        return Site.slugify(String.format("%s-%s-%d-semestre", acronym, executionYear, semester));
    }
}
