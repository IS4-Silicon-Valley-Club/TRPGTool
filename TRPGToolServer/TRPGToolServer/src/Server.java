import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	public static void main(String[] args) {
		Server application = Server.getInstance();
		application.start();
	}

	//サーバーはシングルトン設計。唯一のインスタンス
	private static Server instance;
	public static Server getInstance() {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	//サーバーソケット
	private ServerSocket server;

	//現在開いている部屋オブジェクトの動的配列
	private ArrayList<ChatRoom> roomList;

	//現在チャットに参加している全ユーザーの動的配列
	private ArrayList<ChatClientUser> userList;

	private Server() {
		roomList = new ArrayList<ChatRoom>();
		userList = new ArrayList<ChatClientUser>();
	}

	//main メソッドから呼び出される
	public void start() {
		try {
			server = new ServerSocket(2816);

			while(!server.isClosed()) {
				//新しいクライアントの接続を待つ
				Socket client = server.accept();

				//ユーザーオブジェクトを生成する
				ChatClientUser user = new ChatClientUser(client);
				addUser(user);
			}
		}
		catch(Exception err) {
			err.printStackTrace();
		}
	}

	//チャットルームを追加する
	public void addChatRoom(ChatRoom room) {
		if (roomList.contains(room)) return;

		roomList.add(room);
		System.out.println("addRoom=[" + room + "]");

		//すべてのユーザーにチャットルームが更新されたことを通知する
		for(int i = 0 ; i < userList.size() ; i++) {
			userList.get(i).reachedMessage("getRooms" , "");
		}
	}
	//指定した名前のチャットルームを取得する
	public ChatRoom getChatRoom(String name) {
		for(int i = 0 ; i < roomList.size() ; i++) {
			ChatRoom room = roomList.get(i);
			if (name.equals(room.getName())) return room;
		}
		return null;
	}
	//チャットルームの配列を取得する
	public ChatRoom[] getChatRooms() {
		ChatRoom[] result = new ChatRoom[roomList.size()];

		//現在開かれている部屋のリストを配列に移す
		for(int i = 0 ; i < roomList.size() ; i++) {
			result[i] = roomList.get(i);
		}
		return result;
	}
	//チャットルームを削除する
	public void removeChatRoom(ChatRoom room) {
		roomList.remove(room);
		System.out.println("removeRoom=[" + room + "]");

		//すべてのユーザーにチャットルームが更新されたことを通知する
		for(int i = 0 ; i < userList.size() ; i++) {
			userList.get(i).reachedMessage("getRooms" , "");
		}
	}
	//すべてのチャットルームを削除する
	public void clearChatRoom() { 
		roomList.clear();

		//すべてのユーザーにチャットルームが更新されたことを通知する
		for(int i = 0 ; i < userList.size() ; i++) {
			userList.get(i).reachedMessage("getRooms" , "");
		}
	}

	//ユーザーを追加する
	public void addUser(ChatClientUser user) {
		if (userList.contains(user)) return;

		userList.add(user);
		System.out.println("addUser=[" + user + "]");
	}

	//指定した名前のユーザーを取得する
	public ChatClientUser getUser(String name) {
		for(int i = 0 ; i < userList.size() ; i++) {
			ChatClientUser user = userList.get(i);
			if (user.getName().equals(name)) return user;
		}
		return null;
	}

	//すべてのユーザーを返す
	public ChatClientUser[] getUsers() {
		ChatClientUser[] users = new ChatClientUser[userList.size()];
		userList.toArray(users);
		return users;
	}

	//ユーザーを削除する
	public void removeUser(ChatClientUser user) {
		userList.remove(user);
		System.out.println("removeUser=[" + user + "]");

		//サーバーからユーザーが解放される場合、すなわちユーザーはログアウトする
		//すべての部屋を調べ、ユーザーを部屋から退席させる
		for(int i = 0 ; i < roomList.size() ; i++) {
			if (roomList.get(i).containsUser(user)) roomList.get(i).removeUser(user);
		}
	}

	//すべてのユーザーを削除する
	public void clearUser() { userList.clear(); }

	//サーバーを閉じて切断する
	public void close() throws IOException {
		server.close();
	}
}


