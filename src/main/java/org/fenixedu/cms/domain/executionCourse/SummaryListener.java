package org.fenixedu.cms.domain.executionCourse;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.Locale;
import java.util.Optional;

import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.Professorship;
import net.sourceforge.fenixedu.domain.ShiftType;
import net.sourceforge.fenixedu.domain.Summary;
import net.sourceforge.fenixedu.domain.Teacher;

import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.spaces.domain.Space;

import com.google.common.base.Strings;

public class SummaryListener {
    private static final LocalizedString SUMMARY = getLocalizedString("resources.FenixEduCMSResources", "label.summaries");

    public static void updatePost(Post post, Summary summary) {
        ExecutionCourseSite site = summary.getExecutionCourse().getCmsSite();

        post.setSite(site);
        post.setSlug("summary-" + summary.getOid());
        post.setName(summary.getTitle().toLocalizedString());

        post.setBody(summary.getSummaryText().toLocalizedString());
        post.setCreationDate(summary.getSummaryDateTime());

        post.addCategories(site.categoryForSlug("summary", SUMMARY));

        Professorship professorship = summary.getProfessorship();
        Teacher teacher = summary.getTeacher();
        if (professorship != null || teacher != null) {
            Person professor = professorship != null ? professorship.getPerson() : teacher.getPerson();
            post.setCreatedBy(Optional.ofNullable(professor.getUser()).orElse(Authenticate.getUser()));
            LocalizedString professorName = makeLocalized(professor.getPresentationName());
            post.addCategories(site.categoryForSlug("summary-professor-" + professor.getExternalId(), professorName));
        }

        if (summary.getShift() != null) {
            LocalizedString summaryShiftName = makeLocalized(summary.getShift().getPresentationName());
            post.addCategories(site.categoryForSlug("summary-shift-" + summary.getShift().getOid(), summaryShiftName));
        }

        ShiftType summaryType = summary.getSummaryType();
        if (summaryType != null) {
            LocalizedString summaryTypeName = makeLocalized(summaryType.getFullNameTipoAula());
            post.addCategories(site.categoryForSlug("summary-type-" + summaryType.getSiglaTipoAula(), summaryTypeName));
        }

        Space room = summary.getRoom();
        Optional<LocalizedString> roomName = tryGetRoomName(room);
        if (roomName.isPresent()) {
            post.addCategories(site.categoryForSlug("summary-room-" + room.getExternalId(), roomName.get()));
        }

    }

    private static Optional<LocalizedString> tryGetRoomName(Space room) {
        try {
            if (room != null && !Strings.isNullOrEmpty(room.getName())) {
                return Optional.of(makeLocalized(room.getName()));
            }
        } catch (Exception e) {
        }
        return Optional.empty();
    }

    private static LocalizedString makeLocalized(String value) {
        LocalizedString.Builder builder = new LocalizedString.Builder();
        for (Locale locale : CoreConfiguration.supportedLocales()) {
            builder.with(locale, value);
        }
        return builder.build();
    }

}
