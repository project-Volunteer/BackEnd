package project.volunteer.global.common.component;

public enum LogboardSearchType {
	all,mylog;
	
	public static boolean isAll(String paramSearchType) {
		return LogboardSearchType.all.name().equals(paramSearchType);
	}
	
	public static boolean isMylog(String paramSearchType) {
		return LogboardSearchType.mylog.name().equals(paramSearchType);
	}
}
