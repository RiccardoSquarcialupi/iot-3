#include "BlinkTask.h"

void BlinkTask::init(int taskPeriod, int basePeriod, Light* led) {
  Task::init(taskPeriod, basePeriod);
  this->led = led;
  state = OFF;
}

void BlinkTask::tick() {
  switch (state) {
    case OFF:
      led->switchOn();
      state = ON;
      break;
    case ON:
      led->switchOff();
      state = OFF;
      break;
  }
}
