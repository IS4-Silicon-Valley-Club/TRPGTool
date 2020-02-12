import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;

public class DicerollFrame extends JFrame implements ActionListener {
	
	private Socket socket;

	public DicerollFrame(String name, Socket socket) {
		super(name);
		this.socket = socket;

		JButton btn = new JButton("Diceroll");
		btn.addActionListener(this);
		add(btn);

		setSize(300, 300);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
//		new OpenFrame("Additional Frame");
		sendMessage("diceroll");
		dispose();
	}
	
	public void sendMessage(String msg) {
		try {
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output);

			writer.println(msg);
			writer.flush();
		}
		catch(Exception err) {/* msgTextArea.append("ERROR>" + err + "\n"); */}
	}
}
