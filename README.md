# Firebase chat
One to one chat implementation using firebase.

# Introduction: Why implement chat with firebase?

Because it's easier and you can create a working app with one to one chat system in a single day. Let's consider some pros and cons of firebase if we use it as a back end for our chat application.

###### Pros

* Firebase has a realtime database, that means you just need to add appropriate listeners in your app and you will automatically get notified when something is updated in the database.
* It uses json format (key/value pairs) to represent its database which can be user friendly to some of the users who deal with json data on day to day basis.
* It's very fast and manages offline states automatically.
* It also has a authentication system through which you can provide proper login/signup for users.

###### Cons

* No straightforward api available for sending push notification from one device to another (without the use of a separate backend).
* No inbuilt encryption, you would have to implement your own encryption for chatting.

# Dependencies required for firebase

Add this line to your project level build.gradle file in [**dependencies**](https://github.com/crazyhitty/firebase-chat/blob/master/build.gradle#L9) section.

```
classpath 'com.google.gms:google-services:3.0.0'
```

Then, add these to your app level build.gradle file in [**dependencies**](https://github.com/crazyhitty/firebase-chat/blob/master/app/build.gradle) section.

```
// firebase related dependencies
compile 'com.google.firebase:firebase-database:9.6.1'
compile 'com.google.firebase:firebase-messaging:9.6.1'
compile 'com.google.firebase:firebase-auth:9.6.1'
```

Don't forget to apply google services plugin at the end of your app level build.gradle file.

```
apply plugin: 'com.google.gms.google-services'
```

Also, add [google-services.json](https://support.google.com/firebase/answer/7015592) associated with your project to the app module.

# Understanding firebase database

Before we get our hands dirty in android studio, we must understand how firebase database is structured so we don't get confused in the later stages.

As you would have guessed by now, firebase database is structured in a json format. So let's suppose we have to save a user's information in firebase, then we will write the data in this format:

```
{
  "user": {
    "name": "Kartik",
    "age": 22,
    "hobby": "video games"
  }
}
```

We can write this piece of data in our application by first creating a model class for a user which will contain name, age and hobby fields.

```
public class User {
    public String name;
    public int age;
    public String hobby;

    public User() {
    }

    public User(String name, int age, String hobby) {
        this.name = name;
        this.age = age;
        this.hobby = hobby;
    }
}
```

Then we can get database reference of firebase and add the object of User class to child "user" to save it in the database.

```
User user = new User("Kartik", 22, "video games");
FirebaseDatabase.getInstance()
    .getReference()
    .child("user")
    .setValue(user);
```

Now you can easily add data to firebase database, yay! But wait… what if you wanted to add multiple users. In the above scenario the data for “user” will be always replaced instead of maintaining a list. So let's create a list of users.

```
{
  "users": {
    "unique_id_1": {
      "name": "Kartik",
      "age": 22,
      "hobby": "video games"
    },
    "unique_id_2": {
      "name": "Kajal",
      "age": 22,
      "hobby": "studying"
    }
  }
}
```

We can achieve a somewhat similar format by using **push()** method instead of using **setValue(Object object)** method. This would generate random keys for your user's object. But this would make it difficult to find a particular user without the knowledge of that random key. So instead, we will add key based on unique uid of user which can be retrieved from **FirebaseAuth** class.

```
User user1 = new User("Kartik", 22, "video games");
FirebaseDatabase.getInstance()
    .getReference()
    .child("users")
    .child("unique_id_1")
    .setValue(user1);

User user2 = new User("Kajal", 22, "studying");
FirebaseDatabase.getInstance()
    .getReference()
    .child("users")
    .child("unique_id_2")
    .setValue(user2);
```

Now we have learned how to create a single object and a object containing multiple list of similar objects.

# Creating appropriate model classes for firebase

Let's create a list of model classes we will be requiring in this project:

###### User.java

```
public class User {
    public String uid;
    public String email;
    public String firebaseToken;

    public User() {}

    public User(String uid, String email, String firebaseToken) {
        this.uid = uid;
        this.email = email;
        this.firebaseToken = firebaseToken;
    }
}
```

###### Chat.java

```
public class Chat {
    public String sender;
    public String receiver;
    public String senderUid;
    public String receiverUid;
    public String message;
    public long timestamp;

    public Chat() {}

    public Chat(String sender, String receiver, String senderUid, String receiverUid, String message, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.message = message;
        this.timestamp = timestamp;
    }
}
```

**Note:** Make sure to add an empty constructor to your model classes as sometimes firebase crashes due to the lack of empty constructors.

# Creating users for our application

Well we need users for our app, so we need to find a way to add them to our backend. In the application I have created, I add users to the database after they signup using **FirebaseAuth**. You can skip the authorization part and directly add them to firebase database also. But I would recommend using **FirebaseAuth** as it would make your life a little bit easier and reduce the development time too.

In order to add a new user, we fill the data in our model class and then add this object to our "users" child using the same method as we used above.

```
public void addUserToDatabase(Context context, FirebaseUser firebaseUser) {
    User user = new User(firebaseUser.getUid(),
        firebaseUser.getEmail(),
        new SharedPrefUtil(context).getString(Constants.ARG_FIREBASE_TOKEN));
    FirebaseDatabase.getInstance()
        .getReference()
        .child(Constants.ARG_USERS)
        .child(firebaseUser.getUid())
        .setValue(user)
        .addOnCompleteListener(new OnCompleteListener<Void> () {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // successfully added user
                } else {
                    // failed to add user
                }
            }
        });
}
```

# Getting list of users

Now, the next step is to show listing of available users so that we can select one of them and start chatting.

We need to bind a single time listener to the "users" child so that we can get the initial listing of available users.

```
public void getAllUsersFromFirebase() {
    FirebaseDatabase.getInstance()
        .getReference()
        .child(Constants.ARG_USERS)
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren()
                    .iterator();
                List<User> users = new ArrayList<>();
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    User user = dataSnapshotChild.getValue(User.class);
                    if (!TextUtils.equals(user.uid,
                            FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        users.add(user);
                    }
                }
                // All users are retrieved except the one who is currently logged
                // in device.
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Unable to retrieve the users.
            }
        });
}
```

# Chat rooms

After retrieving the list of users we can create a chat room through which users will chat with each other.

A chat room will consist of two users in case of one to one chat:

*   Currently logged in user.
*   User we want to chat with.

For future references let's take two users:

*   Kartik (logged in user)
*   Kajal (other user Kartik wants to chat with)

Now, we can create a child named "chat_rooms" in our database which will contain all of the data regarding chats between multiple users. We can further create a child of object “chat_rooms” like “Kartik_Kajal” or “Kajal_Kartik”. The reason behind this naming is to make the searching of chat rooms much easier on application's end. You can also use any sort of unique separator instead of underscore, just make sure that it doesn’t conflict with your usernames.

Also, if Kartik is chatting with Kajal for the first time, a room with name “Kartik_Kajal” will be created otherwise if the case is reversed then “Kajal_Kartik” will be created.

```
public void sendMessageToFirebaseUser(final Context context,
                                          final Chat chat,
                                          final String receiverFirebaseToken) {
    final String room_type_1 = chat.senderUid + "_" + chat.receiverUid;
    final String room_type_2 = chat.receiverUid + "_" + chat.senderUid;

    final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
            .getReference();

    databaseReference.child(Constants.ARG_CHAT_ROOMS)
            .getRef()
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(room_type_1)) {
                        Log.e(TAG, "sendMessageToFirebaseUser: " + room_type_1 + " exists");
                        databaseReference.child(Constants.ARG_CHAT_ROOMS)
                                .child(room_type_1)
                                .child(String.valueOf(chat.timestamp))
                                .setValue(chat);
                    } else if (dataSnapshot.hasChild(room_type_2)) {
                        Log.e(TAG, "sendMessageToFirebaseUser: " + room_type_2 + " exists");
                        databaseReference.child(Constants.ARG_CHAT_ROOMS)
                                .child(room_type_2)
                                .child(String.valueOf(chat.timestamp))
                                .setValue(chat);
                    } else {
                        Log.e(TAG, "sendMessageToFirebaseUser: success");
                        databaseReference.child(Constants.ARG_CHAT_ROOMS)
                                .child(room_type_1)
                                .child(String.valueOf(chat.timestamp))
                                .setValue(chat);
                        }
                    }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Unable to send message.
                }
            });
}
```

We can get list of chat messages by adding a listener to either “Kartik_Kajal” or “Kajal_Kartik” depending upon which one exists. In order to check which one exists we can use method hasChild on “chat_rooms” object.

```
public void getMessageFromFirebaseUser(String senderUid, String receiverUid) {
    final String room_type_1 = senderUid + "_" + receiverUid;
    final String room_type_2 = receiverUid + "_" + senderUid;

    final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
            .getReference();

    databaseReference.child(Constants.ARG_CHAT_ROOMS)
            .getRef()
            .addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChild(room_type_1)) {
                Log.e(TAG, "getMessageFromFirebaseUser: " + room_type_1 + " exists");
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child(Constants.ARG_CHAT_ROOMS)
                        .child(room_type_1)
                        .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        // Chat message is retreived.
                        Chat chat = dataSnapshot.getValue(Chat.class);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                            // Unable to get message.
                    }
                });
            } else if (dataSnapshot.hasChild(room_type_2)) {
                Log.e(TAG, "getMessageFromFirebaseUser: " + room_type_2 + " exists");
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child(Constants.ARG_CHAT_ROOMS)
                        .child(room_type_2)
                        .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        // Chat message is retreived.
                        Chat chat = dataSnapshot.getValue(Chat.class);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Unable to get message.
                    }
                });
            } else {
                Log.e(TAG, "getMessageFromFirebaseUser: no such room available");
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Unable to get message
        }
    });
}
```

The above listener will give callbacks on every new chat message added to the firebase database inside the “Kartik_Kajal” chatroom.

# Sending push notifications to users

If you want to send a push notification to a particular user, we would have to use firebase push notifications api.

Before that make sure that you have generated a firebase token (using fcm aka firebase cloud messaging) for your user and saved it in your users table in firebase for every individual user.

[Link to firebase cloud messaging for android tutorial.](https://firebase.google.com/docs/cloud-messaging/android/client)

After you have integrated the code to generate a firebase token, then we can test sample notifications by manually hitting the api in postman.

![Postman screenshot for sending push notification](http://i.imgur.com/A8G7AtQ.png)

Now, we can hit the same api using any rest client on our device with custom data object (containing extra information that you would like to send with you push notification). I have used okhttp for managing the api calls, but feel free to use any rest client as per your requirements.

```
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Author: Kartik Sharma
 * Created on: 10/16/2016 , 1:53 PM
 * Project: FirebaseChat
 */

public class FcmNotificationBuilder {
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "FcmNotificationBuilder";
    private static final String SERVER_API_KEY = "YOUR_SERVER_API_KEY";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTH_KEY = "key=" + SERVER_API_KEY;
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    // json related keys
    private static final String KEY_TO = "to";
    private static final String KEY_NOTIFICATION = "notification";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TEXT = "text";
    private static final String KEY_DATA = "data";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_UID = "uid";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private String mTitle;
    private String mMessage;
    private String mUsername;
    private String mUid;
    private String mFirebaseToken;
    private String mReceiverFirebaseToken;

    private FcmNotificationBuilder() {

    }

    public static FcmNotificationBuilder initialize() {
        return new FcmNotificationBuilder();
    }

    public FcmNotificationBuilder title(String title) {
        mTitle = title;
        return this;
    }

    public FcmNotificationBuilder message(String message) {
        mMessage = message;
        return this;
    }

    public FcmNotificationBuilder username(String username) {
        mUsername = username;
        return this;
    }

    public FcmNotificationBuilder uid(String uid) {
        mUid = uid;
        return this;
    }

    public FcmNotificationBuilder firebaseToken(String firebaseToken) {
        mFirebaseToken = firebaseToken;
        return this;
    }

    public FcmNotificationBuilder receiverFirebaseToken(String receiverFirebaseToken) {
        mReceiverFirebaseToken = receiverFirebaseToken;
        return this;
    }

    public void send() {
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(MEDIA_TYPE_JSON, getValidJsonBody().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                .addHeader(AUTHORIZATION, AUTH_KEY)
                .url(FCM_URL)
                .post(requestBody)
                .build();

        Call call = new OkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onGetAllUsersFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "onResponse: " + response.body().string());
            }
        });
    }

    private JSONObject getValidJsonBody() throws JSONException {
        JSONObject jsonObjectBody = new JSONObject();
        jsonObjectBody.put(KEY_TO, mReceiverFirebaseToken);

        JSONObject jsonObjectData = new JSONObject();
        jsonObjectData.put(KEY_TITLE, mTitle);
        jsonObjectData.put(KEY_TEXT, mMessage);
        jsonObjectData.put(KEY_USERNAME, mUsername);
        jsonObjectData.put(KEY_UID, mUid);
        jsonObjectData.put(KEY_FCM_TOKEN, mFirebaseToken);
        jsonObjectBody.put(KEY_DATA, jsonObjectData);

        return jsonObjectBody;
    }
}
```

Now you can easily create a one to one chatting system with inbuilt push notifications without the need of an extra backend, yay!

# Screenshots

<img src="https://github.com/crazyhitty/firebase-chat/raw/master/screenshots/splash.png" alt="alt text" width="400"> <img src="https://github.com/crazyhitty/firebase-chat/raw/master/screenshots/login.png" alt="alt text" width="400">

<img src="https://github.com/crazyhitty/firebase-chat/raw/master/screenshots/users.png" alt="alt text" width="400"> <img src="https://github.com/crazyhitty/firebase-chat/raw/master/screenshots/chat.png" alt="alt text" width="400">

<img src="https://github.com/crazyhitty/firebase-chat/raw/master/screenshots/push_notification.png" alt="alt text" width="400">
