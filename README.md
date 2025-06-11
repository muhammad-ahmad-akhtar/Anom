# 💬 Real-Time Android Chat App

An Object-Oriented Programming project — this is a real-time chat app built in Java for Android. It allows two users to exchange messages, share files, and even interact with an AI assistant named **Mona**, all powered by a custom socket server, Firebase, and local Room database.

---

## 🚀 Features

- 🔐 **Passwordless Sign Up & Sign In** (via Firebase Authentication)
- 📡 **Real-time Chat** using custom **Java Socket Server**
- 🟢 **User Online/Offline Status**
- 💾 **Room Database** for offline chat history
- 📁 **Send/Receive Files** (images, documents)
- 🤖 **AI Assistant** Mona (Integrated with [Together API](https://www.together.ai))
- 🌍 **ngrok** tunnel for server communication
- 🔧 Written using **OOP principles** and Android's **Single Activity + Fragments** architecture

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| Java       | Core language for app and server |
| Firebase   | Auth + Realtime user status |
| Room       | Local storage for chats |
| Socket Programming | Real-time message transmission |
| Together API | AI assistant (Mona) |
| ngrok      | Tunnel server for local testing |

---

## 📂 Project Structure

📦 RealTimeChatApp
├── 📱 AndroidApp/
│ ├── Activities/
│ ├── Fragments/
│ ├── Adapter/
│ ├── Database/ (Room)
│ ├── Network/ (Client)
│ └── MonaAI/ (API integration)
└── 🌐 Server/
└── Server.java (Java socket server)


---

## 🖼️ Screenshots

> *Coming Soon!* Add screenshots of chat UI, file sharing, and Mona AI responses.

---

## ⚙️ How It Works

1. **Sign Up/Login**: Users sign in using OTP (Firebase passwordless auth).
2. **Connect Socket**: The app connects to the local Java server via **ngrok** tunnel.
3. **Messaging**: Messages are sent/received in real-time over sockets.
4. **Offline Support**: All messages are stored locally in Room for offline viewing.
5. **AI Integration**: Users can chat with Mona using the Together API.

---

## 💡 How to Run

### 🔧 Android App
1. Clone this repo
2. Open in Android Studio
3. Replace `google-services.json` with your Firebase config
4. Set your `ngrok` URL in the socket client
5. Run on an emulator or device

### 🌐 Java Server
1. Navigate to `Server/` folder
2. Compile and run:
    ```bash
    javac Server.java
    java Server
    ```
3. Use ngrok to expose the server:
    ```bash
    ngrok tcp 12345
    ```

---

## 📄 License

This project is open-source under the [MIT License](LICENSE).

---

## 🙋‍♂️ Author

**Ahmad**  
Student & Android Developer  
[LinkedIn](https://linkedin.com/in/360akhtar) | [GitHub](https://github.com/muhammad-ahmad-akhtar)

---

## 📬 Feedback & Contributions

Contributions, feature requests, and feedback are welcome! Feel free to open issues or pull requests.

