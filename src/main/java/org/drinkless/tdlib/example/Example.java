package org.drinkless.tdlib.example;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.Client.ExceptionHandler;
import org.drinkless.tdlib.Client.ResultHandler;
import org.drinkless.tdlib.TdApi.AuthorizationState;
import org.drinkless.tdlib.TdApi.AuthorizationStateWaitOtherDeviceConfirmation;
import org.drinkless.tdlib.TdApi.BasicGroup;
import org.drinkless.tdlib.TdApi.BasicGroupFullInfo;
import org.drinkless.tdlib.TdApi.Chat;
import org.drinkless.tdlib.TdApi.ChatListMain;
import org.drinkless.tdlib.TdApi.Chats;
import org.drinkless.tdlib.TdApi.CheckAuthenticationCode;
import org.drinkless.tdlib.TdApi.CheckAuthenticationPassword;
import org.drinkless.tdlib.TdApi.CheckDatabaseEncryptionKey;
import org.drinkless.tdlib.TdApi.Close;
import org.drinkless.tdlib.TdApi.Error;
import org.drinkless.tdlib.TdApi.FormattedText;
import org.drinkless.tdlib.TdApi.GetChat;
import org.drinkless.tdlib.TdApi.GetChats;
import org.drinkless.tdlib.TdApi.GetMe;
import org.drinkless.tdlib.TdApi.GetTextEntities;
import org.drinkless.tdlib.TdApi.InlineKeyboardButton;
import org.drinkless.tdlib.TdApi.InlineKeyboardButtonTypeUrl;
import org.drinkless.tdlib.TdApi.InputMessageText;
import org.drinkless.tdlib.TdApi.LogOut;
import org.drinkless.tdlib.TdApi.LogStreamFile;
import org.drinkless.tdlib.TdApi.Object;
import org.drinkless.tdlib.TdApi.PhoneNumberAuthenticationSettings;
import org.drinkless.tdlib.TdApi.RegisterUser;
import org.drinkless.tdlib.TdApi.ReplyMarkupInlineKeyboard;
import org.drinkless.tdlib.TdApi.SecretChat;
import org.drinkless.tdlib.TdApi.SendMessage;
import org.drinkless.tdlib.TdApi.SendMessageOptions;
import org.drinkless.tdlib.TdApi.SetAuthenticationPhoneNumber;
import org.drinkless.tdlib.TdApi.SetLogStream;
import org.drinkless.tdlib.TdApi.SetLogVerbosityLevel;
import org.drinkless.tdlib.TdApi.SetTdlibParameters;
import org.drinkless.tdlib.TdApi.Supergroup;
import org.drinkless.tdlib.TdApi.SupergroupFullInfo;
import org.drinkless.tdlib.TdApi.TdlibParameters;
import org.drinkless.tdlib.TdApi.TextEntity;
import org.drinkless.tdlib.TdApi.UpdateAuthorizationState;
import org.drinkless.tdlib.TdApi.UpdateBasicGroup;
import org.drinkless.tdlib.TdApi.UpdateBasicGroupFullInfo;
import org.drinkless.tdlib.TdApi.UpdateChatChatList;
import org.drinkless.tdlib.TdApi.UpdateChatDefaultDisableNotification;
import org.drinkless.tdlib.TdApi.UpdateChatDraftMessage;
import org.drinkless.tdlib.TdApi.UpdateChatIsMarkedAsUnread;
import org.drinkless.tdlib.TdApi.UpdateChatIsPinned;
import org.drinkless.tdlib.TdApi.UpdateChatIsSponsored;
import org.drinkless.tdlib.TdApi.UpdateChatLastMessage;
import org.drinkless.tdlib.TdApi.UpdateChatNotificationSettings;
import org.drinkless.tdlib.TdApi.UpdateChatOrder;
import org.drinkless.tdlib.TdApi.UpdateChatPhoto;
import org.drinkless.tdlib.TdApi.UpdateChatReadInbox;
import org.drinkless.tdlib.TdApi.UpdateChatReadOutbox;
import org.drinkless.tdlib.TdApi.UpdateChatReplyMarkup;
import org.drinkless.tdlib.TdApi.UpdateChatTitle;
import org.drinkless.tdlib.TdApi.UpdateChatUnreadMentionCount;
import org.drinkless.tdlib.TdApi.UpdateMessageMentionRead;
import org.drinkless.tdlib.TdApi.UpdateNewChat;
import org.drinkless.tdlib.TdApi.UpdateSecretChat;
import org.drinkless.tdlib.TdApi.UpdateSupergroup;
import org.drinkless.tdlib.TdApi.UpdateSupergroupFullInfo;
import org.drinkless.tdlib.TdApi.UpdateUser;
import org.drinkless.tdlib.TdApi.UpdateUserFullInfo;
import org.drinkless.tdlib.TdApi.UpdateUserStatus;
import org.drinkless.tdlib.TdApi.User;
import org.drinkless.tdlib.TdApi.UserFullInfo;

