package net.rainfantasy.claims_and_warfares.common.setups.data_types;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SerializableDateTime implements ISerializableNBTData<SerializableDateTime, CompoundTag>, Comparable<SerializableDateTime>, INetworkInfo<SerializableDateTime> {
	
	private final int year;
	private final int month;
	private final int day;
	private final int hour;
	private final int minute;
	private final int second;
	private final int millisecond;
	private final String timeZone;
	
	@Contract(pure = true, value = "-> new")
	public static SerializableDateTime now() {
		return new SerializableDateTime();
	}
	
	public SerializableDateTime(int year, int month, int day, int hour, int minute, int second, int millisecond, String timeZone) {
		if (!isValidDateTime(year, month, day, hour, minute, second, millisecond)) {
			throw new IllegalArgumentException("Invalid DateTime");
		}
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.millisecond = millisecond;
		this.timeZone = timeZone;
	}
	
	public SerializableDateTime(int year, int month, int day, int hour, int minute, int second, int millisecond) {
		this(year, month, day, hour, minute, second, millisecond, "UTC");
	}
	
	public SerializableDateTime(LocalDateTime dateTime) {
		this(
		dateTime.getYear(),
		dateTime.getMonthValue(),
		dateTime.getDayOfMonth(),
		dateTime.getHour(),
		dateTime.getMinute(),
		dateTime.getSecond(),
		dateTime.getNano() / 1000000,
		ZoneId.systemDefault().getId()
		);
	}
	
	public SerializableDateTime(ZonedDateTime dateTime) {
		this(
		dateTime.getYear(),
		dateTime.getMonthValue(),
		dateTime.getDayOfMonth(),
		dateTime.getHour(),
		dateTime.getMinute(),
		dateTime.getSecond(),
		dateTime.getNano() / 1000000,
		dateTime.getZone().getId()
		);
	}
	
	public SerializableDateTime(long timeMillis) {
		this(ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault()));
	}
	
	public SerializableDateTime() {
		this(System.currentTimeMillis());
	}
	
	public static boolean isValidMonth(int month) {
		return month >= 1 && month <= 12;
	}
	
	public static boolean isLeapYear(int year) {
		return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
	}
	
	public static boolean isValidDay(int year, int month, int day) {
		switch (month) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				return day >= 1 && day <= 31;
			case 4:
			case 6:
			case 9:
			case 11:
				return day >= 1 && day <= 30;
			case 2:
				if (isLeapYear(year)) {
					return day >= 1 && day <= 29;
				} else {
					return day >= 1 && day <= 28;
				}
			default:
				return false;
		}
	}
	
	public static boolean isValidHour(int hour) {
		return hour >= 0 && hour <= 23;
	}
	
	public static boolean isValidMinute(int minute) {
		return minute >= 0 && minute <= 59;
	}
	
	public static boolean isValidSecond(int second) {
		return second >= 0 && second <= 59;
	}
	
	public static boolean isValidMillisecond(int millisecond) {
		return millisecond >= 0 && millisecond <= 999;
	}
	
	public static boolean isValidDateTime(int year, int month, int day, int hour, int minute, int second, int millisecond) {
		return isValidMonth(month) && isValidDay(year, month, day) && isValidHour(hour) && isValidMinute(minute) && isValidSecond(second) && isValidMillisecond(millisecond);
	}
	
	@Override
	public SerializableDateTime readFromNBT(CompoundTag nbt) {
		return new SerializableDateTime(
		nbt.getInt("year"),
		nbt.getInt("month"),
		nbt.getInt("day"),
		nbt.getInt("hour"),
		nbt.getInt("minute"),
		nbt.getInt("second"),
		nbt.getInt("millisecond"),
		nbt.getString("timeZone")
		);
	}
	
	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		nbt.putInt("year", this.year);
		nbt.putInt("month", this.month);
		nbt.putInt("day", this.day);
		nbt.putInt("hour", this.hour);
		nbt.putInt("minute", this.minute);
		nbt.putInt("second", this.second);
		nbt.putInt("millisecond", this.millisecond);
		nbt.putString("timeZone", this.timeZone);
		return nbt;
	}
	
	@Override
	public int compareTo(@NotNull SerializableDateTime o) {
		return ZonedDateTime.of(
		this.year, this.month, this.day, this.hour, this.minute, this.second, this.millisecond * 1000000, ZoneId.of(this.timeZone)
		).compareTo(ZonedDateTime.of(
		o.year, o.month, o.day, o.hour, o.minute, o.second, o.millisecond * 1000000, ZoneId.of(o.timeZone)
		));
	}
	
	public long toEpochMillis() {
		return ZonedDateTime.of(
		this.year, this.month, this.day, this.hour, this.minute, this.second, this.millisecond * 1000000, ZoneId.of(this.timeZone)
		).toInstant().toEpochMilli();
	}
	
	public ZonedDateTime toZonedDateTime() {
		return ZonedDateTime.of(
		this.year, this.month, this.day, this.hour, this.minute, this.second, this.millisecond * 1000000, ZoneId.of(this.timeZone)
		);
	}
	
	public SerializableDateTime plusSeconds(long seconds) {
		return new SerializableDateTime(this.toZonedDateTime().plusSeconds(seconds));
	}
	
	public SerializableDateTime plusMinutes(long minutes) {
		return new SerializableDateTime(this.toZonedDateTime().plusMinutes(minutes));
	}
	
	public SerializableDateTime plusHours(long hours) {
		return new SerializableDateTime(this.toZonedDateTime().plusHours(hours));
	}
	
	public SerializableDateTime plusDays(long days) {
		return new SerializableDateTime(this.toZonedDateTime().plusDays(days));
	}
	
	public SerializableDateTime plusMonths(long months) {
		return new SerializableDateTime(this.toZonedDateTime().plusMonths(months));
	}
	
	public SerializableDateTime plusYears(long years) {
		return new SerializableDateTime(this.toZonedDateTime().plusYears(years));
	}
	
	public SerializableDateTime toNextSpecificHour(int hour) {
		ZonedDateTime zonedDateTime = this.toZonedDateTime();
		if (zonedDateTime.getHour() >= hour) {
			zonedDateTime = zonedDateTime.plusDays(1);
		}
		return new SerializableDateTime(zonedDateTime.withHour(hour).withMinute(0).withSecond(0).withNano(0));
	}
	
	public SerializableDateTime toNextSpecificHourWithAtLeastIntervalHourOrElseNextDay(int specificHour, int minIntervalHour) {
		ZonedDateTime zonedDateTime = this.toZonedDateTime();
		if (zonedDateTime.getHour() >= specificHour) {
			zonedDateTime = zonedDateTime.plusDays(1);
		}
		if (zonedDateTime.getHour() - specificHour < minIntervalHour) {
			zonedDateTime = zonedDateTime.plusDays(1);
		}
		return new SerializableDateTime(zonedDateTime.withHour(specificHour).withMinute(0).withSecond(0).withNano(0));
	}
	
	public int getWeekday() {
		return this.getDayOfWeek().getValue();
	}
	
	public DayOfWeek getDayOfWeek() {
		return this.toZonedDateTime().getDayOfWeek();
	}
	
	public int getYear() {
		return year;
	}
	
	public int getMonth() {
		return month;
	}
	
	public int getDay() {
		return day;
	}
	
	public int getHour() {
		return hour;
	}
	
	public int getMinute() {
		return minute;
	}
	
	public int getSecond() {
		return second;
	}
	
	public int getMillisecond() {
		return millisecond;
	}
	
	public String getTimeZone() {
		return timeZone;
	}
	
	//return this - other
	public long getDiffMillis(SerializableDateTime other) {
		return this.toEpochMillis() - other.toEpochMillis();
	}
	
	public Component format(ZoneId zoneId) {
		ZonedDateTime zonedDateTime = this.toZonedDateTime();
		zonedDateTime.withZoneSameInstant(zoneId);
		String year = String.format("%04d", zonedDateTime.getYear());
		String month = String.format("%02d", zonedDateTime.getMonthValue());
		String day = String.format("%02d", zonedDateTime.getDayOfMonth());
		String hour = String.format("%02d", zonedDateTime.getHour());
		String minute = String.format("%02d", zonedDateTime.getMinute());
		String second = String.format("%02d", zonedDateTime.getSecond());
		return Component.translatable("caw.gui.common.time_format", year, month, day, hour, minute, second);
	}
	
	public Component format() {
		return this.format(ZoneId.systemDefault());
	}
	
	@Override
	public void toBytes(FriendlyByteBuf byteBuf) {
		byteBuf.writeInt(this.year);
		byteBuf.writeInt(this.month);
		byteBuf.writeInt(this.day);
		byteBuf.writeInt(this.hour);
		byteBuf.writeInt(this.minute);
		byteBuf.writeInt(this.second);
		byteBuf.writeInt(this.millisecond);
		byteBuf.writeUtf(this.timeZone);
	}
	
	@Override
	public SerializableDateTime fromBytes(FriendlyByteBuf byteBuf) {
		return new SerializableDateTime(
		byteBuf.readInt(),
		byteBuf.readInt(),
		byteBuf.readInt(),
		byteBuf.readInt(),
		byteBuf.readInt(),
		byteBuf.readInt(),
		byteBuf.readInt(),
		byteBuf.readUtf()
		);
	}
}
