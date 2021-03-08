import java.io.*;
import com.fazecast.jSerialComm.*;


public class ReadSerialPort {

    public SerialPort comPort;
    public InputStream in;

    public ReadSerialPort(){
        comPort = SerialPort.getCommPorts()[0];
        comPort.openPort();
        comPort.setBaudRate(115200); // Musi byc takie same jak czestotliwosc wysylania na port
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        in = comPort.getInputStream();
    }

    public String getSensorJson(){
        try
        {
            boolean display = true;
            String result;

            while(display == true) {
                result = "";
                char temp = ' ';

                while(temp != '{')
                    temp = (char)in.read();

                while(temp != '}') {
                    result += temp;
                    temp = (char)in.read();
                }
                result += temp;

                return result;
            }
            in.close();
        } catch (Exception e) { e.printStackTrace();}
        return null;
    }
}