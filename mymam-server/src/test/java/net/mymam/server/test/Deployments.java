/* MyMAM - Open Source Digital Media Asset Management.
 * http://www.mymam.net
 *
 * Copyright 2013, MyMAM contributors as indicated by the @author tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mymam.server.test;

import net.balusc.webapp.FileServlet;
import net.mymam.controller.DashboardBean;
import net.mymam.data.json.MediaFileImportStatus;
import net.mymam.ejb.*;
import net.mymam.entity.MediaFile;
import net.mymam.exceptions.InvalidImportStateException;
import net.mymam.rest.api.MediaFiles;
import net.mymam.rest.util.Jpa2Json;
import net.mymam.security.SecurityFilter;
import net.mymam.ui.UploadedFile;
import net.mymam.upload.UploadMultipartRequestFilter;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author fstab
 */
public class Deployments {

    /**
     * Relative path to the src/main/webapp directory.
     * Will be used to find WEB-INF/jboss-ejb3.xml
     */
    private static final String WEBAPP_SRC = ArquillianTestHelper.makeWebappSrc();

    public static WebArchive createFullDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                        // JPA entities
                .addPackage(MediaFile.class.getPackage())
                        // EJBs
                .addPackage(MediaFileEJB.class.getPackage())
                        // Exceptions
                .addPackage(InvalidImportStateException.class.getPackage())
                        // SecurityFilter
                .addPackage(SecurityFilter.class.getPackage())
                        // mymam-common
                .addPackage(MediaFileImportStatus.class.getPackage())
                        // jsf controllers
                .addPackage(DashboardBean.class.getPackage())
                        // upload multipart
                .addPackage(UploadMultipartRequestFilter.class.getPackage())
                        // BalusC's FileServlet
                .addPackage(FileServlet.class.getPackage())
                        // ui components
                .addPackage(UploadedFile.class.getPackage())
                        // rest interface
                .addPackage(MediaFiles.class.getPackage())
                .addPackage(Jpa2Json.class.getPackage())
                        // xhtml files
                .merge(webappDir(), "/", Filters.exclude("WEB-INF"))
                        // i18n resources
                .merge(i18nDir(), "/WEB-INF/classes", Filters.include(".*.properties$"))
                        // other resources
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(new File(WEBAPP_SRC, "WEB-INF/jboss-ejb3.xml"))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(
                        new StringAsset("<faces-config version=\"2.0\"/>"),
                        "faces-config.xml")
                        // maven dependencies
                .addAsLibraries(mavenDependencies());
    }

    private static File[] mavenDependencies() {
        // https://community.jboss.org/wiki/HowToIAddMavenArtifactsToMyShrinkWrapArchives
        String[] dependencies = new String[]{
            "commons-fileupload:commons-fileupload:1.2.2",
            "commons-io:commons-io:2.4",
            "com.google.guava:guava:14.0-rc1"
        };
        Set<File> result = new HashSet<>();
        File pom = new File(new File(WEBAPP_SRC).getParentFile().getParentFile().getParentFile(), "pom.xml");
        for ( String dependency : dependencies ) {
            File[] files = Maven.resolver().loadPomFromFile(pom).resolve(dependency).withTransitivity().asFile();
            result.addAll(Arrays.asList(files));
        }
        return result.toArray(new File[]{});
    }

    private static GenericArchive webappDir() {
        return ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class).importDirectory(WEBAPP_SRC).as(GenericArchive.class);
    }

    private static GenericArchive i18nDir() {
        File resourcesDir = new File(new File(WEBAPP_SRC).getParentFile(), "resources");
        return ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class).importDirectory(resourcesDir).as(GenericArchive.class);
    }
}
