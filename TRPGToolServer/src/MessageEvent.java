import java.util.EventObject;

class MessageEvent extends EventObject {
	private ChatClientUser source;
	private String name;
	private String value;
	private String date;

	public MessageEvent(ChatClientUser source, String name, String value, String date) {
		super(source);
		this.source = source;
		this.name = name;
		this.value = value;
		this.date = date;
	}

	//イベントを発生させたユーザー
	public ChatClientUser getUser() { return source; }

	//このイベントのコマンド名を返す
	public String getName() { return this.name; }

	//このイベントの
	public String getValue() { return this.value; }

	//日付を返す
	public String getDate() { return this.date; }
}