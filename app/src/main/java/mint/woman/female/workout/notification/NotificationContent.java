package mint.woman.female.workout.notification;


public class NotificationContent {
    private final NotificationType type;
    private final String title;
    private final String message;
    private final String deepLink;

    public NotificationContent(NotificationType type, String title, String message, String deepLink) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.deepLink = deepLink;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getDeepLink() {
        return deepLink;
    }
}