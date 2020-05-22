package qzui.domain;

import org.joda.time.DateTime;
import org.quartz.Trigger;

import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class TriggerDescriptor {

    public static TriggerDescriptor buildDescriptor(Trigger trigger) {

        int every = 0;
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
                .setWhen(trigger.getJobDataMap().getString("when"))
                .setEvery(every);
    }

    private String name;
    private String group;
    private String when;
    private String cron;
    private int every;

    public Trigger buildTrigger(String name, String group) {
        if (!isNullOrEmpty(cron)) {
            return newTrigger()
                    .withIdentity(name, group)
                    .withSchedule(cronSchedule(cron))
                    .usingJobData("cron", cron)
                    .build();

        } else if (!isNullOrEmpty(when)) {
            if ("now".equalsIgnoreCase(when)) {
                return newTrigger()
                        .withIdentity(name, group)
                        .usingJobData("when", when)
                        .build();
            }

            DateTime dateTime = DateTime.parse(when);
            return newTrigger()
                    .withIdentity(name, group)
                    .startAt(dateTime.toDate())
                    .usingJobData("when", when)
                    .build();

        } else if (every > 0) {
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

    public int getEvery() {
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

    public TriggerDescriptor setEvery(final int every) {
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
                ", every='" + every + '\'' +
                '}';
    }
}
