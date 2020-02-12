import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client extends JFrame implements Runnable, ActionListener {
	public static void main(String[] args) {
		Client window = new Client();
		window.setSize(800, 600);
		window.setVisible(true);

	}

	//アプリケーション名
	private static final String APPNAME = "チャットクライアント";

	//接続先サーバーのホスト名
	private static final String HOST = "localhost";

	//接続先ポート番号
	private static final int PORT = 2816;

	//このアプリケーションのクライアントソケット
	private Socket socket;

	//メッセージ受信監視用スレッド
	private Thread thread;

	//現在入室中のチャットルーム名
	private String roomName;

	//以下、コンポーネント
	private JList roomList;	//チャットルームのリスト
	private JList userList;	//現在入室中のチャットルームのユーザー
	private JTextArea msgTextArea;		//メッセージを表示するテキストエリア
	private JTextField msgTextField;	//メッセージ入力用の一行テキスト
	private JTextField nameTextField;	//ユーザー名やチャットルーム名を入力する一行テキスト
	private JButton submitButton;		//「送信」ボタン
	private JButton whisperButton;		//「ウィスパー」ボタン
	private JButton renameButton;		//「名前の変更」ボタン
	private JButton addRoomButton;		//「部屋を追加」ボタン
	private JButton enterRoomButton;	//「入室・退室」ボタン
	private JButton openDicerollFrameButton;	    //「ダイスロール」ボタン
	private JButton dicerollButton; //ダイスロール用ウィンドウを開くボタン

	
	JFrame dicerollFrame = new JFrame();
	
	public Client() {
		super(APPNAME);

		JPanel topPanel = new JPanel();
		JPanel leftPanel = new JPanel();
		JPanel buttomPanel = new JPanel();

		JPanel roomPanel = new JPanel();
		JPanel userPanel = new JPanel();


		roomList = new JList();
		userList = new JList();
		msgTextArea = new JTextArea();
		msgTextField = new JTextField();
		nameTextField = new JTextField();
		submitButton = new JButton("送信");
		whisperButton = new JButton("ウィスパー");
		renameButton = new JButton("名前の変更");
		addRoomButton = new JButton("部屋を追加");
		enterRoomButton = new JButton("入室");
		openDicerollFrameButton = new JButton("ダイスロール");
		dicerollButton = new JButton("ダイスロール");

		submitButton.addActionListener(this);
		submitButton.setActionCommand("submit");
		
		whisperButton.addActionListener(this);
		whisperButton.setActionCommand("whisper");

		renameButton.addActionListener(this);
		renameButton.setActionCommand("rename");

		addRoomButton.addActionListener(this);
		addRoomButton.setActionCommand("addRoom");

		enterRoomButton.addActionListener(this);
		enterRoomButton.setActionCommand("enterRoom");
		
		openDicerollFrameButton.addActionListener(this);
		openDicerollFrameButton.setActionCommand("openDicerollFrame");
		
		dicerollButton.addActionListener(this);
		dicerollButton.setActionCommand("diceroll");

		roomPanel.setLayout(new BorderLayout());
		roomPanel.add(new JLabel("チャットルーム"), BorderLayout.NORTH);
		roomPanel.add(new JScrollPane(roomList), BorderLayout.CENTER);
		roomPanel.add(enterRoomButton, BorderLayout.SOUTH);

		userPanel.setLayout(new BorderLayout());
		userPanel.add(new JLabel("参加ユーザー"), BorderLayout.NORTH);
		userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

		topPanel.setLayout(new FlowLayout());
		topPanel.add(new JLabel("名前"));
		topPanel.add(nameTextField);
		topPanel.add(renameButton);
		topPanel.add(addRoomButton);
		topPanel.add(openDicerollFrameButton);
		
		dicerollFrame.add(dicerollButton);
		dicerollFrame.setSize(300,300);

		nameTextField.setPreferredSize(new Dimension(200, nameTextField.getPreferredSize().height));

		leftPanel.setLayout(new GridLayout(2, 1));
		leftPanel.add(roomPanel);
		leftPanel.add(userPanel);

		buttomPanel.setLayout(new BorderLayout());
		buttomPanel.add(msgTextField, BorderLayout.CENTER);
		buttomPanel.add(submitButton, BorderLayout.EAST);
		buttomPanel.add(whisperButton, BorderLayout.WEST);

		//テキストエリアはメッセージを表示するだけなので編集不可に設定
		msgTextArea.setEditable(false);

		//コンポーネントの状態を退室状態で初期化
		exitedRoom();

		this.getContentPane().add(new JScrollPane(msgTextArea), BorderLayout.CENTER);
		this.getContentPane().add(topPanel, BorderLayout.NORTH);
		this.getContentPane().add(leftPanel, BorderLayout.WEST);
		this.getContentPane().add(buttomPanel, BorderLayout.SOUTH);

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try { close(); }
				catch(Exception err) { }
			}
		});
		connectServer();

		//メッセージ受信監視用のスレッドを生成してスタートさせる
		thread = new Thread(this);
		thread.start();

		//現在の部屋を取得する
		sendMessage("getRooms");
	}

	//サーバーに接続する
	public void connectServer() {
		try {
			socket = new Socket(HOST, PORT);
			msgTextArea.append(">サーバーに接続しました\n"); 
		}
		catch(Exception err) {
			msgTextArea.append("ERROR>" + err + "\n"); 
		}
	}

	//サーバーから切断する
	public void close() throws IOException {
		sendMessage("close");
		socket.close();
	}

	//メッセージをサーバーに送信する
	public void sendMessage(String msg) {
		try {
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output);

			writer.println(msg);
			writer.flush();
		}
		catch(Exception err) { msgTextArea.append("ERROR>" + err + "\n"); }
	}

	//サーバーから送られてきたメッセージの処理
	public void reachedMessage(String name, String value) {
		//チャットルームのリストに変更が加えられた
		if (name.equals("rooms")) {
			if (value.equals("")) {
				roomList.setModel(new DefaultListModel());
			}
			else {
				String[] rooms = value.split(" ");
				roomList.setListData(rooms);
			}
		}
		//ユーザーが入退室した
		else if (name.equals("users")) {
			if (value.equals("")) {
				userList.setModel(new DefaultListModel());
			}
			else {
				String[] users = value.split(" ");
				userList.setListData(users);
			}
		}
		//メッセージが送られてきた
		else if (name.equals("msg")) {
			msgTextArea.append(value + "\n"); 
		}
		//処理に成功した
		else if (name.equals("successful")) {
			if (value.equals("setName")) msgTextArea.append(">名前を変更しました\n"); 
		}
		//エラーが発生した
		else if (name.equals("error")) {
			msgTextArea.append("ERROR>" + value + "\n"); 
		}
	}

	//メッセージ監視用のスレッド
	public void run() {
		try {
			InputStream input = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			while(!socket.isClosed()) {
				String line = reader.readLine();

				String[] msg = line.split(" ", 2);
				String msgName = msg[0];
				String msgValue = (msg.length < 2 ? "" : msg[1]);

				reachedMessage(msgName, msgValue);
			}
		}
		catch(Exception err) { }
	}

	//ボタンが押されたときのイベント処理
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if(cmd.equals("submit")) {	//送信
			sendMessage("msg " + msgTextField.getText());
			msgTextField.setText("");
		}
		else if(cmd.equals("rename")) {	//名前の変更
			sendMessage("setName " + nameTextField.getText());
		}
		else if(cmd.equals("addRoom")) {	//部屋を作成
			String roomName = nameTextField.getText();
			sendMessage("addRoom " + roomName);
			enteredRoom(roomName);
			sendMessage("getUsers " + roomName);
		}
		else if(cmd.equals("enterRoom")) {	//入室
			Object room = roomList.getSelectedValue();
			if (room != null) {
				String roomName = room.toString();
				sendMessage("enterRoom " + roomName);
				enteredRoom(roomName);
			}
		}
		else if(cmd.equals("exitRoom")) {	//退室
			sendMessage("exitRoom " + roomName);
			exitedRoom();
		}
		else if(cmd.equals("openDicerollFrame")) {
			dicerollFrame.setVisible(true);
		}
		else if(cmd.equals("dicerole")) {
			dicerollFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			sendMessage("dicerole");
		}
		else if(cmd.equals("whisper")) {
			Object user = userList.getSelectedValue();
			if(user != null) {
				String userName = user.toString();
				sendMessage("whisper " + user + " " + msgTextField.getText());
			}
		}
	}

	//部屋に入室している状態のコンポーネント設定
	private void enteredRoom(String roomName) {
		this.roomName = roomName;
		setTitle(APPNAME + " " + roomName);

		msgTextField.setEnabled(true);
		submitButton.setEnabled(true);

		addRoomButton.setEnabled(false);
		enterRoomButton.setText("退室");
		enterRoomButton.setActionCommand("exitRoom");
	}

	//部屋に入室していない状態のコンポーネント設定
	private void exitedRoom() {
		roomName = null;
		setTitle(APPNAME);

		msgTextField.setEnabled(false);
		submitButton.setEnabled(false);

		addRoomButton.setEnabled(true);
		enterRoomButton.setText("入室");
		enterRoomButton.setActionCommand("enterRoom");
		userList.setModel(new DefaultListModel());
	}
}