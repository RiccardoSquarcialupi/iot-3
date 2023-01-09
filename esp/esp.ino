#define ECHO D1
#define TRIGGER D2
#define LED D8
#define DIS1 100
#define DIS2 40
#define DELTA 4
#define ALARM_P 1000
#define PRE_P 2000

#include <ArduinoJson.h>
#include <WiFiClient.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include "Sonar.h"
#include "Led.h"
#include "BlinkTask.h"

Sonar sonar(ECHO, TRIGGER);
Led led(LED);
BlinkTask blinkTask;

DynamicJsonDocument json(1024);
char jsonString[100];

enum {NORMALE, ALLARME, PREALLARME} state;
String stateName[3] = {"NORMALE", "ALLARME", "PRE-ALLARME"};

double distance = 0;

char* ssid = "FRITZ!Box 7530 YD";
char* password = "78371902711764011878";
char* serverName = "http://192.168.178.36:8080/api/data/";

void setup() {
  Serial.begin(9600);
  while(!Serial){};
  Serial.println(String("\nConnecting to network ") + ssid);

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println("");
  Serial.println(String("Connected to network ") + ssid);
  // put your setup code here, to run once:
  state = NORMALE;
  blinkTask.init(250, 250, &led);
}

void loop() {
  // put your main code here, to run repeatedly:
  step();
}

void step() {
  sonar.measureResult(24);
  switch (state) {
    case NORMALE:
      normal();
      break;
    case ALLARME:
      alarm();
      break;
    case PREALLARME:
      prealarm();
      break;
  }
}

void normal() {
  led.switchOff();
  if (distance <= DIS2) {
    state = ALLARME;
  } else if (distance <= DIS1) {
    state = PREALLARME;
  }
}

void alarm() {
  led.switchOn();
  sendData();
}

void prealarm() {
  blink();
  sendData();
}

void blink() {
  static unsigned long int tprev = 0;
  int t = millis();
  if (t - tprev >= 250) {
    blinkTask.updateAndCheckTime();
    tprev = t;
  }
}

void sendData() {
  static unsigned long int tprev = 0;

  double period;
  if (state = ALLARME) period = ALARM_P;
  else if (state = PREALLARME) period = PRE_P;

  int t = millis();
  if (t - tprev >= period) {
    populateJson();
    sendPostRequest();

    t = tprev;
  }
}

void sendPostRequest() {
  WiFiClient wifiClient;
  HTTPClient http;
  http.begin(wifiClient, serverName);
  http.addHeader("Content-Type", "application/json");
  Serial.println(http.POST(jsonString));
  http.end();
}

void populateJson() {
  json["valLivello"] = distance;
  json["stato"] = stateName[state];
  json["percApertura"] = getP();

  serializeJson(json, jsonString);
  Serial.println(jsonString);
}

int getP() {
  if (DIS2 - DELTA < distance && distance <= DIS2) {
    return 20;
  } else if (DIS2 - 2 * DELTA < distance && distance <= DIS2 - DELTA) {
    return 40;
  } else if (DIS2 - 3 * DELTA < distance && distance <= DIS2 - 2 * DELTA) {
    return 60;
  } else if (DIS2 - 4 * DELTA < distance && distance <= DIS2 - 3 * DELTA) {
    return 80;
  } else if (distance <= DIS2 - 4 * DELTA) {
    return 100;
  }
}
