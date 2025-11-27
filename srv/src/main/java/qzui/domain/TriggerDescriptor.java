package qzui.domain;

import org.joda.time.DateTime;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimeZone;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class TriggerDescriptor {

    public static TriggerDescriptor buildDescriptor(Trigger trigger) {

        Integer every = null;
        if (trigger.getJobDataMap().containsKey("every")) {
            Object value = trigger.getJobDataMap().get("every");
            if (value instanceof Number) {
                every = ((Number) value).intValue();
            }
        }

        return new TriggerDescriptor()
                .setGroup(trigger.getKey().getGroup())
                .setName(trigger.getKey().getName())
                .setCron(trigger.getJobDataMap().getString("cron"))
                .setCronTz(trigger.getJobDataMap().getString("cronTz"))
                .setWhen(trigger.getJobDataMap().getString("when"))
                .setEvery(every);
    }

    private String name;
    private String group;
    private String when;
    private String cron;
    private String cronTz;
    private Integer every;

    public Trigger buildTrigger(String name, String group) {
        if (!isNullOrEmpty(cron)) {
            TriggerBuilder<Trigger> triggerBuilder = newTrigger()
                    .withIdentity(name, group);
            CronScheduleBuilder cronBuilder = CronScheduleBuilder.cronSchedule(cron);
            triggerBuilder.withSchedule(cronBuilder)
                    .usingJobData("cron", cron);
            if (!isNullOrEmpty(cronTz)) {
                TimeZone tz = TimeZone.getTimeZone(cronTz);
                if (tz.getID().equals(cronTz) || tz.getID().equals("GMT")
                        && cronTz.equals("GMT")) {
                    cronBuilder.inTimeZone(tz);
                    triggerBuilder.usingJobData("cronTz", cronTz);
                }
            }
            return triggerBuilder.build();

        } else if (!isNullOrEmpty(when)) {
            if ("now".equalsIgnoreCase(when)) {
                return newTrigger()
                        .withIdentity(name, group)
                        .withPriority(100)
                        .usingJobData("when", when)
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                .withMisfireHandlingInstructionFireNow())
                        .build();
            }

            DateTime dateTime = DateTime.parse(when);
            return newTrigger()
                    .withIdentity(name, group)
                    .withPriority(100)
                    .startAt(dateTime.toDate())
                    .usingJobData("when", when)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withMisfireHandlingInstructionFireNow())
                    .build();

        } else if (every != null && every > 0) {
            return newTrigger()
                    .withIdentity(name, group)
                    .withSchedule(
                            simpleSchedule().withIntervalInSeconds(every).repeatForever()
                    )
                    .usingJobData("every", every)
                    .build();
        }
        throw new IllegalStateException("unsupported trigger descriptor " + this);
    }

    private String buildName() {
        return isNullOrEmpty(name) ? "trigger-" + UUID.randomUUID() : name;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getWhen() {
        return when;
    }

    public String getCron() {
        return cron;
    }

    public String getCronTz() {
        return cronTz;
    }

    public Integer getEvery() {
        return every;
    }

    public TriggerDescriptor setName(final String name) {
        this.name = name;
        return this;
    }

    public TriggerDescriptor setGroup(final String group) {
        this.group = group;
        return this;
    }

    public TriggerDescriptor setWhen(final String when) {
        this.when = when;
        return this;
    }

    public TriggerDescriptor setCron(final String cron) {
        this.cron = cron;
        return this;
    }

    public TriggerDescriptor setCronTz(final String cronTz) {
        this.cronTz = cronTz;
        return this;
    }

    public TriggerDescriptor setEvery(final Integer every) {
        this.every = every;
        return this;
    }

    @Override
    public String toString() {
        return "TriggerDescriptor{" +
                "name='" + name + '\'' +
                ", group='" + group + '\'' +
                ", when='" + when + '\'' +
                ", cron='" + cron + '\'' +
                ", cronTz='" + cronTz + '\'' +
                ", every='" + every + '\'' +
                '}';
    }
}
