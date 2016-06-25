package UI;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Process.MP3FileClassifier;

@SuppressWarnings("serial")
public class StartFrame extends JFrame implements ActionListener{
	private JFileChooser jfc = new JFileChooser();
	private JLabel open_jlb = new JLabel("�з��� ���� ");
	private JTextField open_jtf = new JTextField();
	private JButton jbt_open = new JButton("����");
	private JLabel save_jlb = new JLabel("�з��� ������ ������ ���� ");
	private JTextField save_jtf = new JTextField();
	private JButton jbt_save = new JButton("����");
	private JButton jbt_go = new JButton(" ���� ");
	
	String sourceDir;
	String destDir;

	public StartFrame(){
		super("Program");
		
		setBounds(200, 200, 450, 148);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		setVisible(true);
		
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		open_jlb.setBounds(12, 9, 96, 23);
		open_jlb.setFont(new Font("����", Font.PLAIN, 12));
		getContentPane().add(open_jlb);
		
		jbt_open.setBounds(367, 9, 57, 23);
		jbt_open.setFont(new Font("����", Font.PLAIN, 12));
		getContentPane().add(jbt_open);
		
		save_jlb.setBounds(12, 34, 148, 27);
		save_jlb.setFont(new Font("����", Font.PLAIN, 12));
		getContentPane().add(save_jlb);

		jbt_save.setBounds(367, 36, 57, 23);
		jbt_save.setFont(new Font("����", Font.PLAIN, 12));
		getContentPane().add(jbt_save);
		
		jbt_go.setBounds(349, 76, 75, 23);
		jbt_go.setFont(new Font("����", Font.PLAIN, 12));
		getContentPane().add(jbt_go);
		
		open_jtf.setBounds(110, 10, 245, 21);
		getContentPane().add(open_jtf);
		open_jtf.setColumns(10);
		open_jtf.setEditable(false);
		
		save_jtf.setBounds(162, 37, 193, 21);
		getContentPane().add(save_jtf);
		save_jtf.setColumns(10);
		save_jtf.setEditable(false);
		
		jbt_open.addActionListener(this);
		jbt_save.addActionListener(this);
		jbt_go.addActionListener(this);
		
		sourceDir = null;
		destDir = null;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource() == jbt_open){
			if(jfc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION ){
				sourceDir = jfc.getSelectedFile().toString();
				open_jtf.setText(sourceDir);
				System.out.println("sourceDir: " + sourceDir);
			}
			
		}else if(arg0.getSource() == jbt_save){
			if(jfc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION ){
				destDir = jfc.getSelectedFile().toString();
				save_jtf.setText(destDir);
				System.out.println("destDir: " + destDir);
			}
		}
		else if(arg0.getSource() == jbt_go){
			if(destDir != null && sourceDir != null){
				if(!destDir.equals(sourceDir)){
					System.out.println("start");
					
					//DO WORK
					new MP3FileClassifier(sourceDir, destDir);
					
					setVisible(false);
				}
				else{
					JDialog error = new JDialog(this, "����");
					JPanel panel = new JPanel();
					JLabel label = new JLabel("�з��� ������ ������ ������ �ٸ��� �������ּ���.");
					
					error.setSize(316,108);
					error.setLocation(200, 150);
					
					label.setFont(new Font("����", Font.PLAIN, 12));
					label.setBounds(12, 10, 276, 50);
					error.add(label);
					error.getContentPane().add(panel, BorderLayout.CENTER);
					
					error.setVisible(true);
					
					System.out.println("destDir == sourceDir");
				}
			}
		}
	}
}
