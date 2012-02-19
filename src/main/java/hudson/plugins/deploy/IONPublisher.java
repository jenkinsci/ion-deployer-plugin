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

import com.github.jeluard.ion.Application;
import com.github.jeluard.ion.Connection;
import com.github.jeluard.ion.DomainConnection;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.maven.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Deploys mule app to iON.
 */
public class IONPublisher extends Notifier implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String accountName;
    private final String domain;
    private final String muleVersion;
    private final int workers;
    private final long maxWaitTime;

    @DataBoundConstructor
    public IONPublisher(final String accountName, final String domain, final String muleVersion, final int workers, final long maxWaitTime) {
        System.out.println("IONPublisher: "+this.accountName+" "+domain+" "+muleVersion+" "+workers+" "+maxWaitTime);
        //accountName is null when saving with no account configured
        if (accountName != null && domain == null) {
            throw new  IllegalArgumentException("null domain");
        }
        if (accountName != null && muleVersion == null) {
            throw new  IllegalArgumentException("null muleVersion");
        }

        this.accountName = accountName;
        this.domain = domain;
        this.muleVersion = muleVersion;
        this.workers = workers;
        this.maxWaitTime = maxWaitTime;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        //No accountName. Do not fail the build.
        if (this.accountName == null) {
            return true;
        }

        final List<File> muleApplications = retrieveMuleApplications(build);
        if (muleApplications.isEmpty()) {
            throw new AbortException(Messages.IONPublisher_NoMuleApplication());
        }
        if (muleApplications.size() > 1) {
            throw new AbortException(Messages.IONPublisher_TooManyMuleApplications(muleApplications));
        }
        final File muleApplication = muleApplications.get(0);
        //TODO copy file locally?
        final IONAccount account = getDescriptor().getAccount(this.accountName);
        final DomainConnection domainConnection = new Connection(account.getUrl(), account.getUsername(), account.getPassword()).on(this.domain);
        domainConnection.deploy(muleApplication, this.muleVersion, this.workers, this.maxWaitTime);

        return true;
    }

    protected final List<File> retrieveMuleApplications(final AbstractBuild<?, ?> build) {
        final List<File> allmuleApplications = new LinkedList<File>();
        final List<MuleApplicationAction> actions = build.getActions(MuleApplicationAction.class);
        if (!actions.isEmpty()) {
            allmuleApplications.add(actions.get(0).getFile());
        }

        if (build instanceof MavenModuleSetBuild) {
            for (final List<MavenBuild> mavenBuilds : ((MavenModuleSetBuild) build).getModuleBuilds().values()) {
                for (final MavenBuild mavenBuild : mavenBuilds) {
                    final List<MuleApplicationAction> moduleActions = mavenBuild.getActions(MuleApplicationAction.class);
                    if (!moduleActions.isEmpty()) {
                        allmuleApplications.add(moduleActions.get(0).getFile());
                    }
                }
            }
        }
        return allmuleApplications;
    }

    @Override
    public Collection<? extends Action> getProjectActions(final AbstractProject<?, ?> project) {
        return Collections.singleton(new IONLinkAction(this.domain));
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private final CopyOnWriteList<IONAccount> accounts = new CopyOnWriteList<IONAccount>();

        public DescriptorImpl() {
            super(IONPublisher.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.IONPublisher_DisplayName();
        }

        @Override
        public Publisher newInstance(final StaplerRequest request, final JSONObject object) {
            final IONPublisher publisher = request.bindParameters(IONPublisher.class, "ionaccount.");
            if (publisher.accountName == null) {
                return null;
            }
            return publisher;
        }

        @Override
        public boolean configure(final StaplerRequest request, final JSONObject object) {
            this.accounts.replaceBy(request.bindParametersToList(IONAccount.class, "ionaccount."));
            save();
            return true;
        }

        public final FormValidation doTestConnection(@QueryParameter("ionaccount.url") final String url, @QueryParameter("ionaccount.username") final String username, @QueryParameter("ionaccount.password") final String password) {
            if (new Connection(url, username, password).test()) {
                return FormValidation.ok(Messages.IONPublisher_TestConnectionSuccess());
            } else {
                return FormValidation.error(Messages.IONPublisher_TestConnectionFailure());
            }
        }

        public final FormValidation doCheckUsername(@QueryParameter final String username) throws IOException, ServletException {
            if (StringUtils.isBlank(username)) {
                return FormValidation.error(Messages.IONPublisher_UsernameCheckFailure());
            }
            return FormValidation.ok();
        }

        public final FormValidation doCheckPassword(@QueryParameter final String password) throws IOException, ServletException {
            if (StringUtils.isBlank(password)) {
                return FormValidation.error(Messages.IONPublisher_PasswordCheckFailure());
            }
            return FormValidation.ok();
        }

        public final FormValidation doCheckDomain(@QueryParameter final String domain, @QueryParameter final String accountName) throws IOException, ServletException {
            if (StringUtils.isBlank(domain)) {
                return FormValidation.error(Messages.IONPublisher_DomainCheckFailure());
            }
            final IONAccount account = getAccount(accountName);
            if (listDomains(account.getUrl(), account.getUsername(), account.getPassword()).contains(domain)) {
                return FormValidation.ok();
            }
            return FormValidation.error(Messages.IONPublisher_DomainCheckFailure());
        }

        /**
         * @param value
         * @return all matching application name from current auto completed value
         * @throws Exception 
         */
        public AutoCompletionCandidates doAutoCompleteDomains(@QueryParameter final String value) throws Exception {
            //TODO Extract parameter values
            final String url = "";//staplerRequest.getParameter("url");
            final String username = "";//staplerRequest.getParameter("username");
            final String password = "";//staplerRequest.getParameter("password");

            final AutoCompletionCandidates candidates = new AutoCompletionCandidates();
            for (final String domain : listDomains(url, username, password)) {
                if (domain.startsWith(value)) {
                    candidates.add(domain);
                }
            }
            return candidates;
        }

        protected final List<String> listDomains(final String url, final String username, final String password) {
            final List<String> domains = new LinkedList<String>();
            for (final Application application : new Connection(url, username, password).list()) {
                domains.add(application.getDomain());
            }
            return domains;
        }

        public final IONAccount[] getAccounts() {
            return this.accounts.toArray(new IONAccount[this.accounts.size()]);
        }

        public final IONAccount getAccount(final String name) {
            for (final IONAccount account : getAccounts()) {
                if (account.getAccountName().equals(name)) {
                    return account;
                }
            }
            throw new IllegalArgumentException(Messages.IONPublisher_UnknownAccount(name));
        }

        @Override
        public final boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return jobType.equals(MavenModule.class) || jobType.equals(MavenModuleSet.class);
        }

    }

}