public final class Example {
    private static Client client = null;
    private static AuthorizationState authorizationState = null;
    private static volatile boolean haveAuthorization = false;
    private static volatile boolean quiting = false;
    private static final ResultHandler defaultHandler = new Example.DefaultHandler();
    private static final Lock authorizationLock = new ReentrantLock();
    private static final Condition gotAuthorization;
    private static final ConcurrentMap<Integer, User> users;
    private static final ConcurrentMap<Integer, BasicGroup> basicGroups;
    private static final ConcurrentMap<Integer, Supergroup> supergroups;
    private static final ConcurrentMap<Integer, SecretChat> secretChats;
    private static final ConcurrentMap<Long, Chat> chats;
    private static final NavigableSet<Example.OrderedChat> mainChatList;
    private static boolean haveFullMainChatList;
    private static final ConcurrentMap<Integer, UserFullInfo> usersFullInfo;
    private static final ConcurrentMap<Integer, BasicGroupFullInfo> basicGroupsFullInfo;
    private static final ConcurrentMap<Integer, SupergroupFullInfo> supergroupsFullInfo;
    private static final String newLine;
    private static final String commandsLine = "Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): ";
    private static volatile String currentPrompt;

    public Example() {
    }

    private static void print(String var0) {
        if (currentPrompt != null) {
            System.out.println("");
        }

        System.out.println(var0);
        if (currentPrompt != null) {
            System.out.print(currentPrompt);
        }

    }

    private static void setChatOrder(Chat var0, long var1) {
        synchronized(mainChatList) {
            synchronized(var0) {
                if (var0.chatList != null && var0.chatList.getConstructor() == -400991316) {
                    boolean var5;
                    if (var0.order != 0L) {
                        var5 = mainChatList.remove(new Example.OrderedChat(var0.order, var0.id));

                        assert var5;
                    }

                    var0.order = var1;
                    if (var0.order != 0L) {
                        var5 = mainChatList.add(new Example.OrderedChat(var0.order, var0.id));

                        assert var5;
                    }

                    return;
                }
            }

        }
    }

