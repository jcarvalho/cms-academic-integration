package org.fenixedu.cms.rendering;

import java.io.InputStream;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;

import org.fenixedu.bennu.cms.domain.CMSThemeLoader;
import org.fenixedu.bennu.cms.rendering.ThemeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FenixEduThemeProvider implements ThemeProvider {

    public Logger logger = LoggerFactory.getLogger(FenixEduThemeProvider.class);

    @Override
    public void registerThemes(ServletContext context) {
        InputStream in = context.getResourceAsStream("/META-INF/resources/WEB-INF/fenixedu-default-theme.zip");
        ZipInputStream zin = new ZipInputStream(in);
        try {
            CMSThemeLoader.createFromZipStream(zin);
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
            logger.error("Could not load fenixedu theme - sites may not work!", e);
        }
    }

}
