#ifndef __SONAR__
#define __SONAR__

class Sonar {
  public:
    Sonar(int echoPin, int trigPin);
    double measureResult(double currentTemp);
  private:
    int echoPin;
    int trigPin;
};

#endif
