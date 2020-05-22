package qzui.rest;

import com.google.common.base.Optional;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import qzui.domain.JobDefinition;
import qzui.domain.JobDescriptor;
import restx.WebException;
import restx.annotations.*;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.security.PermitAll;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RestxResource
@Component
public class TokenJobResource {

    private final Scheduler scheduler;
    private final Collection<JobDefinition> definitions;
    private final String token;

    public TokenJobResource(Scheduler scheduler, Collection<JobDefinition> definitions) {
        this.scheduler = scheduler;
        this.definitions = definitions;
        this.token = System.getProperty("qzui.token", "qzui");
    }

    @PermitAll
    @POST("/qt/groups/{group}/jobs")
    public JobDescriptor addJob(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) Optional<String> token,
                                String group, JobDescriptor jobDescriptor) {

        if (!token.isPresent() || !token.get().equals(this.token)) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        try {
            jobDescriptor.setGroup(group);
            Set<Trigger> triggers = jobDescriptor.buildTriggers();
            JobDetail jobDetail = jobDescriptor.buildJobDetail();
            if (triggers.isEmpty()) {
                scheduler.addJob(jobDetail, false);
            } else {
                scheduler.scheduleJob(jobDetail, triggers, false);
            }
            return jobDescriptor;
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @PermitAll
    @GET("/qt/groups/{group}/jobs")
    public Set<JobKey> getJobKeys(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) Optional<String> token,
                                  String group) {

        if (!token.isPresent() || !token.get().equals(this.token)) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        try {
            return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @PermitAll
    @GET("/qt/groups/{group}/jobs/{name}")
    public Optional<JobDescriptor> getJob(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) Optional<String> token,
                                          String group, String name) {

        if (!token.isPresent() || !token.get().equals(this.token)) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        try {
            JobDetail jobDetail = scheduler.getJobDetail(new JobKey(name, group));
            if (jobDetail == null) {
                return Optional.absent();
            }

            for (JobDefinition definition : definitions) {
                if (definition.acceptJobClass(jobDetail.getJobClass())) {
                    return Optional.of(definition.buildDescriptor(
                            jobDetail, scheduler.getTriggersOfJob(jobDetail.getKey())));
                }
            }

            throw new IllegalStateException("can't find job definition for " + jobDetail
                    + " - available job definitions: " + definitions);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @PermitAll
    @DELETE("/qt/groups/{group}/jobs/{name}")
    public void deleteJob(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) Optional<String> token,
                          String group, String name) {

        if (!token.isPresent() || !token.get().equals(this.token)) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        try {
            scheduler.deleteJob(new JobKey(name, group));
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @PermitAll
    @DELETE("/qt/groups/{group}")
    public void deleteJobs(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) Optional<String> token,
                           String group) {

        if (!token.isPresent() || !token.get().equals(this.token)) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        Set<JobKey> jobKeys;
        try {
            jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));
        } catch (SchedulerException e) {
            e.printStackTrace();
            jobKeys = new HashSet<>();
        }

        for (JobKey jobKey : jobKeys) {
            try {
                scheduler.deleteJob(jobKey);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }
}
