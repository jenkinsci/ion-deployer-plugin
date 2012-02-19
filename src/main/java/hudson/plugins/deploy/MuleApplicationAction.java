/*
 * Copyright 2012 julien.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hudson.plugins.deploy;

import hudson.model.Action;

import java.io.File;

/**
 * Encapsulate artifact location of a maven mule application as a {#link File}.
 */
public final class MuleApplicationAction implements Action {

    private final File file;

    public MuleApplicationAction(final File file) {
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }
    
}