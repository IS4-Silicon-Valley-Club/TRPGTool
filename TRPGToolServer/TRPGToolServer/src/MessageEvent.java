import java.util.EventObject;

class MessageEvent extends EventObject {
	private ChatClientUser source;
	private String name;
	private String value;

	public MessageEvent(ChatClientUser source, String name, String value) {
		super(source);
		this.source = source;
		this.name = name;
		this.value = value;
	}

	//イベントを発生させたユーザー
	public ChatClientUser getUser() { return source; }

	//このイベントのコマンド名を返す
	public String getName() { return this.name; }

	//このイベントの
	public String getValue() { return this.value; }
}