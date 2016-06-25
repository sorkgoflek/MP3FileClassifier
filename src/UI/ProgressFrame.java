package UI;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;


@SuppressWarnings("serial")
public class ProgressFrame extends JFrame implements ActionListener{
	final int ING = 0;
	final int FINISH = 1;
	
	JProgressBar pro;
	JLabel ing_jlb;
	JButton stop_btn;
	
	ProgressThread t;
	
	int status;
	
	static public int N_finishedFile;
	int N_file;
	
	public ProgressFrame(int n_f){
		super("Program");
		pro = new JProgressBar(0,100);
		pro.setBounds(12, 28, 360, 14);
		pro.setForeground(Color.BLUE);
		pro.setStringPainted(true);
		
		ing_jlb = new JLabel("진행중...");
		ing_jlb.setBounds(12, 10, 84, 15);
		ing_jlb.setFont(new Font("돋움", Font.PLAIN, 12));
		
		stop_btn = new JButton("중지");
		stop_btn.setBounds(304, 54, 68, 23);
		stop_btn.setFont(new Font("돋움", Font.PLAIN, 12));
		stop_btn.addActionListener(this);
		
		setBounds(250, 250, 400, 126);
		getContentPane().setLayout(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().add(pro);
		getContentPane().add(ing_jlb);
		getContentPane().add(stop_btn);
		setVisible(true);

		N_finishedFile = 0;
		N_file = n_f;
	}
	
	public void start(){
		setVisible(true);
		
		t = new ProgressThread();
		t.start();
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == stop_btn){
			if(status == ING){
				//한번 물어봐야함
				System.exit(ABORT);
			}
			else if(status == FINISH){
				System.exit(NORMAL);
			}
		}
	}
	
	class ProgressThread extends Thread{
		
		public void run(){
			status = ING;
			
			int per = 0;
			int prevN_finishedFile = 0;
			
			while(per < 100){
				if(prevN_finishedFile < N_finishedFile){
					prevN_finishedFile = N_finishedFile;
					per = N_finishedFile*100 / N_file;
					
					if(per > 100)
						per=100;
					
					pro.setValue(per);
					pro.setString(per + "%");
				}
				else{
					try {
						sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			setEnabled(false);

			ing_jlb.setText("진행중...");
			stop_btn.setText("확인");
			status = FINISH;
			
			System.out.println("ProgressFrame 종료");
		}
	}
	
}
