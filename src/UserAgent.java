public class UserAgent {
    private final String osType;
    private final String browser;

    public UserAgent(String userAgentString) {
        this.osType = parseOsType(userAgentString);
        this.browser = parseBrowser(userAgentString);
    }

    private String parseOsType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) return "Unknown";

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("windows")) return "Windows";
        if (userAgent.contains("macintosh") || userAgent.contains("mac os x")) return "macOS";
        if (userAgent.contains("linux")) return "Linux";
        if (userAgent.contains("android")) return "Android";
        if (userAgent.contains("iphone") || userAgent.contains("ipad")) return "iOS";

        return "Unknown";
    }

    private String parseBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) return "Unknown";

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("edg/") || userAgent.contains("edge")) return "Edge";
        if (userAgent.contains("firefox")) return "Firefox";
        if (userAgent.contains("chrome")) return "Chrome";
        if (userAgent.contains("safari") && !userAgent.contains("chrome")) return "Safari";
        if (userAgent.contains("opera") || userAgent.contains("opr/")) return "Opera";

        return "Other";
    }

    public String getOsType() {
        return osType;
    }

    public String getBrowser() {
        return browser;
    }
}