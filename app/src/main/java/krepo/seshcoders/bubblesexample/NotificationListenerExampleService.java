package krepo.seshcoders.bubblesexample;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationListenerExampleService extends NotificationListenerService {
    private static final String TAG = "NotificationListenerExa";
    public static final String IGNORE_TITLE = "Dymki czatu są aktywne";

    private static final class ApplicationPackageNames {
        public static final String FACEBOOK_PACK_NAME = "com.facebook.katana";
        public static final String FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca";
        public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
        public static final String INSTAGRAM_PACK_NAME = "com.instagram.android";
    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    public static final class InterceptedNotificationCode {
        public static final int FACEBOOK_CODE = 1;
        public static final int WHATSAPP_CODE = 2;
        public static final int INSTAGRAM_CODE = 3;
        public static final int OTHER_NOTIFICATIONS_CODE = 4; // We ignore all notification with code == 4
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);
        Bundle bundle = sbn.getNotification().extras;
        Log.d(TAG, "onNotificationPosted: TEXT: "+bundle.getString(Notification.EXTRA_TEXT)
                +"\n TITLE: "+bundle.getString(Notification.EXTRA_TITLE)
                +"\n TEXT_BIG: "+bundle.getString(Notification.EXTRA_BIG_TEXT));

        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE){
            Intent intent = new  Intent("krepo.seshcoders.notificationlistenerexample");
            intent.putExtra("Notification Code", notificationCode);
            sendBroadcast(intent);
        }
    }



//    @Override
//    public void onNotificationRemoved(StatusBarNotification sbn){
//        int notificationCode = matchNotificationCode(sbn);
//
//        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {
//
//            StatusBarNotification[] activeNotifications = this.getActiveNotifications();
//
//            if(activeNotifications != null && activeNotifications.length > 0) {
//                for (int i = 0; i < activeNotifications.length; i++) {
//                    if (notificationCode == matchNotificationCode(activeNotifications[i])) {
//                        Intent intent = new  Intent("krepo.seshcoders.notificationlistenerexample");
//                        intent.putExtra("Notification Code", notificationCode);
//                        sendBroadcast(intent);
//                        break;
//                    }
//                }
//            }
//        }
//    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        if(packageName.equals(ApplicationPackageNames.FACEBOOK_PACK_NAME)
                || packageName.equals(ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME)){
            return(InterceptedNotificationCode.FACEBOOK_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.INSTAGRAM_PACK_NAME)){
            return(InterceptedNotificationCode.INSTAGRAM_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.WHATSAPP_PACK_NAME)){
            return(InterceptedNotificationCode.WHATSAPP_CODE);
        }
        else{
            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }
}
