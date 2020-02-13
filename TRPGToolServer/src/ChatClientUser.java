import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

//チャットユーザーオブジェクト
class ChatClientUser implements Runnable, MessageListener {
	//ソケット
	private Socket socket;

	//ユーザーの名前
	private String name;

	//チャットサーバー
	private Server server = Server.getInstance();

	//メッセージリスナの動的配列
	//メッセージリスナはこのユーザーが発言したときに呼び出されるイベント
	private ArrayList<MessageListener> messageListeners;

	public ChatClientUser(Socket socket) {
		messageListeners = new ArrayList<MessageListener>();
		this.socket = socket;

		addMessageListener(this);

		Thread thread = new Thread(this);
		thread.start();
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return this.name;
	}

	public void run() {
		try {
			//ユーザーの情報を取得する
			InputStream input = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));

			//ユーザーのメッセージ送信を確認
			while(!socket.isClosed()) {
				String line = reader.readLine();
				System.out.println("INPUT=" + line);

				String[] msg = line.split(" ", 2);
				String msgName = msg[0];
				String msgValue = (msg.length < 2 ? "" : msg[1]);
				String msgDate = (msg.length < 3 ? "" : msg[2]);

				reachedMessage(msgName, msgValue, msgDate);
			}
		}
		catch(Exception err) { err.printStackTrace(); }
	}

	//このユーザーが送信してきたメッセージイベントを受ける
	public void messageThrow(MessageEvent e) {
		String msgType = e.getName();
		String msgValue = e.getValue();

		//切断する
		if (msgType.equals("close")) {
			try { close(); }
			catch(IOException err) { err.printStackTrace(); }
		}
		//名前を更新する
		else if(msgType.equals("setName")) {
			String name = msgValue;

			//半角文字は使えない記号
			if (name.indexOf(" ") == -1) {
				String before = getName();
				setName(name);
				sendMessage("successful setName");
				reachedMessage("msg", before + " から " + name + " に名前を変更しました","");
			}
			else {
				sendMessage("error 名前に半角空白文字を使うことはできません");
			}
		}
		//新しい部屋を追加する
		else if(msgType.equals("addRoom")) {
			String name = msgValue;

			//半角文字は使えない記号
			if (name.indexOf(" ") == -1) {
				ChatRoom room = new ChatRoom(name , this);
				server.addChatRoom(room);
				sendMessage("successful addRoom");
			}
			else sendMessage("error 名前に半角空白文字を使うことはできません");
		}
		//現在存在する部屋を返す
		else if(msgType.equals("getRooms")) {
			String result = "";
			ChatRoom[] rooms = server.getChatRooms();
			for(int i = 0 ; i < rooms.length ; i++) {
				result += rooms[i].getName() + " ";
			}
			sendMessage("rooms " + result);
		}
		//部屋に入る
		else if(msgType.equals("enterRoom")) {
			ChatRoom room = server.getChatRoom(msgValue);
			if (room != null) {
				room.addUser(this);
				sendMessage("successful enterRoom");
			}
			else sendMessage("error \"" + msgValue + "\" が見つかりません");
		}
		//部屋から出る
		else if(msgType.equals("exitRoom")) {
			ChatRoom room = server.getChatRoom(msgValue);
			if (room != null) {
				room.removeUser(this);
				sendMessage("successful exitRoom");
			}
			else sendMessage("error \"" + msgValue + "\" が見つかりません");
		}
		//指定した部屋のユーザーのリストを返す
		else if(msgType.equals("getUsers")) {
			ChatRoom room = server.getChatRoom(msgValue);
			if (room != null) {
				String result = "";
				ChatClientUser[] users = room.getUsers();
				for(int i = 0 ; i < users.length ; i++) {
					result += users[i].getName() + " ";
				}
				sendMessage("users " + result);
			}
		}
	}

	public String toString() {
		return "NAME=" + getName();
	}

	public void close() throws IOException {
		server.removeUser(this);
		messageListeners.clear();
		socket.close();
	}

	//このユーザーに指定されたメッセージを送信する
	public void sendMessage(String message) {
		try {
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output);

			//メッセージの送信
			writer.println(message);

			writer.flush();
		}
		catch(Exception err) {
		}
	}
	//このユーザーが受け取ったメッセージを処理する
	public void reachedMessage(String name, String value, String date) {
		MessageEvent event = new MessageEvent(this, name, value, date);
		for(int i = 0 ; i < messageListeners.size() ; i++ ) {
			messageListeners.get(i).messageThrow(event);
		}
	}

	//このオブジェクトにメッセージリスナを登録する
	public void addMessageListener(MessageListener l) {
		messageListeners.add(l);
	}

	//指定したメッセージリスナをこのオブジェクトから解除する
	public void removeMessageListener(MessageListener l) {
		messageListeners.remove(l);
	}

	//このオブジェクトに登録されているメッセージリスナの配列を返す
	public MessageListener[] getMessageListeners() {
		MessageListener[] listeners = new MessageListener[messageListeners.size()];
		messageListeners.toArray(listeners);
		return listeners;
	}
}