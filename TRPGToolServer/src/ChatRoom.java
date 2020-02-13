import java.util.ArrayList;
import java.util.Random;

//チャットルームオブジェクト
class ChatRoom implements MessageListener {
	//チャットルームの名前
	private String name;

	//このチャットルームの管理権限を持つユーザー
	private ChatClientUser hostUser;

	//このチャットルームに参加している全てのユーザーの動的配列
	//この配列には hostUser も含む
	private ArrayList<ChatClientUser> roomUsers;

	//ダイスロール用Randomクラス
	private Random dice = new Random();

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
			roomUsers.get(i).reachedMessage("getUsers", name, "");
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
			roomUsers.get(i).reachedMessage("getUsers", name, "");
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
				String message = e.getName() + " " + source.getName() + ">" + e.getValue() + " " + e.getDate();
				roomUsers.get(i).sendMessage(message);
			}
		}
		else if (e.getName().equals("whisper")) {
			String s[] = e.getValue().split(" ", 2);
			for(int i = 0 ; i < roomUsers.size() ; i++) {
				if (s[0].equals(roomUsers.get(i).getName() ) ){
					String message = "msg" + " " + source.getName() + " からのウィスパー>" + s[1];
					roomUsers.get(i).sendMessage(message);
				}
				if(source.getName().contentEquals(roomUsers.get(i).getName())){
					String message = "msg" + " " +s[0] + " へのウィスパー>" + s[1];
					roomUsers.get(i).sendMessage(message);

				}
			}
		}
		//ユーザーが名前を変更した
		else if(e.getName().equals("setName")) {
			for(int i = 0 ; i < roomUsers.size() ; i++) {
				roomUsers.get(i).reachedMessage("getUsers", name, "");
			}
		}
		//ユーザーがダイスロールをした
		else if(e.getName().equals("dicerole")) {

			try{
				String s[] = e.getValue().split(" ");
				s = s[0].split("d");
				int dicecount = Integer.parseInt(s[0]);
				int dicenumber = Integer.parseInt(s[1]);
				String dicevalue = " ";
				int total = 0;
				for(int i = 0 ; i < dicecount; i++) {
					int rmd = dice.nextInt(dicenumber) + 1;
					dicevalue +=  (rmd) + " ";
					total += rmd;
				}
				for(int i = 0 ; i < roomUsers.size() ; i++) {
					String message = "msg " + source.getName() + ">" + dicecount + "d" + dicenumber + " [" + dicevalue + "] 合計 " + total;
					roomUsers.get(i).sendMessage(message);
				}
			} catch(NumberFormatException err) {
				source.sendMessage("error 入力が正しくありません　例：2d6");
			}

		}
	}
}