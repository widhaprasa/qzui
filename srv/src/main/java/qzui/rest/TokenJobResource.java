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

import java.util.*;

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
    @POST("/token/job")
    public JobDescriptor addJob(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) String token,
                                JobDescriptor jobDescriptor) {

        if (!this.token.equals(token)) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        try {
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
    @GET("/token/groups")
    public List<String> getAllGroupKeys(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) String token) {

        if (!this.token.equals(token)) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        try {
            return scheduler.getJobGroupNames();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @PermitAll
    @GET("/token/group/{group}/job/{name}")
    public Optional<JobDescriptor> getJob(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) String token,
                                          String group, String name) {

        if (!this.token.equals(token)) {
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
    @GET("/token/group/{group}/jobs")
    public Set<JobKey> getJobKeys(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) String token,
                                  String group) {

        if (!this.token.equals(token)) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        try {
            return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @PermitAll
    @GET("/token/jobs")
    public Set<JobKey> getAllJobKeys(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) String token) {

        if (!this.token.equals(token)) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        try {
            return scheduler.getJobKeys(GroupMatcher.anyJobGroup());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @PermitAll
    @DELETE("/token/group/{group}/job/{name}")
    public void deleteJob(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) String token,
                          String group, String name) {

        if (!this.token.equals(token)) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        try {
            scheduler.deleteJob(new JobKey(name, group));
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @PermitAll
    @DELETE("/token/group/{group}/jobs")
    public void deleteJobKeys(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) String token,
                              String group) {

        if (!this.token.equals(token)) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        Set<JobKey> jobKeys;
        try {
            jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));
        } catch (SchedulerException e) {
            e.printStackTrace();
            jobKeys = new HashSet<>();
        }

        try {
            scheduler.deleteJobs(new ArrayList<JobKey>(jobKeys));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @PermitAll
    @DELETE("/token/jobs")
    public void deleteAllJobs(@Param(value = "Qzui-Token", kind = Param.Kind.HEADER) String token) {

        if (!this.token.equals(token)) {
            throw new WebException(HttpStatus.UNAUTHORIZED);
        }

        try {
            scheduler.clear();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
