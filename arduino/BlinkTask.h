#ifndef __BLINKTASK__
#define __BLINKTASK__

#include "Task.h"
#include "Led.h"

class BlinkTask: public Task {
    Light* led;
    enum { ON, OFF } state;
  public:
    void init(int taskPeriod, int basePeriod, Light* led);
    void tick();
};
#endif
