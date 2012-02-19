/**
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright 2010 Julien Eluard
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     [http://www.apache.org/licenses/LICENSE-2.0]
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package hudson.plugins.deploy;


import hudson.Extension;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.model.BuildListener;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.maven.project.MavenProject;

/**
 * Keep track of `mule` type maven artifacts as {@link MuleApplicationAction}.
 */
public final class MuleApplicationTracker extends MavenReporter implements Serializable {

    @Override
    public boolean postBuild(final MavenBuildProxy build, final MavenProject project, final BuildListener listener) throws InterruptedException, IOException {
        if (project.getArtifact() == null || !"mule".equals(project.getArtifact().getType())) {
            return true;
        }

        if (project.getAttachedArtifacts() != null && !project.getAttachedArtifacts().isEmpty()) {
            //Record the mule application File as an Action
            final String filePath = project.getAttachedArtifacts().get(0).getFile().getPath();
            build.execute(new MavenBuildProxy.BuildCallable<Void, IOException>() {
                @Override
                public Void call(final MavenBuild build) throws IOException, InterruptedException {
                    //Mule packaging type attach mule application file first
                    build.addAction(new MuleApplicationAction(filePath));
                    build.save();
                    return null;
                }
            });
        }
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends MavenReporterDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.MuleApplicationTracker_DisplayName();
        }
        @Override
        public MavenReporter newAutoInstance(final MavenModule module) {
            return new MuleApplicationTracker();
        }
    }

}