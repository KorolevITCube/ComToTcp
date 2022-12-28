package nlmk.com;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ComDriver {
    private static final Logger log = LoggerFactory.getLogger(ComDriver.class);

    // поле, хранящее скорость общения
    private int baudRate = 9600;

    // размер посылки для общения в битах
    private final int dataBits = 8;

    // количество стоп битов
    private final int stopBits = 1;

    // использовать четность или нет
    private final int parity = 0;

    // название порта
    private String portDescriptor = "COM9";

    // тайм-аут чтения
    private final int readTimeOut = 5000;

    // тайм-аут записи
    private final int writeTimeOut = 1000;

    // порт
    private SerialPort serialPort;

    // InputStream
    private InputStream is;

    // OutputStream
    private OutputStream os;

    private static ComDriver instance;

    public static ComDriver getInstance(){
        if(instance == null){
            instance = new ComDriver();
        }
        return instance;
    }

    private ComDriver() {
        try {
            openPort();
        }catch(Exception e){
            log.error("Cant open port "+e.getMessage());
        }
    }

    private synchronized boolean openPort() throws IOException {
        if (baudRate != 1200 && baudRate != 2400 && baudRate != 4800 && baudRate != 9600) {
            throw new RuntimeException("BaudRate is invalid. It must be one of the values: 1200, 2400, 4800, 9600");
        }
        serialPort = SerialPort.getCommPort(portDescriptor);
        serialPort.setComPortParameters(baudRate, dataBits, stopBits, parity);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, readTimeOut, writeTimeOut);
        if (serialPort.openPort()) {
            log.info("Serial port successfully opened with params: baudrate: "+baudRate+" data bits: "+dataBits+" stop bits: "+stopBits+" parity: "+parity);
            return true;
        } else {
            log.error("Can't open serial port");
            throw new IOException("Serial port cannot be opened");
        }
    }

    public synchronized void writeRequest(byte[] request) throws IOException {
        if (serialPort.isOpen()) {
            getOutputStream().write(request, 0, request.length);
            log.info("Request was send: "+request.toString());
        } else {
            log.error("Can't send request, serial port was unexpectedly closed");
            throw new IOException("the serial port was unexpectedly closed");
        }
    }

    public synchronized byte[] readResponse() throws IOException {
        try {
            InputStream is = getInputStream();
            byte[] buffer = new byte[256];
            byte[] result;
            Arrays.fill(buffer, (byte) -1);
            int counter = 0;
            byte current;
            do {
                current = (byte) is.read();
                buffer[counter] = current;
                counter++;
            } while (current != 0x03);
            result = Arrays.copyOfRange(buffer, 0, counter);
            log.info("received array: "+result.toString());
            return result;
        } catch (Exception e) {
            log.error("Cant read data from input stream" + e.getCause());
            throw new IOException("Cant read data from input stream", e.getCause());
        }
    }

    private InputStream getInputStream() throws IOException {
        if(this.serialPort.isOpen()){
            if(this.is == null){
                this.is = this.serialPort.getInputStream();
            }
        }else{
            try {
                serialPort.openPort();
                this.is = this.serialPort.getInputStream();
            }catch(Exception e){
                log.error("The port was unexpectedly closed. The port could not be reopened." + e.getCause());
                throw new IOException("The port was unexpectedly closed. The port could not be reopened.",e.getCause());
            }
        }
        return this.is;
    }

    private OutputStream getOutputStream() throws IOException {
        if (this.serialPort.isOpen()) {
            if (this.os == null) {
                this.os = this.serialPort.getOutputStream();
            }
        } else {
            try {
                serialPort.openPort();
                this.os = this.serialPort.getOutputStream();
            } catch (Exception e) {
                log.error("The port was unexpectedly closed. The port could not be reopened." +
                        e.getCause());
                throw new IOException(
                        "The port was unexpectedly closed. The port could not be reopened.",
                        e.getCause()
                );
            }
        }
        return this.os;
    }
}
