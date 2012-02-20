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

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Encapsulate all details of an iON account.
 */
public final class IONAccount {

    private final String url;
    private final String username;
    private final String password;

    @DataBoundConstructor
    public IONAccount(final String url, final String username, final String password) {
        if (url == null) {
            throw new IllegalArgumentException("null url");
        }
        if (username == null) {
            throw new IllegalArgumentException("null username");
        }
        if (password == null) {
            throw new IllegalArgumentException("null password");
        }

        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getUrl() {
        return this.url;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getAccountName() {
        return this.username+" @ "+this.url;
    }

}