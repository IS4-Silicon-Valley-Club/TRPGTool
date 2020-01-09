import java.util.ArrayList;

//チャットルームオブジェクト
class ChatRoom implements MessageListener {
	//チャットルームの名前
	private String name;

	//このチャットルームの管理権限を持つユーザー
	private ChatClientUser hostUser;

	//このチャットルームに参加している全てのユーザーの動的配列
	//この配列には hostUser も含む
	private ArrayList<ChatClientUser> roomUsers;

	public ChatRoom(String name, ChatClientUser hostUser) {
		roomUsers = new ArrayList<ChatClientUser>();

		this.name = name;
		this.hostUser = hostUser;

		addUser(hostUser);
	}

	//この部屋の名前
	public String getName() {
		return name;
	}

	//この部屋を作成した権限のあるクライアント
	public ChatClientUser getHostUser() {
		return hostUser;
	}

	//この部屋にユーザーを追加（入室）する
	public void addUser(ChatClientUser user) {
		user.addMessageListener(this);
		roomUsers.add(user);
		for(int i = 0 ; i < roomUsers.size() ; i++) {
			roomUsers.get(i).reachedMessage("getUsers", name);
			roomUsers.get(i).sendMessage("msg >" + user.getName() + " さんが入室しました");
		}
	}

	//指定したユーザーがこの部屋にいるかどうか
	public boolean containsUser(ChatClientUser user) {
		return roomUsers.contains(user);
	}

	//この部屋のユーザー全員を取得する
	public ChatClientUser[] getUsers() {
		ChatClientUser[] users = new ChatClientUser[roomUsers.size()];
		roomUsers.toArray(users);
		return users;
	}

	//指定したユーザーをチャットルームから退室させる
	public void removeUser(ChatClientUser user) {
		user.removeMessageListener(this);
		roomUsers.remove(user);
		for(int i = 0 ; i < roomUsers.size() ; i++) {
			roomUsers.get(i).reachedMessage("getUsers", name);
			roomUsers.get(i).sendMessage("msg >" + user.getName() + " さんが退室しました");
		}

		//ユーザーがいなくなったので部屋を削除する
		if (roomUsers.size() == 0) {
			Server.getInstance().removeChatRoom(this);
		}
	}

	//このチャットルームのユーザーがメッセージを処理した
	public void messageThrow(MessageEvent e) {
		ChatClientUser source = e.getUser();

		//ユーザーが発言した
		if (e.getName().equals("msg")) {
			for(int i = 0 ; i < roomUsers.size() ; i++) {
				String message = e.getName() + " " + source.getName() + ">" + e.getValue();
				roomUsers.get(i).sendMessage(message);
			}
		}
		//ユーザーが名前を変更した
		else if(e.getName().equals("setName")) {
			for(int i = 0 ; i < roomUsers.size() ; i++) {
				roomUsers.get(i).reachedMessage("getUsers", name);
			}
		}
	}
}