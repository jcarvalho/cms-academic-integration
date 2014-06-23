package org.fenixedu.cms.rendering;

import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.fenixedu.bennu.cms.domain.CMSThemeLoader;
import org.fenixedu.bennu.cms.rendering.ProvidesThemes;
import org.fenixedu.bennu.cms.rendering.ThemeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ThemeProvider
public class FenixEduThemeProvider implements ProvidesThemes {

    public Logger LOGGER = LoggerFactory.getLogger(FenixEduThemeProvider.class);

    @Override
    public void loadThemes() {
        InputStream in = CMSThemeLoader.class.getResourceAsStream("/META-INF/resources/WEB-INF/fenixedu-default-theme.zip");
        ZipInputStream zin = new ZipInputStream(in);
        try {
            CMSThemeLoader.createFromZipStream(zin);
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage());
        }
    }

}
