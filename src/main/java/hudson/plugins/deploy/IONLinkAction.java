/**
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright 2012 Julien Eluard
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

import hudson.model.Action;

/**
 * Represent the link to the deployed iON instance.
 */
public final class IONLinkAction implements Action {

    private final String domain;

    public IONLinkAction(final String domain) {
        if (domain == null) {
            throw new IllegalArgumentException("null domain");
        }

        this.domain = domain;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/ion-jenkins-deploy/logo.png";
    }

    @Override
    public String getDisplayName() {
        return "iON URL";
    }

    @Override
    public String getUrlName() {
        return "http://"+this.domain+".muleion.com";
    }
    
}