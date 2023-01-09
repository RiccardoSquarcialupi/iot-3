#define PIN_SERVO 5
#define PIN_LED 4
#define PERIOD 250
#define WRITE_PERIOD 1000

#include <Servo.h>
#include <SoftwareSerial.h>
#include "Led.h"
#include "BlinkTask.h"

enum {READ, ALARM, CLOSE, MANUAL} state;

Led* alarmLed;

Servo servo;
BlinkTask blinkTask;
SoftwareSerial bt(2, 3); //rx, tx

String buffer = "";
char terminator = ' ';
bool blinkFlag = false;
bool manualFlag = false;
bool turn = false;

int m = 0;
int p = 0;
int l = 0;
String s = "";

void setup() {
  Serial.begin(9600);
  bt.begin(9600);

  servo.attach(PIN_SERVO);
  setServo(0);
  delay(1000);
  alarmLed = new Led(PIN_LED);
  blinkTask.init(PERIOD, PERIOD, alarmLed);
}

void loop() {
  //Serial.println(state);
  step();
}

void step() {
  blink();
  switch (state) {
    case READ:
      read();
      parse();
      write();
      break;
    case ALARM:
      alarm();
      break;
    case CLOSE:
      close();
      break;
    case MANUAL:
      manual();
      break;
  }
}

//------STATI------//
void read() {
  // leggo tutti i caratteri che mi arrivano in sequenza e li appendo fino al
  // terminatore escluso
  bool end = false;
  if (Serial.available() > 0) {
    Serial.flush();
    buffer.concat(Serial.readStringUntil(terminator));
    /*while(!end) {
      if((char)(Serial.peek()) == terminator) {
        Serial.read();
        end = true;
      } else if(Serial.available() > 0) {
    	  buffer.concat((char)(Serial.read()));
      }
      }*/
    //Serial.print("buffer: ");
    //Serial.println(buffer);
  } else if (bt.available() > 0) {
    /*while(bt.available() > 0 && !end) {
      if(bt.peek() == terminator) {
        bt.read();
        end = true;
      } else {
    	  buffer.concat((char)(bt.read()));
      }
      }*/
    bt.flush();
    buffer.concat(bt.readStringUntil(terminator));
  }

}

void parse() {
  if (buffer.length() > 0) {
    if (buffer[0] == 'p') {
      p = buffer.substring(1).toInt();
      Serial.println(p);
    } else if (buffer[0] == 'l') {
      l = buffer.substring(1).toInt();
      Serial.println(l);
    } else if (buffer[0] == 's') {
      s = buffer.substring(1);
      Serial.println(s);
    } else if (buffer[0] == 'm') {
      m = buffer.substring(1).toInt();
    } else if (buffer[0] == 'M' && p > 0) {
      manualFlag = true;
    } else if (buffer[0] == 'A' && manualFlag) {
      manualFlag = false;
    }
    buffer = "";
  }

  if (p > 0 && !manualFlag) {
    state = ALARM;
  } else if (p == 0) {
    state = CLOSE;
  } else if (manualFlag) {
    state = MANUAL;
  }
}

void write() {
  static unsigned long int tprev = 0;
  int t = millis();
  if (t - tprev >= WRITE_PERIOD) {
    String tmp = String(s + " " + String(l) + " " + String(p));
    //bt.print(tmp);
    tprev = t;
  }

  if (manualFlag) {
    Serial.write("MANUALE");
  }
}

void alarm() {
  setServo(p);
  blinkFlag = true;

  state = READ;
}

void close() {
  setServo(p);
  blinkFlag = false;
  manualFlag = false;
  alarmLed->switchOff();

  state = READ;
}

void manual() {
  blinkFlag = false;
  alarmLed->switchOn();

  setServo(m);

  state = READ;
}
//----------------------------------------//
void blink() {
  static unsigned long int tprev = 0;
  if (blinkFlag) {
    int t = millis();
    if (t - tprev >= PERIOD) {
      blinkTask.updateAndCheckTime();
      tprev = t;
    }
  }
}

void setServo(int value) {
  int tmp = map(value, 0, 100, 5, 175);
  if (servo.read() != tmp) {
    servo.write(tmp);
  }
}
