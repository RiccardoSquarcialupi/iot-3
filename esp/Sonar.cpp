#include "Sonar.h"
#include "Arduino.h"
#include <math.h>

#define TIMEOUT 20000 //microseconds

Sonar::Sonar(int echoPin, int trigPin) {
  this->echoPin = echoPin;
  this->trigPin = trigPin;
  pinMode(echoPin, INPUT);
  pinMode(trigPin, OUTPUT);
}

double Sonar::measureResult(double currentTemp) {
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  //noInterrupts();
  double durata = pulseIn(echoPin, HIGH, TIMEOUT);
  //interrupts();
  //formula per calcolare la distanza in centimetri
  //del suono in funzione della temperatura dell'aria in C°
  //ritorna 0 se pulseIn è andato in timeout
  return durata * ((331.45 + (0.62 * currentTemp)) / 20000);
}
