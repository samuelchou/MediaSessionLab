package studio.stc.mediasessionlab.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;

import studio.stc.mediasessionlab.R;

/**
 * NotificationUtil powered by STC
 * 這是用來處理Notification的函式集，提供一些快速建構通知的函式。
 * 創建日期：2018/12
 * 聯絡我： stc.ntu@gmail.com
 */
public final class NotificationUtil {

    private final static String TAG = "NotificationUtil";

    private static ArrayList<NotificationChannel> channels = new ArrayList<>();

    private final static String EXAMPLE_CHANNEL_ID = "thatChannel";
    private final static String EXAMPLE_CHANNEL_NAME = "That Channel";
    private final static int EXAMPLE_PENDING_INTENT_ID = 1308;
    private final static int EXAMPLE_NOTIFICATION_ID = 2120;
    private final static int EXAMPLE_NOTIFICATION_PICTURE_RESOURCE = R.mipmap.ic_launcher_round;

    /**
     * 建立一個樣本通知。
     * 注意：該方法只是用來示範如何使用。
     *
     * @param context          上下文。
     * @param openThisActivity 點擊通知要打開的Activity。
     */
    static void CreateExampleNotification(Context context, Class<?> openThisActivity) {
        NotificationCompat.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = getNotificationChannel(context, EXAMPLE_CHANNEL_ID, EXAMPLE_CHANNEL_NAME);
            builder = new NotificationCompat.Builder(context, channel.getId());
        } else {
            builder = new NotificationCompat.Builder(context).setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }
        //設定動作為開啟指定活動
        PendingIntent action = createPendingIntent(IntentOpenType.START_ACTIVITY, context, openThisActivity, EXAMPLE_PENDING_INTENT_ID);
        //設定通知內容
        builder.setSmallIcon(EXAMPLE_NOTIFICATION_PICTURE_RESOURCE)
                .setContentTitle("Hello")
                .setContentText("This is an notification.")
                .setContentIntent(action)
                .setAutoCancel(true); //在點選通知後會取消該通知
        SendNotification(context, builder, EXAMPLE_NOTIFICATION_ID);
    }

    /**
     * 在Android 8.0 / API 26 以後出現的新東西，會使你的通知在App Info中進行分類。一般來說只要一個頻道即可。預設重要性為Default(提示聲音)
     * 該方法會嘗試尋找這個頻道；如果沒有找到，則創建一個。
     * See: https://developer.android.com/training/notify-user/channels
     *
     * @param context     上下文。用來進行資源獲取。
     * @param channelID   頻道的ID。必須為單一存在、不可重複的頻道名稱。ID並不會顯示給使用者、然而通知(在8.0以後)必須被指派頻道。建議使用final static的某一String。
     * @param channelName 頻道的名稱。會顯示在使用者App Info中。
     * @return 找到或創建的頻道。
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getNotificationChannel(Context context, String channelID, String channelName) {
        return getNotificationChannel(context, channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
    }

    /**
     * 在Android 8.0 / API 26 以後出現的新東西，會使你的通知在App Info中進行分類。一般來說只要一個頻道即可。
     * 該方法會嘗試尋找這個頻道；如果沒有找到，則創建一個。
     * See: https://developer.android.com/training/notify-user/channels
     *
     * @param context     上下文。用來進行資源獲取。
     * @param channelID   頻道的ID。必須為單一存在、不可重複的頻道名稱。ID並不會顯示給使用者、然而通知(在8.0以後)必須被指派頻道。建議使用final static的某一String。
     * @param channelName 頻道的名稱。會顯示在使用者App Info中。
     * @param importance  設定重要性。請使用NotificationManager.IMPORTANCE_...系列。
     * @return 找到或創建的頻道。
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getNotificationChannel(Context context, String channelID, String channelName, int importance) {
        NotificationChannel channel = getChannel(channelID);
        if (channel == null) {
            return createNotificationChannel(context, channelID, channelName, importance);
        } else return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static NotificationChannel createNotificationChannel(Context context, String channelID, String channelName, int importance) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelID, channelName, importance);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
        channels.add(channel);
        return channel;
    }

    /**
     * 獲取現存的channel。
     *
     * @param channelID 搜尋現存channel中符合該ID的值。
     * @return 現存channel的指定項。如果找不到，將回傳null。
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    static NotificationChannel getChannel(String channelID) {
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).getId().equals(channelID)) {
                return channels.get(i);
            }
        }
        return null;
    }

    /**
     * 獲取現有channel的數量。
     *
     * @return 已經創建的channel數。
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    static int getChannelsCount() {
        return channels.size();
    }

    /**
     * 獲取現存的channel。
     *
     * @param index 索引值。
     * @return 現存channel的指定項。如果找不到，將回傳null。
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    static NotificationChannel getChannel(int index) {
        return channels.size() <= index ? null : channels.get(index);
    }

    /**
     * 獲取現存的channel中第一個channel。
     *
     * @return 現存channel的指定項。如果找不到，將回傳null。
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    static NotificationChannel getDefaultChannel() {
        if (channels.size() == 0) {
            Log.e(TAG, "getDefaultChannel: you didn't create any channel!!!!");
            return null;
        } else {
            return channels.get(0);
        }
    }

    /**
     * 創建PendingIntent。最主要是用來定義通知的互動（例如點擊通知後開啟特定頁面）。預設是Update現有內容。
     *
     * @param actionType 定義動作格式。
     * @param context    上下文。（建議使用）
     * @param openThis   開啟的項目。例如MainActivity.class。
     * @param uniqueID   針對該PendingIntent生成的獨一ID，用以確保該PI不會與專案內其他PI重複。
     * @return 一個PendingIntent，你可以把它塞進Notification Builder的Content Intent裡面。
     */
    public static PendingIntent createPendingIntent(IntentOpenType actionType, Context context, Class<?> openThis, int uniqueID) {
        return createPendingIntent(actionType, context, openThis, uniqueID, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 創建PendingIntent。最主要是用來定義通知的互動（例如點擊通知後開啟特定頁面）。預設是Update現有意圖。
     *
     * @param actionType 定義動作格式。
     * @param context    上下文。（建議使用）
     * @param intent     想要做到的意圖。
     * @param uniqueID   針對該PendingIntent生成的獨一ID，用以確保該PI不會與專案內其他PI重複。
     * @return 一個PendingIntent，你可以把它塞進Notification Builder的Content Intent裡面。
     */
    public static PendingIntent createPendingIntent(IntentOpenType actionType, Context context, Intent intent, int uniqueID) {
        return createPendingIntent(actionType, context, intent, uniqueID, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 創建PendingIntent。最主要是用來定義通知的互動（例如點擊通知後開啟特定頁面）。
     *
     * @param actionType 定義動作格式。
     * @param context    上下文。（建議使用）
     * @param openThis   開啟的項目。例如MainActivity.class。
     * @param uniqueID   針對該PendingIntent生成的獨一ID，用以確保該PI不會與專案內其他PI重複。
     * @param updateFlag 設定Pending Intent的更新方式。請使用PendingIntent.FLAG_...參數。
     * @return 一個PendingIntent，你可以把它塞進Notification Builder的Content Intent裡面。
     */
    static PendingIntent createPendingIntent(IntentOpenType actionType, Context context, Class<?> openThis, int uniqueID, int updateFlag) {
        Intent intent = new Intent(context, openThis);
        return createPendingIntent(actionType, context, intent, uniqueID, updateFlag);
    }

    /**
     * 創建PendingIntent。最主要是用來定義通知的互動（例如點擊通知後開啟特定頁面）。
     *
     * @param actionType 定義動作格式。
     * @param context    上下文。（建議使用）
     * @param intent     想要做到的意圖。
     * @param uniqueID   針對該PendingIntent生成的獨一ID，用以確保該PI不會與專案內其他PI重複。
     * @param updateFlag 設定Pending Intent的更新方式。請使用PendingIntent.FLAG_...參數。
     * @return 一個PendingIntent，你可以把它塞進Notification Builder的Content Intent裡面。
     */
    static PendingIntent createPendingIntent(IntentOpenType actionType, Context context, Intent intent, int uniqueID, int updateFlag) {
        PendingIntent pendingIntent = null;
        switch (actionType) {
            case START_ACTIVITY:
                pendingIntent = PendingIntent.getActivity(context, uniqueID, intent, updateFlag);
                break;
            case START_SERVICE:
                pendingIntent = PendingIntent.getService(context, uniqueID, intent, updateFlag);
                break;
            case SEND_BROADCAST:
                pendingIntent = PendingIntent.getBroadcast(context, uniqueID, intent, updateFlag);
                break;
            case START_FOREGROUND:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    Log.e(TAG, "createPendingIntent: your Android version is too old: START_FOREGROUND is unable to use. Will do nothing.");
                    return null;
                } else {
                    pendingIntent = PendingIntent.getForegroundService(context, uniqueID, intent, updateFlag);
                }
                break;
            default:
                Log.e(TAG, "createPendingIntent: TAKING UN-DEFINED ACTION!!! WILL DO NOTHING.");
        }
        return pendingIntent;
    }

    public enum IntentOpenType {
        START_ACTIVITY, START_SERVICE, SEND_BROADCAST, START_FOREGROUND
    }

    /**
     * 創建PendingIntent，並且內含一個攜帶bundle資訊的Activity。預設是Update現有內容。
     *
     * @param context  上下文。
     * @param openThis 要開啟的Activity class。
     * @param bundle   要夾帶的Bundle資訊。
     * @param uniqueID 針對該PendingIntent生成的獨一ID，用以確保該PI不會與專案內其他PI重複。
     * @return 一個PendingIntent，你可以把它塞進Notification Builder的Content Intent裡面。
     */
    static PendingIntent createBundledActivityPendingIntent(Context context, Class<?> openThis, Bundle bundle, int uniqueID) {
        return PendingIntent.getActivity(context, uniqueID, new Intent(context, openThis), PendingIntent.FLAG_UPDATE_CURRENT, bundle);
    }

    /**
     * 創建PendingIntent，並且內含一個攜帶bundle資訊的Activity。
     *
     * @param context    上下文。
     * @param openThis   要開啟的Activity class。
     * @param bundle     要夾帶的Bundle資訊。
     * @param uniqueID   針對該PendingIntent生成的獨一ID，用以確保該PI不會與專案內其他PI重複。
     * @param updateFlag 設定Pending Intent的更新方式。請使用PendingIntent.FLAG_...參數。
     * @return 一個PendingIntent，你可以把它塞進Notification Builder的Content Intent裡面。
     */
    static PendingIntent createBundledActivityPendingIntent(Context context, Class<?> openThis, Bundle bundle, int uniqueID, int updateFlag) {
        return PendingIntent.getActivity(context, uniqueID, new Intent(context, openThis), updateFlag, bundle);
    }

    /**
     * 創建PendingIntent，並且內含數個Activity們。
     *
     * @param context    上下文。
     * @param openThese  要開啟的Activity class們。
     * @param uniqueID   針對該PendingIntent生成的獨一ID，用以確保該PI不會與專案內其他PI重複。
     * @param updateFlag 設定Pending Intent的更新方式。請使用PendingIntent.FLAG_...參數。
     * @return 一個PendingIntent，你可以把它塞進Notification Builder的Content Intent裡面。
     */
    static PendingIntent createActivitiesPendingIntent(Context context, Class<?>[] openThese, int uniqueID, int updateFlag) {
        Intent[] intents = new Intent[openThese.length];
        for (int i = 0; i < intents.length; i++) intents[i] = new Intent(context, openThese[i]);
        return PendingIntent.getActivities(context, uniqueID, intents, updateFlag);
    }

    /**
     * 創建PendingIntent，並且內含數個攜帶bundle資訊的Activity們。
     *
     * @param context    上下文。
     * @param openThese  要開啟的Activity class們。
     * @param bundle     要夾帶的Bundle資訊。
     * @param uniqueID   針對該PendingIntent生成的獨一ID，用以確保該PI不會與專案內其他PI重複。
     * @param updateFlag 設定Pending Intent的更新方式。請使用PendingIntent.FLAG_...參數。
     * @return 一個PendingIntent，你可以把它塞進Notification Builder的Content Intent裡面。
     */
    static PendingIntent createBundledActivitiesPendingIntent(Context context, Class<?>[] openThese, Bundle bundle, int uniqueID, int updateFlag) {
        Intent[] intents = new Intent[openThese.length];
        for (int i = 0; i < intents.length; i++) intents[i] = new Intent(context, openThese[i]);
        return PendingIntent.getActivities(context, uniqueID, intents, updateFlag, bundle);
    }

    /**
     * 更新Builder內的View。
     *
     * @param builder     要更新的builder。
     * @param remoteViews 新的remoteView。
     * @return 回傳該builder。一般來說不用重新指派回去（builder會自行更新）
     */
    public static NotificationCompat.Builder UpdateContentView(NotificationCompat.Builder builder, RemoteViews remoteViews) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//            Log.e(TAG, "UpdateContentView: BUILD with content.");
            builder.setContent(remoteViews);
        } else {
//            Log.e(TAG, "UpdateContentView: BUILD with custom content.");
            builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(remoteViews);
        }
        return builder;
    }


    /**
     * 送出通知。
     *
     * @param context        上下文。
     * @param builder        建構Notification的 builder。
     * @param notificationID 該通知的ID。如果同個ID的通知已經存在，新通知會把它覆蓋掉。
     */
    public static void SendNotification(Context context, NotificationCompat.Builder builder, int notificationID) {
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (channels.size() == 0) {
                Log.e(TAG, "SendNotification: you didn't create notification channel before sending notification -- it is not recommended in Oreo / API 26 or above. Will do nothing.");
                return;
            } else if (notification.getChannelId().equals("")) {
                Log.e(TAG, "SendNotification: channel ID not assigned. Will be assigned with first created channel.");
                notification = builder.setChannelId(channels.get(0).getId()).build();
            }
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(notificationID, notification);
    }

    /**
     * 送出通知。如果相同ID的通知已經存在，則會更新。
     *
     * @param context        上下文。
     * @param notification   建構Notification的 builder。
     * @param notificationID 該通知的ID。如果同個ID的通知已經存在，新通知會把它覆蓋掉。
     */
    public static void SendNotification(Context context, Notification notification, int notificationID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (channels.size() == 0) {
                Log.e(TAG, "SendNotification: you didn't create notification channel before sending notification -- it is not allowed in Oreo / API 26 or above. Will do nothing.");
                return;
            } else if (notification.getChannelId().equals("")) {
                Log.e(TAG, "SendNotification: channel ID not assigned. Won't send.");
                return;
            }
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(notificationID, notification);
    }

    /**
     * 如字面所說，清除所有通知。
     *
     * @param context 上下文。
     */
    static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancelAll();
    }

    /**
     * 清除指定的通知（如果存在）。
     *
     * @param context        上下文。
     * @param notificationID 該通知的ID。
     */
    static void CancelSingleNotification(Context context, int notificationID) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(notificationID);
    }

    /**
     * 清除指定的通知（如果存在）。
     *
     * @param context         上下文。
     * @param notificationTag 該通知的Tag。 //TODO:（不確定是要幹嘛）
     * @param notificationID  該通知的ID。
     */
    static void CancelSingleNotification(Context context, String notificationTag, int notificationID) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(notificationTag, notificationID);
    }

}
