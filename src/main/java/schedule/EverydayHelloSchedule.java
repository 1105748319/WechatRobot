package schedule;

import config.GlobalConfig;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import schedule.task.GroupEverydayTask;
import utils.DateUtil;
import utils.ScheduleUtil;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Javior
 * @date 2019/6/29 15:01
 */
@Slf4j
public class EverydayHelloSchedule {

    private static final int MILLE_SECONDS_PER_MINUTE = 60 * 1000;

    private static final int MILLE_SECONDS_PER_HOUR = 60 * MILLE_SECONDS_PER_MINUTE;

    private static final int MILLE_SECONDS_PER_DAY = 24 * MILLE_SECONDS_PER_HOUR;

    private static final boolean ENABLE =
            GlobalConfig.getValue("everydayHello.enable", "false").equalsIgnoreCase("true");

    private static String GROUP_TIME = GlobalConfig.getValue("everydayHello.group.time", "").trim();

    private static long GROUP_PERIOD = MILLE_SECONDS_PER_DAY;

    static {
        if (StringUtils.isBlank(GROUP_TIME) || GROUP_TIME.length() != 6) {
            log.info("每日一句定时调度，配置everydayHello.group.time失效，设为默认值083000.");
            GROUP_TIME = "083000";
        }

        final String GROUP_PERIOD_CONFIG = GlobalConfig.getValue("everydayHello.group.period.hour", "").trim();
        if (StringUtils.isNotBlank(GROUP_PERIOD_CONFIG)) {
            try {
                GROUP_PERIOD = Long.parseLong(GROUP_PERIOD_CONFIG) * MILLE_SECONDS_PER_HOUR;
                //                 调试用，以分钟为单位
                //                GROUP_PERIOD = Long.parseLong(GROUP_PERIOD_CONFIG) * 60 * 1000;
            } catch (NumberFormatException e) {
                log.error("startEverydaySchedule解析period失败。GROUP_PERIOD_CONFIG: {}", GROUP_PERIOD_CONFIG);
            }
        }
    }

    public static void startGroupEverydaySchedule() {
        if (!ENABLE) {
            log.info("startGroupEverydaySchedule, 配置为不启用群聊发送每日一句定时调度。");
            return;
        }
        try {
            Date curDate = new Date();
            String curTime = DateUtil.getFormatDate(curDate, "HHmmss");
            Date scheduleDate =
                    DateUtil.parseDate(DateUtil.getFormatDate(curDate, "yyyyMMdd") + GROUP_TIME, "yyyyMMddHHmmss");
            if (scheduleDate == null) {
                log.error("解析scheduleDate为空，调度失败。");
                return;
            }
            if (curTime != null && GROUP_TIME.compareTo(curTime) <= 0) {
                scheduleDate = DateUtil.addOneDay(scheduleDate);
            }

            long initialDelay = Math.max(0, scheduleDate.getTime() - System.currentTimeMillis());
            log.info("EverydayHelloSchedule::startGroupEverydaySchedule, schedule >> scheduleDate: {}, period: {}分钟",
                    DateUtil.getViewDate(scheduleDate), GROUP_PERIOD / MILLE_SECONDS_PER_MINUTE);
            ScheduleUtil
                    .scheduleAtFixedRate(new GroupEverydayTask(), initialDelay, GROUP_PERIOD, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("startGroupEverydaySchedule error, ", e);
        }
    }

}
