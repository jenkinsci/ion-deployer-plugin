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

import hudson.model.InvisibleAction;

/**
 * Encapsulate artifact location of a maven mule application as a {#link File#getPath()}.
 */
public final class MuleApplicationAction extends InvisibleAction {

    private final String filePath;

    public MuleApplicationAction(final String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return this.filePath;
    }
    
}