package timer.timer;
import java.awt.Toolkit;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import timer.timer.HttpClient;
/*
 * Qual é a conversão entre Milissegundos e Segundos
Para saber o valor da conversão de Milissegundos para Segundos, 
pode usar esta fórmula simples e fácil Segundos = Milissegundos*0.001

Rodar a cada 10 minutos = 60*10 = 600 /0.001 = 600000 milissegundos
 */
public class Scheduling {
	Toolkit toolkit;
	Timer timer;
	static int i=0;
	public Scheduling() {
		toolkit = Toolkit.getDefaultToolkit();
		timer = new Timer();
		timer.schedule(new RemindTask(),
				0,        //initial delay
				1*600000);//10 minutos
		//1*15000);  //subsequent rate
	}

	class RemindTask extends TimerTask {
		int numWarningBeeps = 3;
		public void run() {
			HttpClient httpClient = new HttpClient();	
			try {
				i+=1;
				System.out.println(getTime() + " Execucao numero: " + i);
				httpClient.sendGet();
				//httpClient.readJson();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*if (numWarningBeeps > 0) {
                toolkit.beep();
                System.out.println("Beep!");
                numWarningBeeps--;
            } else {
                toolkit.beep(); 
                System.out.println("Time's up!");
                //timer.cancel(); // Not necessary because
                                  // we call System.exit
                System.exit(0);   // Stops the AWT thread 
                                  // (and everything else)
            }*/
		}
	}


	public static void main(String args[]) {
		new Scheduling();
		//System.out.println(getTime() + " Execucao numero: " + i);
	}


	private static String getTime(){
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		Date date = new Date(); 
		return dateFormat.format(date); 	
	}
}