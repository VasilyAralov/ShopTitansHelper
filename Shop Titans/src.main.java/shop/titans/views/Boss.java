package shop.titans.views;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import shop.titans.interfaces.ADBObject;

public class Boss extends ADBObject implements Comparable<Boss> {

	private String name;
	private Period period;
	private ZonedDateTime lastSeen;
	private ZonedDateTime nextRespawn;

	public Boss(int id, String name,
			Integer period, Timestamp lastSeen) {
		super(id, true);
		this.name = name;
		this.period = Period.ofDays(period);
		this.lastSeen = lastSeen.toLocalDateTime().atZone(ZoneOffset.UTC);
	}
	
	public Boss(Integer id) {
		super(id, false);
	}

	public String getName() {
		return name;
		
	}

	public ZonedDateTime nextRespawn() {
		if (nextRespawn == null || nextRespawn.isBefore(ZonedDateTime.now())) {
			if (lastSeen.isAfter(ZonedDateTime.now())) {
				nextRespawn = lastSeen;
				return nextRespawn;
			}
			var multipiier = (Long.valueOf(lastSeen.until(ZonedDateTime.now(), ChronoUnit.DAYS)).intValue()
					/ period.getDays()) + 1;
			var days = period.multipliedBy(multipiier);
			nextRespawn = lastSeen.plus(days);
		}
		return nextRespawn;
	}

	@Override
	public String toString() {
		String text = "Boss " + getName() + " will come at "
				+ nextRespawn().withZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME)
				+ " in ";
		var timeleft = Duration.between(ZonedDateTime.now(), nextRespawn());
		var day = timeleft.toDaysPart();
		if (day > 0) {
			text += day + " day";
			if (day > 1) {
				text += "s ";
			} else {
				text += " ";
			}
		}
		text += "%02d:%02d:%02d";
		return String.format(text, timeleft.toHoursPart(), timeleft.toMinutesPart(), timeleft.toSecondsPart());
	}

	@Override
	public int compareTo(Boss o) {
		int compare = this.nextRespawn().compareTo(o.nextRespawn());
		if (compare == 0) {
			return this.getId() - o.getId();
		}
		return compare;
	}

	@Override
	protected <T extends ADBObject> void join(T that) {
		if (!(that instanceof Boss boss)) {
			throw new IllegalArgumentException("Can't join " + that.getClass().getCanonicalName() + " to " + Item.class.getCanonicalName());
		}
		this.lastSeen = boss.lastSeen;
		this.name = boss.name;
		this.nextRespawn = boss.nextRespawn;
		this.period = boss.period;
	}
}