    private static void onAuthorizationStateUpdated(AuthorizationState var0) {
        if (var0 != null) {
            authorizationState = var0;
        }

        String var2;
        switch(authorizationState.getConstructor()) {
            case -1834871737:
                haveAuthorization = true;
                authorizationLock.lock();

                try {
                    gotAuthorization.signal();
                    break;
                } finally {
                    authorizationLock.unlock();
                }
            case 52643073:
                var2 = promptString("Please enter authentication code: ");
                client.send(new CheckAuthenticationCode(var2), new Example.AuthorizationRequestHandler());
                break;
            case 154449270:
                haveAuthorization = false;
                print("Logging out");
                break;
            case 187548796:
                var2 = promptString("Please enter password: ");
                client.send(new CheckAuthenticationPassword(var2), new Example.AuthorizationRequestHandler());
                break;
            case 306402531:
                var2 = promptString("Please enter phone number: ");
                client.send(new SetAuthenticationPhoneNumber(var2, (PhoneNumberAuthenticationSettings)null), new Example.AuthorizationRequestHandler());
                break;
            case 445855311:
                haveAuthorization = false;
                print("Closing");
                break;
            case 550350511:
                var2 = promptString("Please enter your first name: ");
                String var3 = promptString("Please enter your last name: ");
                client.send(new RegisterUser(var2, var3), new Example.AuthorizationRequestHandler());
                break;
            case 612103496:
                client.send(new CheckDatabaseEncryptionKey(), new Example.AuthorizationRequestHandler());
                break;
            case 860166378:
                var2 = ((AuthorizationStateWaitOtherDeviceConfirmation)authorizationState).link;
                System.out.println("Please confirm this login link on another device: " + var2);
                break;
            case 904720988:
                TdlibParameters var1 = new TdlibParameters();
                var1.databaseDirectory = "tdlib";
                var1.useMessageDatabase = true;
                var1.useSecretChats = true;
                var1.apiId = 94575;
                var1.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2";
                var1.systemLanguageCode = "en";
                var1.deviceModel = "Desktop";
                var1.systemVersion = "Unknown";
                var1.applicationVersion = "1.0";
                var1.enableStorageOptimizer = true;
                client.send(new SetTdlibParameters(var1), new Example.AuthorizationRequestHandler());
                break;
            case 1526047584:
                print("Closed");
                if (!quiting) {
                    client = Client.create(new Example.UpdatesHandler(), (ExceptionHandler)null, (ExceptionHandler)null);
                }
                break;
            default:
                System.err.println("Unsupported authorization state:" + newLine + authorizationState);
        }

    }

    private static int toInt(String var0) {
        int var1 = 0;

        try {
            var1 = Integer.parseInt(var0);
        } catch (NumberFormatException var3) {
        }

        return var1;
    }

    private static long getChatId(String var0) {
        long var1 = 0L;

        try {
            var1 = Long.parseLong(var0);
        } catch (NumberFormatException var4) {
        }

        return var1;
    }

    private static String promptString(String var0) {
        System.out.print(var0);
        currentPrompt = var0;
        BufferedReader var1 = new BufferedReader(new InputStreamReader(System.in));
        String var2 = "";

        try {
            var2 = var1.readLine();
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        currentPrompt = null;
        return var2;
    }

    private static void getCommand() {
        String var0 = promptString("Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): ");
        String[] var1 = var0.split(" ", 2);

        try {
            String var2 = var1[0];
            byte var3 = -1;
            switch(var2.hashCode()) {
                case 113:
                    if (var2.equals("q")) {
                        var3 = 5;
                    }
                    break;
                case 3292:
                    if (var2.equals("gc")) {
                        var3 = 1;
                    }
                    break;
                case 3459:
                    if (var2.equals("lo")) {
                        var3 = 4;
                    }
                    break;
                case 3480:
                    if (var2.equals("me")) {
                        var3 = 2;
                    }
                    break;
                case 3674:
                    if (var2.equals("sm")) {
                        var3 = 3;
                    }
                    break;
                case 102167:
                    if (var2.equals("gcs")) {
                        var3 = 0;
                    }
            }

            switch(var3) {
                case 0:
                    int var6 = 20;
                    if (var1.length > 1) {
                        var6 = toInt(var1[1]);
                    }

                    getMainChatList(var6);
                    break;
                case 1:
                    client.send(new GetChat(getChatId(var1[1])), defaultHandler);
                    break;
                case 2:
                    client.send(new GetMe(), defaultHandler);
                    break;
                case 3:
                    String[] var4 = var1[1].split(" ", 2);
                    sendMessage(getChatId(var4[0]), var4[1]);
                    break;
                case 4:
                    haveAuthorization = false;
                    client.send(new LogOut(), defaultHandler);
                    break;
                case 5:
                    quiting = true;
                    haveAuthorization = false;
                    client.send(new Close(), defaultHandler);
                    break;
                default:
                    System.err.println("Unsupported command: " + var0);
            }
        } catch (ArrayIndexOutOfBoundsException var5) {
            print("Not enough arguments");
        }

    }

    private static void getMainChatList(final int var0) {
        synchronized(mainChatList) {
            long var4;
            if (!haveFullMainChatList && var0 > mainChatList.size()) {
                long var13 = 9223372036854775807L;
                var4 = 0L;
                if (!mainChatList.isEmpty()) {
                    Example.OrderedChat var12 = (Example.OrderedChat)mainChatList.last();
                    var13 = var12.order;
                    var4 = var12.chatId;
                }

                client.send(new GetChats(new ChatListMain(), var13, var4, var0 - mainChatList.size()), new ResultHandler() {
                    public void onResult(Object var1) {
                        switch(var1.getConstructor()) {
                            case -1687756019:
                                long[] var2 = ((Chats)var1).chatIds;
                                if (var2.length == 0) {
                                    synchronized(Example.mainChatList) {
                                        Example.haveFullMainChatList = true;
                                    }
                                }

                                Example.getMainChatList(var0);
                                break;
                            case -1679978726:
                                System.err.println("Receive an error for GetChats:" + Example.newLine + var1);
                                break;
                            default:
                                System.err.println("Receive wrong response from TDLib:" + Example.newLine + var1);
                        }

                    }
                });
            } else {
                Iterator var2 = mainChatList.iterator();
                System.out.println();
                System.out.println("First " + var0 + " chat(s) out of " + mainChatList.size() + " known chat(s):");

                for(int var3 = 0; var3 < var0; ++var3) {
                    var4 = ((Example.OrderedChat)var2.next()).chatId;
                    Chat var6 = (Chat)chats.get(var4);
                    synchronized(var6) {
                        System.out.println(var4 + ": " + var6.title);
                    }
                }

                print("");
            }
        }
    }

    private static void sendMessage(long var0, String var2) {
        InlineKeyboardButton[] var3 = new InlineKeyboardButton[]{new InlineKeyboardButton("https://telegram.org?1", new InlineKeyboardButtonTypeUrl()), new InlineKeyboardButton("https://telegram.org?2", new InlineKeyboardButtonTypeUrl()), new InlineKeyboardButton("https://telegram.org?3", new InlineKeyboardButtonTypeUrl())};
        ReplyMarkupInlineKeyboard var4 = new ReplyMarkupInlineKeyboard(new InlineKeyboardButton[][]{var3, var3, var3});
        InputMessageText var5 = new InputMessageText(new FormattedText(var2, (TextEntity[])null), false, true);
        client.send(new SendMessage(var0, 0L, (SendMessageOptions)null, var4, var5), defaultHandler);
    }

    public static void main(String[] var0) throws InterruptedException {
        Client.execute(new SetLogVerbosityLevel(0));
        if (Client.execute(new SetLogStream(new LogStreamFile("tdlib.log", 134217728L))) instanceof Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        } else {
            client = Client.create(new Example.UpdatesHandler(), (ExceptionHandler)null, (ExceptionHandler)null);
            defaultHandler.onResult(Client.execute(new GetTextEntities("@telegram /test_command https://telegram.org telegram.me @gif @test")));

            while(!quiting) {
                authorizationLock.lock();

                try {
                    while(!haveAuthorization) {
                        gotAuthorization.await();
                    }
                } finally {
                    authorizationLock.unlock();
                }

                while(haveAuthorization) {
                    getCommand();
                }
            }

        }
    }

    static {
        gotAuthorization = authorizationLock.newCondition();
        users = new ConcurrentHashMap();
        basicGroups = new ConcurrentHashMap();
        supergroups = new ConcurrentHashMap();
        secretChats = new ConcurrentHashMap();
        chats = new ConcurrentHashMap();
        mainChatList = new TreeSet();
        haveFullMainChatList = false;
        usersFullInfo = new ConcurrentHashMap();
        basicGroupsFullInfo = new ConcurrentHashMap();
        supergroupsFullInfo = new ConcurrentHashMap();
        newLine = System.getProperty("line.separator");
        currentPrompt = null;

        try {
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError var1) {
            var1.printStackTrace();
        }

    }

    private static class AuthorizationRequestHandler implements ResultHandler {
        private AuthorizationRequestHandler() {
        }

        public void onResult(Object var1) {
            switch(var1.getConstructor()) {
                case -1679978726:
                    System.err.println("Receive an error:" + Example.newLine + var1);
                    Example.onAuthorizationStateUpdated((AuthorizationState)null);
                case -722616727:
                    break;
                default:
                    System.err.println("Receive wrong response from TDLib:" + Example.newLine + var1);
            }

        }
    }

    private static class UpdatesHandler implements ResultHandler {
        private UpdatesHandler() {
        }

        public void onResult(Object var1) {
            Chat var7;
            switch(var1.getConstructor()) {
                case -2131461348:
                    UpdateChatUnreadMentionCount var67 = (UpdateChatUnreadMentionCount)var1;
                    var7 = (Chat)Example.chats.get(var67.chatId);
                    synchronized(var7) {
                        var7.unreadMentionCount = var67.unreadMentionCount;
                        break;
                    }
                case -1666903253:
                    UpdateSecretChat var5 = (UpdateSecretChat)var1;
                    Example.secretChats.put(var5.secretChat.id, var5.secretChat);
                    break;
                case -1601888026:
                    UpdateChatOrder var66 = (UpdateChatOrder)var1;
                    var7 = (Chat)Example.chats.get(var66.chatId);
                    synchronized(var7) {
                        Example.setChatOrder(var7, var66.order);
                        break;
                    }
                case -1443545195:
                    UpdateUserStatus var48 = (UpdateUserStatus)var1;
                    User var49 = (User)Example.users.get(var48.userId);
                    synchronized(var49) {
                        var49.status = var48.status;
                        break;
                    }
                case -1436617498:
                    UpdateChatDraftMessage var65 = (UpdateChatDraftMessage)var1;
                    var7 = (Chat)Example.chats.get(var65.chatId);
                    synchronized(var7) {
                        var7.draftMessage = var65.draftMessage;
                        Example.setChatOrder(var7, var65.order);
                        break;
                    }
                case -1196180070:
                    UpdateChatIsSponsored var64 = (UpdateChatIsSponsored)var1;
                    var7 = (Chat)Example.chats.get(var64.chatId);
                    synchronized(var7) {
                        var7.isSponsored = var64.isSponsored;
                        Example.setChatOrder(var7, var64.order);
                        break;
                    }
                case -1003239581:
                    UpdateBasicGroup var3 = (UpdateBasicGroup)var1;
                    Example.basicGroups.put(var3.basicGroup.id, var3.basicGroup);
                    break;
                case -803163050:
                    UpdateChatNotificationSettings var63 = (UpdateChatNotificationSettings)var1;
                    var7 = (Chat)Example.chats.get(var63.chatId);
                    synchronized(var7) {
                        var7.notificationSettings = var63.notificationSettings;
                        break;
                    }
                case -797952281:
                    UpdateChatReadInbox var62 = (UpdateChatReadInbox)var1;
                    var7 = (Chat)Example.chats.get(var62.chatId);
                    synchronized(var7) {
                        var7.lastReadInboxMessageId = var62.lastReadInboxMessageId;
                        var7.unreadCount = var62.unreadCount;
                        break;
                    }
                case -252228282:
                    UpdateMessageMentionRead var61 = (UpdateMessageMentionRead)var1;
                    var7 = (Chat)Example.chats.get(var61.chatId);
                    synchronized(var7) {
                        var7.unreadMentionCount = var61.unreadMentionCount;
                        break;
                    }
                case -209353966:
                    UpdateChatPhoto var60 = (UpdateChatPhoto)var1;
                    var7 = (Chat)Example.chats.get(var60.chatId);
                    synchronized(var7) {
                        var7.photo = var60.photo;
                        break;
                    }
                case -175405660:
                    UpdateChatTitle var59 = (UpdateChatTitle)var1;
                    var7 = (Chat)Example.chats.get(var59.chatId);
                    synchronized(var7) {
                        var7.title = var59.title;
                        break;
                    }
                case -170455894:
                    UpdateChatChatList var58 = (UpdateChatChatList)var1;
                    var7 = (Chat)Example.chats.get(var58.chatId);
                    synchronized(Example.mainChatList) {
                        synchronized(var7) {
                            assert var7.order == 0L;

                            var7.chatList = var58.chatList;
                            break;
                        }
                    }
                case -76782300:
                    UpdateSupergroup var4 = (UpdateSupergroup)var1;
                    Example.supergroups.put(var4.supergroup.id, var4.supergroup);
                    break;
                case 222103874:
                    UpdateUserFullInfo var57 = (UpdateUserFullInfo)var1;
                    Example.usersFullInfo.put(var57.userId, var57.userFullInfo);
                    break;
                case 464087707:
                    UpdateChatDefaultDisableNotification var55 = (UpdateChatDefaultDisableNotification)var1;
                    var7 = (Chat)Example.chats.get(var55.chatId);
                    synchronized(var7) {
                        var7.defaultDisableNotification = var55.defaultDisableNotification;
                        break;
                    }
                case 488876260:
                    UpdateChatIsPinned var54 = (UpdateChatIsPinned)var1;
                    var7 = (Chat)Example.chats.get(var54.chatId);
                    synchronized(var7) {
                        var7.isPinned = var54.isPinned;
                        Example.setChatOrder(var7, var54.order);
                        break;
                    }
                case 580348828:
                    UpdateChatLastMessage var53 = (UpdateChatLastMessage)var1;
                    var7 = (Chat)Example.chats.get(var53.chatId);
                    synchronized(var7) {
                        var7.lastMessage = var53.lastMessage;
                        Example.setChatOrder(var7, var53.order);
                        break;
                    }
                case 708334213:
                    UpdateChatReadOutbox var52 = (UpdateChatReadOutbox)var1;
                    var7 = (Chat)Example.chats.get(var52.chatId);
                    synchronized(var7) {
                        var7.lastReadOutboxMessageId = var52.lastReadOutboxMessageId;
                        break;
                    }
                case 924030531:
                    UpdateBasicGroupFullInfo var56 = (UpdateBasicGroupFullInfo)var1;
                    Example.basicGroupsFullInfo.put(var56.basicGroupId, var56.basicGroupFullInfo);
                    break;
                case 1183394041:
                    UpdateUser var2 = (UpdateUser)var1;
                    Example.users.put(var2.user.id, var2.user);
                    break;
                case 1288828758:
                    UpdateSupergroupFullInfo var8 = (UpdateSupergroupFullInfo)var1;
                    Example.supergroupsFullInfo.put(var8.supergroupId, var8.supergroupFullInfo);
                    break;
                case 1309386144:
                    UpdateChatReplyMarkup var51 = (UpdateChatReplyMarkup)var1;
                    var7 = (Chat)Example.chats.get(var51.chatId);
                    synchronized(var7) {
                        var7.replyMarkupMessageId = var51.replyMarkupMessageId;
                        break;
                    }
                case 1468347188:
                    UpdateChatIsMarkedAsUnread var50 = (UpdateChatIsMarkedAsUnread)var1;
                    var7 = (Chat)Example.chats.get(var50.chatId);
                    synchronized(var7) {
                        var7.isMarkedAsUnread = var50.isMarkedAsUnread;
                        break;
                    }
                case 1622347490:
                    Example.onAuthorizationStateUpdated(((UpdateAuthorizationState)var1).authorizationState);
                    break;
                case 2075757773:
                    UpdateNewChat var6 = (UpdateNewChat)var1;
                    var7 = var6.chat;
                    synchronized(var7) {
                        Example.chats.put(var7.id, var7);
                        long var9 = var7.order;
                        var7.order = 0L;
                        Example.setChatOrder(var7, var9);
                    }
            }

        }
    }

    private static class DefaultHandler implements ResultHandler {
        private DefaultHandler() {
        }

        public void onResult(Object var1) {
            Example.print(var1.toString());
        }
    }

    private static class OrderedChat implements Comparable<Example.OrderedChat> {
        final long order;
        final long chatId;

        OrderedChat(long var1, long var3) {
            this.order = var1;
            this.chatId = var3;
        }

        public int compareTo(Example.OrderedChat var1) {
            if (this.order != var1.order) {
                return var1.order < this.order ? -1 : 1;
            } else if (this.chatId != var1.chatId) {
                return var1.chatId < this.chatId ? -1 : 1;
            } else {
                return 0;
            }
        }

        public boolean equals(java.lang.Object var1) {
            Example.OrderedChat var2 = (Example.OrderedChat)var1;
            return this.order == var2.order && this.chatId == var2.chatId;
        }
    }
}